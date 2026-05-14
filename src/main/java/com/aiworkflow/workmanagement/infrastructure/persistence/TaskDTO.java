package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.TaskState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Data Transfer Object for Task persistence.
 * Maps between domain Task and JSON representation.
 */
public class TaskDTO {

    private final String id;
    private final String storyId;
    private final String title;
    private final String description;
    private final String state;
    private final String assignedTo;
    private final Integer estimatedHours;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant completedAt;
    private final String blockingReason;

    @JsonCreator
    public TaskDTO(
        @JsonProperty("id") String id,
        @JsonProperty("storyId") String storyId,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("state") String state,
        @JsonProperty("assignedTo") String assignedTo,
        @JsonProperty("estimatedHours") Integer estimatedHours,
        @JsonProperty("createdAt") Instant createdAt,
        @JsonProperty("updatedAt") Instant updatedAt,
        @JsonProperty("completedAt") Instant completedAt,
        @JsonProperty("blockingReason") String blockingReason
    ) {
        this.id = id;
        this.storyId = storyId;
        this.title = title;
        this.description = description;
        this.state = state;
        this.assignedTo = assignedTo;
        this.estimatedHours = estimatedHours;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.blockingReason = blockingReason;
    }

    // Getters for Jackson
    public String getId() { return id; }
    public String getStoryId() { return storyId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getState() { return state; }
    public String getAssignedTo() { return assignedTo; }
    public Integer getEstimatedHours() { return estimatedHours; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getBlockingReason() { return blockingReason; }

    /**
     * Converts domain Task to DTO.
     */
    public static TaskDTO fromDomain(Task task) {
        return new TaskDTO(
            task.getId().getValue(),
            task.getStoryId().getValue(),
            task.getTitle(),
            task.getDescription(),
            task.getState().name(),
            task.getAssignedTo(),
            task.getEstimatedHours(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getCompletedAt(),
            task.getBlockingReason()
        );
    }

    /**
     * Converts DTO to domain Task.
     */
    public Task toDomain() {
        Task task = new Task(
            new TaskId(id),
            new StoryId(storyId),
            title
        );

        // Set optional fields using reflection or package-private methods
        // For now, we'll need to update the Task class to support this
        if (description != null) {
            task.updateDescription(description);
        }
        if (assignedTo != null) {
            task.assignTo(assignedTo);
        }
        if (estimatedHours != null) {
            task.setEstimate(estimatedHours);
        }
        if (blockingReason != null) {
            task.block(blockingReason);
        }

        // Transition to the correct state
        if (state != null && !state.equals("PENDING")) {
            TaskState targetState = TaskState.valueOf(state);
            transitionToState(task, targetState);
        }

        return task;
    }

    /**
     * Helper method to transition task to target state.
     */
    private void transitionToState(Task task, TaskState targetState) {
        TaskState currentState = task.getState();

        // If already at target state, do nothing
        if (currentState == targetState) {
            return;
        }

        // Transition through valid states
        if (targetState == TaskState.IN_PROGRESS && currentState == TaskState.PENDING) {
            task.start();
        } else if (targetState == TaskState.COMPLETED) {
            if (currentState == TaskState.PENDING) {
                task.start();
            }
            if (task.getState() == TaskState.IN_PROGRESS) {
                task.complete();
            }
        } else if (targetState == TaskState.BLOCKED) {
            // Blocking is handled separately via block() method
        }
    }
}
