package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.Collections;
import java.util.List;

/**
 * Diagnostics for LLM configuration parsing/validation.
 */
public class LlmConfigDiagnostics {
    private final List<String> ignoredEntries;
    private final List<String> warnings;

    public LlmConfigDiagnostics(List<String> ignoredEntries, List<String> warnings) {
        this.ignoredEntries = ignoredEntries == null ? List.of() : List.copyOf(ignoredEntries);
        this.warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public List<String> getIgnoredEntries() {
        return Collections.unmodifiableList(ignoredEntries);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}

