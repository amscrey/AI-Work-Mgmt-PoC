package com.aiworkflow.workmanagement.infrastructure.persistence;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for FileBasedActivityLogger.
 * Tests actual file system operations with JSON Lines log files.
 *
 * <p><b>Test Workspace:</b> {@code target/test-workspace-activity-log}</p>
 *
 * <p>These integration tests verify activity logging with JSON Lines format.
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
 *   <li>Activity log directory creation (workspace/activity-log/)</li>
 *   <li>Per-role log file creation ({role}.jsonl)</li>
 *   <li>JSON Lines format (one JSON object per line)</li>
 *   <li>Log entry appending (multiple entries per file)</li>
 *   <li>Success/failure outcome logging</li>
 *   <li>Timestamp, role, activity, storyId persistence</li>
 *   <li>Details map serialization</li>
 *   <li>Error information in failure logs</li>
 *   <li>Multiple roles with independent log files</li>
 * </ul>
 *
 * @see com.aiworkflow.workmanagement.infrastructure.persistence.FileBasedActivityLogger
 */
@DisplayName("FileBasedActivityLogger Integration Tests")
class FileBasedActivityLoggerIT {

    private static final String TEST_WORKSPACE = "target/test-workspace-activity-log";
    private FileBasedActivityLogger logger;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up test workspace from previous run (if exists)
        cleanUpWorkspace();
        logger = new FileBasedActivityLogger(TEST_WORKSPACE);
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
    @DisplayName("Log Directory and File Creation")
    class DirectoryAndFileTests {

        @Test
        @DisplayName("Should create activity-log directory on initialization")
        void shouldCreateActivityLogDirectory() {
            // When - logger is initialized in @BeforeEach

            // Then
            Path logDir = Paths.get(TEST_WORKSPACE, "activity-log");
            assertThat(Files.exists(logDir)).isTrue();
            assertThat(Files.isDirectory(logDir)).isTrue();
        }

        @Test
        @DisplayName("Should create role-specific log file on first log entry")
        void shouldCreateRoleSpecificLogFile() {
            // Given
            Map<String, Object> details = new HashMap<>();
            details.put("title", "Test Story");

            // When
            logger.logSuccess("agent", "create-story", "STORY-001", details);

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            assertThat(Files.exists(logFile)).isTrue();
            assertThat(Files.isRegularFile(logFile)).isTrue();
        }

        @Test
        @DisplayName("Should create separate log files for different roles")
        void shouldCreateSeparateLogFilesForRoles() {
            // When
            logger.logSuccess("agent", "create-story", "STORY-001", null);
            logger.logSuccess("human-reviewer", "approve-story", "STORY-001", null);
            logger.logSuccess("product-owner", "prioritize-story", "STORY-001", null);

            // Then
            Path agentLog = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            Path reviewerLog = Paths.get(TEST_WORKSPACE, "activity-log", "human-reviewer.jsonl");
            Path ownerLog = Paths.get(TEST_WORKSPACE, "activity-log", "product-owner.jsonl");

            assertThat(Files.exists(agentLog)).isTrue();
            assertThat(Files.exists(reviewerLog)).isTrue();
            assertThat(Files.exists(ownerLog)).isTrue();
        }
    }

    @Nested
    @DisplayName("JSON Lines Format")
    class JsonLinesFormatTests {

        @Test
        @DisplayName("Should write log entries in JSON Lines format")
        void shouldWriteJsonLinesFormat() throws IOException {
            // Given
            Map<String, Object> details = new HashMap<>();
            details.put("title", "Feature X");
            details.put("priority", "high");

            // When
            logger.logSuccess("agent", "create-story", "STORY-001", details);

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            List<String> lines = Files.readAllLines(logFile);

            assertThat(lines).hasSize(1);
            String jsonLine = lines.get(0);

            // Verify it's valid JSON (single line, no newlines inside)
            assertThat(jsonLine).doesNotContain("\n");
            assertThat(jsonLine).startsWith("{");
            assertThat(jsonLine).endsWith("}");

            // Verify key fields are present
            assertThat(jsonLine).contains("\"timestamp\"");
            assertThat(jsonLine).contains("\"role\":\"agent\"");
            assertThat(jsonLine).contains("\"activity\":\"create-story\"");
            assertThat(jsonLine).contains("\"storyId\":\"STORY-001\"");
            assertThat(jsonLine).contains("\"outcome\":\"success\"");
            assertThat(jsonLine).contains("\"title\":\"Feature X\"");
            assertThat(jsonLine).contains("\"priority\":\"high\"");
        }

        @Test
        @DisplayName("Should append multiple entries to same file")
        void shouldAppendMultipleEntries() throws IOException {
            // When
            logger.logSuccess("agent", "create-story", "STORY-001", Map.of("action", "create"));
            logger.logSuccess("agent", "add-task", "STORY-001", Map.of("action", "task"));
            logger.logSuccess("agent", "complete-task", "STORY-001", Map.of("action", "complete"));

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            List<String> lines = Files.readAllLines(logFile);

            assertThat(lines).hasSize(3);
            assertThat(lines.get(0)).contains("\"activity\":\"create-story\"");
            assertThat(lines.get(1)).contains("\"activity\":\"add-task\"");
            assertThat(lines.get(2)).contains("\"activity\":\"complete-task\"");
        }

        @Test
        @DisplayName("Should use compact JSON format without indentation")
        void shouldUseCompactJsonFormat() throws IOException {
            // When
            logger.logSuccess("agent", "test-activity", "STORY-001", Map.of("key", "value"));

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            String content = Files.readString(logFile);

            // Compact JSON should be on a single line
            long lineCount = content.lines().count();
            assertThat(lineCount).isEqualTo(1); // Only one line (plus trailing newline = 2 total lines in file)

            // Should not have excessive whitespace
            assertThat(content).doesNotContain("  \""); // No double-space indentation
        }
    }

    @Nested
    @DisplayName("Log Entry Content")
    class LogEntryContentTests {

        @Test
        @DisplayName("Should persist all log entry fields")
        void shouldPersistAllFields() throws IOException {
            // Given
            Map<String, Object> details = new HashMap<>();
            details.put("title", "New Feature");
            details.put("priority", 1);
            details.put("estimated_hours", 8);

            // When
            logger.log("agent", "create-story", "STORY-123", details, "success");

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            String jsonLine = Files.readAllLines(logFile).get(0);

            // Verify all fields
            assertThat(jsonLine).contains("\"timestamp\"");
            assertThat(jsonLine).contains("\"role\":\"agent\"");
            assertThat(jsonLine).contains("\"activity\":\"create-story\"");
            assertThat(jsonLine).contains("\"storyId\":\"STORY-123\"");
            assertThat(jsonLine).contains("\"outcome\":\"success\"");
            assertThat(jsonLine).contains("\"title\":\"New Feature\"");
            assertThat(jsonLine).contains("\"priority\":1");
            assertThat(jsonLine).contains("\"estimated_hours\":8");
        }

        @Test
        @DisplayName("Should handle null storyId")
        void shouldHandleNullStoryId() throws IOException {
            // When
            logger.logSuccess("system", "cleanup-task", null, Map.of("files_deleted", 5));

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "system.jsonl");
            String jsonLine = Files.readAllLines(logFile).get(0);

            assertThat(jsonLine).contains("\"storyId\":null");
            assertThat(jsonLine).contains("\"activity\":\"cleanup-task\"");
        }

        @Test
        @DisplayName("Should handle empty details map")
        void shouldHandleEmptyDetails() throws IOException {
            // When
            logger.logSuccess("agent", "ping", "STORY-001", new HashMap<>());

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            String jsonLine = Files.readAllLines(logFile).get(0);

            assertThat(jsonLine).contains("\"details\":{}");
        }

        @Test
        @DisplayName("Should handle null details map")
        void shouldHandleNullDetails() throws IOException {
            // When
            logger.logSuccess("agent", "ping", "STORY-001", null);

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            String jsonLine = Files.readAllLines(logFile).get(0);

            assertThat(jsonLine).contains("\"details\":{}");
        }
    }

    @Nested
    @DisplayName("Success and Failure Logging")
    class SuccessAndFailureTests {

        @Test
        @DisplayName("Should log success with outcome=success")
        void shouldLogSuccess() throws IOException {
            // When
            logger.logSuccess("agent", "create-story", "STORY-001", Map.of("title", "Test"));

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            String jsonLine = Files.readAllLines(logFile).get(0);

            assertThat(jsonLine).contains("\"outcome\":\"success\"");
            assertThat(jsonLine).contains("\"activity\":\"create-story\"");
        }

        @Test
        @DisplayName("Should log failure with outcome=failure and error details")
        void shouldLogFailure() throws IOException {
            // Given
            Map<String, Object> details = new HashMap<>();
            details.put("attempted_title", "Bad Story");

            // When
            logger.logFailure("agent", "create-story", "STORY-001",
                "Validation failed: title too short", details);

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            String jsonLine = Files.readAllLines(logFile).get(0);

            assertThat(jsonLine).contains("\"outcome\":\"failure\"");
            assertThat(jsonLine).contains("\"activity\":\"create-story\"");
            assertThat(jsonLine).contains("\"error\":\"Validation failed: title too short\"");
            assertThat(jsonLine).contains("\"attempted_title\":\"Bad Story\"");
        }

        @Test
        @DisplayName("Should log both success and failure for same role")
        void shouldLogMixedOutcomes() throws IOException {
            // When
            logger.logSuccess("agent", "create-story", "STORY-001", Map.of("title", "Good Story"));
            logger.logFailure("agent", "create-story", "STORY-002", "Duplicate ID", null);
            logger.logSuccess("agent", "create-story", "STORY-003", Map.of("title", "Another Good Story"));

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            List<String> lines = Files.readAllLines(logFile);

            assertThat(lines).hasSize(3);
            assertThat(lines.get(0)).contains("\"outcome\":\"success\"");
            assertThat(lines.get(1)).contains("\"outcome\":\"failure\"");
            assertThat(lines.get(2)).contains("\"outcome\":\"success\"");
        }
    }

    @Nested
    @DisplayName("Timestamp Handling")
    class TimestampTests {

        @Test
        @DisplayName("Should include ISO-8601 timestamp in log entry")
        void shouldIncludeTimestamp() throws IOException {
            // When
            logger.logSuccess("agent", "test-activity", "STORY-001", null);

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            String jsonLine = Files.readAllLines(logFile).get(0);

            // Verify ISO-8601 format: 2026-03-31T14:30:00Z
            assertThat(jsonLine).containsPattern("\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
        }
    }

    @Nested
    @DisplayName("Multiple Roles")
    class MultipleRolesTests {

        @Test
        @DisplayName("Should maintain separate logs for multiple roles")
        void shouldMaintainSeparateLogsForRoles() throws IOException {
            // When
            logger.logSuccess("agent", "create-story", "STORY-001", Map.of("source", "agent"));
            logger.logSuccess("human-reviewer", "review-story", "STORY-001", Map.of("source", "reviewer"));
            logger.logSuccess("agent", "add-task", "STORY-001", Map.of("source", "agent"));

            // Then
            Path agentLog = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            Path reviewerLog = Paths.get(TEST_WORKSPACE, "activity-log", "human-reviewer.jsonl");

            List<String> agentLines = Files.readAllLines(agentLog);
            List<String> reviewerLines = Files.readAllLines(reviewerLog);

            assertThat(agentLines).hasSize(2);
            assertThat(reviewerLines).hasSize(1);

            assertThat(agentLines.get(0)).contains("\"activity\":\"create-story\"");
            assertThat(agentLines.get(1)).contains("\"activity\":\"add-task\"");
            assertThat(reviewerLines.get(0)).contains("\"activity\":\"review-story\"");
        }

        @Test
        @DisplayName("Should handle role names with special characters")
        void shouldHandleRoleNamesWithSpecialCharacters() throws IOException {
            // When
            logger.logSuccess("ai-agent-v2", "process-story", "STORY-001", null);
            logger.logSuccess("human_reviewer_senior", "approve-story", "STORY-001", null);

            // Then
            Path aiAgentLog = Paths.get(TEST_WORKSPACE, "activity-log", "ai-agent-v2.jsonl");
            Path seniorReviewerLog = Paths.get(TEST_WORKSPACE, "activity-log", "human_reviewer_senior.jsonl");

            assertThat(Files.exists(aiAgentLog)).isTrue();
            assertThat(Files.exists(seniorReviewerLog)).isTrue();
        }
    }

    @Nested
    @DisplayName("Complex Details Scenarios")
    class ComplexDetailsTests {

        @Test
        @DisplayName("Should serialize complex nested details")
        void shouldSerializeComplexDetails() throws IOException {
            // Given
            Map<String, Object> details = new HashMap<>();
            details.put("title", "Complex Story");
            details.put("tags", List.of("backend", "api", "database"));
            details.put("metadata", Map.of("priority", "high", "estimated_days", 5));

            // When
            logger.logSuccess("agent", "create-story", "STORY-001", details);

            // Then
            Path logFile = Paths.get(TEST_WORKSPACE, "activity-log", "agent.jsonl");
            String jsonLine = Files.readAllLines(logFile).get(0);

            assertThat(jsonLine).contains("\"title\":\"Complex Story\"");
            assertThat(jsonLine).contains("\"tags\":[\"backend\",\"api\",\"database\"]");
            assertThat(jsonLine).contains("\"priority\":\"high\"");
            assertThat(jsonLine).contains("\"estimated_days\":5");
        }
    }
}
