package com.aiworkflow.workmanagement.orchestration.domain;

import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object representing the execution context assembled for orchestration.
 * <p>
 * This is a domain object (decoupled from langgraph4j). OrchestrationService
 * handles conversion to OrchestrationState when executing the StateGraph.
 * <p>
 * Contains all the information needed for an agent to perform work:
 * - Story metadata (title, description, acceptance criteria)
 * - Associated tasks
 * - Reference files (from workspace/reference/ directory)
 * - Work intent (what to do)
 */
public class OrchestrationContext {
    private final Story story;
    private final String workIntent;
    private final List<Task> tasks;
    private final List<ReferenceFile> referenceFiles;

    /**
     * Private constructor - use builder() instead.
     */
    private OrchestrationContext(Story story, String workIntent, List<Task> tasks, List<ReferenceFile> referenceFiles) {
        if (story == null) {
            throw new IllegalArgumentException("Story cannot be null");
        }
        if (workIntent == null || workIntent.isBlank()) {
            throw new IllegalArgumentException("Work intent cannot be null or blank");
        }

        this.story = story;
        this.workIntent = workIntent;
        this.tasks = new ArrayList<>(tasks != null ? tasks : Collections.emptyList());
        this.referenceFiles = new ArrayList<>(referenceFiles != null ? referenceFiles : Collections.emptyList());
    }

    /**
     * Creates a builder for OrchestrationContext.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    public Story getStory() {
        return story;
    }

    public String getWorkIntent() {
        return workIntent;
    }

    /**
     * Returns an unmodifiable list of tasks.
     *
     * @return the tasks associated with this context
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Returns an unmodifiable list of reference files.
     *
     * @return the reference files for this context
     */
    public List<ReferenceFile> getReferenceFiles() {
        return Collections.unmodifiableList(referenceFiles);
    }

    /**
     * Checks if this context has any tasks.
     *
     * @return true if tasks are present
     */
    public boolean hasTasks() {
        return !tasks.isEmpty();
    }

    /**
     * Checks if this context has any reference files.
     *
     * @return true if reference files are present
     */
    public boolean hasReferenceFiles() {
        return !referenceFiles.isEmpty();
    }

    /**
     * Returns the total number of characters in the context (story + tasks + references).
     * Useful for estimating token count and checking against context limits.
     *
     * @return approximate character count
     */
    public long estimateContextSize() {
        long size = 0;

        // Story metadata
        if (story.getTitle() != null) size += story.getTitle().length();
        if (story.getDescription() != null) size += story.getDescription().length();
        if (story.getSummary() != null) size += story.getSummary().length();
        size += story.getAcceptanceCriteria().stream()
            .mapToLong(String::length)
            .sum();

        // Tasks
        size += tasks.stream()
            .mapToLong(task -> {
                long taskSize = 0;
                if (task.getTitle() != null) taskSize += task.getTitle().length();
                if (task.getDescription() != null) taskSize += task.getDescription().length();
                return taskSize;
            })
            .sum();

        // Reference files
        size += referenceFiles.stream()
            .mapToLong(ReferenceFile::getFileSize)
            .sum();

        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrchestrationContext that = (OrchestrationContext) o;
        return Objects.equals(story, that.story) &&
               Objects.equals(workIntent, that.workIntent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(story, workIntent);
    }

    @Override
    public String toString() {
        return "OrchestrationContext{" +
            "story=" + story.getId() +
            ", workIntent='" + workIntent + '\'' +
            ", tasks=" + tasks.size() +
            ", referenceFiles=" + referenceFiles.size() +
            ", estimatedSize=" + estimateContextSize() + " chars" +
            '}';
    }

    /**
     * Builder class for OrchestrationContext.
     */
    public static class Builder {
        private Story story;
        private String workIntent;
        private List<Task> tasks;
        private List<ReferenceFile> referenceFiles;

        private Builder() {}

        /**
         * Sets the story (required).
         *
         * @param story the story for this context
         * @return this builder
         */
        public Builder story(Story story) {
            this.story = story;
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
         * Sets the tasks (optional).
         *
         * @param tasks the tasks associated with the story
         * @return this builder
         */
        public Builder tasks(List<Task> tasks) {
            this.tasks = tasks;
            return this;
        }

        /**
         * Sets the reference files (optional).
         *
         * @param referenceFiles the reference files to include
         * @return this builder
         */
        public Builder referenceFiles(List<ReferenceFile> referenceFiles) {
            this.referenceFiles = referenceFiles;
            return this;
        }

        /**
         * Builds the OrchestrationContext.
         *
         * @return a new OrchestrationContext instance
         * @throws IllegalArgumentException if required fields are missing
         */
        public OrchestrationContext build() {
            return new OrchestrationContext(story, workIntent, tasks, referenceFiles);
        }
    }
}
