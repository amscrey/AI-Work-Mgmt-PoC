package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Adapter that invokes a LangChain4j ChatLanguageModel using reflection to avoid
 * hard coupling to specific API signatures.
 */
@Component
@ConditionalOnProperty(prefix = "orchestration", name = "execution-mode", havingValue = "llm")
public class LangChain4jChatClient implements LLMChatClient {

    private final Object chatLanguageModel;

    public LangChain4jChatClient(@Qualifier("chatLanguageModel") Object chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public LlmChatResponse generate(String roleName, String prompt) {
        try {
            Method generate = chatLanguageModel.getClass().getMethod("generate", String.class);
            Object result = generate.invoke(chatLanguageModel, prompt);
            if (result instanceof String) {
                return new LlmChatResponse((String) result, "langchain4j", null, null);
            }
            return new LlmChatResponse(extractText(result), "langchain4j", null, null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke ChatLanguageModel.generate", e);
        }
    }

    private String extractText(Object result) throws Exception {
        if (result == null) {
            return "";
        }
        for (String methodName : new String[]{"text", "content", "getText", "getContent"}) {
            try {
                Method method = result.getClass().getMethod(methodName);
                Object value = method.invoke(result);
                if (value instanceof String) {
                    return (String) value;
                }
            } catch (NoSuchMethodException ignored) {
                // try next method name
            }
        }
        return result.toString();
    }
}
