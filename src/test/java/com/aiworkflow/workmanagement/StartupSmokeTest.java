package com.aiworkflow.workmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test to ensure Spring context loads with all required beans wired.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class StartupSmokeTest {

    @TempDir
    static Path workspaceRoot;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("workmanagement.workspace-root", () -> workspaceRoot.toString());
        registry.add("workmanagement.workspace-name", () -> "test");
        registry.add("orchestration.execution-mode", () -> "template");
        registry.add("spring.devtools.restart.enabled", () -> "false");
    }

    @org.springframework.beans.factory.annotation.Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(com.aiworkflow.workmanagement.api.OrchestrationController.class)).isNotNull();
    }
}
