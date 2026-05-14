# Testing Guide - AI Work Management Platform

This document explains the complete test suite, how to run tests, and how to interpret results.

---

## Table of Contents

1. [Test Pyramid Overview](#test-pyramid-overview)
2. [Test Suite Summary](#test-suite-summary)
3. [Running Tests](#running-tests)
4. [Understanding Test Results](#understanding-test-results)
5. [Test Coverage](#test-coverage)
6. [Tooling & Frameworks](#tooling--frameworks)
7. [Writing New Tests](#writing-new-tests)
8. [Continuous Integration](#continuous-integration)

---

## Test Pyramid Overview

Our test suite follows the **Test Pyramid** approach:

```
         /\
        /  \  E2E Tests (count varies)
       /----\
      /      \  Integration Tests (count varies)
     /--------\
    /          \  Unit Tests (count varies)
   /____________\
```

### Test Layers

**1. Unit Tests (count varies)** - Foundation
- **Purpose**: Test individual classes and methods in isolation
- **Speed**: Very fast (~2 seconds total)
- **Scope**: Single class with mocked dependencies
- **Coverage**: Domain layer + Application layer
- **Tools**: JUnit 5, Mockito, AssertJ

**2. Integration Tests (count varies)** - Middle Layer
- **Purpose**: Test component interactions with real I/O (file system)
- **Speed**: Fast (~2 seconds total)
- **Scope**: Repository implementations and activity logging with actual file operations
- **Coverage**: Infrastructure layer (Story, Task, Comment, ActivityLogger, Verification)
- **Tools**: JUnit 5, AssertJ, Real file system

**3. End-to-End Tests (count varies)** - Top
- **Purpose**: Test complete user workflows from service layer through persistence
- **Speed**: Fast (~1 second total)
- **Scope**: Full stack (services → repositories → file system → activity logging)
- **Coverage**: Story workflows, task lifecycle, comments, multi-entity coordination, error scenarios
- **Tools**: JUnit 5, AssertJ, All real components (no mocks)

---

## Test Suite Summary

### Current Test Count: **See latest `mvn verify` output** ✅

```
✅ Unit Tests:           count varies
✅ Integration Tests:     count varies
✅ E2E Tests:             count varies (may include skips)
```

**Latest Run (2026-04-05):**
- ✅ `mvn verify` runs the full test suite (see console for totals)
- ✅ JaCoCo coverage check passed (>= 80%)
- Note: totals include nested test classes executed by JUnit 5

**Recent Completions:**
- ✅ **2026-03-31**: End-to-End test suite implemented
- ✅ **2026-03-31**: Service-ActivityLogger integration complete
- ✅ **2026-03-31**: Test cleanup pattern improved (artifacts left in target/)
- ✅ **2026-03-31**: Workspace verification test added
- ✅ **2026-03-31**: Directory structure updated (backlog/prioritized only)

### Test Organization

```
src/test/java/com/aiworkflow/workmanagement/
├── domain/
│   ├── model/
│   │   ├── StoryTest.java
│   │   └── WorkflowStateTest.java
│   ├── service/
│   │   └── StateTransitionValidatorTest.java
│   └── valueobject/
│       └── StoryIdTest.java
│
├── application/
│   └── service/impl/
│       ├── StoryServiceImplTest.java
│       ├── TaskServiceImplTest.java
│       └── CommentServiceImplTest.java
│
├── infrastructure/
│   └── persistence/
│       ├── FileBasedStoryRepositoryIT.java
│       ├── FileBasedTaskRepositoryIT.java
│       ├── FileBasedCommentRepositoryIT.java
│       ├── FileBasedActivityLoggerIT.java
│       └── WorkspaceVerificationIT.java
│
└── e2e/
    └── WorkflowEndToEndIT.java
        ├── StoryWorkflowTests
        ├── TaskLifecycleTests
        ├── CommentWorkflowTests
        ├── MultiEntityCoordinationTests
        ├── ErrorScenarioTests
        └── PrioritizationWorkflowTests (skipped)
```

---

## Running Tests

### Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.x or higher
- **OS**: macOS, Linux, or Windows

### Quick Start

```bash
# Run all tests (unit + integration + E2E)
mvn test

# Run tests with coverage check (includes repackage)
mvn verify

# Run tests with coverage report
mvn clean test jacoco:report
```

### Running Specific Test Types

#### 1. Unit Tests Only (count varies)

```bash
# Unit tests only (exclude *IT)
mvn test -Dtest='**/*Test,**/*Tests,**/*TestCase'

# Specific test class
mvn test -Dtest=StoryTest

# Specific test method
mvn test -Dtest=StoryTest#shouldCreateStoryWithValidInputs

# All tests in a package
mvn test -Dtest=com.aiworkflow.workmanagement.domain.**
```

**Expected Output:**
```
[INFO] Tests run: <total>, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### 2. Integration Tests Only (count varies)

```bash
# All integration tests (IT suffix)
mvn test -Dtest='*IT'

# Specific integration test
mvn test -Dtest=FileBasedStoryRepositoryIT

# Story repository tests only
mvn test -Dtest=FileBasedStoryRepositoryIT
```

**Expected Output:**
```
[INFO] Tests run: <total>, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Integration Test Workspace Configuration:**

Integration tests use isolated test workspaces in the `target/` directory to avoid interfering with production data or each other. Each repository test suite uses its own dedicated workspace:

| Test Suite | Workspace Directory | Purpose |
|------------|---------------------|---------|
| FileBasedStoryRepositoryIT | `target/test-workspace-story` | Story persistence testing |
| FileBasedTaskRepositoryIT | `target/test-workspace-task` | Task persistence testing |
| FileBasedCommentRepositoryIT | `target/test-workspace-comment` | Comment persistence testing |
| FileBasedActivityLoggerIT | `target/test-workspace-activity-log` | Activity logging testing |

**How Workspaces Work:**

1. **Isolation**: Each test workspace is completely independent
2. **Lifecycle** (Inspection-Friendly Pattern):
   - `@BeforeEach` - Workspace cleaned from **previous run** (if exists)
   - Test executes with fresh workspace
   - **No `@AfterEach` cleanup** - Artifacts left in `target/` for inspection! 🔍
   - Next test run cleans up the old workspace before starting
3. **Location**: `target/` directory means:
   - Workspaces persist between test runs for inspection
   - Deleted by `mvn clean` (complete cleanup)
   - Not committed to version control (target/ is .gitignored)
   - Automatically cleaned and recreated on next test run

**💡 Developer Benefit**: You can inspect test artifacts after running tests!
- Run tests: `mvn test -Dtest='*IT'`
- Browse created files in `target/test-workspace-*/`
- Examine story.json, task.json, comment markdown files
- Review activity log entries (*.jsonl)
- Next test run will clean up and create fresh artifacts

**🔍 Inspecting Test Artifacts:**

After running integration tests, you can inspect the created files to understand what the system produces:

```bash
# Run integration tests
mvn test -Dtest='*IT'

# Browse the created workspaces
ls -la target/test-workspace-*

# View a story JSON file
cat target/test-workspace-story/backlog/STORY-001/story.json

# View a comment markdown file
cat target/test-workspace-comment/backlog/STORY-001/comments/*.md

# View activity logs (JSON Lines format)
cat target/test-workspace-activity-log/activity-log/agent.jsonl

# See complete workspace structure
tree target/test-workspace-verification  # From WorkspaceVerificationIT
```

**File System Structure Created During Tests:**

```
target/test-workspace-{repository}/
  backlog/                          # Stories in BACKLOG or DEPRIORITIZED state
    STORY-001/
      story.json                    # Story metadata (JSON format)
      tasks/
        TASK-001.json               # Task JSON files
        TASK-002.json
      comments/
        comment__agent__2026-03-30T141530Z.md      # Markdown with YAML frontmatter
        comment__human-reviewer__2026-03-31T100000Z.md
  prioritized/                      # Stories in PRIORITIZED state (actively worked on)
    STORY-002/
      story.json
      tasks/
      comments/
  activity-log/                     # Activity logs (ActivityLogger workspace only)
    agent.jsonl                     # Per-role JSON Lines log files
    human-reviewer.jsonl
    product-owner.jsonl

**Note**: When a story is DEPRIORITIZED, it returns to the backlog/ directory (not a separate directory).
```

**Test Boundaries & What's Verified:**

✅ **File System Operations:**
- Directory creation (backlog/, prioritized/)
- Subdirectory creation (tasks/, comments/)
- File creation (JSON for stories/tasks, Markdown for comments)
- File deletion and cleanup
- Directory deletion (recursive)

✅ **Persistence Formats:**
- JSON serialization/deserialization (Jackson)
- Pretty-printed JSON with indentation
- ISO-8601 timestamp format
- YAML frontmatter in Markdown files
- Multi-line content handling

✅ **Data Integrity:**
- All entity fields persist correctly
- State transitions preserved
- Timestamps recorded accurately
- Null values handled properly
- Special characters in content

✅ **Repository Operations:**
- CRUD operations (Create, Read, Update, Delete)
- Query operations (findByWorkflowState, findByPrioritizationState, etc.)
- Cross-directory searches (tasks/comments across all prioritization states)
- Existence checks
- Error handling (not found, validation errors)

✅ **Activity Logging Operations:**
- Log directory and per-role file creation
- JSON Lines format (one JSON object per line)
- Success/failure outcome logging
- Timestamp, role, activity, storyId persistence
- Complex details serialization
- Multiple role independence

✅ **Multi-Entity Scenarios:**
- Multiple stories in different prioritization states
- Multiple tasks per story
- Multiple comments per story
- Entity independence (updates don't affect other entities)

**What's NOT Tested in Integration Tests:**

❌ Business logic (covered by unit tests)
❌ Service layer coordination (covered by unit tests)
❌ API endpoints (will be covered by E2E tests)
❌ Concurrent access (planned for future E2E tests)
❌ Production workspace operations

**Important Notes for Developers:**

> ⚠️ **Maintaining Test Documentation**: When modifying integration tests, update this section in `etc/docs/TESTING.md` to reflect:
> - New test workspaces
> - Changed file system structures
> - New test boundaries
> - Additional verification scenarios
>
> This ensures both human and AI developers understand the test coverage.

#### 3. All Tests (run by default)

```bash
# Run everything (unit + integration + E2E)
mvn test

# Run everything with coverage check
mvn clean verify
```

**Expected Output:**
```
[INFO] Tests run: <total>, Failures: 0, Errors: 0, Skipped: <total>
[INFO] BUILD SUCCESS
```

Note: Integration and E2E tests are included in default `mvn test`.

### Running Tests by Layer

```bash
# Domain layer only
mvn test -Dtest=com.aiworkflow.workmanagement.domain.**

# Application layer only
mvn test -Dtest=com.aiworkflow.workmanagement.application.**

# Infrastructure layer only
mvn test -Dtest=com.aiworkflow.workmanagement.infrastructure.**
```

### Running Tests in Your IDE

#### IntelliJ IDEA
1. Right-click on test class or method
2. Select "Run 'TestName'"
3. View results in Run window

#### VS Code
1. Install "Java Test Runner" extension
2. Click ▶️ icon next to test class/method
3. View results in Test Explorer

#### Eclipse
1. Right-click on test class
2. Run As → JUnit Test
3. View results in JUnit view

---

## Understanding Test Results

### Successful Test Run

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.aiworkflow.workmanagement.domain.model.StoryTest
[INFO] Tests run: <total>, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.145 s
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: <total>, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

✅ **Interpretation:**
- All tests passed
- No failures or errors
- Build is healthy

### Failed Test Run

```
[ERROR] Tests run: <total>, Failures: 1, Errors: 0, Skipped: 0
[ERROR]
[ERROR] Failures:
[ERROR]   StoryTest.shouldCreateStory:45
Expected: "STORY-001"
     but: was "STORY-002"

[INFO] BUILD FAILURE
```

❌ **Interpretation:**
- 1 test failed
- Assertion mismatch at line 45
- Build failed

### Error vs Failure

**Failure**: Assertion didn't match expected value
```
Expected: "TODO"
but was: "IN_PROGRESS"
```

**Error**: Exception was thrown
```
java.lang.NullPointerException at StoryTest.java:50
```

### Test Execution Time

```
[INFO] Tests run: <total>, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.2 s
```

**Performance Benchmarks:**
- Unit Tests: ~4 seconds (count varies)
- Integration Tests: ~3 seconds (count varies)
- Total: ~7 seconds

If tests run slower, check for:
- File I/O bottlenecks
- Missing @BeforeEach/@AfterEach cleanup
- Network calls (should be mocked)

---

## Test Coverage

### Generating Coverage Reports

```bash
# Run tests with coverage
mvn clean test jacoco:report

# Open the report
open target/site/jacoco/index.html
```

### Reading Coverage Reports

**Coverage Metrics:**
- **Line Coverage**: Percentage of code lines executed
- **Branch Coverage**: Percentage of if/else branches tested
- **Method Coverage**: Percentage of methods called

**Current Coverage:**
```
Overall Coverage: ~85%
├── Domain Layer:      95% coverage
├── Application Layer: 90% coverage
└── Infrastructure:    80% coverage
```

**Coverage Report Location:**
```
target/site/jacoco/
├── index.html         # Main report
├── com.aiworkflow.../  # Package reports
└── jacoco.xml         # Machine-readable format
```

### Coverage Goals

- **Minimum**: 80% line coverage (enforced by Maven)
- **Target**: 90% line coverage
- **Critical Paths**: 100% coverage (state transitions, validations)

### Viewing Coverage in IDE

**IntelliJ IDEA:**
1. Run → Run with Coverage
2. View green/red highlighting in editor
3. Coverage tool window shows percentages

**VS Code:**
1. Install "Coverage Gutters" extension
2. Run tests with coverage
3. View coverage in editor gutter

---

## Tooling & Frameworks

### Testing Framework: JUnit 5

**Features Used:**
- `@Test` - Mark test methods
- `@DisplayName` - Human-readable test names
- `@Nested` - Group related tests
- `@BeforeEach`/`@AfterEach` - Setup/teardown
- `@ExtendWith` - Mockito integration

**Example:**
```java
@DisplayName("Story Creation Tests")
class StoryTest {

    @Test
    @DisplayName("Should create story with valid inputs")
    void shouldCreateStoryWithValidInputs() {
        // Test implementation
    }
}
```

### Assertion Library: AssertJ

**Why AssertJ:**
- Fluent, readable assertions
- Better error messages
- Rich API for collections, exceptions, etc.

**Examples:**
```java
// Instead of: assertEquals("STORY-001", story.getId())
assertThat(story.getId()).isEqualTo("STORY-001");

// Collections
assertThat(tasks).hasSize(5)
    .allMatch(t -> t.isCompleted());

// Exceptions
assertThatThrownBy(() -> story.transitionTo(DONE))
    .isInstanceOf(InvalidStateTransitionException.class)
    .hasMessageContaining("Invalid transition");
```

### Mocking Framework: Mockito

**Purpose**: Mock dependencies in unit tests

**Examples:**
```java
@Mock
private StoryRepository storyRepository;

@Test
void shouldSaveStory() {
    // Given
    when(storyRepository.save(any())).thenReturn(story);

    // When
    service.createStory(command);

    // Then
    verify(storyRepository).save(any(Story.class));
}
```

### Coverage Tool: JaCoCo

**Configuration** (pom.xml):
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <limits>
                    <limit>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

### Build Tool: Maven Surefire

**Configuration:**
- Runs tests automatically during `mvn test`
- Generates reports in `target/surefire-reports/`
- Supports parallel execution (not currently enabled)

---

## Writing New Tests

### Unit Test Template

```java
package com.aiworkflow.workmanagement.domain.model;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("MyClass Tests")
class MyClassTest {

    private MyClass instance;

    @BeforeEach
    void setUp() {
        instance = new MyClass();
    }

    @Test
    @DisplayName("Should do something when condition is met")
    void shouldDoSomethingWhenConditionMet() {
        // Given (Arrange)
        String input = "test";

        // When (Act)
        String result = instance.doSomething(input);

        // Then (Assert)
        assertThat(result).isEqualTo("expected");
    }

    @Test
    @DisplayName("Should throw exception when invalid input")
    void shouldThrowExceptionWhenInvalidInput() {
        // When/Then
        assertThatThrownBy(() -> instance.doSomething(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

### Integration Test Template

```java
package com.aiworkflow.workmanagement.infrastructure;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;

@DisplayName("MyRepository Integration Tests")
class MyRepositoryIT {

    private static final String TEST_WORKSPACE = "target/test-workspace";
    private MyRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        cleanWorkspace();
        repository = new MyRepository(TEST_WORKSPACE);
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanWorkspace();
    }

    private void cleanWorkspace() throws IOException {
        Path workspace = Paths.get(TEST_WORKSPACE);
        if (Files.exists(workspace)) {
            Files.walk(workspace)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    @Test
    @DisplayName("Should persist data to file system")
    void shouldPersistDataToFileSystem() throws IOException {
        // Given
        MyEntity entity = new MyEntity("id", "data");

        // When
        repository.save(entity);

        // Then
        Path file = Paths.get(TEST_WORKSPACE, "id.json");
        assertThat(Files.exists(file)).isTrue();

        MyEntity loaded = repository.findById("id").orElseThrow();
        assertThat(loaded.getData()).isEqualTo("data");
    }
}
```

### Test Naming Conventions

**Class Names:**
- Unit tests: `<ClassName>Test.java`
- Integration tests: `<ClassName>IT.java`

**Method Names:**
- Use `should...When...` pattern
- Example: `shouldReturnEmptyWhenStoryNotFound`
- Or: `shouldThrowExceptionWhenInvalidInput`

**Display Names:**
- Use natural language
- Start with "Should"
- Example: `"Should create story with valid inputs"`

### Best Practices

1. **Arrange-Act-Assert** pattern
   ```java
   // Given (Arrange)
   Story story = new Story(...);

   // When (Act)
   story.transitionTo(IN_PROGRESS);

   // Then (Assert)
   assertThat(story.getState()).isEqualTo(IN_PROGRESS);
   ```

2. **One assertion per test** (when possible)
   - Focus tests on single behavior
   - Makes failures easier to diagnose

3. **Use @Nested for grouping**
   ```java
   @Nested
   @DisplayName("Create Operations")
   class CreateTests {
       // Related create tests
   }
   ```

4. **Clean up resources**
   - Use @AfterEach for cleanup
   - Especially important for integration tests
   - Delete test files/directories

5. **Mock external dependencies**
   - Don't hit real databases/APIs in unit tests
   - Use Mockito for mocking
   - Integration tests can use real I/O

---

## Continuous Integration

### Running Tests in CI/CD

**GitHub Actions Example:**
```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'

      - name: Run tests
        run: mvn clean verify

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml
```

### Pre-commit Hooks

Recommended pre-commit hook:
```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Running tests before commit..."
mvn clean test

if [ $? -ne 0 ]; then
    echo "Tests failed! Commit aborted."
    exit 1
fi
```

### Build Verification

Before pushing code, always run:
```bash
# Clean build with all tests
mvn clean verify

# Check coverage
mvn jacoco:check
```

---

## Troubleshooting

### Common Issues

**Issue: Tests fail with file permission errors**
```
Solution: Check @AfterEach cleanup, ensure files aren't locked
```

**Issue: Slow test execution**
```
Solution:
- Check for Thread.sleep() calls
- Verify mocks are being used (not real I/O)
- Look for missing @BeforeEach/@AfterEach
```

**Issue: Flaky tests (intermittent failures)**
```
Solution:
- Check for time-dependent assertions
- Verify proper cleanup between tests
- Look for race conditions
```

**Issue: Coverage report not generated**
```
Solution: Run mvn clean test jacoco:report
(must run 'test' goal before 'jacoco:report')
```

### Getting Help

- Check Maven output for detailed stack traces
- Review test logs in `target/surefire-reports/`
- Use `-X` flag for debug output: `mvn test -X -Dtest=MyTest`
- Check JaCoCo report for untested code paths

---

## Summary

### Quick Reference

```bash
# Run all unit tests
mvn test

# Run specific test
mvn test -Dtest=StoryTest

# Run integration tests
mvn test -Dtest=*IT

# Run all tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Pyramid

```
✅ Unit Tests      → Foundation (fast)
✅ Integration Tests → Middle (fast)
✅ E2E Tests        → Top (fast)
───────────────────────
✅ Total tests: see latest `mvn verify` output
```

### Success Criteria

✅ All tests passing (see latest totals)
✅ Coverage ≥ 80% (JaCoCo check enforced)
✅ No flaky tests
✅ Fast execution (< 15 seconds total)

---

**Last Updated**: 2026-04-05
**Test Count**: see latest `mvn verify` output
**Coverage**: ≥ 80% (JaCoCo check)
