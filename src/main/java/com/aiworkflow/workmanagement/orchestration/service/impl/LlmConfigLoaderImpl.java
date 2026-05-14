package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.config.OrchestrationLlmProperties;
import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigDiagnostics;
import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleMapping;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LlmConfigLoaderImpl implements LlmConfigLoader {

    private static final Set<String> KNOWN_KEYS = Set.of("default-provider", "providers", "role-mappings", "dummy-providers");
    private static final Set<String> KNOWN_PROVIDER_KEYS = Set.of("api-key", "model", "enabled", "tokenizer-id", "tokenizer-version");

    private final Environment environment;
    private final OrchestrationLlmProperties properties;

    public LlmConfigLoaderImpl(Environment environment, OrchestrationLlmProperties properties) {
        this.environment = environment;
        this.properties = properties;
    }

    @Override
    public LlmConfigSnapshot load() {
        Map<String, Object> raw = Binder.get(environment)
            .bind("orchestration.llm", Bindable.mapOf(String.class, Object.class))
            .orElse(Map.of());

        List<String> ignored = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        raw.keySet().stream()
            .filter(key -> !KNOWN_KEYS.contains(key))
            .forEach(key -> ignored.add("orchestration.llm." + key));

        Map<String, LlmProviderConfig> providerConfigs = new HashMap<>();
        for (Map.Entry<String, OrchestrationLlmProperties.Provider> entry : properties.getProviders().entrySet()) {
            String providerName = entry.getKey();
            OrchestrationLlmProperties.Provider provider = entry.getValue();
            boolean dummy = properties.getDummyProviders().contains(providerName);
            providerConfigs.put(providerName, new LlmProviderConfig(
                providerName,
                provider.getApiKey(),
                provider.getModel(),
                provider.isEnabled(),
                dummy
            ));
        }

        Map<String, LlmRoleMapping> roleMappings = new HashMap<>();
        for (Map.Entry<String, OrchestrationLlmProperties.RoleMapping> entry : properties.getRoleMappings().entrySet()) {
            OrchestrationLlmProperties.RoleMapping mapping = entry.getValue();
            roleMappings.put(entry.getKey(), new LlmRoleMapping(
                entry.getKey(),
                mapping.getProvider(),
                mapping.getModel(),
                mapping.getFallbackProviders()
            ));
        }

        Map<String, Object> providerRaw = asMap(raw.get("providers"));
        for (Map.Entry<String, Object> entry : providerRaw.entrySet()) {
            Map<String, Object> providerFields = asMap(entry.getValue());
            providerFields.keySet().stream()
                .filter(field -> !KNOWN_PROVIDER_KEYS.contains(field))
                .forEach(field -> ignored.add("orchestration.llm.providers." + entry.getKey() + "." + field));
        }

        if (properties.getDefaultProvider() == null || properties.getDefaultProvider().isBlank()) {
            warnings.add("Default provider is not set; using 'anthropic' fallback");
        }

        LlmConfigDiagnostics diagnostics = new LlmConfigDiagnostics(ignored, warnings);
        return new LlmConfigSnapshot(providerConfigs, roleMappings, properties.getDefaultProvider(), diagnostics);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(entry.getKey().toString(), entry.getValue());
                }
            }
            return result;
        }
        return Map.of();
    }
}
