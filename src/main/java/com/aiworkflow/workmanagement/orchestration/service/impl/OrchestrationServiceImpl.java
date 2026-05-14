package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.*;
import com.aiworkflow.workmanagement.orchestration.exception.ArtifactWriteException;
import com.aiworkflow.workmanagement.orchestration.exception.InvalidOrchestrationRequestException;
import com.aiworkflow.workmanagement.orchestration.exception.OrchestrationContextException;
import com.aiworkflow.workmanagement.orchestration.exception.OrchestrationExecutionException;
import com.aiworkflow.workmanagement.orchestration.graph.StateGraphBuilder;
import com.aiworkflow.workmanagement.orchestration.node.NodeAction;
import com.aiworkflow.workmanagement.orchestration.service.ArtifactOutputService;
import com.aiworkflow.workmanagement.orchestration.service.ContextAssemblyService;
import com.aiworkflow.workmanagement.orchestration.service.OrchestrationService;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Implementation of OrchestrationService that coordinates the complete orchestration flow.
 * <p>
 * This is the ONLY component that knows about langgraph4j StateGraph orchestration.
 * It handles conversion between domain objects (OrchestrationContext, OrchestrationResult)
 * and langgraph4j state (OrchestrationState/AgentState).
 * <p>
 * <strong>Execution Flow</strong>:
 * <ol>
 *   <li>Validate request</li>
 *   <li>Log 'orchestration_started' event</li>
 *   <li>Assemble context via ContextAssemblyService</li>
 *   <li>Convert OrchestrationContext → OrchestrationState</li>
 *   <li>Execute StateGraph (research → planning → design → testStrategy)</li>
 *   <li>Extract artifacts from final state</li>
 *   <li>Build OrchestrationResult from state</li>
 *   <li>Write artifacts via ArtifactOutputService (if successful)</li>
 *   <li>Log 'orchestration_completed' event</li>
 *   <li>Return result</li>
 * </ol>
 */
@Service
public class OrchestrationServiceImpl implements OrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestrationServiceImpl.class);

    private final ContextAssemblyService contextAssemblyService;
    private final ArtifactOutputService artifactOutputService;
    private final StateGraphBuilder stateGraphBuilder;
    private final List<NodeAction> templateNodes;
    private final ActivityLogger activityLogger;
    private final CompiledGraph<AgentState> compiledGraph;

    public OrchestrationServiceImpl(
        ContextAssemblyService contextAssemblyService,
        ArtifactOutputService artifactOutputService,
        StateGraphBuilder stateGraphBuilder,
        List<NodeAction> templateNodes,
        ActivityLogger activityLogger
    ) {
        this.contextAssemblyService = contextAssemblyService;
        this.artifactOutputService = artifactOutputService;
        this.stateGraphBuilder = stateGraphBuilder;
        this.templateNodes = templateNodes;
        this.activityLogger = activityLogger;

        // Build and compile graph once at initialization
        this.compiledGraph = stateGraphBuilder.buildGraph(templateNodes);
        logger.info("OrchestrationService initialized with compiled StateGraph");
    }

    @Override
    public OrchestrationResult orchestrate(OrchestrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("OrchestrationRequest cannot be null");
        }

        // Generate unique execution ID
        String executionId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();

        // Determine storyId for logging (handle null for PROJECT-PLANNING)
        String storyIdForLogging = request.isProjectLevel() ? "PROJECT-PLANNING" : request.getStoryId().getValue();

        try {
            // Validate request
            validateRequest(request);

            // Log orchestration started
            logOrchestrationStarted(storyIdForLogging, request.getWorkIntent(), executionId);

            // Step 1: Assemble context (domain object)
            OrchestrationContext context = assembleContext(request);

            // Step 2: Convert OrchestrationContext → OrchestrationState
            OrchestrationState state = convertContextToState(context, request, executionId);

            // Step 3: Execute StateGraph (manually for now, real StateGraph in STORY-008B)
            OrchestrationState finalState = executeNodes(state);

            // Step 4: Build OrchestrationResult from final state
            OrchestrationResult result = buildResultFromState(finalState, executionId, startedAt);

            // Step 5: Write artifacts to filesystem (if successful)
            if (result.isSuccess() && result.hasArtifacts()) {
                writeArtifacts(request, result);
            }

            // Log orchestration completed
            logOrchestrationCompleted(storyIdForLogging, result, executionId);

            return result;

        } catch (InvalidOrchestrationRequestException e) {
            logger.error("Invalid orchestration request for execution {}: {}", executionId, e.getMessage());
            logOrchestrationFailed(storyIdForLogging, e.getMessage(), executionId);
            throw e;

        } catch (OrchestrationContextException e) {
            logger.error("Context assembly failed for execution {}: {}", executionId, e.getMessage());
            logOrchestrationFailed(storyIdForLogging, e.getMessage(), executionId);
            throw e;

        } catch (OrchestrationExecutionException e) {
            logger.error("Orchestration execution failed for execution {}: {}", executionId, e.getMessage());
            logOrchestrationFailed(storyIdForLogging, e.getMessage(), executionId);
            throw e;

        } catch (ArtifactWriteException e) {
            logger.error("Artifact writing failed for execution {}: {}", executionId, e.getMessage());
            logOrchestrationFailed(storyIdForLogging, e.getMessage(), executionId);
            throw e;

        } catch (Exception e) {
            logger.error("Unexpected error during orchestration execution {}: {}", executionId, e.getMessage(), e);
            String errorMessage = "Unexpected error: " + e.getMessage();
            logOrchestrationFailed(storyIdForLogging, errorMessage, executionId);
            throw new OrchestrationExecutionException(errorMessage, e);
        }
    }

    /**
     * Validates the orchestration request.
     *
     * @param request the request to validate
     * @throws InvalidOrchestrationRequestException if validation fails
     */
    private void validateRequest(OrchestrationRequest request) {
        if (request.getWorkIntent() == null || request.getWorkIntent().isBlank()) {
            throw new InvalidOrchestrationRequestException("Work intent cannot be null or blank");
        }
        if (request.getRequestedBy() == null || request.getRequestedBy().isBlank()) {
            throw new InvalidOrchestrationRequestException("RequestedBy cannot be null or blank");
        }
    }

    /**
     * Assembles orchestration context from request.
     *
     * @param request the orchestration request
     * @return the assembled context
     * @throws OrchestrationContextException if context assembly fails
     */
    private OrchestrationContext assembleContext(OrchestrationRequest request) {
        try {
            return contextAssemblyService.assembleContext(request);
        } catch (Exception e) {
            throw new OrchestrationContextException(
                "Failed to assemble context: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Converts OrchestrationContext (domain) to OrchestrationState (langgraph4j).
     * <p>
     * This is the ONLY place where domain objects are converted to langgraph4j state.
     *
     * @param context the domain context
     * @param request the original request
     * @param executionId unique execution ID
     * @return initialized OrchestrationState
     */
    private OrchestrationState convertContextToState(
        OrchestrationContext context,
        OrchestrationRequest request,
        String executionId
    ) {
        // Build state map with all initial values
        Map<String, Object> stateData = new HashMap<>();
        stateData.put(OrchestrationState.EXECUTION_ID, executionId);
        stateData.put(OrchestrationState.WORK_INTENT, request.getWorkIntent());
        stateData.put(OrchestrationState.STORY, context.getStory());
        // Note: Additional context (tasks, reference files) could be added here
        // For now, nodes can access them via story.getTasks() etc.

        return new OrchestrationState(stateData);
    }

    /**
     * Executes orchestration nodes using StateGraph.
     * <p>
     * This method:
     * <ol>
     *   <li>Prepares initial state data as Map</li>
     *   <li>Invokes the compiled StateGraph</li>
     *   <li>Returns final OrchestrationState</li>
     * </ol>
     * <p>
     * The StateGraph executes nodes in sequence: research → planning → design → testStrategy.
     * Artifacts are accumulated across nodes using the Appender reducer.
     *
     * @param state the initial orchestration state
     * @return the final state with accumulated artifacts
     * @throws OrchestrationExecutionException if node execution fails
     */
    private OrchestrationState executeNodes(OrchestrationState state) {
        try {
            logger.info("Executing StateGraph for execution ID: {}", state.getExecutionId());

            // Convert OrchestrationState → AgentState for StateGraph execution
            AgentState initialAgentState = state.toAgentState();

            // Execute StateGraph (research → planning → design → testStrategy)
            // invoke() returns Optional<AgentState>
            AgentState finalAgentState = compiledGraph.invoke(initialAgentState.data())
                .orElseThrow(() -> new OrchestrationExecutionException(
                    "StateGraph execution returned empty result"));

            // Convert AgentState back to OrchestrationState
            OrchestrationState finalState = OrchestrationState.fromAgentState(finalAgentState);

            logger.info("StateGraph execution completed. Artifacts created: {}",
                finalState.getArtifactsCreated().size());

            return finalState;

        } catch (Exception e) {
            logger.error("StateGraph execution failed: {}", e.getMessage(), e);
            throw new OrchestrationExecutionException(
                "Failed to execute StateGraph: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Builds OrchestrationResult (domain object) from final OrchestrationState.
     * <p>
     * This conversion extracts artifacts from langgraph4j state and packages them
     * into a domain object that ArtifactOutputService can use.
     *
     * @param finalState the final state after all nodes executed
     * @param executionId unique execution ID
     * @param startedAt execution start time
     * @return the orchestration result
     */
    private OrchestrationResult buildResultFromState(
        OrchestrationState finalState,
        String executionId,
        Instant startedAt
    ) {
        List<ArtifactReference> artifacts = finalState.getArtifactsCreated();
        Instant completedAt = Instant.now();

        if (artifacts == null || artifacts.isEmpty()) {
            return OrchestrationResult.builder()
                .executionId(executionId)
                .outcome(ExecutionOutcome.SUCCESS)
                .executionSummary("Orchestration completed with no artifacts")
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
        }

        String summary = String.format(
            "Orchestration completed successfully. Created %d artifact(s).",
            artifacts.size()
        );

        return OrchestrationResult.builder()
            .executionId(executionId)
            .outcome(ExecutionOutcome.SUCCESS)
            .artifacts(artifacts)
            .executionSummary(summary)
            .startedAt(startedAt)
            .completedAt(completedAt)
            .build();
    }

    /**
     * Writes artifacts to filesystem via ArtifactOutputService.
     *
     * @param request the original request
     * @param result the orchestration result containing artifacts
     * @throws ArtifactWriteException if artifact writing fails
     */
    private void writeArtifacts(OrchestrationRequest request, OrchestrationResult result) {
        try {
            artifactOutputService.writeArtifacts(request, result);
        } catch (Exception e) {
            throw new ArtifactWriteException(
                "Failed to write artifacts: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Logs orchestration started event.
     */
    private void logOrchestrationStarted(String storyId, String workIntent, String executionId) {
        activityLogger.logSuccess(
            "orchestrator",
            "orchestration_started",
            storyId,
            Map.of(
                "executionId", executionId,
                "workIntent", workIntent
            )
        );
    }

    /**
     * Logs orchestration completed event.
     */
    private void logOrchestrationCompleted(
        String storyId,
        OrchestrationResult result,
        String executionId
    ) {
        activityLogger.logSuccess(
            "orchestrator",
            "orchestration_completed",
            storyId,
            Map.of(
                "executionId", executionId,
                "outcome", result.getOutcome().name(),
                "artifactsCreated", result.getArtifacts().size(),
                "durationMs", result.getDuration().toMillis()
            )
        );
    }

    /**
     * Logs orchestration failed event.
     */
    private void logOrchestrationFailed(String storyId, String errorMessage, String executionId) {
        activityLogger.logFailure(
            "orchestrator",
            "orchestration_failed",
            storyId,
            errorMessage,
            Map.of("executionId", executionId)
        );
    }
}
