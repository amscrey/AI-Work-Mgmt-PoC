package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.ProjectTokenUsageSummary;

/**
 * Aggregates LLM token usage across stories in the workspace.
 */
public interface ProjectTokenUsageService {
    ProjectTokenUsageSummary getProjectSummary(boolean includeInProgress);
}

