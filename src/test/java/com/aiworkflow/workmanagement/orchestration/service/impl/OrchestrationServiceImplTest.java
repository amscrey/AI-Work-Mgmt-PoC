package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.*;
import com.aiworkflow.workmanagement.orchestration.exception.InvalidOrchestrationRequestException;
import com.aiworkflow.workmanagement.orchestration.exception.OrchestrationContextException;
import com.aiworkflow.workmanagement.orchestration.exception.OrchestrationExecutionException;
import com.aiworkflow.workmanagement.orchestration.graph.StateGraphBuilder;
import com.aiworkflow.workmanagement.orchestration.node.NodeAction;
import com.aiworkflow.workmanagement.orchestration.service.ArtifactOutputService;
import com.aiworkflow.workmanagement.orchestration.service.ContextAssemblyService;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrchestrationServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class OrchestrationServiceImplTest {

    @Mock
    private ContextAssemblyService contextAssemblyService;

    @Mock
    private ArtifactOutputService artifactOutputService;

    @Mock
    private StateGraphBuilder stateGraphBuilder;

    @Mock
    private ActivityLogger activityLogger;

    @Mock
    private NodeAction mockNode1;

    @Mock
    private NodeAction mockNode2;

    @Mock
    private NodeAction mockNode3;

    @Mock
    private NodeAction mockNode4;

    @Mock
    private CompiledGraph<AgentState> mockCompiledGraph;

    private OrchestrationServiceImpl orchestrationService;
    private List<NodeAction> templateNodes;

    @BeforeEach
    void setUp() {
        templateNodes = Arrays.asList(mockNode1, mockNode2, mockNode3, mockNode4);

        // Mock StateGraphBuilder to return a mock CompiledGraph
        when(stateGraphBuilder.buildGraph(templateNodes)).thenReturn(mockCompiledGraph);

        orchestrationService = new OrchestrationServiceImpl(
            contextAssemblyService,
            artifactOutputService,
            stateGraphBuilder,
            templateNodes,
            activityLogger
        );
    }

    @Test
    void shouldThrowExceptionWhenRequestIsNull() {
        assertThatThrownBy(() -> orchestrationService.orchestrate(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("OrchestrationRequest cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenWorkIntentIsNull() {
        // OrchestrationRequest validates workIntent at construction time
        assertThatThrownBy(() -> OrchestrationRequest.builder()
            .storyId(new StoryId("STORY-1"))
            .workIntent(null)
            .requestedBy("human")
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Work intent cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenWorkIntentIsBlank() {
        // OrchestrationRequest validates workIntent at construction time
        assertThatThrownBy(() -> OrchestrationRequest.builder()
            .storyId(new StoryId("STORY-1"))
            .workIntent("   ")
            .requestedBy("human")
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Work intent cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenRequestedByIsNull() {
        // OrchestrationRequest validates requestedBy at construction time
        assertThatThrownBy(() -> OrchestrationRequest.builder()
            .storyId(new StoryId("STORY-1"))
            .workIntent("Do research")
            .requestedBy(null)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("RequestedBy cannot be null or blank");
    }

    @Test
    void shouldExecuteFullOrchestrationFlowSuccessfully() {
        // Given
        StoryId storyId = new StoryId("STORY-123");
        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research game mechanics",
            "human"
        );

        Story story = new Story(storyId, "Test Story", "Test summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Test work intent")
            .build();

        // Mock context assembly
        when(contextAssemblyService.assembleContext(request)).thenReturn(context);

        // Mock StateGraph execution
        OrchestrationState finalState = new OrchestrationState();
        finalState.addArtifact(new ArtifactReference("artifact-one", "Content 1", "researcher"));
        finalState.addArtifact(new ArtifactReference("artifact-two", "Content 2", "designer"));

        when(mockCompiledGraph.invoke(anyMap())).thenReturn(Optional.of(finalState.toAgentState()));

        // When
        OrchestrationResult result = orchestrationService.orchestrate(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOutcome()).isEqualTo(ExecutionOutcome.SUCCESS);
        assertThat(result.getArtifacts()).hasSize(2);
        assertThat(result.getExecutionSummary()).contains("Created 2 artifact");
        assertThat(result.isSuccess()).isTrue();

        // Verify context assembly was called
        verify(contextAssemblyService).assembleContext(request);

        // Verify StateGraph was invoked
        verify(mockCompiledGraph).invoke(anyMap());

        // Verify artifacts were written
        verify(artifactOutputService).writeArtifacts(request, result);

        // Verify logging
        verify(activityLogger).logSuccess(
            eq("orchestrator"),
            eq("orchestration_started"),
            eq("STORY-123"),
            anyMap()
        );
        verify(activityLogger).logSuccess(
            eq("orchestrator"),
            eq("orchestration_completed"),
            eq("STORY-123"),
            anyMap()
        );
    }

    @Test
    void shouldHandleProjectLevelRequest() {
        // Given
        OrchestrationRequest request = OrchestrationRequest.forProject(
            "Create project plan",
            "orchestrator"
        );

        Story projectPlanning = new Story(new StoryId("PROJECT-PLANNING"), "Project Planning", "Project level planning", WorkflowState.TODO, PrioritizationState.BACKLOG);
        OrchestrationContext context = OrchestrationContext.builder()
            .story(projectPlanning)
            .workIntent("Create project plan")
            .build();

        when(contextAssemblyService.assembleContext(request)).thenReturn(context);

        OrchestrationState finalState = new OrchestrationState();
        finalState.addArtifact(new ArtifactReference("project-plan", "Content", "planner"));

        when(mockCompiledGraph.invoke(anyMap())).thenReturn(Optional.of(finalState.toAgentState()));

        // When
        OrchestrationResult result = orchestrationService.orchestrate(request);

        // Then
        assertThat(result.isSuccess()).isTrue();

        // Verify logging used PROJECT-PLANNING as storyId
        verify(activityLogger).logSuccess(
            eq("orchestrator"),
            eq("orchestration_started"),
            eq("PROJECT-PLANNING"),
            anyMap()
        );
    }

    @Test
    void shouldHandleOrchestrationWithNoArtifacts() {
        // Given
        StoryId storyId = new StoryId("STORY-456");
        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Analyze story",
            "human"
        );

        Story story = new Story(storyId, "Test Story", "Test summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Test work intent")
            .build();

        when(contextAssemblyService.assembleContext(request)).thenReturn(context);

        // StateGraph returns empty state (no artifacts)
        OrchestrationState emptyState = new OrchestrationState();
        when(mockCompiledGraph.invoke(anyMap())).thenReturn(Optional.of(emptyState.toAgentState()));

        // When
        OrchestrationResult result = orchestrationService.orchestrate(request);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getArtifacts()).isEmpty();
        assertThat(result.getExecutionSummary()).contains("no artifacts");

        // Verify artifacts were NOT written (no artifacts to write)
        verify(artifactOutputService, never()).writeArtifacts(any(), any());
    }

    @Test
    void shouldPropagateContextAssemblyException() {
        // Given
        OrchestrationRequest request = OrchestrationRequest.forStory(
            new StoryId("STORY-999"),
            "Research",
            "human"
        );

        when(contextAssemblyService.assembleContext(request))
            .thenThrow(new RuntimeException("Story not found"));

        // When/Then
        assertThatThrownBy(() -> orchestrationService.orchestrate(request))
            .isInstanceOf(OrchestrationContextException.class)
            .hasMessageContaining("Failed to assemble context");

        // Verify failure was logged
        verify(activityLogger).logFailure(
            eq("orchestrator"),
            eq("orchestration_failed"),
            eq("STORY-999"),
            anyString(),
            anyMap()
        );
    }

    @Test
    void shouldPropagateNodeExecutionException() {
        // Given
        StoryId storyId = new StoryId("STORY-789");
        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Research",
            "human"
        );

        Story story = new Story(storyId, "Test Story", "Test summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Test work intent")
            .build();

        when(contextAssemblyService.assembleContext(request)).thenReturn(context);
        when(mockCompiledGraph.invoke(anyMap()))
            .thenThrow(new RuntimeException("StateGraph execution failed"));

        // When/Then
        assertThatThrownBy(() -> orchestrationService.orchestrate(request))
            .isInstanceOf(OrchestrationExecutionException.class)
            .hasMessageContaining("Failed to execute StateGraph");

        // Verify failure was logged
        verify(activityLogger).logFailure(
            eq("orchestrator"),
            eq("orchestration_failed"),
            eq("STORY-789"),
            anyString(),
            anyMap()
        );
    }

    @Test
    void shouldConvertContextToStateCorrectly() {
        // Given
        StoryId storyId = new StoryId("STORY-100");
        String workIntent = "Do some research";
        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            workIntent,
            "human"
        );

        Story story = new Story(storyId, "Test Story", "Test summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        story.setDescription("Test description");
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Test work intent")
            .build();

        when(contextAssemblyService.assembleContext(request)).thenReturn(context);

        // Capture the state data passed to StateGraph
        ArgumentCaptor<Map<String, Object>> stateDataCaptor = ArgumentCaptor.forClass(Map.class);

        OrchestrationState finalState = new OrchestrationState();
        finalState.addArtifact(new ArtifactReference("test", "content", "researcher"));

        when(mockCompiledGraph.invoke(stateDataCaptor.capture())).thenReturn(Optional.of(finalState.toAgentState()));

        // When
        orchestrationService.orchestrate(request);

        // Then - verify state conversion
        Map<String, Object> capturedData = stateDataCaptor.getValue();
        assertThat(capturedData.get(OrchestrationState.WORK_INTENT)).isEqualTo(workIntent);
        assertThat(capturedData.get(OrchestrationState.STORY)).isEqualTo(story);
        assertThat(capturedData.get(OrchestrationState.EXECUTION_ID)).isNotNull();
    }

    @Test
    void shouldExecuteStateGraphWithAccumulatedArtifacts() {
        // Given
        StoryId storyId = new StoryId("STORY-200");
        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Multi-node test",
            "human"
        );

        Story story = new Story(storyId, "Test Story", "Test summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Test work intent")
            .build();

        when(contextAssemblyService.assembleContext(request)).thenReturn(context);

        // Mock StateGraph to return state with accumulated artifacts
        OrchestrationState finalState = new OrchestrationState();
        finalState.addArtifact(new ArtifactReference("artifact-one", "Content 1", "researcher"));
        finalState.addArtifact(new ArtifactReference("artifact-two", "Content 2", "designer"));

        when(mockCompiledGraph.invoke(anyMap())).thenReturn(Optional.of(finalState.toAgentState()));

        // When
        OrchestrationResult result = orchestrationService.orchestrate(request);

        // Then - verify StateGraph accumulated artifacts
        assertThat(result.getArtifacts()).hasSize(2);
        assertThat(result.getArtifacts().get(0).getName()).isEqualTo("artifact-one");
        assertThat(result.getArtifacts().get(1).getName()).isEqualTo("artifact-two");
    }

    @Test
    void shouldLogExecutionIdInAllLogEntries() {
        // Given
        StoryId storyId = new StoryId("STORY-300");
        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Test logging",
            "human"
        );

        Story story = new Story(storyId, "Test Story", "Test summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Test work intent")
            .build();

        when(contextAssemblyService.assembleContext(request)).thenReturn(context);

        OrchestrationState finalState = new OrchestrationState();
        finalState.addArtifact(new ArtifactReference("test", "content", "researcher"));

        when(mockCompiledGraph.invoke(anyMap())).thenReturn(Optional.of(finalState.toAgentState()));

        // When
        orchestrationService.orchestrate(request);

        // Then - capture log calls
        ArgumentCaptor<Map<String, Object>> startDetailsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, Object>> completedDetailsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(activityLogger).logSuccess(
            eq("orchestrator"),
            eq("orchestration_started"),
            eq("STORY-300"),
            startDetailsCaptor.capture()
        );

        verify(activityLogger).logSuccess(
            eq("orchestrator"),
            eq("orchestration_completed"),
            eq("STORY-300"),
            completedDetailsCaptor.capture()
        );

        // Verify executionId is present in both logs
        String startExecutionId = (String) startDetailsCaptor.getValue().get("executionId");
        String completedExecutionId = (String) completedDetailsCaptor.getValue().get("executionId");

        assertThat(startExecutionId).isNotNull();
        assertThat(completedExecutionId).isNotNull();
        assertThat(startExecutionId).isEqualTo(completedExecutionId);
    }

    @Test
    void shouldIncludeOutcomeAndArtifactCountInCompletedLog() {
        // Given
        StoryId storyId = new StoryId("STORY-400");
        OrchestrationRequest request = OrchestrationRequest.forStory(
            storyId,
            "Test metrics logging",
            "human"
        );

        Story story = new Story(storyId, "Test Story", "Test summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Test work intent")
            .build();

        when(contextAssemblyService.assembleContext(request)).thenReturn(context);

        OrchestrationState finalState = new OrchestrationState();
        finalState.addArtifact(new ArtifactReference("artifact-one", "Content 1", "researcher"));
        finalState.addArtifact(new ArtifactReference("artifact-two", "Content 2", "designer"));
        finalState.addArtifact(new ArtifactReference("artifact-three", "Content 3", "tester"));

        when(mockCompiledGraph.invoke(anyMap())).thenReturn(Optional.of(finalState.toAgentState()));

        // When
        orchestrationService.orchestrate(request);

        // Then
        ArgumentCaptor<Map<String, Object>> completedDetailsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(activityLogger).logSuccess(
            eq("orchestrator"),
            eq("orchestration_completed"),
            eq("STORY-400"),
            completedDetailsCaptor.capture()
        );

        Map<String, Object> details = completedDetailsCaptor.getValue();
        assertThat(details.get("outcome")).isEqualTo("SUCCESS");
        assertThat(details.get("artifactsCreated")).isEqualTo(3);
        assertThat(details.get("durationMs")).isNotNull();
    }
}
