package com.aiworkflow.workmanagement.domain.model;

import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a Task within a Story.
 * Tasks are granular units of work that contribute to completing a Story.
 */
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private final TaskId id;
    private final StoryId storyId;
    private String title;
    private String description;
    private TaskState state;
    private String assignedTo;
    private Integer estimatedHours;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private String blockingReason;

    public Task(TaskId id, StoryId storyId, String title) {
        if (id == null) {
            throw new IllegalArgumentException("TaskId cannot be null");
        }
        if (storyId == null) {
            throw new IllegalArgumentException("StoryId cannot be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be null or blank");
        }

        this.id = id;
        this.storyId = storyId;
        this.title = title;
        this.state = TaskState.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public TaskId getId() {
        return id;
    }

    public StoryId getStoryId() {
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

    // Business methods
    public void updateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be null or blank");
        }
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void transitionState(TaskState newState) {
        if (!state.canTransitionTo(newState)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", state, newState)
            );
        }
        this.state = newState;
        this.updatedAt = Instant.now();

        if (newState == TaskState.COMPLETED) {
            this.completedAt = Instant.now();
        }
    }

    public void assignTo(String assignee) {
        this.assignedTo = assignee;
        this.updatedAt = Instant.now();
    }

    public void setEstimate(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Estimated hours cannot be negative");
        }
        this.estimatedHours = hours;
        this.updatedAt = Instant.now();
    }

    public void block(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Blocking reason cannot be null or blank");
        }
        transitionState(TaskState.BLOCKED);
        this.blockingReason = reason;
    }

    public void unblock() {
        if (state != TaskState.BLOCKED) {
            throw new IllegalStateException("Cannot unblock a task that is not blocked");
        }
        transitionState(TaskState.PENDING);
        this.blockingReason = null;
    }

    public void start() {
        transitionState(TaskState.IN_PROGRESS);
    }

    public void complete() {
        transitionState(TaskState.COMPLETED);
    }

    public boolean isCompleted() {
        return state == TaskState.COMPLETED;
    }

    public boolean isBlocked() {
        return state == TaskState.BLOCKED;
    }

    public boolean isInProgress() {
        return state == TaskState.IN_PROGRESS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
            "id=" + id +
            ", storyId=" + storyId +
            ", title='" + title + '\'' +
            ", state=" + state +
            ", assignedTo='" + assignedTo + '\'' +
            '}';
    }
}
