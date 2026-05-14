package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.api.dto.ArtifactReferenceResponse;
import com.aiworkflow.workmanagement.api.dto.AssistantReference;
import com.aiworkflow.workmanagement.api.dto.AssistantResponse;
import com.aiworkflow.workmanagement.api.dto.OrchestrationRunDetailResponse;
import com.aiworkflow.workmanagement.api.dto.OrchestrationRunRequest;
import com.aiworkflow.workmanagement.api.dto.OrchestrationRunResponse;
import com.aiworkflow.workmanagement.api.dto.OrchestrationStatusResponse;
import com.aiworkflow.workmanagement.api.dto.StoryUsageSummaryResponse;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationRequest;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationResult;
import com.aiworkflow.workmanagement.orchestration.service.OrchestrationService;
import com.aiworkflow.workmanagement.orchestration.service.StoryTokenUsageService;
import com.aiworkflow.workmanagement.application.command.CreateCommentCommand;
import com.aiworkflow.workmanagement.application.service.CommentService;
import com.aiworkflow.workmanagement.domain.model.CommentType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for orchestration and usage reporting.
 */
@RestController
@RequestMapping("/api/v1")
public class OrchestrationController {

    private static final String PROJECT_PLANNING_ID = "PROJECT-PLANNING";
    private static final int SUMMARY_MAX_CHARS = 280;

    private final OrchestrationService orchestrationService;
    private final OrchestrationRunRegistry runRegistry;
    private final StoryTokenUsageService tokenUsageService;
    private final CommentService commentService;

    public OrchestrationController(
        OrchestrationService orchestrationService,
        OrchestrationRunRegistry runRegistry,
        StoryTokenUsageService tokenUsageService,
        CommentService commentService
    ) {
        this.orchestrationService = orchestrationService;
        this.runRegistry = runRegistry;
        this.tokenUsageService = tokenUsageService;
        this.commentService = commentService;
    }

    @PostMapping("/orchestration/runs")
    public ResponseEntity<OrchestrationRunResponse> startRun(@Valid @RequestBody OrchestrationRunRequest request) {
        OrchestrationRequest orchestrationRequest = buildRequest(request);
        OrchestrationResult result = orchestrationService.orchestrate(orchestrationRequest);
        String storyId = resolveStoryId(request.getStoryId());
        runRegistry.record(storyId, result);
        AssistantResponse assistantResponse = buildAssistantResponse(result, storyId);
        OrchestrationRunResponse response = new OrchestrationRunResponse(
            result.getExecutionId(),
            "COMPLETED",
            result.getOutcome(),
            assistantResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/orchestration/prompt", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<OrchestrationRunResponse> promptRun(
        @RequestBody String prompt,
        @RequestParam String requestedBy,
        @RequestParam(required = false) String storyId
    ) {
        OrchestrationRequest orchestrationRequest = buildPromptRequest(prompt, requestedBy, storyId);
        OrchestrationResult result = orchestrationService.orchestrate(orchestrationRequest);
        String resolvedStoryId = resolveStoryId(storyId);
        runRegistry.record(resolvedStoryId, result);
        AssistantResponse assistantResponse = buildAssistantResponse(result, resolvedStoryId);
        OrchestrationRunResponse response = new OrchestrationRunResponse(
            result.getExecutionId(),
            "COMPLETED",
            result.getOutcome(),
            assistantResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orchestration/runs/{executionId}")
    public ResponseEntity<OrchestrationRunDetailResponse> getRun(@PathVariable String executionId) {
        return runRegistry.findByExecutionId(executionId)
            .map(record -> ResponseEntity.ok(toDetailResponse(record)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/stories/{storyId}/orchestration")
    public ResponseEntity<OrchestrationStatusResponse> getStoryOrchestration(@PathVariable String storyId) {
        return runRegistry.findLatestByStory(storyId)
            .map(record -> ResponseEntity.ok(new OrchestrationStatusResponse(record.getExecutionId(), record.getStatus())))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/stories/{storyId}/usage")
    public ResponseEntity<StoryUsageSummaryResponse> getStoryUsage(@PathVariable String storyId) {
        return ResponseEntity.ok(StoryUsageSummaryResponse.from(tokenUsageService.getSummary(new StoryId(storyId))));
    }

    private OrchestrationRequest buildRequest(OrchestrationRunRequest request) {
        if (request.getStoryId() == null || request.getStoryId().isBlank()) {
            return OrchestrationRequest.forProject(request.getWorkIntent(), request.getRequestedBy());
        }
        return OrchestrationRequest.forStory(new StoryId(request.getStoryId()), request.getWorkIntent(), request.getRequestedBy());
    }

    private OrchestrationRequest buildPromptRequest(String prompt, String requestedBy, String storyId) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt cannot be blank");
        }
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new IllegalArgumentException("requestedBy cannot be blank");
        }
        if (storyId == null || storyId.isBlank()) {
            return OrchestrationRequest.forProject(prompt, requestedBy);
        }
        return OrchestrationRequest.forStory(new StoryId(storyId), prompt, requestedBy);
    }

    private OrchestrationRunDetailResponse toDetailResponse(OrchestrationRunRecord record) {
        OrchestrationResult result = record.getResult();
        List<ArtifactReferenceResponse> artifacts = result.getArtifacts().stream()
            .map(ArtifactReferenceResponse::from)
            .collect(Collectors.toList());
        AssistantResponse assistantResponse = buildAssistantResponse(result, record.getStoryId());

        return new OrchestrationRunDetailResponse(
            result.getExecutionId(),
            record.getStatus(),
            result.getOutcome(),
            result.getExecutionSummary(),
            result.getErrors(),
            artifacts,
            result.getStartedAt(),
            result.getCompletedAt(),
            assistantResponse
        );
    }

    private AssistantResponse buildAssistantResponse(OrchestrationResult result, String storyId) {
        List<AssistantReference> references = new ArrayList<>();
        List<String> highlights = new ArrayList<>();

        if (result.getExecutionSummary() != null) {
            highlights.add(summarize(result.getExecutionSummary()));
        }
        if (!result.getErrors().isEmpty()) {
            highlights.add("Errors: " + String.join("; ", result.getErrors()));
        }

        for (ArtifactReference artifact : result.getArtifacts()) {
            references.add(new AssistantReference(
                "artifact",
                storyId,
                null,
                artifact.getName(),
                artifact.getCreatedBy(),
                artifact.getCreatedAt()
            ));
        }

        String commentId = createResponseComment(result, storyId);
        if (commentId != null) {
            references.add(new AssistantReference(
                "comment",
                storyId,
                commentId,
                "assistant-response",
                "orchestrator",
                result.getCompletedAt()
            ));
        }

        String summary = summarize(result.getExecutionSummary());
        return new AssistantResponse("markdown", summary, highlights, references);
    }

    private String createResponseComment(OrchestrationResult result, String storyId) {
        if (storyId == null) {
            return null;
        }
        String commentId = "comment__orchestrator__" + formatTimestamp(result.getCompletedAt()) + ".md";
        String content = buildDetailedResponse(result);
        CreateCommentCommand command = new CreateCommentCommand(
            commentId,
            storyId,
            "orchestrator",
            CommentType.SYSTEM,
            content,
            result.getExecutionId(),
            false
        );
        commentService.createComment(command);
        return commentId;
    }

    private String buildDetailedResponse(OrchestrationResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Orchestration Summary\n\n");
        if (StringUtils.hasText(result.getExecutionSummary())) {
            builder.append(result.getExecutionSummary()).append("\n\n");
        }
        if (!result.getErrors().isEmpty()) {
            builder.append("## Errors\n");
            result.getErrors().forEach(error -> builder.append("- ").append(error).append("\n"));
            builder.append("\n");
        }
        if (!result.getArtifacts().isEmpty()) {
            builder.append("## Artifacts\n");
            for (ArtifactReference artifact : result.getArtifacts()) {
                builder.append("### ").append(artifact.getName()).append(" (" )
                    .append(artifact.getCreatedBy()).append(")\n\n");
                builder.append(artifact.getContent()).append("\n\n");
            }
        }
        return builder.toString().trim();
    }

    private String summarize(String text) {
        if (!StringUtils.hasText(text)) {
            return "Orchestration completed";
        }
        String trimmed = text.trim();
        if (trimmed.length() <= SUMMARY_MAX_CHARS) {
            return trimmed;
        }
        return trimmed.substring(0, SUMMARY_MAX_CHARS - 1) + "…";
    }

    private String resolveStoryId(String storyId) {
        if (storyId == null || storyId.isBlank()) {
            return PROJECT_PLANNING_ID;
        }
        return storyId;
    }

    private String formatTimestamp(Instant instant) {
        Instant timestamp = instant != null ? instant : Instant.now();
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss'Z'")
            .withZone(java.time.ZoneOffset.UTC)
            .format(timestamp);
    }
}

