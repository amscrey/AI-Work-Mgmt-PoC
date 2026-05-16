package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RealLlmChatClient Unit Tests")
class RealLlmChatClientTest {

    @Mock
    private ChatModel mockChatModel;

    private RealLlmChatClient client;

    private static final String PROVIDER_NAME = "anthropic";
    private static final String MODEL_NAME = "claude-sonnet-4-5-20250514";
    private static final String ROLE_NAME = "researcher";
    private static final String PROMPT = "Analyze the requirements and provide recommendations.";

    @BeforeEach
    void setUp() {
        client = new RealLlmChatClient(PROVIDER_NAME, MODEL_NAME, mockChatModel);
    }

    @Nested
    @DisplayName("Successful Response Generation")
    class SuccessfulResponseTests {

        @Test
        @DisplayName("Should generate response with valid input")
        void shouldGenerateResponseWithValidInput() {
            // Given: Mock response with content
            AiMessage aiMessage = AiMessage.from("Here is my analysis of the requirements...");
            ChatResponse mockResponse = ChatResponse.builder()
                .aiMessage(aiMessage)
                .build();
            when(mockChatModel.chat(any(UserMessage.class))).thenReturn(mockResponse);

            // When: Generate response
            LlmChatResponse response = client.generate(ROLE_NAME, PROMPT);

            // Then: Response is populated correctly
            assertThat(response).isNotNull();
            assertThat(response.getText()).isEqualTo("Here is my analysis of the requirements...");
            assertThat(response.getProvider()).isEqualTo(PROVIDER_NAME);
            assertThat(response.getModel()).isEqualTo(MODEL_NAME);

            // Verify UserMessage was created correctly
            ArgumentCaptor<UserMessage> messageCaptor = ArgumentCaptor.forClass(UserMessage.class);
            verify(mockChatModel).chat(messageCaptor.capture());
            assertThat(messageCaptor.getValue().singleText()).isEqualTo(PROMPT);
        }

        @Test
        @DisplayName("Should extract token usage when available")
        void shouldExtractTokenUsageWhenAvailable() {
            // Given: Mock response with token usage
            AiMessage aiMessage = AiMessage.from("Response text");
            TokenUsage tokenUsage = new TokenUsage(150, 75);
            ChatResponse mockResponse = ChatResponse.builder()
                .aiMessage(aiMessage)
                .tokenUsage(tokenUsage)
                .build();
            when(mockChatModel.chat(any(UserMessage.class))).thenReturn(mockResponse);

            // When: Generate response
            long startTime = System.currentTimeMillis();
            LlmChatResponse response = client.generate(ROLE_NAME, PROMPT);
            long endTime = System.currentTimeMillis();

            // Then: Usage is extracted correctly
            assertThat(response.getUsage()).isNotNull();
            LlmUsage usage = response.getUsage();
            assertThat(usage.getPromptTokens()).isEqualTo(150);
            assertThat(usage.getCompletionTokens()).isEqualTo(75);
            assertThat(usage.getTotalTokens()).isEqualTo(225);
            assertThat(usage.getLatencyMs()).isGreaterThanOrEqualTo(0);
            assertThat(usage.getLatencyMs()).isLessThanOrEqualTo(endTime - startTime + 100);
            assertThat(usage.isEstimated()).isFalse();
            assertThat(usage.getRawMetadata()).isNull();
        }

        @Test
        @DisplayName("Should calculate latency correctly")
        void shouldCalculateLatencyCorrectly() throws InterruptedException {
            // Given: Mock response with delay
            AiMessage aiMessage = AiMessage.from("Delayed response");
            TokenUsage tokenUsage = new TokenUsage(100, 50);
            ChatResponse mockResponse = ChatResponse.builder()
                .aiMessage(aiMessage)
                .tokenUsage(tokenUsage)
                .build();
            when(mockChatModel.chat(any(UserMessage.class))).thenAnswer(invocation -> {
                Thread.sleep(50); // Simulate 50ms API call
                return mockResponse;
            });

            // When: Generate response
            long startTime = System.currentTimeMillis();
            LlmChatResponse response = client.generate(ROLE_NAME, PROMPT);
            long endTime = System.currentTimeMillis();
            long actualElapsed = endTime - startTime;

            // Then: Latency is reasonable
            assertThat(response.getUsage()).isNotNull();
            assertThat(response.getUsage().getLatencyMs()).isGreaterThanOrEqualTo(40); // Allow some variance
            assertThat(response.getUsage().getLatencyMs()).isLessThanOrEqualTo(actualElapsed + 50);
        }

        @Test
        @DisplayName("Should handle null token usage gracefully")
        void shouldHandleNullTokenUsage() {
            // Given: Mock response without token usage
            AiMessage aiMessage = AiMessage.from("Response without usage");
            ChatResponse mockResponse = ChatResponse.builder()
                .aiMessage(aiMessage)
                .build();
            when(mockChatModel.chat(any(UserMessage.class))).thenReturn(mockResponse);

            // When: Generate response
            LlmChatResponse response = client.generate(ROLE_NAME, PROMPT);

            // Then: Response is valid but usage is null
            assertThat(response).isNotNull();
            assertThat(response.getText()).isEqualTo("Response without usage");
            assertThat(response.getUsage()).isNull();
        }

        @Test
        @DisplayName("Should handle empty response content")
        void shouldHandleEmptyResponseContent() {
            // Given: Mock response with empty text
            AiMessage aiMessage = AiMessage.from("");
            ChatResponse mockResponse = ChatResponse.builder()
                .aiMessage(aiMessage)
                .build();
            when(mockChatModel.chat(any(UserMessage.class))).thenReturn(mockResponse);

            // When: Generate response
            LlmChatResponse response = client.generate(ROLE_NAME, PROMPT);

            // Then: Returns empty string
            assertThat(response).isNotNull();
            assertThat(response.getText()).isEqualTo("");
            assertThat(response.getProvider()).isEqualTo(PROVIDER_NAME);
            assertThat(response.getModel()).isEqualTo(MODEL_NAME);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should wrap exception with provider context")
        void shouldWrapExceptionWithProviderContext() {
            // Given: ChatModel that throws exception
            RuntimeException apiError = new RuntimeException("API authentication failed");
            when(mockChatModel.chat(any(UserMessage.class))).thenThrow(apiError);

            // When/Then: Exception is wrapped with context
            assertThatThrownBy(() -> client.generate(ROLE_NAME, PROMPT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("LLM API call failed")
                .hasMessageContaining("provider=" + PROVIDER_NAME)
                .hasMessageContaining("model=" + MODEL_NAME)
                .hasCause(apiError);
        }

        @Test
        @DisplayName("Should propagate API timeout exceptions")
        void shouldPropagateApiTimeoutExceptions() {
            // Given: API timeout
            RuntimeException timeoutError = new RuntimeException("Request timeout after 30s");
            when(mockChatModel.chat(any(UserMessage.class))).thenThrow(timeoutError);

            // When/Then: Timeout is propagated with context
            assertThatThrownBy(() -> client.generate(ROLE_NAME, PROMPT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("LLM API call failed")
                .hasCause(timeoutError);
        }

        @Test
        @DisplayName("Should handle rate limit errors")
        void shouldHandleRateLimitErrors() {
            // Given: Rate limit error
            RuntimeException rateLimitError = new RuntimeException("Rate limit exceeded: 429");
            when(mockChatModel.chat(any(UserMessage.class))).thenThrow(rateLimitError);

            // When/Then: Error includes provider context for debugging
            assertThatThrownBy(() -> client.generate(ROLE_NAME, PROMPT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(PROVIDER_NAME)
                .hasCause(rateLimitError);
        }
    }

    @Nested
    @DisplayName("Provider Configuration")
    class ProviderConfigurationTests {

        @Test
        @DisplayName("Should use correct provider name in responses")
        void shouldUseCorrectProviderName() {
            // Given: Multiple clients with different providers
            RealLlmChatClient anthropicClient = new RealLlmChatClient("anthropic", "claude", mockChatModel);
            RealLlmChatClient geminiClient = new RealLlmChatClient("gemini", "gemini-2.0", mockChatModel);

            AiMessage aiMessage = AiMessage.from("test");
            ChatResponse mockResponse = ChatResponse.builder()
                .aiMessage(aiMessage)
                .build();
            when(mockChatModel.chat(any(UserMessage.class))).thenReturn(mockResponse);

            // When: Generate from different clients
            LlmChatResponse anthropicResponse = anthropicClient.generate(ROLE_NAME, PROMPT);
            LlmChatResponse geminiResponse = geminiClient.generate(ROLE_NAME, PROMPT);

            // Then: Provider names are correct
            assertThat(anthropicResponse.getProvider()).isEqualTo("anthropic");
            assertThat(geminiResponse.getProvider()).isEqualTo("gemini");
        }

        @Test
        @DisplayName("Should use correct model name in responses")
        void shouldUseCorrectModelName() {
            // Given: Multiple clients with different models
            RealLlmChatClient sonnetClient = new RealLlmChatClient("anthropic", "claude-sonnet-4-5", mockChatModel);
            RealLlmChatClient opusClient = new RealLlmChatClient("anthropic", "claude-opus-4-0", mockChatModel);

            AiMessage aiMessage = AiMessage.from("test");
            ChatResponse mockResponse = ChatResponse.builder()
                .aiMessage(aiMessage)
                .build();
            when(mockChatModel.chat(any(UserMessage.class))).thenReturn(mockResponse);

            // When: Generate from different clients
            LlmChatResponse sonnetResponse = sonnetClient.generate(ROLE_NAME, PROMPT);
            LlmChatResponse opusResponse = opusClient.generate(ROLE_NAME, PROMPT);

            // Then: Model names are correct
            assertThat(sonnetResponse.getModel()).isEqualTo("claude-sonnet-4-5");
            assertThat(opusResponse.getModel()).isEqualTo("claude-opus-4-0");
        }
    }
}
