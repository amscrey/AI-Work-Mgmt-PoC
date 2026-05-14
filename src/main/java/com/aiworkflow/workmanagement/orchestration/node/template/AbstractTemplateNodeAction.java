package com.aiworkflow.workmanagement.orchestration.node.template;

import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.orchestration.config.OrchestrationConfig;
import com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import com.aiworkflow.workmanagement.orchestration.node.NodeAction;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for template-based node actions.
 * <p>
 * Template nodes generate deterministic markdown output based on story metadata
 * without making LLM API calls. This enables:
 * <ul>
 *   <li>Testing without API costs</li>
 *   <li>Predictable output for development</li>
 *   <li>Fast execution (no network calls)</li>
 * </ul>
 *
 * <h2>Subclass Responsibilities</h2>
 * Subclasses must implement:
 * <ul>
 *   <li>{@link #getNodeName()} - Unique node identifier</li>
 *   <li>{@link #getArtifactName()} - Base artifact name (without timestamp)</li>
 *   <li>{@link #getRoleName()} - Agent role creating the artifact</li>
 *   <li>{@link #generateContent(OrchestrationState)} - Template rendering logic</li>
 * </ul>
 *
 * <h2>Execution Flow</h2>
 * <ol>
 *   <li>Simulate delay if configured</li>
 *   <li>Call {@link #generateContent(OrchestrationState)} to render template</li>
 *   <li>Create {@link ArtifactReference} with timestamp</li>
 *   <li>Return updated state with artifact appended</li>
 * </ol>
 *
 * @see NodeAction
 */
public abstract class AbstractTemplateNodeAction implements NodeAction {

    protected final OrchestrationConfig config;

    /**
     * Constructs a template node action with configuration.
     *
     * @param config orchestration configuration (for delay simulation)
     */
    protected AbstractTemplateNodeAction(OrchestrationConfig config) {
        this.config = config;
    }

    @Override
    public OrchestrationState execute(OrchestrationState state) {
        // Simulate execution delay if configured
        if (config.getTemplate().isSimulateDelay()) {
            try {
                Thread.sleep(config.getTemplate().getDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Template node execution interrupted", e);
            }
        }

        // Generate artifact content from template
        String content = generateContent(state);

        // Create artifact reference
        ArtifactReference artifact = ArtifactReference.builder()
            .name(getArtifactName())
            .content(content)
            .createdBy(getRoleName())
            .build();

        // Add artifact to state
        state.addArtifact(artifact);

        // Update current role to track progress
        state.setCurrentRole(getRoleName());

        return state;
    }

    /**
     * Gets the unique node name for this template action.
     * <p>
     * This is used to track execution progress in the state.
     *
     * @return node name (e.g., "research", "planning", "design")
     */
    protected abstract String getNodeName();

    /**
     * Gets the base artifact name (without role or timestamp).
     * <p>
     * Example: "research-findings" (not "research-findings__researcher__2026-03-31T100000Z.md")
     *
     * @return base artifact name
     */
    protected abstract String getArtifactName();

    /**
     * Gets the agent role name that creates this artifact.
     * <p>
     * This is used in the artifact filename and for attribution.
     *
     * @return role name (e.g., "researcher", "logician", "designer")
     */
    protected abstract String getRoleName();

    /**
     * Generates the artifact content from the orchestration state.
     * <p>
     * Implementations should read story metadata, work intent, and other
     * context from the state and generate realistic markdown output.
     *
     * @param state current orchestration state
     * @return generated markdown content
     */
    protected abstract String generateContent(OrchestrationState state);

    /**
     * Helper method to extract story title from state.
     *
     * @param state orchestration state
     * @return story title or "Unknown Story"
     */
    protected String getStoryTitle(OrchestrationState state) {
        Story story = state.getStory();
        return story != null ? story.getTitle() : "Unknown Story";
    }

    /**
     * Helper method to extract work intent from state.
     *
     * @param state orchestration state
     * @return work intent or empty string
     */
    protected String getWorkIntent(OrchestrationState state) {
        String intent = state.getWorkIntent();
        return intent != null ? intent : "";
    }

    /**
     * Helper method to extract story description from state.
     *
     * @param state orchestration state
     * @return story description or empty string
     */
    protected String getStoryDescription(OrchestrationState state) {
        Story story = state.getStory();
        if (story != null) {
            String description = story.getDescription();
            return description != null ? description : "";
        }
        return "";
    }
}
