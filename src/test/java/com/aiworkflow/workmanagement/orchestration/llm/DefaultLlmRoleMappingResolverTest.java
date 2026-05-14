package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigDiagnostics;
import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleMapping;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleResolution;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultLlmRoleMappingResolverTest {

    @Test
    void resolvesRoleWithPrecedence() {
        LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
            Map.of("anthropic", new LlmProviderConfig("anthropic", "key", "claude", true, false)),
            Map.of("researcher", new LlmRoleMapping("researcher", "anthropic", "claude", List.of("dummy"))),
            "anthropic",
            new LlmConfigDiagnostics(null, null)
        );

        LlmConfigLoader loader = () -> snapshot;
        DefaultLlmRoleMappingResolver resolver = new DefaultLlmRoleMappingResolver(loader);

        LlmRoleResolution resolution = resolver.resolve("researcher");

        assertThat(resolution.getProvider()).isEqualTo("anthropic");
        assertThat(resolution.getFallbackProviders()).contains("dummy");
    }
}

