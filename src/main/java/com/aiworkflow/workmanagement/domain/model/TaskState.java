package com.aiworkflow.workmanagement.domain.model;

/**
 * Represents the state of a Task within a Story.
 * Simpler state model than WorkflowState for granular task tracking.
 */
public enum TaskState {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    BLOCKED;

    /**
     * Determines if a transition from this state to the target state is valid.
     *
     * @param target The target state
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(TaskState target) {
        if (this == target) {
            return false; // No transition to same state
        }

        return switch (this) {
            case PENDING -> target == IN_PROGRESS || target == BLOCKED;
            case IN_PROGRESS -> target == COMPLETED || target == BLOCKED || target == PENDING;
            case BLOCKED -> target == PENDING || target == IN_PROGRESS;
            case COMPLETED -> target == PENDING; // Can reopen completed tasks
        };
    }

    /**
     * Returns whether this state represents an active task.
     */
    public boolean isActive() {
        return this == IN_PROGRESS || this == BLOCKED;
    }

    /**
     * Returns whether this state represents a terminal state.
     */
    public boolean isTerminal() {
        return this == COMPLETED;
    }
}
