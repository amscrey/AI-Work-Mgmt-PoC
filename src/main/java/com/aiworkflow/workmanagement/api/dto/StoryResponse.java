package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;

import java.time.Instant;
import java.util.List;

public class StoryResponse {
    private final String storyId;
    private final String title;
    private final String summary;
    private final String description;
    private final WorkflowState workflowState;
    private final PrioritizationState prioritizationState;
    private final String author;
    private final List<String> acceptanceCriteria;
    private final List<String> tags;
    private final String assignedTo;
    private final Integer estimatedEffort;
    private final Instant createdAt;
    private final Instant updatedAt;

    public StoryResponse(
        String storyId,
        String title,
        String summary,
        String description,
        WorkflowState workflowState,
        PrioritizationState prioritizationState,
        String author,
        List<String> acceptanceCriteria,
        List<String> tags,
        String assignedTo,
        Integer estimatedEffort,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.storyId = storyId;
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.workflowState = workflowState;
        this.prioritizationState = prioritizationState;
        this.author = author;
        this.acceptanceCriteria = acceptanceCriteria;
        this.tags = tags;
        this.assignedTo = assignedTo;
        this.estimatedEffort = estimatedEffort;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StoryResponse from(Story story) {
        String author = story.getAuthor() != null ? story.getAuthor().getValue() : null;
        return new StoryResponse(
            story.getId().getValue(),
            story.getTitle(),
            story.getSummary(),
            story.getDescription(),
            story.getWorkflowState(),
            story.getPrioritizationState(),
            author,
            story.getAcceptanceCriteria(),
            story.getTags(),
            story.getAssignedTo(),
            story.getEstimatedEffort(),
            story.getCreatedAt(),
            story.getUpdatedAt()
        );
    }

    public String getStoryId() {
        return storyId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowState getWorkflowState() {
        return workflowState;
    }

    public PrioritizationState getPrioritizationState() {
        return prioritizationState;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Integer getEstimatedEffort() {
        return estimatedEffort;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

