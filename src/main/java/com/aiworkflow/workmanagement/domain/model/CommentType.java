package com.aiworkflow.workmanagement.domain.model;

/**
 * Represents the type of comment in the workflow system.
 * Categorizes comments for filtering and display purposes.
 */
public enum CommentType {
    /**
     * General note or observation
     */
    NOTE,

    /**
     * Question requiring response
     */
    QUESTION,

    /**
     * Feedback on work performed
     */
    FEEDBACK,

    /**
     * Issue or problem identified
     */
    ISSUE,

    /**
     * Resolution or fix applied
     */
    RESOLUTION,

    /**
     * State transition notification
     */
    STATE_CHANGE,

    /**
     * System-generated comment
     */
    SYSTEM;

    /**
     * Determines if this comment type requires a response.
     */
    public boolean requiresResponse() {
        return this == QUESTION || this == ISSUE;
    }

    /**
     * Determines if this is a system-generated comment.
     */
    public boolean isSystemGenerated() {
        return this == SYSTEM || this == STATE_CHANGE;
    }

    /**
     * Determines if this is a user-generated comment.
     */
    public boolean isUserGenerated() {
        return !isSystemGenerated();
    }
}
