package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigDiagnostics;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleMapping;
import com.aiworkflow.workmanagement.orchestration.domain.LlmSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LlmStartupReportWriterTest {

    @Test
    void writesReportWithDiagnostics(@TempDir Path tempDir) throws Exception {
        LlmSummary summary = new LlmSummary(
            "anthropic",
            Map.of("anthropic", new LlmProviderConfig("anthropic", "key", "claude", true, false)),
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
}
