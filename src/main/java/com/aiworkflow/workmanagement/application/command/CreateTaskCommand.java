package com.aiworkflow.workmanagement.application.command;

import java.util.Objects;

/**
 * Command to create a new Task within a Story.
 */
public class CreateTaskCommand {
    private final String taskId;
    private final String storyId;
    private final String title;
    private final String description;
    private final String assignedTo;
    private final Integer estimatedHours;

    public CreateTaskCommand(String taskId, String storyId, String title, String description) {
        this(taskId, storyId, title, description, null, null);
    }

    public CreateTaskCommand(String taskId, String storyId, String title, String description,
                            String assignedTo, Integer estimatedHours) {
        this.taskId = Objects.requireNonNull(taskId, "Task ID cannot be null");
        this.storyId = Objects.requireNonNull(storyId, "Story ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = description;
        this.assignedTo = assignedTo;
        this.estimatedHours = estimatedHours;
    }

    // Getters
    public String getTaskId() { return taskId; }
    public String getStoryId() { return storyId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAssignedTo() { return assignedTo; }
    public Integer getEstimatedHours() { return estimatedHours; }
}
