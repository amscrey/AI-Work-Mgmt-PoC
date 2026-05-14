package com.aiworkflow.workmanagement.domain.model;

import com.aiworkflow.workmanagement.domain.valueobject.ArtifactName;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing an Artifact in the workflow system.
 * Artifacts are files or documents produced during the completion of a Story.
 */
public class Artifact implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArtifactName name;
    private final StoryId storyId;
    private final Path filePath;
    private ArtifactType type;
    private FileFormat format;
    private String description;
    private final String createdBy;
    private final Instant createdAt;
    private Instant updatedAt;
    private long fileSize;
    private String checksum;

    public Artifact(ArtifactName name, StoryId storyId, Path filePath, String createdBy) {
        if (name == null) {
            throw new IllegalArgumentException("ArtifactName cannot be null");
        }
        if (storyId == null) {
            throw new IllegalArgumentException("StoryId cannot be null");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException("CreatedBy cannot be null or blank");
        }

        this.name = name;
        this.storyId = storyId;
        this.filePath = filePath;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        // Derive type and format from the artifact name
        String extension = name.getExtension();
        this.type = ArtifactType.fromExtension(extension);
        this.format = FileFormat.fromExtension(extension);
    }

    // Getters
    public ArtifactName getName() {
        return name;
    }

    public StoryId getStoryId() {
        return storyId;
    }

    public Path getFilePath() {
        return filePath;
    }

    public ArtifactType getType() {
        return type;
    }

    public FileFormat getFormat() {
        return format;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    // Business methods
    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void updateType(ArtifactType type) {
        if (type == null) {
            throw new IllegalArgumentException("ArtifactType cannot be null");
        }
        this.type = type;
        this.updatedAt = Instant.now();
    }

    public void updateFormat(FileFormat format) {
        this.format = format;
        this.updatedAt = Instant.now();
    }

    public void updateFileSize(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("File size cannot be negative");
        }
        this.fileSize = size;
        this.updatedAt = Instant.now();
    }

    public void updateChecksum(String checksum) {
        if (checksum == null || checksum.isBlank()) {
            throw new IllegalArgumentException("Checksum cannot be null or blank");
        }
        this.checksum = checksum;
        this.updatedAt = Instant.now();
    }

    public String getExtension() {
        return name.getExtension();
    }

    public String getDescriptiveName() {
        return name.getDescriptiveName();
    }

    public String getRoleName() {
        return name.getRole();
    }

    public boolean isSourceCode() {
        return type == ArtifactType.SOURCE_CODE;
    }

    public boolean isDocumentation() {
        return type == ArtifactType.DOCUMENTATION;
    }

    public boolean isExecutable() {
        return type != null && type.isExecutable();
    }

    public String getFormattedSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return Objects.equals(name, artifact.name) &&
               Objects.equals(storyId, artifact.storyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, storyId);
    }

    @Override
    public String toString() {
        return "Artifact{" +
            "name=" + name +
            ", storyId=" + storyId +
            ", type=" + type +
            ", format=" + format +
            ", fileSize=" + getFormattedSize() +
            '}';
    }
}
