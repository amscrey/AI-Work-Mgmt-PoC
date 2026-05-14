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

class DesignTemplateNodeTest {

    private OrchestrationConfig config;
    private DesignTemplateNode node;

    @BeforeEach
    void setUp() {
        config = new OrchestrationConfig();
        config.setExecutionMode("template");
        config.getTemplate().setSimulateDelay(false);

        node = new DesignTemplateNode(config);
    }

    @Test
    void shouldExecuteAndProduceArtifact() {
        // Given
        Story story = new Story(new StoryId("STORY-123"), "Test Story", "Test description", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Design architecture");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        assertThat(artifacts).hasSize(1);

        ArtifactReference artifact = artifacts.get(0);
        assertThat(artifact.getName()).isEqualTo("design-document");
        assertThat(artifact.getCreatedBy()).isEqualTo("designer");
        assertThat(artifact.getContent()).isNotBlank();
        assertThat(artifact.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldIncludeStoryTitleInContent() {
        // Given
        Story story = new Story(new StoryId("STORY-456"), "Implement Caching Layer", "Add Redis caching", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Design solution");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        String content = artifacts.get(0).getContent();

        assertThat(content).contains("Implement Caching Layer");
        assertThat(content).contains("Design solution");
    }

    @Test
    void shouldHaveExpectedContentStructure() {
        // Given
        Story story = new Story(new StoryId("STORY-789"), "Test Story", "Test description", WorkflowState.TODO, PrioritizationState.BACKLOG);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Design");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        String content = artifacts.get(0).getContent();

        assertThat(content).contains("# Design Document:");
        assertThat(content).contains("## Architecture Overview");
        assertThat(content).contains("## Design Decisions");
        assertThat(content).contains("## Implementation Approach");
        assertThat(content).contains("## API Design");
    }

    @Test
    void shouldUpdateCurrentRoleInState() {
        // Given
        Story story = new Story(new StoryId("STORY-123"), "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Design");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        String currentRole = result.getCurrentRole();
        assertThat(currentRole).isEqualTo("designer");
    }
}
