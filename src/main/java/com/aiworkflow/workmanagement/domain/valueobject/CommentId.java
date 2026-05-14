package com.aiworkflow.workmanagement.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a Comment identifier.
 * Format: comment__{role}__{timestamp}.md
 * Example: comment__coder__2026-03-30T141530Z.md
 */
public final class CommentId implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern PATTERN = Pattern.compile(
        "^comment__[a-z]+(-[a-z]+)*__\\d{4}-\\d{2}-\\d{2}T\\d{6}Z\\.md$"
    );

    private final String value;

    public CommentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CommentId cannot be null or blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "CommentId must match pattern comment__{role}__{timestamp}.md, got: " + value
            );
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Extracts the role part.
     * Example: comment__coder__2026-03-30T141530Z.md returns "coder"
     */
    public String getRole() {
        return value.split("__")[1];
    }

    /**
     * Extracts the timestamp part.
     * Example: comment__coder__2026-03-30T141530Z.md returns "2026-03-30T141530Z"
     */
    public String getTimestamp() {
        String timestampPart = value.split("__")[2];
        return timestampPart.substring(0, timestampPart.lastIndexOf('.'));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentId commentId = (CommentId) o;
        return Objects.equals(value, commentId.value);
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
