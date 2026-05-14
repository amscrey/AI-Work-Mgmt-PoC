package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigDiagnostics;
import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmProviderConfig;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultLlmProviderRegistryTest {

    @Test
    void buildsDummyAndStubClients() {
        LlmConfigSnapshot snapshot = new LlmConfigSnapshot(
            Map.of(
                "dummy", new LlmProviderConfig("dummy", null, "dummy", true, true),
                "gemini", new LlmProviderConfig("gemini", "", "gemini-2.0-flash", true, false)
            ),
            Map.of(),
            "anthropic",
            new LlmConfigDiagnostics(null, null)
        );

        LlmConfigLoader loader = () -> snapshot;
        DefaultLlmProviderRegistry registry = new DefaultLlmProviderRegistry(loader);

        assertThat(registry.getClient("dummy")).isInstanceOf(DummyChatClient.class);
        assertThat(registry.getClient("gemini")).isInstanceOf(StubbedProviderChatClient.class);
    }
}
