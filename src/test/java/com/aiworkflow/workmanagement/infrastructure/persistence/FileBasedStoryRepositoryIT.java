package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for FileBasedStoryRepository.
 * Tests actual file system operations.
 *
 * <p><b>Test Workspace:</b> {@code target/test-workspace-story}</p>
 *
 * <p>These integration tests verify file-based persistence with real I/O operations.
 * Each test runs in isolation with a clean workspace created in @BeforeEach.
 * Test artifacts are LEFT in target/ for inspection - next test run cleans up previous artifacts.
 * The workspace is located in the Maven {@code target/} directory, which is automatically
 * cleaned by {@code mvn clean}.</p>
 *
 * <p><b>For Developers (Human & AI):</b></p>
 * <ul>
 *   <li>📖 See {@code etc/docs/TESTING.md} for complete testing guide</li>
 *   <li>📖 Integration test workspace configuration documented in TESTING.md section 3.2</li>
 *   <li>⚠️ When modifying this test class, update etc/docs/TESTING.md to reflect changes</li>
 *   <li>⚠️ Keep test boundaries and verification scenarios documented</li>
 * </ul>
 *
 * <p><b>What This Test Suite Verifies:</b></p>
 * <ul>
 *   <li>Story JSON persistence and deserialization</li>
 *   <li>Directory structure creation (backlog/, prioritized/)</li>
 *   <li>CRUD operations with actual file I/O</li>
 *   <li>Query operations across prioritization states</li>
 *   <li>All story fields persist correctly</li>
 *   <li>Error handling for not-found scenarios</li>
 * </ul>
 *
 * <p><b>Note:</b> DEPRIORITIZED stories are stored in backlog/ directory (not a separate directory)</p>
 *
 * @see com.aiworkflow.workmanagement.infrastructure.persistence.FileBasedStoryRepository
 */
@DisplayName("FileBasedStoryRepository Integration Tests")
class FileBasedStoryRepositoryIT {

    private static final String TEST_WORKSPACE = "target/test-workspace-story";
    private FileBasedStoryRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up test workspace from previous run (if exists)
        cleanUpWorkspace();

        // Create fresh repository
        repository = new FileBasedStoryRepository(TEST_WORKSPACE);
    }

    // NOTE: No @AfterEach cleanup - leaves test artifacts in target/ for inspection
    // Next test run will clean up in @BeforeEach

    private void cleanUpWorkspace() throws IOException {
        Path workspace = Paths.get(TEST_WORKSPACE);
        if (Files.exists(workspace)) {
            try (Stream<Path> walk = Files.walk(workspace)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
            }
        }
    }

    @Nested
    @DisplayName("Save and Find Operations")
    class SaveAndFindTests {

        @Test
        @DisplayName("Should save and retrieve a story")
        void shouldSaveAndRetrieveStory() {
            // Given
            Story story = new Story(
                new StoryId("STORY-001"),
                "Implement feature",
                "Short summary",
                WorkflowState.TODO,
                PrioritizationState.BACKLOG
            );

            // When
            repository.save(story);

            // Then
            Optional<Story> retrieved = repository.findById(new StoryId("STORY-001"));
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getId().getValue()).isEqualTo("STORY-001");
            assertThat(retrieved.get().getTitle()).isEqualTo("Implement feature");
            assertThat(retrieved.get().getSummary()).isEqualTo("Short summary");
            assertThat(retrieved.get().getWorkflowState()).isEqualTo(WorkflowState.TODO);
            assertThat(retrieved.get().getPrioritizationState()).isEqualTo(PrioritizationState.BACKLOG);
        }

        @Test
        @DisplayName("Should create directory structure correctly")
        void shouldCreateDirectoryStructure() throws IOException {
            // Given
            Story story = new Story(
                new StoryId("STORY-002"),
                "Another feature",
                "Summary",
                WorkflowState.TODO,
                PrioritizationState.PRIORITIZED
            );

            // When
            repository.save(story);

            // Then
            Path storyFile = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-002", "story.json");
            assertThat(Files.exists(storyFile)).isTrue();

            Path tasksDir = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-002", "tasks");
            assertThat(Files.exists(tasksDir)).isTrue();
            assertThat(Files.isDirectory(tasksDir)).isTrue();

            Path commentsDir = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-002", "comments");
            assertThat(Files.exists(commentsDir)).isTrue();
            assertThat(Files.isDirectory(commentsDir)).isTrue();
        }

        @Test
        @DisplayName("Should update existing story")
        void shouldUpdateExistingStory() {
            // Given
            Story story = new Story(
                new StoryId("STORY-003"),
                "Original title",
                "Original summary",
                WorkflowState.TODO,
                PrioritizationState.BACKLOG
            );
            repository.save(story);

            // When - update the story
            story.setTitle("Updated title");
            story.setDescription("New description");
            repository.save(story);

            // Then
            Optional<Story> retrieved = repository.findById(new StoryId("STORY-003"));
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getTitle()).isEqualTo("Updated title");
            assertThat(retrieved.get().getDescription()).isEqualTo("New description");
        }

        @Test
        @DisplayName("Should return empty when story not found")
        void shouldReturnEmptyWhenNotFound() {
            // When
            Optional<Story> result = repository.findById(new StoryId("STORY-999"));

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryTests {

        @Test
        @DisplayName("Should find stories by workflow state")
        void shouldFindStoriesByWorkflowState() {
            // Given
            Story story1 = new Story(new StoryId("STORY-001"), "F1", "S1", WorkflowState.TODO, PrioritizationState.BACKLOG);
            Story story2 = new Story(new StoryId("STORY-002"), "F2", "S2", WorkflowState.TODO, PrioritizationState.PRIORITIZED);
            Story story3 = new Story(new StoryId("STORY-003"), "F3", "S3", WorkflowState.IN_PROGRESS, PrioritizationState.BACKLOG);

            repository.save(story1);
            repository.save(story2);
            repository.save(story3);

            // When
            List<Story> todoStories = repository.findByState(WorkflowState.TODO);

            // Then
            assertThat(todoStories).hasSize(2);
            assertThat(todoStories).allMatch(s -> s.getWorkflowState() == WorkflowState.TODO);
        }

        @Test
        @DisplayName("Should find stories by prioritization state")
        void shouldFindStoriesByPrioritizationState() {
            // Given
            Story story1 = new Story(new StoryId("STORY-001"), "F1", "S1", WorkflowState.TODO, PrioritizationState.BACKLOG);
            Story story2 = new Story(new StoryId("STORY-002"), "F2", "S2", WorkflowState.TODO, PrioritizationState.PRIORITIZED);
            Story story3 = new Story(new StoryId("STORY-003"), "F3", "S3", WorkflowState.TODO, PrioritizationState.PRIORITIZED);

            repository.save(story1);
            repository.save(story2);
            repository.save(story3);

            // When
            List<Story> prioritizedStories = repository.findByPrioritization(PrioritizationState.PRIORITIZED);

            // Then
            assertThat(prioritizedStories).hasSize(2);
            assertThat(prioritizedStories).allMatch(s -> s.getPrioritizationState() == PrioritizationState.PRIORITIZED);
        }

        @Test
        @DisplayName("Should find all stories")
        void shouldFindAllStories() {
            // Given
            Story story1 = new Story(new StoryId("STORY-001"), "F1", "S1", WorkflowState.TODO, PrioritizationState.BACKLOG);
            Story story2 = new Story(new StoryId("STORY-002"), "F2", "S2", WorkflowState.TODO, PrioritizationState.PRIORITIZED);
            Story story3 = new Story(new StoryId("STORY-003"), "F3", "S3", WorkflowState.TODO, PrioritizationState.DEPRIORITIZED);

            repository.save(story1);
            repository.save(story2);
            repository.save(story3);

            // When
            List<Story> allStories = repository.findAll();

            // Then
            assertThat(allStories).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete story and its directory")
        void shouldDeleteStoryAndDirectory() throws IOException {
            // Given
            Story story = new Story(
                new StoryId("STORY-901"),
                "To be deleted",
                "Summary",
                WorkflowState.TODO,
                PrioritizationState.BACKLOG
            );
            repository.save(story);

            Path storyDir = Paths.get(TEST_WORKSPACE, "backlog", "STORY-901");
            assertThat(Files.exists(storyDir)).isTrue();

            // When
            repository.delete(new StoryId("STORY-901"));

            // Then
            assertThat(Files.exists(storyDir)).isFalse();
            assertThat(repository.findById(new StoryId("STORY-901"))).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent story")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            // When/Then
            assertThatThrownBy(() -> repository.delete(new StoryId("STORY-999")))
                .isInstanceOf(FileBasedStoryRepository.RepositoryException.class)
                .hasMessageContaining("Story not found");
        }
    }

    @Nested
    @DisplayName("Exists Operations")
    class ExistsTests {

        @Test
        @DisplayName("Should return true when story exists")
        void shouldReturnTrueWhenStoryExists() {
            // Given
            Story story = new Story(
                new StoryId("STORY-801"),
                "Exists",
                "Summary",
                WorkflowState.TODO,
                PrioritizationState.BACKLOG
            );
            repository.save(story);

            // When/Then
            assertThat(repository.exists(new StoryId("STORY-801"))).isTrue();
        }

        @Test
        @DisplayName("Should return false when story does not exist")
        void shouldReturnFalseWhenStoryDoesNotExist() {
            // When/Then
            assertThat(repository.exists(new StoryId("STORY-999"))).isFalse();
        }
    }

    @Nested
    @DisplayName("Persistence Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Should persist all story fields")
        void shouldPersistAllStoryFields() {
            // Given
            Story story = new Story(
                new StoryId("STORY-701"),
                "Full story",
                "Complete summary",
                WorkflowState.IN_PROGRESS,
                PrioritizationState.PRIORITIZED
            );
            story.setDescription("Detailed description");
            story.setEstimatedEffort(13);
            story.addTag("backend");
            story.addTag("high-priority");
            story.addAcceptanceCriterion("AC1: Should work");
            story.addAcceptanceCriterion("AC2: Should be fast");

            // When
            repository.save(story);

            // Then
            Optional<Story> retrieved = repository.findById(new StoryId("STORY-701"));
            assertThat(retrieved).isPresent();

            Story loaded = retrieved.get();
            assertThat(loaded.getDescription()).isEqualTo("Detailed description");
            assertThat(loaded.getEstimatedEffort()).isEqualTo(13);
            assertThat(loaded.getTags()).containsExactly("backend", "high-priority");
            assertThat(loaded.getAcceptanceCriteria()).containsExactly("AC1: Should work", "AC2: Should be fast");
        }
    }
}
