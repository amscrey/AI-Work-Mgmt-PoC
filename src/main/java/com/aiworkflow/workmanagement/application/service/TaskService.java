package com.aiworkflow.workmanagement.application.service;

import com.aiworkflow.workmanagement.application.command.CreateTaskCommand;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.TaskState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing Tasks.
 */
public interface TaskService {

    /**
     * Creates a new task and adds it to a story.
     *
     * @param command The create task command
     * @return The created task
     */
    Task createTask(CreateTaskCommand command);

    /**
     * Updates a task.
     *
     * @param taskId The task ID
     * @param title New title (optional)
     * @param description New description (optional)
     * @return The updated task
     */
    Task updateTask(TaskId taskId, String title, String description);

    /**
     * Transitions a task to a new state.
     *
     * @param taskId The task ID
     * @param newState The new task state
     * @return The updated task
     */
    Task transitionTask(TaskId taskId, TaskState newState);

    /**
     * Assigns a task to a role.
     *
     * @param taskId The task ID
     * @param roleName The role name
     * @return The updated task
     */
    Task assignTask(TaskId taskId, String roleName);

    /**
     * Finds a task by ID.
     *
     * @param taskId The task ID
     * @return Optional containing the task if found
     */
    Optional<Task> findById(TaskId taskId);

    /**
     * Finds all tasks for a story.
     *
     * @param storyId The story ID
     * @return List of tasks for the story
     */
    List<Task> findByStoryId(StoryId storyId);

    /**
     * Deletes a task.
     *
     * @param taskId The task ID
     */
    void deleteTask(TaskId taskId);
}
