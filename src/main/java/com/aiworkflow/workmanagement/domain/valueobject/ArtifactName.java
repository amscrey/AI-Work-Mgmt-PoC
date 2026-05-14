package com.aiworkflow.workmanagement.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an Artifact name.
 * Format: {descriptive-name}__{role}__{timestamp}.{extension}
 * Example: mockup__designer__2026-03-30T141230Z.png
 */
public final class ArtifactName implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern PATTERN = Pattern.compile(
        "^[a-z0-9]+(-[a-z0-9]+)*__[a-z]+(-[a-z]+)*__\\d{4}-\\d{2}-\\d{2}T\\d{6}Z\\..+$"
    );

    private final String value;

    public ArtifactName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ArtifactName cannot be null or blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "ArtifactName must match pattern {name}__{role}__{timestamp}.{ext}, got: " + value
            );
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Extracts the descriptive name part.
     * Example: mockup__designer__2026-03-30T141230Z.png returns "mockup"
     */
    public String getDescriptiveName() {
        return value.split("__")[0];
    }

    /**
     * Extracts the role part.
     * Example: mockup__designer__2026-03-30T141230Z.png returns "designer"
     */
    public String getRole() {
        return value.split("__")[1];
    }

    /**
     * Extracts the timestamp part.
     * Example: mockup__designer__2026-03-30T141230Z.png returns "2026-03-30T141230Z"
     */
    public String getTimestamp() {
        String timestampPart = value.split("__")[2];
        return timestampPart.substring(0, timestampPart.lastIndexOf('.'));
    }

    /**
     * Extracts the file extension.
     * Example: mockup__designer__2026-03-30T141230Z.png returns "png"
     */
    public String getExtension() {
        return value.substring(value.lastIndexOf('.') + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactName that = (ArtifactName) o;
        return Objects.equals(value, that.value);
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
