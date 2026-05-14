package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.config.OrchestrationLlmProperties;
import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

class LlmConfigLoaderImplTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfig.class)
        .withPropertyValues(
            "orchestration.llm.default-provider=anthropic",
            "orchestration.llm.providers.anthropic.api-key=token",
            "orchestration.llm.providers.anthropic.model=claude-3-5-sonnet-20241022",
            "orchestration.llm.providers.gemini.api-key=gemini-token",
            "orchestration.llm.providers.gemini.model=gemini-2.0-flash",
            "orchestration.llm.role-mappings.researcher.provider=gemini",
            "orchestration.llm.role-mappings.researcher.model=gemini-2.0-flash",
            "orchestration.llm.dummy-providers=dummy",
            "orchestration.llm.unknown-key=ignored"
        );

    @Test
    void loadsConfigAndDiagnostics() {
        contextRunner.run(context -> {
            LlmConfigLoader loader = context.getBean(LlmConfigLoader.class);
            LlmConfigSnapshot snapshot = loader.load();

            assertThat(snapshot.getProviders()).containsKey("anthropic");
            assertThat(snapshot.getProviders()).containsKey("gemini");
            assertThat(snapshot.getRoleMappings()).containsKey("researcher");
            assertThat(snapshot.getDiagnostics().getIgnoredEntries())
                .contains("orchestration.llm.unknown-key");
        });
    }

    @Configuration
    @EnableConfigurationProperties(OrchestrationLlmProperties.class)
    static class TestConfig {

        @Bean
        LlmConfigLoader llmConfigLoader(Environment environment, OrchestrationLlmProperties properties) {
            return new LlmConfigLoaderImpl(environment, properties);
        }
    }
}
