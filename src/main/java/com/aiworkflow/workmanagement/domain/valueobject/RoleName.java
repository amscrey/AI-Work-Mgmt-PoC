package com.aiworkflow.workmanagement.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a Role name.
 * Format: lowercase letters with optional hyphens
 * Examples: orchestrator, java-spring-architect, file-contract-validator
 */
public final class RoleName implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern PATTERN = Pattern.compile("^[a-z]+(-[a-z]+)*$");

    private final String value;

    public RoleName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("RoleName cannot be null or blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "RoleName must be lowercase letters with optional hyphens, got: " + value
            );
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleName roleName = (RoleName) o;
        return Objects.equals(value, roleName.value);
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
