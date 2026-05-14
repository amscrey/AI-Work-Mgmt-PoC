package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.domain.model.Artifact;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.valueobject.ArtifactName;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationRequest;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationResult;
import com.aiworkflow.workmanagement.orchestration.exception.ArtifactWriteException;
import com.aiworkflow.workmanagement.orchestration.service.ArtifactOutputService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Implementation of {@link ArtifactOutputService}.
 * <p>
 * This service writes orchestration artifacts to the filesystem following
 * the naming convention: {name}__{role}__{timestamp}.{ext}
 * <p>
 * Artifacts are append-only (never overwritten) and are added to the Story aggregate.
 *
 * @see ArtifactOutputService
 */
@Service
public class ArtifactOutputServiceImpl implements ArtifactOutputService {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactOutputServiceImpl.class);

    private static final String PROJECT_PLANNING_ID = "PROJECT-PLANNING";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final StoryRepository storyRepository;
    private final WorkspaceConfig workspaceConfig;
    private final ActivityLogger activityLogger;

    public ArtifactOutputServiceImpl(
        StoryRepository storyRepository,
        WorkspaceConfig workspaceConfig,
        ActivityLogger activityLogger
    ) {
        this.storyRepository = storyRepository;
        this.workspaceConfig = workspaceConfig;
        this.activityLogger = activityLogger;
    }

    @Override
    public void writeArtifacts(OrchestrationRequest request, OrchestrationResult result) {
        if (request == null) {
            throw new IllegalArgumentException("Orchestration request cannot be null");
        }
        if (result == null) {
            throw new IllegalArgumentException("Orchestration result cannot be null");
        }

        logger.info("Writing {} artifacts for orchestration execution {}",
            result.getArtifacts().size(),
            result.getExecutionId());

        StoryId storyId = request.getStoryId() != null
            ? request.getStoryId()
            : new StoryId(PROJECT_PLANNING_ID);

        // Load story
        Story story = storyRepository.findById(storyId)
            .orElseThrow(() -> new ArtifactWriteException(
                "Story not found: " + storyId.getValue()
            ));

        Path artifactsDir = getArtifactsDirectory(story);

        // Ensure directory exists
        try {
            Files.createDirectories(artifactsDir);
        } catch (IOException e) {
            throw new ArtifactWriteException(
                "Failed to create artifacts directory: " + artifactsDir, e
            );
        }

        // Write each artifact
        for (ArtifactReference artifactRef : result.getArtifacts()) {
            writeArtifact(story, artifactsDir, artifactRef);
        }

        // Save updated story
        storyRepository.save(story);

        logger.info("Successfully wrote {} artifacts to {}", result.getArtifacts().size(), artifactsDir);
    }

    /**
     * Writes a single artifact to the filesystem and adds it to the story.
     */
    private void writeArtifact(Story story, Path artifactsDir, ArtifactReference artifactRef) {
        // Generate filename following convention: {name}__{role}__{timestamp}.md
        String filename = buildArtifactFilename(
            artifactRef.getName(),
            artifactRef.getCreatedBy(),
            artifactRef.getCreatedAt()
        );

        Path artifactPath = artifactsDir.resolve(filename);

        // Write to filesystem (append-only)
        try {
            Files.writeString(artifactPath, artifactRef.getContent());
            logger.debug("Wrote artifact to: {}", artifactPath);
        } catch (IOException e) {
            throw new ArtifactWriteException(
                "Failed to write artifact: " + filename, e
            );
        }

        // Create Artifact entity (use full filename that follows pattern)
        Artifact artifact = new Artifact(
            new ArtifactName(filename),
            story.getId(),
            artifactPath,
            artifactRef.getCreatedBy()
        );

        // Add to story aggregate
        story.addArtifact(artifact);

        // Log activity
        activityLogger.logSuccess(
            "system",
            "orchestration_artifact_created",
            story.getId().getValue(),
            Map.of(
                "artifact", artifactRef.getName(),
                "role", artifactRef.getCreatedBy(),
                "filename", filename
            )
        );

        logger.info("Created artifact: {} for story {}", filename, story.getId().getValue());
    }

    /**
     * Builds artifact filename following naming convention.
     * <p>
     * Format: {name}__{role}__{timestamp}.md
     * <p>
     * Example: research-findings__researcher__2026-03-31T100000Z.md
     */
    private String buildArtifactFilename(String name, String role, Instant createdAt) {
        String timestamp = TIMESTAMP_FORMATTER.format(createdAt);

        // Normalize name and role (lowercase, hyphens for spaces)
        String normalizedName = normalizePart(name);
        String normalizedRole = normalizePart(role);

        return String.format("%s__%s__%s.md", normalizedName, normalizedRole, timestamp);
    }

    /**
     * Normalizes a filename part (lowercase, hyphens for spaces).
     */
    private String normalizePart(String part) {
        return part.toLowerCase()
            .replaceAll("\\s+", "-")
            .replaceAll("[^a-z0-9-]", "");
    }

    /**
     * Gets the artifacts directory for a story.
     * <p>
     * For PROJECT-PLANNING: workspace/stories/PROJECT-PLANNING/artifacts/
     * For regular stories: workspace/stories/{story-id}/artifacts/
     */
    private Path getArtifactsDirectory(Story story) {
        Path storyDir = workspaceConfig.getWorkspaceRoot()
            .resolve(story.getPrioritizationState().getDirectoryName())
            .resolve(story.getId().getValue());
        return storyDir.resolve("artifacts");
    }
}
