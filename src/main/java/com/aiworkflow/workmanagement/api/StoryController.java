package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.api.dto.StoryCreateRequest;
import com.aiworkflow.workmanagement.api.dto.StoryResponse;
import com.aiworkflow.workmanagement.api.dto.StoryTransitionRequest;
import com.aiworkflow.workmanagement.api.dto.StoryUpdateRequest;
import com.aiworkflow.workmanagement.application.command.CreateStoryCommand;
import com.aiworkflow.workmanagement.application.command.TransitionStoryCommand;
import com.aiworkflow.workmanagement.application.command.UpdateStoryCommand;
import com.aiworkflow.workmanagement.application.service.StoryService;
import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Role;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for stories.
 */
@RestController
@RequestMapping("/api/v1/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping
    public ResponseEntity<StoryResponse> createStory(@Valid @RequestBody StoryCreateRequest request) {
        CreateStoryCommand command = new CreateStoryCommand(
            request.getStoryId(),
            request.getTitle(),
            request.getSummary(),
            request.getDescription(),
            request.getAuthor(),
            request.getInitialState(),
            request.getPrioritization(),
            request.getAcceptanceCriteria(),
            request.getTags(),
            request.getEstimatedEffort()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(StoryResponse.from(storyService.createStory(command)));
    }

    @GetMapping("/{storyId}")
    public ResponseEntity<StoryResponse> getStory(@PathVariable String storyId) {
        return storyService.findById(new StoryId(storyId))
            .map(story -> ResponseEntity.ok(StoryResponse.from(story)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<StoryResponse>> queryStories(
        @RequestParam(required = false) WorkflowState state,
        @RequestParam(required = false) PrioritizationState prioritization
    ) {
        if (state == null && prioritization == null) {
            throw new IllegalArgumentException("At least one filter (state or prioritization) is required");
        }
        List<com.aiworkflow.workmanagement.domain.model.Story> stories;
        if (state != null) {
            stories = storyService.findByState(state);
        } else {
            stories = storyService.findByPrioritization(prioritization);
        }
        if (state != null && prioritization != null) {
            stories = stories.stream()
                .filter(story -> story.getPrioritizationState() == prioritization)
                .collect(Collectors.toList());
        }
        return ResponseEntity.ok(stories.stream().map(StoryResponse::from).collect(Collectors.toList()));
    }

    @PatchMapping("/{storyId}")
    public ResponseEntity<StoryResponse> updateStory(
        @PathVariable String storyId,
        @Valid @RequestBody StoryUpdateRequest request
    ) {
        UpdateStoryCommand command = new UpdateStoryCommand(
            storyId,
            request.getTitle(),
            request.getSummary(),
            request.getDescription(),
            request.getAcceptanceCriteria(),
            request.getTags(),
            request.getEstimatedEffort()
        );
        return ResponseEntity.ok(StoryResponse.from(storyService.updateStory(command)));
    }

    @PostMapping("/{storyId}/transition")
    public ResponseEntity<StoryResponse> transitionStory(
        @PathVariable String storyId,
        @Valid @RequestBody StoryTransitionRequest request
    ) {
        Role initiator = parseRole(request.getInitiatorRole());
        TransitionStoryCommand command = new TransitionStoryCommand(
            storyId,
            request.getTargetState(),
            initiator,
            request.getReason()
        );
        return ResponseEntity.ok(StoryResponse.from(storyService.transitionStory(command)));
    }

    private Role parseRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return Role.SYSTEM;
        }
        String normalized = roleName.trim().toUpperCase().replace('-', '_');
        return Role.valueOf(normalized);
    }
}

