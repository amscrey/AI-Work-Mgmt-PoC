package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class StoryCreateRequest {
    @NotBlank
    private String storyId;
    @NotBlank
    private String title;
    @NotBlank
    private String summary;
    private String description;
    @NotBlank
    private String author;
    private WorkflowState initialState;
    @NotNull
    private PrioritizationState prioritization;
    private List<String> acceptanceCriteria;
    private List<String> tags;
    private Integer estimatedEffort;

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public WorkflowState getInitialState() {
        return initialState;
    }

    public void setInitialState(WorkflowState initialState) {
        this.initialState = initialState;
    }

    public PrioritizationState getPrioritization() {
        return prioritization;
    }

    public void setPrioritization(PrioritizationState prioritization) {
        this.prioritization = prioritization;
    }

    public List<String> getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public void setAcceptanceCriteria(List<String> acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getEstimatedEffort() {
        return estimatedEffort;
    }

    public void setEstimatedEffort(Integer estimatedEffort) {
        this.estimatedEffort = estimatedEffort;
    }
}

