package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigDiagnostics;
import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleMapping;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingLlmChatClientTest {

    @Test
    void routesToPrimaryProvider() {
        LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
            Map.of("anthropic", new LlmProviderConfig("anthropic", null, "claude", null, true, false)),
            Map.of("tester", new LlmRoleMapping("tester", "anthropic", "claude", List.of())),
            "anthropic",
            new LlmConfigDiagnostics(null, null)
        );

        LlmConfigLoader loader = () -> snapshot;
        LlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);
        LlmRoleMappingResolver resolver = new DefaultLlmRoleMappingResolver(loader);
        RoutingLlmChatClient router = new RoutingLlmChatClient(registry, resolver);

        String response = router.generate("tester", "prompt").getText();

        // With no API key, should get stubbed response
        assertThat(response).contains("stubbed-response");
    }

    @Test
    void fallsBackWhenProviderMissing() {
        LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
            Map.of("dummy", new LlmProviderConfig("dummy", null, "instant", null, true, true)),
            Map.of("designer", new LlmRoleMapping("designer", "missing", null, List.of("dummy"))),
            "dummy",
            new LlmConfigDiagnostics(null, null)
        );

        LlmConfigLoader loader = () -> snapshot;
        LlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);
        LlmRoleMappingResolver resolver = new DefaultLlmRoleMappingResolver(loader);
        RoutingLlmChatClient router = new RoutingLlmChatClient(registry, resolver);

        String response = router.generate("designer", "prompt").getText();

        assertThat(response).contains("dummy-response");
    }
}
