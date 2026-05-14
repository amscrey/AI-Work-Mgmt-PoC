package com.aiworkflow.workmanagement.domain.model;

/**
 * Represents the role of an entity in the workflow system.
 * Used for access control and state transition validation.
 */
public enum Role {
    AGENT,
    HUMAN_REVIEWER,
    HUMAN_APPROVER,
    SYSTEM;

    /**
     * Determines if this role is an agent.
     */
    public boolean isAgent() {
        return this == AGENT;
    }

    /**
     * Determines if this role is a human.
     */
    public boolean isHuman() {
        return this == HUMAN_REVIEWER || this == HUMAN_APPROVER;
    }

    /**
     * Determines if this role can approve workflow transitions.
     */
    public boolean canApprove() {
        return this == HUMAN_APPROVER;
    }

    /**
     * Determines if this role can review work.
     */
    public boolean canReview() {
        return this == HUMAN_REVIEWER || this == HUMAN_APPROVER;
    }

    /**
     * Determines if this role is a system role.
     */
    public boolean isSystem() {
        return this == SYSTEM;
    }

    /**
     * Returns the lowercase hyphenated name for file naming.
     * Example: HUMAN_REVIEWER returns "human-reviewer"
     */
    public String getFileName() {
        return name().toLowerCase().replace('_', '-');
    }

    /**
     * Creates a Role from a file name.
     * Example: "human-reviewer" returns HUMAN_REVIEWER
     */
    public static Role fromFileName(String fileName) {
        return valueOf(fileName.toUpperCase().replace('-', '_'));
    }
}
