package com.aiworkflow.workmanagement.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StoryId Value Object Tests")
class StoryIdTest {

    @Test
    @DisplayName("Should create StoryId with valid pattern")
    void shouldCreateStoryIdWithValidPattern() {
        StoryId storyId = new StoryId("STORY-123");

        assertThat(storyId.getValue()).isEqualTo("STORY-123");
    }

    @Test
    @DisplayName("Should extract number from StoryId")
    void shouldExtractNumberFromStoryId() {
        StoryId storyId = new StoryId("STORY-456");

        assertThat(storyId.getNumber()).isEqualTo(456);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STORY-1", "STORY-99", "STORY-1234567890"})
    @DisplayName("Should accept valid StoryId patterns")
    void shouldAcceptValidStoryIdPatterns(String value) {
        StoryId storyId = new StoryId(value);

        assertThat(storyId.getValue()).isEqualTo(value);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("Should reject null or blank values")
    void shouldRejectNullOrBlankValues(String value) {
        assertThatThrownBy(() -> new StoryId(value))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("StoryId cannot be null or blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "STORY-",
        "STORY-ABC",
        "Story-123",
        "story-123",
        "STORY123",
        "TASK-123",
        "STORY--123",
        "STORY-12-3",
        "STORY-12 3"
    })
    @DisplayName("Should reject invalid patterns")
    void shouldRejectInvalidPatterns(String value) {
        assertThatThrownBy(() -> new StoryId(value))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("StoryId must match pattern STORY-{number}");
    }

    @Test
    @DisplayName("Should have proper equality based on value")
    void shouldHaveProperEqualityBasedOnValue() {
        StoryId storyId1 = new StoryId("STORY-123");
        StoryId storyId2 = new StoryId("STORY-123");
        StoryId storyId3 = new StoryId("STORY-456");

        assertThat(storyId1).isEqualTo(storyId2);
        assertThat(storyId1).isNotEqualTo(storyId3);
        assertThat(storyId1.hashCode()).isEqualTo(storyId2.hashCode());
    }

    @Test
    @DisplayName("Should have proper hashCode")
    void shouldHaveProperHashCode() {
        StoryId storyId1 = new StoryId("STORY-789");
        StoryId storyId2 = new StoryId("STORY-789");

        assertThat(storyId1.hashCode()).isEqualTo(storyId2.hashCode());
    }

    @Test
    @DisplayName("Should return value in toString")
    void shouldReturnValueInToString() {
        StoryId storyId = new StoryId("STORY-999");

        assertThat(storyId.toString()).isEqualTo("STORY-999");
    }

    @Test
    @DisplayName("Should handle single digit numbers")
    void shouldHandleSingleDigitNumbers() {
        StoryId storyId = new StoryId("STORY-1");

        assertThat(storyId.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle large numbers")
    void shouldHandleLargeNumbers() {
        StoryId storyId = new StoryId("STORY-999999");

        assertThat(storyId.getNumber()).isEqualTo(999999);
    }

    @Test
    @DisplayName("Should accept STORY-0")
    void shouldAcceptStoryWithZero() {
        StoryId storyId = new StoryId("STORY-0");

        assertThat(storyId.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        StoryId storyId = new StoryId("STORY-123");

        assertThat(storyId).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
        StoryId storyId = new StoryId("STORY-123");

        assertThat(storyId).isNotEqualTo("STORY-123");
    }

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        StoryId storyId = new StoryId("STORY-123");

        assertThat(storyId).isEqualTo(storyId);
    }

    @Test
    @DisplayName("Should reject pattern with no digits after hyphen")
    void shouldRejectPatternWithNoDigitsAfterHyphen() {
        assertThatThrownBy(() -> new StoryId("STORY-"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("StoryId must match pattern STORY-{number}");
    }

    @Test
    @DisplayName("Should include invalid value in error message")
    void shouldIncludeInvalidValueInErrorMessage() {
        assertThatThrownBy(() -> new StoryId("INVALID-123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("got: INVALID-123");
    }
}
