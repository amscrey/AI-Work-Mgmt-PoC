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

class LlmDefaultProviderTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfig.class)
        .withPropertyValues(
            "orchestration.llm.providers.anthropic.api-key=token",
            "orchestration.llm.providers.anthropic.model=claude-3-5-sonnet-20241022"
        );

    @Test
    void defaultsToAnthropicWhenNotSet() {
        contextRunner.run(context -> {
            LlmConfigLoader loader = context.getBean(LlmConfigLoader.class);
            LlmConfigSnapshot snapshot = loader.load();

            assertThat(snapshot.getDefaultProvider()).isEqualTo("anthropic");
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

