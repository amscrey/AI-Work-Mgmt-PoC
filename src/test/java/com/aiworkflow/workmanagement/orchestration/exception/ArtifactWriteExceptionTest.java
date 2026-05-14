package com.aiworkflow.workmanagement.orchestration.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactWriteExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        String message = "Failed to write artifact to filesystem";
        ArtifactWriteException exception = new ArtifactWriteException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        String message = "Failed to write artifact";
        Throwable cause = new IOException("Permission denied");
        ArtifactWriteException exception = new ArtifactWriteException(message, cause);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause()).isInstanceOf(IOException.class);
    }

    @Test
    void shouldExtendOrchestrationException() {
        ArtifactWriteException exception = new ArtifactWriteException("test");
        assertThat(exception).isInstanceOf(OrchestrationException.class);
    }

    @Test
    void shouldBeRuntimeException() {
        ArtifactWriteException exception = new ArtifactWriteException("test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
