package com.aiworkflow.workmanagement.orchestration.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * Value object representing a reference file from the workspace/reference/ directory.
 * Reference files are read-only markdown files that provide context for orchestration.
 * These are typically approved artifacts or documentation created by humans or agents.
 */
public class ReferenceFile {
    private final Path filePath;
    private final String fileName;
    private final String content;
    private final long fileSize;
    private final Instant lastModified;

    /**
     * Private constructor - use fromFile() factory method instead.
     */
    private ReferenceFile(Path filePath, String fileName, String content, long fileSize, Instant lastModified) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.content = content;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }

    /**
     * Factory method to create a ReferenceFile from a filesystem path.
     * Reads the file content and metadata.
     *
     * @param path the path to the reference file
     * @return a new ReferenceFile instance
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the path is null or doesn't exist
     */
    public static ReferenceFile fromFile(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File does not exist: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path is not a regular file: " + path);
        }
        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("File is not readable: " + path);
        }

        // Read file content
        String content = Files.readString(path);

        // Get file metadata
        String fileName = path.getFileName().toString();
        long fileSize = Files.size(path);
        Instant lastModified = Files.getLastModifiedTime(path).toInstant();

        return new ReferenceFile(path, fileName, content, fileSize, lastModified);
    }

    /**
     * Attempts to create a ReferenceFile from a path, returning null if it fails.
     * Useful for scenarios where file read errors should be handled gracefully.
     *
     * @param path the path to the reference file
     * @return a new ReferenceFile instance, or null if the file cannot be read
     */
    public static ReferenceFile tryFromFile(Path path) {
        try {
            return fromFile(path);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    /**
     * Returns the file extension (e.g., "md", "txt").
     *
     * @return the file extension without the dot
     */
    public String getExtension() {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * Checks if this is a markdown file.
     *
     * @return true if the file extension is .md
     */
    public boolean isMarkdown() {
        return "md".equalsIgnoreCase(getExtension());
    }

    /**
     * Returns the content size in a human-readable format.
     *
     * @return formatted file size (e.g., "1.5 KB", "234 bytes")
     */
    public String getFormattedSize() {
        if (fileSize < 1024) {
            return fileSize + " bytes";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    /**
     * Returns a summary of the reference file for logging/debugging.
     *
     * @return summary string
     */
    public String getSummary() {
        return String.format("%s (%s, modified %s)",
            fileName, getFormattedSize(), lastModified);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferenceFile that = (ReferenceFile) o;
        return Objects.equals(filePath, that.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }

    @Override
    public String toString() {
        return "ReferenceFile{" +
            "fileName='" + fileName + '\'' +
            ", fileSize=" + getFormattedSize() +
            ", lastModified=" + lastModified +
            '}';
    }
}
