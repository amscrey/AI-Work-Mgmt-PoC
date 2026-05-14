package com.aiworkflow.workmanagement.orchestration.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrchestrationConfigTest {

    private OrchestrationConfig config;

    @BeforeEach
    void setUp() {
        config = new OrchestrationConfig();
    }

    @Test
    void shouldHaveDefaultExecutionMode() {
        assertThat(config.getExecutionMode()).isEqualTo("template");
    }

    @Test
    void shouldSetAndGetExecutionMode() {
        config.setExecutionMode("llm");
        assertThat(config.getExecutionMode()).isEqualTo("llm");
    }

    @Test
    void shouldIdentifyTemplateMode() {
        config.setExecutionMode("template");
        assertThat(config.isTemplateMode()).isTrue();
        assertThat(config.isLlmMode()).isFalse();
    }

    @Test
    void shouldIdentifyLlmMode() {
        config.setExecutionMode("llm");
        assertThat(config.isLlmMode()).isTrue();
        assertThat(config.isTemplateMode()).isFalse();
    }

    @Test
    void shouldBeTemplateModeCaseInsensitive() {
        config.setExecutionMode("TEMPLATE");
        assertThat(config.isTemplateMode()).isTrue();

        config.setExecutionMode("Template");
        assertThat(config.isTemplateMode()).isTrue();
    }

    @Test
    void shouldBeLlmModeCaseInsensitive() {
        config.setExecutionMode("LLM");
        assertThat(config.isLlmMode()).isTrue();

        config.setExecutionMode("Llm");
        assertThat(config.isLlmMode()).isTrue();
    }

    @Test
    void shouldHaveDefaultTemplateConfig() {
        assertThat(config.getTemplate()).isNotNull();
        assertThat(config.getTemplate().isSimulateDelay()).isTrue();
        assertThat(config.getTemplate().getDelayMs()).isEqualTo(1000);
    }

    @Test
    void shouldHaveDefaultContextConfig() {
        assertThat(config.getContext()).isNotNull();
        assertThat(config.getContext().getMaxReferenceFiles()).isEqualTo(5);
        assertThat(config.getContext().getMaxArtifacts()).isEqualTo(10);
        assertThat(config.getContext().getMaxContextChars()).isEqualTo(50_000);
    }

    @Test
    void shouldSetTemplateConfig() {
        OrchestrationConfig.TemplateConfig templateConfig = new OrchestrationConfig.TemplateConfig();
        templateConfig.setSimulateDelay(false);
        templateConfig.setDelayMs(500);

        config.setTemplate(templateConfig);

        assertThat(config.getTemplate().isSimulateDelay()).isFalse();
        assertThat(config.getTemplate().getDelayMs()).isEqualTo(500);
    }

    @Test
    void shouldSetContextConfig() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxReferenceFiles(10);
        contextConfig.setMaxArtifacts(20);
        contextConfig.setMaxContextChars(100_000);

        config.setContext(contextConfig);

        assertThat(config.getContext().getMaxReferenceFiles()).isEqualTo(10);
        assertThat(config.getContext().getMaxArtifacts()).isEqualTo(20);
        assertThat(config.getContext().getMaxContextChars()).isEqualTo(100_000);
    }

    @Test
    void shouldValidateSuccessfullyWithTemplateMode() {
        config.setExecutionMode("template");
        config.validate(); // Should not throw
    }

    @Test
    void shouldValidateSuccessfullyWithLlmMode() {
        config.setExecutionMode("llm");
        config.validate(); // Should not throw
    }

    @Test
    void shouldNormalizeExecutionModeOnValidation() {
        config.setExecutionMode("TEMPLATE");
        config.validate();
        assertThat(config.getExecutionMode()).isEqualTo("template");

        config.setExecutionMode("LLM");
        config.validate();
        assertThat(config.getExecutionMode()).isEqualTo("llm");
    }

    @Test
    void shouldRejectNullExecutionMode() {
        config.setExecutionMode(null);
        assertThatThrownBy(() -> config.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("execution-mode must be set");
    }

    @Test
    void shouldRejectEmptyExecutionMode() {
        config.setExecutionMode("");
        assertThatThrownBy(() -> config.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("execution-mode must be set");
    }

    @Test
    void shouldRejectBlankExecutionMode() {
        config.setExecutionMode("   ");
        assertThatThrownBy(() -> config.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("execution-mode must be set");
    }

    @Test
    void shouldRejectInvalidExecutionMode() {
        config.setExecutionMode("invalid");
        assertThatThrownBy(() -> config.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be 'template' or 'llm'")
            .hasMessageContaining("invalid");
    }

    @Test
    void shouldValidateTemplateConfig() {
        OrchestrationConfig.TemplateConfig templateConfig = new OrchestrationConfig.TemplateConfig();
        templateConfig.setDelayMs(-100);

        config.setTemplate(templateConfig);

        assertThatThrownBy(() -> config.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("delay-ms must be non-negative");
    }

    @Test
    void shouldValidateContextConfig() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxReferenceFiles(-1);

        config.setContext(contextConfig);

        assertThatThrownBy(() -> config.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("max-reference-files must be non-negative");
    }

    // TemplateConfig tests

    @Test
    void shouldSetTemplateSimulateDelay() {
        OrchestrationConfig.TemplateConfig templateConfig = new OrchestrationConfig.TemplateConfig();
        templateConfig.setSimulateDelay(false);
        assertThat(templateConfig.isSimulateDelay()).isFalse();
    }

    @Test
    void shouldSetTemplateDelayMs() {
        OrchestrationConfig.TemplateConfig templateConfig = new OrchestrationConfig.TemplateConfig();
        templateConfig.setDelayMs(2000);
        assertThat(templateConfig.getDelayMs()).isEqualTo(2000);
    }

    @Test
    void shouldValidateTemplateConfigWithZeroDelay() {
        OrchestrationConfig.TemplateConfig templateConfig = new OrchestrationConfig.TemplateConfig();
        templateConfig.setDelayMs(0);
        templateConfig.validate(); // Should not throw
    }

    @Test
    void shouldRejectNegativeTemplateDelay() {
        OrchestrationConfig.TemplateConfig templateConfig = new OrchestrationConfig.TemplateConfig();
        templateConfig.setDelayMs(-1);
        assertThatThrownBy(() -> templateConfig.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("delay-ms must be non-negative");
    }

    // ContextConfig tests

    @Test
    void shouldSetContextMaxReferenceFiles() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxReferenceFiles(15);
        assertThat(contextConfig.getMaxReferenceFiles()).isEqualTo(15);
    }

    @Test
    void shouldSetContextMaxArtifacts() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxArtifacts(25);
        assertThat(contextConfig.getMaxArtifacts()).isEqualTo(25);
    }

    @Test
    void shouldSetContextMaxContextChars() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxContextChars(75_000);
        assertThat(contextConfig.getMaxContextChars()).isEqualTo(75_000);
    }

    @Test
    void shouldValidateContextConfigWithZeroReferenceFiles() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxReferenceFiles(0);
        contextConfig.validate(); // Should not throw
    }

    @Test
    void shouldValidateContextConfigWithZeroArtifacts() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxArtifacts(0);
        contextConfig.validate(); // Should not throw
    }

    @Test
    void shouldRejectNegativeMaxReferenceFiles() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxReferenceFiles(-1);
        assertThatThrownBy(() -> contextConfig.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("max-reference-files must be non-negative");
    }

    @Test
    void shouldRejectNegativeMaxArtifacts() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxArtifacts(-5);
        assertThatThrownBy(() -> contextConfig.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("max-artifacts must be non-negative");
    }

    @Test
    void shouldRejectZeroMaxContextChars() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxContextChars(0);
        assertThatThrownBy(() -> contextConfig.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("max-context-chars must be positive");
    }

    @Test
    void shouldRejectNegativeMaxContextChars() {
        OrchestrationConfig.ContextConfig contextConfig = new OrchestrationConfig.ContextConfig();
        contextConfig.setMaxContextChars(-1000);
        assertThatThrownBy(() -> contextConfig.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("max-context-chars must be positive");
    }
}
