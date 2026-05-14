package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigDiagnostics;
import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleMapping;
import com.aiworkflow.workmanagement.orchestration.domain.LlmSummary;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultLlmSummaryServiceTest {

    @Test
    void buildsSummaryFromSnapshot() {
        LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
            Map.of("anthropic", new LlmProviderConfig("anthropic", "key", "claude", true, false)),
            Map.of("researcher", new LlmRoleMapping("researcher", "anthropic", "claude", null)),
            "anthropic",
            new LlmConfigDiagnostics(null, null)
        );

        LlmConfigLoader loader = () -> snapshot;
        DefaultLlmSummaryService service = new DefaultLlmSummaryService(loader);

        LlmSummary summary = service.getSummary();

        assertThat(summary.getProviders()).containsKey("anthropic");
        assertThat(summary.getRoleMappings()).containsKey("researcher");
        assertThat(summary.getDefaultProvider()).isEqualTo("anthropic");
    }
}

