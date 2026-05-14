package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.repository.TaskRepository;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.config.OrchestrationConfig;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationContext;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationRequest;
import com.aiworkflow.workmanagement.orchestration.domain.ReferenceFile;
import com.aiworkflow.workmanagement.orchestration.exception.OrchestrationContextException;
import com.aiworkflow.workmanagement.orchestration.service.ContextAssemblyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ContextAssemblyService}.
 * <p>
 * This service gathers all necessary context for orchestration execution:
 * <ul>
 *   <li>Story metadata and description</li>
 *   <li>Associated tasks</li>
 *   <li>Reference files from workspace/reference/</li>
 *   <li>Work intent from request</li>
 * </ul>
 * <p>
 * Context size is limited according to configuration to avoid exceeding LLM token limits.
 *
 * @see ContextAssemblyService
 */
@Service
public class ContextAssemblyServiceImpl implements ContextAssemblyService {

    private static final Logger logger = LoggerFactory.getLogger(ContextAssemblyServiceImpl.class);

    private static final String PROJECT_PLANNING_ID = "PROJECT-PLANNING";
    private static final String PROJECT_PLANNING_TITLE = "Project Planning";
    private static final String PROJECT_PLANNING_DESCRIPTION =
        "Meta work item for project-level planning, research, and coordination activities.";

    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceConfig workspaceConfig;
    private final OrchestrationConfig orchestrationConfig;

    public ContextAssemblyServiceImpl(
        StoryRepository storyRepository,
        TaskRepository taskRepository,
        WorkspaceConfig workspaceConfig,
        OrchestrationConfig orchestrationConfig
    ) {
        this.storyRepository = storyRepository;
        this.taskRepository = taskRepository;
        this.workspaceConfig = workspaceConfig;
        this.orchestrationConfig = orchestrationConfig;
    }

    @Override
    public OrchestrationContext assembleContext(OrchestrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Orchestration request cannot be null");
        }

        logger.debug("Assembling orchestration context for request: {}", request);

        // Get or create story
        Story story = getOrCreateStory(request.getStoryId());

        // Load associated tasks
        List<Task> tasks = loadTasks(request.getStoryId());

        // Load reference files
        List<ReferenceFile> referenceFiles = loadReferenceFiles();

        // Build context
        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent(request.getWorkIntent())
            .tasks(tasks)
            .referenceFiles(referenceFiles)
            .build();

        // Apply size limits
        context = applySizeLimits(context);

        logger.info("Assembled context: story={}, tasks={}, referenceFiles={}, size={}",
            story.getId().getValue(),
            tasks.size(),
            referenceFiles.size(),
            context.estimateContextSize());

        return context;
    }

    /**
     * Gets existing story or creates PROJECT-PLANNING if storyId is null.
     */
    private Story getOrCreateStory(StoryId storyId) {
        if (storyId == null) {
            return getOrCreateProjectPlanning();
        }

        return storyRepository.findById(storyId)
            .orElseThrow(() -> new OrchestrationContextException(
                "Story not found: " + storyId.getValue()
            ));
    }

    /**
     * Gets or creates the PROJECT-PLANNING meta work item.
     * <p>
     * PROJECT-PLANNING is a special story for project-level orchestration.
     * It is automatically created if it doesn't exist.
     */
    private Story getOrCreateProjectPlanning() {
        StoryId projectPlanningId = new StoryId(PROJECT_PLANNING_ID);

        return storyRepository.findById(projectPlanningId)
            .orElseGet(() -> {
                logger.info("Creating PROJECT-PLANNING meta work item");

                Story projectPlanning = new Story(
                    projectPlanningId,
                    PROJECT_PLANNING_TITLE,
                    PROJECT_PLANNING_DESCRIPTION,
                    WorkflowState.TODO,
                    PrioritizationState.PRIORITIZED
                );

                // Create directory structure
                createProjectPlanningDirectories();

                // Save and return
                return storyRepository.save(projectPlanning);
            });
    }

    /**
     * Creates directory structure for PROJECT-PLANNING.
     */
    private void createProjectPlanningDirectories() {
        try {
            Path projectPlanningDir = workspaceConfig.getWorkspaceRoot()
                .resolve(PrioritizationState.PRIORITIZED.getDirectoryName())
                .resolve(PROJECT_PLANNING_ID);

            Files.createDirectories(projectPlanningDir.resolve("artifacts"));
            Files.createDirectories(projectPlanningDir.resolve("tasks"));
            Files.createDirectories(projectPlanningDir.resolve("comments"));

            logger.debug("Created PROJECT-PLANNING directories at: {}", projectPlanningDir);
        } catch (IOException e) {
            throw new OrchestrationContextException(
                "Failed to create PROJECT-PLANNING directories", e
            );
        }
    }

    /**
     * Loads tasks associated with the story.
     */
    private List<Task> loadTasks(StoryId storyId) {
        if (storyId == null) {
            // PROJECT-PLANNING has no tasks initially
            return Collections.emptyList();
        }

        try {
            return taskRepository.findByStoryId(storyId);
        } catch (Exception e) {
            logger.warn("Failed to load tasks for story {}: {}", storyId.getValue(), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Loads reference files from workspace/reference/ directory.
     * <p>
     * Only loads markdown files (.md extension).
     * Applies limit from configuration.
     */
    private List<ReferenceFile> loadReferenceFiles() {
        Path referenceDir = workspaceConfig.getWorkspaceRoot().resolve("reference");

        if (!Files.exists(referenceDir) || !Files.isDirectory(referenceDir)) {
            logger.debug("Reference directory not found: {}", referenceDir);
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.walk(referenceDir, 2)) {
            List<ReferenceFile> files = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".md"))
                .sorted(Comparator.comparing(this::getLastModifiedTime).reversed())
                .limit(orchestrationConfig.getContext().getMaxReferenceFiles())
                .map(this::loadReferenceFile)
                .filter(file -> file != null)
                .collect(Collectors.toList());

            logger.debug("Loaded {} reference files from {}", files.size(), referenceDir);
            return files;
        } catch (IOException e) {
            logger.warn("Failed to load reference files from {}: {}", referenceDir, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Loads a single reference file.
     */
    private ReferenceFile loadReferenceFile(Path path) {
        try {
            return ReferenceFile.fromFile(path);
        } catch (IOException e) {
            logger.warn("Failed to load reference file {}: {}", path, e.getMessage());
            return null;
        }
    }

    /**
     * Gets last modified time for a file (for sorting).
     */
    private long getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Applies context size limits to prevent exceeding LLM token limits.
     * <p>
     * If context exceeds max size, reference files are truncated first.
     */
    private OrchestrationContext applySizeLimits(OrchestrationContext context) {
        long currentSize = context.estimateContextSize();
        long maxSize = orchestrationConfig.getContext().getMaxContextChars();

        if (currentSize <= maxSize) {
            return context;
        }

        logger.warn("Context size ({}) exceeds limit ({}), applying truncation", currentSize, maxSize);

        // Strategy: Remove reference files one by one until under limit
        List<ReferenceFile> truncatedFiles = new ArrayList<>(context.getReferenceFiles());

        while (currentSize > maxSize && !truncatedFiles.isEmpty()) {
            truncatedFiles.remove(truncatedFiles.size() - 1); // Remove last (oldest)

            OrchestrationContext testContext = OrchestrationContext.builder()
                .story(context.getStory())
                .workIntent(context.getWorkIntent())
                .tasks(context.getTasks())
                .referenceFiles(truncatedFiles)
                .build();

            currentSize = testContext.estimateContextSize();
        }

        if (currentSize > maxSize) {
            logger.error("Context still exceeds limit after removing all reference files");
        }

        return OrchestrationContext.builder()
            .story(context.getStory())
            .workIntent(context.getWorkIntent())
            .tasks(context.getTasks())
            .referenceFiles(truncatedFiles)
            .build();
    }
}
