package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.RoleName;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object for Story persistence.
 * Maps between domain Story and JSON representation.
 */
public class StoryDTO {

    private final String id;
    private final String title;
    private final String summary;
    private final String description;
    private final String workflowState;
    private final String prioritizationState;
    private final String assignedTo;
    private final String author;
    private final Integer estimatedEffort;
    private final List<String> acceptanceCriteria;
    private final List<String> tags;
    private final Instant createdAt;
    private final Instant updatedAt;

    @JsonCreator
    public StoryDTO(
        @JsonProperty("id") String id,
        @JsonProperty("title") String title,
        @JsonProperty("summary") String summary,
        @JsonProperty("description") String description,
        @JsonProperty("workflowState") String workflowState,
        @JsonProperty("prioritizationState") String prioritizationState,
        @JsonProperty("assignedTo") String assignedTo,
        @JsonProperty("author") String author,
        @JsonProperty("estimatedEffort") Integer estimatedEffort,
        @JsonProperty("acceptanceCriteria") List<String> acceptanceCriteria,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("createdAt") Instant createdAt,
        @JsonProperty("updatedAt") Instant updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.workflowState = workflowState;
        this.prioritizationState = prioritizationState;
        this.assignedTo = assignedTo;
        this.author = author;
        this.estimatedEffort = estimatedEffort;
        this.acceptanceCriteria = acceptanceCriteria;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters for Jackson
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getWorkflowState() { return workflowState; }
    public String getPrioritizationState() { return prioritizationState; }
    public String getAssignedTo() { return assignedTo; }
    public String getAuthor() { return author; }
    public Integer getEstimatedEffort() { return estimatedEffort; }
    public List<String> getAcceptanceCriteria() { return acceptanceCriteria; }
    public List<String> getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    /**
     * Converts domain Story to DTO.
     */
    public static StoryDTO fromDomain(Story story) {
        return new StoryDTO(
            story.getId().getValue(),
            story.getTitle(),
            story.getSummary(),
            story.getDescription(),
            story.getWorkflowState().name(),
            story.getPrioritizationState().name(),
            story.getAssignedTo(),
            story.getAuthor() != null ? story.getAuthor().getValue() : null,
            story.getEstimatedEffort(),
            story.getAcceptanceCriteria(),
            story.getTags(),
            story.getCreatedAt(),
            story.getUpdatedAt()
        );
    }

    /**
     * Converts DTO to domain Story.
     */
    public Story toDomain() {
        Story story = new Story(
            new StoryId(id),
            title,
            summary,
            WorkflowState.valueOf(workflowState),
            PrioritizationState.valueOf(prioritizationState)
        );

        // Set optional fields
        if (description != null) {
            story.setDescription(description);
        }
        if (assignedTo != null) {
            story.assignTo(assignedTo);
        }
        if (author != null) {
            story.setAuthor(new RoleName(author));
        }
        if (estimatedEffort != null) {
            story.setEstimatedEffort(estimatedEffort);
        }
        if (acceptanceCriteria != null) {
            acceptanceCriteria.forEach(story::addAcceptanceCriterion);
        }
        if (tags != null) {
            tags.forEach(story::addTag);
        }

        return story;
    }
}
