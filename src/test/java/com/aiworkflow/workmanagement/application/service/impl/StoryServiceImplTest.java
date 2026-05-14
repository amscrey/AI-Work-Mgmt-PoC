package com.aiworkflow.workmanagement.application.service.impl;

import com.aiworkflow.workmanagement.application.command.CreateStoryCommand;
import com.aiworkflow.workmanagement.application.command.TransitionStoryCommand;
import com.aiworkflow.workmanagement.application.command.UpdateStoryCommand;
import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Role;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.service.InvalidStateTransitionException;
import com.aiworkflow.workmanagement.domain.service.StateTransitionValidator;
import com.aiworkflow.workmanagement.domain.valueobject.RoleName;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoryServiceImpl Tests")
class StoryServiceImplTest {

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private StateTransitionValidator stateTransitionValidator;

    @Mock
    private ActivityLogger activityLogger;

    @InjectMocks
    private StoryServiceImpl storyService;

    @Nested
    @DisplayName("Create Story Tests")
    class CreateStoryTests {

        @Test
        @DisplayName("Should create story with all fields")
        void shouldCreateStoryWithAllFields() {
            // Given
            CreateStoryCommand command = new CreateStoryCommand(
                "STORY-001",
                "Implement feature",
                "Short summary",
                "agent",
                PrioritizationState.BACKLOG
            );

            Story expectedStory = new Story(
                new StoryId("STORY-001"),
                "Implement feature",
                "Short summary",
                WorkflowState.TODO,
                PrioritizationState.BACKLOG
            );

            when(storyRepository.save(any(Story.class))).thenReturn(expectedStory);

            // When
            Story result = storyService.createStory(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId().getValue()).isEqualTo("STORY-001");
            assertThat(result.getTitle()).isEqualTo("Implement feature");

            // Verify repository was called
            ArgumentCaptor<Story> storyCaptor = ArgumentCaptor.forClass(Story.class);
            verify(storyRepository).save(storyCaptor.capture());

            Story savedStory = storyCaptor.getValue();
            assertThat(savedStory.getTitle()).isEqualTo("Implement feature");
            assertThat(savedStory.getState()).isEqualTo(WorkflowState.TODO);

            // Verify activity logging (role is "agent" from command.getAuthor())
            verify(activityLogger).logSuccess(
                eq("agent"),
                eq("create_story"),
                eq("STORY-001"),
                any(Map.class)
            );
        }

        @Test
        @DisplayName("Should create story with defaults when optional fields are null")
        void shouldCreateStoryWithDefaults() {
            // Given
            CreateStoryCommand command = new CreateStoryCommand(
                "STORY-002",
                "Another feature",
                "Summary",
                "system",
                PrioritizationState.BACKLOG
            );

            Story expectedStory = new Story(
                new StoryId("STORY-002"),
                "Another feature",
                "Summary",
                WorkflowState.TODO,
                PrioritizationState.BACKLOG
            );

            when(storyRepository.save(any(Story.class))).thenReturn(expectedStory);

            // When
            Story result = storyService.createStory(command);

            // Then
            assertThat(result).isNotNull();
            verify(storyRepository).save(any(Story.class));
            verify(activityLogger).logSuccess(
                eq("system"),
                eq("create_story"),
                eq("STORY-002"),
                any(Map.class)
            );
        }

        @Test
        @DisplayName("Should log failure when repository throws exception")
        void shouldLogFailureOnRepositoryException() {
            // Given
            CreateStoryCommand command = new CreateStoryCommand(
                "STORY-003",
                "Feature",
                "Summary",
                "agent",
                PrioritizationState.BACKLOG
            );

            when(storyRepository.save(any(Story.class)))
                .thenThrow(new RuntimeException("Database error"));

            // When/Then
            assertThatThrownBy(() -> storyService.createStory(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

            verify(activityLogger).logFailure(
                eq("agent"),
                eq("create_story"),
                eq("STORY-003"),
                eq("Database error"),
                any(Map.class)  // Now includes failure details
            );
        }
    }

    @Nested
    @DisplayName("Update Story Tests")
    class UpdateStoryTests {

        private Story existingStory;

        @BeforeEach
        void setUp() {
            existingStory = new Story(
                new StoryId("STORY-001"),
                "Original title",
                "Original summary",
                WorkflowState.TODO,
                PrioritizationState.BACKLOG
            );
        }

        @Test
        @DisplayName("Should update story title and description")
        void shouldUpdateStoryTitleAndDescription() {
            // Given
            StoryId storyId = new StoryId("STORY-001");
            UpdateStoryCommand command = new UpdateStoryCommand(
                "STORY-001",
                "Updated title",
                "Updated summary",
                "Updated description",
                null,
                null,
                null
            );

            when(storyRepository.findById(storyId)).thenReturn(Optional.of(existingStory));
            when(storyRepository.save(any(Story.class))).thenReturn(existingStory);

            // When
            Story result = storyService.updateStory(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated title");
            verify(storyRepository).save(existingStory);
            verify(activityLogger).logSuccess(
                eq("system"),
                eq("update_story"),
                eq("STORY-001"),
                any(Map.class)  // Now includes details of what was updated
            );
        }

        @Test
        @DisplayName("Should update estimated effort")
        void shouldUpdateEstimatedEffort() {
            // Given
            StoryId storyId = new StoryId("STORY-001");
            UpdateStoryCommand command = new UpdateStoryCommand(
                "STORY-001",
                null,
                null,
                null,
                null,  // acceptanceCriteria
                null,  // tags
                13     // estimatedEffort (story points)
            );

            when(storyRepository.findById(storyId)).thenReturn(Optional.of(existingStory));
            when(storyRepository.save(any(Story.class))).thenReturn(existingStory);

            // When
            Story result = storyService.updateStory(command);

            // Then
            assertThat(result.getEstimatedEffort()).isEqualTo(13);
            verify(storyRepository).save(existingStory);
        }

        @Test
        @DisplayName("Should throw exception when story not found")
        void shouldThrowExceptionWhenStoryNotFound() {
            // Given
            StoryId storyId = new StoryId("STORY-999");
            UpdateStoryCommand command = new UpdateStoryCommand(
                "STORY-999",
                "Title",
                null,
                null,
                null,
                null,
                null
            );

            when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> storyService.updateStory(command))
                .isInstanceOf(StoryServiceImpl.StoryNotFoundException.class)
                .hasMessageContaining("STORY-999");

            verify(storyRepository, never()).save(any(Story.class));
        }
    }

    @Nested
    @DisplayName("Transition Story Tests")
    class TransitionStoryTests {

        private Story existingStory;

        @BeforeEach
        void setUp() {
            existingStory = new Story(
                new StoryId("STORY-001"),
                "Feature",
                "Summary",
                WorkflowState.TODO,
                PrioritizationState.BACKLOG
            );
        }

        @Test
        @DisplayName("Should transition story to IN_PROGRESS")
        void shouldTransitionStoryToInProgress() {
            // Given
            StoryId storyId = new StoryId("STORY-001");
            TransitionStoryCommand command = new TransitionStoryCommand(
                "STORY-001",
                WorkflowState.IN_PROGRESS,
                Role.AGENT,
                "Starting work"
            );

            when(storyRepository.findById(storyId)).thenReturn(Optional.of(existingStory));
            when(storyRepository.save(any(Story.class))).thenReturn(existingStory);
            doNothing().when(stateTransitionValidator)
                .validateWorkflowTransition(existingStory, WorkflowState.IN_PROGRESS, Role.AGENT);

            // When
            Story result = storyService.transitionStory(command);

            // Then
            assertThat(result.getState()).isEqualTo(WorkflowState.IN_PROGRESS);
            verify(stateTransitionValidator).validateWorkflowTransition(
                existingStory,
                WorkflowState.IN_PROGRESS,
                Role.AGENT
            );
            verify(storyRepository).save(existingStory);
            verify(activityLogger).logSuccess(
                eq("agent"),
                eq("transition_story"),
                eq("STORY-001"),
                any(Map.class)
            );
        }

        @Test
        @DisplayName("Should throw exception on invalid transition")
        void shouldThrowExceptionOnInvalidTransition() {
            // Given
            StoryId storyId = new StoryId("STORY-001");
            TransitionStoryCommand command = new TransitionStoryCommand(
                "STORY-001",
                WorkflowState.DONE,
                Role.AGENT,
                "Invalid transition"
            );

            when(storyRepository.findById(storyId)).thenReturn(Optional.of(existingStory));
            doThrow(new InvalidStateTransitionException("Invalid transition"))
                .when(stateTransitionValidator)
                .validateWorkflowTransition(existingStory, WorkflowState.DONE, Role.AGENT);

            // When/Then
            assertThatThrownBy(() -> storyService.transitionStory(command))
                .isInstanceOf(InvalidStateTransitionException.class);

            verify(storyRepository, never()).save(any(Story.class));
            verify(activityLogger).logFailure(
                eq("system"),
                eq("transition_story"),
                eq("STORY-001"),
                eq("Invalid transition"),
                eq(null)
            );
        }
    }

    // Note: ChangePrioritization tests removed - feature not yet implemented in Story entity

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {

        @Test
        @DisplayName("Should find story by ID")
        void shouldFindStoryById() {
            // Given
            StoryId storyId = new StoryId("STORY-001");
            Story story = new Story(
                storyId,
                "Feature",
                "Summary",
                WorkflowState.TODO,
                PrioritizationState.BACKLOG
            );

            when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

            // When
            Optional<Story> result = storyService.findById(storyId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(storyId);
            verify(storyRepository).findById(storyId);
        }

        @Test
        @DisplayName("Should find stories by state")
        void shouldFindStoriesByState() {
            // Given
            List<Story> expectedStories = Arrays.asList(
                new Story(new StoryId("STORY-001"), "F1", "S1", WorkflowState.TODO, PrioritizationState.BACKLOG),
                new Story(new StoryId("STORY-002"), "F2", "S2", WorkflowState.TODO, PrioritizationState.BACKLOG)
            );

            when(storyRepository.findByState(WorkflowState.TODO)).thenReturn(expectedStories);

            // When
            List<Story> result = storyService.findByState(WorkflowState.TODO);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(s -> s.getState() == WorkflowState.TODO);
            verify(storyRepository).findByState(WorkflowState.TODO);
        }

        @Test
        @DisplayName("Should find stories by prioritization")
        void shouldFindStoriesByPrioritization() {
            // Given
            List<Story> expectedStories = Arrays.asList(
                new Story(new StoryId("STORY-001"), "F1", "S1", WorkflowState.TODO, PrioritizationState.PRIORITIZED)
            );

            when(storyRepository.findByPrioritization(PrioritizationState.PRIORITIZED))
                .thenReturn(expectedStories);

            // When
            List<Story> result = storyService.findByPrioritization(PrioritizationState.PRIORITIZED);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPrioritization()).isEqualTo(PrioritizationState.PRIORITIZED);
            verify(storyRepository).findByPrioritization(PrioritizationState.PRIORITIZED);
        }

        @Test
        @DisplayName("Should find all stories")
        void shouldFindAllStories() {
            // Given
            List<Story> expectedStories = Arrays.asList(
                new Story(new StoryId("STORY-001"), "F1", "S1", WorkflowState.TODO, PrioritizationState.BACKLOG),
                new Story(new StoryId("STORY-002"), "F2", "S2", WorkflowState.IN_PROGRESS, PrioritizationState.PRIORITIZED)
            );

            when(storyRepository.findAll()).thenReturn(expectedStories);

            // When
            List<Story> result = storyService.findAll();

            // Then
            assertThat(result).hasSize(2);
            verify(storyRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Delete Story Tests")
    class DeleteStoryTests {

        @Test
        @DisplayName("Should delete story successfully")
        void shouldDeleteStory() {
            // Given
            StoryId storyId = new StoryId("STORY-001");
            doNothing().when(storyRepository).delete(storyId);

            // When
            storyService.deleteStory(storyId);

            // Then
            verify(storyRepository).delete(storyId);
            verify(activityLogger).logSuccess(
                eq("system"),
                eq("delete_story"),
                eq("STORY-001"),
                eq(null)
            );
        }

        @Test
        @DisplayName("Should log failure when delete throws exception")
        void shouldLogFailureOnDeleteException() {
            // Given
            StoryId storyId = new StoryId("STORY-001");
            doThrow(new RuntimeException("Delete failed"))
                .when(storyRepository).delete(storyId);

            // When/Then
            assertThatThrownBy(() -> storyService.deleteStory(storyId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Delete failed");

            verify(activityLogger).logFailure(
                eq("system"),
                eq("delete_story"),
                eq("STORY-001"),
                eq("Delete failed"),
                eq(null)
            );
        }
    }
}
