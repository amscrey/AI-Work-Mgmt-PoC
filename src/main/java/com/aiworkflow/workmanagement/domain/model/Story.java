package com.aiworkflow.workmanagement.domain.model;

import com.aiworkflow.workmanagement.domain.valueobject.RoleName;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root representing a Story in the AI workflow management system.
 * A Story represents a unit of work that can contain multiple tasks and artifacts.
 */
public class Story implements Serializable {
    private static final long serialVersionUID = 1L;

    private final StoryId id;
    private String title;
    private String summary;
    private String description;
    private WorkflowState workflowState;
    private PrioritizationState prioritizationState;
    private RoleName author;
    private final List<Task> tasks;
    private final List<Artifact> artifacts;
    private final List<Comment> comments;
    private final List<String> acceptanceCriteria;
    private final List<String> tags;
    private final Instant createdAt;
    private Instant updatedAt;
    private String assignedTo;
    private Integer estimatedEffort;

    /**
     * Legacy constructor for backward compatibility.
     */
    public Story(StoryId id, String title, String description) {
        this(id, title, title, WorkflowState.TODO, PrioritizationState.BACKLOG);
        this.description = description;
    }

    /**
     * Primary constructor matching original design.
     */
    public Story(StoryId id, String title, String summary, WorkflowState state, PrioritizationState prioritization) {
        if (id == null) {
            throw new IllegalArgumentException("StoryId cannot be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Story title cannot be null or blank");
        }
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("Story summary cannot be null or blank");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Story title cannot exceed 200 characters");
        }
        if (summary.length() > 500) {
            throw new IllegalArgumentException("Story summary cannot exceed 500 characters");
        }

        this.id = id;
        this.title = title;
        this.summary = summary;
        this.workflowState = state != null ? state : WorkflowState.TODO;
        this.prioritizationState = prioritization != null ? prioritization : PrioritizationState.BACKLOG;
        this.tasks = new ArrayList<>();
        this.artifacts = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.acceptanceCriteria = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public StoryId getId() {
        return id;
    }

    public String getTitle() {
        return title;
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

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public List<Artifact> getArtifacts() {
        return Collections.unmodifiableList(artifacts);
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getSummary() {
        return summary;
    }

    public RoleName getAuthor() {
        return author;
    }

    public List<String> getAcceptanceCriteria() {
        return Collections.unmodifiableList(acceptanceCriteria);
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public Integer getEstimatedEffort() {
        return estimatedEffort;
    }

    // Convenience methods for backward compatibility with original design
    /**
     * Alias for getWorkflowState() for backward compatibility.
     */
    public WorkflowState getState() {
        return getWorkflowState();
    }

    /**
     * Alias for getPrioritizationState() for backward compatibility.
     */
    public PrioritizationState getPrioritization() {
        return getPrioritizationState();
    }

    // Business methods
    public void updateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Story title cannot be null or blank");
        }
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void transitionWorkflowState(WorkflowState newState, Role initiator) {
        if (!workflowState.canTransitionTo(newState, initiator)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s for role %s",
                    workflowState, newState, initiator)
            );
        }
        this.workflowState = newState;
        this.updatedAt = Instant.now();
    }

    public void updatePrioritizationState(PrioritizationState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("PrioritizationState cannot be null");
        }
        this.prioritizationState = newState;
        this.updatedAt = Instant.now();
    }

    /**
     * Alias for transitionWorkflowState() for backward compatibility.
     */
    public void transitionTo(WorkflowState newState, Role initiator) {
        transitionWorkflowState(newState, initiator);
    }

    /**
     * Alias for updatePrioritizationState() for backward compatibility.
     */
    public void changePrioritization(PrioritizationState newPrioritization) {
        updatePrioritizationState(newPrioritization);
    }

    public void assignTo(String assignee) {
        this.assignedTo = assignee;
        this.updatedAt = Instant.now();
    }

    // Additional setters for fields
    public void setTitle(String title) {
        updateTitle(title);
    }

    public void setSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("Story summary cannot be null or blank");
        }
        if (summary.length() > 500) {
            throw new IllegalArgumentException("Story summary cannot exceed 500 characters");
        }
        this.summary = summary;
        this.updatedAt = Instant.now();
    }

    public void setDescription(String description) {
        updateDescription(description);
    }

    public void setAuthor(RoleName author) {
        this.author = author;
        this.updatedAt = Instant.now();
    }

    public void setEstimatedEffort(Integer estimatedEffort) {
        this.estimatedEffort = estimatedEffort;
        this.updatedAt = Instant.now();
    }

    public void addAcceptanceCriterion(String criterion) {
        if (criterion == null || criterion.isBlank()) {
            throw new IllegalArgumentException("Acceptance criterion cannot be null or blank");
        }
        this.acceptanceCriteria.add(criterion);
        this.updatedAt = Instant.now();
    }

    public void removeAcceptanceCriterion(String criterion) {
        this.acceptanceCriteria.remove(criterion);
        this.updatedAt = Instant.now();
    }

    public void addTag(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be null or blank");
        }
        if (!tags.contains(tag)) {
            this.tags.add(tag);
            this.updatedAt = Instant.now();
        }
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
        this.updatedAt = Instant.now();
    }

    public void removeTask(Task task) {
        this.tasks.remove(task);
        this.updatedAt = Instant.now();
    }

    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        this.tasks.add(task);
        this.updatedAt = Instant.now();
    }

    public void addArtifact(Artifact artifact) {
        if (artifact == null) {
            throw new IllegalArgumentException("Artifact cannot be null");
        }
        this.artifacts.add(artifact);
        this.updatedAt = Instant.now();
    }

    public void addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        this.comments.add(comment);
        this.updatedAt = Instant.now();
    }

    public boolean isCompleted() {
        return workflowState == WorkflowState.DONE;
    }

    public boolean isInProgress() {
        return workflowState == WorkflowState.IN_PROGRESS;
    }

    public boolean isPrioritized() {
        return prioritizationState.isPrioritized();
    }

    public long countCompletedTasks() {
        return tasks.stream()
            .filter(Task::isCompleted)
            .count();
    }

    public double getTaskCompletionPercentage() {
        if (tasks.isEmpty()) {
            return 0.0;
        }
        return (double) countCompletedTasks() / tasks.size() * 100.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Story story = (Story) o;
        return Objects.equals(id, story.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Story{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", workflowState=" + workflowState +
            ", prioritizationState=" + prioritizationState +
            ", tasks=" + tasks.size() +
            ", artifacts=" + artifacts.size() +
            '}';
    }
}
