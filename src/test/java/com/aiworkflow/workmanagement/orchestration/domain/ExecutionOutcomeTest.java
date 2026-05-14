package com.aiworkflow.workmanagement.orchestration.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExecutionOutcome Enum Tests")
class ExecutionOutcomeTest {

    @Test
    @DisplayName("Should identify SUCCESS outcome")
    void shouldIdentifySuccessOutcome() {
        ExecutionOutcome outcome = ExecutionOutcome.SUCCESS;

        assertThat(outcome.isSuccess()).isTrue();
        assertThat(outcome.isFailure()).isFalse();
        assertThat(outcome.isPartial()).isFalse();
        assertThat(outcome.hasArtifacts()).isTrue();
    }

    @Test
    @DisplayName("Should identify FAILURE outcome")
    void shouldIdentifyFailureOutcome() {
        ExecutionOutcome outcome = ExecutionOutcome.FAILURE;

        assertThat(outcome.isSuccess()).isFalse();
        assertThat(outcome.isFailure()).isTrue();
        assertThat(outcome.isPartial()).isFalse();
        assertThat(outcome.hasArtifacts()).isFalse();
    }

    @Test
    @DisplayName("Should identify PARTIAL outcome")
    void shouldIdentifyPartialOutcome() {
        ExecutionOutcome outcome = ExecutionOutcome.PARTIAL;

        assertThat(outcome.isSuccess()).isFalse();
        assertThat(outcome.isFailure()).isFalse();
        assertThat(outcome.isPartial()).isTrue();
        assertThat(outcome.hasArtifacts()).isTrue();
    }

    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
        ExecutionOutcome[] values = ExecutionOutcome.values();

        assertThat(values).hasSize(3);
        assertThat(values).contains(
            ExecutionOutcome.SUCCESS,
            ExecutionOutcome.FAILURE,
            ExecutionOutcome.PARTIAL
        );
    }

    @Test
    @DisplayName("Should support valueOf")
    void shouldSupportValueOf() {
        assertThat(ExecutionOutcome.valueOf("SUCCESS")).isEqualTo(ExecutionOutcome.SUCCESS);
        assertThat(ExecutionOutcome.valueOf("FAILURE")).isEqualTo(ExecutionOutcome.FAILURE);
        assertThat(ExecutionOutcome.valueOf("PARTIAL")).isEqualTo(ExecutionOutcome.PARTIAL);
    }
}
