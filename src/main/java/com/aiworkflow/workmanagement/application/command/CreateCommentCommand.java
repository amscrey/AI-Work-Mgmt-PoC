package com.aiworkflow.workmanagement.application.command;

import com.aiworkflow.workmanagement.domain.model.CommentType;

import java.util.Objects;

/**
 * Command to create a Comment on a Story.
 */
public class CreateCommentCommand {
    private final String commentId;
    private final String storyId;
    private final String author;
    private final CommentType type;
    private final String content;
    private final String executionId;
    private final boolean includeUsageBlock;

    public CreateCommentCommand(String commentId, String storyId, String author,
                               CommentType type, String content) {
        this(commentId, storyId, author, type, content, null, false);
    }

    public CreateCommentCommand(String commentId, String storyId, String author,
                               CommentType type, String content, String executionId, boolean includeUsageBlock) {
        this.commentId = Objects.requireNonNull(commentId, "Comment ID cannot be null");
        this.storyId = Objects.requireNonNull(storyId, "Story ID cannot be null");
        this.author = Objects.requireNonNull(author, "Author cannot be null");
        this.type = Objects.requireNonNull(type, "Comment type cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.executionId = executionId;
        this.includeUsageBlock = includeUsageBlock;
    }

    // Getters
    public String getCommentId() { return commentId; }
    public String getStoryId() { return storyId; }
    public String getAuthor() { return author; }
    public CommentType getType() { return type; }
    public String getContent() { return content; }
    public String getExecutionId() { return executionId; }
    public boolean isIncludeUsageBlock() { return includeUsageBlock; }
}
