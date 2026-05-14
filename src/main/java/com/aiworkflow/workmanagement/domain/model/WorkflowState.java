package com.aiworkflow.workmanagement.domain.model;

/**
 * Represents the workflow state of a Story.
 * Includes transition logic based on initiator role.
 */
public enum WorkflowState {
    TODO,
    IN_PROGRESS,
    AWAITING_APPROVAL,
    DONE;

    /**
     * Determines if a transition from this state to the target state is valid
     * for the given initiator role.
     *
     * Agent transitions: TODO → IN_PROGRESS → AWAITING_APPROVAL (linear)
     * Human transitions: Can approve, reject, reopen, cancel
     *
     * @param target    The target state
     * @param initiator The role attempting the transition
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(WorkflowState target, Role initiator) {
        if (this == target) {
            return false; // No transition to same state
        }

        if (initiator.isAgent()) {
            // Agents follow linear progression
            return (this == TODO && target == IN_PROGRESS) ||
                   (this == IN_PROGRESS && target == AWAITING_APPROVAL);
        }

        if (initiator.isHuman()) {
            // Humans can:
            // - Approve: AWAITING_APPROVAL → DONE
            // - Reject: AWAITING_APPROVAL → IN_PROGRESS
            // - Reopen: DONE → IN_PROGRESS
            // - Cancel: any state → DONE
            return (this == AWAITING_APPROVAL && target == DONE) ||
                   (this == AWAITING_APPROVAL && target == IN_PROGRESS) ||
                   (this == DONE && target == IN_PROGRESS) ||
                   (target == DONE); // Can cancel from any state
        }

        return false;
    }

    /**
     * Returns the directory name for this workflow state.
     * Example: IN_PROGRESS returns "in-progress"
     */
    public String getDirectoryName() {
        return name().toLowerCase().replace('_', '-');
    }

    /**
     * Creates a WorkflowState from a directory name.
     * Example: "in-progress" returns IN_PROGRESS
     */
    public static WorkflowState fromDirectoryName(String directoryName) {
        return valueOf(directoryName.toUpperCase().replace('-', '_'));
    }
}
