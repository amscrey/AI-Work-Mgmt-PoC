package com.aiworkflow.workmanagement.application.command;

import com.aiworkflow.workmanagement.domain.model.Role;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;

import java.util.Objects;

/**
 * Command to transition a Story to a new workflow state.
 */
public class TransitionStoryCommand {
    private final String storyId;
    private final WorkflowState targetState;
    private final Role initiator;
    private final String reason;

    public TransitionStoryCommand(String storyId, WorkflowState targetState, Role initiator) {
        this(storyId, targetState, initiator, null);
    }

    public TransitionStoryCommand(String storyId, WorkflowState targetState, Role initiator, String reason) {
        this.storyId = Objects.requireNonNull(storyId, "Story ID cannot be null");
        this.targetState = Objects.requireNonNull(targetState, "Target state cannot be null");
        this.initiator = Objects.requireNonNull(initiator, "Initiator cannot be null");
        this.reason = reason;
    }

    // Getters
    public String getStoryId() { return storyId; }
    public WorkflowState getTargetState() { return targetState; }
    public Role getInitiator() { return initiator; }
    public String getReason() { return reason; }
}
