package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationContext;
import com.aiworkflow.workmanagement.orchestration.domain.ReferenceFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptTemplateServiceImplTest {

    private final PromptTemplateServiceImpl service = new PromptTemplateServiceImpl();

    @Test
    void buildsResearchPromptWithStoryAndCriteria() {
        OrchestrationContext context = buildContext(List.of(), List.of());

        String prompt = service.buildResearchPrompt(context);

        assertThat(prompt).contains("# Research Prompt");
        assertThat(prompt).contains("Work Intent:");
        assertThat(prompt).contains("## Story");
        assertThat(prompt).contains("### Acceptance Criteria");
        assertThat(prompt).contains("- Must support LLM prompts");
    }

    @Test
    void omitsTasksSectionWhenNoTasks() {
        OrchestrationContext context = buildContext(List.of(), List.of());

        String prompt = service.buildDesignPrompt(context);

        assertThat(prompt).doesNotContain("## Tasks");
    }

    @Test
    void includesTasksSectionWhenTasksPresent() {
        Task task = new Task(new TaskId("TASK-1"), new StoryId("STORY-010"), "Draft prompt templates");
        task.updateDescription("Add deterministic prompt sections.");
        OrchestrationContext context = buildContext(List.of(task), List.of());

        String prompt = service.buildTaskPlanningPrompt(context);

        assertThat(prompt).contains("## Tasks");
        assertThat(prompt).contains("Draft prompt templates: Add deterministic prompt sections.");
    }

    @Test
    void truncatesReferenceContentWhenTooLong(@TempDir Path tempDir) throws IOException {
        String content = "A".repeat(800);
        Path file = tempDir.resolve("reference.md");
        Files.writeString(file, content);

        ReferenceFile referenceFile = ReferenceFile.fromFile(file);
        OrchestrationContext context = buildContext(List.of(), List.of(referenceFile));

        String prompt = service.buildTestStrategyPrompt(context);

        assertThat(prompt).contains("## Reference Files");
        assertThat(prompt).contains("[TRUNCATED]");
    }

    @Test
    void showsNoneWhenAcceptanceCriteriaEmpty() {
        Story story = new Story(
            new StoryId("STORY-010"),
            "Prompt Template Service",
            "Builds LLM prompts",
            WorkflowState.TODO,
            PrioritizationState.PRIORITIZED
        );
        story.setDescription("Implement prompt building for LLM nodes.");

        OrchestrationContext context = OrchestrationContext.builder()
            .story(story)
            .workIntent("Generate prompts for LLM nodes")
            .build();

        String prompt = service.buildResearchPrompt(context);

        assertThat(prompt).contains("### Acceptance Criteria");
        assertThat(prompt).contains("- (none)");
    }

    @Test
    void doesNotTruncateReferenceWhenBelowLimit(@TempDir Path tempDir) throws IOException {
        String content = "Short reference content";
        Path file = tempDir.resolve("reference.md");
        Files.writeString(file, content);

        ReferenceFile referenceFile = ReferenceFile.fromFile(file);
        OrchestrationContext context = buildContext(List.of(), List.of(referenceFile));

        String prompt = service.buildDesignPrompt(context);

        assertThat(prompt).contains("Excerpt: " + content);
        assertThat(prompt).doesNotContain("[TRUNCATED]");
    }

    @Test
    void includesReferenceFilePathAndName(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("reference.md");
        Files.writeString(file, "Reference content");

        ReferenceFile referenceFile = ReferenceFile.fromFile(file);
        OrchestrationContext context = buildContext(List.of(), List.of(referenceFile));

        String prompt = service.buildTestStrategyPrompt(context);

        assertThat(prompt).contains(referenceFile.getFileName());
        assertThat(prompt).contains(referenceFile.getFilePath().toString());
    }

    @Test
    void preservesSectionOrdering() {
        OrchestrationContext context = buildContext(List.of(), List.of());

        String prompt = service.buildTaskPlanningPrompt(context);

        int headerIndex = prompt.indexOf("# Task Planning Prompt");
        int storyIndex = prompt.indexOf("## Story");
        int instructionsIndex = prompt.indexOf("## Instructions");

        assertThat(headerIndex).isGreaterThanOrEqualTo(0);
        assertThat(storyIndex).isGreaterThan(headerIndex);
        assertThat(instructionsIndex).isGreaterThan(storyIndex);
    }

    @Test
    void rejectsBlankWorkIntent() {
        Story story = new Story(
            new StoryId("STORY-010"),
            "Prompt Template Service",
            "Builds LLM prompts",
            WorkflowState.TODO,
            PrioritizationState.PRIORITIZED
        );
        story.setDescription("Implement prompt building for LLM nodes.");

        assertThatThrownBy(() -> OrchestrationContext.builder()
            .story(story)
            .workIntent(" ")
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Work intent cannot be null or blank");
    }

    private OrchestrationContext buildContext(List<Task> tasks, List<ReferenceFile> references) {
        Story story = new Story(
            new StoryId("STORY-010"),
            "Prompt Template Service",
            "Builds LLM prompts",
            WorkflowState.TODO,
            PrioritizationState.PRIORITIZED
        );
        story.setDescription("Implement prompt building for LLM nodes.");
        story.addAcceptanceCriterion("Must support LLM prompts");

        return OrchestrationContext.builder()
            .story(story)
            .workIntent("Generate prompts for LLM nodes")
            .tasks(tasks)
            .referenceFiles(references)
            .build();
    }
}

