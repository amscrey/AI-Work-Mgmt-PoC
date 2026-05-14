package com.aiworkflow.workmanagement.application.service.impl;

import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.model.CommentType;
import com.aiworkflow.workmanagement.domain.repository.CommentRepository;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Delete Comment Tests")
class CommentServiceImplDeleteCommentTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ActivityLogger activityLogger;

    @InjectMocks
    private CommentServiceImpl commentService;

    private StoryId storyId;

    @BeforeEach
    void setUp() {
        storyId = new StoryId("STORY-001");
    }

    @Test
    @DisplayName("Should delete comment successfully")
    void shouldDeleteComment() {
        CommentId commentId = new CommentId("comment__agent__2026-03-30T141530Z.md");
        doNothing().when(commentRepository).delete(commentId);

        commentService.deleteComment(commentId);

        verify(commentRepository).delete(commentId);
        verify(activityLogger).logSuccess(
            eq("system"),
            eq("delete_comment"),
            eq("comment__agent__2026-03-30T141530Z.md"),
            anyMap()
        );
    }

    @Test
    @DisplayName("Should log failure when delete throws exception")
    void shouldLogFailureOnDeleteException() {
        CommentId commentId = new CommentId("comment__agent__2026-03-30T141530Z.md");
        doThrow(new RuntimeException("Delete failed"))
            .when(commentRepository).delete(commentId);

        assertThatThrownBy(() -> commentService.deleteComment(commentId))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Delete failed");

        verify(activityLogger).logFailure(
            eq("system"),
            eq("delete_comment"),
            eq("comment__agent__2026-03-30T141530Z.md"),
            eq("Delete failed"),
            eq(null)
        );
    }

    @Test
    @DisplayName("Should log delete details when comment exists")
    void shouldLogDeleteDetailsWhenCommentExists() {
        CommentId commentId = new CommentId("comment__agent__2026-03-30T141530Z.md");
        Comment comment = new Comment(
            commentId,
            storyId,
            "agent",
            "Test comment",
            CommentType.QUESTION
        );

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(commentId);

        commentService.deleteComment(commentId);

        verify(activityLogger).logSuccess(
            eq("agent"),
            eq("delete_comment"),
            eq("comment__agent__2026-03-30T141530Z.md"),
            anyMap()
        );
    }
}
