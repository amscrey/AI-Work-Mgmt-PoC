package com.aiworkflow.workmanagement.application.service;

import com.aiworkflow.workmanagement.application.command.CreateStoryCommand;
import com.aiworkflow.workmanagement.application.command.TransitionStoryCommand;
import com.aiworkflow.workmanagement.application.command.UpdateStoryCommand;
import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing Stories.
 */
public interface StoryService {

    /**
     * Creates a new story.
     *
     * @param command The create story command
     * @return The created story
     */
    Story createStory(CreateStoryCommand command);

    /**
     * Updates an existing story.
     *
     * @param command The update story command
     * @return The updated story
     */
    Story updateStory(UpdateStoryCommand command);

    /**
     * Transitions a story to a new workflow state.
     *
     * @param command The transition command
     * @return The updated story
     */
    Story transitionStory(TransitionStoryCommand command);

    /**
     * Changes the prioritization state of a story.
     *
     * @param storyId The story ID
     * @param newPrioritization The new prioritization state
     * @return The updated story
     */
    Story changePrioritization(StoryId storyId, PrioritizationState newPrioritization);

    /**
     * Finds a story by ID.
     *
     * @param storyId The story ID
     * @return Optional containing the story if found
     */
    Optional<Story> findById(StoryId storyId);

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
     * @param storyId The story ID
     */
    void deleteStory(StoryId storyId);
}
