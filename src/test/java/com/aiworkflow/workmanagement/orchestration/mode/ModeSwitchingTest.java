package com.aiworkflow.workmanagement.orchestration.mode;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.config.OrchestrationConfig;
import com.aiworkflow.workmanagement.orchestration.config.OrchestrationLlmProperties;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationContext;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import com.aiworkflow.workmanagement.orchestration.llm.LLMChatClient;
import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import com.aiworkflow.workmanagement.orchestration.node.llm.DesignLLMNode;
import com.aiworkflow.workmanagement.orchestration.node.llm.ResearchLLMNode;
import com.aiworkflow.workmanagement.orchestration.node.llm.TaskPlanningLLMNode;
import com.aiworkflow.workmanagement.orchestration.node.llm.TestStrategyLLMNode;
import com.aiworkflow.workmanagement.orchestration.node.template.DesignTemplateNode;
import com.aiworkflow.workmanagement.orchestration.node.template.ResearchTemplateNode;
import com.aiworkflow.workmanagement.orchestration.node.template.TaskPlanningTemplateNode;
import com.aiworkflow.workmanagement.orchestration.node.template.TestStrategyTemplateNode;
import com.aiworkflow.workmanagement.orchestration.service.PromptTemplateService;
import com.aiworkflow.workmanagement.orchestration.service.impl.PromptTemplateServiceImpl;
import com.aiworkflow.workmanagement.orchestration.service.StoryTokenUsageService;
import com.aiworkflow.workmanagement.orchestration.service.TokenEstimationService;
import com.aiworkflow.workmanagement.orchestration.service.impl.DefaultTokenEstimationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

class ModeSwitchingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(ModeSwitchingConfig.class);

    @Test
    void templateModeLoadsTemplateNodesOnly() {
        contextRunner
            .withPropertyValues(
                "orchestration.execution-mode=template",
                "orchestration.template.simulate-delay=false",
                "orchestration.template.delay-ms=0"
            )
            .run(context -> {
                assertThat(context.getBeanProvider(ResearchTemplateNode.class).getIfAvailable()).isNotNull();
                assertThat(context.getBeanProvider(TaskPlanningTemplateNode.class).getIfAvailable()).isNotNull();
                assertThat(context.getBeanProvider(DesignTemplateNode.class).getIfAvailable()).isNotNull();
                assertThat(context.getBeanProvider(TestStrategyTemplateNode.class).getIfAvailable()).isNotNull();

                assertThat(context.getBeanProvider(ResearchLLMNode.class).getIfAvailable()).isNull();
                assertThat(context.getBeanProvider(TaskPlanningLLMNode.class).getIfAvailable()).isNull();
                assertThat(context.getBeanProvider(DesignLLMNode.class).getIfAvailable()).isNull();
                assertThat(context.getBeanProvider(TestStrategyLLMNode.class).getIfAvailable()).isNull();
            });
    }

    @Test
    void llmModeLoadsLlmNodesOnly() {
        contextRunner
            .withPropertyValues("orchestration.execution-mode=llm")
            .run(context -> {
                assertThat(context.getBeanProvider(ResearchLLMNode.class).getIfAvailable()).isNotNull();
                assertThat(context.getBeanProvider(TaskPlanningLLMNode.class).getIfAvailable()).isNotNull();
                assertThat(context.getBeanProvider(DesignLLMNode.class).getIfAvailable()).isNotNull();
                assertThat(context.getBeanProvider(TestStrategyLLMNode.class).getIfAvailable()).isNotNull();

                assertThat(context.getBeanProvider(ResearchTemplateNode.class).getIfAvailable()).isNull();
                assertThat(context.getBeanProvider(TaskPlanningTemplateNode.class).getIfAvailable()).isNull();
                assertThat(context.getBeanProvider(DesignTemplateNode.class).getIfAvailable()).isNull();
                assertThat(context.getBeanProvider(TestStrategyTemplateNode.class).getIfAvailable()).isNull();
            });
    }

    @Test
    void templateAndLlmNodesProduceCompatibleArtifacts() {
        OrchestrationState state = buildState();
        OrchestrationConfig config = new OrchestrationConfig();
        config.getTemplate().setSimulateDelay(false);
        config.getTemplate().setDelayMs(0);

        PromptTemplateService promptTemplateService = new PromptTemplateServiceImpl();
        LLMChatClient llmChatClient = (role, prompt) -> new LlmChatResponse("mock-response", "test", "model", null);

        ResearchTemplateNode templateResearch = new ResearchTemplateNode(config);
        ResearchLLMNode llmResearch = new ResearchLLMNode(promptTemplateService, llmChatClient, null, null);
        assertCompatible(templateResearch.execute(state.copy()), llmResearch.execute(state.copy()));

        TaskPlanningTemplateNode templatePlanning = new TaskPlanningTemplateNode(config);
        TaskPlanningLLMNode llmPlanning = new TaskPlanningLLMNode(promptTemplateService, llmChatClient, null, null);
        assertCompatible(templatePlanning.execute(state.copy()), llmPlanning.execute(state.copy()));

        DesignTemplateNode templateDesign = new DesignTemplateNode(config);
        DesignLLMNode llmDesign = new DesignLLMNode(promptTemplateService, llmChatClient, null, null);
        assertCompatible(templateDesign.execute(state.copy()), llmDesign.execute(state.copy()));

        TestStrategyTemplateNode templateTest = new TestStrategyTemplateNode(config);
        TestStrategyLLMNode llmTest = new TestStrategyLLMNode(promptTemplateService, llmChatClient, null, null);
        assertCompatible(templateTest.execute(state.copy()), llmTest.execute(state.copy()));
    }

    private void assertCompatible(OrchestrationState templateState, OrchestrationState llmState) {
        assertThat(templateState.getArtifactsCreated()).hasSize(1);
        assertThat(llmState.getArtifactsCreated()).hasSize(1);
        assertThat(templateState.getArtifactsCreated().get(0).getName())
            .isEqualTo(llmState.getArtifactsCreated().get(0).getName());
        assertThat(templateState.getArtifactsCreated().get(0).getCreatedBy())
            .isEqualTo(llmState.getArtifactsCreated().get(0).getCreatedBy());
    }

    private OrchestrationState buildState() {
        Story story = new Story(
            new StoryId("STORY-012"),
            "Mode Switching",
            "Validate mode switching",
            WorkflowState.TODO,
            PrioritizationState.PRIORITIZED
        );
        story.setDescription("Validate template vs llm node compatibility.");
        story.addAcceptanceCriterion("Modes are compatible");

        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Validate orchestration modes")
            .build();

        return OrchestrationState.fromContext(context, "exec-mode");
    }

    @Configuration
    @EnableConfigurationProperties(OrchestrationConfig.class)
    @Import({
        ResearchTemplateNode.class,
        TaskPlanningTemplateNode.class,
        DesignTemplateNode.class,
        TestStrategyTemplateNode.class,
        ResearchLLMNode.class,
        TaskPlanningLLMNode.class,
        DesignLLMNode.class,
        TestStrategyLLMNode.class
    })
    static class ModeSwitchingConfig {

        @Bean
        PromptTemplateService promptTemplateService() {
            return new PromptTemplateServiceImpl();
        }

        @Bean
        LLMChatClient llmChatClient() {
            return (role, prompt) -> new LlmChatResponse("mock-response", "test", "model", null);
        }

        @Bean
        StoryTokenUsageService storyTokenUsageService() {
            return new StoryTokenUsageService() {
                @Override
                public void recordUsage(com.aiworkflow.workmanagement.orchestration.domain.LlmUsageRecord record) {
                    // no-op for tests
                }

                @Override
                public java.util.List<com.aiworkflow.workmanagement.orchestration.domain.LlmUsageRecord> getRecords(com.aiworkflow.workmanagement.domain.valueobject.StoryId storyId) {
                    return java.util.List.of();
                }

                @Override
                public com.aiworkflow.workmanagement.orchestration.domain.StoryTokenUsageSummary getSummary(com.aiworkflow.workmanagement.domain.valueobject.StoryId storyId) {
                    return new com.aiworkflow.workmanagement.orchestration.domain.StoryTokenUsageSummary(
                        storyId.getValue(),
                        new com.aiworkflow.workmanagement.orchestration.domain.TokenTotals(),
                        new com.aiworkflow.workmanagement.orchestration.domain.TokenTotals(),
                        java.util.List.of()
                    );
                }
            };
        }

        @Bean
        TokenEstimationService tokenEstimationService() {
            return new DefaultTokenEstimationService(new OrchestrationLlmProperties());
        }
    }
}
