package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.model.CommentType;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Verification test to demonstrate that integration tests DO create
 * workspace directories and files during execution.
 *
 * This test intentionally does NOT clean up in @AfterEach so you can
 * inspect the created directories and files.
 *
 * Run this test and then check target/test-workspace-verification/
 * to see the created structure.
 */
@DisplayName("Workspace Verification Test (Leaves Files for Inspection)")
class WorkspaceVerificationIT {

    private static final String TEST_WORKSPACE = "target/test-workspace-verification";
    private FileBasedStoryRepository storyRepository;
    private FileBasedTaskRepository taskRepository;
    private FileBasedCommentRepository commentRepository;
    private FileBasedActivityLogger activityLogger;

    @BeforeEach
    void setUp() throws IOException {
        cleanUpWorkspace();
        storyRepository = new FileBasedStoryRepository(TEST_WORKSPACE);
        taskRepository = new FileBasedTaskRepository(TEST_WORKSPACE);
        commentRepository = new FileBasedCommentRepository(TEST_WORKSPACE);
        activityLogger = new FileBasedActivityLogger(TEST_WORKSPACE);
    }

    // INTENTIONALLY NO @AfterEach cleanup - leaves files for inspection!

    @AfterAll
    static void afterAll() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("WORKSPACE VERIFICATION TEST COMPLETE");
        System.out.println("=".repeat(80));
        System.out.println("Check the following directory to see created files:");
        System.out.println("  " + Paths.get(TEST_WORKSPACE).toAbsolutePath());
        System.out.println("\nExpected structure:");
        System.out.println("  " + TEST_WORKSPACE + "/");
        System.out.println("    backlog/");
        System.out.println("      STORY-9001/");
        System.out.println("        story.json");
        System.out.println("        tasks/");
        System.out.println("          TASK-9001.json");
        System.out.println("        comments/");
        System.out.println("          comment__agent__2026-03-31T120000Z.md");
        System.out.println("    prioritized/");
        System.out.println("      STORY-9002/");
        System.out.println("        story.json");
        System.out.println("    activity-log/");
        System.out.println("      agent.jsonl");
        System.out.println("      human-reviewer.jsonl");
        System.out.println("=".repeat(80));
        System.out.println();
    }

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

    @Test
    @DisplayName("Create complete workspace with all entity types")
    void shouldCreateCompleteWorkspace() throws IOException {
        // Create Story in BACKLOG
        Story story1 = new Story(
            new StoryId("STORY-9001"),
            "Verification Story 1",
            "This demonstrates workspace creation",
            WorkflowState.TODO,
            PrioritizationState.BACKLOG
        );
        storyRepository.save(story1);

        // Create Story in PRIORITIZED
        Story story2 = new Story(
            new StoryId("STORY-9002"),
            "Verification Story 2",
            "Another story in different prioritization",
            WorkflowState.IN_PROGRESS,
            PrioritizationState.PRIORITIZED
        );
        storyRepository.save(story2);

        // Create Task for story1
        Task task1 = new Task(
            new TaskId("TASK-9001"),
            new StoryId("STORY-9001"),
            "Verification Task"
        );
        task1.updateDescription("This task demonstrates task file creation");
        task1.assignTo("agent");
        task1.setEstimate(5);
        taskRepository.save(task1);

        // Create Comment for story1
        CommentId commentId = new CommentId("comment__agent__2026-03-31T120000Z.md");
        Comment comment = new Comment(
            commentId,
            new StoryId("STORY-9001"),
            "agent",
            "This comment demonstrates markdown file creation with YAML frontmatter",
            CommentType.NOTE
        );
        commentRepository.save(comment);

        // Create Activity Logs
        activityLogger.logSuccess("agent", "create_story", "STORY-9001",
            java.util.Map.of("title", "Verification Story 1", "action", "test"));
        activityLogger.logSuccess("human-reviewer", "review_story", "STORY-9001",
            java.util.Map.of("status", "approved", "action", "test"));

        // Verify all files exist
        Path backlogStory = Paths.get(TEST_WORKSPACE, "backlog", "STORY-9001", "story.json");
        assertThat(Files.exists(backlogStory)).isTrue();
        System.out.println("✅ Created: " + backlogStory);

        Path prioritizedStory = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-9002", "story.json");
        assertThat(Files.exists(prioritizedStory)).isTrue();
        System.out.println("✅ Created: " + prioritizedStory);

        Path taskFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-9001", "tasks", "TASK-9001.json");
        assertThat(Files.exists(taskFile)).isTrue();
        System.out.println("✅ Created: " + taskFile);

        Path commentFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-9001", "comments", "comment__agent__2026-03-31T120000Z.md");
        assertThat(Files.exists(commentFile)).isTrue();
        System.out.println("✅ Created: " + commentFile);

        Path agentLog = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
        assertThat(Files.exists(agentLog)).isTrue();
        System.out.println("✅ Created: " + agentLog);

        Path reviewerLog = Paths.get(TEST_WORKSPACE, "activity-log", "human-reviewer.jsonl");
        assertThat(Files.exists(reviewerLog)).isTrue();
        System.out.println("✅ Created: " + reviewerLog);

        // Print file contents for inspection
        System.out.println("\n📄 Story JSON:");
        System.out.println(Files.readString(backlogStory));

        System.out.println("\n📄 Task JSON:");
        System.out.println(Files.readString(taskFile));

        System.out.println("\n📄 Comment Markdown:");
        System.out.println(Files.readString(commentFile));

        System.out.println("\n📄 Activity Log (agent):");
        System.out.println(Files.readString(agentLog));

        System.out.println("\n📄 Activity Log (human-reviewer):");
        System.out.println(Files.readString(reviewerLog));
    }
}
