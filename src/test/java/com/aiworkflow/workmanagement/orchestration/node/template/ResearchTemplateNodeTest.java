package com.aiworkflow.workmanagement.orchestration.node.template;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.config.OrchestrationConfig;
import com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResearchTemplateNodeTest {

    private OrchestrationConfig config;
    private ResearchTemplateNode node;

    @BeforeEach
    void setUp() {
        config = new OrchestrationConfig();
        config.setExecutionMode("template");
        config.getTemplate().setSimulateDelay(false); // Disable delay for tests

        node = new ResearchTemplateNode(config);
    }

    @Test
    void shouldExecuteAndProduceArtifact() {
        // Given: Initial state with story
        Story story = new Story(
            new StoryId("STORY-123"),
            "Test Story",
            "Test description",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Research game mechanics");

        OrchestrationState state = new OrchestrationState(initialState);

        // When: Execute node
        OrchestrationState result = node.execute(state);

        // Then: Artifact created
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        assertThat(artifacts).hasSize(1);

        ArtifactReference artifact = artifacts.get(0);
        assertThat(artifact.getName()).isEqualTo("research-findings");
        assertThat(artifact.getCreatedBy()).isEqualTo("researcher");
        assertThat(artifact.getContent()).isNotBlank();
        assertThat(artifact.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldIncludeStoryTitleInContent() {
        // Given
        Story story = new Story(new StoryId("STORY-123"), "Implement User Authentication", "Add login functionality", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Research auth options");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        ArtifactReference artifact = artifacts.get(0);

        assertThat(artifact.getContent()).contains("Implement User Authentication");
        assertThat(artifact.getContent()).contains("Research auth options");
    }

    @Test
    void shouldIncludeStoryDescriptionInContent() {
        // Given
        Story story = new Story(new StoryId("STORY-456"), "Test Story", "Summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        story.setDescription("Detailed story description with requirements");

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Research");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        ArtifactReference artifact = artifacts.get(0);

        assertThat(artifact.getContent()).contains("Detailed story description");
    }

    @Test
    void shouldHaveExpectedContentStructure() {
        // Given
        Story story = new Story(new StoryId("STORY-789"), "Test Story", "Test description", WorkflowState.TODO, PrioritizationState.BACKLOG);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Research");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        String content = artifacts.get(0).getContent();

        assertThat(content).contains("# Research Findings:");
        assertThat(content).contains("## Context");
        assertThat(content).contains("## Key Findings");
        assertThat(content).contains("## Recommendations");
        assertThat(content).contains("## Next Steps");
    }

    @Test
    void shouldUpdateCurrentRoleInState() {
        // Given
        Story story = new Story(new StoryId("STORY-123"), "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Research");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        String currentRole = result.getCurrentRole();
        assertThat(currentRole).isEqualTo("researcher");
    }

    @Test
    void shouldSimulateDelayWhenConfigured() {
        // Given
        config.getTemplate().setSimulateDelay(true);
        config.getTemplate().setDelayMs(100);

        Story story = new Story(new StoryId("STORY-123"), "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Research");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        long start = System.currentTimeMillis();
        node.execute(state);
        long duration = System.currentTimeMillis() - start;

        // Then: Should take at least delay time
        assertThat(duration).isGreaterThanOrEqualTo(100);
    }

    @Test
    void shouldHandleMissingWorkIntent() {
        // Given: State without work intent
        Story story = new Story(
            new StoryId("STORY-123"),
            "Test Story",
            "Test description",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        // No work intent

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then: Should still produce artifact
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        assertThat(artifacts).hasSize(1);
        assertThat(artifacts.get(0).getContent()).isNotBlank();
    }
}
