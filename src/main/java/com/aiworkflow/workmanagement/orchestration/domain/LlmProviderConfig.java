package com.aiworkflow.workmanagement.orchestration.domain;

/**
 * Normalized LLM provider configuration.
 */
public class LlmProviderConfig {
    private final String name;
    private final String apiKey;
    private final String model;
    private final boolean enabled;
    private final boolean dummy;

    public LlmProviderConfig(String name, String apiKey, String model, boolean enabled, boolean dummy) {
        this.name = name;
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled;
        this.dummy = dummy;
    }

    public String getName() {
        return name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDummy() {
        return dummy;
    }
}

