package com.aiworkflow.workmanagement.orchestration.node;

import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;

/**
 * Interface for langgraph4j node actions in the orchestration workflow.
 * <p>
 * This interface is implemented by both template-based nodes (deterministic, no LLM)
 * and LLM-based nodes (real Claude API calls). Both implementations are completely
 * interchangeable and use the same StateGraph execution model.
 *
 * <h2>Node Execution Model</h2>
 * Each node:
 * <ul>
 *   <li>Receives current {@link OrchestrationState}</li>
 *   <li>Performs work (template rendering or LLM call)</li>
 *   <li>Returns updated state with new artifacts appended</li>
 * </ul>
 *
 * <h2>State Modification</h2>
 * Nodes MUST return a new state object with updates. The langgraph4j framework
 * uses the Appender reducer to accumulate artifacts across nodes.
 *
 * <h2>Implementation Types</h2>
 * <ul>
 *   <li><strong>Template Nodes</strong>: Use predefined templates, fast, deterministic</li>
 *   <li><strong>LLM Nodes</strong>: Call Claude API via LangChain4j, slower, adaptive</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // In StateGraph construction
 * StateGraph<OrchestrationState> graph = new StateGraph<>(OrchestrationState.class)
 *     .addNode("research", researchNodeAction)
 *     .addNode("planning", planningNodeAction)
 *     .addEdge("research", "planning")
 *     .setEntryPoint("research");
 *
 * // Node execution (handled by langgraph4j)
 * OrchestrationState result = graph.compile().invoke(initialState);
 * }</pre>
 *
 * @see OrchestrationState
 */
@FunctionalInterface
public interface NodeAction {

    /**
     * Executes the node action on the given orchestration state.
     * <p>
     * This method is called by the langgraph4j framework during graph execution.
     * Implementations should:
     * <ol>
     *   <li>Read input from state (story, work intent, etc.)</li>
     *   <li>Perform work (template rendering or LLM call)</li>
     *   <li>Create artifact(s) from the output</li>
     *   <li>Return new state with artifacts appended</li>
     * </ol>
     *
     * <h3>State Updates</h3>
     * The returned state should contain:
     * <ul>
     *   <li>All previous state values (immutable copy)</li>
     *   <li>New artifact(s) appended to ARTIFACTS_CREATED list</li>
     *   <li>Updated CURRENT_STEP (node name)</li>
     *   <li>Optional: Updated metadata (execution time, token count, etc.)</li>
     * </ul>
     *
     * <h3>Error Handling</h3>
     * Nodes should handle errors gracefully:
     * <ul>
     *   <li>Catch exceptions and add error details to state</li>
     *   <li>Set ERROR_MESSAGE key in state if execution fails</li>
     *   <li>Optionally: Add partial artifacts if some work succeeded</li>
     * </ul>
     *
     * @param state the current orchestration state
     * @return updated state with new artifacts and metadata
     * @throws RuntimeException if node execution fails unrecoverably
     */
    OrchestrationState execute(OrchestrationState state);
}
