package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigDiagnostics;
import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultLlmProviderRegistry Unit Tests")
class DefaultLlmProviderRegistryTest {

    @Test
    void buildsDummyAndStubClients() {
        LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
            Map.of(
                "dummy", new LlmProviderConfig("dummy", null, "dummy", null, true, true),
                "gemini", new LlmProviderConfig("gemini", "", "gemini-2.0-flash", null, true, false)
            ),
            Map.of(),
            "anthropic",
            new LlmConfigDiagnostics(null, null)
        );

        LlmConfigLoader loader = () -> snapshot;
        DefaultLlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);

        assertThat(registry.getClient("dummy")).isInstanceOf(DummyChatClient.class);
        assertThat(registry.getClient("gemini")).isInstanceOf(StubbedProviderChatClient.class);
    }

    @Nested
    @DisplayName("Client Type Selection")
    class ClientTypeSelectionTests {

        @Test
        @DisplayName("Should create real client when API key is present")
        void shouldCreateRealClientWhenApiKeyPresent() {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "anthropic", new LlmProviderConfig("anthropic", "sk-ant-1234567890", "claude-sonnet-4-5", null, true, false)
                ),
                Map.of(),
                "anthropic",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            DefaultLlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);

            LLMChatClient client = registry.getClient("anthropic");
            assertThat(client).isInstanceOf(RealLlmChatClient.class);
        }

        @Test
        @DisplayName("Should create stubbed client when API key is null")
        void shouldCreateStubbedClientWhenApiKeyIsNull() {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "anthropic", new LlmProviderConfig("anthropic", null, "claude-sonnet-4-5", null, true, false)
                ),
                Map.of(),
                "anthropic",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            DefaultLlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);

            LLMChatClient client = registry.getClient("anthropic");
            assertThat(client).isInstanceOf(StubbedProviderChatClient.class);
        }

        @Test
        @DisplayName("Should create stubbed client when API key is empty string")
        void shouldCreateStubbedClientWhenApiKeyIsEmpty() {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "gemini", new LlmProviderConfig("gemini", "", "gemini-2.0-flash", null, true, false)
                ),
                Map.of(),
                "gemini",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            DefaultLlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);

            LLMChatClient client = registry.getClient("gemini");
            assertThat(client).isInstanceOf(StubbedProviderChatClient.class);
        }

        @Test
        @DisplayName("Should create stubbed client when API key is blank")
        void shouldCreateStubbedClientWhenApiKeyIsBlank() {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "groq", new LlmProviderConfig("groq", "   ", "llama-3.3-70b", null, true, false)
                ),
                Map.of(),
                "groq",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            DefaultLlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);

            LLMChatClient client = registry.getClient("groq");
            assertThat(client).isInstanceOf(StubbedProviderChatClient.class);
        }

        @Test
        @DisplayName("Should always create dummy client for dummy provider")
        void shouldAlwaysCreateDummyClientForDummyProvider() {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "dummy", new LlmProviderConfig("dummy", "some-key", "instant", null, true, true)
                ),
                Map.of(),
                "dummy",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            DefaultLlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);

            LLMChatClient client = registry.getClient("dummy");
            assertThat(client).isInstanceOf(DummyChatClient.class);
        }
    }

    @Nested
    @DisplayName("Base URL Configuration")
    class BaseUrlConfigurationTests {

        @Test
        @DisplayName("Should respect custom base URL configuration")
        @ExtendWith(OutputCaptureExtension.class)
        void shouldRespectCustomBaseUrl(CapturedOutput output) {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "anthropic", new LlmProviderConfig(
                        "anthropic",
                        "sk-ant-1234567890",
                        "claude-sonnet-4-5",
                        "https://ai-model-proxy.aks-ur-prd-internal.8451.cloud",
                        true,
                        false
                    )
                ),
                Map.of(),
                "anthropic",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            DefaultLlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);

            LLMChatClient client = registry.getClient("anthropic");
            assertThat(client).isInstanceOf(RealLlmChatClient.class);

            // Verify custom baseUrl was logged
            assertThat(output.getOut()).contains("baseUrl=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud");
        }

        @Test
        @DisplayName("Should log default when base URL is null")
        @ExtendWith(OutputCaptureExtension.class)
        void shouldLogDefaultWhenBaseUrlIsNull(CapturedOutput output) {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "anthropic", new LlmProviderConfig("anthropic", "sk-ant-1234567890", "claude-sonnet-4-5", null, true, false)
                ),
                Map.of(),
                "anthropic",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            new DefaultLlmProviderRegistry(loader);

            assertThat(output.getOut()).contains("baseUrl=default");
        }
    }

    @Nested
    @DisplayName("API Key Masking")
    class ApiKeyMaskingTests {

        @Test
        @DisplayName("Should mask API key with correct pattern for normal keys")
        @ExtendWith(OutputCaptureExtension.class)
        void shouldMaskApiKeyCorrectly(CapturedOutput output) {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "anthropic", new LlmProviderConfig("anthropic", "sk-ant-api-1234567890abcdef", "claude-sonnet-4-5", null, true, false)
                ),
                Map.of(),
                "anthropic",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            new DefaultLlmProviderRegistry(loader);

            // Should show first 5 + ... + last 4
            assertThat(output.getOut()).contains("apiKey=sk-an...cdef");
        }

        @Test
        @DisplayName("Should log warning when API key is null")
        @ExtendWith(OutputCaptureExtension.class)
        void shouldLogWarningForNullApiKey(CapturedOutput output) {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "gemini", new LlmProviderConfig("gemini", null, "gemini-2.0-flash", null, true, false)
                ),
                Map.of(),
                "gemini",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            new DefaultLlmProviderRegistry(loader);

            assertThat(output.getOut()).contains("LLM provider 'gemini' missing API key");
            assertThat(output.getOut()).contains("using stubbed client");
        }

        @Test
        @DisplayName("Should log warning when API key is empty")
        @ExtendWith(OutputCaptureExtension.class)
        void shouldLogWarningForEmptyApiKey(CapturedOutput output) {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "gemini", new LlmProviderConfig("gemini", "", "gemini-2.0-flash", null, true, false)
                ),
                Map.of(),
                "gemini",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            new DefaultLlmProviderRegistry(loader);

            assertThat(output.getOut()).contains("LLM provider 'gemini' missing API key");
            assertThat(output.getOut()).contains("using stubbed client");
        }

        @Test
        @DisplayName("Should handle short API keys gracefully")
        @ExtendWith(OutputCaptureExtension.class)
        void shouldHandleShortApiKeys(CapturedOutput output) {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "anthropic", new LlmProviderConfig("anthropic", "short", "claude-sonnet-4-5", null, true, false)
                ),
                Map.of(),
                "anthropic",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            new DefaultLlmProviderRegistry(loader);

            // Short key should just show first 5 chars + ...
            assertThat(output.getOut()).contains("apiKey=short...");
        }
    }

    @Nested
    @DisplayName("Mixed Configuration Scenarios")
    class MixedConfigurationTests {

        @Test
        @DisplayName("Should handle mixed real and stubbed providers")
        void shouldHandleMixedProviders() {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "anthropic", new LlmProviderConfig("anthropic", "sk-ant-1234567890", "claude-sonnet-4-5", null, true, false),
                    "gemini", new LlmProviderConfig("gemini", null, "gemini-2.0-flash", null, true, false),
                    "groq", new LlmProviderConfig("groq", "", "llama-3.3-70b", null, true, false),
                    "dummy", new LlmProviderConfig("dummy", null, "instant", null, true, true)
                ),
                Map.of(),
                "anthropic",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            DefaultLlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);

            // Verify each client type
            assertThat(registry.getClient("anthropic")).isInstanceOf(RealLlmChatClient.class);
            assertThat(registry.getClient("gemini")).isInstanceOf(StubbedProviderChatClient.class);
            assertThat(registry.getClient("groq")).isInstanceOf(StubbedProviderChatClient.class);
            assertThat(registry.getClient("dummy")).isInstanceOf(DummyChatClient.class);
        }
    }

    @Nested
    @DisplayName("Logging Verification")
    class LoggingTests {

        @Test
        @DisplayName("Should log provider registration details for real client")
        @ExtendWith(OutputCaptureExtension.class)
        void shouldLogRealClientRegistration(CapturedOutput output) {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "anthropic", new LlmProviderConfig(
                        "anthropic",
                        "sk-ant-api-1234567890abcdef",
                        "claude-sonnet-4-5",
                        "https://api.anthropic.com",
                        true,
                        false
                    )
                ),
                Map.of(),
                "anthropic",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            new DefaultLlmProviderRegistry(loader);

            String logOutput = output.getOut();
            assertThat(logOutput).contains("Registered REAL LLM client");
            assertThat(logOutput).contains("provider=anthropic");
            assertThat(logOutput).contains("model=claude-sonnet-4-5");
            assertThat(logOutput).contains("baseUrl=https://api.anthropic.com");
            assertThat(logOutput).contains("apiKey=sk-an...cdef");
        }

        @Test
        @DisplayName("Should log warning for stubbed client due to missing key")
        @ExtendWith(OutputCaptureExtension.class)
        void shouldLogStubbedClientWarning(CapturedOutput output) {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "gemini", new LlmProviderConfig("gemini", null, "gemini-2.0-flash", null, true, false)
                ),
                Map.of(),
                "gemini",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            new DefaultLlmProviderRegistry(loader);

            String logOutput = output.getOut();
            assertThat(logOutput).contains("LLM provider 'gemini' missing API key");
            assertThat(logOutput).contains("using stubbed client");
        }

        @Test
        @DisplayName("Should log dummy client registration")
        @ExtendWith(OutputCaptureExtension.class)
        void shouldLogDummyClientRegistration(CapturedOutput output) {
            LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
                Map.of(
                    "dummy", new LlmProviderConfig("dummy", null, "instant", null, true, true)
                ),
                Map.of(),
                "dummy",
                new LlmConfigDiagnostics(null, null)
            );

            LlmConfigLoader loader = () -> snapshot;
            new DefaultLlmProviderRegistry(loader);

            String logOutput = output.getOut();
            assertThat(logOutput).contains("Registering dummy LLM client");
            assertThat(logOutput).contains("provider=dummy");
        }
    }
}

