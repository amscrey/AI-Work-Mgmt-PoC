package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
                // For now we are not constructing a real LLM client b/c we current do not want to use real LLMS.
                // TODO - consider whether we want to include disabled providers in the registry with a client that throws if used, to catch misconfiguration issues earlier
                // TODO - consider having a "disabled" client that throws if used, to catch misconfiguration issues earlier
                logger.warn("LLM provider '{}' is disabled", provider.getName());
                continue;
            }
            if (provider.isDummy()) {
                map.put(provider.getName(), new DummyChatClient(provider.getName(), provider.getModel()));
                continue;
            }
            if (provider.getApiKey() == null || provider.getApiKey().isBlank()) {
                logger.warn("LLM provider '{}' missing API key; using stub client", provider.getName());
            }
            map.put(provider.getName(), new StubbedProviderChatClient(provider.getName(), provider.getModel()));
        }
        return map;
    }
}
