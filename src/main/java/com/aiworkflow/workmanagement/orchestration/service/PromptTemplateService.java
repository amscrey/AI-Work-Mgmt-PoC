package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationContext;

/**
 * Builds prompts for LLM node actions based on the orchestration context.
 *
 * This service is intentionally decoupled from langgraph4j and LLM clients.
 */
public interface PromptTemplateService {

    String buildResearchPrompt(OrchestrationContext context);

    String buildTaskPlanningPrompt(OrchestrationContext context);

    String buildDesignPrompt(OrchestrationContext context);

    String buildTestStrategyPrompt(OrchestrationContext context);
}

