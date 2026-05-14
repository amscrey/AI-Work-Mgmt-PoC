package com.aiworkflow.workmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskAssignRequest {
    @NotBlank
    private String assignedTo;

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}

