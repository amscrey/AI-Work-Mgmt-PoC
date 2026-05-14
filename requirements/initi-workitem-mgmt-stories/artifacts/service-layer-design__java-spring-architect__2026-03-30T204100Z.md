# Service Layer Design

**Role**: java-spring-architect
**Story**: STORY-003
**Status**: Complete

## Service Interfaces & Implementations

### StoryService
```java
public interface StoryService {
    Story createStory(CreateStoryCommand command);
    Story updateStory(UpdateStoryCommand command);
    void transitionState(StoryId id, WorkflowState targetState, Role initiator);
    void changePrioritization(StoryId id, PrioritizationState newState);
    Optional<Story> findById(StoryId id);
    List<Story> findByState(WorkflowState state);
    List<Story> findByPrioritization(PrioritizationState prioritization);
    void deleteStory(StoryId id);
}

@Service
@Transactional
public class StoryServiceImpl implements StoryService {
    private final StoryRepository storyRepository;
    private final ValidationService validationService;
    private final ActivityLogger activityLogger;
    private final StateTransitionService stateTransitionService;

    // Constructor injection, methods implementation
}
```

### TaskService, ArtifactService, CommentService, WorkspaceService, ValidationService
- Similar patterns with CRUD + domain-specific operations
- All use constructor injection
- All have @Transactional where needed
- All log activities

## Test Design Outline
**Unit Tests (with Mocks)**:
- Mock repositories, test business logic
- Verify validation called
- Verify logging called
- Assert correct exceptions thrown

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class StoryServiceImplTest {
    @Mock StoryRepository storyRepository;
    @Mock ValidationService validationService;
    @InjectMocks StoryServiceImpl storyService;

    @Test
    void shouldCreateStory() {
        // Given: valid command
        // When: createStory called
        // Then: repository.save called, logger called
    }
}
```
