package com.aiworkflow.workmanagement.orchestration.exception;

/**
 * Exception thrown when writing orchestration artifacts to the filesystem fails.
 * <p>
 * This exception indicates that the ArtifactOutputService encountered an error while
 * persisting artifacts, such as:
 * - File I/O errors
 * - Permission denied
 * - Disk full
 * - Story not found (cannot add artifact)
 * - Invalid artifact naming
 * <p>
 * This exception wraps underlying filesystem and repository exceptions that occur
 * during the artifact writing phase.
 */
public class ArtifactWriteException extends OrchestrationException {

    /**
     * Constructs a new artifact write exception with the specified detail message.
     *
     * @param message the detail message explaining the write failure
     */
    public ArtifactWriteException(String message) {
        super(message);
    }

    /**
     * Constructs a new artifact write exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the write failure
     * @param cause the underlying cause (e.g., IOException, RepositoryException)
     */
    public ArtifactWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
