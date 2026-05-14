package com.aiworkflow.workmanagement.api;

/**
 * Standard error payload for API responses.
 */
public class ErrorResponse {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String correlationId;

    public ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}

