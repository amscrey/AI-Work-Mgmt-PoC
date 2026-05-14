package com.aiworkflow.workmanagement.orchestration.domain;

/**
 * Response wrapper for LLM chat calls.
 */
public class LlmChatResponse {
    private final String text;
    private final String provider;
    private final String model;
    private final LlmUsage usage;

    public LlmChatResponse(String text, String provider, String model, LlmUsage usage) {
        this.text = text;
        this.provider = provider;
        this.model = model;
        this.usage = usage;
    }

    public String getText() {
        return text;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public LlmUsage getUsage() {
        return usage;
    }
}

