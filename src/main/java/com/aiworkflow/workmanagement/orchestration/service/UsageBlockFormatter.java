package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.UsageBlock;

/**
 * Builds standardized usage blocks for comments/logs.
 */
public interface UsageBlockFormatter {
    UsageBlock format(StoryId storyId);
}

