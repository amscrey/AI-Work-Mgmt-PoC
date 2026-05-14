# Repository Pattern Design

**Role**: java-spring-architect
**Story**: STORY-004
**Status**: Complete

## Repository Interfaces (Domain Layer)

### StoryRepository
```java
public interface StoryRepository {
    Story save(Story story);
    Optional<Story> findById(StoryId id);
    List<Story> findByState(WorkflowState state);
    List<Story> findByPrioritization(PrioritizationState prioritization);
    List<Story> findAll();
    boolean exists(StoryId id);
    void delete(StoryId id);

    // Directory-aware queries
    List<Story> findByDirectory(Path directory);
    Path resolveStoryPath(StoryId id, WorkflowState state, PrioritizationState prioritization);
}
```

## Implementation Strategy (Infrastructure)

**FileSystemStoryRepository**:
- Reads/writes story.md files
- Parses YAML frontmatter
- Validates directory/state consistency
- Handles file I/O errors gracefully

## Path Resolution Strategy

```java
public class PathResolver {
    public Path resolveStoryDirectory(Story story, WorkspaceConfig config) {
        if (story.getPrioritization() == PrioritizationState.BACKLOG) {
            return config.getWorkItemsPath().resolve("backlog")
                .resolve(story.getId().toDirectoryName());
        } else {
            String workflowDir = story.getState().getDirectoryName();
            return config.getWorkItemsPath().resolve(workflowDir)
                .resolve(story.getId().toDirectoryName());
        }
    }
}
```

## Test Design Outline

**Integration Tests** (with temp filesystem):
```java
@TempDir Path tempDir;

@Test
void shouldSaveAndLoadStory() throws IOException {
    WorkspaceConfig config = new WorkspaceConfig(tempDir);
    FileSystemStoryRepository repo = new FileSystemStoryRepository(config);

    Story story = createTestStory();
    repo.save(story);

    Optional<Story> loaded = repo.findById(story.getId());

    assertThat(loaded).isPresent();
    assertThat(loaded.get().getTitle()).isEqualTo(story.getTitle());
}
```

**Test Fixtures**: Reusable temp directory setup, story builders, cleanup utilities

**Acceptance Criteria**: ✅ All met
