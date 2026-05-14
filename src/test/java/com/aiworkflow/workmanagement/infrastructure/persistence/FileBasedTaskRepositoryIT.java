package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.TaskState;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for FileBasedTaskRepository.
 * Tests actual file system operations.
 *
 * <p><b>Test Workspace:</b> {@code target/test-workspace-task}</p>
 *
 * <p>These integration tests verify task persistence with real file I/O operations.
 * Tests create parent stories in the workspace before testing task operations.
 * Each test runs in isolation with a clean workspace created in @BeforeEach.
 * Test artifacts are LEFT in target/ for inspection - next test run cleans up previous artifacts.</p>
 *
 * <p><b>For Developers (Human & AI):</b></p>
 * <ul>
 *   <li>📖 See {@code etc/docs/TESTING.md} for complete testing guide</li>
 *   <li>📖 Integration test workspace configuration documented in TESTING.md section 3.2</li>
 *   <li>⚠️ When modifying this test class, update etc/docs/TESTING.md to reflect changes</li>
 *   <li>⚠️ Keep test boundaries and verification scenarios documented</li>
 * </ul>
 *
 * <p><b>What This Test Suite Verifies:</b></p>
 * <ul>
 *   <li>Task JSON persistence in story's tasks/ directory</li>
 *   <li>Task state transitions (PENDING → IN_PROGRESS → COMPLETED/BLOCKED)</li>
 *   <li>Cross-directory task search (across all prioritization states)</li>
 *   <li>All task fields including timestamps, assignee, estimates</li>
 *   <li>Multi-task scenarios (multiple tasks per story)</li>
 *   <li>Task independence (updates don't affect other tasks)</li>
 *   <li>Error handling (orphan tasks, not found scenarios)</li>
 * </ul>
 *
 * @see com.aiworkflow.workmanagement.infrastructure.persistence.FileBasedTaskRepository
 */
@DisplayName("FileBasedTaskRepository Integration Tests")
class FileBasedTaskRepositoryIT {

    private static final String TEST_WORKSPACE = "target/test-workspace-task";
    private FileBasedTaskRepository taskRepository;
    private FileBasedStoryRepository storyRepository;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up test workspace from previous run (if exists)
        cleanUpWorkspace();

        // Create repositories
        storyRepository = new FileBasedStoryRepository(TEST_WORKSPACE);
        taskRepository = new FileBasedTaskRepository(TEST_WORKSPACE);

        // Create test stories for tasks to belong to
        createTestStories();
    }

    // NOTE: No @AfterEach cleanup - leaves test artifacts in target/ for inspection
    // Next test run will clean up in @BeforeEach

    private void cleanUpWorkspace() throws IOException {
        Path workspace = Paths.get(TEST_WORKSPACE);
        if (Files.exists(workspace)) {
            try (Stream<Path> walk = Files.walk(workspace)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
            }
        }
    }

    private void createTestStories() {
        // Create stories in different prioritization states
        Story story1 = new Story(
            new StoryId("STORY-001"),
            "Test Story 1",
            "Summary 1",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );

        Story story2 = new Story(
            new StoryId("STORY-002"),
            "Test Story 2",
            "Summary 2",
            WorkflowState.IN_PROGRESS,
            PrioritizationState.PRIORITIZED
        );

        storyRepository.save(story1);
        storyRepository.save(story2);
    }

    @Nested
    @DisplayName("Save and Find Operations")
    class SaveAndFindTests {

        @Test
        @DisplayName("Should save and retrieve a task")
        void shouldSaveAndRetrieveTask() {
            // Given
            Task task = new Task(
                new TaskId("TASK-001"),
                new StoryId("STORY-001"),
                "Implement feature"
            );
            task.updateDescription("Detailed description");

            // When
            taskRepository.save(task);

            // Then
            Optional<Task> retrieved = taskRepository.findById(new TaskId("TASK-001"));
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getId().getValue()).isEqualTo("TASK-001");
            assertThat(retrieved.get().getTitle()).isEqualTo("Implement feature");
            assertThat(retrieved.get().getDescription()).isEqualTo("Detailed description");
            assertThat(retrieved.get().getStoryId()).isEqualTo(new StoryId("STORY-001"));
            assertThat(retrieved.get().getState()).isEqualTo(TaskState.PENDING);
        }

        @Test
        @DisplayName("Should create task file in correct directory")
        void shouldCreateTaskFileInCorrectDirectory() throws IOException {
            // Given
            Task task = new Task(
                new TaskId("TASK-002"),
                new StoryId("STORY-001"),
                "Another task"
            );

            // When
            taskRepository.save(task);

            // Then
            Path taskFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-001", "tasks", "TASK-002.json");
            assertThat(Files.exists(taskFile)).isTrue();
            assertThat(Files.isRegularFile(taskFile)).isTrue();

            // Verify JSON content
            String content = Files.readString(taskFile);
            assertThat(content).contains("\"id\" : \"TASK-002\"");
            assertThat(content).contains("\"title\" : \"Another task\"");
            assertThat(content).contains("\"storyId\" : \"STORY-001\"");
        }

        @Test
        @DisplayName("Should update existing task")
        void shouldUpdateExistingTask() {
            // Given
            Task task = new Task(
                new TaskId("TASK-003"),
                new StoryId("STORY-001"),
                "Original title"
            );
            taskRepository.save(task);

            // When - update the task
            task.updateTitle("Updated title");
            task.updateDescription("New description");
            task.assignTo("agent");
            taskRepository.save(task);

            // Then
            Optional<Task> retrieved = taskRepository.findById(new TaskId("TASK-003"));
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getTitle()).isEqualTo("Updated title");
            assertThat(retrieved.get().getDescription()).isEqualTo("New description");
            assertThat(retrieved.get().getAssignedTo()).isEqualTo("agent");
        }

        @Test
        @DisplayName("Should return empty when task not found")
        void shouldReturnEmptyWhenNotFound() {
            // When
            Optional<Task> result = taskRepository.findById(new TaskId("TASK-999"));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find task across different prioritization directories")
        void shouldFindTaskAcrossPrioritizations() {
            // Given - task in PRIORITIZED story
            Task task = new Task(
                new TaskId("TASK-004"),
                new StoryId("STORY-002"),
                "Task in prioritized story"
            );
            taskRepository.save(task);

            // When
            Optional<Task> retrieved = taskRepository.findById(new TaskId("TASK-004"));

            // Then
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getStoryId()).isEqualTo(new StoryId("STORY-002"));
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryTests {

        @Test
        @DisplayName("Should find all tasks for a story")
        void shouldFindAllTasksForStory() {
            // Given
            Task task1 = new Task(new TaskId("TASK-101"), new StoryId("STORY-001"), "Task 1");
            Task task2 = new Task(new TaskId("TASK-102"), new StoryId("STORY-001"), "Task 2");
            Task task3 = new Task(new TaskId("TASK-103"), new StoryId("STORY-002"), "Task 3");

            taskRepository.save(task1);
            taskRepository.save(task2);
            taskRepository.save(task3);

            // When
            List<Task> story1Tasks = taskRepository.findByStoryId(new StoryId("STORY-001"));

            // Then
            assertThat(story1Tasks).hasSize(2);
            assertThat(story1Tasks).allMatch(t -> t.getStoryId().equals(new StoryId("STORY-001")));
            assertThat(story1Tasks).extracting(t -> t.getId().getValue())
                .containsExactlyInAnyOrder("TASK-101", "TASK-102");
        }

        @Test
        @DisplayName("Should return empty list when story has no tasks")
        void shouldReturnEmptyListWhenNoTasks() {
            // When
            List<Task> tasks = taskRepository.findByStoryId(new StoryId("STORY-001"));

            // Then
            assertThat(tasks).isEmpty();
        }

        @Test
        @DisplayName("Should find tasks in correct directory based on story prioritization")
        void shouldFindTasksInCorrectDirectory() {
            // Given - tasks in different prioritization levels
            Task backlogTask = new Task(new TaskId("TASK-201"), new StoryId("STORY-001"), "Backlog task");
            Task prioritizedTask = new Task(new TaskId("TASK-202"), new StoryId("STORY-002"), "Prioritized task");

            taskRepository.save(backlogTask);
            taskRepository.save(prioritizedTask);

            // When
            List<Task> backlogTasks = taskRepository.findByStoryId(new StoryId("STORY-001"));
            List<Task> prioritizedTasks = taskRepository.findByStoryId(new StoryId("STORY-002"));

            // Then
            assertThat(backlogTasks).hasSize(1);
            assertThat(backlogTasks.get(0).getId().getValue()).isEqualTo("TASK-201");

            assertThat(prioritizedTasks).hasSize(1);
            assertThat(prioritizedTasks.get(0).getId().getValue()).isEqualTo("TASK-202");
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete task file")
        void shouldDeleteTaskFile() throws IOException {
            // Given
            Task task = new Task(
                new TaskId("TASK-301"),
                new StoryId("STORY-001"),
                "To be deleted"
            );
            taskRepository.save(task);

            Path taskFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-001", "tasks", "TASK-301.json");
            assertThat(Files.exists(taskFile)).isTrue();

            // When
            taskRepository.delete(new TaskId("TASK-301"));

            // Then
            assertThat(Files.exists(taskFile)).isFalse();
            assertThat(taskRepository.findById(new TaskId("TASK-301"))).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent task")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            // When/Then
            assertThatThrownBy(() -> taskRepository.delete(new TaskId("TASK-999")))
                .isInstanceOf(FileBasedTaskRepository.RepositoryException.class)
                .hasMessageContaining("Task not found");
        }
    }

    @Nested
    @DisplayName("Exists Operations")
    class ExistsTests {

        @Test
        @DisplayName("Should return true when task exists")
        void shouldReturnTrueWhenTaskExists() {
            // Given
            Task task = new Task(
                new TaskId("TASK-401"),
                new StoryId("STORY-001"),
                "Exists"
            );
            taskRepository.save(task);

            // When/Then
            assertThat(taskRepository.exists(new TaskId("TASK-401"))).isTrue();
        }

        @Test
        @DisplayName("Should return false when task does not exist")
        void shouldReturnFalseWhenTaskDoesNotExist() {
            // When/Then
            assertThat(taskRepository.exists(new TaskId("TASK-999"))).isFalse();
        }
    }

    @Nested
    @DisplayName("Persistence Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Should persist all task fields")
        void shouldPersistAllTaskFields() {
            // Given
            Task task = new Task(
                new TaskId("TASK-501"),
                new StoryId("STORY-001"),
                "Full task"
            );
            task.updateDescription("Detailed description");
            task.assignTo("agent");
            task.setEstimate(8);

            // When
            taskRepository.save(task);

            // Then
            Optional<Task> retrieved = taskRepository.findById(new TaskId("TASK-501"));
            assertThat(retrieved).isPresent();

            Task loaded = retrieved.get();
            assertThat(loaded.getTitle()).isEqualTo("Full task");
            assertThat(loaded.getDescription()).isEqualTo("Detailed description");
            assertThat(loaded.getAssignedTo()).isEqualTo("agent");
            assertThat(loaded.getEstimatedHours()).isEqualTo(8);
            assertThat(loaded.getState()).isEqualTo(TaskState.PENDING);
        }

        @Test
        @DisplayName("Should persist task state transitions")
        void shouldPersistTaskStateTransitions() {
            // Given
            Task task = new Task(
                new TaskId("TASK-502"),
                new StoryId("STORY-001"),
                "Task with transitions"
            );

            // Transition through states
            task.start(); // PENDING -> IN_PROGRESS
            taskRepository.save(task);

            // When
            Optional<Task> retrieved = taskRepository.findById(new TaskId("TASK-502"));

            // Then
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getState()).isEqualTo(TaskState.IN_PROGRESS);
            assertThat(retrieved.get().isInProgress()).isTrue();
        }

        @Test
        @DisplayName("Should persist completed task with timestamp")
        void shouldPersistCompletedTask() {
            // Given
            Task task = new Task(
                new TaskId("TASK-503"),
                new StoryId("STORY-001"),
                "Completed task"
            );
            task.start();
            task.complete();
            taskRepository.save(task);

            // When
            Optional<Task> retrieved = taskRepository.findById(new TaskId("TASK-503"));

            // Then
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getState()).isEqualTo(TaskState.COMPLETED);
            assertThat(retrieved.get().isCompleted()).isTrue();
            assertThat(retrieved.get().getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should persist blocked task with reason")
        void shouldPersistBlockedTask() {
            // Given
            Task task = new Task(
                new TaskId("TASK-504"),
                new StoryId("STORY-001"),
                "Blocked task"
            );
            task.block("Waiting for API documentation");
            taskRepository.save(task);

            // When
            Optional<Task> retrieved = taskRepository.findById(new TaskId("TASK-504"));

            // Then
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getState()).isEqualTo(TaskState.BLOCKED);
            assertThat(retrieved.get().isBlocked()).isTrue();
            assertThat(retrieved.get().getBlockingReason()).isEqualTo("Waiting for API documentation");
        }

        @Test
        @DisplayName("Should persist timestamps correctly")
        void shouldPersistTimestamps() {
            // Given
            Task task = new Task(
                new TaskId("TASK-505"),
                new StoryId("STORY-001"),
                "Task with timestamps"
            );
            taskRepository.save(task);

            // When
            Optional<Task> retrieved = taskRepository.findById(new TaskId("TASK-505"));

            // Then
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getCreatedAt()).isNotNull();
            assertThat(retrieved.get().getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception when saving task for non-existent story")
        void shouldThrowExceptionWhenStoryNotFound() {
            // Given
            Task task = new Task(
                new TaskId("TASK-601"),
                new StoryId("STORY-999"),
                "Orphan task"
            );

            // When/Then
            assertThatThrownBy(() -> taskRepository.save(task))
                .isInstanceOf(FileBasedTaskRepository.RepositoryException.class)
                .hasMessageContaining("Story directory not found");
        }
    }

    @Nested
    @DisplayName("Multiple Tasks Tests")
    class MultipleTasksTests {

        @Test
        @DisplayName("Should handle multiple tasks in same story")
        void shouldHandleMultipleTasksInSameStory() {
            // Given
            for (int i = 1; i <= 5; i++) {
                Task task = new Task(
                    new TaskId("TASK-70" + i),
                    new StoryId("STORY-001"),
                    "Task " + i
                );
                taskRepository.save(task);
            }

            // When
            List<Task> tasks = taskRepository.findByStoryId(new StoryId("STORY-001"));

            // Then
            assertThat(tasks).hasSize(5);
        }

        @Test
        @DisplayName("Should maintain task independence")
        void shouldMaintainTaskIndependence() {
            // Given
            Task task1 = new Task(new TaskId("TASK-801"), new StoryId("STORY-001"), "Task 1");
            Task task2 = new Task(new TaskId("TASK-802"), new StoryId("STORY-001"), "Task 2");

            task1.assignTo("agent");
            task1.start();

            taskRepository.save(task1);
            taskRepository.save(task2);

            // When
            Optional<Task> retrieved1 = taskRepository.findById(new TaskId("TASK-801"));
            Optional<Task> retrieved2 = taskRepository.findById(new TaskId("TASK-802"));

            // Then
            assertThat(retrieved1.get().getState()).isEqualTo(TaskState.IN_PROGRESS);
            assertThat(retrieved1.get().getAssignedTo()).isEqualTo("agent");

            assertThat(retrieved2.get().getState()).isEqualTo(TaskState.PENDING);
            assertThat(retrieved2.get().getAssignedTo()).isNull();
        }
    }
}
