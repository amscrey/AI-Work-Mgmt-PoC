package com.aiworkflow.workmanagement.orchestration.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrchestrationExecutionExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        String message = "Node execution failed";
        OrchestrationExecutionException exception = new OrchestrationExecutionException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        String message = "LLM API call failed";
        Throwable cause = new RuntimeException("Rate limit exceeded");
        OrchestrationExecutionException exception = new OrchestrationExecutionException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldExtendOrchestrationException() {
        OrchestrationExecutionException exception = new OrchestrationExecutionException("test");
        assertThat(exception).isInstanceOf(OrchestrationException.class);
    }

    @Test
    void shouldBeRuntimeException() {
        OrchestrationExecutionException exception = new OrchestrationExecutionException("test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
