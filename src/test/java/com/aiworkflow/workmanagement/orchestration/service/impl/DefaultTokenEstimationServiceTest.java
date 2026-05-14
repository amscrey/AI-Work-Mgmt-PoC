package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.config.OrchestrationLlmProperties;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultTokenEstimationServiceTest {

    @Test
    void estimatesTokensAndIncludesTokenizerMetadata() {
        OrchestrationLlmProperties properties = new OrchestrationLlmProperties();
        OrchestrationLlmProperties.Provider provider = new OrchestrationLlmProperties.Provider();
        provider.setTokenizerId("test-tokenizer");
        provider.setTokenizerVersion("v2");
        properties.setProviders(Map.of("gemini", provider));

        DefaultTokenEstimationService service = new DefaultTokenEstimationService(properties);
        LlmUsage usage = service.estimate("gemini", "gemini-2.0-flash", "hello world", "ok");

        assertThat(usage.isEstimated()).isTrue();
        assertThat(usage.getPromptTokens()).isEqualTo(2);
        assertThat(usage.getCompletionTokens()).isEqualTo(1);
        assertThat(usage.getRawMetadata()).containsEntry("tokenizerId", "test-tokenizer");
        assertThat(usage.getRawMetadata()).containsEntry("tokenizerVersion", "v2");
    }

    @Test
    void usesDefaultTokenizerMetadataWhenProviderMissing() {
        OrchestrationLlmProperties properties = new OrchestrationLlmProperties();
        DefaultTokenEstimationService service = new DefaultTokenEstimationService(properties);

        LlmUsage usage = service.estimate(null, null, "one two three", "ok");

        assertThat(usage.isEstimated()).isTrue();
        assertThat(usage.getPromptTokens()).isEqualTo(3);
        assertThat(usage.getRawMetadata()).containsEntry("tokenizerId", "simple-whitespace");
        assertThat(usage.getRawMetadata()).containsEntry("tokenizerVersion", "v1");
        assertThat(usage.getRawMetadata()).containsEntry("contextTokens", 3L);
    }
}
