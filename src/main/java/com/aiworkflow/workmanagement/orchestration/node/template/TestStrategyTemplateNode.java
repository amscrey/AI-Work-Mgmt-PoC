package com.aiworkflow.workmanagement.orchestration.node.template;

import com.aiworkflow.workmanagement.orchestration.config.OrchestrationConfig;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Template node for test strategy operations.
 * <p>
 * Generates deterministic test strategy documentation based on story metadata.
 * This node simulates what a tester agent would produce when planning
 * comprehensive test coverage for a story.
 *
 * <h2>Output Artifact</h2>
 * <ul>
 *   <li><strong>Name</strong>: test-strategy</li>
 *   <li><strong>Role</strong>: tester</li>
 *   <li><strong>Format</strong>: Markdown with test cases, coverage plan, and scenarios</li>
 * </ul>
 *
 * <h2>Template Structure</h2>
 * <pre>
 * # Test Strategy: {story title}
 *
 * ## Test Coverage Plan
 * Overview of testing approach
 *
 * ## Unit Tests
 * Component-level test cases
 *
 * ## Integration Tests
 * Integration test scenarios
 *
 * ## E2E Tests
 * End-to-end test cases
 * </pre>
 */
@Component
@ConditionalOnProperty(name = "orchestration.execution-mode", havingValue = "template")
public class TestStrategyTemplateNode extends AbstractTemplateNodeAction {

    public TestStrategyTemplateNode(OrchestrationConfig config) {
        super(config);
    }

    @Override
    protected String getNodeName() {
        return "test-strategy";
    }

    @Override
    protected String getArtifactName() {
        return "test-strategy";
    }

    @Override
    protected String getRoleName() {
        return "tester";
    }

    @Override
    protected String generateContent(OrchestrationState state) {
        String storyTitle = getStoryTitle(state);
        String workIntent = getWorkIntent(state);
        String description = getStoryDescription(state);

        StringBuilder content = new StringBuilder();
        content.append("# Test Strategy: ").append(storyTitle).append("\n\n");

        if (!workIntent.isEmpty()) {
            content.append("**Work Intent**: ").append(workIntent).append("\n\n");
        }

        // Test Coverage Plan
        content.append("## Test Coverage Plan\n\n");
        content.append("This test strategy follows the testing pyramid approach:\n\n");
        content.append("```\n");
        content.append("       ╱╲\n");
        content.append("      ╱E2E╲      (Few - Critical paths)\n");
        content.append("     ╱────╲\n");
        content.append("    ╱ Intg ╲     (Some - Component integration)\n");
        content.append("   ╱────────╲\n");
        content.append("  ╱   Unit   ╲   (Many - Business logic)\n");
        content.append(" ╱────────────╲\n");
        content.append("```\n\n");

        content.append("**Coverage Goals**:\n");
        content.append("- Unit tests: >80% code coverage\n");
        content.append("- Integration tests: All service interactions\n");
        content.append("- E2E tests: Main user workflows\n\n");

        // Unit Tests
        content.append("## Unit Tests\n\n");
        content.append("### Domain Model Tests\n");
        content.append("- Entity creation and validation\n");
        content.append("- Value object immutability\n");
        content.append("- Domain invariant enforcement\n");
        content.append("- Business logic correctness\n\n");

        content.append("### Service Layer Tests\n");
        content.append("- Service method behavior (happy paths)\n");
        content.append("- Error handling and validation\n");
        content.append("- Edge cases and boundary conditions\n");
        content.append("- Null safety and optional handling\n\n");

        content.append("**Example Test Cases**:\n");
        content.append("```java\n");
        content.append("@Test\n");
        content.append("void shouldCreateEntityWithValidData() {\n");
        content.append("    // Given: Valid input data\n");
        content.append("    // When: Entity created\n");
        content.append("    // Then: Entity has correct state\n");
        content.append("}\n\n");
        content.append("@Test\n");
        content.append("void shouldRejectInvalidData() {\n");
        content.append("    // Given: Invalid input\n");
        content.append("    // When: Validation runs\n");
        content.append("    // Then: Exception thrown with message\n");
        content.append("}\n");
        content.append("```\n\n");

        // Integration Tests
        content.append("## Integration Tests\n\n");
        content.append("### Service Integration Tests\n");
        content.append("- Service interaction with repositories\n");
        content.append("- Transaction behavior\n");
        content.append("- External service integration (if applicable)\n");
        content.append("- Configuration loading\n\n");

        content.append("**Test Scenarios**:\n");
        content.append("1. **Happy Path**: Complete workflow with valid data\n");
        content.append("2. **Error Recovery**: Graceful handling of failures\n");
        content.append("3. **Concurrent Access**: Thread safety verification\n");
        content.append("4. **Data Persistence**: Verify data correctly saved/loaded\n\n");

        // E2E Tests
        content.append("## E2E Tests\n\n");
        content.append("### Critical User Workflows\n");
        content.append("1. **Main Workflow**: End-to-end happy path\n");
        content.append("   - Setup: Prepare test data\n");
        content.append("   - Execute: Run complete workflow\n");
        content.append("   - Verify: Check all side effects\n\n");

        content.append("2. **Error Scenario**: Error handling verification\n");
        content.append("   - Setup: Create error conditions\n");
        content.append("   - Execute: Trigger error\n");
        content.append("   - Verify: Graceful degradation\n\n");

        // Test Data Strategy
        content.append("## Test Data Strategy\n\n");
        content.append("- **Unit Tests**: Use builders and test fixtures\n");
        content.append("- **Integration Tests**: Use @TempDir for file-based tests\n");
        content.append("- **E2E Tests**: Create realistic test scenarios\n");
        content.append("- **Cleanup**: Ensure tests are isolated and repeatable\n\n");

        // Quality Gates
        content.append("## Quality Gates\n\n");
        content.append("All tests must pass before story completion:\n");
        content.append("- [ ] All unit tests passing\n");
        content.append("- [ ] All integration tests passing\n");
        content.append("- [ ] All E2E tests passing\n");
        content.append("- [ ] Code coverage >80%\n");
        content.append("- [ ] No failing assertions\n");
        content.append("- [ ] No ignored/skipped tests without justification\n\n");

        // Metadata footer
        content.append("---\n");
        content.append("*Generated by: Template Test Strategy Node*\n");

        return content.toString();
    }
}
