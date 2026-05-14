package com.aiworkflow.workmanagement.domain.repository;

import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Task entity.
 */
public interface TaskRepository {

    /**
     * Saves a task (create or update).
     *
     * @param task The task to save
     * @return The saved task
     */
    Task save(Task task);

    /**
     * Finds a task by its ID.
     *
     * @param id The task ID
     * @return Optional containing the task if found
     */
    Optional<Task> findById(TaskId id);

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
     * @param id The task ID
     */
    void delete(TaskId id);

    /**
     * Checks if a task exists.
     *
     * @param id The task ID
     * @return true if the task exists, false otherwise
     */
    boolean exists(TaskId id);
}
