package com.aiworkflow.workmanagement.domain.model;

import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Story Entity Tests")
class StoryTest {

    private StoryId storyId;
    private String title;
    private String description;

    @BeforeEach
    void setUp() {
        storyId = new StoryId("STORY-1");
        title = "Test Story";
        description = "Test description";
    }

    @Test
    @DisplayName("Should create story with valid parameters")
    void shouldCreateStoryWithValidParameters() {
        Story story = new Story(storyId, title, description);

        assertThat(story.getId()).isEqualTo(storyId);
        assertThat(story.getTitle()).isEqualTo(title);
        assertThat(story.getDescription()).isEqualTo(description);
        assertThat(story.getWorkflowState()).isEqualTo(WorkflowState.TODO);
        assertThat(story.getPrioritizationState()).isEqualTo(PrioritizationState.BACKLOG);
        assertThat(story.getCreatedAt()).isNotNull();
        assertThat(story.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should reject null StoryId")
    void shouldRejectNullStoryId() {
        assertThatThrownBy(() -> new Story(null, title, description))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("StoryId cannot be null");
    }

    @Test
    @DisplayName("Should reject null title")
    void shouldRejectNullTitle() {
        assertThatThrownBy(() -> new Story(storyId, null, description))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Story title cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank title")
    void shouldRejectBlankTitle() {
        assertThatThrownBy(() -> new Story(storyId, "   ", description))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Story title cannot be null or blank");
    }

    @Test
    @DisplayName("Should accept null description")
    void shouldAcceptNullDescription() {
        Story story = new Story(storyId, title, null);

        assertThat(story.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should update title")
    void shouldUpdateTitle() {
        Story story = new Story(storyId, title, description);
        Instant originalUpdatedAt = story.getUpdatedAt();

        // Wait a tiny bit to ensure timestamp changes
        try { Thread.sleep(2); } catch (InterruptedException e) {}

        story.updateTitle("New Title");

        assertThat(story.getTitle()).isEqualTo("New Title");
        assertThat(story.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("Should reject null title in update")
    void shouldRejectNullTitleInUpdate() {
        Story story = new Story(storyId, title, description);

        assertThatThrownBy(() -> story.updateTitle(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Story title cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank title in update")
    void shouldRejectBlankTitleInUpdate() {
        Story story = new Story(storyId, title, description);

        assertThatThrownBy(() -> story.updateTitle("  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Story title cannot be null or blank");
    }

    @Test
    @DisplayName("Should update description")
    void shouldUpdateDescription() {
        Story story = new Story(storyId, title, description);
        Instant originalUpdatedAt = story.getUpdatedAt();

        try { Thread.sleep(2); } catch (InterruptedException e) {}

        story.updateDescription("New description");

        assertThat(story.getDescription()).isEqualTo("New description");
        assertThat(story.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("Should have equality based on id")
    void shouldHaveEqualityBasedOnId() {
        Story story1 = new Story(storyId, title, description);
        Story story2 = new Story(storyId, "Different Title", "Different Description");
        Story story3 = new Story(new StoryId("STORY-2"), title, description);

        assertThat(story1).isEqualTo(story2);
        assertThat(story1).isNotEqualTo(story3);
        assertThat(story1.hashCode()).isEqualTo(story2.hashCode());
    }

    @Test
    @DisplayName("Should return tasks as unmodifiable list")
    void shouldReturnTasksAsUnmodifiableList() {
        Story story = new Story(storyId, title, description);

        assertThatThrownBy(() -> story.getTasks().add(createTestTask()))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should return artifacts as unmodifiable list")
    void shouldReturnArtifactsAsUnmodifiableList() {
        Story story = new Story(storyId, title, description);

        assertThatThrownBy(() -> story.getArtifacts().add(createTestArtifact()))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should return comments as unmodifiable list")
    void shouldReturnCommentsAsUnmodifiableList() {
        Story story = new Story(storyId, title, description);

        assertThatThrownBy(() -> story.getComments().add(createTestComment()))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Nested
    @DisplayName("Task Management")
    class TaskManagement {

        @Test
        @DisplayName("Should add task")
        void shouldAddTask() {
            Story story = new Story(storyId, title, description);
            Task task = createTestTask();

            story.addTask(task);

            assertThat(story.getTasks()).hasSize(1);
            assertThat(story.getTasks()).contains(task);
        }

        @Test
        @DisplayName("Should reject null task")
        void shouldRejectNullTask() {
            Story story = new Story(storyId, title, description);

            assertThatThrownBy(() -> story.addTask(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task cannot be null");
        }

        @Test
        @DisplayName("Should count completed tasks")
        void shouldCountCompletedTasks() {
            Story story = new Story(storyId, title, description);
            Task task1 = createTestTask();
            Task task2 = createTestTask();
            task2.transitionState(TaskState.IN_PROGRESS);
            task2.transitionState(TaskState.COMPLETED);

            story.addTask(task1);
            story.addTask(task2);

            assertThat(story.countCompletedTasks()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should calculate task completion percentage")
        void shouldCalculateTaskCompletionPercentage() {
            Story story = new Story(storyId, title, description);
            Task task1 = createTestTask();
            Task task2 = createTestTask();
            task1.transitionState(TaskState.IN_PROGRESS);
            task1.transitionState(TaskState.COMPLETED);

            story.addTask(task1);
            story.addTask(task2);

            assertThat(story.getTaskCompletionPercentage()).isEqualTo(50.0);
        }
    }

    @Nested
    @DisplayName("State Transitions")
    class StateTransitions {

        @Test
        @DisplayName("Should allow valid agent transition")
        void shouldAllowValidAgentTransition() {
            Story story = new Story(storyId, title, description);
            story.assignTo("agent-1");

            story.transitionWorkflowState(WorkflowState.IN_PROGRESS, Role.AGENT);

            assertThat(story.getWorkflowState()).isEqualTo(WorkflowState.IN_PROGRESS);
        }

        @Test
        @DisplayName("Should reject invalid transition")
        void shouldRejectInvalidTransition() {
            Story story = new Story(storyId, title, description);

            assertThatThrownBy(() -> story.transitionWorkflowState(WorkflowState.DONE, Role.AGENT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from TODO to DONE");
        }

        @Test
        @DisplayName("Should allow human to cancel from any state")
        void shouldAllowHumanToCancelFromAnyState() {
            Story story = new Story(storyId, title, description);

            story.transitionWorkflowState(WorkflowState.DONE, Role.HUMAN_APPROVER);

            assertThat(story.getWorkflowState()).isEqualTo(WorkflowState.DONE);
        }
    }

    @Nested
    @DisplayName("Artifact Management")
    class ArtifactManagement {

        @Test
        @DisplayName("Should add artifact")
        void shouldAddArtifact() {
            Story story = new Story(storyId, title, description);
            Artifact artifact = createTestArtifact();

            story.addArtifact(artifact);

            assertThat(story.getArtifacts()).hasSize(1);
            assertThat(story.getArtifacts()).contains(artifact);
        }

        @Test
        @DisplayName("Should reject null artifact")
        void shouldRejectNullArtifact() {
            Story story = new Story(storyId, title, description);

            assertThatThrownBy(() -> story.addArtifact(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Artifact cannot be null");
        }
    }

    @Nested
    @DisplayName("Comment Management")
    class CommentManagement {

        @Test
        @DisplayName("Should add comment")
        void shouldAddComment() {
            Story story = new Story(storyId, title, description);
            Comment comment = createTestComment();

            story.addComment(comment);

            assertThat(story.getComments()).hasSize(1);
            assertThat(story.getComments()).contains(comment);
        }

        @Test
        @DisplayName("Should reject null comment")
        void shouldRejectNullComment() {
            Story story = new Story(storyId, title, description);

            assertThatThrownBy(() -> story.addComment(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment cannot be null");
        }
    }

    @Test
    @DisplayName("Should update prioritization state")
    void shouldUpdatePrioritizationState() {
        Story story = new Story(storyId, title, description);

        story.updatePrioritizationState(PrioritizationState.PRIORITIZED);

        assertThat(story.getPrioritizationState()).isEqualTo(PrioritizationState.PRIORITIZED);
    }

    @Test
    @DisplayName("Should reject null prioritization state")
    void shouldRejectNullPrioritizationState() {
        Story story = new Story(storyId, title, description);

        assertThatThrownBy(() -> story.updatePrioritizationState(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PrioritizationState cannot be null");
    }

    @Test
    @DisplayName("Should check if story is completed")
    void shouldCheckIfStoryIsCompleted() {
        Story story = new Story(storyId, title, description);

        assertThat(story.isCompleted()).isFalse();

        story.transitionWorkflowState(WorkflowState.DONE, Role.HUMAN_APPROVER);

        assertThat(story.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("Should check if story is in progress")
    void shouldCheckIfStoryIsInProgress() {
        Story story = new Story(storyId, title, description);
        story.assignTo("agent-1");

        assertThat(story.isInProgress()).isFalse();

        story.transitionWorkflowState(WorkflowState.IN_PROGRESS, Role.AGENT);

        assertThat(story.isInProgress()).isTrue();
    }

    @Test
    @DisplayName("Should check if story is prioritized")
    void shouldCheckIfStoryIsPrioritized() {
        Story story = new Story(storyId, title, description);

        assertThat(story.isPrioritized()).isFalse();

        story.updatePrioritizationState(PrioritizationState.PRIORITIZED);

        assertThat(story.isPrioritized()).isTrue();
    }

    @Test
    @DisplayName("Should assign to agent")
    void shouldAssignToAgent() {
        Story story = new Story(storyId, title, description);

        story.assignTo("agent-1");

        assertThat(story.getAssignedTo()).isEqualTo("agent-1");
    }

    @Test
    @DisplayName("Should return zero completion percentage for story with no tasks")
    void shouldReturnZeroCompletionPercentageForStoryWithNoTasks() {
        Story story = new Story(storyId, title, description);

        assertThat(story.getTaskCompletionPercentage()).isEqualTo(0.0);
    }

    // Helper methods
    private Task createTestTask() {
        return new Task(
            new TaskId("TASK-" + System.nanoTime()),
            storyId,
            "Test Task"
        );
    }

    private Artifact createTestArtifact() {
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now());
        return new Artifact(
            new com.aiworkflow.workmanagement.domain.valueobject.ArtifactName(
                "test__agent__" + timestamp + ".java"
            ),
            storyId,
            Path.of("/tmp/test.java"),
            "agent-1"
        );
    }

    private Comment createTestComment() {
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now());
        return new Comment(
            new com.aiworkflow.workmanagement.domain.valueobject.CommentId(
                "comment__agent__" + timestamp + ".md"
            ),
            storyId,
            "agent-1",
            "Test comment content",
            CommentType.NOTE
        );
    }
}
