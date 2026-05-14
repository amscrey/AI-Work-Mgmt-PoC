package com.aiworkflow.workmanagement.orchestration.exception;

/**
 * Exception thrown when orchestration execution fails.
 * <p>
 * This exception indicates that the StateGraph execution encountered an error, such as:
 * - Node execution failure
 * - LLM API errors (timeout, rate limit, authentication)
 * - Template rendering errors
 * - State transition errors
 * - Unexpected graph execution errors
 * <p>
 * This exception wraps errors that occur during the actual orchestration process,
 * after context has been assembled and before artifacts are written.
 */
public class OrchestrationExecutionException extends OrchestrationException {

    /**
     * Constructs a new orchestration execution exception with the specified detail message.
     *
     * @param message the detail message explaining the execution failure
     */
    public OrchestrationExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new orchestration execution exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the execution failure
     * @param cause the underlying cause (e.g., LLM API exception, template error)
     */
    public OrchestrationExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
