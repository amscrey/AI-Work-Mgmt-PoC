package com.aiworkflow.workmanagement.application.command;

import java.util.List;
import java.util.Objects;

/**
 * Command to update an existing Story.
 */
public class UpdateStoryCommand {
    private final String storyId;
    private final String title;
    private final String summary;
    private final String description;
    private final List<String> acceptanceCriteria;
    private final List<String> tags;
    private final Integer estimatedEffort;

    public UpdateStoryCommand(String storyId, String title, String summary) {
        this(storyId, title, summary, null, null, null, null);
    }

    public UpdateStoryCommand(String storyId, String title, String summary, String description,
                             List<String> acceptanceCriteria, List<String> tags, Integer estimatedEffort) {
        this.storyId = Objects.requireNonNull(storyId, "Story ID cannot be null");
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.acceptanceCriteria = acceptanceCriteria;
        this.tags = tags;
        this.estimatedEffort = estimatedEffort;
    }

    // Getters
    public String getStoryId() { return storyId; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public List<String> getAcceptanceCriteria() { return acceptanceCriteria; }
    public List<String> getTags() { return tags; }
    public Integer getEstimatedEffort() { return estimatedEffort; }
}
