package com.aiworkflow.workmanagement.orchestration.domain;

import com.aiworkflow.workmanagement.domain.valueobject.StoryId;

import java.util.Objects;

/**
 * Value object representing a request to execute orchestration.
 * <p>
 * Orchestration can be requested for:
 * - A specific story (storyId provided)
 * - Project-level work (storyId is null, routes to PROJECT-PLANNING meta work item)
 * <p>
 * The workIntent is a free-form string describing what work should be done,
 * allowing flexible orchestration without predefined operation types.
 */
public class OrchestrationRequest {
    private final StoryId storyId;
    private final String workIntent;
    private final String requestedBy;

    /**
     * Private constructor - use builder() instead.
     */
    private OrchestrationRequest(StoryId storyId, String workIntent, String requestedBy) {
        if (workIntent == null || workIntent.isBlank()) {
            throw new IllegalArgumentException("Work intent cannot be null or blank");
        }
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new IllegalArgumentException("RequestedBy cannot be null or blank");
        }

        this.storyId = storyId; // Can be null for PROJECT-PLANNING
        this.workIntent = workIntent;
        this.requestedBy = requestedBy;
    }

    /**
     * Creates a builder for OrchestrationRequest.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a story-specific orchestration request.
     *
     * @param storyId the story to perform orchestration on
     * @param workIntent description of what work to perform
     * @param requestedBy who requested this orchestration
     * @return a new OrchestrationRequest
     */
    public static OrchestrationRequest forStory(StoryId storyId, String workIntent, String requestedBy) {
        if (storyId == null) {
            throw new IllegalArgumentException("StoryId cannot be null for story-specific request. Use forProject() instead.");
        }
        return new Builder()
            .storyId(storyId)
            .workIntent(workIntent)
            .requestedBy(requestedBy)
            .build();
    }

    /**
     * Creates a project-level orchestration request (routes to PROJECT-PLANNING).
     *
     * @param workIntent description of what work to perform
     * @param requestedBy who requested this orchestration
     * @return a new OrchestrationRequest with null storyId
     */
    public static OrchestrationRequest forProject(String workIntent, String requestedBy) {
        return new Builder()
            .storyId(null) // Explicitly null for PROJECT-PLANNING
            .workIntent(workIntent)
            .requestedBy(requestedBy)
            .build();
    }

    public StoryId getStoryId() {
        return storyId;
    }

    public String getWorkIntent() {
        return workIntent;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    /**
     * Checks if this is a project-level request (routes to PROJECT-PLANNING).
     *
     * @return true if storyId is null
     */
    public boolean isProjectLevel() {
        return storyId == null;
    }

    /**
     * Checks if this is a story-specific request.
     *
     * @return true if storyId is not null
     */
    public boolean isStorySpecific() {
        return storyId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrchestrationRequest that = (OrchestrationRequest) o;
        return Objects.equals(storyId, that.storyId) &&
               Objects.equals(workIntent, that.workIntent) &&
               Objects.equals(requestedBy, that.requestedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storyId, workIntent, requestedBy);
    }

    @Override
    public String toString() {
        return "OrchestrationRequest{" +
            "storyId=" + (storyId != null ? storyId : "PROJECT-PLANNING") +
            ", workIntent='" + workIntent + '\'' +
            ", requestedBy='" + requestedBy + '\'' +
            '}';
    }

    /**
     * Builder class for OrchestrationRequest.
     */
    public static class Builder {
        private StoryId storyId;
        private String workIntent;
        private String requestedBy;

        private Builder() {}

        /**
         * Sets the story ID. Can be null for project-level requests.
         *
         * @param storyId the story ID, or null for PROJECT-PLANNING
         * @return this builder
         */
        public Builder storyId(StoryId storyId) {
            this.storyId = storyId;
            return this;
        }

        /**
         * Sets the work intent (required).
         *
         * @param workIntent description of what work to perform
         * @return this builder
         */
        public Builder workIntent(String workIntent) {
            this.workIntent = workIntent;
            return this;
        }

        /**
         * Sets who requested this orchestration (required).
         *
         * @param requestedBy the requester (user, agent, system)
         * @return this builder
         */
        public Builder requestedBy(String requestedBy) {
            this.requestedBy = requestedBy;
            return this;
        }

        /**
         * Builds the OrchestrationRequest.
         *
         * @return a new OrchestrationRequest instance
         * @throws IllegalArgumentException if required fields are missing
         */
        public OrchestrationRequest build() {
            return new OrchestrationRequest(storyId, workIntent, requestedBy);
        }
    }
}
