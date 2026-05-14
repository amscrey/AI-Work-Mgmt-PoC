package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.TaskState;

import java.time.Instant;

public class TaskResponse {
    private final String taskId;
    private final String storyId;
    private final String title;
    private final String description;
    private final TaskState state;
    private final String assignedTo;
    private final Integer estimatedHours;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant completedAt;
    private final String blockingReason;

    public TaskResponse(
        String taskId,
        String storyId,
        String title,
        String description,
        TaskState state,
        String assignedTo,
        Integer estimatedHours,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        String blockingReason
    ) {
        this.taskId = taskId;
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

    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId().getValue(),
            task.getStoryId().getValue(),
            task.getTitle(),
            task.getDescription(),
            task.getState(),
            task.getAssignedTo(),
            task.getEstimatedHours(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getCompletedAt(),
            task.getBlockingReason()
        );
    }

    public String getTaskId() {
        return taskId;
    }

    public String getStoryId() {
        return storyId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskState getState() {
        return state;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getBlockingReason() {
        return blockingReason;
    }
}

