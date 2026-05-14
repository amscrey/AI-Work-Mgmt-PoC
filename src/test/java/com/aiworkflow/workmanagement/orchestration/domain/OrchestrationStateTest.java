package com.aiworkflow.workmanagement.orchestration.domain;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrchestrationState Tests")
class OrchestrationStateTest {

    @Test
    @DisplayName("Should create empty state")
    void shouldCreateEmptyState() {
        OrchestrationState state = new OrchestrationState();

        assertThat(state.getWorkIntent()).isNull();
        assertThat(state.getStory()).isNull();
        assertThat(state.getSelectedRole()).isNull();
        assertThat(state.getCurrentRole()).isNull();
        assertThat(state.getExecutionId()).isNull();
        assertThat(state.getArtifactsCreated()).isEmpty();
    }

    @Test
    @DisplayName("Should create state with initial values")
    void shouldCreateStateWithInitialValues() {
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.WORK_INTENT, "Research game mechanics");
        initialState.put(OrchestrationState.EXECUTION_ID, "exec-123");

        OrchestrationState state = new OrchestrationState(initialState);

        assertThat(state.getWorkIntent()).isEqualTo("Research game mechanics");
        assertThat(state.getExecutionId()).isEqualTo("exec-123");
    }

    @Test
    @DisplayName("Should set and get work intent")
    void shouldSetAndGetWorkIntent() {
        OrchestrationState state = new OrchestrationState();

        state.setWorkIntent("Design user interface");

        assertThat(state.getWorkIntent()).isEqualTo("Design user interface");
    }

    @Test
    @DisplayName("Should set and get story")
    void shouldSetAndGetStory() {
        OrchestrationState state = new OrchestrationState();
        Story story = new Story(
            new StoryId("STORY-123"),
            "Test Story",
            "Summary",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );

        state.setStory(story);

        assertThat(state.getStory()).isEqualTo(story);
    }

    @Test
    @DisplayName("Should set and get selected role")
    void shouldSetAndGetSelectedRole() {
        OrchestrationState state = new OrchestrationState();

        state.setSelectedRole("researcher");

        assertThat(state.getSelectedRole()).isEqualTo("researcher");
    }

    @Test
    @DisplayName("Should set and get current role")
    void shouldSetAndGetCurrentRole() {
        OrchestrationState state = new OrchestrationState();

        state.setCurrentRole("logician");

        assertThat(state.getCurrentRole()).isEqualTo("logician");
    }

    @Test
    @DisplayName("Should set and get execution ID")
    void shouldSetAndGetExecutionId() {
        OrchestrationState state = new OrchestrationState();

        state.setExecutionId("exec-456");

        assertThat(state.getExecutionId()).isEqualTo("exec-456");
    }

    @Test
    @DisplayName("Should initialize artifacts created as empty list")
    void shouldInitializeArtifactsCreatedAsEmptyList() {
        OrchestrationState state = new OrchestrationState();

        List<ArtifactReference> artifacts = state.getArtifactsCreated();

        assertThat(artifacts).isNotNull();
        assertThat(artifacts).isEmpty();
    }

    @Test
    @DisplayName("Should add artifacts to artifacts created list")
    void shouldAddArtifactsToArtifactsCreatedList() {
        OrchestrationState state = new OrchestrationState();
        ArtifactReference artifact1 = new ArtifactReference("research", "content1", "researcher");
        ArtifactReference artifact2 = new ArtifactReference("analysis", "content2", "logician");

        state.addArtifact(artifact1);
        state.addArtifact(artifact2);

        List<ArtifactReference> artifacts = state.getArtifactsCreated();
        assertThat(artifacts).hasSize(2);
        assertThat(artifacts).containsExactly(artifact1, artifact2);
    }

    @Test
    @DisplayName("Should handle null artifacts list in initial state gracefully")
    void shouldHandleNullArtifactsListInInitialStateGracefully() {
        Map<String, Object> initialState = new HashMap<>();
        initialState.put(OrchestrationState.ARTIFACTS_CREATED, null);

        OrchestrationState state = new OrchestrationState(initialState);

        state.addArtifact(new ArtifactReference("art", "content", "researcher"));

        assertThat(state.getArtifactsCreated()).hasSize(1);
    }

    @Test
    @DisplayName("Should set arbitrary values using setValue")
    void shouldSetArbitraryValuesUsingSetValue() {
        OrchestrationState state = new OrchestrationState();

        state.setValue("custom-key", "custom-value");

        assertThat((String) state.getValue("custom-key")).isEqualTo("custom-value");
    }

    @Test
    @DisplayName("Should return null for non-existent keys")
    void shouldReturnNullForNonExistentKeys() {
        OrchestrationState state = new OrchestrationState();

        Object value = state.getValue("non-existent");
        assertThat(value).isNull();
    }

    @Test
    @DisplayName("Should return state map with all values")
    void shouldReturnStateMapWithAllValues() {
        OrchestrationState state = new OrchestrationState();
        state.setWorkIntent("work");
        state.setExecutionId("exec-1");
        state.setSelectedRole("researcher");

        Map<String, Object> stateMap = state.data();

        assertThat(stateMap).containsKey(OrchestrationState.WORK_INTENT);
        assertThat(stateMap).containsKey(OrchestrationState.EXECUTION_ID);
        assertThat(stateMap).containsKey(OrchestrationState.SELECTED_ROLE);
        assertThat(stateMap).containsKey(OrchestrationState.ARTIFACTS_CREATED);
    }

    @Test
    @DisplayName("Should return actual state map (not defensive copy)")
    void shouldReturnActualStateMap() {
        OrchestrationState state = new OrchestrationState();
        state.setWorkIntent("original");

        Map<String, Object> stateMap = state.data();
        stateMap.put(OrchestrationState.WORK_INTENT, "modified");

        // State should be modified since data() returns actual map
        assertThat(state.getWorkIntent()).isEqualTo("modified");
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        OrchestrationState state = new OrchestrationState();
        state.setExecutionId("exec-123");
        state.setWorkIntent("Research");
        state.setSelectedRole("researcher");

        Story story = new Story(
            new StoryId("STORY-456"),
            "Story",
            "Summary",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );
        state.setStory(story);

        state.addArtifact(new ArtifactReference("art", "content", "researcher"));

        String toString = state.toString();

        assertThat(toString).contains("exec-123");
        assertThat(toString).contains("Research");
        assertThat(toString).contains("researcher");
        assertThat(toString).contains("STORY-456");
        assertThat(toString).contains("artifactsCreated=1");
    }

    @Test
    @DisplayName("Should handle null story in toString")
    void shouldHandleNullStoryInToString() {
        OrchestrationState state = new OrchestrationState();
        state.setExecutionId("exec-1");

        String toString = state.toString();

        assertThat(toString).contains("exec-1");
        assertThat(toString).contains("storyId=null");
    }

    @Test
    @DisplayName("Should define all required state keys as constants")
    void shouldDefineAllRequiredStateKeysAsConstants() {
        assertThat(OrchestrationState.WORK_INTENT).isEqualTo("workIntent");
        assertThat(OrchestrationState.STORY).isEqualTo("story");
        assertThat(OrchestrationState.SELECTED_ROLE).isEqualTo("selectedRole");
        assertThat(OrchestrationState.ARTIFACTS_CREATED).isEqualTo("artifactsCreated");
        assertThat(OrchestrationState.CURRENT_ROLE).isEqualTo("currentRole");
        assertThat(OrchestrationState.EXECUTION_ID).isEqualTo("executionId");
    }

    @Test
    @DisplayName("Should preserve artifacts when getting state map")
    void shouldPreserveArtifactsWhenGettingStateMap() {
        OrchestrationState state = new OrchestrationState();
        ArtifactReference artifact = new ArtifactReference("art", "content", "researcher");
        state.addArtifact(artifact);

        Map<String, Object> stateMap = state.data();

        @SuppressWarnings("unchecked")
        List<ArtifactReference> artifacts = (List<ArtifactReference>) stateMap.get(OrchestrationState.ARTIFACTS_CREATED);

        assertThat(artifacts).hasSize(1);
        assertThat(artifacts).contains(artifact);
    }
}
