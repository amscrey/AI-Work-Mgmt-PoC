package com.aiworkflow.workmanagement.orchestration.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ArtifactReference Tests")
class ArtifactReferenceTest {

    @Test
    @DisplayName("Should create artifact reference with valid parameters")
    void shouldCreateArtifactReferenceWithValidParameters() {
        String name = "research-findings";
        String content = "# Research Findings\n\nSome content here.";
        String createdBy = "researcher";

        ArtifactReference artifact = new ArtifactReference(name, content, createdBy);

        assertThat(artifact.getName()).isEqualTo(name);
        assertThat(artifact.getContent()).isEqualTo(content);
        assertThat(artifact.getCreatedBy()).isEqualTo(createdBy);
        assertThat(artifact.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should create artifact reference using builder")
    void shouldCreateArtifactReferenceUsingBuilder() {
        ArtifactReference artifact = ArtifactReference.builder()
            .name("task-breakdown")
            .content("# Task Breakdown\n\n1. Task 1\n2. Task 2")
            .createdBy("logician")
            .build();

        assertThat(artifact.getName()).isEqualTo("task-breakdown");
        assertThat(artifact.getContent()).contains("Task Breakdown");
        assertThat(artifact.getCreatedBy()).isEqualTo("logician");
    }

    @Test
    @DisplayName("Should reject null name")
    void shouldRejectNullName() {
        assertThatThrownBy(() -> new ArtifactReference(null, "content", "researcher"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Artifact name cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank name")
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> new ArtifactReference("   ", "content", "researcher"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Artifact name cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject null content")
    void shouldRejectNullContent() {
        assertThatThrownBy(() -> new ArtifactReference("name", null, "researcher"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Artifact content cannot be null");
    }

    @Test
    @DisplayName("Should accept empty content")
    void shouldAcceptEmptyContent() {
        ArtifactReference artifact = new ArtifactReference("name", "", "researcher");

        assertThat(artifact.getContent()).isEmpty();
        assertThat(artifact.getContentSize()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reject null createdBy")
    void shouldRejectNullCreatedBy() {
        assertThatThrownBy(() -> new ArtifactReference("name", "content", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CreatedBy cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject blank createdBy")
    void shouldRejectBlankCreatedBy() {
        assertThatThrownBy(() -> new ArtifactReference("name", "content", "   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CreatedBy cannot be null or blank");
    }

    @Test
    @DisplayName("Should return default extension as md")
    void shouldReturnDefaultExtensionAsMd() {
        ArtifactReference artifact = new ArtifactReference("name", "content", "researcher");

        assertThat(artifact.getExtension()).isEqualTo("md");
    }

    @Test
    @DisplayName("Should calculate content size correctly")
    void shouldCalculateContentSizeCorrectly() {
        String content = "Hello, World!";
        ArtifactReference artifact = new ArtifactReference("name", content, "researcher");

        assertThat(artifact.getContentSize()).isEqualTo(content.getBytes().length);
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        ArtifactReference artifact = new ArtifactReference("research", "content", "researcher");

        String toString = artifact.toString();

        assertThat(toString).contains("research");
        assertThat(toString).contains("researcher");
        assertThat(toString).contains("bytes");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        ArtifactReference artifact1 = new ArtifactReference("name", "content1", "researcher");

        // Sleep briefly to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ArtifactReference artifact2 = new ArtifactReference("name", "content2", "researcher");

        // Different timestamps mean they're not equal
        assertThat(artifact1).isNotEqualTo(artifact2);
    }

    @Test
    @DisplayName("Should handle large content")
    void shouldHandleLargeContent() {
        String largeContent = "x".repeat(100000);
        ArtifactReference artifact = new ArtifactReference("large", largeContent, "researcher");

        assertThat(artifact.getContentSize()).isEqualTo(100000);
        assertThat(artifact.getContent()).hasSize(100000);
    }
}
