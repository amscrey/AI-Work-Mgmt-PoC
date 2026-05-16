# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AI Work Management Platform - A filesystem-based work management system designed for AI agents, built with Spring Boot 3.2.3 and Java 17. The system uses Domain-Driven Design principles with LLM orchestration capabilities via LangChain4j and LangGraph4j.

## Build & Test Commands

### Build
```bash
# Build without tests
mvn clean install -DskipTests

# Full build with tests and coverage
mvn clean verify
```

### Testing
```bash
# Run all tests (unit + integration + E2E)
mvn test

# Run only unit tests
mvn test -Dtest='**/*Test,**/*Tests,**/*TestCase'

# Run only integration tests
mvn test -Dtest='*IT'

# Run specific test class
mvn test -Dtest=FileBasedStoryRepositoryIT

# Run specific test method
mvn test -Dtest=StoryTest#shouldCreateStoryWithValidInputs

# Generate coverage report
mvn clean test jacoco:report

# Coverage report location: target/site/jacoco/index.html
```

### Running the Application
```bash
# Default mode (template mode - no LLM calls)
mvn spring-boot:run

# Set workspace root
WORKSPACE_ROOT=/path/to/workspace mvn spring-boot:run

# Enable LLM mode with real Anthropic API calls
export ANTHROPIC_API_KEY=your-key
export MODEL_ANTHROPIC=claude-sonnet-4-5-20250514  # Optional override
./quickstart/run-llm-anthropic.sh

# Corporate proxy mode
export ANTHROPIC_API_KEY=your-proxy-key
export ANTHROPIC_BASE_URL=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud
./quickstart/run-llm-anthropic.sh
```

## Architecture Overview

### Layered Architecture (Domain-Driven Design)

**Domain Layer** (`com.aiworkflow.workmanagement.domain`)
- Core business entities: Story, Task, Comment, Artifact
- Value objects: StoryId, TaskId, CommentId, RoleName, ArtifactName
- Domain services: StateTransitionValidator
- Enums: WorkflowState, TaskState, PrioritizationState, CommentType, ArtifactType
- Repository interfaces (domain-owned contracts)

**Application Layer** (`com.aiworkflow.workmanagement.application`)
- Services: StoryService, TaskService, CommentService, ActivityLogger
- Commands: CreateStoryCommand, UpdateStoryCommand, TransitionStoryCommand, CreateTaskCommand
- Service implementations coordinate between domain and infrastructure

**Infrastructure Layer** (`com.aiworkflow.workmanagement.infrastructure`)
- File-based repositories: FileBasedStoryRepository, FileBasedTaskRepository, FileBasedCommentRepository
- Persistence DTOs: StoryDTO, TaskDTO (Jackson serialization)
- File formats: JSON for structured data, Markdown with YAML frontmatter for stories
- Activity logging: FileBasedActivityLogger (JSON Lines format, per-role log files)

**Orchestration Layer** (`com.aiworkflow.workmanagement.orchestration`)
- LLM integration via LangChain4j (Anthropic, Gemini, Groq providers)
- StateGraph orchestration via LangGraph4j
- Execution modes: "template" (deterministic, no LLM) or "llm" (real API calls)
- Node types: Research, Design, TestStrategy, TaskPlanning (both Template and LLM variants)
- Services: OrchestrationService, PromptTemplateService, RoleModelService, TokenEstimationService

### Key Domain Concepts

**Story Workflow States**
- Backlog: PRIORITIZED (ready for work)
- Active Work: IN_PROGRESS, IN_REVIEW, BLOCKED
- Terminal: DONE, CANCELLED

**Task States**
- TODO, IN_PROGRESS, DONE, CANCELLED

**Agent Roles** (orchestration)
- Defined in role files loaded at startup
- Role-to-LLM-model mapping configurable
- Used by orchestration nodes for context-aware execution

### Persistence Strategy

**File Structure** (workspace-based)
```
{WORKSPACE_ROOT}/{workspace-name}/
├── stories/
│   ├── prioritized/
│   │   └── {story-id}.md          # Markdown with YAML frontmatter
│   ├── in-progress/
│   └── done/
├── tasks/
│   └── {task-id}.json             # JSON format
├── comments/
│   └── {parent-id}/
│       └── {comment-id}.json      # JSON format
├── artifacts/
│   └── {story-id}/
│       └── {artifact-name}        # Various formats
└── logs/
    └── activity/
        └── {role-name}.jsonl      # JSON Lines per role
```

**Data Formats**
- Stories: Markdown with YAML frontmatter (human-readable)
- Tasks/Comments: Pure JSON (structured data)
- Activity Logs: JSON Lines (append-only, per-role)

## Test Suite Architecture

### Test Pyramid (>= 80% coverage required)

**Unit Tests** (~100+ tests, ~2s execution)
- Domain model validation (Story, Task, Comment)
- Service layer logic (StoryServiceImpl, TaskServiceImpl)
- Value object behavior (StoryId, TaskId, etc.)
- State transition rules
- No I/O, all mocked dependencies

**Integration Tests** (~50+ tests, ~2s execution)
- File-based repository operations (CRUD)
- Persistence format validation (JSON, Markdown, JSONL)
- Activity logger file operations
- Real file system interaction
- Test workspaces in `target/test-workspace-*` (isolated per suite)

**End-to-End Tests** (varies, ~1s execution)
- Complete workflows through all layers
- Story creation → task assignment → completion
- Multi-entity coordination
- Error scenario handling
- No mocks, all real components

### Test Workspace Configuration

Each integration test suite uses an isolated workspace in `target/`:
- `target/test-workspace-story` - Story repository tests
- `target/test-workspace-task` - Task repository tests
- `target/test-workspace-comment` - Comment repository tests
- `target/test-workspace-activity-log` - Activity logger tests

Workspaces are cleaned before each test and preserved after for inspection.

## Important Configuration

**Environment Variables**
- `WORKSPACE_ROOT` - Base directory for workspace data (default: `AI-Work-Mgmt-LLM-Orch-AddOn`)
- `WORKSPACE_NAME` - Workspace subdirectory name (default: `workspace`)
- `ANTHROPIC_API_KEY` - Required for LLM mode
- `GEMINI_API_KEY` - Optional (Gemini provider)
- `GROQ_API_KEY` - Optional (Groq provider)

**Application Modes** (application.yml)
- `orchestration.execution-mode: template` - Deterministic mode, no LLM calls
- `orchestration.execution-mode: llm` - Real LLM API calls (or stubbed if no API key)

**LLM Client Selection** (automatic)
- ✅ API key provided → Real LLM client (actual API calls via LangChain4j)
- ⚠️ API key missing → Stubbed client (fake responses for testing)
- 🔧 Dummy provider → File-bridge/clipboard mode (human-in-loop)

**Maven Profiles**
- Default: Anthropic provider only
- `-Pllm-providers`: Includes Gemini and Groq providers

## Code Patterns & Conventions

### When Adding New Entities
1. Create domain model in `domain.model` package
2. Add value object ID class (extends base ID pattern)
3. Define repository interface in `domain.repository`
4. Implement file-based repository in `infrastructure.persistence`
5. Add service interface in `application.service`
6. Implement service in `application.service.impl`
7. Create DTO for serialization if needed

### When Writing Tests
1. Unit tests for domain logic go in corresponding test package
2. Integration tests for repositories end with `IT` suffix
3. Use `@BeforeEach` to set up test workspace (clean from previous run)
4. Leave test artifacts in `target/` for inspection (not cleaned in `@AfterEach`)
5. Always update `etc/docs/TESTING.md` when adding integration tests
6. Include comprehensive header in integration test classes with workspace location

### File Naming Conventions
- Stories: `{story-id}.md` (e.g., `STORY-001.md`)
- Tasks: `{task-id}.json` (e.g., `TASK-001.json`)
- Comments: `{parent-id}/{comment-id}.json`
- Artifacts: `{story-id}/{artifact-name}.{ext}`
- Activity logs: `{role-name}.jsonl` (e.g., `product-manager.jsonl`)

### State Transitions
- Always use `StateTransitionValidator` for workflow state changes
- Invalid transitions throw `InvalidStateTransitionException`
- Story state determines directory location in filesystem

## LLM Orchestration Architecture

### StateGraph Pattern (LangGraph4j)
- Nodes represent workflow steps (Research, Design, TestStrategy, TaskPlanning)
- Each node has template mode (deterministic) and LLM mode (API calls)
- Context assembly from reference files, artifacts, role definitions
- Token usage tracking per story, per provider, per model

### Node Execution Flow
1. Context assembly (reference files, previous artifacts, role definitions)
2. Template rendering or LLM API call
3. Artifact output to filesystem
4. Activity logging
5. State transition (conditional edges based on outcomes)

### LLM Provider Abstraction
- `LLMChatClient` interface abstracts provider details
- `RoutingLlmChatClient` routes to configured provider
- `DummyChatClient` for testing without API calls
- Provider configurations in `application.yml`

## Documentation Structure

**Primary Docs** (etc/docs/)
- `TESTING.md` - Comprehensive testing guide (ESSENTIAL READING)
- `REST-API.md` - API conventions (if implementing REST endpoints)
- `history/` - Phase completion documents with implementation details

**Phase History** (etc/docs/history/)
- `PHASE-1A-COMPLETE.md` - Domain layer implementation
- `PHASE-1B-*.md` - Application layer implementation
- `PHASE-1C-*.md` - Infrastructure layer implementation
- `ACTIVITY-LOGGER-COMPLETE.md` - Activity logging implementation

## Development Workflow

1. Read `etc/docs/TESTING.md` for test expectations
2. Follow TDD approach: write tests first
3. Implement in appropriate layer (domain → application → infrastructure)
4. Ensure >= 80% test coverage (enforced by JaCoCo)
5. Update TESTING.md if adding integration tests
6. Create phase completion doc for major features
7. Run full test suite before committing: `mvn verify`

## Common Pitfalls

- Don't skip reading existing tests before implementing similar functionality
- Don't modify test workspaces manually during test execution
- Don't use `mvn clean` during active debugging (removes test artifacts)
- Always validate state transitions through `StateTransitionValidator`
- Don't commit test data (target/ is in .gitignore)
- Integration test headers must reference TESTING.md and workspace location