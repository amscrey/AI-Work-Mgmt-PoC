package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsageRecord;
import com.aiworkflow.workmanagement.orchestration.domain.StoryTokenUsageSummary;

import java.util.List;

/**
 * Tracks LLM token usage for stories.
 */
public interface StoryTokenUsageService {
    void recordUsage(LlmUsageRecord record);

    List<LlmUsageRecord> getRecords(StoryId storyId);

    StoryTokenUsageSummary getSummary(StoryId storyId);
}

