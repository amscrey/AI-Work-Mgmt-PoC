package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationRequest;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationResult;
import com.aiworkflow.workmanagement.orchestration.exception.ArtifactWriteException;

/**
 * Service responsible for writing orchestration artifacts to the filesystem.
 * <p>
 * This service takes an {@link OrchestrationResult} (domain object) and persists all
 * artifacts to the workspace filesystem following the naming convention. It also
 * integrates with the Story aggregate and ActivityLogger.
 * <p>
 * The service is decoupled from langgraph4j - OrchestrationService extracts artifacts
 * from the final OrchestrationState and builds the OrchestrationResult before passing
 * it to this service.
 *
 * <h2>Artifact Naming Convention</h2>
 * All artifacts follow the pattern: <code>{name}__{role}__{timestamp}.{ext}</code>
 * <ul>
 *   <li><strong>name</strong>: Descriptive name (lowercase, hyphens for spaces)</li>
 *   <li><strong>role</strong>: Agent role that created it (lowercase, hyphens for spaces)</li>
 *   <li><strong>timestamp</strong>: ISO 8601 format: yyyy-MM-dd'T'HHmmss'Z'</li>
 *   <li><strong>ext</strong>: File extension (typically "md" for markdown)</li>
 * </ul>
 * <p>
 * Example: <code>research-findings__researcher__2026-03-31T100000Z.md</code>
 *
 * <h2>Artifact Locations</h2>
 * <ul>
 *   <li><strong>PROJECT-PLANNING</strong>: workspace/stories/PROJECT-PLANNING/artifacts/</li>
 *   <li><strong>Story-specific</strong>: workspace/stories/{story-id}/artifacts/</li>
 * </ul>
 *
 * <h2>Append-Only Guarantee</h2>
 * Artifacts are <strong>never overwritten</strong>. The timestamp in the filename ensures
 * uniqueness. If a file with the same name exists, it is preserved (older version).
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // After orchestration execution
 * OrchestrationResult result = OrchestrationResult.success(
 *     "exec-123",
 *     List.of(
 *         new ArtifactReference("research-findings", "# Research...", "researcher"),
 *         new ArtifactReference("task-breakdown", "# Tasks...", "logician")
 *     ),
 *     "Research and planning completed"
 * );
 *
 * // Write artifacts to filesystem
 * artifactOutputService.writeArtifacts(request, result);
 *
 * // Result: Files created at:
 * // - workspace/stories/STORY-123/artifacts/research-findings__researcher__2026-03-31T100000Z.md
 * // - workspace/stories/STORY-123/artifacts/task-breakdown__logician__2026-03-31T100005Z.md
 * }</pre>
 *
 * @see OrchestrationResult
 * @see com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference
 */
public interface ArtifactOutputService {

    /**
     * Writes orchestration artifacts to the workspace filesystem.
     * <p>
     * This method:
     * <ol>
     *   <li>Determines artifact location (PROJECT-PLANNING or story-specific)</li>
     *   <li>For each artifact in the result:</li>
     *   <ul>
     *     <li>Generates filename following naming convention</li>
     *     <li>Writes content to filesystem (append-only)</li>
     *     <li>Creates Artifact entity</li>
     *     <li>Adds artifact to Story aggregate via story.addArtifact()</li>
     *     <li>Logs 'orchestration_artifact_created' event to ActivityLogger</li>
     *   </ul>
     *   <li>Saves updated Story via StoryRepository</li>
     * </ol>
     *
     * @param request the original orchestration request (contains storyId for routing)
     * @param result the orchestration result containing artifacts to write
     * @throws ArtifactWriteException if artifact writing fails (file I/O error,
     *         story not found, permission denied, etc.)
     * @throws IllegalArgumentException if request or result is null
     */
    void writeArtifacts(OrchestrationRequest request, OrchestrationResult result);
}
