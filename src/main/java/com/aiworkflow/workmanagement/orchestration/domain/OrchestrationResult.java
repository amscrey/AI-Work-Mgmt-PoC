package com.aiworkflow.workmanagement.orchestration.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object representing the outcome of an orchestration execution.
 * <p>
 * This is a domain object (decoupled from langgraph4j). OrchestrationService
 * extracts artifacts from the final OrchestrationState and builds this result
 * before passing it to ArtifactOutputService.
 * <p>
 * Contains the execution outcome, artifacts produced, and any errors or metadata.
 */
public class OrchestrationResult {
    private final String executionId;
    private final ExecutionOutcome outcome;
    private final List<ArtifactReference> artifacts;
    private final String executionSummary;
    private final List<String> errors;
    private final Instant startedAt;
    private final Instant completedAt;

    /**
     * Private constructor - use builder() or factory methods instead.
     */
    private OrchestrationResult(
        String executionId,
        ExecutionOutcome outcome,
        List<ArtifactReference> artifacts,
        String executionSummary,
        List<String> errors,
        Instant startedAt,
        Instant completedAt
    ) {
        if (executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("ExecutionId cannot be null or blank");
        }
        if (outcome == null) {
            throw new IllegalArgumentException("ExecutionOutcome cannot be null");
        }

        this.executionId = executionId;
        this.outcome = outcome;
        this.artifacts = new ArrayList<>(artifacts != null ? artifacts : Collections.emptyList());
        this.executionSummary = executionSummary;
        this.errors = new ArrayList<>(errors != null ? errors : Collections.emptyList());
        this.startedAt = startedAt != null ? startedAt : Instant.now();
        this.completedAt = completedAt != null ? completedAt : Instant.now();
    }

    /**
     * Creates a builder for OrchestrationResult.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Factory method for a successful execution with artifacts.
     *
     * @param executionId unique ID for this execution
     * @param artifacts artifacts produced during execution
     * @param summary execution summary
     * @return a new OrchestrationResult with SUCCESS outcome
     */
    public static OrchestrationResult success(
        String executionId,
        List<ArtifactReference> artifacts,
        String summary
    ) {
        return builder()
            .executionId(executionId)
            .outcome(ExecutionOutcome.SUCCESS)
            .artifacts(artifacts)
            .executionSummary(summary)
            .build();
    }

    /**
     * Factory method for a failed execution.
     *
     * @param executionId unique ID for this execution
     * @param errorMessage error description
     * @return a new OrchestrationResult with FAILURE outcome
     */
    public static OrchestrationResult failure(String executionId, String errorMessage) {
        return builder()
            .executionId(executionId)
            .outcome(ExecutionOutcome.FAILURE)
            .executionSummary("Execution failed")
            .addError(errorMessage)
            .build();
    }

    /**
     * Factory method for a partial execution with some artifacts.
     *
     * @param executionId unique ID for this execution
     * @param artifacts artifacts that were successfully produced
     * @param errors list of errors encountered
     * @return a new OrchestrationResult with PARTIAL outcome
     */
    public static OrchestrationResult partial(
        String executionId,
        List<ArtifactReference> artifacts,
        List<String> errors
    ) {
        return builder()
            .executionId(executionId)
            .outcome(ExecutionOutcome.PARTIAL)
            .artifacts(artifacts)
            .errors(errors)
            .executionSummary("Execution partially completed with " + artifacts.size() + " artifacts")
            .build();
    }

    public String getExecutionId() {
        return executionId;
    }

    public ExecutionOutcome getOutcome() {
        return outcome;
    }

    /**
     * Returns an unmodifiable list of artifacts.
     *
     * @return the artifacts produced
     */
    public List<ArtifactReference> getArtifacts() {
        return Collections.unmodifiableList(artifacts);
    }

    public String getExecutionSummary() {
        return executionSummary;
    }

    /**
     * Returns an unmodifiable list of errors.
     *
     * @return the errors encountered
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * Returns the execution duration.
     *
     * @return duration between start and completion
     */
    public Duration getDuration() {
        return Duration.between(startedAt, completedAt);
    }

    /**
     * Checks if the execution was successful.
     *
     * @return true if outcome is SUCCESS
     */
    public boolean isSuccess() {
        return outcome == ExecutionOutcome.SUCCESS;
    }

    /**
     * Checks if the execution failed.
     *
     * @return true if outcome is FAILURE
     */
    public boolean isFailure() {
        return outcome == ExecutionOutcome.FAILURE;
    }

    /**
     * Checks if any artifacts were produced.
     *
     * @return true if artifacts list is not empty
     */
    public boolean hasArtifacts() {
        return !artifacts.isEmpty();
    }

    /**
     * Checks if any errors occurred.
     *
     * @return true if errors list is not empty
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrchestrationResult that = (OrchestrationResult) o;
        return Objects.equals(executionId, that.executionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId);
    }

    @Override
    public String toString() {
        return "OrchestrationResult{" +
            "executionId='" + executionId + '\'' +
            ", outcome=" + outcome +
            ", artifacts=" + artifacts.size() +
            ", errors=" + errors.size() +
            ", duration=" + getDuration().toMillis() + "ms" +
            '}';
    }

    /**
     * Builder class for OrchestrationResult.
     */
    public static class Builder {
        private String executionId;
        private ExecutionOutcome outcome;
        private List<ArtifactReference> artifacts;
        private String executionSummary;
        private List<String> errors;
        private Instant startedAt;
        private Instant completedAt;

        private Builder() {
            this.artifacts = new ArrayList<>();
            this.errors = new ArrayList<>();
        }

        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder outcome(ExecutionOutcome outcome) {
            this.outcome = outcome;
            return this;
        }

        public Builder artifacts(List<ArtifactReference> artifacts) {
            this.artifacts = new ArrayList<>(artifacts != null ? artifacts : Collections.emptyList());
            return this;
        }

        public Builder addArtifact(ArtifactReference artifact) {
            if (artifact != null) {
                this.artifacts.add(artifact);
            }
            return this;
        }

        public Builder executionSummary(String executionSummary) {
            this.executionSummary = executionSummary;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = new ArrayList<>(errors != null ? errors : Collections.emptyList());
            return this;
        }

        public Builder addError(String error) {
            if (error != null && !error.isBlank()) {
                this.errors.add(error);
            }
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public OrchestrationResult build() {
            return new OrchestrationResult(
                executionId,
                outcome,
                artifacts,
                executionSummary,
                errors,
                startedAt,
                completedAt
            );
        }
    }
}
