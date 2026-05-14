package com.aiworkflow.workmanagement.orchestration.exception;

/**
 * Exception thrown when an orchestration request is invalid.
 * <p>
 * This exception indicates that the OrchestrationRequest contains invalid parameters,
 * such as:
 * - Null or blank work intent
 * - Invalid story ID
 * - Missing required fields
 * - Malformed request data
 * <p>
 * This is a validation exception that should be caught and handled by the caller
 * to provide appropriate feedback to the user.
 */
public class InvalidOrchestrationRequestException extends OrchestrationException {

    /**
     * Constructs a new invalid orchestration request exception with the specified detail message.
     *
     * @param message the detail message explaining what is invalid
     */
    public InvalidOrchestrationRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a new invalid orchestration request exception with the specified detail message and cause.
     *
     * @param message the detail message explaining what is invalid
     * @param cause the underlying cause of the validation failure
     */
    public InvalidOrchestrationRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
