# LLM Orchestration Layer - Requirements

**Date**: 2026-03-31
**Status**: Initial Requirements Collection

---

## Executive Summary

Add an **LLM orchestration layer** to the AI Work Management Platform that enables AI-assisted work on stories and tasks. The orchestration layer is an **add-on** that operates on top of the existing work item management foundation.

**Critical Principle**: The external filesystem workspace (stories, tasks, artifacts) remains authoritative. LLM orchestration operates *through* the work management model, not *around* it.

---

## Current Foundation (Complete)

✅ **Work Item Management System**:
- Domain model: Story (aggregate root), Task, Comment, Artifact entities
- State machines with role-based transitions (WorkflowState, TaskState, PrioritizationState)
- File-based persistence to external workspace (JSON format)
- Service layer with CRUD operations (StoryService, TaskService, CommentService)
- Activity logging for audit trails
- **225 passing tests** (146 unit + 72 integration + 7 E2E)

✅ **Technology Stack**:
- Java 17 + Spring Boot 3.2.3
- Jackson for JSON/YAML processing
- External filesystem as system of record
- Clean architecture (domain → application → infrastructure)

---

## High-Level Goals

### Primary Objectives

1. **Enable LLM-Assisted Work**
   - AI agents can perform operations on stories/tasks
   - Generate artifacts (research, designs, code, tests, documentation)
   - Provide multi-agent collaboration capabilities

2. **Preserve Architectural Integrity**
   - Orchestration builds ON TOP of work management (no redesign)
   - External workspace remains authoritative
   - Append-only artifacts maintained
   - All provenance and auditability preserved

3. **Support Dual Implementation Modes**
   - **Dummy mode**: Deterministic, no API calls (testing/development)
   - **Real mode**: Actual LLM calls via provider API
   - Both modes use **identical interfaces**

4. **Prepare for Future Capabilities**
   - Multi-agent workflows
   - Graph-based execution flows
   - Human-in-the-loop approval
   - Memory and context management
   - Multiple LLM providers

---

## Technology Selection

### LLM Framework: LangChain4j

**Decision**: Use **LangChain4j** (NOT LangGraph - Python-only)

**Rationale**:
- Mature, established Java LLM framework (245+ Maven Central usages)
- Supports 20+ LLM providers including Anthropic Claude
- Agent and tool calling capabilities
- RAG (Retrieval-Augmented Generation) support
- Chat memory management
- Spring Boot integration available
- Active community and examples

**Alternatives Considered**:
- Spring AI (less mature, fewer features)
- Custom implementation (too much work, reinventing wheel)
- Temporal + custom LLM (over-engineered for initial implementation)

---

## Architectural Design

### Orchestration Layers

```
┌─────────────────────────────────────────────┐
│  Provider/Model Layer (LangChain4j)         │  ← Claude API, prompt templates
├─────────────────────────────────────────────┤
│  Workspace Integration Layer                │  ← Read context, write artifacts
├─────────────────────────────────────────────┤
│  Orchestration Engine Layer                 │  ← Dummy vs Real implementation
├─────────────────────────────────────────────┤
│  Orchestration Domain Layer                 │  ← Requests, contexts, results
├─────────────────────────────────────────────┤
│  Work Item Management Layer (EXISTS)        │  ← Stories, tasks, artifacts
└─────────────────────────────────────────────┘
```

### Key Concepts

**Orchestration Domain**:
- `OrchestrationRequest`: What work to perform (story/task, operation type, constraints)
- `OrchestrationContext`: Assembled input (story metadata, tasks, reference docs)
- `OrchestrationPlan`: Execution plan (agent role, steps, expected outputs)
- `OrchestrationResult`: Outcome (artifacts created, logs, errors, summary)
- `AgentRole`: Specialized roles (DESIGNER, TESTER, LOGICIAN, RESEARCHER, etc.)
- `OperationType`: Types of work (RESEARCH, TASK_PLANNING, IMPLEMENTATION, REVIEW, etc.)

**Orchestration Engine Interface** (implemented by both dummy and real):
```java
public interface OrchestrationEngine {
    OrchestrationResult execute(
        OrchestrationContext context,
        OrchestrationPlan plan
    );
}
```

**Dummy Implementation**:
- Deterministic artifact generation based on story metadata
- Simulates realistic output structure and timing
- Supports all same operations as real mode
- Enables testing without API costs

**Real Implementation (LangChain4j-backed)**:
- Uses LangChain4j ChatLanguageModel
- Calls Claude API via Anthropic provider
- Parses and structures LLM responses
- Produces actual AI-generated artifacts

---

## Operation Types

### Agent Roles (Phase 1 Enum)

**Design Decision**: All roles with `.agent/roles/{role-name}/ROLE.md` files MUST be represented in the AgentRole enum. This ensures type safety and compile-time validation for roles that have defined capabilities and constraints.

All roles with ROLE.md files will be represented in the AgentRole enum:

```java
public enum AgentRole {
    ORCHESTRATOR("orchestrator", "Workflow coordination and task delegation"),
    LOGICIAN("logician", "Algorithms, business logic, and system implementation"),
    REVIEWER("reviewer", "Code and design review, quality assurance"),
    DOCUMENTER("documenter", "Technical documentation and user guides"),
    CREATIVE_WRITER("creative-writer", "Creative content, storytelling, and narrative"),
    TESTER("tester", "Testing strategies, test cases, and QA"),
    CODER("coder", "Software development and implementation"),
    RESEARCHER("researcher", "Research, analysis, and information gathering"),
    ART_DESIGNER("art-designer", "UX/UI design and visual art generation"),
    GAME_RULES_DESIGNER("game-rules-designer", "Game mechanics, rules, and balance");

    private final String fileName;
    private final String description;
}
```

### Supported Orchestration Operations

| Operation Type | Input | Output | Agent Role |
|---------------|-------|--------|------------|
| **RESEARCH** | Story summary + acceptance criteria | Research findings document | RESEARCHER |
| **TASK_PLANNING** | Story details | Task breakdown and estimates | LOGICIAN |
| **SOLUTION_OPTIONS** | Story + constraints | Multiple solution approaches | LOGICIAN |
| **DESIGN_UX** | Story + acceptance criteria | UI mockups, wireframes, user flows | ART_DESIGNER |
| **DESIGN_VISUAL** | Story + visual requirements | Images, illustrations, graphics | ART_DESIGNER |
| **GAME_MECHANICS** | Game requirements | Rules, balance, mechanics | GAME_RULES_DESIGNER |
| **IMPLEMENTATION** | Story + tasks | Code artifacts, configuration | CODER |
| **TEST_STRATEGY** | Story + acceptance criteria | Test plan, test cases | TESTER |
| **REVIEW** | Story + artifacts | Review comments, feedback | REVIEWER |
| **DOCUMENTATION** | Story + implementation | Technical docs, user guides | DOCUMENTER |
| **CREATIVE_CONTENT** | Story + narrative requirements | Storytelling, flavor text | CREATIVE_WRITER |

---

## Artifact Output

### Artifact Naming Convention
```
{descriptive-name}__{role}__{timestamp}.{ext}
```

**Examples**:
- `research-findings__researcher__2026-03-31T140512Z.md`
- `task-breakdown__logician__2026-03-31T141230Z.md`
- `ui-mockup__art-designer__2026-03-31T142045Z.png`
- `game-mechanics__game-rules-designer__2026-03-31T142530Z.md`
- `test-strategy__tester__2026-03-31T143000Z.md`
- `implementation__coder__2026-03-31T143530Z.java`

**Rules**:
- Append-only (never overwrite)
- Immutable after creation
- Written to story's `artifacts/` directory
- Logged in activity logs
- Follows existing artifact model

---

## Context Assembly

### What Gets Included in Orchestration Context

**Always Included**:
- Story ID, title, summary, description
- Story workflow state and prioritization state
- Acceptance criteria
- Story tags and metadata

**Optionally Included** (based on scope):
- All tasks for the story
- Existing artifacts (filtered by relevance)
- Reference documents (limited to relevant subset)
- Previous orchestration results
- Related stories (if specified)

**Context Limits**:
- Max tokens configurable (default: 50,000)
- Max reference documents (default: 5)
- Max artifacts (default: 10)
- Prioritizes recent and relevant content

---

## Configuration Strategy

### application.yml Configuration

```yaml
orchestration:
  mode: dummy  # or "real"

  dummy:
    simulateDelay: true
    delayMs: 1000

  real:
    provider: langchain4j
    langchain4j:
      anthropic:
        apiKey: ${ANTHROPIC_API_KEY}
        modelName: claude-3-5-sonnet-20241022
        temperature: 0.7
        maxTokens: 4096
        timeout: 60s

  context:
    maxReferenceDocuments: 5
    maxArtifacts: 10
    maxContextTokens: 50000

  agents:
    defaultRole: LOGICIAN
    roles:
      - DESIGNER
      - TESTER
      - LOGICIAN
      - RESEARCHER
      - ARCHITECT
      - REVIEWER
```

### Mode Switching

**Development/Testing**:
```
orchestration.mode=dummy
```

**Production**:
```
orchestration.mode=real
ANTHROPIC_API_KEY=sk-ant-...
```

---

## API Design

### Orchestration Service API

```java
public interface OrchestrationService {

    /**
     * Execute orchestration for a story
     */
    OrchestrationResult orchestrate(OrchestrationRequest request);

    /**
     * Get orchestration result by ID
     */
    Optional<OrchestrationResult> getResult(String orchestrationId);

    /**
     * List orchestration runs for a story
     */
    List<OrchestrationSummary> listRuns(StoryId storyId);
}
```

### Usage Example

```java
// Create orchestration request
OrchestrationRequest request = OrchestrationRequest.builder()
    .storyId(new StoryId("STORY-123"))
    .operationType(OperationType.RESEARCH)
    .agentRole(AgentRole.RESEARCHER)
    .scope(OrchestrationScope.STORY_ONLY)
    .build();

// Execute (uses dummy or real based on config)
OrchestrationResult result = orchestrationService.orchestrate(request);

// Check outcome
if (result.isSuccess()) {
    List<Artifact> artifacts = result.getArtifactsCreated();
    // Artifacts already written to story/artifacts/
}
```

---

## Implementation Phases

### Phase 1: Foundation (2-3 days)
- Define orchestration domain model
- Create service interfaces
- Add configuration structure
- **Deliverable**: Domain model with unit tests

### Phase 2: Dummy Implementation (2-3 days)
- Implement dummy orchestration engine
- Implement context assembly service
- Implement artifact output service
- **Deliverable**: Working dummy orchestration with integration tests

### Phase 3: Orchestration Service (1-2 days)
- Implement orchestration service facade
- Add end-to-end tests
- **Deliverable**: Complete dummy orchestration flow

### Phase 4: LangChain4j Integration (3-4 days)
- Add LangChain4j dependencies
- Implement real orchestration engine
- Configure Claude API integration
- Create prompt templates
- **Deliverable**: Real orchestration engine (may be partial)

### Phase 5: Testing & Documentation (1-2 days)
- Add comprehensive tests
- Create documentation
- Update testing guide
- **Deliverable**: Production-ready orchestration layer

**Total Estimate**: 9-14 days

---

## Testing Strategy

### Test Coverage Goals

**Unit Tests** (~30 new tests):
- Domain model classes (OrchestrationRequest, Context, Result, etc.)
- Dummy orchestration engine logic
- Context assembly service
- Artifact output service

**Integration Tests** (~15 new tests):
- Dummy orchestration end-to-end
- Context assembly from repositories
- Artifact writing to filesystem
- Mode switching (dummy ↔ real)

**E2E Tests** (~5 new tests):
- Complete orchestration flow with dummy engine
- Verify artifacts in workspace
- Verify activity logs
- Test success and failure scenarios
- Contract tests (dummy vs real interface compatibility)

**Target**: ~50 new tests, bringing total to ~275 tests

---

## Success Criteria

### Definition of Done

The LLM orchestration layer is complete when:

1. ✅ Domain model defined and tested
2. ✅ Dummy orchestration engine implemented and working
3. ✅ Real orchestration engine integrated with LangChain4j
4. ✅ Both engines implement identical interface
5. ✅ Context assembly service can gather story/task/reference context
6. ✅ Artifact output service writes append-only artifacts
7. ✅ Orchestration logs captured in activity logs
8. ✅ Configuration allows switching between dummy and real modes
9. ✅ External workspace remains authoritative
10. ✅ All tests passing (target: ~275 total tests)
11. ✅ Documentation updated
12. ✅ Can execute all operation types (RESEARCH, TASK_PLANNING, etc.)

---

## Future Enhancements (Out of Scope for Initial Implementation)

- Multi-agent collaboration (multiple roles on same story)
- Graph-based workflows (complex agent coordination)
- Tool calling (agents can invoke workspace functions)
- Streaming responses
- Human-in-the-loop approval workflows
- Memory management (agent memory across conversations)
- Promotion service (approved artifacts → reference/)
- Retry and failure handling
- Cost tracking and token limits
- Multiple provider support (OpenAI, Gemini, etc.)

---

## Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| LangChain4j API changes | Medium | Pin dependency versions, monitor releases |
| Claude API rate limits | Medium | Implement retry with backoff, use dummy mode for testing |
| Context size limits | Medium | Implement smart context filtering, configurable limits |
| Cost of API calls during development | Low | Use dummy mode for most development/testing |
| Over-engineering orchestration | Medium | Start simple, iterate based on real needs |

---

## Dependencies

### Maven Dependencies to Add

```xml
<!-- LangChain4j Core -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-core</artifactId>
    <version>0.35.0</version>
</dependency>

<!-- LangChain4j Anthropic Provider -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-anthropic</artifactId>
    <version>0.35.0</version>
</dependency>

<!-- LangChain4j Spring Boot Starter -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>0.35.0</version>
</dependency>
```

---

## Non-Functional Requirements

### Performance
- Dummy mode: < 2 seconds per orchestration
- Real mode: Depends on LLM response time (typically 5-30 seconds)
- Context assembly: < 1 second for typical story

### Scalability
- Support concurrent orchestration requests
- Handle workspaces with 100+ stories
- Limit context to prevent token overflow

### Reliability
- Graceful handling of API failures
- Fallback to dummy mode if API unavailable (optional)
- All errors logged with context

### Security
- API keys stored in environment variables (never in code)
- No sensitive data in prompts
- Audit trail of all orchestration activities

---

## Package Structure (Proposed)

```
com.aiworkflow.workmanagement.orchestration/
├── domain/
│   ├── OrchestrationRequest.java
│   ├── OrchestrationContext.java
│   ├── OrchestrationPlan.java
│   ├── OrchestrationResult.java
│   ├── AgentRole.java (enum)
│   ├── OperationType.java (enum)
│   └── ExecutionOutcome.java (enum)
│
├── engine/
│   ├── OrchestrationEngine.java (interface)
│   ├── dummy/
│   │   └── DummyOrchestrationEngine.java
│   └── langchain4j/
│       ├── LangChain4jOrchestrationEngine.java
│       ├── PromptTemplateService.java
│       └── ResponseParser.java
│
├── service/
│   ├── OrchestrationService.java (interface)
│   ├── ContextAssemblyService.java (interface)
│   ├── ArtifactOutputService.java (interface)
│   └── impl/
│       ├── OrchestrationServiceImpl.java
│       ├── ContextAssemblyServiceImpl.java
│       └── ArtifactOutputServiceImpl.java
│
└── config/
    ├── OrchestrationConfig.java
    └── LangChain4jConfig.java
```

---

## Next Steps

1. ✅ Requirements documented
2. ⏭️ Create workspace: `~/dev/PoC/AI-Assisted-Projects/AI-Work-Mgmt-LLM-Orch-AddOn`
3. ⏭️ Break down into stories using work management system
4. ⏭️ Prioritize and sequence stories
5. ⏭️ Begin implementation (Phase 1: Foundation)

---

**Status**: Ready for story breakdown and planning