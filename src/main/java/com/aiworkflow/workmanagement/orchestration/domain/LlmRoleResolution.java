package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.Collections;
import java.util.List;

/**
 * Resolution result for role-to-provider selection.
 */
public class LlmRoleResolution {
    private final String roleName;
    private final String provider;
    private final String model;
    private final List<String> fallbackProviders;
    private final List<String> warnings;

    public LlmRoleResolution(
        String roleName,
        String provider,
        String model,
        List<String> fallbackProviders,
        List<String> warnings
    ) {
        this.roleName = roleName;
        this.provider = provider;
        this.model = model;
        this.fallbackProviders = fallbackProviders == null ? List.of() : List.copyOf(fallbackProviders);
        this.warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public String getRoleName() {
        return roleName;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public List<String> getFallbackProviders() {
        return Collections.unmodifiableList(fallbackProviders);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}

