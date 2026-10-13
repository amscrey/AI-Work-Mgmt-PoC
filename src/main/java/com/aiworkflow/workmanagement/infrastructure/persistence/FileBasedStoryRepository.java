package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.Artifact;
import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.repository.CommentRepository;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.valueobject.ArtifactName;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File-based implementation of StoryRepository.
 * Stores stories as JSON files in a directory structure organized by prioritization state.
 *
 * <p>This implementation loads artifacts and comments when retrieving stories,
 * ensuring the full Story aggregate is available for orchestration context.
 *
 * Directory structure:
 * workspace/
 *   backlog/        (contains BACKLOG and DEPRIORITIZED stories)
 *     STORY-001/
 *       story.json
 *       tasks/
 *       comments/
 *       artifacts/
 *   prioritized/    (contains PRIORITIZED stories)
 *     STORY-002/
 *       story.json
 *       tasks/
 *       comments/
 *       artifacts/
 */
public class FileBasedStoryRepository implements StoryRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedStoryRepository.class);

    // Pattern for artifact filenames: {name}__{role}__{timestamp}.{ext}
    private static final Pattern ARTIFACT_FILENAME_PATTERN = Pattern.compile(
        "^[a-z0-9]+(-[a-z0-9]+)*__[a-z]+(-[a-z]+)*__\\d{4}-\\d{2}-\\d{2}T\\d{6}Z\\..+$"
    );

    private final Path workspaceRoot;
    private final ObjectMapper objectMapper;
    private final CommentRepository commentRepository;

    /**
     * Creates a repository without comment loading capability.
     * Used primarily for testing and backward compatibility.
     */
    public FileBasedStoryRepository(String workspaceRootPath) {
        this(workspaceRootPath, null);
    }

    /**
     * Creates a repository with full artifact and comment loading capability.
     *
     * @param workspaceRootPath path to workspace root
     * @param commentRepository repository for loading comments (nullable)
     */
    public FileBasedStoryRepository(String workspaceRootPath, CommentRepository commentRepository) {
        this.workspaceRoot = Paths.get(workspaceRootPath);
        this.commentRepository = commentRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        initializeWorkspace();
    }

    private void initializeWorkspace() {
        try {
            // Create workspace root and prioritization directories
            Files.createDirectories(workspaceRoot);
            Files.createDirectories(workspaceRoot.resolve("backlog"));
            Files.createDirectories(workspaceRoot.resolve("prioritized"));
            Files.createDirectories(workspaceRoot.resolve("done"));
        } catch (IOException e) {
            throw new RepositoryException("Failed to initialize workspace", e);
        }
    }

    @Override
    public Story save(Story story) {
        try {
            Path storyDir = getStoryDirectory(story);
            Files.createDirectories(storyDir);

            // Create subdirectories for tasks and comments
            Files.createDirectories(storyDir.resolve("tasks"));
            Files.createDirectories(storyDir.resolve("comments"));

            // Serialize story to JSON
            Path storyFile = storyDir.resolve("story.json");
            StoryDTO dto = StoryDTO.fromDomain(story);
            objectMapper.writeValue(storyFile.toFile(), dto);

            return story;
        } catch (IOException e) {
            throw new RepositoryException("Failed to save story: " + story.getId().getValue(), e);
        }
    }

    @Override
    public Optional<Story> findById(StoryId id) {
        try {
            // Search in all prioritization directories
            for (PrioritizationState state : PrioritizationState.values()) {
                Path storyDir = workspaceRoot
                    .resolve(state.getDirectoryName())
                    .resolve(id.getValue());
                Path storyFile = storyDir.resolve("story.json");

                if (Files.exists(storyFile)) {
                    StoryDTO dto = objectMapper.readValue(storyFile.toFile(), StoryDTO.class);
                    Story story = dto.toDomain();

                    // Load artifacts and comments
                    loadArtifacts(story, storyDir);
                    loadComments(story);

                    return Optional.of(story);
                }
            }

            return Optional.empty();
        } catch (IOException e) {
            throw new RepositoryException("Failed to find story: " + id.getValue(), e);
        }
    }

    @Override
    public List<Story> findByState(WorkflowState state) {
        List<Story> stories = new ArrayList<>();

        try {
            // Search in all unique physical directories (backlog and prioritized only)
            for (String directoryName : getUniqueDirectoryNames()) {
                Path prioritizationDir = workspaceRoot.resolve(directoryName);

                if (Files.exists(prioritizationDir)) {
                    try (Stream<Path> storyDirs = Files.list(prioritizationDir)) {
                        storyDirs.filter(Files::isDirectory)
                            .forEach(storyDir -> {
                                Path storyFile = storyDir.resolve("story.json");
                                if (Files.exists(storyFile)) {
                                    try {
                                        StoryDTO dto = objectMapper.readValue(storyFile.toFile(), StoryDTO.class);
                                        Story story = dto.toDomain();
                                        if (story.getWorkflowState() == state) {
                                            stories.add(story);
                                        }
                                    } catch (IOException e) {
                                        throw new RepositoryException("Failed to read story file: " + storyFile, e);
                                    }
                                }
                            });
                    }
                }
            }

            return stories;
        } catch (IOException e) {
            throw new RepositoryException("Failed to find stories by state: " + state, e);
        }
    }

    @Override
    public List<Story> findByPrioritization(PrioritizationState prioritization) {
        List<Story> stories = new ArrayList<>();

        try {
            Path prioritizationDir = workspaceRoot.resolve(prioritization.getDirectoryName());

            if (Files.exists(prioritizationDir)) {
                try (Stream<Path> storyDirs = Files.list(prioritizationDir)) {
                    stories = storyDirs.filter(Files::isDirectory)
                        .map(storyDir -> storyDir.resolve("story.json"))
                        .filter(Files::exists)
                        .map(storyFile -> {
                            try {
                                StoryDTO dto = objectMapper.readValue(storyFile.toFile(), StoryDTO.class);
                                return dto.toDomain();
                            } catch (IOException e) {
                                throw new RepositoryException("Failed to read story file: " + storyFile, e);
                            }
                        })
                        // Filter by actual prioritization state from JSON (not just directory)
                        // This is necessary because both BACKLOG and DEPRIORITIZED map to "backlog" directory
                        .filter(story -> story.getPrioritizationState() == prioritization)
                        .collect(Collectors.toList());
                }
            }

            return stories;
        } catch (IOException e) {
            throw new RepositoryException("Failed to find stories by prioritization: " + prioritization, e);
        }
    }

    @Override
    public List<Story> findAll() {
        List<Story> stories = new ArrayList<>();

        try {
            // Scan only unique physical directories to avoid duplicates
            // (since BACKLOG and DEPRIORITIZED both map to "backlog" directory)
            for (String directoryName : getUniqueDirectoryNames()) {
                Path prioritizationDir = workspaceRoot.resolve(directoryName);

                if (Files.exists(prioritizationDir)) {
                    try (Stream<Path> storyDirs = Files.list(prioritizationDir)) {
                        List<Story> dirStories = storyDirs.filter(Files::isDirectory)
                            .map(storyDir -> storyDir.resolve("story.json"))
                            .filter(Files::exists)
                            .map(storyFile -> {
                                try {
                                    StoryDTO dto = objectMapper.readValue(storyFile.toFile(), StoryDTO.class);
                                    return dto.toDomain();
                                } catch (IOException e) {
                                    throw new RepositoryException("Failed to read story file: " + storyFile, e);
                                }
                            })
                            .collect(Collectors.toList());
                        stories.addAll(dirStories);
                    }
                }
            }
            return stories;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find all stories", e);
        }
    }

    /**
     * Returns the set of unique physical directory names.
     * Since BACKLOG and DEPRIORITIZED both map to "backlog", we only need to scan:
     * - "backlog"
     * - "prioritized"
     */
    private Set<String> getUniqueDirectoryNames() {
        Set<String> uniqueDirs = new HashSet<>();
        for (PrioritizationState state : PrioritizationState.values()) {
            uniqueDirs.add(state.getDirectoryName());
        }
        return uniqueDirs;
    }

    @Override
    public void delete(StoryId id) {
        try {
            Optional<Story> story = findById(id);
            if (story.isEmpty()) {
                throw new RepositoryException("Story not found: " + id.getValue());
            }

            Path storyDir = getStoryDirectory(story.get());
            deleteDirectoryRecursively(storyDir);
        } catch (IOException e) {
            throw new RepositoryException("Failed to delete story: " + id.getValue(), e);
        }
    }

    @Override
    public boolean exists(StoryId id) {
        return findById(id).isPresent();
    }

    /**
     * Gets the directory path for a story based on its prioritization state.
     */
    private Path getStoryDirectory(Story story) {
        String prioritizationDir = story.getPrioritizationState().getDirectoryName();
        return workspaceRoot.resolve(prioritizationDir).resolve(story.getId().getValue());
    }

    /**
     * Loads artifacts from the story's artifacts directory and adds them to the story.
     * Artifacts are stored as files following the naming convention: {name}__{role}__{timestamp}.{ext}
     */
    private void loadArtifacts(Story story, Path storyDir) {
        Path artifactsDir = storyDir.resolve("artifacts");

        if (!Files.exists(artifactsDir) || !Files.isDirectory(artifactsDir)) {
            logger.debug("No artifacts directory for story {}", story.getId().getValue());
            return;
        }

        try (Stream<Path> artifactFiles = Files.list(artifactsDir)) {
            artifactFiles
                .filter(Files::isRegularFile)
                .forEach(artifactPath -> {
                    String filename = artifactPath.getFileName().toString();

                    // Only load files matching the artifact naming pattern
                    if (ARTIFACT_FILENAME_PATTERN.matcher(filename).matches()) {
                        try {
                            ArtifactName artifactName = new ArtifactName(filename);
                            Artifact artifact = new Artifact(
                                artifactName,
                                story.getId(),
                                artifactPath,
                                artifactName.getRole()
                            );
                            story.addArtifact(artifact);
                            logger.debug("Loaded artifact {} for story {}",
                                filename, story.getId().getValue());
                        } catch (IllegalArgumentException e) {
                            logger.warn("Skipping invalid artifact file: {} - {}",
                                filename, e.getMessage());
                        }
                    } else {
                        logger.debug("Skipping non-artifact file: {}", filename);
                    }
                });
        } catch (IOException e) {
            logger.warn("Failed to load artifacts for story {}: {}",
                story.getId().getValue(), e.getMessage());
        }
    }

    /**
     * Loads comments from the comment repository and adds them to the story.
     * Requires commentRepository to be configured.
     */
    private void loadComments(Story story) {
        if (commentRepository == null) {
            logger.debug("CommentRepository not configured, skipping comment loading for story {}",
                story.getId().getValue());
            return;
        }

        try {
            List<Comment> comments = commentRepository.findByStoryId(story.getId());
            for (Comment comment : comments) {
                story.addComment(comment);
            }
            logger.debug("Loaded {} comments for story {}",
                comments.size(), story.getId().getValue());
        } catch (Exception e) {
            logger.warn("Failed to load comments for story {}: {}",
                story.getId().getValue(), e.getMessage());
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     */
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (Stream<Path> walk = Files.walk(directory)) {
                walk.sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    });
            }
        }
    }

    /**
     * Exception thrown when repository operations fail.
     */
    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String message) {
            super(message);
        }

        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
