package com.aiworkflow.workmanagement.application.service.impl;

import com.aiworkflow.workmanagement.application.command.CreateTaskCommand;
import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.application.service.TaskService;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.TaskState;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.repository.TaskRepository;
import com.aiworkflow.workmanagement.domain.valueobject.RoleName;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of TaskService.
 * Note: Spring annotations will be added when Spring Boot is configured.
 */
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final StoryRepository storyRepository;
    private final ActivityLogger activityLogger;

    public TaskServiceImpl(TaskRepository taskRepository,
                          StoryRepository storyRepository,
                          ActivityLogger activityLogger) {
        this.taskRepository = taskRepository;
        this.storyRepository = storyRepository;
        this.activityLogger = activityLogger;
    }

    @Override
    public Task createTask(CreateTaskCommand command) {
        String role = command.getAssignedTo() != null ? command.getAssignedTo() : "system";
        try {
            // Find the parent story
            StoryId storyId = new StoryId(command.getStoryId());
            Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found: " + command.getStoryId()));

            // Create the task
            TaskId taskId = new TaskId(command.getTaskId());
            Task task = new Task(taskId, storyId, command.getTitle());

            // Set optional fields
            if (command.getDescription() != null) {
                task.updateDescription(command.getDescription());
            }
            if (command.getAssignedTo() != null) {
                task.assignTo(command.getAssignedTo());
            }
            if (command.getEstimatedHours() != null) {
                task.setEstimate(command.getEstimatedHours());
            }

            // Save the task
            Task savedTask = taskRepository.save(task);

            // Add task to story
            story.addTask(savedTask);
            storyRepository.save(story);

            // Log the activity with enriched details
            Map<String, Object> details = new HashMap<>();
            details.put("title", command.getTitle());
            details.put("story_id", command.getStoryId());
            details.put("story_title", story.getTitle());
            details.put("state", TaskState.PENDING.toString());
            if (command.getAssignedTo() != null) {
                details.put("assigned_to", command.getAssignedTo());
            }
            if (command.getEstimatedHours() != null) {
                details.put("estimated_hours", command.getEstimatedHours());
            }
            activityLogger.logSuccess(role, "create_task", command.getTaskId(), details);

            return savedTask;
        } catch (Exception e) {
            Map<String, Object> failureDetails = new HashMap<>();
            failureDetails.put("attempted_title", command.getTitle());
            failureDetails.put("story_id", command.getStoryId());
            activityLogger.logFailure(role, "create_task", command.getTaskId(), e.getMessage(), failureDetails);
            throw e;
        }
    }

    @Override
    public Task updateTask(TaskId taskId, String title, String description) {
        try {
            Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));

            // Track what's being updated
            Map<String, Object> details = new HashMap<>();
            String role = task.getAssignedTo() != null ? task.getAssignedTo() : "system";

            if (title != null) {
                details.put("updated_title", title);
                task.updateTitle(title);
            }
            if (description != null) {
                details.put("updated_description", true);
                task.updateDescription(description);
            }

            Task updatedTask = taskRepository.save(task);

            activityLogger.logSuccess(role, "update_task", taskId.getValue(), details);

            return updatedTask;
        } catch (Exception e) {
            activityLogger.logFailure("system", "update_task", taskId.getValue(), e.getMessage(), null);
            throw e;
        }
    }

    @Override
    public Task transitionTask(TaskId taskId, TaskState newState) {
        try {
            Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));

            TaskState oldState = task.getState();
            String role = task.getAssignedTo() != null ? task.getAssignedTo() : "system";

            task.transitionState(newState);
            Task updatedTask = taskRepository.save(task);

            Map<String, Object> details = new HashMap<>();
            details.put("from_state", oldState.toString());
            details.put("to_state", newState.toString());
            details.put("title", task.getTitle());
            if (task.getAssignedTo() != null) {
                details.put("assigned_to", task.getAssignedTo());
            }
            activityLogger.logSuccess(role, "transition_task", taskId.getValue(), details);

            return updatedTask;
        } catch (Exception e) {
            Map<String, Object> failureDetails = new HashMap<>();
            failureDetails.put("attempted_state", newState.toString());
            activityLogger.logFailure("system", "transition_task", taskId.getValue(), e.getMessage(), failureDetails);
            throw e;
        }
    }

    @Override
    public Task assignTask(TaskId taskId, String roleName) {
        try {
            Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));

            task.assignTo(roleName);
            Task updatedTask = taskRepository.save(task);

            Map<String, Object> details = new HashMap<>();
            details.put("assignedTo", roleName);
            activityLogger.logSuccess("system", "assign_task", taskId.getValue(), details);

            return updatedTask;
        } catch (Exception e) {
            activityLogger.logFailure("system", "assign_task", taskId.getValue(), e.getMessage(), null);
            throw e;
        }
    }

    @Override
    public Optional<Task> findById(TaskId taskId) {
        return taskRepository.findById(taskId);
    }

    @Override
    public List<Task> findByStoryId(StoryId storyId) {
        return taskRepository.findByStoryId(storyId);
    }

    @Override
    public void deleteTask(TaskId taskId) {
        try {
            taskRepository.delete(taskId);
            activityLogger.logSuccess("system", "delete_task", taskId.getValue(), null);
        } catch (Exception e) {
            activityLogger.logFailure("system", "delete_task", taskId.getValue(), e.getMessage(), null);
            throw e;
        }
    }

    /**
     * Exception thrown when a task is not found.
     */
    public static class TaskNotFoundException extends RuntimeException {
        public TaskNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when a story is not found.
     */
    public static class StoryNotFoundException extends RuntimeException {
        public StoryNotFoundException(String message) {
            super(message);
        }
    }
}
