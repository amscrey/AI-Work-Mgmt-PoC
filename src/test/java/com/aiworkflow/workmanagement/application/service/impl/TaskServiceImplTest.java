package com.aiworkflow.workmanagement.application.service.impl;

import com.aiworkflow.workmanagement.application.command.CreateTaskCommand;
import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.domain.model.*;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.repository.TaskRepository;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl Tests")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private ActivityLogger activityLogger;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Story parentStory;
    private StoryId storyId;

    @BeforeEach
    void setUp() {
        storyId = new StoryId("STORY-001");
        parentStory = new Story(
            storyId,
            "Parent Story",
            "Summary",
            WorkflowState.TODO,
            PrioritizationState.PRIORITIZED
        );
    }

    @Nested
    @DisplayName("Create Task Tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create task with all fields")
        void shouldCreateTaskWithAllFields() {
            // Given
            CreateTaskCommand command = new CreateTaskCommand(
                "TASK-001",
                "STORY-001",
                "Implement feature",
                "Detailed description",
                "agent",
                8
            );

            Task expectedTask = new Task(
                new TaskId("TASK-001"),
                storyId,
                "Implement feature"
            );

            when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
            when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);
            when(storyRepository.save(any(Story.class))).thenReturn(parentStory);

            // When
            Task result = taskService.createTask(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId().getValue()).isEqualTo("TASK-001");

            // Verify task was created with correct properties
            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(taskCaptor.capture());

            Task savedTask = taskCaptor.getValue();
            assertThat(savedTask.getTitle()).isEqualTo("Implement feature");
            assertThat(savedTask.getStoryId()).isEqualTo(storyId);

            // Verify task was added to story
            ArgumentCaptor<Story> storyCaptor = ArgumentCaptor.forClass(Story.class);
            verify(storyRepository).save(storyCaptor.capture());

            // Verify activity logging (role is "agent" from command.getAssignedTo())
            verify(activityLogger).logSuccess(
                eq("agent"),
                eq("create_task"),
                eq("TASK-001"),
                any(Map.class)
            );
        }

        @Test
        @DisplayName("Should create task without optional fields")
        void shouldCreateTaskWithoutOptionalFields() {
            // Given
            CreateTaskCommand command = new CreateTaskCommand(
                "TASK-002",
                "STORY-001",
                "Another task",
                null,   // no description
                null,   // not assigned
                null    // no estimate
            );

            Task expectedTask = new Task(
                new TaskId("TASK-002"),
                storyId,
                "Another task"
            );

            when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
            when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);
            when(storyRepository.save(any(Story.class))).thenReturn(parentStory);

            // When
            Task result = taskService.createTask(command);

            // Then
            assertThat(result).isNotNull();
            verify(taskRepository).save(any(Task.class));
            verify(storyRepository).save(parentStory);
        }

        @Test
        @DisplayName("Should throw exception when parent story not found")
        void shouldThrowExceptionWhenStoryNotFound() {
            // Given
            CreateTaskCommand command = new CreateTaskCommand(
                "TASK-003",
                "STORY-999",
                "Task title",
                "Description",
                null,
                null
            );

            when(storyRepository.findById(any(StoryId.class))).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> taskService.createTask(command))
                .isInstanceOf(TaskServiceImpl.StoryNotFoundException.class)
                .hasMessageContaining("STORY-999");

            verify(taskRepository, never()).save(any(Task.class));
            verify(activityLogger).logFailure(
                eq("system"),
                eq("create_task"),
                eq("TASK-003"),
                anyString(),
                any(Map.class)  // Now includes failure details
            );
        }

        @Test
        @DisplayName("Should log failure when repository throws exception")
        void shouldLogFailureOnRepositoryException() {
            // Given
            CreateTaskCommand command = new CreateTaskCommand(
                "TASK-004",
                "STORY-001",
                "Task",
                "Description",
                null,
                null
            );

            when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
            when(taskRepository.save(any(Task.class)))
                .thenThrow(new RuntimeException("Database error"));

            // When/Then
            assertThatThrownBy(() -> taskService.createTask(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

            verify(activityLogger).logFailure(
                eq("system"),
                eq("create_task"),
                eq("TASK-004"),
                eq("Database error"),
                any(Map.class)  // Now includes failure details
            );
        }
    }

    @Nested
    @DisplayName("Update Task Tests")
    class UpdateTaskTests {

        private Task existingTask;

        @BeforeEach
        void setUp() {
            existingTask = new Task(
                new TaskId("TASK-001"),
                storyId,
                "Original title"
            );
        }

        @Test
        @DisplayName("Should update task title and description")
        void shouldUpdateTaskTitleAndDescription() {
            // Given
            TaskId taskId = new TaskId("TASK-001");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

            // When
            Task result = taskService.updateTask(taskId, "New title", "New description");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("New title");
            assertThat(result.getDescription()).isEqualTo("New description");
            verify(taskRepository).save(existingTask);
            verify(activityLogger).logSuccess(
                eq("system"),
                eq("update_task"),
                eq("TASK-001"),
                any(Map.class)  // Now includes details of what was updated
            );
        }

        @Test
        @DisplayName("Should update only title when description is null")
        void shouldUpdateOnlyTitle() {
            // Given
            TaskId taskId = new TaskId("TASK-001");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

            // When
            Task result = taskService.updateTask(taskId, "Updated title", null);

            // Then
            assertThat(result.getTitle()).isEqualTo("Updated title");
            verify(taskRepository).save(existingTask);
        }

        @Test
        @DisplayName("Should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            TaskId taskId = new TaskId("TASK-999");

            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(taskId, "Title", "Description"))
                .isInstanceOf(TaskServiceImpl.TaskNotFoundException.class)
                .hasMessageContaining("TASK-999");

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Transition Task Tests")
    class TransitionTaskTests {

        private Task existingTask;

        @BeforeEach
        void setUp() {
            existingTask = new Task(
                new TaskId("TASK-001"),
                storyId,
                "Task to transition"
            );
        }

        @Test
        @DisplayName("Should transition task to IN_PROGRESS")
        void shouldTransitionTaskToInProgress() {
            // Given
            TaskId taskId = new TaskId("TASK-001");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

            // When
            Task result = taskService.transitionTask(taskId, TaskState.IN_PROGRESS);

            // Then
            assertThat(result.getState()).isEqualTo(TaskState.IN_PROGRESS);
            verify(taskRepository).save(existingTask);
            verify(activityLogger).logSuccess(
                eq("system"),
                eq("transition_task"),
                eq("TASK-001"),
                any(Map.class)
            );
        }

        @Test
        @DisplayName("Should transition task to COMPLETED")
        void shouldTransitionTaskToCompleted() {
            // Given
            TaskId taskId = new TaskId("TASK-001");
            existingTask.start();  // Move to IN_PROGRESS first

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

            // When
            Task result = taskService.transitionTask(taskId, TaskState.COMPLETED);

            // Then
            assertThat(result.getState()).isEqualTo(TaskState.COMPLETED);
            assertThat(result.isCompleted()).isTrue();
            verify(taskRepository).save(existingTask);
        }

        @Test
        @DisplayName("Should throw exception on invalid transition")
        void shouldThrowExceptionOnInvalidTransition() {
            // Given
            TaskId taskId = new TaskId("TASK-001");
            // Task is in PENDING state, cannot transition directly to COMPLETED

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

            // When/Then
            assertThatThrownBy(() -> taskService.transitionTask(taskId, TaskState.COMPLETED))
                .isInstanceOf(IllegalStateException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Assign Task Tests")
    class AssignTaskTests {

        private Task existingTask;

        @BeforeEach
        void setUp() {
            existingTask = new Task(
                new TaskId("TASK-001"),
                storyId,
                "Unassigned task"
            );
        }

        @Test
        @DisplayName("Should assign task to role")
        void shouldAssignTaskToRole() {
            // Given
            TaskId taskId = new TaskId("TASK-001");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

            // When
            Task result = taskService.assignTask(taskId, "agent");

            // Then
            assertThat(result.getAssignedTo()).isEqualTo("agent");
            verify(taskRepository).save(existingTask);
            verify(activityLogger).logSuccess(
                eq("system"),
                eq("assign_task"),
                eq("TASK-001"),
                any(Map.class)
            );
        }

        @Test
        @DisplayName("Should reassign task to different role")
        void shouldReassignTask() {
            // Given
            TaskId taskId = new TaskId("TASK-001");
            existingTask.assignTo("agent");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

            // When
            Task result = taskService.assignTask(taskId, "human-reviewer");

            // Then
            assertThat(result.getAssignedTo()).isEqualTo("human-reviewer");
            verify(taskRepository).save(existingTask);
        }
    }

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {

        @Test
        @DisplayName("Should find task by ID")
        void shouldFindTaskById() {
            // Given
            TaskId taskId = new TaskId("TASK-001");
            Task task = new Task(taskId, storyId, "Task title");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When
            Optional<Task> result = taskService.findById(taskId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(taskId);
            verify(taskRepository).findById(taskId);
        }

        @Test
        @DisplayName("Should return empty when task not found")
        void shouldReturnEmptyWhenTaskNotFound() {
            // Given
            TaskId taskId = new TaskId("TASK-999");

            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When
            Optional<Task> result = taskService.findById(taskId);

            // Then
            assertThat(result).isEmpty();
            verify(taskRepository).findById(taskId);
        }

        @Test
        @DisplayName("Should find tasks by story ID")
        void shouldFindTasksByStoryId() {
            // Given
            List<Task> expectedTasks = Arrays.asList(
                new Task(new TaskId("TASK-001"), storyId, "Task 1"),
                new Task(new TaskId("TASK-002"), storyId, "Task 2")
            );

            when(taskRepository.findByStoryId(storyId)).thenReturn(expectedTasks);

            // When
            List<Task> result = taskService.findByStoryId(storyId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t -> t.getStoryId().equals(storyId));
            verify(taskRepository).findByStoryId(storyId);
        }
    }

    @Nested
    @DisplayName("Delete Task Tests")
    class DeleteTaskTests {

        @Test
        @DisplayName("Should delete task successfully")
        void shouldDeleteTask() {
            // Given
            TaskId taskId = new TaskId("TASK-001");
            doNothing().when(taskRepository).delete(taskId);

            // When
            taskService.deleteTask(taskId);

            // Then
            verify(taskRepository).delete(taskId);
            verify(activityLogger).logSuccess(
                eq("system"),
                eq("delete_task"),
                eq("TASK-001"),
                eq(null)
            );
        }

        @Test
        @DisplayName("Should log failure when delete throws exception")
        void shouldLogFailureOnDeleteException() {
            // Given
            TaskId taskId = new TaskId("TASK-001");
            doThrow(new RuntimeException("Delete failed"))
                .when(taskRepository).delete(taskId);

            // When/Then
            assertThatThrownBy(() -> taskService.deleteTask(taskId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Delete failed");

            verify(activityLogger).logFailure(
                eq("system"),
                eq("delete_task"),
                eq("TASK-001"),
                eq("Delete failed"),
                eq(null)
            );
        }
    }
}
