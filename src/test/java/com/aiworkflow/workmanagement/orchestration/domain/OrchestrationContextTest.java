package com.aiworkflow.workmanagement.orchestration.domain;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.TaskState;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrchestrationContext Tests")
class OrchestrationContextTest {

    @TempDir
    Path tempDir;

    private Story story;
    private String workIntent;

    @BeforeEach
    void setUp() {
        story = new Story(
            new StoryId("STORY-123"),
            "Test Story",
            "Summary",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );
        story.setDescription("Test description");
        story.addAcceptanceCriterion("Criterion 1");
        story.addAcceptanceCriterion("Criterion 2");

        workIntent = "Research game mechanics";
    }

    @Test
    @DisplayName("Should create context with story and work intent")
    void shouldCreateContextWithStoryAndWorkIntent() {
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .build();

        assertThat(context.getStory()).isEqualTo(story);
        assertThat(context.getWorkIntent()).isEqualTo(workIntent);
        assertThat(context.getTasks()).isEmpty();
        assertThat(context.getReferenceFiles()).isEmpty();
    }

    @Test
    @DisplayName("Should create context with tasks")
    void shouldCreateContextWithTasks() {
        Task task1 = new Task(new TaskId("TASK-1"), story.getId(), "Task 1");
        Task task2 = new Task(new TaskId("TASK-2"), story.getId(), "Task 2");
        List<Task> tasks = List.of(task1, task2);

        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .tasks(tasks)
            .build();

        assertThat(context.getTasks()).hasSize(2);
        assertThat(context.getTasks()).containsExactly(task1, task2);
        assertThat(context.hasTasks()).isTrue();
    }

    @Test
    @DisplayName("Should create context with reference files")
    void shouldCreateContextWithReferenceFiles() throws IOException {
        Path refFile1 = tempDir.resolve("ref1.md");
        Path refFile2 = tempDir.resolve("ref2.md");
        Files.writeString(refFile1, "Reference 1 content");
        Files.writeString(refFile2, "Reference 2 content");

        List<ReferenceFile> referenceFiles = List.of(
            ReferenceFile.fromFile(refFile1),
            ReferenceFile.fromFile(refFile2)
        );

        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .referenceFiles(referenceFiles)
            .build();

        assertThat(context.getReferenceFiles()).hasSize(2);
        assertThat(context.hasReferenceFiles()).isTrue();
    }

    @Test
    @DisplayName("Should reject null story")
    void shouldRejectNullStory() {
        assertThatThrownBy(() -> OrchestrationContext.builder()
            .story(null)
            .workIntent(workIntent)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Story cannot be null");
    }

    @Test
    @DisplayName("Should reject null work intent")
    void shouldRejectNullWorkIntent() {
        assertThatThrownBy(() -> OrchestrationContext.builder()
            .story(story)
            .workIntent(null)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Work intent cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank work intent")
    void shouldRejectBlankWorkIntent() {
        assertThatThrownBy(() -> OrchestrationContext.builder()
            .story(story)
            .workIntent("   ")
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Work intent cannot be null or blank");
    }

    @Test
    @DisplayName("Should handle null tasks list")
    void shouldHandleNullTasksList() {
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .tasks(null)
            .build();

        assertThat(context.getTasks()).isEmpty();
        assertThat(context.hasTasks()).isFalse();
    }

    @Test
    @DisplayName("Should handle null reference files list")
    void shouldHandleNullReferenceFilesList() {
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .referenceFiles(null)
            .build();

        assertThat(context.getReferenceFiles()).isEmpty();
        assertThat(context.hasReferenceFiles()).isFalse();
    }

    @Test
    @DisplayName("Should return unmodifiable tasks list")
    void shouldReturnUnmodifiableTasksList() {
        Task task = new Task(new TaskId("TASK-1"), story.getId(), "Task 1");

        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .tasks(List.of(task))
            .build();

        assertThatThrownBy(() -> context.getTasks().add(task))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should return unmodifiable reference files list")
    void shouldReturnUnmodifiableReferenceFilesList() throws IOException {
        Path refFile = tempDir.resolve("ref.md");
        Files.writeString(refFile, "content");
        ReferenceFile ref = ReferenceFile.fromFile(refFile);

        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .referenceFiles(List.of(ref))
            .build();

        assertThatThrownBy(() -> context.getReferenceFiles().add(ref))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should estimate context size correctly")
    void shouldEstimateContextSizeCorrectly() throws IOException {
        Task task = new Task(new TaskId("TASK-1"), story.getId(), "Task with description");
        task.updateDescription("Task description here");

        Path refFile = tempDir.resolve("ref.md");
        String refContent = "Reference file content";
        Files.writeString(refFile, refContent);

        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .tasks(List.of(task))
            .referenceFiles(List.of(ReferenceFile.fromFile(refFile)))
            .build();

        long estimatedSize = context.estimateContextSize();

        // Should include story title, description, summary, acceptance criteria, tasks, and reference files
        assertThat(estimatedSize).isGreaterThan(0);
        assertThat(estimatedSize).isGreaterThan(story.getTitle().length());
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .build();

        String toString = context.toString();

        assertThat(toString).contains("STORY-123");
        assertThat(toString).contains(workIntent);
        assertThat(toString).contains("tasks=0");
        assertThat(toString).contains("referenceFiles=0");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        OrchestrationContext context1 = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .build();

        OrchestrationContext context2 = OrchestrationContext.builder()
            .story(story)
            .workIntent(workIntent)
            .build();

        Story differentStory = new Story(
            new StoryId("STORY-999"),
            "Different Story",
            "Summary",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );

        OrchestrationContext context3 = OrchestrationContext.builder()
            .story(differentStory)
            .workIntent(workIntent)
            .build();

        assertThat(context1).isEqualTo(context2);
        assertThat(context1.hashCode()).isEqualTo(context2.hashCode());
        assertThat(context1).isNotEqualTo(context3);
    }

    @Test
    @DisplayName("Should handle empty acceptance criteria")
    void shouldHandleEmptyAcceptanceCriteria() {
        Story storyWithoutCriteria = new Story(
            new StoryId("STORY-999"),
            "Story",
            "Summary",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );

        OrchestrationContext context = OrchestrationContext.builder()
            .story(storyWithoutCriteria)
            .workIntent(workIntent)
            .build();

        long estimatedSize = context.estimateContextSize();
        assertThat(estimatedSize).isGreaterThan(0);
    }
}
