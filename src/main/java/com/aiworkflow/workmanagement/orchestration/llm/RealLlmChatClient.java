package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;

/**
 * Adapter that wraps a real LangChain4j ChatLanguageModel for actual LLM API calls.
 * This is used when API keys are provided (vs stubbed clients when keys are missing).
 */
public class RealLlmChatClient implements LLMChatClient {

    private final String providerName;
    private final String modelName;
    private final ChatLanguageModel chatModel;

    public RealLlmChatClient(String providerName, String modelName, ChatLanguageModel chatModel) {
        this.providerName = providerName;
        this.modelName = modelName;
        this.chatModel = chatModel;
    }

    @Override
    public LlmChatResponse generate(String roleName, String prompt) {
        long startTime = System.currentTimeMillis();
        try {
            // Use the UserMessage API to get response with token usage metadata
            Response<AiMessage> response = chatModel.generate(UserMessage.from(prompt));
            long endTime = System.currentTimeMillis();

            // Extract content
            String content = "";
            if (response.content() != null) {
                content = response.content().text();
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

            return new LlmChatResponse(content, providerName, modelName, usage);
        } catch (Exception e) {
            throw new RuntimeException("LLM API call failed for provider=" + providerName + ", model=" + modelName, e);
        }
    }
}
