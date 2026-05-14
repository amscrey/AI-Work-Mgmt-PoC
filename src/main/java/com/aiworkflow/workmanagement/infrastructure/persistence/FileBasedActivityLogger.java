package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * File-based implementation of ActivityLogger.
 * Writes activity logs in JSON Lines format (one JSON object per line).
 *
 * <p><b>File Organization:</b></p>
 * <ul>
 *   <li>Log directory: {@code workspace/activity-log/}</li>
 *   <li>Per-role files: {@code activity-log/{role}.jsonl}</li>
 *   <li>Format: JSON Lines (newline-delimited JSON)</li>
 * </ul>
 *
 * <p><b>Log Entry Format:</b></p>
 * <pre>
 * {"timestamp":"2026-03-31T14:30:00Z","role":"agent","activity":"create-story",
 *  "storyId":"STORY-001","outcome":"success","details":{"title":"Feature X"}}
 * </pre>
 *
 * <p>JSON Lines format benefits:</p>
 * <ul>
 *   <li>Easy to parse (one line = one record)</li>
 *   <li>Append-friendly (no need to parse entire file)</li>
 *   <li>Stream-processable</li>
 *   <li>Human-readable when needed</li>
 * </ul>
 *
 * @see ActivityLogger
 */
public class FileBasedActivityLogger implements ActivityLogger {

    private final Path activityLogDir;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new FileBasedActivityLogger.
     *
     * @param workspacePath The workspace root directory path
     */
    public FileBasedActivityLogger(String workspacePath) {
        this.activityLogDir = Paths.get(workspacePath, "activity-log");
        this.objectMapper = createObjectMapper();
        initializeLogDirectory();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.INDENT_OUTPUT); // JSON Lines = compact, one-line format
        return mapper;
    }

    private void initializeLogDirectory() {
        try {
            Files.createDirectories(activityLogDir);
        } catch (IOException e) {
            throw new ActivityLoggerException("Failed to create activity log directory: " + activityLogDir, e);
        }
    }

    @Override
    public void log(String role, String activity, String storyId, Map<String, Object> details, String outcome) {
        ActivityLogEntry entry = new ActivityLogEntry(
            Instant.now(),
            role,
            activity,
            storyId,
            outcome,
            details != null ? details : new HashMap<>()
        );

        writeLogEntry(role, entry);
    }

    @Override
    public void logSuccess(String role, String activity, String storyId, Map<String, Object> details) {
        log(role, activity, storyId, details, "success");
    }

    @Override
    public void logFailure(String role, String activity, String storyId, String error, Map<String, Object> details) {
        Map<String, Object> enhancedDetails = details != null ? new HashMap<>(details) : new HashMap<>();
        enhancedDetails.put("error", error);
        log(role, activity, storyId, enhancedDetails, "failure");
    }

    private void writeLogEntry(String role, ActivityLogEntry entry) {
        Path logFile = activityLogDir.resolve(role + ".jsonl");

        try {
            // Convert entry to single-line JSON
            String jsonLine = objectMapper.writeValueAsString(entry) + "\n";

            // Append to log file (create if doesn't exist)
            Files.writeString(
                logFile,
                jsonLine,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new ActivityLoggerException("Failed to write activity log entry for role: " + role, e);
        }
    }

    /**
     * Internal DTO for activity log entries.
     */
    private static class ActivityLogEntry {
        private final Instant timestamp;
        private final String role;
        private final String activity;
        private final String storyId;
        private final String outcome;
        private final Map<String, Object> details;

        ActivityLogEntry(Instant timestamp, String role, String activity, String storyId,
                        String outcome, Map<String, Object> details) {
            this.timestamp = timestamp;
            this.role = role;
            this.activity = activity;
            this.storyId = storyId;
            this.outcome = outcome;
            this.details = details;
        }

        // Getters for Jackson serialization
        public Instant getTimestamp() { return timestamp; }
        public String getRole() { return role; }
        public String getActivity() { return activity; }
        public String getStoryId() { return storyId; }
        public String getOutcome() { return outcome; }
        public Map<String, Object> getDetails() { return details; }
    }

    /**
     * Exception thrown when activity logging fails.
     */
    public static class ActivityLoggerException extends RuntimeException {
        public ActivityLoggerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
