package com.aiworkflow.workmanagement.orchestration.graph;

import com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import com.aiworkflow.workmanagement.orchestration.node.NodeAction;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class StateGraphBuilderTest {

    @Test
    void shouldAccumulateArtifactsAcrossNodesWithAppenderReducer() {
        StateGraphBuilder builder = new StateGraphBuilder();

        List<NodeAction> nodes = Arrays.asList(
            state -> addArtifact(state, "artifact-1", "researcher"),
            state -> addArtifact(state, "artifact-2", "planner"),
            state -> addArtifact(state, "artifact-3", "designer"),
            state -> addArtifact(state, "artifact-4", "tester")
        );

        CompiledGraph<AgentState> graph = builder.buildGraph(nodes);

        OrchestrationState initialState = new OrchestrationState();
        AgentState finalAgentState = graph.invoke(initialState.toAgentState().data())
            .orElseThrow(() -> new IllegalStateException("Expected final AgentState"));
        OrchestrationState finalState = OrchestrationState.fromAgentState(finalAgentState);

        assertThat(finalState.getArtifactsCreated()).hasSize(4);
        assertThat(finalState.getArtifactsCreated().stream()
            .map(ArtifactReference::getName)
            .collect(Collectors.toList()))
            .containsExactly("artifact-1", "artifact-2", "artifact-3", "artifact-4");
    }

    private OrchestrationState addArtifact(OrchestrationState state, String name, String role) {
        state.addArtifact(new ArtifactReference(name, "content", role));
        state.setCurrentRole(role);
        return state;
    }
}

