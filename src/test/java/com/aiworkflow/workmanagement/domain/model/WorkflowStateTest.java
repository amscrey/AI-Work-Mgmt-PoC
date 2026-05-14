package com.aiworkflow.workmanagement.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowState Enum Tests")
class WorkflowStateTest {

    @Test
    @DisplayName("Should allow agent to transition TODO to IN_PROGRESS")
    void shouldAllowAgentToTransitionTodoToInProgress() {
        boolean canTransition = WorkflowState.TODO.canTransitionTo(WorkflowState.IN_PROGRESS, Role.AGENT);

        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("Should allow agent to transition IN_PROGRESS to AWAITING_APPROVAL")
    void shouldAllowAgentToTransitionInProgressToAwaitingApproval() {
        boolean canTransition = WorkflowState.IN_PROGRESS.canTransitionTo(WorkflowState.AWAITING_APPROVAL, Role.AGENT);

        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("Should not allow agent to transition TODO directly to AWAITING_APPROVAL")
    void shouldNotAllowAgentToTransitionTodoToAwaitingApproval() {
        boolean canTransition = WorkflowState.TODO.canTransitionTo(WorkflowState.AWAITING_APPROVAL, Role.AGENT);

        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("Should not allow agent to transition to DONE")
    void shouldNotAllowAgentToTransitionToDone() {
        boolean canTransition = WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.DONE, Role.AGENT);

        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("Should allow human to transition AWAITING_APPROVAL to DONE (approve)")
    void shouldAllowHumanToApprove() {
        boolean canTransition = WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.DONE, Role.HUMAN_APPROVER);

        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("Should allow human to transition AWAITING_APPROVAL to IN_PROGRESS (reject)")
    void shouldAllowHumanToReject() {
        boolean canTransition = WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.IN_PROGRESS, Role.HUMAN_REVIEWER);

        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("Should allow human to transition DONE to IN_PROGRESS (reopen)")
    void shouldAllowHumanToReopen() {
        boolean canTransition = WorkflowState.DONE.canTransitionTo(WorkflowState.IN_PROGRESS, Role.HUMAN_APPROVER);

        assertThat(canTransition).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "TODO, DONE",
        "IN_PROGRESS, DONE",
        "AWAITING_APPROVAL, DONE",
        "DONE, DONE"
    })
    @DisplayName("Should allow human to cancel (transition to DONE from any state)")
    void shouldAllowHumanToCancel(WorkflowState from, WorkflowState to) {
        boolean canTransition = from.canTransitionTo(to, Role.HUMAN_APPROVER);

        // DONE to DONE is same state, should be false
        if (from == WorkflowState.DONE && to == WorkflowState.DONE) {
            assertThat(canTransition).isFalse();
        } else {
            assertThat(canTransition).isTrue();
        }
    }

    @Test
    @DisplayName("Should not allow transition to same state")
    void shouldNotAllowTransitionToSameState() {
        assertThat(WorkflowState.TODO.canTransitionTo(WorkflowState.TODO, Role.AGENT)).isFalse();
        assertThat(WorkflowState.IN_PROGRESS.canTransitionTo(WorkflowState.IN_PROGRESS, Role.AGENT)).isFalse();
        assertThat(WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.AWAITING_APPROVAL, Role.AGENT)).isFalse();
        assertThat(WorkflowState.DONE.canTransitionTo(WorkflowState.DONE, Role.AGENT)).isFalse();
    }

    @Test
    @DisplayName("Should convert to directory name correctly")
    void shouldConvertToDirectoryName() {
        assertThat(WorkflowState.TODO.getDirectoryName()).isEqualTo("todo");
        assertThat(WorkflowState.IN_PROGRESS.getDirectoryName()).isEqualTo("in-progress");
        assertThat(WorkflowState.AWAITING_APPROVAL.getDirectoryName()).isEqualTo("awaiting-approval");
        assertThat(WorkflowState.DONE.getDirectoryName()).isEqualTo("done");
    }

    @Test
    @DisplayName("Should convert from directory name correctly")
    void shouldConvertFromDirectoryName() {
        assertThat(WorkflowState.fromDirectoryName("todo")).isEqualTo(WorkflowState.TODO);
        assertThat(WorkflowState.fromDirectoryName("in-progress")).isEqualTo(WorkflowState.IN_PROGRESS);
        assertThat(WorkflowState.fromDirectoryName("awaiting-approval")).isEqualTo(WorkflowState.AWAITING_APPROVAL);
        assertThat(WorkflowState.fromDirectoryName("done")).isEqualTo(WorkflowState.DONE);
    }

    @Test
    @DisplayName("Should not allow agent backward transitions")
    void shouldNotAllowAgentBackwardTransitions() {
        assertThat(WorkflowState.IN_PROGRESS.canTransitionTo(WorkflowState.TODO, Role.AGENT)).isFalse();
        assertThat(WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.IN_PROGRESS, Role.AGENT)).isFalse();
        assertThat(WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.TODO, Role.AGENT)).isFalse();
    }

    @Test
    @DisplayName("Should not allow human to transition TODO to AWAITING_APPROVAL")
    void shouldNotAllowHumanToSkipInProgress() {
        boolean canTransition = WorkflowState.TODO.canTransitionTo(WorkflowState.AWAITING_APPROVAL, Role.HUMAN_REVIEWER);

        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("Should not allow human to transition DONE to TODO")
    void shouldNotAllowHumanToTransitionDoneToTodo() {
        boolean canTransition = WorkflowState.DONE.canTransitionTo(WorkflowState.TODO, Role.HUMAN_APPROVER);

        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("Should not allow human to transition DONE to AWAITING_APPROVAL")
    void shouldNotAllowHumanToTransitionDoneToAwaitingApproval() {
        boolean canTransition = WorkflowState.DONE.canTransitionTo(WorkflowState.AWAITING_APPROVAL, Role.HUMAN_REVIEWER);

        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("Should allow reviewer to approve")
    void shouldAllowReviewerToApprove() {
        boolean canTransition = WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.DONE, Role.HUMAN_REVIEWER);

        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("Should allow both reviewer and approver to transition AWAITING_APPROVAL to IN_PROGRESS")
    void shouldAllowBothReviewerAndApproverToReject() {
        assertThat(WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.IN_PROGRESS, Role.HUMAN_REVIEWER)).isTrue();
        assertThat(WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.IN_PROGRESS, Role.HUMAN_APPROVER)).isTrue();
    }

    @Test
    @DisplayName("Should implement full agent workflow progression")
    void shouldImplementFullAgentWorkflowProgression() {
        // Agent can only do linear progression
        assertThat(WorkflowState.TODO.canTransitionTo(WorkflowState.IN_PROGRESS, Role.AGENT)).isTrue();
        assertThat(WorkflowState.IN_PROGRESS.canTransitionTo(WorkflowState.AWAITING_APPROVAL, Role.AGENT)).isTrue();

        // Agent cannot complete work
        assertThat(WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.DONE, Role.AGENT)).isFalse();
    }

    @Test
    @DisplayName("Should implement full human approval workflow")
    void shouldImplementFullHumanApprovalWorkflow() {
        // Human can approve
        assertThat(WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.DONE, Role.HUMAN_APPROVER)).isTrue();

        // Human can reject (send back to in progress)
        assertThat(WorkflowState.AWAITING_APPROVAL.canTransitionTo(WorkflowState.IN_PROGRESS, Role.HUMAN_APPROVER)).isTrue();

        // Human can reopen completed work
        assertThat(WorkflowState.DONE.canTransitionTo(WorkflowState.IN_PROGRESS, Role.HUMAN_APPROVER)).isTrue();
    }
}
