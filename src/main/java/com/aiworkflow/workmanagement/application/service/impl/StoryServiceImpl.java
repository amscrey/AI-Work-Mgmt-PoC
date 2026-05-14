package com.aiworkflow.workmanagement.application.service.impl;

import com.aiworkflow.workmanagement.application.command.CreateStoryCommand;
import com.aiworkflow.workmanagement.application.command.TransitionStoryCommand;
import com.aiworkflow.workmanagement.application.command.UpdateStoryCommand;
import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.application.service.StoryService;
import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.service.StateTransitionValidator;
import com.aiworkflow.workmanagement.domain.valueobject.RoleName;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of StoryService.
 * Note: Spring annotations (@Service, @Transactional) will be added when Spring Boot is configured.
 */
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final StateTransitionValidator stateTransitionValidator;
    private final ActivityLogger activityLogger;

    public StoryServiceImpl(StoryRepository storyRepository,
                           StateTransitionValidator stateTransitionValidator,
                           ActivityLogger activityLogger) {
        this.storyRepository = storyRepository;
        this.stateTransitionValidator = stateTransitionValidator;
        this.activityLogger = activityLogger;
    }

    @Override
    public Story createStory(CreateStoryCommand command) {
        String role = command.getAuthor() != null ? command.getAuthor() : "system";
        try {
            // Create the story entity
            StoryId storyId = new StoryId(command.getStoryId());
            Story story = new Story(
                storyId,
                command.getTitle(),
                command.getSummary(),
                command.getInitialState(),
                command.getPrioritization()
            );

            // Set optional fields
            if (command.getDescription() != null) {
                story.setDescription(command.getDescription());
            }
            if (command.getAuthor() != null) {
                story.setAuthor(new RoleName(command.getAuthor()));
            }
            if (command.getEstimatedEffort() != null) {
                story.setEstimatedEffort(command.getEstimatedEffort());
            }
            if (command.getAcceptanceCriteria() != null) {
                command.getAcceptanceCriteria().forEach(story::addAcceptanceCriterion);
            }
            if (command.getTags() != null) {
                command.getTags().forEach(story::addTag);
            }

            // Save the story
            Story savedStory = storyRepository.save(story);

            // Log the activity with enriched details
            Map<String, Object> details = new HashMap<>();
            details.put("title", command.getTitle());
            details.put("summary", command.getSummary());
            details.put("workflow_state", command.getInitialState().toString());
            details.put("prioritization", command.getPrioritization().toString());
            if (command.getEstimatedEffort() != null) {
                details.put("estimated_effort", command.getEstimatedEffort());
            }
            if (command.getTags() != null && !command.getTags().isEmpty()) {
                details.put("tags", command.getTags());
            }
            activityLogger.logSuccess(role, "create_story", command.getStoryId(), details);

            return savedStory;
        } catch (Exception e) {
            Map<String, Object> failureDetails = new HashMap<>();
            failureDetails.put("attempted_title", command.getTitle());
            activityLogger.logFailure(role, "create_story", command.getStoryId(), e.getMessage(), failureDetails);
            throw e;
        }
    }

    @Override
    public Story updateStory(UpdateStoryCommand command) {
        try {
            StoryId storyId = new StoryId(command.getStoryId());
            Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found: " + command.getStoryId()));

            // Track what's being updated
            Map<String, Object> details = new HashMap<>();

            // Update fields if provided
            if (command.getTitle() != null) {
                details.put("updated_title", command.getTitle());
                story.setTitle(command.getTitle());
            }
            if (command.getSummary() != null) {
                details.put("updated_summary", command.getSummary());
                story.setSummary(command.getSummary());
            }
            if (command.getDescription() != null) {
                details.put("updated_description", true);
                story.setDescription(command.getDescription());
            }
            if (command.getEstimatedEffort() != null) {
                details.put("updated_estimated_effort", command.getEstimatedEffort());
                story.setEstimatedEffort(command.getEstimatedEffort());
            }

            // Save the updated story
            Story updatedStory = storyRepository.save(story);

            // Log the activity with details of what changed
            activityLogger.logSuccess("system", "update_story", command.getStoryId(), details);

            return updatedStory;
        } catch (Exception e) {
            activityLogger.logFailure("system", "update_story", command.getStoryId(), e.getMessage(), null);
            throw e;
        }
    }

    @Override
    public Story transitionStory(TransitionStoryCommand command) {
        try {
            StoryId storyId = new StoryId(command.getStoryId());
            Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found: " + command.getStoryId()));

            // Validate the transition
            stateTransitionValidator.validateWorkflowTransition(story, command.getTargetState(), command.getInitiator());

            // Perform the transition
            story.transitionWorkflowState(command.getTargetState(), command.getInitiator());

            // Save the updated story
            Story updatedStory = storyRepository.save(story);

            // Log the activity
            Map<String, Object> details = new HashMap<>();
            details.put("from", story.getWorkflowState());
            details.put("to", command.getTargetState());
            details.put("initiator", command.getInitiator());
            if (command.getReason() != null) {
                details.put("reason", command.getReason());
            }
            activityLogger.logSuccess(command.getInitiator().name().toLowerCase(), "transition_story", command.getStoryId(), details);

            return updatedStory;
        } catch (Exception e) {
            activityLogger.logFailure("system", "transition_story", command.getStoryId(), e.getMessage(), null);
            throw e;
        }
    }

    @Override
    public Story changePrioritization(StoryId storyId, PrioritizationState newPrioritization) {
        try {
            Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found: " + storyId));

            // Note: The Story entity doesn't have changePrioritization method in current implementation
            // This would require re-saving the story with new prioritization
            // For now, log that this is not yet implemented
            throw new UnsupportedOperationException("changePrioritization not yet implemented in Story entity");
        } catch (Exception e) {
            activityLogger.logFailure("system", "change_prioritization", storyId.getValue(), e.getMessage(), null);
            throw e;
        }
    }

    @Override
    public Optional<Story> findById(StoryId storyId) {
        return storyRepository.findById(storyId);
    }

    @Override
    public List<Story> findByState(WorkflowState state) {
        return storyRepository.findByState(state);
    }

    @Override
    public List<Story> findByPrioritization(PrioritizationState prioritization) {
        return storyRepository.findByPrioritization(prioritization);
    }

    @Override
    public List<Story> findAll() {
        return storyRepository.findAll();
    }

    @Override
    public void deleteStory(StoryId storyId) {
        try {
            storyRepository.delete(storyId);
            activityLogger.logSuccess("system", "delete_story", storyId.getValue(), null);
        } catch (Exception e) {
            activityLogger.logFailure("system", "delete_story", storyId.getValue(), e.getMessage(), null);
            throw e;
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
