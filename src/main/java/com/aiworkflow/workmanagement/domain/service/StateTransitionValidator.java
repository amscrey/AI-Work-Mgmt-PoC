package com.aiworkflow.workmanagement.domain.service;

import com.aiworkflow.workmanagement.domain.model.Role;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.model.TaskState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain service responsible for validating state transitions.
 * Encapsulates complex business rules for state changes.
 */
public class StateTransitionValidator {

    /**
     * Alias for validateWorkflowTransition() for backward compatibility.
     */
    public void validateTransition(Story story, WorkflowState targetState, Role initiator) {
        validateWorkflowTransition(story, targetState, initiator);
    }

    /**
     * Validates a workflow state transition for a story.
     *
     * @param story     The story to transition
     * @param toState   The target state
     * @param initiator The role initiating the transition
     * @throws InvalidStateTransitionException if the transition is invalid
     */
    public void validateWorkflowTransition(Story story, WorkflowState toState, Role initiator) {
        if (story == null) {
            throw new IllegalArgumentException("Story cannot be null");
        }
        if (toState == null) {
            throw new IllegalArgumentException("Target state cannot be null");
        }
        if (initiator == null) {
            throw new IllegalArgumentException("Initiator role cannot be null");
        }

        WorkflowState currentState = story.getWorkflowState();

        // Check if transition is allowed by the state machine
        if (!currentState.canTransitionTo(toState, initiator)) {
            throw new InvalidStateTransitionException(currentState, toState, initiator);
        }

        // Additional business rules
        validateBusinessRules(story, currentState, toState, initiator);
    }

    /**
     * Validates a task state transition.
     *
     * @param task    The task to transition
     * @param toState The target state
     * @throws InvalidStateTransitionException if the transition is invalid
     */
    public void validateTaskTransition(Task task, TaskState toState) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if (toState == null) {
            throw new IllegalArgumentException("Target state cannot be null");
        }

        TaskState currentState = task.getState();

        // Check if transition is allowed by the state machine
        if (!currentState.canTransitionTo(toState)) {
            throw new InvalidStateTransitionException(currentState, toState);
        }
    }

    /**
     * Validates additional business rules for workflow transitions.
     */
    private void validateBusinessRules(Story story, WorkflowState fromState, WorkflowState toState, Role initiator) {
        // Rule: Agents cannot complete work directly, must go through approval
        if (initiator.isAgent() && toState == WorkflowState.DONE) {
            throw new InvalidStateTransitionException(
                "Agents cannot transition directly to DONE. Must go through AWAITING_APPROVAL."
            );
        }

        // Rule: Cannot transition to AWAITING_APPROVAL if there are no artifacts
        if (toState == WorkflowState.AWAITING_APPROVAL && story.getArtifacts().isEmpty()) {
            throw new InvalidStateTransitionException(
                "Cannot transition to AWAITING_APPROVAL without any artifacts"
            );
        }

        // Rule: Only approvers can approve (transition from AWAITING_APPROVAL to DONE)
        if (fromState == WorkflowState.AWAITING_APPROVAL &&
            toState == WorkflowState.DONE &&
            !initiator.canApprove()) {
            throw new InvalidStateTransitionException(
                "Only HUMAN_APPROVER role can approve work (transition from AWAITING_APPROVAL to DONE)"
            );
        }

        // Rule: Cannot start work (TODO -> IN_PROGRESS) if not assigned
        if (fromState == WorkflowState.TODO &&
            toState == WorkflowState.IN_PROGRESS &&
            story.getAssignedTo() == null) {
            throw new InvalidStateTransitionException(
                "Cannot start work on unassigned story"
            );
        }
    }

    /**
     * Checks if a workflow transition is valid without throwing an exception.
     *
     * @param story     The story to transition
     * @param toState   The target state
     * @param initiator The role initiating the transition
     * @return true if the transition is valid, false otherwise
     */
    public boolean isValidWorkflowTransition(Story story, WorkflowState toState, Role initiator) {
        try {
            validateWorkflowTransition(story, toState, initiator);
            return true;
        } catch (InvalidStateTransitionException e) {
            return false;
        }
    }

    /**
     * Checks if a task transition is valid without throwing an exception.
     *
     * @param task    The task to transition
     * @param toState The target state
     * @return true if the transition is valid, false otherwise
     */
    public boolean isValidTaskTransition(Task task, TaskState toState) {
        try {
            validateTaskTransition(task, toState);
            return true;
        } catch (InvalidStateTransitionException e) {
            return false;
        }
    }

    /**
     * Gets all valid next states for a story's current workflow state.
     *
     * @param story     The story
     * @param initiator The role initiating the transition
     * @return List of valid next states
     */
    public List<WorkflowState> getValidNextWorkflowStates(Story story, Role initiator) {
        List<WorkflowState> validStates = new ArrayList<>();

        for (WorkflowState state : WorkflowState.values()) {
            if (isValidWorkflowTransition(story, state, initiator)) {
                validStates.add(state);
            }
        }

        return validStates;
    }

    /**
     * Gets all valid next states for a task's current state.
     *
     * @param task The task
     * @return List of valid next states
     */
    public List<TaskState> getValidNextTaskStates(Task task) {
        List<TaskState> validStates = new ArrayList<>();

        for (TaskState state : TaskState.values()) {
            if (isValidTaskTransition(task, state)) {
                validStates.add(state);
            }
        }

        return validStates;
    }

    /**
     * Validates that all tasks in a story are completed before marking story as done.
     *
     * @param story The story to validate
     * @throws InvalidStateTransitionException if not all tasks are completed
     */
    public void validateAllTasksCompleted(Story story) {
        if (!story.getTasks().isEmpty()) {
            long incompleteTasks = story.getTasks().stream()
                .filter(task -> !task.isCompleted())
                .count();

            if (incompleteTasks > 0) {
                throw new InvalidStateTransitionException(
                    String.format("Cannot complete story with %d incomplete tasks", incompleteTasks)
                );
            }
        }
    }
}
