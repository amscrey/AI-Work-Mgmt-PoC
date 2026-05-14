package com.aiworkflow.workmanagement.orchestration.domain;

/**
 * Represents the outcome of an orchestration execution.
 * Used to indicate whether the execution was successful, failed, or partially completed.
 */
public enum ExecutionOutcome {
    /**
     * Execution completed successfully with all expected artifacts produced.
     */
    SUCCESS,

    /**
     * Execution failed with no artifacts produced.
     */
    FAILURE,

    /**
     * Execution partially completed with some artifacts produced but not all expected.
     * This can occur when some steps succeed but others fail.
     */
    PARTIAL;

    /**
     * Checks if the outcome represents a successful execution.
     *
     * @return true if the outcome is SUCCESS
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * Checks if the outcome represents a failed execution.
     *
     * @return true if the outcome is FAILURE
     */
    public boolean isFailure() {
        return this == FAILURE;
    }

    /**
     * Checks if the outcome represents a partial execution.
     *
     * @return true if the outcome is PARTIAL
     */
    public boolean isPartial() {
        return this == PARTIAL;
    }

    /**
     * Checks if any artifacts were produced (success or partial).
     *
     * @return true if the outcome is SUCCESS or PARTIAL
     */
    public boolean hasArtifacts() {
        return this == SUCCESS || this == PARTIAL;
    }
}
