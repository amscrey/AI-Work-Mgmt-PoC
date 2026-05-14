package com.aiworkflow.workmanagement.api.dto;

public class OrchestrationStatusResponse {
    private final String latestExecutionId;
    private final String status;

    public OrchestrationStatusResponse(String latestExecutionId, String status) {
        this.latestExecutionId = latestExecutionId;
        this.status = status;
    }

    public String getLatestExecutionId() {
        return latestExecutionId;
    }

    public String getStatus() {
        return status;
    }
}

