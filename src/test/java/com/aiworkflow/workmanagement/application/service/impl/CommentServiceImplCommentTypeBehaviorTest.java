package com.aiworkflow.workmanagement.application.service.impl;

import com.aiworkflow.workmanagement.application.command.CreateCommentCommand;
import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.model.CommentType;
import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.repository.CommentRepository;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Comment Type Behavior Tests")
class CommentServiceImplCommentTypeBehaviorTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private ActivityLogger activityLogger;

    @InjectMocks
    private CommentServiceImpl commentService;

    private StoryId storyId;
    private Story parentStory;

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
    @DisplayName("Should handle question comment that requires response")
    void shouldHandleQuestionCommentRequiresResponse() {
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__agent__2026-03-30T152000Z.md",
            "STORY-001",
            "agent",
            CommentType.QUESTION,
            "How should this be implemented?"
        );

        Comment comment = new Comment(
            new CommentId("comment__agent__2026-03-30T152000Z.md"),
            storyId,
            "agent",
            "How should this be implemented?",
            CommentType.QUESTION
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(storyRepository.save(any(Story.class))).thenReturn(parentStory);

        Comment result = commentService.createComment(command);

        assertThat(result.requiresResponse()).isTrue();
        assertThat(result.isSystemGenerated()).isFalse();
        assertThat(result.isUserGenerated()).isTrue();
    }

    @Test
    @DisplayName("Should handle system info comment")
    void shouldHandleSystemInfoComment() {
        CreateCommentCommand command = new CreateCommentCommand(
            "comment__system__2026-03-30T153000Z.md",
            "STORY-001",
            "system",
            CommentType.SYSTEM,
            "Automated status update"
        );

        Comment comment = new Comment(
            new CommentId("comment__system__2026-03-30T153000Z.md"),
            storyId,
            "system",
            "Automated status update",
            CommentType.SYSTEM
        );

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(parentStory));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(storyRepository.save(any(Story.class))).thenReturn(parentStory);

        Comment result = commentService.createComment(command);

        assertThat(result.isSystemGenerated()).isTrue();
        assertThat(result.isUserGenerated()).isFalse();
    }
}

