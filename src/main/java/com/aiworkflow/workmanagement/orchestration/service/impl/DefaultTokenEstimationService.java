package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.config.OrchestrationLlmProperties;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsage;
import com.aiworkflow.workmanagement.orchestration.service.TokenEstimationService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Default tokenizer-based estimation service.
 */
@Service
public class DefaultTokenEstimationService implements TokenEstimationService {

    private static final String DEFAULT_TOKENIZER_ID = "simple-whitespace";
    private static final String DEFAULT_TOKENIZER_VERSION = "v1";

    private final OrchestrationLlmProperties properties;

    public DefaultTokenEstimationService(OrchestrationLlmProperties properties) {
        this.properties = properties;
    }

    @Override
    public LlmUsage estimate(String provider, String model, String prompt, String responseText) {
        long promptTokens = estimateTokens(prompt);
        long completionTokens = estimateTokens(responseText);
        long totalTokens = promptTokens + completionTokens;

        String tokenizerId = DEFAULT_TOKENIZER_ID;
        String tokenizerVersion = DEFAULT_TOKENIZER_VERSION;
        if (provider != null && properties.getProviders().containsKey(provider)) {
            OrchestrationLlmProperties.Provider providerConfig = properties.getProviders().get(provider);
            if (providerConfig.getTokenizerId() != null && !providerConfig.getTokenizerId().isBlank()) {
                tokenizerId = providerConfig.getTokenizerId();
            }
            if (providerConfig.getTokenizerVersion() != null && !providerConfig.getTokenizerVersion().isBlank()) {
                tokenizerVersion = providerConfig.getTokenizerVersion();
            }
        }

        Map<String, Object> rawMetadata = new HashMap<>();
        rawMetadata.put("tokenizerId", tokenizerId);
        rawMetadata.put("tokenizerVersion", tokenizerVersion);
        rawMetadata.put("contextTokens", promptTokens);

        return new LlmUsage(promptTokens, completionTokens, totalTokens, 0L, true, rawMetadata);
    }

    private long estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0L;
        }
        String[] parts = text.trim().split("\\s+");
        return parts.length;
    }
}

