package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.domain.LlmSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

/**
 * Writes a startup report for LLM configuration.
 */
public class LlmStartupReportWriter {

    public void writeReport(Path reportPath, LlmSummary summary) throws IOException {
        Files.createDirectories(reportPath.getParent());
        Files.writeString(reportPath, buildReport(summary));
    }

    private String buildReport(LlmSummary summary) {
        StringBuilder builder = new StringBuilder();
        builder.append("# LLM Startup Report\n\n");
        builder.append("Generated: ").append(Instant.now()).append("\n\n");
        builder.append("Default provider: ").append(summary.getDefaultProvider()).append("\n\n");

        builder.append("## Providers\n\n");
        for (Map.Entry<String, LlmProviderConfig> entry : summary.getProviders().entrySet()) {
            LlmProviderConfig provider = entry.getValue();
            builder.append("- ").append(provider.getName())
                .append(" (model: ").append(provider.getModel()).append(", enabled: ")
                .append(provider.isEnabled()).append(", dummy: ").append(provider.isDummy()).append(")\n");
        }

        builder.append("\n## Role Mappings\n\n");
        summary.getRoleMappings().forEach((role, mapping) -> {
            builder.append("- ").append(role)
                .append(" → ").append(mapping.getProvider())
                .append(" (model: ").append(mapping.getModel()).append(")\n");
        });

        builder.append("\n## Diagnostics\n\n");
        summary.getDiagnostics().getIgnoredEntries().forEach(entry ->
            builder.append("- ignored: ").append(entry).append("\n")
        );
        summary.getDiagnostics().getWarnings().forEach(entry ->
            builder.append("- warning: ").append(entry).append("\n")
        );

        return builder.toString();
    }
}

