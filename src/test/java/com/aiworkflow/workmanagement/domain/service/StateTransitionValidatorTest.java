package com.aiworkflow.workmanagement.domain.service;

import com.aiworkflow.workmanagement.domain.model.Role;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.ArtifactName;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.model.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StateTransitionValidator Domain Service Tests")
class SttateTransitionValidatorTest {

    private StateTransitionValidator validator;
    private Story story;

    @BeforeEach
    void setUp() {
        validator = new StateTransitionValidator();
        story = new Story(
            new StoryId("STORY-1"),
            "Test Story",
            "Test description"
        );
        story.assignTo("agent-1");
    }

    @Test
    @DisplayName("Should validate valid agent transition")
    void shouldValidateValidAgentTransition() {
        validator.validateWorkflowTransition(story, WorkflowState.IN_PROGRESS, Role.AGENT);

        // If no exception thrown, validation passed
        assertThat(story.getWorkflowState()).isEqualTo(WorkflowState.TODO);
    }

    @Test
    @DisplayName("Should throw exception for invalid agent transition")
    void shouldThrowExceptionForInvalidAgentTransition() {
        assertThatThrownBy(() ->
            validator.validateWorkflowTransition(story, WorkflowState.DONE, Role.AGENT)
        )
        .isInstanceOf(InvalidStateTransitionException.class)
        .hasMessageContaining("Invalid workflow state transition");
    }

    @Test
    @DisplayName("Should allow human to cancel from any state")
    void shouldAllowHumanToCancelFromAnyState() {
        validator.validateWorkflowTransition(story, WorkflowState.DONE, Role.HUMAN_APPROVER);

        // If no exception thrown, validation passed
        assertThat(story.getWorkflowState()).isEqualTo(WorkflowState.TODO);
    }

    @Test
    @DisplayName("Should return true for valid transition using isValidWorkflowTransition")
    void shouldReturnTrueForValidTransition() {
        boolean isValid = validator.isValidWorkflowTransition(story, WorkflowState.IN_PROGRESS, Role.AGENT);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false for invalid transition using isValidWorkflowTransition")
    void shouldReturnFalseForInvalidTransition() {
        boolean isValid = validator.isValidWorkflowTransition(story, WorkflowState.AWAITING_APPROVAL, Role.AGENT);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject null story")
    void shouldRejectNullStory() {
        assertThatThrownBy(() ->
            validator.validateWorkflowTransition(null, WorkflowState.IN_PROGRESS, Role.AGENT)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Story cannot be null");
    }

    @Test
    @DisplayName("Should reject null target state")
    void shouldRejectNullTargetState() {
        assertThatThrownBy(() ->
            validator.validateWorkflowTransition(story, null, Role.AGENT)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Target state cannot be null");
    }

    @Test
    @DisplayName("Should reject null initiator role")
    void shouldRejectNullInitiatorRole() {
        assertThatThrownBy(() ->
            validator.validateWorkflowTransition(story, WorkflowState.IN_PROGRESS, null)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Initiator role cannot be null");
    }

    @Test
    @DisplayName("Should validate full agent workflow progression")
    void shouldValidateFullAgentWorkflowProgression() {
        // Agent starts work
        validator.validateWorkflowTransition(story, WorkflowState.IN_PROGRESS, Role.AGENT);
        story.transitionWorkflowState(WorkflowState.IN_PROGRESS, Role.AGENT);

        // Add artifact for approval
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now());
        Artifact artifact = new Artifact(
            new ArtifactName("design__agent__" + timestamp + ".md"),
            story.getId(),
            Path.of("/tmp/design.md"),
            "agent-1"
        );
        story.addArtifact(artifact);

        // Agent submits for approval
        validator.validateWorkflowTransition(story, WorkflowState.AWAITING_APPROVAL, Role.AGENT);
        story.transitionWorkflowState(WorkflowState.AWAITING_APPROVAL, Role.AGENT);

        // Human approves
        validator.validateWorkflowTransition(story, WorkflowState.DONE, Role.HUMAN_APPROVER);

        assertThat(story.getWorkflowState()).isEqualTo(WorkflowState.AWAITING_APPROVAL);
    }

    @Test
    @DisplayName("Should validate human rejection and reopen workflow")
    void shouldValidateHumanRejectionAndReopenWorkflow() {
        // Setup story in AWAITING_APPROVAL state
        story.transitionWorkflowState(WorkflowState.IN_PROGRESS, Role.AGENT);

        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now());
        Artifact artifact = new Artifact(
            new ArtifactName("design__agent__" + timestamp + ".md"),
            story.getId(),
            Path.of("/tmp/design.md"),
            "agent-1"
        );
        story.addArtifact(artifact);

        story.transitionWorkflowState(WorkflowState.AWAITING_APPROVAL, Role.AGENT);

        // Human rejects (sends back to IN_PROGRESS)
        validator.validateWorkflowTransition(story, WorkflowState.IN_PROGRESS, Role.HUMAN_REVIEWER);
        story.transitionWorkflowState(WorkflowState.IN_PROGRESS, Role.HUMAN_REVIEWER);

        // Later, human can reopen from DONE
        story.transitionWorkflowState(WorkflowState.AWAITING_APPROVAL, Role.AGENT);
        story.transitionWorkflowState(WorkflowState.DONE, Role.HUMAN_APPROVER);

        validator.validateWorkflowTransition(story, WorkflowState.IN_PROGRESS, Role.HUMAN_APPROVER);

        assertThat(story.getWorkflowState()).isEqualTo(WorkflowState.DONE);
    }

    @Test
    @DisplayName("Should reject agent transition to DONE")
    void shouldRejectAgentTransitionToDone() {
        story.transitionWorkflowState(WorkflowState.IN_PROGRESS, Role.AGENT);

        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now());
        Artifact artifact = new Artifact(
            new ArtifactName("design__agent__" + timestamp + ".md"),
            story.getId(),
            Path.of("/tmp/design.md"),
            "agent-1"
        );
        story.addArtifact(artifact);

        story.transitionWorkflowState(WorkflowState.AWAITING_APPROVAL, Role.AGENT);

        assertThatThrownBy(() ->
            validator.validateWorkflowTransition(story, WorkflowState.DONE, Role.AGENT)
        )
        .isInstanceOf(InvalidStateTransitionException.class)
        .hasMessageContaining("Invalid workflow state transition");
    }

    @Test
    @DisplayName("Should reject transition to AWAITING_APPROVAL without artifacts")
    void shouldRejectTransitionToAwaitingApprovalWithoutArtifacts() {
        story.transitionWorkflowState(WorkflowState.IN_PROGRESS, Role.AGENT);

        assertThatThrownBy(() ->
            validator.validateWorkflowTransition(story, WorkflowState.AWAITING_APPROVAL, Role.AGENT)
        )
        .isInstanceOf(InvalidStateTransitionException.class)
        .hasMessageContaining("Cannot transition to AWAITING_APPROVAL without any artifacts");
    }

    @Test
    @DisplayName("Should reject approval by non-approver role")
    void shouldRejectApprovalByNonApproverRole() {
        story.transitionWorkflowState(WorkflowState.IN_PROGRESS, Role.AGENT);

        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now());
        Artifact artifact = new Artifact(
            new ArtifactName("design__agent__" + timestamp + ".md"),
            story.getId(),
            Path.of("/tmp/design.md"),
            "agent-1"
        );
        story.addArtifact(artifact);

        story.transitionWorkflowState(WorkflowState.AWAITING_APPROVAL, Role.AGENT);

        assertThatThrownBy(() ->
            validator.validateWorkflowTransition(story, WorkflowState.DONE, Role.AGENT)
        )
        .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("Should reject starting work on unassigned story")
    void shouldRejectStartingWorkOnUnassignedStory() {
        Story unassignedStory = new Story(
            new StoryId("STORY-2"),
            "Unassigned Story",
            "Test description"
        );

        assertThatThrownBy(() ->
            validator.validateWorkflowTransition(unassignedStory, WorkflowState.IN_PROGRESS, Role.AGENT)
        )
        .isInstanceOf(InvalidStateTransitionException.class)
        .hasMessageContaining("Cannot start work on unassigned story");
    }

    @Test
    @DisplayName("Should get valid next workflow states for story")
    void shouldGetValidNextWorkflowStates() {
        List<WorkflowState> validStates = validator.getValidNextWorkflowStates(story, Role.AGENT);

        assertThat(validStates).containsExactly(WorkflowState.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should get valid next workflow states for human")
    void shouldGetValidNextWorkflowStatesForHuman() {
        List<WorkflowState> validStates = validator.getValidNextWorkflowStates(story, Role.HUMAN_APPROVER);

        // Human can cancel to DONE from TODO
        assertThat(validStates).contains(WorkflowState.DONE);
    }
}
