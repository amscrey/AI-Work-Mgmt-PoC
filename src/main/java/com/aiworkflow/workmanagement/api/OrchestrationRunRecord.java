package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.orchestration.domain.ExecutionOutcome;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationResult;

public class OrchestrationRunRecord {
    private final String storyId;
    private final OrchestrationResult result;

    public OrchestrationRunRecord(String storyId, OrchestrationResult result) {
        this.storyId = storyId;
        this.result = result;
    }

    public String getExecutionId() {
        return result.getExecutionId();
    }

    public String getStoryId() {
        return storyId;
    }

    public OrchestrationResult getResult() {
        return result;
    }

    public String getStatus() {
        ExecutionOutcome outcome = result.getOutcome();
        return outcome != null ? outcome.name() : "UNKNOWN";
    }
}

