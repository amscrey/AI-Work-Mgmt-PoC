package com.aiworkflow.workmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;

public class OrchestrationRunRequest {
    private String storyId;
    @NotBlank
    private String workIntent;
    @NotBlank
    private String requestedBy;
    private String executionMode;

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getWorkIntent() {
        return workIntent;
    }

    public void setWorkIntent(String workIntent) {
        this.workIntent = workIntent;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String executionMode) {
        this.executionMode = executionMode;
    }
}

