package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.orchestration.domain.ExecutionOutcome;

import java.time.Instant;
import java.util.List;

public class OrchestrationRunDetailResponse {
    private final String executionId;
    private final String status;
    private final ExecutionOutcome outcome;
    private final String executionSummary;
    private final List<String> errors;
    private final List<ArtifactReferenceResponse> artifacts;
    private final Instant startedAt;
    private final Instant completedAt;
    private final AssistantResponse assistantResponse;

    public OrchestrationRunDetailResponse(
        String executionId,
        String status,
        ExecutionOutcome outcome,
        String executionSummary,
        List<String> errors,
        List<ArtifactReferenceResponse> artifacts,
        Instant startedAt,
        Instant completedAt,
        AssistantResponse assistantResponse
    ) {
        this.executionId = executionId;
        this.status = status;
        this.outcome = outcome;
        this.executionSummary = executionSummary;
        this.errors = errors;
        this.artifacts = artifacts;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
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

    public String getExecutionSummary() {
        return executionSummary;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<ArtifactReferenceResponse> getArtifacts() {
        return artifacts;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public AssistantResponse getAssistantResponse() {
        return assistantResponse;
    }
}
