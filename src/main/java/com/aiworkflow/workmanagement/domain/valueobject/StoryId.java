package com.aiworkflow.workmanagement.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a Story identifier.
 * Format: STORY-{number}
 * Example: STORY-123
 */
public final class StoryId implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern PATTERN = Pattern.compile("^STORY-\\d{1,}$");
    private static final String PROJECT_PLANNING = "PROJECT-PLANNING";  // Special reserved ID

    private final String value;

    public StoryId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("StoryId cannot be null or blank");
        }
        // Allow PROJECT-PLANNING as a special case
        if (!PROJECT_PLANNING.equals(value) && !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "StoryId must match pattern STORY-{number} or be PROJECT-PLANNING, got: " + value
            );
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Extracts the numeric part from the story ID.
     * Example: STORY-123 returns 123
     * @return the numeric part, or 0 for PROJECT-PLANNING
     */
    public int getNumber() {
        if (PROJECT_PLANNING.equals(value)) {
            return 0;  // PROJECT-PLANNING has no number
        }
        return Integer.parseInt(value.substring(6)); // Skip "STORY-"
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoryId storyId = (StoryId) o;
        return Objects.equals(value, storyId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
