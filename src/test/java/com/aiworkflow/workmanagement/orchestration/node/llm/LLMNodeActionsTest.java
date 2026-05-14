package com.aiworkflow.workmanagement.orchestration.node.llm;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationContext;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import com.aiworkflow.workmanagement.orchestration.llm.LLMChatClient;
import com.aiworkflow.workmanagement.orchestration.service.PromptTemplateService;
import com.aiworkflow.workmanagement.orchestration.service.impl.PromptTemplateServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LLMNodeActionsTest {

    private final PromptTemplateService promptTemplateService = new PromptTemplateServiceImpl();
    private final LLMChatClient chatClient = (role, prompt) -> new com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse("mock-response", "test", "model", null);

    @Test
    void researchNodeAddsArtifact() {
        OrchestrationState state = buildState();
        ResearchLLMNode node = new ResearchLLMNode(promptTemplateService, chatClient, null, null);

        OrchestrationState result = node.execute(state);

        assertThat(result.getArtifactsCreated()).hasSize(1);
        assertThat(result.getArtifactsCreated().get(0).getName()).isEqualTo("research-findings");
        assertThat(result.getCurrentRole()).isEqualTo("researcher");
    }

    @Test
    void taskPlanningNodeAddsArtifact() {
        OrchestrationState state = buildState();
        TaskPlanningLLMNode node = new TaskPlanningLLMNode(promptTemplateService, chatClient, null, null);

        OrchestrationState result = node.execute(state);

        assertThat(result.getArtifactsCreated()).hasSize(1);
        assertThat(result.getArtifactsCreated().get(0).getName()).isEqualTo("task-breakdown");
        assertThat(result.getCurrentRole()).isEqualTo("logician");
    }

    @Test
    void designNodeAddsArtifact() {
        OrchestrationState state = buildState();
        DesignLLMNode node = new DesignLLMNode(promptTemplateService, chatClient, null, null);

        OrchestrationState result = node.execute(state);

        assertThat(result.getArtifactsCreated()).hasSize(1);
        assertThat(result.getArtifactsCreated().get(0).getName()).isEqualTo("design-document");
        assertThat(result.getCurrentRole()).isEqualTo("designer");
    }

    @Test
    void testStrategyNodeAddsArtifact() {
        OrchestrationState state = buildState();
        TestStrategyLLMNode node = new TestStrategyLLMNode(promptTemplateService, chatClient, null, null);

        OrchestrationState result = node.execute(state);

        assertThat(result.getArtifactsCreated()).hasSize(1);
        assertThat(result.getArtifactsCreated().get(0).getName()).isEqualTo("test-strategy");
        assertThat(result.getCurrentRole()).isEqualTo("tester");
    }

    private OrchestrationState buildState() {
        Story story = new Story(
            new StoryId("STORY-011"),
            "LLM Nodes",
            "Implement LLM nodes",
            WorkflowState.TODO,
            PrioritizationState.PRIORITIZED
        );
        story.setDescription("Build LLM node actions.");
        story.addAcceptanceCriterion("Mocked LLM responses");

        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Implement LLM nodes")
            .build();

        return OrchestrationState.fromContext(context, "exec-llm");
    }
}
