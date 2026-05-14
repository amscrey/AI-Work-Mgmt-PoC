package com.aiworkflow.workmanagement.orchestration.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Persisted usage record for a single LLM call.
 */
public class LlmUsageRecord {
    private final String storyId;
    private final String executionId;
    private final String provider;
    private final String model;
    private final long promptTokens;
    private final long completionTokens;
    private final long totalTokens;
    private final long latencyMs;
    private final boolean estimated;
    private final Instant timestamp;
    private final Map<String, Object> rawMetadata;

    @JsonCreator
    public LlmUsageRecord(
        @JsonProperty("storyId") String storyId,
        @JsonProperty("executionId") String executionId,
        @JsonProperty("provider") String provider,
        @JsonProperty("model") String model,
        @JsonProperty("promptTokens") long promptTokens,
        @JsonProperty("completionTokens") long completionTokens,
        @JsonProperty("totalTokens") long totalTokens,
        @JsonProperty("latencyMs") long latencyMs,
        @JsonProperty("estimated") boolean estimated,
        @JsonProperty("timestamp") Instant timestamp,
        @JsonProperty("rawMetadata") Map<String, Object> rawMetadata
    ) {
        this.storyId = storyId;
        this.executionId = executionId;
        this.provider = provider;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.latencyMs = latencyMs;
        this.estimated = estimated;
        this.timestamp = timestamp;
        this.rawMetadata = rawMetadata;
    }

    public String getStoryId() {
        return storyId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getRawMetadata() {
        return rawMetadata;
    }
}
