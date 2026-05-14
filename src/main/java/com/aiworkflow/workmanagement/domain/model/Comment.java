package com.aiworkflow.workmanagement.domain.model;

import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a Comment in the workflow system.
 * Comments provide communication and feedback on Stories and Tasks.
 */
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CommentId id;
    private final StoryId storyId;
    private final String author;
    private String content;
    private CommentType type;
    private final Instant createdAt;
    private Instant updatedAt;
    private boolean resolved;
    private String resolvedBy;
    private Instant resolvedAt;

    public Comment(CommentId id, StoryId storyId, String author, String content, CommentType type) {
        if (id == null) {
            throw new IllegalArgumentException("CommentId cannot be null");
        }
        if (storyId == null) {
            throw new IllegalArgumentException("StoryId cannot be null");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Author cannot be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("CommentType cannot be null");
        }

        this.id = id;
        this.storyId = storyId;
        this.author = author;
        this.content = content;
        this.type = type;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.resolved = false;
    }

    // Getters
    public CommentId getId() {
        return id;
    }

    public StoryId getStoryId() {
        return storyId;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public CommentType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    // Business methods
    public void updateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public void updateType(CommentType type) {
        if (type == null) {
            throw new IllegalArgumentException("CommentType cannot be null");
        }
        this.type = type;
        this.updatedAt = Instant.now();
    }

    public void resolve(String resolvedBy) {
        if (resolved) {
            throw new IllegalStateException("Comment is already resolved");
        }
        if (resolvedBy == null || resolvedBy.isBlank()) {
            throw new IllegalArgumentException("ResolvedBy cannot be null or blank");
        }

        this.resolved = true;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void unresolve() {
        if (!resolved) {
            throw new IllegalStateException("Comment is not resolved");
        }

        this.resolved = false;
        this.resolvedBy = null;
        this.resolvedAt = null;
        this.updatedAt = Instant.now();
    }

    public boolean requiresResponse() {
        return type.requiresResponse() && !resolved;
    }

    public boolean isSystemGenerated() {
        return type.isSystemGenerated();
    }

    public boolean isUserGenerated() {
        return type.isUserGenerated();
    }

    public String getRoleFromId() {
        return id.getRole();
    }

    public String getTimestampFromId() {
        return id.getTimestamp();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Comment{" +
            "id=" + id +
            ", storyId=" + storyId +
            ", author='" + author + '\'' +
            ", type=" + type +
            ", resolved=" + resolved +
            '}';
    }
}
