package com.aiworkflow.workmanagement.orchestration.graph;

import com.aiworkflow.workmanagement.application.command.CreateCommentCommand;
import com.aiworkflow.workmanagement.application.service.CommentService;
import com.aiworkflow.workmanagement.domain.model.CommentType;
import com.aiworkflow.workmanagement.domain.model.Story;
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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    // Node name to role name mapping
    private static final Map<String, String> NODE_ROLES = Map.of(
        NODE_RESEARCH, "researcher",
        NODE_PLANNING, "logician",
        NODE_DESIGN, "designer",
        NODE_TEST_STRATEGY, "tester"
    );

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    /**
     * Schema definition for StateGraph.
     * Uses Appender channel for artifacts to accumulate them across nodes.
     */
    private static final Map<String, Channel<?>> SCHEMA = Map.of(
        OrchestrationState.ARTIFACTS_CREATED, Channels.<ArtifactReference>appender(ArrayList::new)
    );

    private final CommentService commentService;

    /**
     * Constructs a StateGraphBuilder with optional comment service for agent progress tracking.
     *
     * @param commentService service for creating comments (nullable for backward compatibility)
     */
    public StateGraphBuilder(CommentService commentService) {
        this.commentService = commentService;
    }

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
     *   <li>Creates a "starting work" comment</li>
     *   <li>Converts AgentState → OrchestrationState (via fromAgentState)</li>
     *   <li>Executes NodeAction with mutable OrchestrationState</li>
     *   <li>Creates a "completed work" comment with artifact details</li>
     *   <li>Returns Map of state updates (for StateGraph to merge)</li>
     * </ol>
     *
     * @param nodeAction the NodeAction to wrap
     * @param nodeName the node name (for logging and comments)
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

            // Get story ID and role name for comments
            Story story = state.getStory();
            String storyId = story != null ? story.getId().getValue() : null;
            String roleName = NODE_ROLES.getOrDefault(nodeName, nodeName);
            String executionId = state.getExecutionId();

            // Create "starting work" comment
            createNodeStartComment(storyId, roleName, executionId);

            // Capture current artifacts to compute delta for Appender reducer
            List<ArtifactReference> beforeArtifacts = new ArrayList<>(state.getArtifactsCreated());

            // Execute node (mutates state and returns it)
            OrchestrationState updatedState = nodeAction.execute(state);

            List<ArtifactReference> afterArtifacts = updatedState.getArtifactsCreated();
            List<ArtifactReference> newArtifacts = afterArtifacts.size() > beforeArtifacts.size()
                ? new ArrayList<>(afterArtifacts.subList(beforeArtifacts.size(), afterArtifacts.size()))
                : new ArrayList<>();

            // Create "completed work" comment
            createNodeCompleteComment(storyId, roleName, executionId, newArtifacts);

            // Convert back to AgentState updates; only send delta artifacts for Appender reducer
            logger.debug("Node {} completed. New artifacts: {}", nodeName, newArtifacts.size());

            Map<String, Object> updates = new HashMap<>(updatedState.data());
            updates.put(OrchestrationState.ARTIFACTS_CREATED, newArtifacts);

            return updates;
        };
    }

    /**
     * Creates a comment indicating an agent is starting work.
     */
    private void createNodeStartComment(String storyId, String roleName, String executionId) {
        if (commentService == null || storyId == null) {
            logger.debug("Skipping start comment: commentService={}, storyId={}",
                commentService != null, storyId);
            return;
        }

        try {
            String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
            String commentId = String.format("comment__%s__start__%s.md", roleName, timestamp);
            String content = String.format("**%s** is starting analysis...", capitalize(roleName));

            CreateCommentCommand command = new CreateCommentCommand(
                commentId,
                storyId,
                roleName,
                CommentType.NOTE,
                content,
                executionId,
                false
            );

            commentService.createComment(command);
            logger.debug("Created start comment for {} on story {}", roleName, storyId);
        } catch (Exception e) {
            logger.warn("Failed to create start comment for {}: {}", roleName, e.getMessage());
        }
    }

    /**
     * Creates a comment indicating an agent has completed work.
     */
    private void createNodeCompleteComment(String storyId, String roleName, String executionId,
                                           List<ArtifactReference> artifacts) {
        if (commentService == null || storyId == null) {
            logger.debug("Skipping complete comment: commentService={}, storyId={}",
                commentService != null, storyId);
            return;
        }

        try {
            String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
            String commentId = String.format("comment__%s__complete__%s.md", roleName, timestamp);

            StringBuilder content = new StringBuilder();
            content.append(String.format("**%s** completed work.", capitalize(roleName)));

            if (!artifacts.isEmpty()) {
                content.append("\n\nArtifacts created:");
                for (ArtifactReference artifact : artifacts) {
                    content.append(String.format("\n- `%s`", artifact.getName()));
                }
            }

            CreateCommentCommand command = new CreateCommentCommand(
                commentId,
                storyId,
                roleName,
                CommentType.NOTE,
                content.toString(),
                executionId,
                false
            );

            commentService.createComment(command);
            logger.debug("Created complete comment for {} on story {} with {} artifacts",
                roleName, storyId, artifacts.size());
        } catch (Exception e) {
            logger.warn("Failed to create complete comment for {}: {}", roleName, e.getMessage());
        }
    }

    /**
     * Capitalizes the first letter of a string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
