package com.aiworkflow.workmanagement.application.service;

import java.util.Map;

/**
 * Service for logging agent activities.
 * Implementations will write to JSON Lines log files.
 */
public interface ActivityLogger {

    /**
     * Logs an activity with outcome.
     *
     * @param role The role performing the activity
     * @param activity The activity being performed
     * @param storyId The story ID (if applicable)
     * @param details Additional details
     * @param outcome The outcome (success/failure)
     */
    void log(String role, String activity, String storyId, Map<String, Object> details, String outcome);

    /**
     * Logs a successful activity.
     *
     * @param role The role performing the activity
     * @param activity The activity being performed
     * @param storyId The story ID (if applicable)
     * @param details Additional details
     */
    void logSuccess(String role, String activity, String storyId, Map<String, Object> details);

    /**
     * Logs a failed activity.
     *
     * @param role The role performing the activity
     * @param activity The activity being performed
     * @param storyId The story ID (if applicable)
     * @param error The error message
     * @param details Additional details
     */
    void logFailure(String role, String activity, String storyId, String error, Map<String, Object> details);
}
