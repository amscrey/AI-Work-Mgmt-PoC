package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationContext;
import com.aiworkflow.workmanagement.orchestration.domain.ReferenceFile;
import com.aiworkflow.workmanagement.orchestration.service.PromptTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of PromptTemplateService.
 *
 * Produces deterministic prompts with stable section ordering.
 */
@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private static final int MAX_REFERENCE_CHARS = 600;

    @Override
    public String buildResearchPrompt(OrchestrationContext context) {
        return buildPrompt(context, PromptOperation.RESEARCH);
    }

    @Override
    public String buildTaskPlanningPrompt(OrchestrationContext context) {
        return buildPrompt(context, PromptOperation.TASK_PLANNING);
    }

    @Override
    public String buildDesignPrompt(OrchestrationContext context) {
        return buildPrompt(context, PromptOperation.DESIGN);
    }

    @Override
    public String buildTestStrategyPrompt(OrchestrationContext context) {
        return buildPrompt(context, PromptOperation.TEST_STRATEGY);
    }

    private String buildPrompt(OrchestrationContext context, PromptOperation operation) {
        Story story = context.getStory();
        StringBuilder prompt = new StringBuilder();

        appendHeader(prompt, operation, context.getWorkIntent());
        appendStorySection(prompt, story);

        if (context.hasTasks()) {
            appendTaskSection(prompt, context.getTasks());
        }

        if (context.hasReferenceFiles()) {
            appendReferenceSection(prompt, context.getReferenceFiles());
        }

        appendInstructionSection(prompt, operation);

        return prompt.toString();
    }

    private void appendHeader(StringBuilder prompt, PromptOperation operation, String workIntent) {
        prompt.append("# ").append(operation.getDisplayName()).append(" Prompt").append('\n');
        prompt.append("Mode: llm").append('\n');
        prompt.append("Work Intent: ").append(workIntent).append('\n');
        prompt.append("Constraints: No real LLM calls during automated tests.").append("\n\n");
    }

    private void appendStorySection(StringBuilder prompt, Story story) {
        prompt.append("## Story").append('\n');
        prompt.append("Id: ").append(story.getId().getValue()).append('\n');
        prompt.append("Title: ").append(nullSafe(story.getTitle())).append('\n');
        prompt.append("Summary: ").append(nullSafe(story.getSummary())).append('\n');
        prompt.append("Description: ").append(nullSafe(story.getDescription())).append("\n\n");

        prompt.append("### Acceptance Criteria").append('\n');
        if (story.getAcceptanceCriteria().isEmpty()) {
            prompt.append("- (none)").append('\n');
        } else {
            for (String criterion : story.getAcceptanceCriteria()) {
                prompt.append("- ").append(criterion).append('\n');
            }
        }
        prompt.append('\n');
    }

    private void appendTaskSection(StringBuilder prompt, List<Task> tasks) {
        prompt.append("## Tasks").append('\n');
        for (Task task : tasks) {
            prompt.append("- ").append(nullSafe(task.getTitle()));
            if (task.getDescription() != null && !task.getDescription().isBlank()) {
                prompt.append(": ").append(task.getDescription());
            }
            prompt.append('\n');
        }
        prompt.append('\n');
    }

    private void appendReferenceSection(StringBuilder prompt, List<ReferenceFile> referenceFiles) {
        prompt.append("## Reference Files").append('\n');
        for (ReferenceFile reference : referenceFiles) {
            prompt.append("- ").append(reference.getFileName())
                .append(" (path: ").append(reference.getFilePath()).append(")\n");
            prompt.append("  Excerpt: ").append(truncate(reference.getContent(), MAX_REFERENCE_CHARS)).append('\n');
        }
        prompt.append('\n');
    }

    private void appendInstructionSection(StringBuilder prompt, PromptOperation operation) {
        prompt.append("## Instructions").append('\n');
        prompt.append(operation.getInstructions()).append('\n');
        prompt.append("- Respond in Markdown with headings and bullet lists.\n");
        prompt.append("- Use short sections and avoid overly long paragraphs.\n");
    }

    private String truncate(String content, int maxChars) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxChars) {
            return content;
        }
        return content.substring(0, maxChars) + " [TRUNCATED]";
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private enum PromptOperation {
        RESEARCH("Research", "Provide research findings, risks, and relevant context."),
        TASK_PLANNING("Task Planning", "Break down the story into executable tasks with sequencing."),
        DESIGN("Design", "Produce a technical design aligned with the story requirements."),
        TEST_STRATEGY("Test Strategy", "Outline a test plan and key test cases for the story.");

        private final String displayName;
        private final String instructions;

        PromptOperation(String displayName, String instructions) {
            this.displayName = displayName;
            this.instructions = instructions;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getInstructions() {
            return instructions;
        }
    }
}
