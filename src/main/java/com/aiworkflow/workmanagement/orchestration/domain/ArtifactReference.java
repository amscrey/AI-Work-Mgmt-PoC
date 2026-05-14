package com.aiworkflow.workmanagement.orchestration.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Value object representing a reference to an artifact created during orchestration.
 * This is a lightweight reference used during orchestration execution, before the artifact
 * is persisted to the filesystem and added to the Story aggregate.
 */
public class ArtifactReference implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String content;
    private final String createdBy;
    private final Instant createdAt;

    /**
     * Creates a new ArtifactReference.
     *
     * @param name the descriptive name of the artifact (without role/timestamp suffix)
     * @param content the artifact content (markdown, code, etc.)
     * @param createdBy the role/agent that created this artifact
     */
    public ArtifactReference(String name, String content, String createdBy) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Artifact name cannot be null or blank");
        }
        if (content == null) {
            throw new IllegalArgumentException("Artifact content cannot be null");
        }
        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException("CreatedBy cannot be null or blank");
        }

        this.name = name;
        this.content = content;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    /**
     * Builder for creating ArtifactReference instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the file extension based on content type.
     * Defaults to .md for markdown content.
     *
     * @return the file extension (e.g., "md", "java", "json")
     */
    public String getExtension() {
        // Default to markdown for most orchestration artifacts
        // Can be enhanced later to detect from content or explicit specification
        return "md";
    }

    /**
     * Returns the size of the content in bytes.
     *
     * @return content size in bytes
     */
    public long getContentSize() {
        return content.getBytes().length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactReference that = (ArtifactReference) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(createdBy, that.createdBy) &&
               Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, createdBy, createdAt);
    }

    @Override
    public String toString() {
        return "ArtifactReference{" +
            "name='" + name + '\'' +
            ", createdBy='" + createdBy + '\'' +
            ", size=" + getContentSize() + " bytes" +
            '}';
    }

    /**
     * Builder class for ArtifactReference.
     */
    public static class Builder {
        private String name;
        private String content;
        private String createdBy;

        private Builder() {}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public ArtifactReference build() {
            return new ArtifactReference(name, content, createdBy);
        }
    }
}
