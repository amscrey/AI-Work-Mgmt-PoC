package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.repository.TaskRepository;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import com.aiworkflow.workmanagement.orchestration.config.OrchestrationConfig;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationContext;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationRequest;
import com.aiworkflow.workmanagement.orchestration.exception.OrchestrationContextException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContextAssemblyServiceImplTest {

    @TempDir
    Path tempDir;

    private StoryRepository storyRepository;
    private TaskRepository taskRepository;
    private WorkspaceConfig workspaceConfig;
    private OrchestrationConfig orchestrationConfig;
    private ContextAssemblyServiceImpl service;

    @BeforeEach
    void setUp() {
        storyRepository = mock(StoryRepository.class);
        taskRepository = mock(TaskRepository.class);
        workspaceConfig = new WorkspaceConfig(tempDir, "Test Workspace");
        orchestrationConfig = new OrchestrationConfig();

        service = new ContextAssemblyServiceImpl(
            storyRepository,
            taskRepository,
            workspaceConfig,
            orchestrationConfig
        );
    }

    @Test
    void shouldRejectNullRequest() {
        assertThatThrownBy(() -> service.assembleContext(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("request cannot be null");
    }

    @Test
    void shouldAssembleContextForExistingStory() {
        // Given
        StoryId storyId = new StoryId("STORY-123");
        Story story = new Story(storyId, "Test Story", "Test description", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        Task task1 = new Task(
            new TaskId("TASK-001"),
            storyId,
            "Task 1"
        );
        Task task2 = new Task(
            new TaskId("TASK-002"),
            storyId,
            "Task 2"
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(taskRepository.findByStoryId(storyId)).thenReturn(List.of(task1, task2));

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research game mechanics",
            "human"
        );

        // When
        OrchestrationContext context = service.assembleContext(request);

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getStory()).isEqualTo(story);
        assertThat(context.getWorkIntent()).isEqualTo("Research game mechanics");
        assertThat(context.getTasks()).hasSize(2);
        assertThat(context.getTasks()).containsExactly(task1, task2);
    }

    @Test
    void shouldThrowWhenStoryNotFound() {
        // Given
        StoryId storyId = new StoryId("STORY-999");
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        // When/Then
        assertThatThrownBy(() -> service.assembleContext(request))
            .isInstanceOf(OrchestrationContextException.class)
            .hasMessageContaining("Story not found");
    }

    @Test
    void shouldCreateProjectPlanningWhenStoryIdIsNull() {
        // Given
        OrchestrationRequest request = OrchestrationRequest.forProject(
            "Create project plan",
            "orchestrator"
        );

        StoryId projectPlanningId = new StoryId("PROJECT-PLANNING");

        // First call: PROJECT-PLANNING doesn't exist
        when(storyRepository.findById(projectPlanningId))
            .thenReturn(Optional.empty());

        // Capture and return saved story
        when(storyRepository.save(any(Story.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(taskRepository.findByStoryId(projectPlanningId))
            .thenReturn(List.of());

        // When
        OrchestrationContext context = service.assembleContext(request);

        // Then
        assertThat(context.getStory()).isNotNull();
        assertThat(context.getStory().getId().getValue()).isEqualTo("PROJECT-PLANNING");
        assertThat(context.getTasks()).isEmpty();

        // Verify PROJECT-PLANNING was saved
        verify(storyRepository).save(argThat(story ->
            story.getId().getValue().equals("PROJECT-PLANNING") &&
            story.getTitle().equals("Project Planning")
        ));
    }

    @Test
    void shouldReuseExistingProjectPlanning() {
        // Given
        OrchestrationRequest request = OrchestrationRequest.forProject(
            "Create project plan",
            "orchestrator"
        );

        StoryId projectPlanningId = new StoryId("PROJECT-PLANNING");
        Story existingProjectPlanning = new Story(projectPlanningId, "Project Planning", "Existing meta work item", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(projectPlanningId))
            .thenReturn(Optional.of(existingProjectPlanning));
        when(taskRepository.findByStoryId(projectPlanningId))
            .thenReturn(List.of());

        // When
        OrchestrationContext context = service.assembleContext(request);

        // Then
        assertThat(context.getStory()).isEqualTo(existingProjectPlanning);

        // Verify PROJECT-PLANNING was NOT saved (already exists)
        verify(storyRepository, never()).save(any());
    }

    @Test
    void shouldLoadReferenceFilesFromWorkspace() throws IOException {
        // Given
        Path referenceDir = tempDir.resolve("reference");
        Files.createDirectories(referenceDir);

        // Create reference files
        Files.writeString(referenceDir.resolve("reference1.md"), "# Reference 1\n\nContent");
        Files.writeString(referenceDir.resolve("reference2.md"), "# Reference 2\n\nContent");
        Files.writeString(referenceDir.resolve("ignored.txt"), "Not loaded"); // Non-md file

        StoryId storyId = new StoryId("STORY-123");
        Story story = new Story(storyId, "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(taskRepository.findByStoryId(storyId)).thenReturn(List.of());

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        // When
        OrchestrationContext context = service.assembleContext(request);

        // Then
        assertThat(context.getReferenceFiles()).hasSize(2);
        assertThat(context.getReferenceFiles())
            .allMatch(ref -> ref.getFilePath().getFileName().toString().endsWith(".md"));
    }

    @Test
    void shouldLimitReferenceFilesAccordingToConfig() throws IOException, InterruptedException {
        // Given
        orchestrationConfig.getContext().setMaxReferenceFiles(2);

        Path referenceDir = tempDir.resolve("reference");
        Files.createDirectories(referenceDir);

        // Create 5 reference files
        for (int i = 1; i <= 5; i++) {
            Files.writeString(
                referenceDir.resolve("ref" + i + ".md"),
                "# Reference " + i
            );
            Thread.sleep(10); // Ensure different timestamps
        }

        StoryId storyId = new StoryId("STORY-123");
        Story story = new Story(storyId, "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(taskRepository.findByStoryId(storyId)).thenReturn(List.of());

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        // When
        OrchestrationContext context = service.assembleContext(request);

        // Then: Only max allowed reference files loaded
        assertThat(context.getReferenceFiles()).hasSize(2);
    }

    @Test
    void shouldHandleMissingReferenceDirectory() {
        // Given: No reference directory exists
        StoryId storyId = new StoryId("STORY-123");
        Story story = new Story(storyId, "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(taskRepository.findByStoryId(storyId)).thenReturn(List.of());

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        // When
        OrchestrationContext context = service.assembleContext(request);

        // Then: Context created with empty reference files
        assertThat(context.getReferenceFiles()).isEmpty();
    }

    @Test
    void shouldApplyContextSizeLimits() throws IOException {
        // Given: Very small context limit
        orchestrationConfig.getContext().setMaxContextChars(500);

        Path referenceDir = tempDir.resolve("reference");
        Files.createDirectories(referenceDir);

        // Create large reference files
        String largeContent = "x".repeat(1000);
        for (int i = 1; i <= 5; i++) {
            Files.writeString(
                referenceDir.resolve("large" + i + ".md"),
                largeContent
            );
        }

        StoryId storyId = new StoryId("STORY-123");
        Story story = new Story(storyId, "Test", "Test", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(taskRepository.findByStoryId(storyId)).thenReturn(List.of());

        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        // When
        OrchestrationContext context = service.assembleContext(request);

        // Then: Context size limited (reference files removed to fit)
        assertThat(context.estimateContextSize()).isLessThan(orchestrationConfig.getContext().getMaxContextChars());
    }
}
