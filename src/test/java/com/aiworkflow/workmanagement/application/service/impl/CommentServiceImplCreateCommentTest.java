package com.aiworkflow.workmanagement.application.service.impl;

import com.aiworkflow.workmanagement.application.command.CreateCommentCommand;
import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.domain.model.*;
import com.aiworkflow.workmanagement.domain.repository.CommentRepository;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.UsageBlock;
import com.aiworkflow.workmanagement.orchestration.service.UsageBlockFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Create Comment Tests")
class CommentServiceImplCreateCommentTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private ActivityLogger activityLogger;

    @Mock
    private UsageBlockFormatter usageBlockFormatter;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Story parentStory;
    private StoryId storyId;

    @BeforeEach
    void setUp() {
        storyId = new StoryId("STORY-001");
        parentStory = new Story(
            storyId,
            "Parent Story",
            "Summary",
            WorkflowState.TODO,
            PrioritizationState.PRIORITIZED
        );
    }

    @Test
    @DisplayName("Should create question comment")
    void shouldCreateQuestionComment() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__agent__2026-03-30T141530Z.md",
            "STORY-001",
            "agent",
            CommentType.QUESTION,
            "What is the expected behavior?"
        );

        Comment expectedComment = new Comment(
            new CommentId("comment__agent__2026-03-30T141530Z.md"),
            storyId,
            "agent",
            "What is the expected behavior?",
            CommentType.QUESTION
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
        when(commentRepository.save(any(Comment.class))).thenReturn(expectedComment);
        when(storyRepository.save(any(Story.class))).thenReturn(parentStory);

        // When
        Comment result = commentService.createComment(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId().getValue()).isEqualTo("comment__agent__2026-03-30T141530Z.md");

        // Verify comment was created with correct properties
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getAuthor()).isEqualTo("agent");
        assertThat(savedComment.getContent()).isEqualTo("What is the expected behavior?");
        assertThat(savedComment.getType()).isEqualTo(CommentType.QUESTION);

        // Verify comment was added to story
        ArgumentCaptor<Story> storyCaptor = ArgumentCaptor.forClass(Story.class);
        verify(storyRepository).save(storyCaptor.capture());

        // Verify activity logging
        verify(activityLogger).logSuccess(
            eq("agent"),
            eq("create_comment"),
            eq("comment__agent__2026-03-30T141530Z.md"),
            anyMap()
        );
    }

    @Test
    @DisplayName("Should create feedback comment")
    void shouldCreateFeedbackComment() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__human-reviewer__2026-03-30T142000Z.md",
            "STORY-001",
            "human-reviewer",
            CommentType.FEEDBACK,
            "This looks good, but please add more tests"
        );

        Comment expectedComment = new Comment(
            new CommentId("comment__human-reviewer__2026-03-30T142000Z.md"),
            storyId,
            "human-reviewer",
            "This looks good, but please add more tests",
            CommentType.FEEDBACK
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
        when(commentRepository.save(any(Comment.class))).thenReturn(expectedComment);
        when(storyRepository.save(any(Story.class))).thenReturn(parentStory);

        // When
        Comment result = commentService.createComment(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(CommentType.FEEDBACK);
        verify(commentRepository).save(any(Comment.class));
        verify(storyRepository).save(parentStory);
    }

    @Test
    @DisplayName("Should create approval comment")
    void shouldCreateApprovalComment() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__human-approver__2026-03-30T143000Z.md",
            "STORY-001",
            "human-approver",
            CommentType.FEEDBACK,
            "Approved for production"
        );

        Comment expectedComment = new Comment(
            new CommentId("comment__human-approver__2026-03-30T143000Z.md"),
            storyId,
            "human-approver",
            "Approved for production",
            CommentType.FEEDBACK
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
        when(commentRepository.save(any(Comment.class))).thenReturn(expectedComment);
        when(storyRepository.save(any(Story.class))).thenReturn(parentStory);

        // When
        Comment result = commentService.createComment(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(CommentType.FEEDBACK);
        assertThat(result.getAuthor()).isEqualTo("human-approver");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should create info comment")
    void shouldCreateInfoComment() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__system__2026-03-30T144000Z.md",
            "STORY-001",
            "system",
            CommentType.SYSTEM,
            "Story state changed to IN_PROGRESS"
        );

        Comment expectedComment = new Comment(
            new CommentId("comment__system__2026-03-30T144000Z.md"),
            storyId,
            "system",
            "Story state changed to IN_PROGRESS",
            CommentType.SYSTEM
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
        when(commentRepository.save(any(Comment.class))).thenReturn(expectedComment);
        when(storyRepository.save(any(Story.class))).thenReturn(parentStory);

        // When
        Comment result = commentService.createComment(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(CommentType.SYSTEM);
        assertThat(result.isSystemGenerated()).isTrue();
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw exception when parent story not found")
    void shouldThrowExceptionWhenStoryNotFound() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__agent__2026-03-30T145000Z.md",
            "STORY-999",
            "agent",
            CommentType.QUESTION,
            "Comment content"
        );

        when(storyRepository.findById(any(StoryId.class))).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.createComment(command))
            .isInstanceOf(CommentServiceImpl.StoryNotFoundException.class)
            .hasMessageContaining("STORY-999");

        verify(commentRepository, never()).save(any(Comment.class));
        verify(activityLogger).logFailure(
            eq("agent"),
            eq("create_comment"),
            eq("comment__agent__2026-03-30T145000Z.md"),
            anyString(),
            anyMap()
        );
    }

    @Test
    @DisplayName("Should log failure when repository throws exception")
    void shouldLogFailureOnRepositoryException() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__agent__2026-03-30T150000Z.md",
            "STORY-001",
            "agent",
            CommentType.QUESTION,
            "Content"
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
        when(commentRepository.save(any(Comment.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When/Then
        assertThatThrownBy(() -> commentService.createComment(command))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");

        verify(activityLogger).logFailure(
            eq("agent"),
            eq("create_comment"),
            eq("comment__agent__2026-03-30T150000Z.md"),
            eq("Database error"),
            anyMap()
        );
    }

    @Test
    @DisplayName("Should verify comment parameter order is correct")
    void shouldVerifyCommentParameterOrderIsCorrect() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__agent__2026-03-30T151000Z.md",
            "STORY-001",
            "agent",
            CommentType.QUESTION,
            "test content"
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(storyRepository.save(any(Story.class))).thenReturn(parentStory);

        // When
        commentService.createComment(command);

        // Then
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        // Verify constructor was called with correct order: id, storyId, author, content, type
        assertThat(savedComment.getId().getValue()).isEqualTo("comment__agent__2026-03-30T151000Z.md");
        assertThat(savedComment.getStoryId()).isEqualTo(storyId);
        assertThat(savedComment.getAuthor()).isEqualTo("agent");
        assertThat(savedComment.getContent()).isEqualTo("test content");
        assertThat(savedComment.getType()).isEqualTo(CommentType.QUESTION);
    }

    @Test
    @DisplayName("Should append usage block when requested")
    void shouldAppendUsageBlockWhenRequested() {
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__agent__2026-03-30T145000Z.md",
            "STORY-001",
            "agent",
            CommentType.NOTE,
            "Work complete",
            "exec-123",
            true
        );

        UsageBlock usageBlock = new UsageBlock("---\nToken usage\n---", Map.of("usage_exact_total", 42));

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(storyRepository.save(any(Story.class))).thenReturn(parentStory);
        when(usageBlockFormatter.format(storyId)).thenReturn(usageBlock);

        Comment result = commentService.createComment(command);

        assertThat(result.getContent()).contains("Token usage");
        verify(activityLogger).logSuccess(eq("agent"), eq("create_comment"), eq("comment__agent__2026-03-30T145000Z.md"), anyMap());
    }
}
