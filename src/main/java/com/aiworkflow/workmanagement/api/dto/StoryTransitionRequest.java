package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StoryTransitionRequest {
    @NotNull
    private WorkflowState targetState;
    @NotBlank
    private String initiatorRole;
    private String reason;

    public WorkflowState getTargetState() {
        return targetState;
    }

    public void setTargetState(WorkflowState targetState) {
        this.targetState = targetState;
    }

    public String getInitiatorRole() {
        return initiatorRole;
    }

    public void setInitiatorRole(String initiatorRole) {
        this.initiatorRole = initiatorRole;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

