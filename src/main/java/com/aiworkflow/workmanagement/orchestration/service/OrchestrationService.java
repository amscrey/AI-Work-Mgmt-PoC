package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationRequest;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationResult;
import com.aiworkflow.workmanagement.orchestration.exception.InvalidOrchestrationRequestException;
import com.aiworkflow.workmanagement.orchestration.exception.OrchestrationContextException;
import com.aiworkflow.workmanagement.orchestration.exception.OrchestrationExecutionException;
import com.aiworkflow.workmanagement.orchestration.exception.ArtifactWriteException;

/**
 * Facade service that orchestrates the complete LLM-assisted work execution flow.
 * <p>
 * This is the main entry point for orchestration. It coordinates:
 * <ul>
 *   <li><strong>Context Assembly</strong>: Gathers story, tasks, and reference files</li>
 *   <li><strong>StateGraph Execution</strong>: Executes langgraph4j workflow with nodes</li>
 *   <li><strong>Artifact Output</strong>: Persists generated artifacts to filesystem</li>
 *   <li><strong>Activity Logging</strong>: Records orchestration events</li>
 * </ul>
 *
 * <h2>Orchestration Flow</h2>
 * <pre>
 * User/System
 *     ↓
 * 1. OrchestrationService.orchestrate(request)
 *     ↓
 * 2. ContextAssemblyService.assembleContext(request)
 *     ↓ (returns OrchestrationContext - domain object)
 * 3. Convert OrchestrationContext → OrchestrationState (for langgraph4j)
 *     ↓
 * 4. Execute CompiledGraph<OrchestrationState> (StateGraph with nodes)
 *     ↓ (nodes return artifacts accumulated via Appender)
 * 5. Extract artifacts from final OrchestrationState
 *     ↓
 * 6. Build OrchestrationResult (domain object) from state
 *     ↓
 * 7. ArtifactOutputService.writeArtifacts(request, result)
 *     ↓
 * 8. Return OrchestrationResult to caller
 * </pre>
 *
 * <h2>Execution Modes</h2>
 * The service supports two execution modes (configured via orchestration.execution-mode):
 * <ul>
 *   <li><strong>template</strong>: Template-based nodes (deterministic, no LLM calls)</li>
 *   <li><strong>llm</strong>: LLM nodes (real Claude API calls via LangChain4j)</li>
 * </ul>
 * Both modes use the same NodeAction interface and are interchangeable.
 *
 * <h2>State Conversion (Decoupling Strategy)</h2>
 * This service handles conversion between domain objects and langgraph4j state:
 * <ul>
 *   <li>OrchestrationContext (domain) → OrchestrationState (langgraph4j)</li>
 *   <li>OrchestrationState (langgraph4j) → OrchestrationResult (domain)</li>
 * </ul>
 * This keeps ContextAssemblyService and ArtifactOutputService decoupled from langgraph4j.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create orchestration request
 * OrchestrationRequest request = OrchestrationRequest.forStory(
 *     new StoryId("STORY-123"),
 *     "Research game mechanics for inventory system",
 *     "human"
 * );
 *
 * // Execute orchestration
 * OrchestrationResult result = orchestrationService.orchestrate(request);
 *
 * // Check result
 * if (result.isSuccess()) {
 *     System.out.println("Created " + result.getArtifacts().size() + " artifacts");
 *     System.out.println("Summary: " + result.getExecutionSummary());
 * } else {
 *     System.err.println("Orchestration failed: " + result.getErrors());
 * }
 * }</pre>
 *
 * <h2>Error Handling</h2>
 * The service gracefully handles errors at each phase:
 * <ul>
 *   <li><strong>Validation</strong>: InvalidOrchestrationRequestException</li>
 *   <li><strong>Context Assembly</strong>: OrchestrationContextException</li>
 *   <li><strong>Graph Execution</strong>: OrchestrationExecutionException</li>
 *   <li><strong>Artifact Writing</strong>: ArtifactWriteException</li>
 * </ul>
 * Partial results (PARTIAL outcome) are returned when some artifacts succeed but others fail.
 *
 * @see OrchestrationRequest
 * @see OrchestrationResult
 * @see ContextAssemblyService
 * @see ArtifactOutputService
 */
public interface OrchestrationService {

    /**
     * Orchestrates LLM-assisted work execution for a story or project-level request.
     * <p>
     * This method executes the complete orchestration flow:
     * <ol>
     *   <li>Validates request</li>
     *   <li>Logs 'orchestration_started' event</li>
     *   <li>Assembles execution context via ContextAssemblyService</li>
     *   <li>Converts context to OrchestrationState</li>
     *   <li>Executes StateGraph (template or LLM nodes)</li>
     *   <li>Extracts artifacts from final state (via Appender)</li>
     *   <li>Builds OrchestrationResult from state</li>
     *   <li>Writes artifacts via ArtifactOutputService (if successful)</li>
     *   <li>Logs 'orchestration_completed' event</li>
     *   <li>Returns result</li>
     * </ol>
     *
     * @param request the orchestration request containing storyId and workIntent
     * @return the orchestration result containing outcome, artifacts, and summary
     * @throws InvalidOrchestrationRequestException if request validation fails
     * @throws OrchestrationContextException if context assembly fails
     * @throws OrchestrationExecutionException if StateGraph execution fails unrecoverably
     * @throws ArtifactWriteException if artifact persistence fails
     * @throws IllegalArgumentException if request is null
     */
    OrchestrationResult orchestrate(OrchestrationRequest request);
}
