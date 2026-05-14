package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of effective LLM configuration.
 */
public class LlmConfigSnapshot {
    private final Map<String, LlmProviderConfig> providers;
    private final Map<String, LlmRoleMapping> roleMappings;
    private final String defaultProvider;
    private final LlmConfigDiagnostics diagnostics;

    public LlmConfigSnapshot(
        Map<String, LlmProviderConfig> providers,
        Map<String, LlmRoleMapping> roleMappings,
        String defaultProvider,
        LlmConfigDiagnostics diagnostics
    ) {
        this.providers = providers == null ? Map.of() : Map.copyOf(providers);
        this.roleMappings = roleMappings == null ? Map.of() : Map.copyOf(roleMappings);
        this.defaultProvider = defaultProvider;
        this.diagnostics = diagnostics;
    }

    public Map<String, LlmProviderConfig> getProviders() {
        return Collections.unmodifiableMap(providers);
    }

    public Map<String, LlmRoleMapping> getRoleMappings() {
        return Collections.unmodifiableMap(roleMappings);
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public LlmConfigDiagnostics getDiagnostics() {
        return diagnostics;
    }
}

