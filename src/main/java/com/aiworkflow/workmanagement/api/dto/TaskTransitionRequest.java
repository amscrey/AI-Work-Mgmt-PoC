package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.domain.model.TaskState;
import jakarta.validation.constraints.NotNull;

public class TaskTransitionRequest {
    @NotNull
    private TaskState targetState;

    public TaskState getTargetState() {
        return targetState;
    }

    public void setTargetState(TaskState targetState) {
        this.targetState = targetState;
    }
}

