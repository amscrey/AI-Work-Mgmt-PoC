package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.LlmSummary;

/**
 * Provides a runtime summary of LLM configuration.
 */
public interface LlmSummaryService {
    LlmSummary getSummary();
}

