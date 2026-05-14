package com.aiworkflow.workmanagement.orchestration.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidOrchestrationRequestExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        String message = "Invalid request: missing storyId";
        InvalidOrchestrationRequestException exception = new InvalidOrchestrationRequestException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        String message = "Invalid request";
        Throwable cause = new IllegalArgumentException("Validation failed");
        InvalidOrchestrationRequestException exception = new InvalidOrchestrationRequestException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldExtendOrchestrationException() {
        InvalidOrchestrationRequestException exception = new InvalidOrchestrationRequestException("test");
        assertThat(exception).isInstanceOf(OrchestrationException.class);
    }

    @Test
    void shouldBeRuntimeException() {
        InvalidOrchestrationRequestException exception = new InvalidOrchestrationRequestException("test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
