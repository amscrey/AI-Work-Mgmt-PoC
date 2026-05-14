# Repository Pattern Design

**Role**: java-spring-architect
**Story**: STORY-004
**Status**: Complete

## Repository Interfaces (Domain Layer)

```java
public interface StoryRepository {
    Story save(Story story);
    Optional<Story> findById(StoryId id);
    List<Story> findByState(WorkflowState state);
    List<Story> findByPrioritization(PrioritizationState prioritization);
    List<Story> findAll();
    void delete(StoryId id);
    boolean exists(StoryId id);
}
```

## Implementation (Infrastructure Layer)

```java
@Repository
public class FileSystemStoryRepository implements StoryRepository {
    private final WorkspaceConfig workspaceConfig;
    private final ObjectMapper objectMapper;
    private final PathResolver pathResolver;

    @Override
    public Story save(Story story) {
        Path storyPath = pathResolver.resolveStoryPath(story.getId(), story.getState(), story.getPrioritization());
        // Write story.md with YAML frontmatter + body
        // Handle IOException
    }

    @Override
    public Optional<Story> findById(StoryId id) {
        // Search across all state directories
        // Parse YAML frontmatter
        // Construct Story entity
    }
}
```

## Test Design Outline
**Integration Tests (with @TempDir)**:
```java
class FileSystemStoryRepositoryTest {
    @TempDir Path tempDir;
    FileSystemStoryRepository repository;

    @BeforeEach
    void setup() {
        WorkspaceConfig config = new WorkspaceConfig(tempDir);
        repository = new FileSystemStoryRepository(config, new ObjectMapper());
    }

    @Test
    void shouldSaveAndLoadStory() {
        // Given: temp workspace
        // When: save story
        // Then: file exists, can be loaded
    }

    @AfterEach
    void cleanup() {
        // Temp dir auto-cleaned
    }
}
```
