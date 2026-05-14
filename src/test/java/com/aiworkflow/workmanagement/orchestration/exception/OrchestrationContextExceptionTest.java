package com.aiworkflow.workmanagement.orchestration.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class OrchestrationContextExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        String message = "Story not found: STORY-123";
        OrchestrationContextException exception = new OrchestrationContextException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        String message = "Failed to read reference file";
        Throwable cause = new IOException("File not found");
        OrchestrationContextException exception = new OrchestrationContextException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause()).isInstanceOf(IOException.class);
    }

    @Test
    void shouldExtendOrchestrationException() {
        OrchestrationContextException exception = new OrchestrationContextException("test");
        assertThat(exception).isInstanceOf(OrchestrationException.class);
    }

    @Test
    void shouldBeRuntimeException() {
        OrchestrationContextException exception = new OrchestrationContextException("test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
