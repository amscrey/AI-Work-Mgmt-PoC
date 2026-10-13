package com.aiworkflow.workmanagement.application.service.impl;

import com.aiworkflow.workmanagement.application.command.CreateStoryCommand;
import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.application.service.ProjectPlanningService;
import com.aiworkflow.workmanagement.application.service.StoryService;
import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Implementation of ProjectPlanningService.
 * <p>
 * Parses PROJECT-PLANNING artifacts for story suggestions and creates stories.
 * <p>
 * Supported formats for story suggestions in artifacts:
 * <pre>
 * ## STORY-001: Title here
 * Summary text follows...
 *
 * ## Story: Another title
 * Summary text follows...
 *
 * 1. **Story Title**: Description
 * 2. **Another Story**: Description
 * </pre>
 */
@Service
public class ProjectPlanningServiceImpl implements ProjectPlanningService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectPlanningServiceImpl.class);

    private static final String PROJECT_PLANNING_ID = "PROJECT-PLANNING";

    // Patterns for detecting story suggestions in artifacts
    // Pattern 1: ## STORY-XXX: Title
    private static final Pattern STORY_HEADER_PATTERN = Pattern.compile(
        "^##\\s*(STORY-\\d+):\\s*(.+)$", Pattern.MULTILINE
    );

    // Pattern 2: ## Story: Title or ### Story: Title
    private static final Pattern STORY_MARKDOWN_PATTERN = Pattern.compile(
        "^#{2,3}\\s*Story:\\s*(.+)$", Pattern.MULTILINE
    );

    // Pattern 3: Numbered list with bold title: 1. **Title**: Description
    private static final Pattern NUMBERED_STORY_PATTERN = Pattern.compile(
        "^\\d+\\.\\s*\\*\\*(.+?)\\*\\*[:\\s]*(.*)$", Pattern.MULTILINE
    );

    // Pattern 4: Story suggestion block
    private static final Pattern STORY_BLOCK_PATTERN = Pattern.compile(
        "^###?\\s*(?:Story\\s*)?(?:Suggestion)?\\s*(?:\\d+)?[:\\s]*(.+?)$\\s*(?:^>\\s*)?(.+?)(?=^#{2,3}|\\z)",
        Pattern.MULTILINE | Pattern.DOTALL
    );

    private final WorkspaceConfig workspaceConfig;
    private final StoryService storyService;
    private final StoryRepository storyRepository;
    private final ActivityLogger activityLogger;

    public ProjectPlanningServiceImpl(
        WorkspaceConfig workspaceConfig,
        StoryService storyService,
        StoryRepository storyRepository,
        ActivityLogger activityLogger
    ) {
        this.workspaceConfig = workspaceConfig;
        this.storyService = storyService;
        this.storyRepository = storyRepository;
        this.activityLogger = activityLogger;
    }

    @Override
    public List<StorySuggestion> parseStorySuggestions() {
        List<StorySuggestion> suggestions = new ArrayList<>();

        Path artifactsDir = getProjectPlanningArtifactsDir();
        if (!Files.exists(artifactsDir)) {
            logger.info("No PROJECT-PLANNING artifacts directory found at {}", artifactsDir);
            return suggestions;
        }

        try (Stream<Path> artifactFiles = Files.list(artifactsDir)) {
            artifactFiles
                .filter(path -> path.toString().endsWith(".md"))
                .forEach(artifactPath -> {
                    try {
                        String content = Files.readString(artifactPath);
                        List<StorySuggestion> parsed = parseArtifactContent(content, artifactPath.getFileName().toString());
                        suggestions.addAll(parsed);
                    } catch (IOException e) {
                        logger.warn("Failed to read artifact {}: {}", artifactPath, e.getMessage());
                    }
                });
        } catch (IOException e) {
            logger.error("Failed to list PROJECT-PLANNING artifacts: {}", e.getMessage());
        }

        logger.info("Found {} story suggestions from PROJECT-PLANNING artifacts", suggestions.size());
        return suggestions;
    }

    @Override
    public List<Story> createStoriesFromPlanning(String requestedBy) {
        List<StorySuggestion> suggestions = parseStorySuggestions();
        List<Story> createdStories = new ArrayList<>();

        if (suggestions.isEmpty()) {
            logger.info("No story suggestions found in PROJECT-PLANNING artifacts");
            return createdStories;
        }

        // Find the highest existing story number
        int nextStoryNumber = findNextStoryNumber();

        for (StorySuggestion suggestion : suggestions) {
            try {
                // Generate story ID if not provided
                String storyId = suggestion.suggestedId();
                if (storyId == null || storyId.isBlank() || storyRepository.exists(new com.aiworkflow.workmanagement.domain.valueobject.StoryId(storyId))) {
                    storyId = "STORY-" + nextStoryNumber++;
                }

                // Create the story
                CreateStoryCommand command = new CreateStoryCommand(
                    storyId,
                    suggestion.title(),
                    truncateSummary(suggestion.summary()),
                    suggestion.description(),
                    requestedBy,
                    null, // WorkflowState defaults to TODO
                    PrioritizationState.BACKLOG,
                    null, // acceptance criteria
                    suggestion.tags(),
                    null  // estimated effort
                );

                Story story = storyService.createStory(command);
                createdStories.add(story);

                logger.info("Created story {} from PROJECT-PLANNING: {}", storyId, suggestion.title());

                activityLogger.logSuccess(
                    "orchestrator",
                    "story_created_from_planning",
                    storyId,
                    Map.of(
                        "title", suggestion.title(),
                        "source", "PROJECT-PLANNING"
                    )
                );
            } catch (Exception e) {
                logger.error("Failed to create story from suggestion '{}': {}",
                    suggestion.title(), e.getMessage());
            }
        }

        logger.info("Created {} stories from PROJECT-PLANNING artifacts", createdStories.size());
        return createdStories;
    }

    /**
     * Parses artifact content for story suggestions.
     */
    private List<StorySuggestion> parseArtifactContent(String content, String filename) {
        List<StorySuggestion> suggestions = new ArrayList<>();

        // Try Pattern 1: ## STORY-XXX: Title
        Matcher matcher1 = STORY_HEADER_PATTERN.matcher(content);
        while (matcher1.find()) {
            String storyId = matcher1.group(1);
            String title = matcher1.group(2).trim();
            String summary = extractSummaryAfterMatch(content, matcher1.end());
            suggestions.add(new StorySuggestion(storyId, title, summary, null, List.of("from-planning")));
        }

        // If no explicit STORY-XXX patterns found, try other patterns
        if (suggestions.isEmpty()) {
            // Try Pattern 2: ## Story: Title
            Matcher matcher2 = STORY_MARKDOWN_PATTERN.matcher(content);
            while (matcher2.find()) {
                String title = matcher2.group(1).trim();
                String summary = extractSummaryAfterMatch(content, matcher2.end());
                suggestions.add(new StorySuggestion(null, title, summary, null, List.of("from-planning")));
            }
        }

        // Also try numbered list pattern regardless of other patterns
        Matcher matcher3 = NUMBERED_STORY_PATTERN.matcher(content);
        while (matcher3.find()) {
            String title = matcher3.group(1).trim();
            String description = matcher3.group(2).trim();
            // Avoid duplicates
            String finalTitle = title;
            if (suggestions.stream().noneMatch(s -> s.title().equalsIgnoreCase(finalTitle))) {
                String summary = description.isEmpty() ? title : description;
                suggestions.add(new StorySuggestion(null, title, summary, description, List.of("from-planning")));
            }
        }

        return suggestions;
    }

    /**
     * Extracts summary text following a match position.
     */
    private String extractSummaryAfterMatch(String content, int startPos) {
        // Look for the next paragraph after the title
        int endPos = content.indexOf("\n\n", startPos);
        if (endPos == -1) {
            endPos = Math.min(startPos + 500, content.length());
        }

        if (startPos >= content.length()) {
            return "";
        }

        String summary = content.substring(startPos, endPos).trim();
        // Remove leading newlines and markdown formatting
        summary = summary.replaceAll("^[\\s>]*", "");
        return truncateSummary(summary);
    }

    /**
     * Truncates summary to fit within 500 char limit.
     */
    private String truncateSummary(String summary) {
        if (summary == null) {
            return "";
        }
        summary = summary.trim();
        if (summary.length() <= 500) {
            return summary;
        }
        return summary.substring(0, 497) + "...";
    }

    /**
     * Finds the next available story number.
     */
    private int findNextStoryNumber() {
        int maxNumber = 100; // Start from 100 if no stories exist

        List<Story> allStories = storyRepository.findAll();
        for (Story story : allStories) {
            String id = story.getId().getValue();
            if (id.startsWith("STORY-")) {
                try {
                    int number = Integer.parseInt(id.substring(6));
                    if (number >= maxNumber) {
                        maxNumber = number + 1;
                    }
                } catch (NumberFormatException e) {
                    // Skip non-numeric story IDs
                }
            }
        }

        return maxNumber;
    }

    /**
     * Gets the PROJECT-PLANNING artifacts directory.
     */
    private Path getProjectPlanningArtifactsDir() {
        return workspaceConfig.getWorkspaceRoot()
            .resolve("prioritized")
            .resolve(PROJECT_PLANNING_ID)
            .resolve("artifacts");
    }
}
