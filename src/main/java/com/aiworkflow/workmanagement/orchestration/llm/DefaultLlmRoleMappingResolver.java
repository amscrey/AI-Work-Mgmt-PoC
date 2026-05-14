package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleMapping;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleResolution;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Default role mapping resolver with precedence rules.
 */
@Service
public class DefaultLlmRoleMappingResolver implements LlmRoleMappingResolver {

    private final LlmConfigSnapshot snapshot;

    public DefaultLlmRoleMappingResolver(LlmConfigLoader loader) {
        this.snapshot = loader.load();
    }

    @Override
    public LlmRoleResolution resolve(String roleName) {
        List<String> warnings = new ArrayList<>();
        String normalizedRole = roleName != null ? roleName.trim().toLowerCase() : "";
        LlmRoleMapping mapping = snapshot.getRoleMappings().get(normalizedRole);

        if (mapping == null) {
            warnings.add("No role mapping found for role: " + roleName);
            return new LlmRoleResolution(roleName, snapshot.getDefaultProvider(), null, List.of(), warnings);
        }

        String provider = mapping.getProvider() != null ? mapping.getProvider() : snapshot.getDefaultProvider();
        if (!snapshot.getProviders().containsKey(provider)) {
            warnings.add("Unknown provider for role mapping: " + provider);
        }

        for (String fallback : mapping.getFallbackProviders()) {
            if (!snapshot.getProviders().containsKey(fallback)) {
                warnings.add("Unknown fallback provider: " + fallback);
            }
        }

        return new LlmRoleResolution(
            mapping.getRoleName(),
            provider,
            mapping.getModel(),
            mapping.getFallbackProviders(),
            warnings
        );
    }
}

