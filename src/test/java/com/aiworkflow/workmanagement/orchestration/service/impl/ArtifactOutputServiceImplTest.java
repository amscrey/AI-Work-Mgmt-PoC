package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationRequest;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationResult;
import com.aiworkflow.workmanagement.orchestration.exception.ArtifactWriteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ArtifactOutputServiceImplTest {

    @TempDir
    Path tempDir;

    private StoryRepository storyRepository;
    private WorkspaceConfig workspaceConfig;
    private ActivityLogger activityLogger;
    private ArtifactOutputServiceImpl service;

    @BeforeEach
    void setUp() {
        storyRepository = mock(StoryRepository.class);
        workspaceConfig = new WorkspaceConfig(tempDir, "Test Workspace");
        activityLogger = mock(ActivityLogger.class);

        service = new ArtifactOutputServiceImpl(
            storyRepository,
            workspaceConfig,
            activityLogger
        );
    }

    @Test
    void shouldRejectNullRequest() {
        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            List.of(),
            "Done"
        );

        assertThatThrownBy(() -> service.writeArtifacts(null, result))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("request cannot be null");
    }

    @Test
    void shouldRejectNullResult() {
        OrchestrationRequest request = OrchestrationRequest.forStory(
            new StoryId("STORY-123"),
            "Research",
            "human"
        );

        assertThatThrownBy(() -> service.writeArtifacts(request, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("result cannot be null");
    }

    @Test
    void shouldWriteArtifactsToFilesystem() throws IOException {
        // Given
        StoryId storyId = new StoryId("STORY-123");
        Story story = new Story(
            storyId,
            "Test Story",
            "Test description",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(storyRepository.save(any(Story.class))).thenReturn(story);

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        ArtifactReference artifact = ArtifactReference.builder()
            .name("research-findings")
            .content("# Research Findings\n\nContent here")
            .createdBy("researcher")
            .build();

        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            List.of(artifact),
            "Completed"
        );

        // When
        service.writeArtifacts(request, result);

        // Then: Artifact file created
        Path artifactsDir = tempDir.resolve("backlog").resolve("STORY-123").resolve("artifacts");
        assertThat(artifactsDir).exists();

        List<Path> files = Files.list(artifactsDir).toList();
        assertThat(files).hasSize(1);

        Path artifactFile = files.get(0);
        assertThat(artifactFile.getFileName().toString())
            .matches("research-findings__researcher__\\d{4}-\\d{2}-\\d{2}T\\d{6}Z\\.md");

        String content = Files.readString(artifactFile);
        assertThat(content).isEqualTo("# Research Findings\n\nContent here");
    }

    @Test
    void shouldFollowNamingConvention() throws IOException {
        // Given
        StoryId storyId = new StoryId("STORY-456");
        Story story = new Story(storyId, "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(storyRepository.save(any(Story.class))).thenReturn(story);

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        ArtifactReference artifact = ArtifactReference.builder()
            .name("task breakdown")  // With spaces
            .content("Content")
            .createdBy("logician agent")  // With spaces
            .build();

        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            List.of(artifact),
            "Done"
        );

        // When
        service.writeArtifacts(request, result);

        // Then: Filename normalized (spaces to hyphens, lowercase)
        Path artifactsDir = tempDir.resolve("prioritized").resolve("STORY-456").resolve("artifacts");
        List<Path> files = Files.list(artifactsDir).toList();

        assertThat(files.get(0).getFileName().toString())
            .contains("task-breakdown")
            .contains("logician-agent");
    }

    @Test
    void shouldAddArtifactsToStoryAggregate() {
        // Given
        StoryId storyId = new StoryId("STORY-789");
        Story story = new Story(storyId, "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(storyRepository.save(any(Story.class))).thenReturn(story);

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        ArtifactReference artifact = ArtifactReference.builder()
            .name("test-artifact")
            .content("Content")
            .createdBy("tester")
            .build();

        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            List.of(artifact),
            "Done"
        );

        // When
        service.writeArtifacts(request, result);

        // Then: Story updated with artifact
        verify(storyRepository).save(argThat(savedStory ->
            savedStory.getArtifacts().size() == 1 &&
            savedStory.getArtifacts().get(0).getName().getValue().startsWith("test-artifact__tester__")
        ));
    }

    @Test
    void shouldLogArtifactCreation() {
        // Given
        StoryId storyId = new StoryId("STORY-123");
        Story story = new Story(storyId, "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(storyRepository.save(any(Story.class))).thenReturn(story);

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        ArtifactReference artifact = ArtifactReference.builder()
            .name("research-findings")
            .content("Content")
            .createdBy("researcher")
            .build();

        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            List.of(artifact),
            "Done"
        );

        // When
        service.writeArtifacts(request, result);

        // Then: Activity logged
        verify(activityLogger).logSuccess(
            eq("system"),
            eq("orchestration_artifact_created"),
            eq(storyId.getValue()),
            argThat(metadata -> {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) metadata;
                return map.get("artifact").equals("research-findings") &&
                       map.get("role").equals("researcher");
            })
        );
    }

    @Test
    void shouldWriteMultipleArtifacts() throws IOException {
        // Given
        StoryId storyId = new StoryId("STORY-999");
        Story story = new Story(storyId, "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(storyRepository.save(any(Story.class))).thenReturn(story);

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        ArtifactReference artifact1 = ArtifactReference.builder()
            .name("artifact-one")
            .content("Content 1")
            .createdBy("researcher")
            .build();

        ArtifactReference artifact2 = ArtifactReference.builder()
            .name("artifact-two")
            .content("Content 2")
            .createdBy("designer")
            .build();

        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            List.of(artifact1, artifact2),
            "Done"
        );

        // When
        service.writeArtifacts(request, result);

        // Then: Both artifacts written
        Path artifactsDir = tempDir.resolve("prioritized").resolve("STORY-999").resolve("artifacts");
        List<Path> files = Files.list(artifactsDir).toList();
        assertThat(files).hasSize(2);

        // Story updated with both artifacts
        verify(storyRepository).save(argThat(savedStory ->
            savedStory.getArtifacts().size() == 2
        ));
    }

    @Test
    void shouldThrowWhenStoryNotFound() {
        // Given
        StoryId storyId = new StoryId("STORY-999");  // Non-existent story
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            List.of(),
            "Done"
        );

        // When/Then
        assertThatThrownBy(() -> service.writeArtifacts(request, result))
            .isInstanceOf(ArtifactWriteException.class)
            .hasMessageContaining("Story not found");
    }

    @Test
    void shouldHandleProjectPlanningStory() throws IOException {
        // Given: Request with null storyId (PROJECT-PLANNING)
        StoryId projectPlanningId = new StoryId("PROJECT-PLANNING");
        Story projectPlanning = new Story(projectPlanningId, "Project Planning", "Meta work item", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(projectPlanningId)).thenReturn(Optional.of(projectPlanning));
        when(storyRepository.save(any(Story.class))).thenReturn(projectPlanning);

        OrchestrationRequest request = OrchestrationRequest.forProject(
            "Create plan",
            "orchestrator"
        );

        ArtifactReference artifact = ArtifactReference.builder()
            .name("project-plan")
            .content("# Project Plan")
            .createdBy("orchestrator")
            .build();

        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            List.of(artifact),
            "Done"
        );

        // When
        service.writeArtifacts(request, result);

        // Then: Artifact written to PROJECT-PLANNING directory
        Path artifactsDir = tempDir.resolve("prioritized").resolve("PROJECT-PLANNING").resolve("artifacts");
        assertThat(artifactsDir).exists();

        List<Path> files = Files.list(artifactsDir).toList();
        assertThat(files).hasSize(1);
    }
}
