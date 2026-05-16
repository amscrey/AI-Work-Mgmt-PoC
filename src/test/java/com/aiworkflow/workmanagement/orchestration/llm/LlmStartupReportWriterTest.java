package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigDiagnostics;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleMapping;
import com.aiworkflow.workmanagement.orchestration.domain.LlmSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LlmStartupReportWriter Unit Tests")
class LlmStartupReportWriterTest {

    @Test
    void writesReportWithDiagnostics(@TempDir Path tempDir) throws Exception {
        LlmSummary summary = new LlmSummary(
            "anthropic",
            Map.of("anthropic", new LlmProviderConfig("anthropic", "key", "claude", null, true, false)),
            Map.of("researcher", new LlmRoleMapping("researcher", "anthropic", "claude", null)),
            new LlmConfigDiagnostics(List.of("orchestration.llm.unknown"), List.of("warning"))
        );

        Path reportPath = tempDir.resolve("llm-startup-report.md");
        new LlmStartupReportWriter().writeReport(reportPath, summary);

        String content = Files.readString(reportPath);
        assertThat(content).contains("LLM Startup Report");
        assertThat(content).contains("ignored: orchestration.llm.unknown");
        assertThat(content).contains("warning: warning");
    }

    @Nested
    @DisplayName("API Key Masking Tests")
    class ApiKeyMaskingTests {

        @Test
        @DisplayName("Should include masked API keys in report")
        void shouldIncludeMaskedApiKeysInReport(@TempDir Path tempDir) throws Exception {
            LlmSummary summary = new LlmSummary(
                "anthropic",
                Map.of(
                    "anthropic", new LlmProviderConfig("anthropic", "sk-ant-api-1234567890abcdef", "claude-sonnet-4-5", null, true, false),
                    "gemini", new LlmProviderConfig("gemini", "AIzaSyDemoKey123456789", "gemini-2.0-flash", null, true, false)
                ),
                Map.of("researcher", new LlmRoleMapping("researcher", "anthropic", "claude-sonnet-4-5", null)),
                new LlmConfigDiagnostics(null, null)
            );

            Path reportPath = tempDir.resolve("llm-startup-report.md");
            new LlmStartupReportWriter().writeReport(reportPath, summary);

            String content = Files.readString(reportPath);

            // Should show masked keys (first 5 + ... + last 4)
            assertThat(content).contains("apiKey: sk-an...cdef");
            assertThat(content).contains("apiKey: AIzaS...6789");

            // Should NOT contain full API keys
            assertThat(content).doesNotContain("sk-ant-api-1234567890abcdef");
            assertThat(content).doesNotContain("AIzaSyDemoKey123456789");
        }

        @Test
        @DisplayName("Should show [NOT SET] for null API key")
        void shouldShowNotSetForNullApiKey(@TempDir Path tempDir) throws Exception {
            LlmSummary summary = new LlmSummary(
                "anthropic",
                Map.of("anthropic", new LlmProviderConfig("anthropic", null, "claude-sonnet-4-5", null, true, false)),
                Map.of(),
                new LlmConfigDiagnostics(null, null)
            );

            Path reportPath = tempDir.resolve("llm-startup-report.md");
            new LlmStartupReportWriter().writeReport(reportPath, summary);

            String content = Files.readString(reportPath);
            assertThat(content).contains("apiKey: [NOT SET]");
        }

        @Test
        @DisplayName("Should show [NOT SET] for empty API key")
        void shouldShowNotSetForEmptyApiKey(@TempDir Path tempDir) throws Exception {
            LlmSummary summary = new LlmSummary(
                "gemini",
                Map.of("gemini", new LlmProviderConfig("gemini", "", "gemini-2.0-flash", null, true, false)),
                Map.of(),
                new LlmConfigDiagnostics(null, null)
            );

            Path reportPath = tempDir.resolve("llm-startup-report.md");
            new LlmStartupReportWriter().writeReport(reportPath, summary);

            String content = Files.readString(reportPath);
            assertThat(content).contains("apiKey: [NOT SET]");
        }

        @Test
        @DisplayName("Should handle short API keys correctly")
        void shouldHandleShortApiKeys(@TempDir Path tempDir) throws Exception {
            LlmSummary summary = new LlmSummary(
                "groq",
                Map.of("groq", new LlmProviderConfig("groq", "short", "llama-3.3-70b", null, true, false)),
                Map.of(),
                new LlmConfigDiagnostics(null, null)
            );

            Path reportPath = tempDir.resolve("llm-startup-report.md");
            new LlmStartupReportWriter().writeReport(reportPath, summary);

            String content = Files.readString(reportPath);
            assertThat(content).contains("apiKey: short...");
        }
    }

    @Nested
    @DisplayName("Base URL Tests")
    class BaseUrlTests {

        @Test
        @DisplayName("Should include custom base URL in report")
        void shouldIncludeCustomBaseUrl(@TempDir Path tempDir) throws Exception {
            LlmSummary summary = new LlmSummary(
                "anthropic",
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
                new LlmConfigDiagnostics(null, null)
            );

            Path reportPath = tempDir.resolve("llm-startup-report.md");
            new LlmStartupReportWriter().writeReport(reportPath, summary);

            String content = Files.readString(reportPath);
            assertThat(content).contains("baseUrl: https://ai-model-proxy.aks-ur-prd-internal.8451.cloud");
        }

        @Test
        @DisplayName("Should show 'default' when base URL is null")
        void shouldShowDefaultWhenBaseUrlIsNull(@TempDir Path tempDir) throws Exception {
            LlmSummary summary = new LlmSummary(
                "anthropic",
                Map.of("anthropic", new LlmProviderConfig("anthropic", "sk-ant-1234567890", "claude-sonnet-4-5", null, true, false)),
                Map.of(),
                new LlmConfigDiagnostics(null, null)
            );

            Path reportPath = tempDir.resolve("llm-startup-report.md");
            new LlmStartupReportWriter().writeReport(reportPath, summary);

            String content = Files.readString(reportPath);
            assertThat(content).contains("baseUrl: default");
        }

        @Test
        @DisplayName("Should show 'default' when base URL is empty")
        void shouldShowDefaultWhenBaseUrlIsEmpty(@TempDir Path tempDir) throws Exception {
            LlmSummary summary = new LlmSummary(
                "gemini",
                Map.of("gemini", new LlmProviderConfig("gemini", "key", "gemini-2.0-flash", "", true, false)),
                Map.of(),
                new LlmConfigDiagnostics(null, null)
            );

            Path reportPath = tempDir.resolve("llm-startup-report.md");
            new LlmStartupReportWriter().writeReport(reportPath, summary);

            String content = Files.readString(reportPath);
            assertThat(content).contains("baseUrl: default");
        }
    }

    @Nested
    @DisplayName("Complete Report Structure Tests")
    class ReportStructureTests {

        @Test
        @DisplayName("Should include all provider configuration details")
        void shouldIncludeAllProviderDetails(@TempDir Path tempDir) throws Exception {
            LlmSummary summary = new LlmSummary(
                "anthropic",
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
                Map.of("researcher", new LlmRoleMapping("researcher", "anthropic", "claude-sonnet-4-5", null)),
                new LlmConfigDiagnostics(null, null)
            );

            Path reportPath = tempDir.resolve("llm-startup-report.md");
            new LlmStartupReportWriter().writeReport(reportPath, summary);

            String content = Files.readString(reportPath);

            // Provider details
            assertThat(content).contains("anthropic");
            assertThat(content).contains("model: claude-sonnet-4-5");
            assertThat(content).contains("apiKey: sk-an...cdef");
            assertThat(content).contains("baseUrl: https://api.anthropic.com");
            assertThat(content).contains("enabled: true");
            assertThat(content).contains("dummy: false");

            // Role mapping
            assertThat(content).contains("researcher");
        }

        @Test
        @DisplayName("Should handle multiple providers in report")
        void shouldHandleMultipleProviders(@TempDir Path tempDir) throws Exception {
            LlmSummary summary = new LlmSummary(
                "anthropic",
                Map.of(
                    "anthropic", new LlmProviderConfig("anthropic", "sk-ant-key123456789", "claude-sonnet-4-5", null, true, false),
                    "gemini", new LlmProviderConfig("gemini", null, "gemini-2.0-flash", null, true, false),
                    "groq", new LlmProviderConfig("groq", "gsk-key987654321", "llama-3.3-70b", "https://api.groq.com", true, false)
                ),
                Map.of(
                    "researcher", new LlmRoleMapping("researcher", "anthropic", "claude-sonnet-4-5", null),
                    "coder", new LlmRoleMapping("coder", "groq", "llama-3.3-70b", List.of("anthropic"))
                ),
                new LlmConfigDiagnostics(null, null)
            );

            Path reportPath = tempDir.resolve("llm-startup-report.md");
            new LlmStartupReportWriter().writeReport(reportPath, summary);

            String content = Files.readString(reportPath);

            // All providers present
            assertThat(content).contains("anthropic");
            assertThat(content).contains("gemini");
            assertThat(content).contains("groq");

            // Masked keys
            assertThat(content).contains("apiKey: sk-an...6789");
            assertThat(content).contains("apiKey: [NOT SET]");
            assertThat(content).contains("apiKey: gsk-k...4321");

            // Base URLs
            assertThat(content).contains("baseUrl: https://api.groq.com");
            assertThat(content).containsPattern("anthropic.*baseUrl: default");
        }
    }
}

