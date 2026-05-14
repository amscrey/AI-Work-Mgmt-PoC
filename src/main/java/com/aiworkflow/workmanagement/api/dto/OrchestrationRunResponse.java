package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.orchestration.domain.ExecutionOutcome;

public class OrchestrationRunResponse {
    private final String executionId;
    private final String status;
    private final ExecutionOutcome outcome;
    private final AssistantResponse assistantResponse;

    public OrchestrationRunResponse(String executionId, String status, ExecutionOutcome outcome, AssistantResponse assistantResponse) {
        this.executionId = executionId;
        this.status = status;
        this.outcome = outcome;
        this.assistantResponse = assistantResponse;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getStatus() {
        return status;
    }

    public ExecutionOutcome getOutcome() {
        return outcome;
    }

    public AssistantResponse getAssistantResponse() {
        return assistantResponse;
    }
}
