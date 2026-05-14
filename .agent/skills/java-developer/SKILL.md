---
name: java-developer
description: Implement production-quality Java code from architectural designs. Use when writing Java classes, implementing services/repositories, writing Spring Boot configuration, or translating designs into working code.
---

# Java Developer

Translates architectural designs and domain models into production-quality Java implementation code.

## Input
- Architectural designs (from java-spring-architect)
- Domain model definitions
- Service interface contracts
- Repository interface contracts
- Test requirements
- Code review feedback

## Output
- Java class implementations
- Spring Boot configuration classes
- Service implementations
- Repository implementations
- Utility classes
- Exception classes
- Unit tests
- Integration tests

---

## Process

### Phase 1: Understand the Design
1. Read architectural design documents
2. Read domain model definitions
3. Understand package structure
4. Review interface contracts (if implementing)
5. Identify dependencies and imports needed
6. Clarify ambiguities with architect/logician

### Phase 2: Implement Core Classes
1. Start with domain entities (if not using records)
2. Implement value objects (usually records)
3. Implement enumerations with logic
4. Add validation in constructors
5. Follow immutability patterns where specified
6. Keep domain layer free of framework dependencies

### Phase 3: Implement Application Layer
1. Implement service classes
2. Add @Service annotations
3. Use constructor injection for dependencies
4. Implement business logic methods
5. Add transaction boundaries (@Transactional where needed)
6. Implement exception handling

### Phase 4: Implement Infrastructure Layer
1. Implement repository classes
2. Add @Repository or @Component annotations
3. Implement file I/O operations
4. Handle filesystem exceptions
5. Implement path resolution
6. Add logging

### Phase 5: Add Configuration
1. Create @Configuration classes
2. Create @ConfigurationProperties classes
3. Wire beans if needed
4. Create application.yml/properties entries

### Phase 6: Write Tests
1. Write unit tests for domain logic
2. Write service layer unit tests with mocks
3. Write repository integration tests
4. Use test fixtures and temp directories
5. Aim for high coverage on critical paths

---

## Java Best Practices

### Code Quality
- Follow Java naming conventions (camelCase for methods/fields, PascalCase for classes)
- Use meaningful variable and method names
- Keep methods small and focused (< 20 lines ideal)
- Avoid deep nesting (max 3 levels)
- Use early returns to reduce nesting
- Prefer composition over inheritance
- Follow SOLID principles

### Spring Boot Patterns
- Use constructor injection over field injection
- Prefer interfaces over concrete classes in dependencies
- Use @Transactional for methods that modify state
- Use @Validated for input validation
- Create custom exceptions for domain errors
- Use @ControllerAdvice for global exception handling

### Error Handling
- Catch specific exceptions, not general Exception
- Log errors with context
- Create custom exceptions for business errors
- Don't swallow exceptions silently
- Provide meaningful error messages

### Null Safety
- Use Optional<T> for optional return values
- Validate constructor parameters (fail-fast)
- Use Objects.requireNonNull() for null checks
- Consider using @NonNull annotations

### Immutability
- Use final for fields that shouldn't change
- Use records for value objects
- Make defensive copies of mutable collections
- Return unmodifiable collections from getters

### Performance
- Close resources properly (try-with-resources)
- Use appropriate collection types (ArrayList vs LinkedList)
- Avoid premature optimization
- Profile before optimizing

---

## Common Implementation Patterns

### Constructor Injection
```java
@Service
public class StoryServiceImpl implements StoryService {
    private final StoryRepository storyRepository;
    private final ValidationService validationService;

    public StoryServiceImpl(
        StoryRepository storyRepository,
        ValidationService validationService
    ) {
        this.storyRepository = Objects.requireNonNull(storyRepository);
        this.validationService = Objects.requireNonNull(validationService);
    }
}
```

### Repository Pattern (Filesystem)
```java
@Repository
public class FileSystemStoryRepository implements StoryRepository {
    private final WorkspaceConfig workspaceConfig;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Story> findById(StoryId id) {
        Path storyPath = resolveStoryPath(id);
        if (!Files.exists(storyPath)) {
            return Optional.empty();
        }
        try {
            Story story = parseStoryFile(storyPath);
            return Optional.of(story);
        } catch (IOException e) {
            throw new RepositoryException("Failed to read story: " + id, e);
        }
    }
}
```

### Value Object Validation
```java
public record StoryId(String value) {
    private static final Pattern PATTERN =
        Pattern.compile("STORY-\\d{1,}-[a-z0-9-]+");

    public StoryId {
        Objects.requireNonNull(value, "Story ID cannot be null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Invalid story ID format: " + value
            );
        }
    }
}
```

### Service Layer
```java
@Service
@Transactional
public class StoryServiceImpl implements StoryService {

    @Override
    public Story createStory(CreateStoryCommand command) {
        // Validate
        validationService.validate(command);

        // Create domain object
        Story story = new Story(
            StoryId.generate(),
            command.title(),
            WorkflowState.TODO,
            PrioritizationState.BACKLOG,
            command.author(),
            command.summary(),
            command.acceptanceCriteria()
        );

        // Persist
        Story saved = storyRepository.save(story);

        // Log
        activityLogger.log(ActivityType.STORY_CREATED, saved.getId());

        return saved;
    }
}
```

---

## Testing Patterns

### Unit Test (Domain Logic)
```java
@Test
void shouldRejectInvalidTransition() {
    Story story = createTestStory(WorkflowState.TODO);

    assertThrows(InvalidTransitionException.class, () -> {
        story.transitionTo(WorkflowState.DONE, Role.ORCHESTRATOR);
    });
}
```

### Service Test (with Mocks)
```java
@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private StoryServiceImpl storyService;

    @Test
    void shouldCreateStory() {
        CreateStoryCommand command = new CreateStoryCommand(...);

        when(storyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Story result = storyService.createStory(command);

        assertThat(result).isNotNull();
        verify(storyRepository).save(any(Story.class));
    }
}
```

### Repository Test (with Temp Filesystem)
```java
@TempDir
Path tempDir;

@Test
void shouldSaveAndLoadStory() throws IOException {
    WorkspaceConfig config = new WorkspaceConfig(tempDir);
    FileSystemStoryRepository repo = new FileSystemStoryRepository(config);

    Story story = createTestStory();
    repo.save(story);

    Optional<Story> loaded = repo.findById(story.getId());

    assertThat(loaded).isPresent();
    assertThat(loaded.get().getId()).isEqualTo(story.getId());
}
```

---

## File Organization

When creating Java files, organize by layer:

```
src/main/java/com/workmanagement/
  ├── domain/
  │   ├── model/Story.java
  │   ├── value/StoryId.java
  │   └── enums/WorkflowState.java
  │
  ├── application/
  │   ├── story/StoryService.java
  │   └── story/StoryServiceImpl.java
  │
  ├── infrastructure/
  │   ├── filesystem/FileSystemStoryRepository.java
  │   └── validation/StoryValidator.java
  │
  └── config/
      └── WorkspaceConfiguration.java
```

---

## Completeness Checklist
- □ All required imports added?
- □ Constructor injection used (not field injection)?
- □ All fields final where appropriate?
- □ Null checks on constructor parameters?
- □ Exceptions handled or propagated appropriately?
- □ Logging added for important operations?
- □ Unit tests written for logic?
- □ Integration tests written for I/O?
- □ Code follows naming conventions?
- □ No unused imports or variables?
- □ JavaDoc comments for public API?

## Rules
1. **ALWAYS** follow the architectural design provided
2. **ALWAYS** use constructor injection over field injection
3. **ALWAYS** validate inputs (fail-fast)
4. **ALWAYS** handle or declare exceptions
5. **ALWAYS** write tests for your implementations
6. **ALWAYS** use meaningful names for variables and methods
7. **NEVER** add Spring annotations to domain layer classes
8. **NEVER** catch and ignore exceptions silently
9. **NEVER** use field injection (@Autowired on fields)
10. **NEVER** return null from methods - use Optional<T>
11. **ALWAYS** close resources (use try-with-resources)
12. **ALWAYS** log errors with context
13. **ALWAYS** make defensive copies of mutable parameters
14. **ALWAYS** prefer immutability (final, records)
15. **NEVER** hardcode paths or configuration values