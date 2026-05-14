package com.aiworkflow.workmanagement.orchestration.llm;

import java.util.Map;

/**
 * Registry for LLM provider chat clients.
 */
public interface LlmProviderRegistry {

    LLMChatClient getClient(String providerName);

    Map<String, LLMChatClient> getAllClients();
}

