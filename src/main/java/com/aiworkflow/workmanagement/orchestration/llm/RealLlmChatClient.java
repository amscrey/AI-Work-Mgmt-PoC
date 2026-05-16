package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that wraps a real LangChain4j ChatModel for actual LLM API calls.
 * This is used when API keys are provided (vs stubbed clients when keys are missing).
 */
public class RealLlmChatClient implements LLMChatClient {

    private static final Logger logger = LoggerFactory.getLogger(RealLlmChatClient.class);

    private final String providerName;
    private final String modelName;
    private final ChatModel chatModel;

    public RealLlmChatClient(String providerName, String modelName, ChatModel chatModel) {
        this.providerName = providerName;
        this.modelName = modelName;
        this.chatModel = chatModel;
    }

    @Override
    public LlmChatResponse generate(String roleName, String prompt) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Making LLM API call: provider={}, model={}, role={}, chatModel={}",
                providerName, modelName, roleName, chatModel.getClass().getName());

            // LangChain4j 1.x API: chat() instead of generate()
            ChatResponse response = chatModel.chat(UserMessage.from(prompt));
            long endTime = System.currentTimeMillis();

            // Extract content from AiMessage
            String content = "";
            if (response.aiMessage() != null) {
                content = response.aiMessage().text();
            }

            // Extract token usage if available
            LlmUsage usage = null;
            if (response.tokenUsage() != null) {
                long promptTokens = response.tokenUsage().inputTokenCount();
                long completionTokens = response.tokenUsage().outputTokenCount();
                long totalTokens = promptTokens + completionTokens;
                long latencyMs = endTime - startTime;
                usage = new LlmUsage(promptTokens, completionTokens, totalTokens, latencyMs, false, null);
            }

            logger.debug("LLM API call succeeded: provider={}, model={}, latencyMs={}",
                providerName, modelName, endTime - startTime);

            return new LlmChatResponse(content, providerName, modelName, usage);
        } catch (Exception e) {
            logger.error("LLM API call failed: provider={}, model={}, chatModel={}, error={}",
                providerName, modelName, chatModel.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("LLM API call failed for provider=" + providerName + ", model=" + modelName, e);
        }
    }
}
