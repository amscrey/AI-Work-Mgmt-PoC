package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrchestrationRunRegistry {
    private final Map<String, OrchestrationRunRecord> byExecutionId = new ConcurrentHashMap<>();
    private final Map<String, String> latestByStoryId = new ConcurrentHashMap<>();

    public OrchestrationRunRecord record(String storyId, OrchestrationResult result) {
        OrchestrationRunRecord record = new OrchestrationRunRecord(storyId, result);
        byExecutionId.put(record.getExecutionId(), record);
        if (storyId != null) {
            latestByStoryId.put(storyId, record.getExecutionId());
        }
        return record;
    }

    public Optional<OrchestrationRunRecord> findByExecutionId(String executionId) {
        return Optional.ofNullable(byExecutionId.get(executionId));
    }

    public Optional<OrchestrationRunRecord> findLatestByStory(String storyId) {
        String executionId = latestByStoryId.get(storyId);
        if (executionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byExecutionId.get(executionId));
    }
}

