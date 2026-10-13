package com.aiworkflow.workmanagement.orchestration.node.template;

import com.aiworkflow.workmanagement.orchestration.config.OrchestrationConfig;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Template node for design operations.
 * <p>
 * Generates deterministic design documentation based on story metadata.
 * This node simulates what a designer agent would produce when creating
 * architectural and implementation designs for a story.
 *
 * <h2>Output Artifact</h2>
 * <ul>
 *   <li><strong>Name</strong>: design-document</li>
 *   <li><strong>Role</strong>: designer</li>
 *   <li><strong>Format</strong>: Markdown with architecture diagrams, patterns, and decisions</li>
 * </ul>
 *
 * <h2>Template Structure</h2>
 * <pre>
 * # Design Document: {story title}
 *
 * ## Architecture Overview
 * High-level component structure
 *
 * ## Design Decisions
 * Key architectural choices and rationale
 *
 * ## Implementation Approach
 * Detailed implementation strategy
 *
 * ## API Design
 * Interfaces and contracts
 * </pre>
 */
@Component
@ConditionalOnProperty(name = "orchestration.execution-mode", havingValue = "template")
public class DesignTemplateNode extends AbstractTemplateNodeAction {

    public DesignTemplateNode(OrchestrationConfig config) {
        super(config);
    }

    @Override
    protected String getNodeName() {
        return "design";
    }

    @Override
    protected String getArtifactName() {
        return "design-document";
    }

    @Override
    protected String getRoleName() {
        return "designer";
    }

    private static final String PROJECT_PLANNING_ID = "PROJECT-PLANNING";

    /**
     * Checks if we're working on PROJECT-PLANNING (project-level orchestration).
     */
    private boolean isProjectPlanning(OrchestrationState state) {
        var story = state.getStory();
        if (story == null) {
            return true; // No story means project-level
        }
        String storyId = story.getId().getValue();
        return PROJECT_PLANNING_ID.equalsIgnoreCase(storyId);
    }

    @Override
    protected String generateContent(OrchestrationState state) {
        // For PROJECT-PLANNING, generate story suggestions instead of design docs
        if (isProjectPlanning(state)) {
            return generateStorySuggestions(state);
        }
        return generateDesignDocument(state);
    }

    /**
     * Generates a PROJECT-PLANNING response for template mode.
     * <p>
     * In template mode, we cannot dynamically generate story suggestions based on
     * the user's requirements - that requires LLM mode. Instead, we:
     * 1. Acknowledge the work intent
     * 2. Provide guidance on using LLM mode for real story generation
     * 3. Offer a manual endpoint for creating stories from existing artifacts
     * <p>
     * For actual story generation based on requirements, use LLM mode
     * (orchestration.execution-mode=llm) which will analyze the requirements
     * and generate appropriate story suggestions.
     */
    private String generateStorySuggestions(OrchestrationState state) {
        String workIntent = getWorkIntent(state);

        StringBuilder content = new StringBuilder();
        content.append("# PROJECT-PLANNING: Requirements Analysis\n\n");

        content.append("## Project Scope\n\n");
        if (!workIntent.isEmpty()) {
            content.append(workIntent).append("\n\n");
        } else {
            content.append("(No work intent provided)\n\n");
        }

        content.append("## Template Mode Notice\n\n");
        content.append("> **Note**: This is running in template mode (deterministic, no LLM calls).\n");
        content.append("> Template mode cannot dynamically analyze requirements and generate story suggestions.\n\n");

        content.append("### To Generate Real Story Suggestions\n\n");
        content.append("**Option 1: Use LLM Mode**\n");
        content.append("Set `orchestration.execution-mode=llm` in configuration to enable LLM-powered\n");
        content.append("analysis that will generate story suggestions based on your actual requirements.\n\n");

        content.append("**Option 2: Create Stories Manually**\n");
        content.append("If you already have PROJECT-PLANNING artifacts with story suggestions,\n");
        content.append("use the story creation endpoint:\n");
        content.append("```bash\n");
        content.append("curl -X POST 'http://localhost:8080/api/v1/project-planning/create-stories?requestedBy=human'\n");
        content.append("```\n\n");

        content.append("**Option 3: Create Stories via REST API**\n");
        content.append("Create stories directly:\n");
        content.append("```bash\n");
        content.append("curl -X POST http://localhost:8080/api/v1/stories \\\n");
        content.append("  -H 'Content-Type: application/json' \\\n");
        content.append("  -d '{\"storyId\":\"STORY-101\",\"title\":\"Your Story Title\",\"summary\":\"Description\",\"author\":\"human\"}'\n");
        content.append("```\n\n");

        content.append("## Requirements Captured\n\n");
        content.append("The following requirements were captured from the work intent:\n\n");
        if (!workIntent.isEmpty()) {
            // Echo back the requirements so they're preserved in artifacts
            content.append("```\n");
            content.append(workIntent);
            content.append("\n```\n\n");
        }

        content.append("---\n");
        content.append("*Generated by: Template Design Node (PROJECT-PLANNING mode)*\n");
        content.append("*For intelligent story generation, switch to LLM mode.*\n");

        return content.toString();
    }

    /**
     * Generates standard design document for regular stories.
     */
    private String generateDesignDocument(OrchestrationState state) {
        String storyTitle = getStoryTitle(state);
        String workIntent = getWorkIntent(state);
        String description = getStoryDescription(state);

        StringBuilder content = new StringBuilder();
        content.append("# Design Document: ").append(storyTitle).append("\n\n");

        if (!workIntent.isEmpty()) {
            content.append("**Work Intent**: ").append(workIntent).append("\n\n");
        }

        // Architecture Overview
        content.append("## Architecture Overview\n\n");
        content.append("This design follows the existing layered architecture pattern:\n\n");
        content.append("```\n");
        content.append("┌─────────────────────────────┐\n");
        content.append("│   Application Layer         │  (Services, DTOs)\n");
        content.append("├─────────────────────────────┤\n");
        content.append("│   Domain Layer              │  (Entities, Value Objects)\n");
        content.append("├─────────────────────────────┤\n");
        content.append("│   Infrastructure Layer      │  (Repositories, External)\n");
        content.append("└─────────────────────────────┘\n");
        content.append("```\n\n");

        // Design Decisions
        content.append("## Design Decisions\n\n");
        content.append("### 1. Domain-Driven Design\n");
        content.append("- **Decision**: Use rich domain model with behavior\n");
        content.append("- **Rationale**: Encapsulates business logic, improves maintainability\n");
        content.append("- **Trade-offs**: Slightly more complex than anemic model\n\n");

        content.append("### 2. Immutability\n");
        content.append("- **Decision**: Use immutable value objects with builders\n");
        content.append("- **Rationale**: Thread-safe, prevents accidental mutations\n");
        content.append("- **Trade-offs**: More object creation overhead (minimal)\n\n");

        content.append("### 3. Repository Pattern\n");
        content.append("- **Decision**: Abstract persistence behind repository interfaces\n");
        content.append("- **Rationale**: Decouples domain from infrastructure\n");
        content.append("- **Trade-offs**: Additional abstraction layer\n\n");

        // Implementation Approach
        content.append("## Implementation Approach\n\n");
        content.append("### Phase 1: Domain Model\n");
        content.append("1. Create/update entity classes\n");
        content.append("2. Define value objects with validation\n");
        content.append("3. Implement domain invariants\n");
        content.append("4. Add unit tests for domain logic\n\n");

        content.append("### Phase 2: Service Layer\n");
        content.append("1. Define service interfaces\n");
        content.append("2. Implement business logic\n");
        content.append("3. Add error handling\n");
        content.append("4. Create integration tests\n\n");

        content.append("### Phase 3: Integration\n");
        content.append("1. Wire up dependencies\n");
        content.append("2. Add configuration\n");
        content.append("3. Create E2E tests\n");
        content.append("4. Update documentation\n\n");

        // API Design
        content.append("## API Design\n\n");
        content.append("### Service Interface\n");
        content.append("```java\n");
        content.append("public interface ExampleService {\n");
        content.append("    // Main operation\n");
        content.append("    Result performOperation(Request request);\n");
        content.append("    \n");
        content.append("    // Query methods\n");
        content.append("    Optional<Entity> findById(EntityId id);\n");
        content.append("    List<Entity> findAll();\n");
        content.append("}\n");
        content.append("```\n\n");

        content.append("### Error Handling Strategy\n");
        content.append("- Use custom exceptions for domain errors\n");
        content.append("- Return Optional for nullable results\n");
        content.append("- Validate inputs at service boundary\n");
        content.append("- Log errors with appropriate context\n\n");

        // Quality Attributes
        content.append("## Quality Attributes\n\n");
        content.append("- **Testability**: All components unit-testable\n");
        content.append("- **Maintainability**: Clear separation of concerns\n");
        content.append("- **Extensibility**: Open for extension via interfaces\n");
        content.append("- **Reliability**: Comprehensive error handling\n\n");

        // Metadata footer
        content.append("---\n");
        content.append("*Generated by: Template Design Node*\n");

        return content.toString();
    }
}
