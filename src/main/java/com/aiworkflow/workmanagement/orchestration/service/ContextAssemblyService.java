package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationContext;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationRequest;
import com.aiworkflow.workmanagement.orchestration.exception.OrchestrationContextException;

/**
 * Service responsible for assembling execution context for orchestration.
 * <p>
 * This service bridges the Work Management domain model with the orchestration layer by
 * gathering all necessary information for LLM execution:
 * - Story metadata (title, description, acceptance criteria)
 * - Associated tasks
 * - Reference files from workspace/reference/ directory
 * - Work intent from the request
 * <p>
 * The service returns an {@link OrchestrationContext} (domain object), NOT OrchestrationState.
 * OrchestrationService handles conversion to langgraph4j state for graph execution.
 *
 * <h2>PROJECT-PLANNING Support</h2>
 * When the request has a null storyId, this service:
 * <ul>
 *   <li>Auto-creates the PROJECT-PLANNING work item if it doesn't exist</li>
 *   <li>Creates directory structure: workspace/stories/PROJECT-PLANNING/{artifacts,tasks,comments}</li>
 *   <li>Returns context for project-level orchestration</li>
 * </ul>
 *
 * <h2>Context Size Limiting</h2>
 * To avoid exceeding LLM context windows, this service applies limits:
 * <ul>
 *   <li>Max reference files (default: 5)</li>
 *   <li>Max artifacts (default: 10, most recent first)</li>
 *   <li>Max total context tokens (default: 50,000)</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // For a specific story
 * OrchestrationRequest request = OrchestrationRequest.forStory(
 *     new StoryId("STORY-123"),
 *     "Research game mechanics for inventory system",
 *     "human"
 * );
 * OrchestrationContext context = contextAssemblyService.assembleContext(request);
 *
 * // For project-level work
 * OrchestrationRequest projectRequest = OrchestrationRequest.forProject(
 *     "Create high-level project plan",
 *     "orchestrator"
 * );
 * OrchestrationContext projectContext = contextAssemblyService.assembleContext(projectRequest);
 * }</pre>
 *
 * @see OrchestrationContext
 * @see OrchestrationRequest
 */
public interface ContextAssemblyService {

    /**
     * Assembles execution context from an orchestration request.
     * <p>
     * This method:
     * <ol>
     *   <li>Loads the story (or creates PROJECT-PLANNING if storyId is null)</li>
     *   <li>Loads associated tasks</li>
     *   <li>Scans workspace/reference/ for ReferenceFile markdown files</li>
     *   <li>Applies context size limits</li>
     *   <li>Returns OrchestrationContext (domain object)</li>
     * </ol>
     *
     * @param request the orchestration request containing storyId and workIntent
     * @return the assembled orchestration context
     * @throws OrchestrationContextException if context assembly fails (story not found,
     *         reference files cannot be read, PROJECT-PLANNING creation fails, etc.)
     * @throws IllegalArgumentException if request is null
     */
    OrchestrationContext assembleContext(OrchestrationRequest request);
}
