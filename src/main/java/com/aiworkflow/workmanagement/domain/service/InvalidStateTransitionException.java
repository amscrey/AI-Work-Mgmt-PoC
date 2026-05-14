package com.aiworkflow.workmanagement.domain.service;

import com.aiworkflow.workmanagement.domain.model.Role;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.model.TaskState;

/**
 * Exception thrown when an invalid state transition is attempted.
 * This is a domain exception that encapsulates business rule violations.
 */
public class InvalidStateTransitionException extends RuntimeException {

    private final Object fromState;
    private final Object toState;
    private final Role initiator;

    public InvalidStateTransitionException(WorkflowState fromState, WorkflowState toState, Role initiator) {
        super(String.format(
            "Invalid workflow state transition: cannot transition from %s to %s for role %s",
            fromState, toState, initiator
        ));
        this.fromState = fromState;
        this.toState = toState;
        this.initiator = initiator;
    }

    public InvalidStateTransitionException(TaskState fromState, TaskState toState) {
        super(String.format(
            "Invalid task state transition: cannot transition from %s to %s",
            fromState, toState
        ));
        this.fromState = fromState;
        this.toState = toState;
        this.initiator = null;
    }

    public InvalidStateTransitionException(String message) {
        super(message);
        this.fromState = null;
        this.toState = null;
        this.initiator = null;
    }

    public InvalidStateTransitionException(String message, Throwable cause) {
        super(message, cause);
        this.fromState = null;
        this.toState = null;
        this.initiator = null;
    }

    public Object getFromState() {
        return fromState;
    }

    public Object getToState() {
        return toState;
    }

    public Role getInitiator() {
        return initiator;
    }

    public boolean hasStateInformation() {
        return fromState != null && toState != null;
    }

    public boolean hasInitiator() {
        return initiator != null;
    }
}
