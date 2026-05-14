package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.domain.model.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentCreateRequest {
    @NotBlank
    private String commentId;
    @NotBlank
    private String storyId;
    @NotBlank
    private String author;
    @NotNull
    private CommentType type;
    @NotBlank
    private String content;
    private String executionId;
    private boolean includeUsageBlock;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public CommentType getType() {
        return type;
    }

    public void setType(CommentType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public boolean isIncludeUsageBlock() {
        return includeUsageBlock;
    }

    public void setIncludeUsageBlock(boolean includeUsageBlock) {
        this.includeUsageBlock = includeUsageBlock;
    }
}

