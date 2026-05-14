package com.aiworkflow.workmanagement.orchestration.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrchestrationResult Tests")
class OrchestrationResultTest {

    @Test
    @DisplayName("Should create successful result")
    void shouldCreateSuccessfulResult() {
        ArtifactReference artifact1 = new ArtifactReference("research", "content1", "researcher");
        ArtifactReference artifact2 = new ArtifactReference("analysis", "content2", "logician");
        List<ArtifactReference> artifacts = List.of(artifact1, artifact2);

        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            artifacts,
            "Research and analysis completed successfully"
        );

        assertThat(result.getExecutionId()).isEqualTo("exec-123");
        assertThat(result.getOutcome()).isEqualTo(ExecutionOutcome.SUCCESS);
        assertThat(result.getArtifacts()).hasSize(2);
        assertThat(result.getExecutionSummary()).contains("successfully");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.hasArtifacts()).isTrue();
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("Should create failed result")
    void shouldCreateFailedResult() {
        OrchestrationResult result = OrchestrationResult.failure(
            "exec-456",
            "LLM API timeout after 30 seconds"
        );

        assertThat(result.getExecutionId()).isEqualTo("exec-456");
        assertThat(result.getOutcome()).isEqualTo(ExecutionOutcome.FAILURE);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.hasArtifacts()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).contains("LLM API timeout after 30 seconds");
    }

    @Test
    @DisplayName("Should create partial result")
    void shouldCreatePartialResult() {
        ArtifactReference artifact = new ArtifactReference("partial", "content", "researcher");
        List<String> errors = List.of("Node 2 failed", "Node 3 timeout");

        OrchestrationResult result = OrchestrationResult.partial(
            "exec-789",
            List.of(artifact),
            errors
        );

        assertThat(result.getExecutionId()).isEqualTo("exec-789");
        assertThat(result.getOutcome()).isEqualTo(ExecutionOutcome.PARTIAL);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.hasArtifacts()).isTrue();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getArtifacts()).hasSize(1);
        assertThat(result.getErrors()).hasSize(2);
    }

    @Test
    @DisplayName("Should create result using builder")
    void shouldCreateResultUsingBuilder() {
        ArtifactReference artifact = new ArtifactReference("test", "content", "tester");

        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-111")
            .outcome(ExecutionOutcome.SUCCESS)
            .addArtifact(artifact)
            .executionSummary("Test execution")
            .build();

        assertThat(result.getExecutionId()).isEqualTo("exec-111");
        assertThat(result.getOutcome()).isEqualTo(ExecutionOutcome.SUCCESS);
        assertThat(result.getArtifacts()).hasSize(1);
    }

    @Test
    @DisplayName("Should reject null execution ID")
    void shouldRejectNullExecutionId() {
        assertThatThrownBy(() -> OrchestrationResult.builder()
            .executionId(null)
            .outcome(ExecutionOutcome.SUCCESS)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ExecutionId cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank execution ID")
    void shouldRejectBlankExecutionId() {
        assertThatThrownBy(() -> OrchestrationResult.builder()
            .executionId("   ")
            .outcome(ExecutionOutcome.SUCCESS)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ExecutionId cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject null outcome")
    void shouldRejectNullOutcome() {
        assertThatThrownBy(() -> OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(null)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ExecutionOutcome cannot be null");
    }

    @Test
    @DisplayName("Should handle null artifacts list")
    void shouldHandleNullArtifactsList() {
        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(ExecutionOutcome.SUCCESS)
            .artifacts(null)
            .build();

        assertThat(result.getArtifacts()).isEmpty();
        assertThat(result.hasArtifacts()).isFalse();
    }

    @Test
    @DisplayName("Should handle null errors list")
    void shouldHandleNullErrorsList() {
        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(ExecutionOutcome.FAILURE)
            .errors(null)
            .build();

        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should add artifacts using builder")
    void shouldAddArtifactsUsingBuilder() {
        ArtifactReference artifact1 = new ArtifactReference("art1", "content1", "researcher");
        ArtifactReference artifact2 = new ArtifactReference("art2", "content2", "logician");

        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(ExecutionOutcome.SUCCESS)
            .addArtifact(artifact1)
            .addArtifact(artifact2)
            .build();

        assertThat(result.getArtifacts()).hasSize(2);
    }

    @Test
    @DisplayName("Should add errors using builder")
    void shouldAddErrorsUsingBuilder() {
        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(ExecutionOutcome.FAILURE)
            .addError("Error 1")
            .addError("Error 2")
            .build();

        assertThat(result.getErrors()).hasSize(2);
    }

    @Test
    @DisplayName("Should ignore null artifacts when adding")
    void shouldIgnoreNullArtifactsWhenAdding() {
        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(ExecutionOutcome.SUCCESS)
            .addArtifact(null)
            .build();

        assertThat(result.getArtifacts()).isEmpty();
    }

    @Test
    @DisplayName("Should ignore null or blank errors when adding")
    void shouldIgnoreNullOrBlankErrorsWhenAdding() {
        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(ExecutionOutcome.FAILURE)
            .addError(null)
            .addError("   ")
            .addError("Valid error")
            .build();

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors()).contains("Valid error");
    }

    @Test
    @DisplayName("Should return unmodifiable artifacts list")
    void shouldReturnUnmodifiableArtifactsList() {
        ArtifactReference artifact = new ArtifactReference("art", "content", "researcher");

        OrchestrationResult result = OrchestrationResult.success(
            "exec-1",
            List.of(artifact),
            "summary"
        );

        assertThatThrownBy(() -> result.getArtifacts().add(artifact))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should return unmodifiable errors list")
    void shouldReturnUnmodifiableErrorsList() {
        OrchestrationResult result = OrchestrationResult.failure("exec-1", "error");

        assertThatThrownBy(() -> result.getErrors().add("another error"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should calculate duration correctly")
    void shouldCalculateDurationCorrectly() {
        Instant start = Instant.now();
        Instant end = start.plusSeconds(5);

        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(ExecutionOutcome.SUCCESS)
            .startedAt(start)
            .completedAt(end)
            .build();

        Duration duration = result.getDuration();
        assertThat(duration.getSeconds()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should use current time for null timestamps")
    void shouldUseCurrentTimeForNullTimestamps() {
        Instant before = Instant.now();

        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(ExecutionOutcome.SUCCESS)
            .build();

        Instant after = Instant.now();

        assertThat(result.getStartedAt()).isBetween(before, after);
        assertThat(result.getCompletedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        OrchestrationResult result = OrchestrationResult.success(
            "exec-123",
            List.of(new ArtifactReference("art", "content", "researcher")),
            "summary"
        );

        String toString = result.toString();

        assertThat(toString).contains("exec-123");
        assertThat(toString).contains("SUCCESS");
        assertThat(toString).contains("artifacts=1");
        assertThat(toString).contains("ms");
    }

    @Test
    @DisplayName("Should implement equals and hashCode based on execution ID")
    void shouldImplementEqualsAndHashCodeBasedOnExecutionId() {
        OrchestrationResult result1 = OrchestrationResult.success("exec-1", List.of(), "summary");
        OrchestrationResult result2 = OrchestrationResult.success("exec-1", List.of(), "different");
        OrchestrationResult result3 = OrchestrationResult.success("exec-2", List.of(), "summary");

        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        assertThat(result1).isNotEqualTo(result3);
    }

    @Test
    @DisplayName("Should handle empty artifacts and errors")
    void shouldHandleEmptyArtifactsAndErrors() {
        OrchestrationResult result = OrchestrationResult.builder()
            .executionId("exec-1")
            .outcome(ExecutionOutcome.SUCCESS)
            .executionSummary("No artifacts produced")
            .build();

        assertThat(result.hasArtifacts()).isFalse();
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getArtifacts()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
    }
}
