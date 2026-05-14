package com.aiworkflow.workmanagement.orchestration.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrchestrationExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        String message = "Orchestration failed";
        OrchestrationException exception = new OrchestrationException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        String message = "Orchestration failed";
        Throwable cause = new RuntimeException("Root cause");
        OrchestrationException exception = new OrchestrationException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        OrchestrationException exception = new OrchestrationException(cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldBeRuntimeException() {
        OrchestrationException exception = new OrchestrationException("test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldPreserveCauseStackTrace() {
        Exception rootCause = new Exception("Root cause");
        OrchestrationException exception = new OrchestrationException("Wrapped", rootCause);

        assertThat(exception.getCause()).isEqualTo(rootCause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Root cause");
    }
}
