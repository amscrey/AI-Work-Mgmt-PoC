package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;

/**
 * Stubbed LLM client for configured providers without real API calls.
 */
public class StubbedProviderChatClient implements LLMChatClient {

    private final String providerName;
    private final String model;

    public StubbedProviderChatClient(String providerName, String model) {
        this.providerName = providerName;
        this.model = model;
    }

    @Override
    public LlmChatResponse generate(String roleName, String prompt) {
        String text = "stubbed-response:" + providerName + ":" + (model != null ? model : "default");
        return new LlmChatResponse(text, providerName, model, null);
    }
}
