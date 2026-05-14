package com.aiworkflow.workmanagement.application.service;

import com.aiworkflow.workmanagement.application.command.CreateCommentCommand;
import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing Comments.
 */
public interface CommentService {

    /**
     * Creates a new comment on a story.
     *
     * @param command The create comment command
     * @return The created comment
     */
    Comment createComment(CreateCommentCommand command);

    /**
     * Finds a comment by ID.
     *
     * @param commentId The comment ID
     * @return Optional containing the comment if found
     */
    Optional<Comment> findById(CommentId commentId);

    /**
     * Finds all comments for a story.
     *
     * @param storyId The story ID
     * @return List of comments for the story
     */
    List<Comment> findByStoryId(StoryId storyId);

    /**
     * Deletes a comment.
     *
     * @param commentId The comment ID
     */
    void deleteComment(CommentId commentId);
}
