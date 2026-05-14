package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;

/**
 * Loads and validates LLM configuration into an internal snapshot.
 */
public interface LlmConfigLoader {
    LlmConfigSnapshot load();
}

