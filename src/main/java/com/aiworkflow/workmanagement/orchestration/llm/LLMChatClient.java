package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;

/**
 * Adapter interface for LLM chat clients.
 *
 * This is intentionally minimal to keep LLM node implementations decoupled
 * from concrete client APIs. The default implementation wraps LangChain4j's
 * ChatLanguageModel.
 */
public interface LLMChatClient {

    LlmChatResponse generate(String roleName, String prompt);
}
