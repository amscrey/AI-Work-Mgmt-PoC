package com.aiworkflow.workmanagement.orchestration.node.template;

import com.aiworkflow.workmanagement.orchestration.config.OrchestrationConfig;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Template node for task planning operations.
 * <p>
 * Generates deterministic task breakdown based on story metadata.
 * This node simulates what a logician agent would produce when creating
 * a structured task plan for implementing a story.
 *
 * <h2>Output Artifact</h2>
 * <ul>
 *   <li><strong>Name</strong>: task-breakdown</li>
 *   <li><strong>Role</strong>: logician</li>
 *   <li><strong>Format</strong>: Markdown with numbered tasks and acceptance criteria</li>
 * </ul>
 *
 * <h2>Template Structure</h2>
 * <pre>
 * # Task Breakdown: {story title}
 *
 * ## Overview
 * {summary of planned tasks}
 *
 * ## Tasks
 * ### Task 1: {task title}
 * - Description
 * - Acceptance criteria
 * - Dependencies
 *
 * ### Task 2: {task title}
 * ...
 *
 * ## Execution Order
 * Recommended sequence for task completion
 * </pre>
 */
@Component
@ConditionalOnProperty(name = "orchestration.execution-mode", havingValue = "template")
public class TaskPlanningTemplateNode extends AbstractTemplateNodeAction {

    public TaskPlanningTemplateNode(OrchestrationConfig config) {
        super(config);
    }

    @Override
    protected String getNodeName() {
        return "task-planning";
    }

    @Override
    protected String getArtifactName() {
        return "task-breakdown";
    }

    @Override
    protected String getRoleName() {
        return "logician";
    }

    @Override
    protected String generateContent(OrchestrationState state) {
        String storyTitle = getStoryTitle(state);
        String workIntent = getWorkIntent(state);
        String description = getStoryDescription(state);

        StringBuilder content = new StringBuilder();
        content.append("# Task Breakdown: ").append(storyTitle).append("\n\n");

        // Overview section
        content.append("## Overview\n\n");
        content.append("This task breakdown provides a structured approach to implementing the story. ");
        content.append("Tasks are ordered to minimize dependencies and enable parallel work where possible.\n\n");

        if (!workIntent.isEmpty()) {
            content.append("**Work Intent**: ").append(workIntent).append("\n\n");
        }

        // Tasks section
        content.append("## Tasks\n\n");

        // Task 1: Foundation
        content.append("### Task 1: Define Domain Model\n\n");
        content.append("**Description**: Create or update domain entities and value objects needed for this story.\n\n");
        content.append("**Acceptance Criteria**:\n");
        content.append("- [ ] All required entities defined\n");
        content.append("- [ ] Value objects created with proper validation\n");
        content.append("- [ ] Domain invariants enforced\n");
        content.append("- [ ] Unit tests cover all domain logic\n\n");
        content.append("**Dependencies**: None\n\n");
        content.append("**Estimated Effort**: Small (1-2 hours)\n\n");

        // Task 2: Service Layer
        content.append("### Task 2: Implement Service Layer\n\n");
        content.append("**Description**: Create or update service interfaces and implementations.\n\n");
        content.append("**Acceptance Criteria**:\n");
        content.append("- [ ] Service interface defined\n");
        content.append("- [ ] Implementation created with business logic\n");
        content.append("- [ ] Error handling implemented\n");
        content.append("- [ ] Unit and integration tests passing\n\n");
        content.append("**Dependencies**: Task 1 (Domain Model)\n\n");
        content.append("**Estimated Effort**: Medium (3-4 hours)\n\n");

        // Task 3: Integration
        content.append("### Task 3: Integration and Testing\n\n");
        content.append("**Description**: Integrate components and create end-to-end tests.\n\n");
        content.append("**Acceptance Criteria**:\n");
        content.append("- [ ] Components integrated successfully\n");
        content.append("- [ ] E2E tests cover main scenarios\n");
        content.append("- [ ] Error cases tested\n");
        content.append("- [ ] Documentation updated\n\n");
        content.append("**Dependencies**: Task 2 (Service Layer)\n\n");
        content.append("**Estimated Effort**: Medium (2-3 hours)\n\n");

        // Execution order section
        content.append("## Execution Order\n\n");
        content.append("**Recommended Sequence**:\n");
        content.append("1. Task 1 (Foundation) - Must be completed first\n");
        content.append("2. Task 2 (Service Layer) - Depends on Task 1\n");
        content.append("3. Task 3 (Integration) - Depends on Task 2\n\n");

        content.append("**Total Estimated Effort**: 6-9 hours\n\n");

        // Notes section
        content.append("## Notes\n\n");
        content.append("- Follow existing code patterns and conventions\n");
        content.append("- Ensure all tests pass before marking tasks complete\n");
        content.append("- Update documentation as you go\n");
        content.append("- Consider adding integration tests for critical paths\n\n");

        // Metadata footer
        content.append("---\n");
        content.append("*Generated by: Template Task Planning Node*\n");

        return content.toString();
    }
}
