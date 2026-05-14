package com.aiworkflow.workmanagement.orchestration.domain;

import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrchestrationRequest Tests")
class OrchestrationRequestTest {

    @Test
    @DisplayName("Should create story-specific request")
    void shouldCreateStorySpecificRequest() {
        StoryId storyId = new StoryId("STORY-123");
        String workIntent = "Research game mechanics for inventory system";
        String requestedBy = "human";

        OrchestrationRequest request = OrchestrationRequest.forStory(storyId, workIntent, requestedBy);

        assertThat(request.getStoryId()).isEqualTo(storyId);
        assertThat(request.getWorkIntent()).isEqualTo(workIntent);
        assertThat(request.getRequestedBy()).isEqualTo(requestedBy);
        assertThat(request.isStorySpecific()).isTrue();
        assertThat(request.isProjectLevel()).isFalse();
    }

    @Test
    @DisplayName("Should create project-level request")
    void shouldCreateProjectLevelRequest() {
        String workIntent = "Create high-level project plan";
        String requestedBy = "human";

        OrchestrationRequest request = OrchestrationRequest.forProject(workIntent, requestedBy);

        assertThat(request.getStoryId()).isNull();
        assertThat(request.getWorkIntent()).isEqualTo(workIntent);
        assertThat(request.getRequestedBy()).isEqualTo(requestedBy);
        assertThat(request.isProjectLevel()).isTrue();
        assertThat(request.isStorySpecific()).isFalse();
    }

    @Test
    @DisplayName("Should create request using builder with story")
    void shouldCreateRequestUsingBuilderWithStory() {
        StoryId storyId = new StoryId("STORY-456");

        OrchestrationRequest request = OrchestrationRequest.builder()
            .storyId(storyId)
            .workIntent("Design user interface")
            .requestedBy("designer")
            .build();

        assertThat(request.getStoryId()).isEqualTo(storyId);
        assertThat(request.isStorySpecific()).isTrue();
    }

    @Test
    @DisplayName("Should create request using builder without story")
    void shouldCreateRequestUsingBuilderWithoutStory() {
        OrchestrationRequest request = OrchestrationRequest.builder()
            .storyId(null)
            .workIntent("Analyze project requirements")
            .requestedBy("analyst")
            .build();

        assertThat(request.getStoryId()).isNull();
        assertThat(request.isProjectLevel()).isTrue();
    }

    @Test
    @DisplayName("Should reject null work intent")
    void shouldRejectNullWorkIntent() {
        assertThatThrownBy(() -> OrchestrationRequest.builder()
            .storyId(new StoryId("STORY-1"))
            .workIntent(null)
            .requestedBy("human")
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Work intent cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank work intent")
    void shouldRejectBlankWorkIntent() {
        assertThatThrownBy(() -> OrchestrationRequest.builder()
            .storyId(new StoryId("STORY-1"))
            .workIntent("   ")
            .requestedBy("human")
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Work intent cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject null requestedBy")
    void shouldRejectNullRequestedBy() {
        assertThatThrownBy(() -> OrchestrationRequest.builder()
            .storyId(new StoryId("STORY-1"))
            .workIntent("Do work")
            .requestedBy(null)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("RequestedBy cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank requestedBy")
    void shouldRejectBlankRequestedBy() {
        assertThatThrownBy(() -> OrchestrationRequest.builder()
            .storyId(new StoryId("STORY-1"))
            .workIntent("Do work")
            .requestedBy("   ")
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("RequestedBy cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject null storyId in forStory factory")
    void shouldRejectNullStoryIdInForStoryFactory() {
        assertThatThrownBy(() -> OrchestrationRequest.forStory(null, "work", "human"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("StoryId cannot be null");
    }

    @Test
    @DisplayName("Should have meaningful toString for story request")
    void shouldHaveMeaningfulToStringForStoryRequest() {
        StoryId storyId = new StoryId("STORY-789");
        OrchestrationRequest request = OrchestrationRequest.forStory(storyId, "work", "human");

        String toString = request.toString();

        assertThat(toString).contains("STORY-789");
        assertThat(toString).contains("work");
        assertThat(toString).contains("human");
    }

    @Test
    @DisplayName("Should have meaningful toString for project request")
    void shouldHaveMeaningfulToStringForProjectRequest() {
        OrchestrationRequest request = OrchestrationRequest.forProject("plan project", "human");

        String toString = request.toString();

        assertThat(toString).contains("PROJECT-PLANNING");
        assertThat(toString).contains("plan project");
        assertThat(toString).contains("human");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        StoryId storyId = new StoryId("STORY-1");
        OrchestrationRequest request1 = OrchestrationRequest.forStory(storyId, "work", "human");
        OrchestrationRequest request2 = OrchestrationRequest.forStory(storyId, "work", "human");
        OrchestrationRequest request3 = OrchestrationRequest.forStory(new StoryId("STORY-2"), "work", "human");

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1).isNotEqualTo(request3);
    }

    @Test
    @DisplayName("Should handle long work intent")
    void shouldHandleLongWorkIntent() {
        String longIntent = "Research and analyze the following topics: " + "x".repeat(1000);

        OrchestrationRequest request = OrchestrationRequest.forProject(longIntent, "researcher");

        assertThat(request.getWorkIntent()).hasSize(longIntent.length());
    }

    @Test
    @DisplayName("Should distinguish between project and story requests")
    void shouldDistinguishBetweenProjectAndStoryRequests() {
        OrchestrationRequest projectRequest = OrchestrationRequest.forProject("plan", "human");
        OrchestrationRequest storyRequest = OrchestrationRequest.forStory(
            new StoryId("STORY-1"), "plan", "human"
        );

        assertThat(projectRequest.isProjectLevel()).isTrue();
        assertThat(projectRequest.isStorySpecific()).isFalse();

        assertThat(storyRequest.isProjectLevel()).isFalse();
        assertThat(storyRequest.isStorySpecific()).isTrue();
    }
}
