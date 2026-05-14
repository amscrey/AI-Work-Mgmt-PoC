package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.*;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
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
 * Integration tests for FileBasedCommentRepository.
 * Tests actual file system operations with Markdown files.
 *
 * <p><b>Test Workspace:</b> {@code target/test-workspace-comment}</p>
 *
 * <p>These integration tests verify comment persistence using Markdown files with YAML frontmatter.
 * Tests create parent stories in the workspace before testing comment operations.
 * Each test runs in isolation with a clean workspace created in @BeforeEach.
 * Test artifacts are LEFT in target/ for inspection - next test run cleans up previous artifacts.</p>
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
 *   <li>Markdown file creation with YAML frontmatter in story's comments/ directory</li>
 *   <li>File naming convention: comment__{role}__{timestamp}.md</li>
 *   <li>YAML frontmatter parsing and generation (type, author, timestamps, resolved status)</li>
 *   <li>All comment types (NOTE, QUESTION, FEEDBACK, ISSUE, RESOLUTION, STATE_CHANGE, SYSTEM)</li>
 *   <li>Resolution status persistence and updates</li>
 *   <li>Multi-line content and special character handling</li>
 *   <li>Multiple comments per story</li>
 *   <li>Comment independence (updates don't affect other comments)</li>
 *   <li>Cross-directory comment search (across all prioritization states)</li>
 * </ul>
 *
 * @see com.aiworkflow.workmanagement.infrastructure.persistence.FileBasedCommentRepository
 */
@DisplayName("FileBasedCommentRepository Integration Tests")
class FileBasedCommentRepositoryIT {

    private static final String TEST_WORKSPACE = "target/test-workspace-comment";
    private FileBasedCommentRepository commentRepository;
    private FileBasedStoryRepository storyRepository;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up test workspace from previous run (if exists)
        cleanUpWorkspace();

        // Create repositories
        storyRepository = new FileBasedStoryRepository(TEST_WORKSPACE);
        commentRepository = new FileBasedCommentRepository(TEST_WORKSPACE);

        // Create test stories
        createTestStories();
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

    private void createTestStories() {
        Story story1 = new Story(
            new StoryId("STORY-001"),
            "Test Story 1",
            "Summary 1",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );

        Story story2 = new Story(
            new StoryId("STORY-002"),
            "Test Story 2",
            "Summary 2",
            WorkflowState.IN_PROGRESS,
            PrioritizationState.PRIORITIZED
        );

        storyRepository.save(story1);
        storyRepository.save(story2);
    }

    @Nested
    @DisplayName("Save and Find Operations")
    class SaveAndFindTests {

        @Test
        @DisplayName("Should save and retrieve a comment")
        void shouldSaveAndRetrieveComment() {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T141530Z.md");
            StoryId storyId = new StoryId("STORY-001");

            Comment comment = new Comment(
                commentId,
                storyId,
                "agent",
                "What is the expected behavior?",
                CommentType.QUESTION
            );

            // When
            commentRepository.save(comment);

            // Then
            Optional<Comment> retrieved = commentRepository.findById(commentId);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getId()).isEqualTo(commentId);
            assertThat(retrieved.get().getAuthor()).isEqualTo("agent");
            assertThat(retrieved.get().getContent()).isEqualTo("What is the expected behavior?");
            assertThat(retrieved.get().getType()).isEqualTo(CommentType.QUESTION);
            assertThat(retrieved.get().isResolved()).isFalse();
        }

        @Test
        @DisplayName("Should create markdown file in correct directory")
        void shouldCreateMarkdownFileInCorrectDirectory() throws IOException {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T142000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "This is a test comment",
                CommentType.NOTE
            );

            // When
            commentRepository.save(comment);

            // Then
            Path commentFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-001", "comments",
                "comment__agent__2026-03-30T142000Z.md");
            assertThat(Files.exists(commentFile)).isTrue();
            assertThat(Files.isRegularFile(commentFile)).isTrue();

            // Verify markdown content
            String content = Files.readString(commentFile);
            assertThat(content).contains("---");
            assertThat(content).contains("type: NOTE");
            assertThat(content).contains("author: agent");
            assertThat(content).contains("resolved: false");
            assertThat(content).contains("This is a test comment");
        }

        @Test
        @DisplayName("Should persist YAML frontmatter correctly")
        void shouldPersistYamlFrontmatterCorrectly() throws IOException {
            // Given
            CommentId commentId = new CommentId("comment__human-reviewer__2026-03-30T143000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "human-reviewer",
                "Looks good, but please add tests",
                CommentType.FEEDBACK
            );

            // When
            commentRepository.save(comment);

            // Then
            Path commentFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-001", "comments",
                "comment__human-reviewer__2026-03-30T143000Z.md");
            String content = Files.readString(commentFile);

            // Verify frontmatter structure
            String[] parts = content.split("---\n", 3);
            assertThat(parts).hasSize(3);

            String frontmatter = parts[1];
            assertThat(frontmatter).contains("type: FEEDBACK");
            assertThat(frontmatter).contains("author: human-reviewer");
            assertThat(frontmatter).contains("createdAt:");
            assertThat(frontmatter).contains("updatedAt:");
            assertThat(frontmatter).contains("resolved: false");

            String body = parts[2].trim();
            assertThat(body).isEqualTo("Looks good, but please add tests");
        }

        @Test
        @DisplayName("Should update existing comment")
        void shouldUpdateExistingComment() {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T144000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "Original content",
                CommentType.QUESTION
            );
            commentRepository.save(comment);

            // When - update the comment
            comment.updateContent("Updated content");
            commentRepository.save(comment);

            // Then
            Optional<Comment> retrieved = commentRepository.findById(commentId);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getContent()).isEqualTo("Updated content");
        }

        @Test
        @DisplayName("Should return empty when comment not found")
        void shouldReturnEmptyWhenNotFound() {
            // When
            Optional<Comment> result = commentRepository.findById(
                new CommentId("comment__agent__2099-01-01T000000Z.md")
            );

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find comment across different prioritization directories")
        void shouldFindCommentAcrossPrioritizations() {
            // Given - comment in PRIORITIZED story
            CommentId commentId = new CommentId("comment__system__2026-03-30T145000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-002"),
                "system",
                "State changed to IN_PROGRESS",
                CommentType.STATE_CHANGE
            );
            commentRepository.save(comment);

            // When
            Optional<Comment> retrieved = commentRepository.findById(commentId);

            // Then
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getType()).isEqualTo(CommentType.STATE_CHANGE);
            assertThat(retrieved.get().isSystemGenerated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryTests {

        @Test
        @DisplayName("Should find all comments for a story")
        void shouldFindAllCommentsForStory() {
            // Given
            Comment comment1 = new Comment(
                new CommentId("comment__agent__2026-03-30T150000Z.md"),
                new StoryId("STORY-001"),
                "agent",
                "Question 1",
                CommentType.QUESTION
            );
            Comment comment2 = new Comment(
                new CommentId("comment__human-reviewer__2026-03-30T151000Z.md"),
                new StoryId("STORY-001"),
                "human-reviewer",
                "Feedback 1",
                CommentType.FEEDBACK
            );
            Comment comment3 = new Comment(
                new CommentId("comment__agent__2026-03-30T152000Z.md"),
                new StoryId("STORY-002"),
                "agent",
                "Question 2",
                CommentType.QUESTION
            );

            commentRepository.save(comment1);
            commentRepository.save(comment2);
            commentRepository.save(comment3);

            // When
            List<Comment> story1Comments = commentRepository.findByStoryId(new StoryId("STORY-001"));

            // Then
            assertThat(story1Comments).hasSize(2);
            assertThat(story1Comments).allMatch(c -> c.getStoryId().equals(new StoryId("STORY-001")));
            assertThat(story1Comments).extracting(c -> c.getType())
                .containsExactlyInAnyOrder(CommentType.QUESTION, CommentType.FEEDBACK);
        }

        @Test
        @DisplayName("Should return empty list when story has no comments")
        void shouldReturnEmptyListWhenNoComments() {
            // When
            List<Comment> comments = commentRepository.findByStoryId(new StoryId("STORY-001"));

            // Then
            assertThat(comments).isEmpty();
        }

        @Test
        @DisplayName("Should find only markdown files")
        void shouldFindOnlyMarkdownFiles() throws IOException {
            // Given - create a comment and a non-markdown file
            CommentId commentId = new CommentId("comment__agent__2026-03-30T153000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "Test",
                CommentType.NOTE
            );
            commentRepository.save(comment);

            // Create a non-markdown file in same directory
            Path commentsDir = Paths.get(TEST_WORKSPACE, "backlog", "STORY-001", "comments");
            Files.writeString(commentsDir.resolve("README.txt"), "This is not a comment");

            // When
            List<Comment> comments = commentRepository.findByStoryId(new StoryId("STORY-001"));

            // Then
            assertThat(comments).hasSize(1);
            assertThat(comments.get(0).getId()).isEqualTo(commentId);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete comment file")
        void shouldDeleteCommentFile() throws IOException {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T154000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "To be deleted",
                CommentType.NOTE
            );
            commentRepository.save(comment);

            Path commentFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-001", "comments",
                "comment__agent__2026-03-30T154000Z.md");
            assertThat(Files.exists(commentFile)).isTrue();

            // When
            commentRepository.delete(commentId);

            // Then
            assertThat(Files.exists(commentFile)).isFalse();
            assertThat(commentRepository.findById(commentId)).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent comment")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            // When/Then
            assertThatThrownBy(() -> commentRepository.delete(
                new CommentId("comment__agent__2099-01-01T000000Z.md")
            ))
                .isInstanceOf(FileBasedCommentRepository.RepositoryException.class)
                .hasMessageContaining("Comment not found");
        }
    }

    @Nested
    @DisplayName("Comment Type Tests")
    class CommentTypeTests {

        @Test
        @DisplayName("Should persist QUESTION comment")
        void shouldPersistQuestionComment() {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T155000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "How should this work?",
                CommentType.QUESTION
            );

            // When
            commentRepository.save(comment);

            // Then
            Optional<Comment> retrieved = commentRepository.findById(commentId);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getType()).isEqualTo(CommentType.QUESTION);
            assertThat(retrieved.get().requiresResponse()).isTrue();
            assertThat(retrieved.get().isUserGenerated()).isTrue();
        }

        @Test
        @DisplayName("Should persist ISSUE comment")
        void shouldPersistIssueComment() {
            // Given
            CommentId commentId = new CommentId("comment__human-reviewer__2026-03-30T160000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "human-reviewer",
                "This breaks the API contract",
                CommentType.ISSUE
            );

            // When
            commentRepository.save(comment);

            // Then
            Optional<Comment> retrieved = commentRepository.findById(commentId);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getType()).isEqualTo(CommentType.ISSUE);
            assertThat(retrieved.get().requiresResponse()).isTrue();
        }

        @Test
        @DisplayName("Should persist STATE_CHANGE comment")
        void shouldPersistStateChangeComment() {
            // Given
            CommentId commentId = new CommentId("comment__system__2026-03-30T161000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "system",
                "Transitioned from TODO to IN_PROGRESS",
                CommentType.STATE_CHANGE
            );

            // When
            commentRepository.save(comment);

            // Then
            Optional<Comment> retrieved = commentRepository.findById(commentId);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getType()).isEqualTo(CommentType.STATE_CHANGE);
            assertThat(retrieved.get().isSystemGenerated()).isTrue();
            assertThat(retrieved.get().requiresResponse()).isFalse();
        }
    }

    @Nested
    @DisplayName("Resolution Tests")
    class ResolutionTests {

        @Test
        @DisplayName("Should persist resolved comment")
        void shouldPersistResolvedComment() throws IOException {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T162000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "Question about implementation",
                CommentType.QUESTION
            );
            comment.resolve("human-reviewer");

            // When
            commentRepository.save(comment);

            // Then
            Optional<Comment> retrieved = commentRepository.findById(commentId);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().isResolved()).isTrue();
            assertThat(retrieved.get().getResolvedBy()).isEqualTo("human-reviewer");
            assertThat(retrieved.get().getResolvedAt()).isNotNull();

            // Verify frontmatter includes resolution info
            Path commentFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-001", "comments",
                "comment__agent__2026-03-30T162000Z.md");
            String content = Files.readString(commentFile);
            assertThat(content).contains("resolved: true");
            assertThat(content).contains("resolvedBy: human-reviewer");
            assertThat(content).contains("resolvedAt:");
        }

        @Test
        @DisplayName("Should persist unresolved comment")
        void shouldPersistUnresolvedComment() throws IOException {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T163000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "Unresolved question",
                CommentType.QUESTION
            );

            // When
            commentRepository.save(comment);

            // Then
            Path commentFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-001", "comments",
                "comment__agent__2026-03-30T163000Z.md");
            String content = Files.readString(commentFile);
            assertThat(content).contains("resolved: false");
            assertThat(content).doesNotContain("resolvedBy:");
            assertThat(content).doesNotContain("resolvedAt:");
        }

        @Test
        @DisplayName("Should update resolution status")
        void shouldUpdateResolutionStatus() {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T164000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "Question",
                CommentType.QUESTION
            );
            commentRepository.save(comment);

            // When - resolve the comment
            comment.resolve("human-approver");
            commentRepository.save(comment);

            // Then
            Optional<Comment> retrieved = commentRepository.findById(commentId);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().isResolved()).isTrue();
            assertThat(retrieved.get().getResolvedBy()).isEqualTo("human-approver");
        }
    }

    @Nested
    @DisplayName("Markdown Format Tests")
    class MarkdownFormatTests {

        @Test
        @DisplayName("Should create human-readable markdown")
        void shouldCreateHumanReadableMarkdown() throws IOException {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T165000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "This is a multi-line comment.\n\nIt has paragraphs.\n\n- And lists\n- With items",
                CommentType.NOTE
            );

            // When
            commentRepository.save(comment);

            // Then
            Path commentFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-001", "comments",
                "comment__agent__2026-03-30T165000Z.md");
            String content = Files.readString(commentFile);

            // Should be readable
            assertThat(content).startsWith("---\n");
            assertThat(content).contains("This is a multi-line comment");
            assertThat(content).contains("It has paragraphs");
            assertThat(content).contains("- And lists");
            assertThat(content).contains("- With items");
        }

        @Test
        @DisplayName("Should handle special characters in content")
        void shouldHandleSpecialCharactersInContent() {
            // Given
            CommentId commentId = new CommentId("comment__agent__2026-03-30T170000Z.md");
            Comment comment = new Comment(
                commentId,
                new StoryId("STORY-001"),
                "agent",
                "Special chars: @#$%^&*() \"quotes\" 'apostrophes' <tags>",
                CommentType.NOTE
            );

            // When
            commentRepository.save(comment);

            // Then
            Optional<Comment> retrieved = commentRepository.findById(commentId);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getContent())
                .isEqualTo("Special chars: @#$%^&*() \"quotes\" 'apostrophes' <tags>");
        }
    }

    @Nested
    @DisplayName("Multiple Comments Tests")
    class MultipleCommentsTests {

        @Test
        @DisplayName("Should handle multiple comments for same story")
        void shouldHandleMultipleCommentsForSameStory() {
            // Given
            for (int i = 1; i <= 5; i++) {
                CommentId commentId = new CommentId(
                    String.format("comment__agent__2026-03-30T18%02d00Z.md", i)
                );
                Comment comment = new Comment(
                    commentId,
                    new StoryId("STORY-001"),
                    "agent",
                    "Comment " + i,
                    CommentType.NOTE
                );
                commentRepository.save(comment);
            }

            // When
            List<Comment> comments = commentRepository.findByStoryId(new StoryId("STORY-001"));

            // Then
            assertThat(comments).hasSize(5);
        }

        @Test
        @DisplayName("Should maintain comment independence")
        void shouldMaintainCommentIndependence() {
            // Given
            CommentId id1 = new CommentId("comment__agent__2026-03-30T190000Z.md");
            CommentId id2 = new CommentId("comment__human-reviewer__2026-03-30T191000Z.md");

            Comment comment1 = new Comment(id1, new StoryId("STORY-001"), "agent", "Q1", CommentType.QUESTION);
            Comment comment2 = new Comment(id2, new StoryId("STORY-001"), "human-reviewer", "F1", CommentType.FEEDBACK);

            comment1.resolve("human-reviewer");

            commentRepository.save(comment1);
            commentRepository.save(comment2);

            // When
            Optional<Comment> retrieved1 = commentRepository.findById(id1);
            Optional<Comment> retrieved2 = commentRepository.findById(id2);

            // Then
            assertThat(retrieved1.get().isResolved()).isTrue();
            assertThat(retrieved2.get().isResolved()).isFalse();
        }
    }
}
