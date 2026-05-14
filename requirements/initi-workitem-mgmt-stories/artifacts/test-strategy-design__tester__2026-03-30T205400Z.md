# Test Strategy Design

**Role**: tester
**Story**: STORY-013
**Status**: Complete

## Testing Pyramid

```
         ┌────────────┐
         │   E2E      │ < 5% (Future: Full workflow tests)
         └────────────┘
      ┌──────────────────┐
      │   Integration    │ 25% (Filesystem operations)
      └──────────────────┘
   ┌────────────────────────┐
   │      Unit Tests        │ 70% (Domain logic, services)
   └────────────────────────┘
```

---

## Unit Test Strategy

### Domain Layer Tests
**Target**: Business logic, validation rules, state transitions
**Framework**: JUnit 5 + AssertJ
**Mocking**: None (pure domain logic)

#### Example: Story Entity Test
```java
@DisplayName("Story Entity Tests")
class StoryTest {

    @Test
    @DisplayName("Should create story with valid data")
    void shouldCreateStory() {
        Story story = new Story(
            new StoryId("STORY-123"),
            "Dashboard UI",
            "Build user dashboard",
            WorkflowState.TODO,
            PrioritizationState.PRIORITIZED
        );

        assertThat(story.getId().getValue()).isEqualTo("STORY-123");
        assertThat(story.getState()).isEqualTo(WorkflowState.TODO);
    }

    @Nested
    @DisplayName("Task Management")
    class TaskManagement {
        @Test
        void shouldAddTaskToStory() {
            Story story = createTestStory();
            Task task = createTestTask();

            story.addTask(task);

            assertThat(story.getTasks()).hasSize(1);
            assertThat(story.getTasks()).contains(task);
        }
    }
}
```

#### Example: WorkflowState Transition Test
```java
@DisplayName("WorkflowState Transition Tests")
class WorkflowStateTest {

    @ParameterizedTest
    @CsvSource({
        "TODO, IN_PROGRESS, AGENT, true",
        "IN_PROGRESS, AWAITING_APPROVAL, AGENT, true",
        "TODO, DONE, AGENT, false",
        "AWAITING_APPROVAL, DONE, HUMAN, true",
        "DONE, IN_PROGRESS, HUMAN, true"
    })
    void shouldValidateTransitions(WorkflowState from, WorkflowState to,
                                   Role initiator, boolean expected) {
        boolean result = from.canTransitionTo(to, initiator);
        assertThat(result).isEqualTo(expected);
    }
}
```

### Service Layer Tests
**Target**: Business use cases, orchestration
**Framework**: JUnit 5 + Mockito + AssertJ
**Mocking**: Repository interfaces, validators

#### Example: StoryService Test
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("StoryService Tests")
class StoryServiceTest {

    @Mock private StoryRepository storyRepository;
    @Mock private ValidationService validationService;
    @Mock private ActivityLogger activityLogger;
    @InjectMocks private StoryServiceImpl storyService;

    @Test
    @DisplayName("Should create story successfully")
    void shouldCreateStory() {
        // Given
        CreateStoryCommand command = new CreateStoryCommand(
            "STORY-123", "Dashboard UI", "Build user dashboard"
        );
        Story expectedStory = new Story(/* ... */);

        when(storyRepository.save(any(Story.class))).thenReturn(expectedStory);

        // When
        Story result = storyService.createStory(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId().getValue()).isEqualTo("STORY-123");
        verify(validationService).validateStory(any(Story.class));
        verify(activityLogger).logSuccess(eq("orchestrator"), eq("create_story"), any(), any());
        verify(storyRepository).save(any(Story.class));
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void shouldThrowWhenValidationFails() {
        // Given
        CreateStoryCommand command = new CreateStoryCommand("INVALID", "", "");
        doThrow(new ValidationException("Invalid story"))
            .when(validationService).validateStory(any());

        // When/Then
        assertThatThrownBy(() -> storyService.createStory(command))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Invalid story");

        verify(storyRepository, never()).save(any());
    }
}
```

---

## Integration Test Strategy

### Repository Layer Tests
**Target**: Filesystem operations (read, write, query)
**Framework**: JUnit 5 + Spring Test + @TempDir
**Real Dependencies**: Actual filesystem, real file I/O

#### Example: FileSystemStoryRepository Test
```java
@DisplayName("FileSystemStoryRepository Integration Tests")
class FileSystemStoryRepositoryTest {

    @TempDir
    Path tempWorkspace;

    private FileSystemStoryRepository repository;
    private WorkspaceConfig workspaceConfig;

    @BeforeEach
    void setup() throws IOException {
        // Create workspace structure
        Files.createDirectories(tempWorkspace.resolve("todo"));
        Files.createDirectories(tempWorkspace.resolve("in-progress"));
        Files.createDirectories(tempWorkspace.resolve("done"));

        workspaceConfig = new WorkspaceConfig(tempWorkspace);
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        repository = new FileSystemStoryRepository(workspaceConfig, objectMapper);
    }

    @Test
    @DisplayName("Should save story and create file")
    void shouldSaveStory() {
        // Given
        Story story = new Story(
            new StoryId("STORY-123"),
            "Dashboard UI",
            "Build user dashboard",
            WorkflowState.TODO,
            PrioritizationState.PRIORITIZED
        );

        // When
        Story saved = repository.save(story);

        // Then
        assertThat(saved).isNotNull();

        Path expectedPath = tempWorkspace
            .resolve("todo/STORY-123-dashboard-ui/story.md");
        assertThat(expectedPath).exists();

        String content = Files.readString(expectedPath);
        assertThat(content).contains("id: STORY-123");
        assertThat(content).contains("title: Dashboard UI");
    }

    @Test
    @DisplayName("Should find story by ID")
    void shouldFindById() {
        // Given
        Story story = createAndSaveStory();

        // When
        Optional<Story> found = repository.findById(story.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(story.getId());
        assertThat(found.get().getTitle()).isEqualTo(story.getTitle());
    }

    @Test
    @DisplayName("Should find stories by state")
    void shouldFindByState() {
        // Given
        createAndSaveStory("STORY-1", WorkflowState.TODO);
        createAndSaveStory("STORY-2", WorkflowState.TODO);
        createAndSaveStory("STORY-3", WorkflowState.IN_PROGRESS);

        // When
        List<Story> todoStories = repository.findByState(WorkflowState.TODO);

        // Then
        assertThat(todoStories).hasSize(2);
        assertThat(todoStories).allMatch(s -> s.getState() == WorkflowState.TODO);
    }

    @AfterEach
    void cleanup() {
        // @TempDir automatically cleans up
    }
}
```

### Validation Tests
**Target**: Regex patterns, naming rules, state consistency
**Framework**: JUnit 5 + @ParameterizedTest

#### Example: Validator Integration Test
```java
@DisplayName("Naming Validator Integration Tests")
class NamingValidatorIntegrationTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "STORY-1-fix",
        "STORY-123-dashboard-ui",
        "STORY-999-multi-word-feature"
    })
    @DisplayName("Should accept valid story directory names")
    void shouldAcceptValidStoryDirectories(String dirname) {
        StoryDirectoryNameValidator validator = new StoryDirectoryNameValidator();
        assertThat(validator.isValid(Path.of(dirname))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "story-123-ui",        // lowercase prefix
        "STORY-123-UI",        // uppercase slug
        "STORY-123",           // missing slug
        "STORY-123-",          // trailing dash
        "STORY-123--ui"        // double dash
    })
    @DisplayName("Should reject invalid story directory names")
    void shouldRejectInvalidStoryDirectories(String dirname) {
        StoryDirectoryNameValidator validator = new StoryDirectoryNameValidator();
        assertThat(validator.isValid(Path.of(dirname))).isFalse();
    }
}
```

---

## Test Fixtures & Utilities

### TestDataBuilder Pattern

```java
public class StoryTestBuilder {
    private StoryId id = new StoryId("STORY-TEST-123");
    private String title = "Test Story";
    private String summary = "Test summary";
    private WorkflowState state = WorkflowState.TODO;
    private PrioritizationState prioritization = PrioritizationState.PRIORITIZED;

    public StoryTestBuilder withId(String id) {
        this.id = new StoryId(id);
        return this;
    }

    public StoryTestBuilder withState(WorkflowState state) {
        this.state = state;
        return this;
    }

    public Story build() {
        return new Story(id, title, summary, state, prioritization);
    }
}

// Usage:
Story story = new StoryTestBuilder()
    .withId("STORY-456")
    .withState(WorkflowState.IN_PROGRESS)
    .build();
```

### Workspace Test Fixture Utility

```java
public class WorkspaceTestFixture {

    public static void createWorkspaceStructure(Path root) throws IOException {
        Files.createDirectories(root.resolve("backlog"));
        Files.createDirectories(root.resolve("todo"));
        Files.createDirectories(root.resolve("in-progress"));
        Files.createDirectories(root.resolve("awaiting-approval"));
        Files.createDirectories(root.resolve("done"));
        Files.createDirectories(root.resolve("logs"));
        Files.createDirectories(root.resolve("templates"));
    }

    public static void createStoryFixture(Path workspace, String storyId,
                                          WorkflowState state) throws IOException {
        String stateDirName = state.name().toLowerCase().replace("_", "-");
        Path storyDir = workspace.resolve(stateDirName)
            .resolve(storyId + "-test-story");
        Files.createDirectories(storyDir);
        Files.createDirectories(storyDir.resolve("artifacts"));
        Files.createDirectories(storyDir.resolve("comments"));

        String storyContent = """
            ---
            id: %s
            type: story
            title: Test Story
            state: %s
            ---
            # Test Story
            This is a test story.
            """.formatted(storyId, state.name().toLowerCase());

        Files.writeString(storyDir.resolve("story.md"), storyContent);
    }
}
```

### AssertJ Custom Assertions

```java
public class StoryAssert extends AbstractAssert<StoryAssert, Story> {

    public StoryAssert(Story story) {
        super(story, StoryAssert.class);
    }

    public static StoryAssert assertThat(Story story) {
        return new StoryAssert(story);
    }

    public StoryAssert hasId(String expectedId) {
        isNotNull();
        if (!actual.getId().getValue().equals(expectedId)) {
            failWithMessage("Expected story id to be <%s> but was <%s>",
                expectedId, actual.getId().getValue());
        }
        return this;
    }

    public StoryAssert isInState(WorkflowState expectedState) {
        isNotNull();
        if (actual.getState() != expectedState) {
            failWithMessage("Expected story state to be <%s> but was <%s>",
                expectedState, actual.getState());
        }
        return this;
    }
}

// Usage:
StoryAssert.assertThat(story)
    .hasId("STORY-123")
    .isInState(WorkflowState.TODO);
```

---

## Test Coverage Targets

| Layer | Target Coverage | Priority |
|-------|----------------|----------|
| Domain Entities | 90%+ | Critical |
| Domain Value Objects | 95%+ | Critical |
| Domain Services | 85%+ | Critical |
| Application Services | 80%+ | High |
| Infrastructure (Repositories) | 75%+ | High |
| Infrastructure (Validators) | 85%+ | High |
| Presentation (Controllers) | 70%+ | Medium |

**Overall Target**: 80%+ line coverage, 75%+ branch coverage

---

## Test Organization

```
src/test/java/com/aiworkflow/workmanagement/
├── domain/
│   ├── model/
│   │   ├── StoryTest.java
│   │   ├── TaskTest.java
│   │   └── ArtifactTest.java
│   ├── valueobject/
│   │   ├── StoryIdTest.java
│   │   └── WorkflowStateTest.java
│   └── service/
│       └── StateTransitionValidatorTest.java
├── application/
│   └── service/
│       ├── StoryServiceTest.java
│       ├── TaskServiceTest.java
│       └── ValidationServiceTest.java
├── infrastructure/
│   ├── persistence/
│   │   ├── FileSystemStoryRepositoryTest.java
│   │   └── PathResolverTest.java
│   ├── logging/
│   │   └── JSONLinesWriterTest.java
│   └── validation/
│       ├── StoryDirectoryNameValidatorTest.java
│       └── ArtifactFileNameValidatorTest.java
└── testutil/
    ├── builders/
    │   ├── StoryTestBuilder.java
    │   └── TaskTestBuilder.java
    ├── fixtures/
    │   └── WorkspaceTestFixture.java
    └── assertions/
        ├── StoryAssert.java
        └── TaskAssert.java
```

---

## Test Naming Conventions

### Method Naming Pattern
`should<ExpectedBehavior>When<Condition>`

**Examples**:
- `shouldCreateStoryWhenValidDataProvided()`
- `shouldThrowExceptionWhenInvalidTransition()`
- `shouldReturnEmptyWhenStoryNotFound()`

### Display Names
Use `@DisplayName` for readable test descriptions:

```java
@Test
@DisplayName("Should transition story from TODO to IN_PROGRESS when agent starts work")
void shouldTransitionStoryWhenAgentStarts() { }
```

---

## Concurrent Testing Strategy

### Thread-Safety Tests for JSONLinesWriter

```java
@Test
@DisplayName("Should handle concurrent writes safely")
void shouldHandleConcurrentWrites() throws Exception {
    int threadCount = 10;
    int writesPerThread = 100;

    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
        final int threadId = i;
        executor.submit(() -> {
            try {
                for (int j = 0; j < writesPerThread; j++) {
                    LogEntry entry = createTestLogEntry(threadId, j);
                    writer.write("test-role", entry);
                }
            } finally {
                latch.countDown();
            }
        });
    }

    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    // Verify all lines written
    List<String> lines = Files.readAllLines(logFile);
    assertThat(lines).hasSize(threadCount * writesPerThread);
}
```

---

## CI/CD Integration

### Maven Surefire Configuration

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
        <excludes>
            <exclude>**/integration/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

### JaCoCo Coverage Plugin

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## Summary

### Test Strategy Overview
- **70% unit tests** (domain, services with mocks)
- **25% integration tests** (filesystem operations with @TempDir)
- **5% E2E tests** (future: full workflow validation)

### Key Testing Patterns
1. **Builder Pattern** for test data construction
2. **@ParameterizedTest** for multiple input validation
3. **@TempDir** for isolated filesystem testing
4. **Custom AssertJ assertions** for domain-specific assertions
5. **Thread-safety tests** for concurrent operations

### Coverage Targets
- **Domain layer**: 90%+
- **Application layer**: 80%+
- **Infrastructure layer**: 75%+
- **Overall**: 80%+ line coverage

### Test Utilities
- `StoryTestBuilder`, `TaskTestBuilder` for fixtures
- `WorkspaceTestFixture` for filesystem setup
- `StoryAssert`, `TaskAssert` for fluent assertions

**Ready for implementation** with comprehensive testing from day one.
