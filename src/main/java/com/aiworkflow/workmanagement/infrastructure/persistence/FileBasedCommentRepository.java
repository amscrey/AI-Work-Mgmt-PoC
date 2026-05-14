package com.aiworkflow.workmanagement.infrastructure.persistence;

import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.model.CommentType;
import com.aiworkflow.workmanagement.domain.repository.CommentRepository;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;

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
 * File-based implementation of CommentRepository.
 * Stores comments as markdown files in the story's comments directory.
 *
 * File structure:
 * workspace/{prioritization}/{STORY-ID}/comments/comment__{role}__{timestamp}.md
 *
 * File format:
 * ---
 * type: QUESTION
 * author: agent
 * createdAt: 2026-03-30T14:15:30Z
 * resolved: false
 * ---
 * Comment content here...
 */
public class FileBasedCommentRepository implements CommentRepository {

    private final Path workspaceRoot;

    public FileBasedCommentRepository(String workspaceRootPath) {
        this.workspaceRoot = Paths.get(workspaceRootPath);
    }

    @Override
    public Comment save(Comment comment) {
        try {
            Path commentFile = findCommentFilePath(comment.getId());

            if (commentFile == null) {
                // New comment - find the story directory
                commentFile = findStoryCommentsDirectory(comment.getStoryId())
                    .resolve(comment.getId().getValue());
            }

            // Write comment as markdown with frontmatter
            String content = CommentMarkdownFormatter.format(comment);
            Files.writeString(commentFile, content);

            return comment;
        } catch (IOException e) {
            throw new RepositoryException("Failed to save comment: " + comment.getId().getValue(), e);
        }
    }

    @Override
    public Optional<Comment> findById(CommentId id) {
        try {
            Path commentFile = findCommentFilePath(id);

            if (commentFile != null && Files.exists(commentFile)) {
                String content = Files.readString(commentFile);
                return Optional.of(CommentMarkdownFormatter.parse(id, content));
            }

            return Optional.empty();
        } catch (IOException e) {
            throw new RepositoryException("Failed to find comment: " + id.getValue(), e);
        }
    }

    @Override
    public List<Comment> findByStoryId(StoryId storyId) {
        try {
            Path commentsDir = findStoryCommentsDirectory(storyId);

            if (!Files.exists(commentsDir)) {
                return new ArrayList<>();
            }

            try (Stream<Path> commentFiles = Files.list(commentsDir)) {
                return commentFiles
                    .filter(path -> path.toString().endsWith(".md"))
                    .map(commentFile -> {
                        try {
                            String fileName = commentFile.getFileName().toString();
                            CommentId commentId = new CommentId(fileName);
                            String content = Files.readString(commentFile);
                            return CommentMarkdownFormatter.parse(commentId, content);
                        } catch (IOException e) {
                            throw new RepositoryException("Failed to read comment file: " + commentFile, e);
                        }
                    })
                    .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to find comments for story: " + storyId.getValue(), e);
        }
    }

    @Override
    public void delete(CommentId id) {
        try {
            Path commentFile = findCommentFilePath(id);

            if (commentFile == null) {
                throw new RepositoryException("Comment not found: " + id.getValue());
            }

            Files.deleteIfExists(commentFile);
        } catch (IOException e) {
            throw new RepositoryException("Failed to delete comment: " + id.getValue(), e);
        }
    }

    /**
     * Finds the file path for a comment by searching all story directories.
     */
    private Path findCommentFilePath(CommentId commentId) throws IOException {
        String fileName = commentId.getValue();

        // Search in all prioritization directories
        for (String prioritization : new String[]{"backlog", "prioritized", "deprioritized"}) {
            Path prioritizationDir = workspaceRoot.resolve(prioritization);

            if (Files.exists(prioritizationDir)) {
                try (Stream<Path> storyDirs = Files.list(prioritizationDir)) {
                    Optional<Path> commentFile = storyDirs
                        .filter(Files::isDirectory)
                        .map(storyDir -> storyDir.resolve("comments").resolve(fileName))
                        .filter(Files::exists)
                        .findFirst();

                    if (commentFile.isPresent()) {
                        return commentFile.get();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds the comments directory for a story.
     */
    private Path findStoryCommentsDirectory(StoryId storyId) throws IOException {
        // Search in all prioritization directories
        for (String prioritization : new String[]{"backlog", "prioritized", "deprioritized"}) {
            Path storyDir = workspaceRoot.resolve(prioritization).resolve(storyId.getValue());

            if (Files.exists(storyDir)) {
                Path commentsDir = storyDir.resolve("comments");
                Files.createDirectories(commentsDir);
                return commentsDir;
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

    /**
     * Utility class for formatting comments as markdown with frontmatter.
     */
    private static class CommentMarkdownFormatter {

        /**
         * Formats a comment as markdown with YAML frontmatter.
         */
        public static String format(Comment comment) {
            StringBuilder sb = new StringBuilder();

            // YAML frontmatter
            sb.append("---\n");
            sb.append("type: ").append(comment.getType().name()).append("\n");
            sb.append("author: ").append(comment.getAuthor()).append("\n");
            sb.append("createdAt: ").append(comment.getCreatedAt().toString()).append("\n");
            sb.append("updatedAt: ").append(comment.getUpdatedAt().toString()).append("\n");
            sb.append("resolved: ").append(comment.isResolved()).append("\n");

            if (comment.getResolvedBy() != null) {
                sb.append("resolvedBy: ").append(comment.getResolvedBy()).append("\n");
                sb.append("resolvedAt: ").append(comment.getResolvedAt().toString()).append("\n");
            }

            sb.append("---\n\n");

            // Content
            sb.append(comment.getContent());

            return sb.toString();
        }

        /**
         * Parses markdown with frontmatter back into a Comment.
         */
        public static Comment parse(CommentId commentId, String content) {
            // Split frontmatter and content
            String[] parts = content.split("---\n", 3);

            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid comment format: missing frontmatter");
            }

            String frontmatter = parts[1];
            String commentContent = parts[2].trim();

            // Parse frontmatter
            String[] lines = frontmatter.split("\n");
            String type = null;
            String author = null;
            boolean resolved = false;
            String resolvedBy = null;

            for (String line : lines) {
                String[] kv = line.split(": ", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String value = kv[1].trim();

                    switch (key) {
                        case "type":
                            type = value;
                            break;
                        case "author":
                            author = value;
                            break;
                        case "resolved":
                            resolved = Boolean.parseBoolean(value);
                            break;
                        case "resolvedBy":
                            resolvedBy = value;
                            break;
                    }
                }
            }

            // Extract storyId from commentId (it's embedded in the path)
            // This is a simplification - in real implementation, we'd get it from the file path
            String storyIdValue = "STORY-001"; // TODO: Extract from path

            StoryId storyId = new StoryId(storyIdValue);
            Comment comment = new Comment(
                commentId,
                storyId,
                author,
                commentContent,
                CommentType.valueOf(type)
            );

            // Set resolved state if applicable
            if (resolved && resolvedBy != null) {
                comment.resolve(resolvedBy);
            }

            return comment;
        }
    }
}
