package com.aiworkflow.workmanagement.domain.repository;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Story aggregate.
 * Implementations will handle persistence (filesystem, database, etc.)
 */
public interface StoryRepository {

    /**
     * Saves a story (create or update).
     *
     * @param story The story to save
     * @return The saved story
     */
    Story save(Story story);

    /**
     * Finds a story by its ID.
     *
     * @param id The story ID
     * @return Optional containing the story if found
     */
    Optional<Story> findById(StoryId id);

    /**
     * Finds all stories in a specific workflow state.
     *
     * @param state The workflow state
     * @return List of stories in the given state
     */
    List<Story> findByState(WorkflowState state);

    /**
     * Finds all stories with a specific prioritization.
     *
     * @param prioritization The prioritization state
     * @return List of stories with the given prioritization
     */
    List<Story> findByPrioritization(PrioritizationState prioritization);

    /**
     * Finds all stories.
     *
     * @return List of all stories
     */
    List<Story> findAll();

    /**
     * Deletes a story.
     *
     * @param id The story ID
     */
    void delete(StoryId id);

    /**
     * Checks if a story exists.
     *
     * @param id The story ID
     * @return true if the story exists, false otherwise
     */
    boolean exists(StoryId id);
}
