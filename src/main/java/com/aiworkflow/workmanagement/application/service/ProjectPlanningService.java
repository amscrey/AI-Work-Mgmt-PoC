package com.aiworkflow.workmanagement.application.service;

import com.aiworkflow.workmanagement.domain.model.Story;

import java.util.List;

/**
 * Service for processing PROJECT-PLANNING artifacts and creating stories.
 * <p>
 * PROJECT-PLANNING is a special reserved story used for project-level work:
 * - Initial requirements gathering
 * - Story ideation and backlog creation
 * - Architecture planning
 * <p>
 * This service can parse PROJECT-PLANNING artifacts for story suggestions
 * and create corresponding stories in the backlog.
 */
public interface ProjectPlanningService {

    /**
     * Parses PROJECT-PLANNING artifacts and extracts story suggestions.
     * <p>
     * Story suggestions are identified by patterns like:
     * - "STORY-XXX: Title" headers
     * - "## Story: Title" markdown sections
     * - Numbered lists with story descriptions
     *
     * @return list of parsed story suggestions (not yet persisted)
     */
    List<StorySuggestion> parseStorySuggestions();

    /**
     * Creates stories from PROJECT-PLANNING artifacts.
     * <p>
     * Parses artifacts, generates story IDs, and creates stories in BACKLOG state.
     *
     * @param requestedBy who requested the story creation
     * @return list of created stories
     */
    List<Story> createStoriesFromPlanning(String requestedBy);

    /**
     * Represents a story suggestion extracted from PROJECT-PLANNING artifacts.
     */
    record StorySuggestion(
        String suggestedId,
        String title,
        String summary,
        String description,
        List<String> tags
    ) {}
}
