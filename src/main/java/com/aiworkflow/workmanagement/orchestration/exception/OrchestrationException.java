package com.aiworkflow.workmanagement.orchestration.exception;

/**
 * Base exception for all orchestration-related errors.
 * <p>
 * This is the root of the orchestration exception hierarchy. All orchestration-specific
 * exceptions should extend this class to allow for unified exception handling.
 * <p>
 * Subclasses include:
 * - InvalidOrchestrationRequestException: Invalid request parameters
 * - OrchestrationContextException: Context assembly errors
 * - OrchestrationExecutionException: Graph execution errors
 * - ArtifactWriteException: Artifact persistence errors
 */
public class OrchestrationException extends RuntimeException {

    /**
     * Constructs a new orchestration exception with the specified detail message.
     *
     * @param message the detail message
     */
    public OrchestrationException(String message) {
        super(message);
    }

    /**
     * Constructs a new orchestration exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public OrchestrationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new orchestration exception with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public OrchestrationException(Throwable cause) {
        super(cause);
    }
}
