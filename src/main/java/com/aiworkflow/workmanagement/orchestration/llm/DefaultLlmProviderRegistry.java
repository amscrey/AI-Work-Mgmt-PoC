package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default registry that builds LLM clients from configuration.
 */
@Service
public class DefaultLlmProviderRegistry implements LlmProviderRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLlmProviderRegistry.class);

    private final Map<String, LLMChatClient> clients;

    public DefaultLlmProviderRegistry(LlmConfigLoader configLoader) {
        this.clients = Collections.unmodifiableMap(buildClients(configLoader.load()));
    }

    @Override
    public LLMChatClient getClient(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return null;
        }
        return clients.get(providerName);
    }

    @Override
    public Map<String, LLMChatClient> getAllClients() {
        return clients;
    }

    private Map<String, LLMChatClient> buildClients(LlmConfigSnapshot snapshot) {
        Map<String, LLMChatClient> map = new HashMap<>();
        for (LlmProviderConfig provider : snapshot.getProviders().values()) {
            if (!provider.isEnabled()) {
                logger.warn("LLM provider '{}' is disabled", provider.getName());
                continue;
            }

            // Dummy providers (file-bridge, clipboard, etc.)
            if (provider.isDummy()) {
                logger.info("Registering dummy LLM client: provider={}, mode={}", provider.getName(), provider.getModel());
                map.put(provider.getName(), new DummyChatClient(provider.getName(), provider.getModel()));
                continue;
            }

            // Real LLM providers
            if (provider.getApiKey() == null || provider.getApiKey().isBlank()) {
                logger.warn("LLM provider '{}' missing API key; using stubbed client (returns fake responses)", provider.getName());
                map.put(provider.getName(), new StubbedProviderChatClient(provider.getName(), provider.getModel()));
                continue;
            }

            // Create real LLM client based on provider name
            try {
                ChatModel chatModel = createChatModel(provider);
                String maskedKey = maskApiKey(provider.getApiKey());
                logger.info("Registered REAL LLM client: provider={}, model={}, baseUrl={}, apiKey={}",
                    provider.getName(), provider.getModel(),
                    provider.getBaseUrl() != null ? provider.getBaseUrl() : "default",
                    maskedKey);
                map.put(provider.getName(), new RealLlmChatClient(provider.getName(), provider.getModel(), chatModel));
            } catch (Exception e) {
                logger.error("Failed to create real LLM client for provider '{}'; falling back to stubbed client. Error: {}",
                    provider.getName(), e.getMessage());
                map.put(provider.getName(), new StubbedProviderChatClient(provider.getName(), provider.getModel()));
            }
        }
        return map;
    }

    /**
     * Masks an API key for safe logging.
     * Shows first 5 characters and last 4 characters, masks the rest.
     * Example: "sk-ant-api03-1234567890abcdef" → "sk-an...cdef"
     *
     * @param apiKey the API key to mask
     * @return masked key safe for logging
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "[MISSING]";
        }
        if (apiKey.length() <= 9) {
            // Too short to mask safely, just show prefix
            return apiKey.substring(0, Math.min(5, apiKey.length())) + "...";
        }
        // Show first 5 chars, last 4 chars
        String prefix = apiKey.substring(0, 5);
        String suffix = apiKey.substring(apiKey.length() - 4);
        return prefix + "..." + suffix;
    }

    private ChatModel createChatModel(LlmProviderConfig provider) {
        String providerName = provider.getName().toLowerCase();

        switch (providerName) {
            case "anthropic":
                return createAnthropicModel(provider);
            case "gemini":
                return createGeminiModel(provider);
            case "groq":
                return createGroqModel(provider);
            default:
                throw new IllegalArgumentException("Unknown LLM provider: " + provider.getName() +
                    ". Supported: anthropic, gemini, groq");
        }
    }

    private ChatModel createAnthropicModel(LlmProviderConfig provider) {
        AnthropicChatModel.AnthropicChatModelBuilder builder = AnthropicChatModel.builder()
            .apiKey(provider.getApiKey())
            .modelName(provider.getModel())
            .timeout(Duration.ofMinutes(5));

        // Support custom base URL (for corporate proxies)
        if (provider.getBaseUrl() != null && !provider.getBaseUrl().isBlank()) {
            builder.baseUrl(provider.getBaseUrl());
        }

        return builder.build();
    }

    private ChatModel createGeminiModel(LlmProviderConfig provider) {
        // Gemini model creation requires langchain4j-google-ai-gemini dependency
        // For now, throw exception if Gemini is requested without the dependency
        try {
            Class<?> geminiClass = Class.forName("dev.langchain4j.model.googleai.GoogleAiGeminiChatModel");
            Object builder = geminiClass.getMethod("builder").invoke(null);

            builder.getClass().getMethod("apiKey", String.class).invoke(builder, provider.getApiKey());
            builder.getClass().getMethod("modelName", String.class).invoke(builder, provider.getModel());
            builder.getClass().getMethod("timeout", Duration.class).invoke(builder, Duration.ofMinutes(5));

            if (provider.getBaseUrl() != null && !provider.getBaseUrl().isBlank()) {
                builder.getClass().getMethod("baseUrl", String.class).invoke(builder, provider.getBaseUrl());
            }

            return (ChatModel) builder.getClass().getMethod("build").invoke(builder);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Gemini provider requires langchain4j-google-ai-gemini dependency. " +
                "Add it to pom.xml or run with -Pllm-providers profile.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Gemini chat model", e);
        }
    }

    private ChatModel createGroqModel(LlmProviderConfig provider) {
        // Groq uses OpenAI-compatible API, so we can use the OpenAI client
        try {
            Class<?> openAiClass = Class.forName("dev.langchain4j.model.openai.OpenAiChatModel");
            Object builder = openAiClass.getMethod("builder").invoke(null);

            builder.getClass().getMethod("apiKey", String.class).invoke(builder, provider.getApiKey());
            builder.getClass().getMethod("modelName", String.class).invoke(builder, provider.getModel());
            builder.getClass().getMethod("timeout", Duration.class).invoke(builder, Duration.ofMinutes(5));

            // Groq requires custom base URL
            String baseUrl = provider.getBaseUrl() != null && !provider.getBaseUrl().isBlank()
                ? provider.getBaseUrl()
                : "https://api.groq.com/openai/v1";
            builder.getClass().getMethod("baseUrl", String.class).invoke(builder, baseUrl);

            return (ChatModel) builder.getClass().getMethod("build").invoke(builder);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Groq provider requires langchain4j-open-ai dependency. " +
                "Add it to pom.xml or run with -Pllm-providers profile.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Groq chat model", e);
        }
    }
}
