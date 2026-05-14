package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.model.CommentType;

import java.time.Instant;

public class CommentResponse {
    private final String commentId;
    private final String storyId;
    private final String author;
    private final CommentType type;
    private final String content;
    private final boolean resolved;
    private final String resolvedBy;
    private final Instant resolvedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    public CommentResponse(
        String commentId,
        String storyId,
        String author,
        CommentType type,
        String content,
        boolean resolved,
        String resolvedBy,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.commentId = commentId;
        this.storyId = storyId;
        this.author = author;
        this.type = type;
        this.content = content;
        this.resolved = resolved;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId().getValue(),
            comment.getStoryId().getValue(),
            comment.getAuthor(),
            comment.getType(),
            comment.getContent(),
            comment.isResolved(),
            comment.getResolvedBy(),
            comment.getResolvedAt(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }

    public String getCommentId() {
        return commentId;
    }

    public String getStoryId() {
        return storyId;
    }

    public String getAuthor() {
        return author;
    }

    public CommentType getType() {
        return type;
    }

    public String getContent() {
        return content;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

