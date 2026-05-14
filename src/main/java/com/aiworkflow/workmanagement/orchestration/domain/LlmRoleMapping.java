package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.Collections;
import java.util.List;

/**
 * Role mapping for LLM provider selection.
 */
public class LlmRoleMapping {
    private final String roleName;
    private final String provider;
    private final String model;
    private final List<String> fallbackProviders;

    public LlmRoleMapping(String roleName, String provider, String model, List<String> fallbackProviders) {
        this.roleName = roleName;
        this.provider = provider;
        this.model = model;
        this.fallbackProviders = fallbackProviders == null ? List.of() : List.copyOf(fallbackProviders);
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
}
