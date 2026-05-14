package com.aiworkflow.workmanagement.orchestration.exception;

/**
 * Exception thrown when assembling orchestration context fails.
 * <p>
 * This exception indicates that the ContextAssemblyService encountered an error while
 * assembling the execution context, such as:
 * - Story not found in repository
 * - Failed to load reference files
 * - Context size exceeds limits
 * - PROJECT-PLANNING directory creation failure
 * - Reference file read errors
 * <p>
 * This exception typically wraps underlying I/O or repository exceptions.
 */
public class OrchestrationContextException extends OrchestrationException {

    /**
     * Constructs a new orchestration context exception with the specified detail message.
     *
     * @param message the detail message explaining the context assembly failure
     */
    public OrchestrationContextException(String message) {
        super(message);
    }

    /**
     * Constructs a new orchestration context exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the context assembly failure
     * @param cause the underlying cause (e.g., IOException, RepositoryException)
     */
    public OrchestrationContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
