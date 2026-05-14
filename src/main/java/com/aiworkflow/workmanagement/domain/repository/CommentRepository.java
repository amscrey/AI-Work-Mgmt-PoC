package com.aiworkflow.workmanagement.domain.repository;

import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Comment entity.
 */
public interface CommentRepository {

    /**
     * Saves a comment.
     *
     * @param comment The comment to save
     * @return The saved comment
     */
    Comment save(Comment comment);

    /**
     * Finds a comment by its ID.
     *
     * @param id The comment ID
     * @return Optional containing the comment if found
     */
    Optional<Comment> findById(CommentId id);

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
     * @param id The comment ID
     */
    void delete(CommentId id);
}
