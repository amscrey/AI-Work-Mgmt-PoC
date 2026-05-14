package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.api.dto.CommentCreateRequest;
import com.aiworkflow.workmanagement.api.dto.CommentResponse;
import com.aiworkflow.workmanagement.application.command.CreateCommentCommand;
import com.aiworkflow.workmanagement.application.service.CommentService;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for comments.
 */
@RestController
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/comments")
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentCreateRequest request) {
        CreateCommentCommand command = new CreateCommentCommand(
            request.getCommentId(),
            request.getStoryId(),
            request.getAuthor(),
            request.getType(),
            request.getContent(),
            request.getExecutionId(),
            request.isIncludeUsageBlock()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(commentService.createComment(command)));
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable String commentId) {
        return commentService.findById(new CommentId(commentId))
            .map(comment -> ResponseEntity.ok(CommentResponse.from(comment)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/stories/{storyId}/comments")
    public ResponseEntity<List<CommentResponse>> listComments(@PathVariable String storyId) {
        List<CommentResponse> responses = commentService.findByStoryId(new StoryId(storyId))
            .stream()
            .map(CommentResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(new CommentId(commentId));
        return ResponseEntity.noContent().build();
    }
}

