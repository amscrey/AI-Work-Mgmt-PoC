package com.aiworkflow.workmanagement.orchestration.domain;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable report payload for role discovery and startup reporting.
 */
public class RoleModelReport {
    private final List<AgentRole> roles;
    private final Map<String, List<String>> roleKeywords;
    private final WorkTypeAssociations workTypeAssociations;
    private final List<String> warnings;
    private final Path reportPath;

    public RoleModelReport(
        List<AgentRole> roles,
        Map<String, List<String>> roleKeywords,
        WorkTypeAssociations workTypeAssociations,
        List<String> warnings,
        Path reportPath
    ) {
        this.roles = roles == null ? List.of() : List.copyOf(roles);
        this.roleKeywords = roleKeywords == null ? Map.of() : Map.copyOf(roleKeywords);
        this.workTypeAssociations = workTypeAssociations;
        this.warnings = warnings == null ? List.of() : List.copyOf(warnings);
        this.reportPath = reportPath;
    }

    public List<AgentRole> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public Map<String, List<String>> getRoleKeywords() {
        return Collections.unmodifiableMap(roleKeywords);
    }

    public WorkTypeAssociations getWorkTypeAssociations() {
        return workTypeAssociations;
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public Path getReportPath() {
        return reportPath;
    }
}

