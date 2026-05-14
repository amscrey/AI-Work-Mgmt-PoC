package com.aiworkflow.workmanagement.orchestration.graph;

import com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import com.aiworkflow.workmanagement.orchestration.node.NodeAction;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Builder for constructing langgraph4j StateGraph from NodeAction implementations.
 * <p>
 * This class creates a StateGraph workflow with nodes connected in a linear sequence:
 * research → planning → design → testStrategy
 * <p>
 * The StateGraph uses the Appender reducer to accumulate artifacts across nodes.
 * Each node receives the state from the previous node, executes its logic,
 * and returns updated state with new artifacts appended.
 *
 * <h2>Graph Structure</h2>
 * <pre>
 * START → research → planning → design → testStrategy → END
 * </pre>
 *
 * <h2>State Conversion</h2>
 * This builder handles conversion between OrchestrationState (domain) and AgentState (langgraph4j):
 * <ul>
 *   <li>AgentState created by factory function from Map</li>
 *   <li>NodeActions receive OrchestrationState (via fromAgentState conversion)</li>
 *   <li>Node results converted back to Map via toAgentState</li>
 * </ul>
 *
 * <h2>Node Wrapping</h2>
 * NodeAction implementations work with OrchestrationState (convenient API).
 * This builder wraps NodeActions using node_async() to make them compatible with StateGraph.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Build graph
 * CompiledGraph<AgentState> graph = stateGraphBuilder.buildGraph(templateNodes);
 *
 * // Execute graph
 * OrchestrationState initialState = new OrchestrationState();
 * initialState.setWorkIntent("Research game mechanics");
 * AgentState result = graph.invoke(initialState.toAgentState().data()).orElseThrow();
 * OrchestrationState finalState = OrchestrationState.fromAgentState(result);
 * }</pre>
 *
 * @see NodeAction
 * @see OrchestrationState
 * @see StateGraph
 */
@Component
public class StateGraphBuilder {

    private static final Logger logger = LoggerFactory.getLogger(StateGraphBuilder.class);

    // Node names (must match expected order)
    private static final String NODE_RESEARCH = "research";
    private static final String NODE_PLANNING = "planning";
    private static final String NODE_DESIGN = "design";
    private static final String NODE_TEST_STRATEGY = "testStrategy";

    /**
     * Schema definition for StateGraph.
     * Uses Appender channel for artifacts to accumulate them across nodes.
     */
    private static final Map<String, Channel<?>> SCHEMA = Map.of(
        OrchestrationState.ARTIFACTS_CREATED, Channels.<ArtifactReference>appender(ArrayList::new)
    );

    /**
     * Builds a compiled StateGraph from the provided NodeAction implementations.
     * <p>
     * This method:
     * <ol>
     *   <li>Creates a StateGraph with AgentState schema and factory</li>
     *   <li>Adds nodes by wrapping NodeActions with node_async()</li>
     *   <li>Defines edges (START → research → planning → design → testStrategy → END)</li>
     *   <li>Compiles and returns the graph</li>
     * </ol>
     *
     * @param nodeActions list of NodeAction implementations (must contain 4 nodes in order)
     * @return compiled graph ready for invocation
     * @throws IllegalArgumentException if nodeActions is null or doesn't contain exactly 4 nodes
     * @throws RuntimeException if graph construction fails
     */
    public CompiledGraph<AgentState> buildGraph(List<NodeAction> nodeActions) {
        if (nodeActions == null || nodeActions.size() != 4) {
            throw new IllegalArgumentException(
                "Expected exactly 4 node actions (research, planning, design, testStrategy), got: " +
                (nodeActions != null ? nodeActions.size() : "null")
            );
        }

        logger.info("Building StateGraph with {} nodes", nodeActions.size());

        try {
            // Create StateGraph with schema and factory function
            StateGraph<AgentState> graph = new StateGraph<>(SCHEMA, AgentState::new);

            // Add nodes in order: research, planning, design, testStrategy
            // Wrap each NodeAction with node_async() for compatibility
            graph.addNode(NODE_RESEARCH, node_async(wrapNodeAction(nodeActions.get(0), NODE_RESEARCH)));
            graph.addNode(NODE_PLANNING, node_async(wrapNodeAction(nodeActions.get(1), NODE_PLANNING)));
            graph.addNode(NODE_DESIGN, node_async(wrapNodeAction(nodeActions.get(2), NODE_DESIGN)));
            graph.addNode(NODE_TEST_STRATEGY, node_async(wrapNodeAction(nodeActions.get(3), NODE_TEST_STRATEGY)));

            // Define edges (linear flow)
            graph.addEdge(START, NODE_RESEARCH);
            graph.addEdge(NODE_RESEARCH, NODE_PLANNING);
            graph.addEdge(NODE_PLANNING, NODE_DESIGN);
            graph.addEdge(NODE_DESIGN, NODE_TEST_STRATEGY);
            graph.addEdge(NODE_TEST_STRATEGY, END);

            // Compile graph
            CompiledGraph<AgentState> compiled = graph.compile();
            logger.info("StateGraph compiled successfully");

            return compiled;

        } catch (GraphStateException e) {
            logger.error("Failed to build StateGraph: {}", e.getMessage(), e);
            throw new RuntimeException("StateGraph construction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Wraps a NodeAction (works with OrchestrationState) as a langgraph4j node function.
     * <p>
     * This wrapper:
     * <ol>
     *   <li>Converts AgentState → OrchestrationState (via fromAgentState)</li>
     *   <li>Executes NodeAction with mutable OrchestrationState</li>
     *   <li>Returns Map of state updates (for StateGraph to merge)</li>
     * </ol>
     *
     * @param nodeAction the NodeAction to wrap
     * @param nodeName the node name (for logging)
     * @return wrapped function compatible with langgraph4j
     */
    private org.bsc.langgraph4j.action.NodeAction<AgentState> wrapNodeAction(
        NodeAction nodeAction,
        String nodeName
    ) {
        return agentState -> {
            logger.debug("Executing node: {}", nodeName);

            // Convert AgentState → OrchestrationState (mutable domain object)
            OrchestrationState state = OrchestrationState.fromAgentState(agentState);

            // Capture current artifacts to compute delta for Appender reducer
            List<ArtifactReference> beforeArtifacts = new ArrayList<>(state.getArtifactsCreated());

            // Execute node (mutates state and returns it)
            OrchestrationState updatedState = nodeAction.execute(state);

            List<ArtifactReference> afterArtifacts = updatedState.getArtifactsCreated();
            List<ArtifactReference> newArtifacts = afterArtifacts.size() > beforeArtifacts.size()
                ? new ArrayList<>(afterArtifacts.subList(beforeArtifacts.size(), afterArtifacts.size()))
                : new ArrayList<>();

            // Convert back to AgentState updates; only send delta artifacts for Appender reducer
            logger.debug("Node {} completed. New artifacts: {}", nodeName, newArtifacts.size());

            Map<String, Object> updates = new HashMap<>(updatedState.data());
            updates.put(OrchestrationState.ARTIFACTS_CREATED, newArtifacts);

            return updates;
        };
    }
}
