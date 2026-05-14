package com.aiworkflow.workmanagement.orchestration.domain;

import com.aiworkflow.workmanagement.domain.model.Story;
import org.bsc.langgraph4j.state.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable state object for orchestration workflows.
 * <p>
 * This is our domain object with a mutable API for convenient use in services and nodes.
 * It can be converted to/from langgraph4j's immutable AgentState at the StateGraph boundary.
 * <p>
 * OrchestrationState contains all the context and accumulates artifacts as the workflow executes.
 * <p>
 * State Keys:
 * - WORK_INTENT: String - what work to perform
 * - STORY: Story - the story being worked on
 * - SELECTED_ROLE: String - the agent role selected for execution
 * - ARTIFACTS_CREATED: List<ArtifactReference> - artifacts produced (accumulated via Appender)
 * - CURRENT_ROLE: String - the role currently executing
 * - EXECUTION_ID: String - unique ID for this execution
 */
public class OrchestrationState {

    private static final Logger logger = LoggerFactory.getLogger(OrchestrationState.class);

    // State keys as constants for type safety
    public static final String WORK_INTENT = "workIntent";
    public static final String STORY = "story";
    public static final String SELECTED_ROLE = "selectedRole";
    public static final String ARTIFACTS_CREATED = "artifactsCreated";
    public static final String CURRENT_ROLE = "currentRole";
    public static final String EXECUTION_ID = "executionId";

    // Internal mutable state map
    private final Map<String, Object> state;

    /**
     * Creates a new OrchestrationState with empty state.
     */
    public OrchestrationState() {
        this.state = new HashMap<>();
        this.state.put(ARTIFACTS_CREATED, new ArrayList<ArtifactReference>());
    }

    /**
     * Creates a new OrchestrationState with initial values.
     *
     * @param initialState initial state values
     */
    public OrchestrationState(Map<String, Object> initialState) {
        this.state = new HashMap<>(initialState);
        if (!this.state.containsKey(ARTIFACTS_CREATED) || this.state.get(ARTIFACTS_CREATED) == null) {
            this.state.put(ARTIFACTS_CREATED, new ArrayList<ArtifactReference>());
        }
    }

    /**
     * Gets a value from the state.
     *
     * @param key the state key
     * @return the value, or null if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) state.get(key);
    }

    /**
     * Sets a value in the state.
     *
     * @param key the state key
     * @param value the value to set
     */
    public void setValue(String key, Object value) {
        state.put(key, value);
    }

    /**
     * Gets the work intent.
     *
     * @return the work intent string
     */
    public String getWorkIntent() {
        return getValue(WORK_INTENT);
    }

    /**
     * Sets the work intent.
     *
     * @param workIntent the work intent
     */
    public void setWorkIntent(String workIntent) {
        setValue(WORK_INTENT, workIntent);
    }

    /**
     * Gets the story.
     *
     * @return the Story entity
     */
    public Story getStory() {
        Story story = getValue(STORY);
        if (story != null) {
            logger.warn("OrchestrationState.getStory classloaders: instance={}, class={}",
                story.getClass().getClassLoader(),
                Story.class.getClassLoader());
        } else {
            logger.warn("OrchestrationState.getStory returned null (classloader check skipped)");
        }
        return story;
    }

    /**
     * Sets the story.
     *
     * @param story the Story entity
     */
    public void setStory(Story story) {
        setValue(STORY, story);
    }

    /**
     * Gets the selected role.
     *
     * @return the selected role name
     */
    public String getSelectedRole() {
        return getValue(SELECTED_ROLE);
    }

    /**
     * Sets the selected role.
     *
     * @param roleName the role name
     */
    public void setSelectedRole(String roleName) {
        setValue(SELECTED_ROLE, roleName);
    }

    /**
     * Gets the current role.
     *
     * @return the current role name
     */
    public String getCurrentRole() {
        return getValue(CURRENT_ROLE);
    }

    /**
     * Sets the current role.
     *
     * @param roleName the role name
     */
    public void setCurrentRole(String roleName) {
        setValue(CURRENT_ROLE, roleName);
    }

    /**
     * Gets the execution ID.
     *
     * @return the execution ID
     */
    public String getExecutionId() {
        return getValue(EXECUTION_ID);
    }

    /**
     * Sets the execution ID.
     *
     * @param executionId the execution ID
     */
    public void setExecutionId(String executionId) {
        setValue(EXECUTION_ID, executionId);
    }

    /**
     * Gets the list of artifacts created.
     * <p>
     * This list is accumulated as the workflow executes.
     *
     * @return the list of artifacts
     */
    public List<ArtifactReference> getArtifactsCreated() {
        List<ArtifactReference> artifacts = getValue(ARTIFACTS_CREATED);
        return artifacts != null ? artifacts : new ArrayList<>();
    }

    /**
     * Adds an artifact to the artifacts created list.
     *
     * @param artifact the artifact to add
     */
    public void addArtifact(ArtifactReference artifact) {
        List<ArtifactReference> artifacts = getArtifactsCreated();
        if (artifacts == null) {
            artifacts = new ArrayList<>();
            setValue(ARTIFACTS_CREATED, artifacts);
        }
        artifacts.add(artifact);
    }

    /**
     * Returns the underlying state map for direct access.
     *
     * @return the state map
     */
    public Map<String, Object> data() {
        return state;
    }

    /**
     * Converts this OrchestrationState to a langgraph4j AgentState.
     * <p>
     * Use this when passing state to StateGraph execution.
     *
     * @return immutable AgentState containing this state's data
     */
    public AgentState toAgentState() {
        return new AgentState(new HashMap<>(state));
    }

    /**
     * Creates an OrchestrationState from a langgraph4j AgentState.
     * <p>
     * Use this when receiving state from StateGraph execution.
     *
     * @param agentState the AgentState to convert
     * @return mutable OrchestrationState
     */
    public static OrchestrationState fromAgentState(AgentState agentState) {
        return new OrchestrationState(agentState.data());
    }

    public static OrchestrationState fromContext(OrchestrationContext context, String executionId) {
        Map<String, Object> stateData = new HashMap<>();
        stateData.put(EXECUTION_ID, executionId);
        stateData.put(WORK_INTENT, context.getWorkIntent());
        stateData.put(STORY, context.getStory());
        return new OrchestrationState(stateData);
    }

    public OrchestrationContext toContext() {
        Story story = getStory();
        return OrchestrationContext.builder()
            .story(story)
            .workIntent(getWorkIntent())
            .tasks(story != null ? story.getTasks() : List.of())
            .referenceFiles(List.of())
            .build();
    }

    public Map<String, Object> toStateMap() {
        return new HashMap<>(state);
    }

    /**
     * Creates a copy of this OrchestrationState.
     * <p>
     * This is used in tests to avoid shared mutable state between node executions.
     *
     * @return a copy of this OrchestrationState
     */
    public OrchestrationState copy() {
        Map<String, Object> copied = new HashMap<>(state);
        Object artifacts = copied.get(ARTIFACTS_CREATED);
        if (artifacts instanceof List<?> artifactList) {
            copied.put(ARTIFACTS_CREATED, new ArrayList<>(artifactList));
        }
        return new OrchestrationState(copied);
    }

    @Override
    public String toString() {
        return "OrchestrationState{" +
            "executionId='" + getExecutionId() + '\'' +
            ", workIntent='" + getWorkIntent() + '\'' +
            ", storyId=" + (getStory() != null ? getStory().getId() : null) +
            ", selectedRole='" + getSelectedRole() + '\'' +
            ", artifactsCreated=" + getArtifactsCreated().size() +
            '}';
    }
}
