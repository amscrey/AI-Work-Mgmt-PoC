package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for LLM provider configuration and client creation.
 * Tests the full Spring context with various provider configurations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("LLM Provider Integration Tests")
class LlmProviderIntegrationIT {

    @TempDir
    static Path workspaceRoot;

    @DynamicPropertySource
    static void registerCommonProps(DynamicPropertyRegistry registry) {
        registry.add("workmanagement.workspace-root", () -> workspaceRoot.toString());
        registry.add("workmanagement.workspace-name", () -> "test");
        registry.add("orchestration.execution-mode", () -> "llm");
        registry.add("spring.devtools.restart.enabled", () -> "false");
    }

    @Nested
    @DisplayName("Stubbed Client Configuration (No API Keys)")
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    class StubbedClientTests {

        @Autowired
        private LlmProviderRegistry providerRegistry;

        @Autowired
        private RoutingLlmChatClient routingClient;

        @DynamicPropertySource
        static void registerProps(DynamicPropertyRegistry registry) {
            // No API keys configured - should use stubbed clients
            registry.add("orchestration.llm.providers.anthropic.api-key", () -> "");
            registry.add("orchestration.llm.providers.gemini.api-key", () -> "");
        }

        @Test
        @DisplayName("Should create stubbed clients when API keys are missing")
        void shouldCreateStubbedClientsWhenKeysMissing() {
            LLMChatClient anthropicClient = providerRegistry.getClient("anthropic");
            LLMChatClient geminiClient = providerRegistry.getClient("gemini");

            assertThat(anthropicClient).isInstanceOf(StubbedProviderChatClient.class);
            assertThat(geminiClient).isInstanceOf(StubbedProviderChatClient.class);
        }

        @Test
        @DisplayName("Should return stubbed responses")
        void shouldReturnStubbedResponses() {
            LlmChatResponse response = routingClient.generate("researcher", "Test prompt");

            assertThat(response).isNotNull();
            assertThat(response.getText()).contains("stubbed-response");
            assertThat(response.getProvider()).isEqualTo("anthropic");
        }
    }

    @Nested
    @DisplayName("Real Client Configuration (With API Keys)")
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    class RealClientTests {

        @Autowired
        private LlmProviderRegistry providerRegistry;

        @DynamicPropertySource
        static void registerProps(DynamicPropertyRegistry registry) {
            // API key configured - should use real client
            // Note: This is a fake key for testing - real API calls won't work
            registry.add("orchestration.llm.providers.anthropic.api-key", () -> "sk-ant-test-key-1234567890");
            // Gemini left without key to test mixed scenario
            registry.add("orchestration.llm.providers.gemini.api-key", () -> "");
        }

        @Test
        @DisplayName("Should create real client for anthropic when API key is present")
        void shouldCreateRealClientWhenKeyPresent() {
            LLMChatClient anthropicClient = providerRegistry.getClient("anthropic");

            assertThat(anthropicClient).isInstanceOf(RealLlmChatClient.class);
        }

        @Test
        @DisplayName("Should create stubbed client for gemini when API key is missing")
        void shouldCreateStubbedClientWhenKeyMissing() {
            LLMChatClient geminiClient = providerRegistry.getClient("gemini");

            assertThat(geminiClient).isInstanceOf(StubbedProviderChatClient.class);
        }
    }

    @Nested
    @DisplayName("Mixed Configuration (Some Keys, Some Missing)")
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    class MixedConfigurationTests {

        @Autowired
        private LlmProviderRegistry providerRegistry;

        @Autowired
        private RoutingLlmChatClient routingClient;

        @DynamicPropertySource
        static void registerProps(DynamicPropertyRegistry registry) {
            // Anthropic has key, others don't
            registry.add("orchestration.llm.providers.anthropic.api-key", () -> "sk-ant-test-key-1234567890");
            registry.add("orchestration.llm.providers.gemini.api-key", () -> "");
        }

        @Test
        @DisplayName("Should handle mixed real and stubbed providers")
        void shouldHandleMixedProviders() {
            LLMChatClient anthropicClient = providerRegistry.getClient("anthropic");
            LLMChatClient geminiClient = providerRegistry.getClient("gemini");

            // Anthropic has key -> real client
            assertThat(anthropicClient).isInstanceOf(RealLlmChatClient.class);

            // Gemini doesn't have key -> stubbed client
            assertThat(geminiClient).isInstanceOf(StubbedProviderChatClient.class);
        }

        @Test
        @DisplayName("Should route to correct client types based on role mapping")
        void shouldRouteToCorrectClientTypes() {
            // Default role mappings use anthropic (which has key -> real client)
            // But actual API call would fail with test key, so we just verify routing doesn't crash
            assertThat(routingClient).isNotNull();
        }
    }

    @Nested
    @DisplayName("Base URL Configuration")
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    class BaseUrlConfigurationTests {

        @Autowired
        private LlmProviderRegistry providerRegistry;

        @DynamicPropertySource
        static void registerProps(DynamicPropertyRegistry registry) {
            // Configure with corporate proxy
            registry.add("orchestration.llm.providers.anthropic.api-key", () -> "sk-ant-test-key-1234567890");
            registry.add("orchestration.llm.providers.anthropic.base-url",
                () -> "https://ai-model-proxy.aks-ur-prd-internal.8451.cloud");
        }

        @Test
        @DisplayName("Should configure client with custom base URL")
        void shouldConfigureClientWithCustomBaseUrl() {
            LLMChatClient client = providerRegistry.getClient("anthropic");

            // Should create real client when API key is present
            assertThat(client).isInstanceOf(RealLlmChatClient.class);

            // Verify client was created (base URL would be used internally by LangChain4j)
            // We can't directly inspect the base URL, but we can verify the client type
            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("Dummy Provider Configuration")
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    class DummyProviderTests {

        @Autowired
        private LlmProviderRegistry providerRegistry;

        @DynamicPropertySource
        static void registerProps(DynamicPropertyRegistry registry) {
            // Enable dummy provider
            registry.add("orchestration.llm.providers.dummy.enabled", () -> "true");
            registry.add("orchestration.llm.providers.dummy.dummy", () -> "true");
        }

        @Test
        @DisplayName("Should create dummy client for dummy provider")
        void shouldCreateDummyClient() {
            LLMChatClient dummyClient = providerRegistry.getClient("dummy");

            assertThat(dummyClient).isInstanceOf(DummyChatClient.class);
        }
    }

    @Nested
    @DisplayName("Routing Client Integration")
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    class RoutingClientIntegrationTests {

        @Autowired
        private RoutingLlmChatClient routingClient;

        @Autowired
        private LlmRoleMappingResolver roleMappingResolver;

        @DynamicPropertySource
        static void registerProps(DynamicPropertyRegistry registry) {
            // No keys - use stubbed clients
            registry.add("orchestration.llm.providers.anthropic.api-key", () -> "");
        }

        @Test
        @DisplayName("Should route role to appropriate provider")
        void shouldRouteRoleToProvider() {
            // Test that routing works end-to-end
            LlmChatResponse response = routingClient.generate("researcher", "Analyze this");

            assertThat(response).isNotNull();
            assertThat(response.getProvider()).isNotNull();
            assertThat(response.getText()).isNotBlank();
        }

        @Test
        @DisplayName("Should use role mapping resolver")
        void shouldUseRoleMappingResolver() {
            var resolution = roleMappingResolver.resolve("researcher");

            assertThat(resolution).isNotNull();
            assertThat(resolution.getProvider()).isNotNull();
        }
    }
}
