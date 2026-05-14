package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File-based implementation of StoryRepository.
 * Stores stories as JSON files in a directory structure organized by prioritization state.
 *
 * Directory structure:
 * workspace/
 *   backlog/        (contains BACKLOG and DEPRIORITIZED stories)
 *     STORY-001/
 *       story.json
 *       tasks/
 *       comments/
 *   prioritized/    (contains PRIORITIZED stories)
 *     STORY-002/
 *       story.json
 *       tasks/
 *       comments/
 */
public class FileBasedStoryRepository implements StoryRepository {

    private final Path workspaceRoot;
    private final ObjectMapper objectMapper;

    public FileBasedStoryRepository(String workspaceRootPath) {
        this.workspaceRoot = Paths.get(workspaceRootPath);
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
                Path storyFile = workspaceRoot
                    .resolve(state.getDirectoryName())
                    .resolve(id.getValue())
                    .resolve("story.json");

                if (Files.exists(storyFile)) {
                    StoryDTO dto = objectMapper.readValue(storyFile.toFile(), StoryDTO.class);
                    return Optional.of(dto.toDomain());
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
