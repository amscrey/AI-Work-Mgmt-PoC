package com.aiworkflow.workmanagement.application.service.impl;

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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Query Tests")
class CommentServiceImplQueryTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private StoryId storyId;

    @BeforeEach
    void setUp() {
        storyId = new StoryId("STORY-001");
    }

    @Test
    @DisplayName("Should find comment by ID")
    void shouldFindCommentById() {
        CommentId commentId = new CommentId("comment__agent__2026-03-30T141530Z.md");
        Comment comment = new Comment(
            commentId,
            storyId,
            "agent",
            "Test comment",
            CommentType.QUESTION
        );

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Optional<Comment> result = commentService.findById(commentId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(commentId);
        assertThat(result.get().getContent()).isEqualTo("Test comment");
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("Should return empty when comment not found")
    void shouldReturnEmptyWhenCommentNotFound() {
        CommentId commentId = new CommentId("comment__agent__2026-03-30T154000Z.md");
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        Optional<Comment> result = commentService.findById(commentId);

        assertThat(result).isEmpty();
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("Should find comments by story ID")
    void shouldFindCommentsByStoryId() {
        List<Comment> expectedComments = List.of(
            new Comment(
                new CommentId("comment__agent__2026-03-30T155000Z.md"),
                storyId,
                "agent",
                "First comment",
                CommentType.QUESTION
            ),
            new Comment(
                new CommentId("comment__human-reviewer__2026-03-30T160000Z.md"),
                storyId,
                "human-reviewer",
                "Second comment",
                CommentType.FEEDBACK
            )
        );

        when(commentRepository.findByStoryId(storyId)).thenReturn(expectedComments);

        List<Comment> result = commentService.findByStoryId(storyId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getStoryId().equals(storyId));
        assertThat(result.get(0).getType()).isEqualTo(CommentType.QUESTION);
        assertThat(result.get(1).getType()).isEqualTo(CommentType.FEEDBACK);
        verify(commentRepository).findByStoryId(storyId);
    }

    @Test
    @DisplayName("Should return empty list when no comments for story")
    void shouldReturnEmptyListWhenNoComments() {
        when(commentRepository.findByStoryId(storyId)).thenReturn(List.of());

        List<Comment> result = commentService.findByStoryId(storyId);

        assertThat(result).isEmpty();
        verify(commentRepository).findByStoryId(storyId);
    }
}
