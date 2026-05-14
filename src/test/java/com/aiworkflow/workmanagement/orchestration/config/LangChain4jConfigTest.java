package com.aiworkflow.workmanagement.orchestration.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LangChain4jConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(LangChain4jConfig.class);

    @Test
    void whenExecutionModeIsTemplate_thenNoChatLanguageModelBean() {
        contextRunner.withPropertyValues("orchestration.execution-mode=template")
            .run(ctx -> assertThat(ctx.containsBean("chatLanguageModel")).isFalse());
    }

    @Test
    void whenExecutionModeIsLlm_thenChatLanguageModelBeanPresent() {
        contextRunner.withPropertyValues("orchestration.execution-mode=llm")
            .run(ctx -> assertThat(ctx.containsBean("chatLanguageModel")).isTrue());
    }
}

