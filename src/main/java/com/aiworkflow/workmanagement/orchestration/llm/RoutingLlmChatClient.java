package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Routes LLM calls to provider clients based on role mappings.
 */
@Component
@Primary
public class RoutingLlmChatClient implements LLMChatClient {

    private static final Logger logger = LoggerFactory.getLogger(RoutingLlmChatClient.class);

    private final LlmProviderRegistry providerRegistry;
    private final LlmRoleMappingResolver roleMappingResolver;

    public RoutingLlmChatClient(LlmProviderRegistry providerRegistry, LlmRoleMappingResolver roleMappingResolver) {
        this.providerRegistry = providerRegistry;
        this.roleMappingResolver = roleMappingResolver;
    }

    @Override
    public LlmChatResponse generate(String roleName, String prompt) {
        LlmRoleResolution resolution = roleMappingResolver.resolve(roleName);
        LLMChatClient client = providerRegistry.getClient(resolution.getProvider());
        if (client != null) {
            LlmChatResponse response = client.generate(roleName, prompt);
            return enrichResponse(response, resolution);
        }

        for (String fallback : resolution.getFallbackProviders()) {
            LLMChatClient fallbackClient = providerRegistry.getClient(fallback);
            if (fallbackClient != null) {
                LlmChatResponse response = fallbackClient.generate(roleName, prompt);
                return enrichResponse(response, resolution);
            }
        }

        logger.warn("No LLM provider resolved for role '{}'", roleName);
        return new LlmChatResponse("", resolution.getProvider(), resolution.getModel(), null);
    }

    private LlmChatResponse enrichResponse(LlmChatResponse response, LlmRoleResolution resolution) {
        if (response == null) {
            return new LlmChatResponse("", resolution.getProvider(), resolution.getModel(), null);
        }
        String provider = response.getProvider() != null ? response.getProvider() : resolution.getProvider();
        String model = response.getModel() != null ? response.getModel() : resolution.getModel();
        return new LlmChatResponse(response.getText(), provider, model, response.getUsage());
    }
}
