package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.repository.TaskRepository;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File-based implementation of TaskRepository.
 * Stores tasks as JSON files in the story's tasks directory.
 *
 * File structure:
 * workspace/{prioritization}/{STORY-ID}/tasks/{TASK-ID}.json
 */
public class FileBasedTaskRepository implements TaskRepository {

    private final Path workspaceRoot;
    private final ObjectMapper objectMapper;

    public FileBasedTaskRepository(String workspaceRootPath) {
        this.workspaceRoot = Paths.get(workspaceRootPath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public Task save(Task task) {
        try {
            Path taskFile = findTaskFilePath(task.getId());

            if (taskFile == null) {
                // New task - need to find the story directory
                taskFile = findStoryTasksDirectory(task.getStoryId())
                    .resolve(task.getId().getValue() + ".json");
            }

            TaskDTO dto = TaskDTO.fromDomain(task);
            objectMapper.writeValue(taskFile.toFile(), dto);

            return task;
        } catch (IOException e) {
            throw new RepositoryException("Failed to save task: " + task.getId().getValue(), e);
        }
    }

    @Override
    public Optional<Task> findById(TaskId id) {
        try {
            Path taskFile = findTaskFilePath(id);

            if (taskFile != null && Files.exists(taskFile)) {
                TaskDTO dto = objectMapper.readValue(taskFile.toFile(), TaskDTO.class);
                return Optional.of(dto.toDomain());
            }

            return Optional.empty();
        } catch (IOException e) {
            throw new RepositoryException("Failed to find task: " + id.getValue(), e);
        }
    }

    @Override
    public List<Task> findByStoryId(StoryId storyId) {
        try {
            Path tasksDir = findStoryTasksDirectory(storyId);

            if (!Files.exists(tasksDir)) {
                return new ArrayList<>();
            }

            try (Stream<Path> taskFiles = Files.list(tasksDir)) {
                return taskFiles
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(taskFile -> {
                        try {
                            TaskDTO dto = objectMapper.readValue(taskFile.toFile(), TaskDTO.class);
                            return dto.toDomain();
                        } catch (IOException e) {
                            throw new RepositoryException("Failed to read task file: " + taskFile, e);
                        }
                    })
                    .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to find tasks for story: " + storyId.getValue(), e);
        }
    }

    @Override
    public void delete(TaskId id) {
        try {
            Path taskFile = findTaskFilePath(id);

            if (taskFile == null) {
                throw new RepositoryException("Task not found: " + id.getValue());
            }

            Files.deleteIfExists(taskFile);
        } catch (IOException e) {
            throw new RepositoryException("Failed to delete task: " + id.getValue(), e);
        }
    }

    @Override
    public boolean exists(TaskId id) {
        try {
            return findTaskFilePath(id) != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Finds the file path for a task by searching all story directories.
     */
    private Path findTaskFilePath(TaskId taskId) throws IOException {
        String fileName = taskId.getValue() + ".json";

        // Search in all prioritization directories
        for (String prioritization : new String[]{"backlog", "prioritized", "deprioritized"}) {
            Path prioritizationDir = workspaceRoot.resolve(prioritization);

            if (Files.exists(prioritizationDir)) {
                try (Stream<Path> storyDirs = Files.list(prioritizationDir)) {
                    Optional<Path> taskFile = storyDirs
                        .filter(Files::isDirectory)
                        .map(storyDir -> storyDir.resolve("tasks").resolve(fileName))
                        .filter(Files::exists)
                        .findFirst();

                    if (taskFile.isPresent()) {
                        return taskFile.get();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds the tasks directory for a story.
     */
    private Path findStoryTasksDirectory(StoryId storyId) throws IOException {
        // Search in all prioritization directories
        for (String prioritization : new String[]{"backlog", "prioritized", "deprioritized"}) {
            Path storyDir = workspaceRoot.resolve(prioritization).resolve(storyId.getValue());

            if (Files.exists(storyDir)) {
                Path tasksDir = storyDir.resolve("tasks");
                Files.createDirectories(tasksDir);
                return tasksDir;
            }
        }

        throw new RepositoryException("Story directory not found for: " + storyId.getValue());
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
