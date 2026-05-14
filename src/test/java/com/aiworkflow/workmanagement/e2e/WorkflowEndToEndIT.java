package com.aiworkflow.workmanagement.e2e;

import com.aiworkflow.workmanagement.application.command.CreateCommentCommand;
import com.aiworkflow.workmanagement.application.command.CreateStoryCommand;
import com.aiworkflow.workmanagement.application.command.CreateTaskCommand;
import com.aiworkflow.workmanagement.application.command.TransitionStoryCommand;
import com.aiworkflow.workmanagement.application.service.CommentService;
import com.aiworkflow.workmanagement.application.service.StoryService;
import com.aiworkflow.workmanagement.application.service.TaskService;
import com.aiworkflow.workmanagement.application.service.impl.CommentServiceImpl;
import com.aiworkflow.workmanagement.application.service.impl.StoryServiceImpl;
import com.aiworkflow.workmanagement.application.service.impl.TaskServiceImpl;
import com.aiworkflow.workmanagement.domain.model.*;
import com.aiworkflow.workmanagement.domain.repository.CommentRepository;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.service.StateTransitionValidator;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import com.aiworkflow.workmanagement.infrastructure.persistence.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-End Integration Tests for complete workflows.
 * Tests full stack from service layer → repositories → file system → activity logging.
 *
 * <p><b>Test Workspace:</b> {@code target/test-workspace-e2e}</p>
 *
 * <p>These E2E tests verify complete user scenarios with real components:
 * - Service layer (StoryService, TaskService, CommentService)
 * - Repository layer (FileBasedStoryRepository, FileBasedTaskRepository, FileBasedCommentRepository)
 * - Activity logging (FileBasedActivityLogger)
 * - File system persistence (JSON, Markdown, JSON Lines)
 * </p>
 *
 * <p><b>For Developers (Human & AI):</b></p>
 * <ul>
 *   <li>📖 See {@code etc/docs/TESTING.md} for complete testing guide</li>
 *   <li>🔍 Test artifacts LEFT in target/test-workspace-e2e/ for inspection</li>
 *   <li>⚠️ When adding new workflows, update etc/docs/TESTING.md</li>
 * </ul>
 *
 * <p><b>What This Test Suite Verifies:</b></p>
 * <ul>
 *   <li>Complete story creation workflow (story → tasks → comments)</li>
 *   <li>Task lifecycle (create → assign → start → complete)</li>
 *   <li>Comment workflows (questions → feedback → resolution)</li>
 *   <li>Prioritization state transitions (backlog → prioritized → deprioritized)</li>
 *   <li>Activity logging across all operations</li>
 *   <li>Multi-entity coordination and file structure</li>
 *   <li>Cross-repository operations</li>
 *   <li>Error scenarios and validation</li>
 * </ul>
 */
@DisplayName("End-to-End Workflow Integration Tests")
class WorkflowEndToEndIT {

    private static final String TEST_WORKSPACE = "target/test-workspace-e2e";

    // Services (application layer)
    private StoryService storyService;
    private TaskService taskService;
    private CommentService commentService;

    // Repositories (infrastructure layer)
    private FileBasedStoryRepository storyRepository;
    private FileBasedTaskRepository taskRepository;
    private FileBasedCommentRepository commentRepository;
    private FileBasedActivityLogger activityLogger;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up test workspace from previous run (if exists)
        cleanUpWorkspace();

        // Create infrastructure layer (repositories + activity logger)
        storyRepository = new FileBasedStoryRepository(TEST_WORKSPACE);
        taskRepository = new FileBasedTaskRepository(TEST_WORKSPACE);
        commentRepository = new FileBasedCommentRepository(TEST_WORKSPACE);
        activityLogger = new FileBasedActivityLogger(TEST_WORKSPACE);

        // Create domain services
        StateTransitionValidator stateTransitionValidator = new StateTransitionValidator();

        // Create application layer (services)
        storyService = new StoryServiceImpl(storyRepository, stateTransitionValidator, activityLogger);
        taskService = new TaskServiceImpl(taskRepository, storyRepository, activityLogger);
        commentService = new CommentServiceImpl(commentRepository, storyRepository, activityLogger);
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
    @DisplayName("Complete Story Workflow Tests")
    class StoryWorkflowTests {

        @Test
        @DisplayName("Should complete full story creation workflow with tasks and comments")
        void shouldCompleteFullStoryWorkflow() throws IOException {
            // GIVEN: A new feature request from product owner
            CreateStoryCommand storyCommand = new CreateStoryCommand(
                "STORY-1001",
                "Implement user authentication",
                "Add OAuth 2.0 login functionality",
                null, // description
                "product-owner", // author
                WorkflowState.TODO,
                PrioritizationState.PRIORITIZED,
                null, // acceptance criteria
                null, // tags
                13    // estimated effort
            );

            // WHEN: Product owner creates the story
            Story story = storyService.createStory(storyCommand);

            // THEN: Story is created and persisted
            assertThat(story).isNotNull();
            assertThat(story.getId().getValue()).isEqualTo("STORY-1001");
            assertThat(story.getWorkflowState()).isEqualTo(WorkflowState.TODO);

            // Verify story file exists
            Path storyFile = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-1001", "story.json");
            assertThat(Files.exists(storyFile)).isTrue();

            // WHEN: Agent adds tasks to the story
            CreateTaskCommand task1Command = new CreateTaskCommand(
                "TASK-1001",
                "STORY-1001",
                "Setup OAuth provider configuration",
                "Configure OAuth client ID and secret",
                "agent",
                5
            );
            Task task1 = taskService.createTask(task1Command);

            CreateTaskCommand task2Command = new CreateTaskCommand(
                "TASK-1002",
                "STORY-1001",
                "Implement login endpoint",
                "Create /api/auth/login endpoint",
                "agent",
                8
            );
            Task task2 = taskService.createTask(task2Command);

            // THEN: Tasks are created and linked to story
            assertThat(task1).isNotNull();
            assertThat(task2).isNotNull();

            Path task1File = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-1001", "tasks", "TASK-1001.json");
            Path task2File = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-1001", "tasks", "TASK-1002.json");
            assertThat(Files.exists(task1File)).isTrue();
            assertThat(Files.exists(task2File)).isTrue();

            // WHEN: Agent asks a question
            CreateCommentCommand questionCommand = new CreateCommentCommand(
                "comment__agent__2026-03-31T140000Z.md",
                "STORY-1001",
                "agent",
                CommentType.QUESTION,
                "Should we support Google and GitHub OAuth providers?"
            );
            Comment question = commentService.createComment(questionCommand);

            // THEN: Question comment is created
            assertThat(question).isNotNull();
            Path questionFile = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-1001", "comments",
                "comment__agent__2026-03-31T140000Z.md");
            assertThat(Files.exists(questionFile)).isTrue();

            // WHEN: Human reviewer provides feedback
            CreateCommentCommand feedbackCommand = new CreateCommentCommand(
                "comment__human-reviewer__2026-03-31T141000Z.md",
                "STORY-1001",
                "human-reviewer",
                CommentType.FEEDBACK,
                "Yes, both Google and GitHub. Also add unit tests for the login flow."
            );
            Comment feedback = commentService.createComment(feedbackCommand);

            // THEN: Feedback comment is created
            assertThat(feedback).isNotNull();

            // VERIFY: Activity log contains all operations
            Path activityLog = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            assertThat(Files.exists(activityLog)).isTrue();

            List<String> logLines = Files.readAllLines(activityLog);
            assertThat(logLines.size()).isGreaterThanOrEqualTo(3); // create_story, 2x create_task, create_comment
            assertThat(logLines).anyMatch(line -> line.contains("create_task"));
            assertThat(logLines).anyMatch(line -> line.contains("create_comment"));

            // VERIFY: Can retrieve all entities
            Story retrievedStory = storyService.findById(new StoryId("STORY-1001")).orElseThrow();
            assertThat(retrievedStory.getTitle()).isEqualTo("Implement user authentication");

            List<Task> storyTasks = taskService.findByStoryId(new StoryId("STORY-1001"));
            assertThat(storyTasks).hasSize(2);

            List<Comment> storyComments = commentService.findByStoryId(new StoryId("STORY-1001"));
            assertThat(storyComments).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Task Lifecycle Workflow Tests")
    class TaskLifecycleTests {

        @Test
        @DisplayName("Should complete full task lifecycle from creation to completion")
        void shouldCompleteTaskLifecycle() throws IOException {
            // SETUP: Create parent story
            CreateStoryCommand storyCommand = new CreateStoryCommand(
                "STORY-2001",
                "Refactor database layer",
                "Improve query performance",
                "product-owner",
                PrioritizationState.PRIORITIZED
            );
            storyService.createStory(storyCommand);

            // WHEN: Agent creates task
            CreateTaskCommand taskCommand = new CreateTaskCommand(
                "TASK-2001",
                "STORY-2001",
                "Add database indexes",
                "Create indexes on frequently queried columns",
                "agent",
                5
            );
            Task task = taskService.createTask(taskCommand);

            // THEN: Task starts in PENDING state
            assertThat(task.getState()).isEqualTo(TaskState.PENDING);
            assertThat(task.getAssignedTo()).isEqualTo("agent");

            // WHEN: Agent starts working on the task
            task = taskService.transitionTask(new TaskId("TASK-2001"), TaskState.IN_PROGRESS);

            // THEN: Task is now IN_PROGRESS
            assertThat(task.getState()).isEqualTo(TaskState.IN_PROGRESS);
            assertThat(task.isInProgress()).isTrue();

            // WHEN: Agent completes the task
            task = taskService.transitionTask(new TaskId("TASK-2001"), TaskState.COMPLETED);

            // THEN: Task is COMPLETED with timestamp
            assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
            assertThat(task.isCompleted()).isTrue();
            assertThat(task.getCompletedAt()).isNotNull();

            // VERIFY: Activity log shows state transitions
            Path activityLog = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            List<String> logLines = Files.readAllLines(activityLog);

            assertThat(logLines).anyMatch(line ->
                line.contains("transition_task") && line.contains("IN_PROGRESS"));
            assertThat(logLines).anyMatch(line ->
                line.contains("transition_task") && line.contains("COMPLETED"));

            // VERIFY: Task file reflects final state
            Path taskFile = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-2001", "tasks", "TASK-2001.json");
            String taskJson = Files.readString(taskFile);
            assertThat(taskJson).contains("\"state\" : \"COMPLETED\"");
            assertThat(taskJson).contains("\"completedAt\"");
        }

        @Test
        @DisplayName("Should handle task blocking scenario")
        void shouldHandleTaskBlocking() throws IOException {
            // SETUP: Create story and task
            CreateStoryCommand storyCommand = new CreateStoryCommand(
                "STORY-2002",
                "Deploy to production",
                "Production deployment",
                "product-owner",
                PrioritizationState.PRIORITIZED
            );
            storyService.createStory(storyCommand);

            CreateTaskCommand taskCommand = new CreateTaskCommand(
                "TASK-2002",
                "STORY-2002",
                "Run production deployment",
                null,
                "agent",
                null
            );
            Task task = taskService.createTask(taskCommand);

            // Start the task
            task = taskService.transitionTask(new TaskId("TASK-2002"), TaskState.IN_PROGRESS);

            // WHEN: Task becomes blocked
            task = taskService.transitionTask(new TaskId("TASK-2002"), TaskState.BLOCKED);

            // THEN: Task is in BLOCKED state
            assertThat(task.getState()).isEqualTo(TaskState.BLOCKED);

            // WHEN: Blocker is resolved, resume work
            task = taskService.transitionTask(new TaskId("TASK-2002"), TaskState.IN_PROGRESS);

            // THEN: Task returns to IN_PROGRESS
            assertThat(task.getState()).isEqualTo(TaskState.IN_PROGRESS);

            // VERIFY: All transitions logged
            Path activityLog = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            List<String> logLines = Files.readAllLines(activityLog);

            assertThat(logLines).anyMatch(line -> line.contains("BLOCKED"));
            assertThat(logLines.stream().filter(line -> line.contains("IN_PROGRESS")).count())
                .isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Prioritization Workflow Tests")
    class PrioritizationWorkflowTests {

        @Test
        @Disabled("changePrioritization not yet implemented in Story entity")
        @DisplayName("Should handle story prioritization changes with directory moves")
        void shouldHandlePrioritizationChanges() throws IOException {
            // WHEN: Create story in BACKLOG
            CreateStoryCommand storyCommand = new CreateStoryCommand(
                "STORY-3001",
                "Add email notifications",
                "Send email on important events",
                "product-owner",
                PrioritizationState.BACKLOG
            );
            Story story = storyService.createStory(storyCommand);

            // THEN: Story is in backlog directory
            Path backlogFile = Paths.get(TEST_WORKSPACE, "backlog", "STORY-3001", "story.json");
            assertThat(Files.exists(backlogFile)).isTrue();

            // WHEN: Story is prioritized
            story = storyService.changePrioritization(new StoryId("STORY-3001"), PrioritizationState.PRIORITIZED);

            // THEN: Story moved to prioritized directory
            Path prioritizedFile = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-3001", "story.json");
            assertThat(Files.exists(prioritizedFile)).isTrue();
            assertThat(Files.exists(backlogFile)).isFalse();

            // Add task to prioritized story
            CreateTaskCommand taskCommand = new CreateTaskCommand(
                "TASK-3001",
                "STORY-3001",
                "Setup email service",
                null,
                "agent",
                3
            );
            taskService.createTask(taskCommand);

            // Verify task is in prioritized directory
            Path taskFile = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-3001", "tasks", "TASK-3001.json");
            assertThat(Files.exists(taskFile)).isTrue();

            // WHEN: Story is deprioritized
            story = storyService.changePrioritization(new StoryId("STORY-3001"), PrioritizationState.DEPRIORITIZED);

            // THEN: Story moved to deprioritized directory (with tasks)
            Path deprioritizedFile = Paths.get(TEST_WORKSPACE, "deprioritized", "STORY-3001", "story.json");
            assertThat(Files.exists(deprioritizedFile)).isTrue();
            assertThat(Files.exists(prioritizedFile)).isFalse();

            Path movedTaskFile = Paths.get(TEST_WORKSPACE, "deprioritized", "STORY-3001", "tasks", "TASK-3001.json");
            assertThat(Files.exists(movedTaskFile)).isTrue();
        }
    }

    @Nested
    @DisplayName("Comment Workflow Tests")
    class CommentWorkflowTests {

        @Test
        @DisplayName("Should handle question-answer workflow with resolution")
        void shouldHandleQuestionAnswerWorkflow() throws IOException {
            // SETUP: Create story
            CreateStoryCommand storyCommand = new CreateStoryCommand(
                "STORY-4001",
                "Update API documentation",
                "Refresh OpenAPI specs",
                "product-owner",
                PrioritizationState.PRIORITIZED
            );
            storyService.createStory(storyCommand);

            // WHEN: Agent asks question
            CreateCommentCommand questionCommand = new CreateCommentCommand(
                "comment__agent__2026-03-31T150000Z.md",
                "STORY-4001",
                "agent",
                CommentType.QUESTION,
                "Should we include deprecated endpoints in the documentation?"
            );
            Comment question = commentService.createComment(questionCommand);

            // THEN: Question is unresolved
            assertThat(question.requiresResponse()).isTrue();
            assertThat(question.isResolved()).isFalse();

            // WHEN: Product owner provides answer
            CreateCommentCommand answerCommand = new CreateCommentCommand(
                "comment__product-owner__2026-03-31T151000Z.md",
                "STORY-4001",
                "product-owner",
                CommentType.FEEDBACK,
                "No, remove all deprecated endpoints from docs. They'll be removed in next version."
            );
            commentService.createComment(answerCommand);

            // WHEN: Question is resolved (load, resolve, save)
            CommentId questionId = new CommentId("comment__agent__2026-03-31T150000Z.md");
            Comment questionToResolve = commentRepository.findById(questionId).orElseThrow();
            questionToResolve.resolve("product-owner");
            commentRepository.save(questionToResolve);

            // THEN: Question is marked as resolved
            Comment resolvedComment = commentRepository.findById(questionId).orElseThrow();
            assertThat(resolvedComment.isResolved()).isTrue();

            // VERIFY: Comment markdown file shows resolved status
            Path commentFile = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-4001", "comments",
                "comment__agent__2026-03-31T150000Z.md");
            String content = Files.readString(commentFile);
            assertThat(content).contains("resolved: true");

            // VERIFY: Activity logs show all comment operations
            List<String> agentLog = Files.readAllLines(
                Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl"));
            assertThat(agentLog).anyMatch(line ->
                line.contains("create_comment") && line.contains("QUESTION"));

            List<String> poLog = Files.readAllLines(
                Paths.get(TEST_WORKSPACE, "activity-log", "product-owner.jsonl"));
            assertThat(poLog).anyMatch(line ->
                line.contains("create_comment") && line.contains("FEEDBACK"));
        }
    }

    @Nested
    @DisplayName("Multi-Entity Coordination Tests")
    class MultiEntityCoordinationTests {

        @Test
        @DisplayName("Should coordinate multiple entities in complex workflow")
        void shouldCoordinateMultipleEntities() throws IOException {
            // Scenario: Complete feature development workflow

            // 1. Product owner creates story
            Story story = storyService.createStory(new CreateStoryCommand(
                "STORY-5001",
                "Implement search feature",
                "Add full-text search to product catalog",
                "product-owner",
                PrioritizationState.PRIORITIZED
            ));

            // 2. Agent breaks down into tasks
            Task task1 = taskService.createTask(new CreateTaskCommand(
                "TASK-5001", "STORY-5001", "Setup Elasticsearch", null, "agent", 8));
            Task task2 = taskService.createTask(new CreateTaskCommand(
                "TASK-5002", "STORY-5001", "Implement search API", null, "agent", 8));
            Task task3 = taskService.createTask(new CreateTaskCommand(
                "TASK-5003", "STORY-5001", "Add search UI", null, "agent", 5));

            // 3. Assign story to agent
            Story assignedStory = storyRepository.findById(new StoryId("STORY-5001")).orElseThrow();
            assignedStory.assignTo("agent");
            storyRepository.save(assignedStory);

            // 4. Agent starts working on first task
            taskService.transitionTask(new TaskId("TASK-5001"), TaskState.IN_PROGRESS);

            // 5. Story transitions to IN_PROGRESS
            TransitionStoryCommand transitionCommand = new TransitionStoryCommand(
                "STORY-5001",
                WorkflowState.IN_PROGRESS,
                Role.AGENT
            );
            storyService.transitionStory(transitionCommand);

            // 6. Agent encounters issue and asks question
            commentService.createComment(new CreateCommentCommand(
                "comment__agent__2026-03-31T160000Z.md",
                "STORY-5001",
                "agent",
                CommentType.QUESTION,
                "Should we use Elasticsearch or our own search implementation?"
            ));

            // 7. Product owner provides feedback
            commentService.createComment(new CreateCommentCommand(
                "comment__product-owner__2026-03-31T161000Z.md",
                "STORY-5001",
                "product-owner",
                CommentType.FEEDBACK,
                "Use Elasticsearch - better performance and features."
            ));

            // 8. Agent completes first task
            taskService.transitionTask(new TaskId("TASK-5001"), TaskState.COMPLETED);

            // 9. Agent works on second task
            taskService.transitionTask(new TaskId("TASK-5002"), TaskState.IN_PROGRESS);
            taskService.transitionTask(new TaskId("TASK-5002"), TaskState.COMPLETED);

            // 10. Agent works on third task
            taskService.transitionTask(new TaskId("TASK-5003"), TaskState.IN_PROGRESS);
            taskService.transitionTask(new TaskId("TASK-5003"), TaskState.COMPLETED);

            // 11. Human reviewer reviews and approves
            commentService.createComment(new CreateCommentCommand(
                "comment__human-reviewer__2026-03-31T170000Z.md",
                "STORY-5001",
                "human-reviewer",
                CommentType.FEEDBACK,
                "Search feature looks great! Tested on staging. Approved for production."
            ));

            // 12. Story is completed
            TransitionStoryCommand doneCommand = new TransitionStoryCommand(
                "STORY-5001",
                WorkflowState.DONE,
                Role.HUMAN_REVIEWER
            );
            storyService.transitionStory(doneCommand);

            // VERIFY: Complete workspace structure created
            Path storyDir = Paths.get(TEST_WORKSPACE, "prioritized", "STORY-5001");
            assertThat(Files.exists(storyDir.resolve("story.json"))).isTrue();
            assertThat(Files.exists(storyDir.resolve("tasks/TASK-5001.json"))).isTrue();
            assertThat(Files.exists(storyDir.resolve("tasks/TASK-5002.json"))).isTrue();
            assertThat(Files.exists(storyDir.resolve("tasks/TASK-5003.json"))).isTrue();
            assertThat(Files.list(storyDir.resolve("comments")).count()).isEqualTo(3);

            // VERIFY: All roles have activity logs
            assertThat(Files.exists(Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl"))).isTrue();
            assertThat(Files.exists(Paths.get(TEST_WORKSPACE, "activity-log", "product-owner.jsonl"))).isTrue();
            assertThat(Files.exists(Paths.get(TEST_WORKSPACE, "activity-log", "human-reviewer.jsonl"))).isTrue();

            // VERIFY: Final state
            Story finalStory = storyService.findById(new StoryId("STORY-5001")).orElseThrow();
            assertThat(finalStory.getWorkflowState()).isEqualTo(WorkflowState.DONE);

            List<Task> allTasks = taskService.findByStoryId(new StoryId("STORY-5001"));
            assertThat(allTasks).hasSize(3);
            assertThat(allTasks).allMatch(Task::isCompleted);

            List<Comment> allComments = commentService.findByStoryId(new StoryId("STORY-5001"));
            assertThat(allComments).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Error Scenario Tests")
    class ErrorScenarioTests {

        @Test
        @DisplayName("Should handle operations on non-existent entities")
        void shouldHandleNonExistentEntities() {
            // Attempt to add task to non-existent story
            CreateTaskCommand taskCommand = new CreateTaskCommand(
                "TASK-9999",
                "STORY-9999",
                "Invalid task",
                null,
                "agent",
                null
            );

            assertThatThrownBy(() -> taskService.createTask(taskCommand))
                .isInstanceOf(TaskServiceImpl.StoryNotFoundException.class)
                .hasMessageContaining("STORY-9999");

            // Attempt to comment on non-existent story
            CreateCommentCommand commentCommand = new CreateCommentCommand(
                "comment__agent__2026-03-31T180000Z.md",
                "STORY-9999",
                "agent",
                CommentType.NOTE,
                "This should fail"
            );

            assertThatThrownBy(() -> commentService.createComment(commentCommand))
                .isInstanceOf(CommentServiceImpl.StoryNotFoundException.class)
                .hasMessageContaining("STORY-9999");
        }

        @Test
        @DisplayName("Should validate invalid state transitions")
        void shouldValidateInvalidTransitions() throws IOException {
            // Create story and task
            storyService.createStory(new CreateStoryCommand(
                "STORY-6001",
                "Test story",
                "Summary",
                "product-owner",
                PrioritizationState.BACKLOG
            ));

            taskService.createTask(new CreateTaskCommand(
                "TASK-6001",
                "STORY-6001",
                "Test task",
                null,
                "agent",
                null
            ));

            // Attempt invalid transition: PENDING → COMPLETED (must go through IN_PROGRESS)
            assertThatThrownBy(() ->
                taskService.transitionTask(new TaskId("TASK-6001"), TaskState.COMPLETED))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @AfterAll
    static void afterAll() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("END-TO-END INTEGRATION TESTS COMPLETE");
        System.out.println("=".repeat(80));
        System.out.println("Check the following directory to see complete workflow artifacts:");
        System.out.println("  " + Paths.get(TEST_WORKSPACE).toAbsolutePath());
        System.out.println("\nYou can inspect:");
        System.out.println("  - Story files (story.json)");
        System.out.println("  - Task files (tasks/*.json)");
        System.out.println("  - Comment files (comments/*.md)");
        System.out.println("  - Activity logs (activity-log/*.jsonl)");
        System.out.println("=".repeat(80));
        System.out.println();
    }
}
