package com.aiworkflow.workmanagement.application.command;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;

import java.util.List;
import java.util.Objects;

/**
 * Command to create a new Story.
 */
public class CreateStoryCommand {
    private final String storyId;
    private final String title;
    private final String summary;
    private final String description;
    private final String author;
    private final WorkflowState initialState;
    private final PrioritizationState prioritization;
    private final List<String> acceptanceCriteria;
    private final List<String> tags;
    private final Integer estimatedEffort;

    public CreateStoryCommand(String storyId, String title, String summary,
                             String author, PrioritizationState prioritization) {
        this(storyId, title, summary, null, author, WorkflowState.TODO, prioritization, null, null, null);
    }

    public CreateStoryCommand(String storyId, String title, String summary, String description,
                             String author, WorkflowState initialState, PrioritizationState prioritization,
                             List<String> acceptanceCriteria, List<String> tags, Integer estimatedEffort) {
        this.storyId = Objects.requireNonNull(storyId, "Story ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.summary = Objects.requireNonNull(summary, "Summary cannot be null");
        this.description = description;
        this.author = Objects.requireNonNull(author, "Author cannot be null");
        this.initialState = initialState != null ? initialState : WorkflowState.TODO;
        this.prioritization = Objects.requireNonNull(prioritization, "Prioritization cannot be null");
        this.acceptanceCriteria = acceptanceCriteria;
        this.tags = tags;
        this.estimatedEffort = estimatedEffort;
    }

    // Getters
    public String getStoryId() { return storyId; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public WorkflowState getInitialState() { return initialState; }
    public PrioritizationState getPrioritization() { return prioritization; }
    public List<String> getAcceptanceCriteria() { return acceptanceCriteria; }
    public List<String> getTags() { return tags; }
    public Integer getEstimatedEffort() { return estimatedEffort; }
}
