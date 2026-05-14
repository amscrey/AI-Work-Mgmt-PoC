package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.LlmUsage;

/**
 * Estimates token usage when provider metadata is unavailable.
 */
public interface TokenEstimationService {
    LlmUsage estimate(String provider, String model, String prompt, String responseText);
}

