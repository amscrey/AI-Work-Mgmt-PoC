package com.aiworkflow.workmanagement.domain.model;

/**
 * Represents the prioritization state of a Story.
 * Determines the order in which stories should be worked on.
 *
 * <p><b>Directory Mapping:</b></p>
 * <ul>
 *   <li>BACKLOG → backlog/</li>
 *   <li>PRIORITIZED → prioritized/</li>
 *   <li>DEPRIORITIZED → backlog/ (returns to backlog when deprioritized)</li>
 * </ul>
 */
public enum PrioritizationState {
    BACKLOG,
    PRIORITIZED,
    DEPRIORITIZED;

    /**
     * Returns the directory name for this prioritization state.
     *
     * <p>Note: Both BACKLOG and DEPRIORITIZED map to "backlog" directory.
     * There is no separate "deprioritized" directory.</p>
     *
     * @return "backlog" for BACKLOG and DEPRIORITIZED, "prioritized" for PRIORITIZED
     */
    public String getDirectoryName() {
        // Both BACKLOG and DEPRIORITIZED go to backlog directory
        // Only PRIORITIZED goes to prioritized directory
        return (this == PRIORITIZED) ? "prioritized" : "backlog";
    }

    /**
     * Creates a PrioritizationState from a directory name.
     *
     * <p>Note: Since both BACKLOG and DEPRIORITIZED map to "backlog" directory,
     * this method returns BACKLOG when "backlog" is provided. The actual state
     * (BACKLOG vs DEPRIORITIZED) is preserved in the story's JSON metadata.</p>
     *
     * @param directoryName "backlog" or "prioritized"
     * @return BACKLOG for "backlog", PRIORITIZED for "prioritized"
     * @throws IllegalArgumentException if directory name is not recognized
     */
    public static PrioritizationState fromDirectoryName(String directoryName) {
        if ("backlog".equalsIgnoreCase(directoryName)) {
            return BACKLOG;
        } else if ("prioritized".equalsIgnoreCase(directoryName)) {
            return PRIORITIZED;
        } else {
            throw new IllegalArgumentException("Unknown directory name: " + directoryName);
        }
    }

    /**
     * Determines if this is a state where work should be actively prioritized.
     */
    public boolean isPrioritized() {
        return this == PRIORITIZED;
    }
}
