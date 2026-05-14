package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.domain.AgentRole;
import com.aiworkflow.workmanagement.orchestration.domain.RoleModelReport;
import com.aiworkflow.workmanagement.orchestration.domain.WorkTypeAssociations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Writes a startup report for role discovery.
 */
public class RoleStartupReportWriter {

    public void writeReport(RoleModelReport report) throws IOException {
        Path reportPath = report.getReportPath();
        Files.createDirectories(reportPath.getParent());
        Files.writeString(reportPath, buildReport(report));
    }

    private String buildReport(RoleModelReport report) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Role Model Startup Report\n\n");
        builder.append("Generated: ").append(Instant.now()).append("\n\n");
        builder.append("## Summary\n");
        builder.append("Roles discovered: ").append(report.getRoles().size()).append("\n\n");

        builder.append("## Execution Mode Notes\n");
        builder.append("- Template mode uses deterministic nodes and role mapping hints (research, planning, design, test strategy).\n");
        builder.append("- LLM mode uses PromptTemplateService + LLM nodes for the same role set.\n\n");

        builder.append("## Roles\n\n");
        builder.append("| Role | Description | Keywords | Source |\n");
        builder.append("| --- | --- | --- | --- |\n");
        for (AgentRole role : report.getRoles()) {
            List<String> keywords = report.getRoleKeywords().getOrDefault(role.getName(), List.of());
            builder.append("|")
                .append(role.getName()).append("|")
                .append(role.getDescription()).append("|")
                .append(String.join(", ", keywords)).append("|")
                .append(role.getRoleFilePath()).append("|\n");
        }

        builder.append("\n## Work Type Associations\n\n");
        WorkTypeAssociations associations = report.getWorkTypeAssociations();
        for (Map.Entry<String, String> entry : associations.getKeywordToRole().entrySet()) {
            builder.append("- ").append(entry.getKey()).append(" → ").append(entry.getValue()).append("\n");
        }

        builder.append("\n## Health Checks\n\n");
        builder.append("- Roles loaded: ").append(report.getRoles().size()).append("\n");
        builder.append("- Warnings: ").append(report.getWarnings().size()).append("\n");

        if (!report.getWarnings().isEmpty()) {
            builder.append("\n## Warnings\n\n");
            StringJoiner joiner = new StringJoiner("\n");
            report.getWarnings().forEach(warning -> joiner.add("- " + warning));
            builder.append(joiner).append("\n");
        }

        return builder.toString();
    }
}
