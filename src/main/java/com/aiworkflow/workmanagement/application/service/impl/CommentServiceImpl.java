package com.aiworkflow.workmanagement.application.service.impl;

import com.aiworkflow.workmanagement.application.command.CreateCommentCommand;
import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.application.service.CommentService;
import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.repository.CommentRepository;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.UsageBlock;
import com.aiworkflow.workmanagement.orchestration.service.UsageBlockFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of CommentService.
 * Note: Spring annotations will be added when Spring Boot is configured.
 */
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final StoryRepository storyRepository;
    private final ActivityLogger activityLogger;
    private final UsageBlockFormatter usageBlockFormatter;

    public CommentServiceImpl(CommentRepository commentRepository,
                             StoryRepository storyRepository,
                             ActivityLogger activityLogger) {
        this(commentRepository, storyRepository, activityLogger, null);
    }

    public CommentServiceImpl(CommentRepository commentRepository,
                             StoryRepository storyRepository,
                             ActivityLogger activityLogger,
                             UsageBlockFormatter usageBlockFormatter) {
        this.commentRepository = commentRepository;
        this.storyRepository = storyRepository;
        this.activityLogger = activityLogger;
        this.usageBlockFormatter = usageBlockFormatter;
    }

    @Override
    public Comment createComment(CreateCommentCommand command) {
        try {
            // Find the parent story
            StoryId storyId = new StoryId(command.getStoryId());
            Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException("Story not found: " + command.getStoryId()));

            // Create the comment
            CommentId commentId = new CommentId(command.getCommentId());
            String content = command.getContent();
            UsageBlock usageBlock = null;
            if (command.isIncludeUsageBlock() && usageBlockFormatter != null) {
                usageBlock = usageBlockFormatter.format(storyId);
                content = content + "\n\n" + usageBlock.getText();
            }

            Comment comment = new Comment(
                commentId,
                storyId,
                command.getAuthor(),
                content,
                command.getType()
            );

            // Save the comment
            Comment savedComment = commentRepository.save(comment);

            // Add comment to story
            story.addComment(savedComment);
            storyRepository.save(story);

            // Log the activity with enriched details
            Map<String, Object> details = new HashMap<>();
            details.put("author", command.getAuthor());
            details.put("type", command.getType().toString());
            details.put("story_id", command.getStoryId());
            details.put("story_title", story.getTitle());
            details.put("content_length", content.length());
            details.put("content_preview", content.substring(0, Math.min(50, content.length())));
            if (command.getExecutionId() != null) {
                details.put("execution_id", command.getExecutionId());
            }
            if (usageBlock != null) {
                details.putAll(usageBlock.getMetrics());
            }
            activityLogger.logSuccess(command.getAuthor(), "create_comment", command.getCommentId(), details);

            return savedComment;
        } catch (Exception e) {
            Map<String, Object> failureDetails = new HashMap<>();
            failureDetails.put("author", command.getAuthor());
            failureDetails.put("type", command.getType().toString());
            failureDetails.put("story_id", command.getStoryId());
            activityLogger.logFailure(command.getAuthor(), "create_comment", command.getCommentId(), e.getMessage(), failureDetails);
            throw e;
        }
    }

    @Override
    public Optional<Comment> findById(CommentId commentId) {
        return commentRepository.findById(commentId);
    }

    @Override
    public List<Comment> findByStoryId(StoryId storyId) {
        return commentRepository.findByStoryId(storyId);
    }

    @Override
    public void deleteComment(CommentId commentId) {
        try {
            // Try to find the comment first to get author info
            Optional<Comment> commentOpt = commentRepository.findById(commentId);
            String role = "system";
            Map<String, Object> details = new HashMap<>();

            if (commentOpt.isPresent()) {
                Comment comment = commentOpt.get();
                role = comment.getAuthor();
                details.put("author", comment.getAuthor());
                details.put("type", comment.getType().toString());
                details.put("story_id", comment.getStoryId().getValue());
            }

            commentRepository.delete(commentId);
            activityLogger.logSuccess(role, "delete_comment", commentId.getValue(), details);
        } catch (Exception e) {
            activityLogger.logFailure("system", "delete_comment", commentId.getValue(), e.getMessage(), null);
            throw e;
        }
    }

    /**
     * Exception thrown when a story is not found.
     */
    public static class StoryNotFoundException extends RuntimeException {
        public StoryNotFoundException(String message) {
            super(message);
        }
    }
}
