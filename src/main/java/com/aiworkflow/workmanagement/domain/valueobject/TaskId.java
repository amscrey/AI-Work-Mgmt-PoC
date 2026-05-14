package com.aiworkflow.workmanagement.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a Task identifier.
 * Format: TASK-{number}
 * Example: TASK-001
 */
public final class TaskId implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern PATTERN = Pattern.compile("^TASK-\\d{1,}$");

    private final String value;

    public TaskId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TaskId cannot be null or blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "TaskId must match pattern TASK-{number}, got: " + value
            );
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Extracts the numeric part from the task ID.
     * Example: TASK-001 returns 1
     */
    public int getNumber() {
        return Integer.parseInt(value.substring(5)); // Skip "TASK-"
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskId taskId = (TaskId) o;
        return Objects.equals(value, taskId.value);
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
