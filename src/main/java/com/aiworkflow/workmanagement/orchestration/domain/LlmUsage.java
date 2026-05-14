package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.Map;

/**
 * Usage details returned from an LLM call.
 */
public class LlmUsage {
    private final long promptTokens;
    private final long completionTokens;
    private final long totalTokens;
    private final long latencyMs;
    private final boolean estimated;
    private final Map<String, Object> rawMetadata;

    public LlmUsage(
        long promptTokens,
        long completionTokens,
        long totalTokens,
        long latencyMs,
        boolean estimated,
        Map<String, Object> rawMetadata
    ) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.latencyMs = latencyMs;
        this.estimated = estimated;
        this.rawMetadata = rawMetadata;
    }

    public long getPromptTokens() {
        return promptTokens;
    }

    public long getCompletionTokens() {
        return completionTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public boolean isEstimated() {
        return estimated;
    }

    public Map<String, Object> getRawMetadata() {
        return rawMetadata;
    }
}
