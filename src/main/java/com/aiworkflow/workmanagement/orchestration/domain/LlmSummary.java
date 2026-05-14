package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Summary of effective LLM configuration for reporting.
 */
public class LlmSummary {
    private final String defaultProvider;
    private final Map<String, LlmProviderConfig> providers;
    private final Map<String, LlmRoleMapping> roleMappings;
    private final LlmConfigDiagnostics diagnostics;

    public LlmSummary(
        String defaultProvider,
        Map<String, LlmProviderConfig> providers,
        Map<String, LlmRoleMapping> roleMappings,
        LlmConfigDiagnostics diagnostics
    ) {
        this.defaultProvider = defaultProvider;
        this.providers = providers == null ? Map.of() : Map.copyOf(providers);
        this.roleMappings = roleMappings == null ? Map.of() : Map.copyOf(roleMappings);
        this.diagnostics = diagnostics;
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public Map<String, LlmProviderConfig> getProviders() {
        return Collections.unmodifiableMap(providers);
    }

    public Map<String, LlmRoleMapping> getRoleMappings() {
        return Collections.unmodifiableMap(roleMappings);
    }

    public LlmConfigDiagnostics getDiagnostics() {
        return diagnostics;
    }
}

