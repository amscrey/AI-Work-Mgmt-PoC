package com.aiworkflow.workmanagement;

import com.aiworkflow.workmanagement.orchestration.llm.LLMChatClient;
import com.aiworkflow.workmanagement.orchestration.llm.RoutingLlmChatClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test to ensure Spring context loads in llm mode.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class StartupSmokeLlmModeTest {

    @TempDir
    static Path workspaceRoot;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("workmanagement.workspace-root", () -> workspaceRoot.toString());
        registry.add("workmanagement.workspace-name", () -> "test");
        registry.add("orchestration.execution-mode", () -> "llm");
        registry.add("spring.devtools.restart.enabled", () -> "false");
    }

    @org.springframework.beans.factory.annotation.Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(RoutingLlmChatClient.class)).isNotNull();
        assertThat(applicationContext.getBean(LLMChatClient.class)).isNotNull();
    }
}
