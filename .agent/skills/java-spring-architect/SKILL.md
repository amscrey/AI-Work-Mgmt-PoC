---
name: java-spring-architect
description: Design Java Spring Boot application architecture with clean separation of concerns. Use when starting new Spring Boot projects, designing service layers, planning package structure, or applying domain-driven design patterns.
---

# Java Spring Architect

Designs clean, maintainable Java Spring Boot application architecture with proper separation of concerns and adherence to Spring best practices.

## Input
- Project requirements and domain model
- Technology constraints (Java version, Spring Boot version)
- Architectural style (layered, hexagonal, clean architecture)
- Integration requirements (REST, persistence, messaging)

## Output
- Package structure design
- Domain model classes
- Service layer interfaces and implementations
- Repository contracts
- Controller design
- Configuration classes
- Dependency injection strategy
- Architecture Decision Records (ADRs)

---

## Process

### Phase 1: Domain Analysis
1. Identify core domain entities (e.g., WorkItem, Story, Task, Artifact)
2. Identify value objects (e.g., StoryId, ArtifactName, LifecycleState)
3. Identify aggregates and aggregate roots
4. Define domain services for business logic
5. Identify domain events (if event-driven)
6. Document ubiquitous language and terminology

### Phase 2: Package Structure Design
1. Define top-level package organization:
   ```
   com.example.workmanagement/
   ├── domain/              # Core domain model
   ├── application/         # Application services, use cases
   ├── infrastructure/      # Technical concerns
   ├── presentation/        # Controllers, DTOs
   └── config/             # Spring configuration
   ```
2. Design domain package structure:
   ```
   domain/
   ├── model/              # Entities, value objects
   ├── repository/         # Repository interfaces
   ├── service/            # Domain services
   └── exception/          # Domain exceptions
   ```
3. Design infrastructure package structure:
   ```
   infrastructure/
   ├── filesystem/         # Workspace file operations
   ├── persistence/        # Repository implementations (if using DB)
   ├── validation/         # File contract validators
   └── template/           # Template generators
   ```
4. Design application package structure:
   ```
   application/
   ├── story/              # Story use cases
   ├── task/               # Task use cases
   ├── artifact/           # Artifact use cases
   └── workspace/          # Workspace use cases
   ```

### Phase 3: Layer Boundary Design
1. **Domain Layer (innermost)**:
   - No Spring dependencies
   - Pure Java domain model
   - Business logic and invariants
   - Repository interfaces (ports)
2. **Application Layer**:
   - Orchestrates use cases
   - Transaction boundaries
   - Minimal Spring annotations (@Service, @Transactional)
   - Depends on domain, not infrastructure
3. **Infrastructure Layer**:
   - Implements domain repository interfaces
   - Filesystem operations
   - External system integration
   - Spring-heavy (@Component, @Repository)
4. **Presentation Layer**:
   - REST controllers
   - Request/Response DTOs
   - Input validation
   - Error handling (@RestController, @ControllerAdvice)

### Phase 4: Dependency Management
1. Define Maven/Gradle dependencies:
   - Spring Boot starters (web, validation, actuator)
   - Testing libraries (JUnit, AssertJ, Mockito)
   - YAML parsing (Jackson, SnakeYAML)
   - File operations (NIO.2, Apache Commons)
2. Configure dependency injection:
   - Constructor injection (prefer over field injection)
   - Interface-based dependencies
   - Configuration properties (@ConfigurationProperties)
3. Design configuration classes:
   - WorkspaceConfig
   - ValidationConfig
   - FileSystemConfig

### Phase 5: Cross-Cutting Concerns
1. **Exception Handling**:
   - Define domain exceptions (InvalidWorkItemException, WorkspaceValidationException)
   - Design @ControllerAdvice for REST error handling
   - Map exceptions to HTTP status codes
2. **Validation**:
   - Bean Validation for DTOs (@Valid, @NotNull, @Pattern)
   - Domain validation in entities
   - File contract validation in infrastructure
3. **Logging**:
   - SLF4J facade
   - Structured logging for operations
   - Audit logging to agents/logs/
4. **Testing Strategy**:
   - Unit tests for domain logic
   - Integration tests for file operations
   - Controller tests with @WebMvcTest
   - End-to-end tests with @SpringBootTest

---

## Design Principles

1. **Dependency Rule**: Dependencies point inward (presentation → application → domain)
2. **Single Responsibility**: Each class has one reason to change
3. **Interface Segregation**: Clients depend on minimal interfaces
4. **Dependency Inversion**: Depend on abstractions, not concretions
5. **Don't Repeat Yourself**: Extract common logic to shared components
6. **Keep It Simple**: Avoid over-engineering, implement only what's needed

## Architecture Patterns

### Repository Pattern
```java
// Domain layer - interface
public interface StoryRepository {
    Story save(Story story);
    Optional<Story> findById(StoryId id);
    List<Story> findByState(LifecycleState state);
}

// Infrastructure layer - implementation
@Repository
public class FileSystemStoryRepository implements StoryRepository {
    // File-based implementation
}
```

### Service Pattern
```java
// Application layer
@Service
public class StoryService {
    private final StoryRepository storyRepository;

    @Transactional
    public Story createStory(CreateStoryCommand command) {
        // Use case logic
    }
}
```

### Value Object Pattern
```java
// Domain layer
public record StoryId(String value) {
    public StoryId {
        if (!value.matches("STORY-\\d{3,}-[a-z0-9-]+")) {
            throw new IllegalArgumentException("Invalid story ID format");
        }
    }
}
```

---

## Completeness Checklist
- □ Package structure documented with clear responsibilities?
- □ Domain model identified with entities and value objects?
- □ Layer boundaries defined with dependency rules?
- □ Repository interfaces defined in domain layer?
- □ Service layer designed for use cases?
- □ Configuration classes planned?
- □ Exception handling strategy defined?
- □ Testing strategy documented?

## Rules
1. **ALWAYS** keep domain layer free of Spring dependencies
2. **ALWAYS** use constructor injection over field injection
3. **ALWAYS** define repository interfaces in domain, implementations in infrastructure
4. **ALWAYS** validate inputs at layer boundaries
5. **ALWAYS** use immutable value objects where possible
6. **NEVER** let domain layer depend on infrastructure or presentation
7. **NEVER** use @Autowired field injection in new code
8. **NEVER** put business logic in controllers or repository implementations
9. **NEVER** expose domain entities directly in REST APIs (use DTOs)
10. **NEVER** skip exception handling strategy