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

class TaskPlanningTemplateNodeTest {

    private OrchestrationConfig config;
    private TaskPlanningTemplateNode node;

    @BeforeEach
    void setUp() {
        config = new OrchestrationConfig();
        config.setExecutionMode("template");
        config.getTemplate().setSimulateDelay(false);

        node = new TaskPlanningTemplateNode(config);
    }

    @Test
    void shouldExecuteAndProduceArtifact() {
        // Given
        Story story = new Story(new StoryId("STORY-123"), "Test Story", "Test description", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Create task breakdown");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        assertThat(artifacts).hasSize(1);

        ArtifactReference artifact = artifacts.get(0);
        assertThat(artifact.getName()).isEqualTo("task-breakdown");
        assertThat(artifact.getCreatedBy()).isEqualTo("logician");
        assertThat(artifact.getContent()).isNotBlank();
        assertThat(artifact.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldIncludeStoryTitleInContent() {
        // Given
        Story story = new Story(new StoryId("STORY-456"), "Build Payment System", "Implement payment processing", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Plan tasks");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        List<ArtifactReference> artifacts = result.getArtifactsCreated();
        String content = artifacts.get(0).getContent();

        assertThat(content).contains("Build Payment System");
        assertThat(content).contains("Plan tasks");
    }

    @Test
    void shouldHaveExpectedContentStructure() {
        // Given
        Story story = new Story(new StoryId("STORY-789"), "Test Story", "Test description", WorkflowState.TODO, PrioritizationState.BACKLOG);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Plan");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        String content = result.getArtifactsCreated()
            .stream()
            .map(ref -> ((ArtifactReference) ref).getContent())
            .findFirst()
            .orElse("");

        assertThat(content).contains("# Task Breakdown:");
        assertThat(content).contains("## Overview");
        assertThat(content).contains("## Tasks");
        assertThat(content).contains("### Task 1:");
        assertThat(content).contains("### Task 2:");
        assertThat(content).contains("## Execution Order");
    }

    @Test
    void shouldUpdateCurrentRoleInState() {
        // Given
        Story story = new Story(new StoryId("STORY-123"), "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.STORY, story);
        initialState.put(OrchestrationState.WORK_INTENT, "Plan");

        OrchestrationState state = new OrchestrationState(initialState);

        // When
        OrchestrationState result = node.execute(state);

        // Then
        String currentRole = result.getCurrentRole();
        assertThat(currentRole).isEqualTo("logician");
    }
}
