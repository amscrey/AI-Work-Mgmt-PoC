# LangGraph4j Architecture Revision

## Overview

This document describes the architectural pivot to use **langgraph4j** for orchestration workflows alongside **LangChain4j** for LLM integration.

## Technology Stack

### Core Technologies

1. **langgraph4j** - Stateful workflow orchestration
   - `StateGraph<T>` for workflow definition
   - Node-based execution model
   - Appender reducer for accumulating artifacts
   - Conditional routing support

2. **LangChain4j** - LLM integration
   - Multi-provider support (Claude, Gemini, Grok, etc.)
   - ChatLanguageModel for LLM calls
   - Prompt templates
   - Tool calling capabilities

## Architectural Decision

**Original Plan**: Use LangChain4j exclusively
**Revised Plan**: Use langgraph4j for workflows + LangChain4j for LLM calls

### Why This Change?

- **Separation of Concerns**: langgraph4j handles workflow orchestration; LangChain4j handles LLM communication
- **StateGraph Model**: Better fit for multi-agent workflows than pure LangChain4j
- **Flexibility**: Template nodes and LLM nodes use the same `NodeAction` interface
- **Testability**: Template mode for testing without LLM API costs

## Architecture Layers

### Layer 1: Domain Model (Existing - No Changes)
- Story, Task, Comment, Artifact entities
- File-based persistence
- State machines with transitions
- Activity logging

### Layer 2: Orchestration Domain (New)
**Domain Objects** (decoupled from langgraph4j):
- `OrchestrationRequest` - Input (what to do)
- `OrchestrationContext` - Assembled context (story + tasks + references)
- `OrchestrationResult` - Output (artifacts + summary)
- `ArtifactReference` - Lightweight artifact representation
- `AgentRole` - Role definitions (researcher, designer, etc.)

**langgraph4j State**:
- `OrchestrationState` - State object for StateGraph
  - Maps domain objects for graph execution
  - Accumulates artifacts via Appender reducer
  - Placeholder until langgraph4j dependency added (STORY-008A)

### Layer 3: Node Actions (New)
**Interface**: `NodeAction`
```java
@FunctionalInterface
public interface NodeAction {
    OrchestrationState execute(OrchestrationState state);
}
```

**Two Implementations**:

1. **Template Nodes** (execution-mode: template)
   - Deterministic markdown generation
   - No LLM calls
   - Fast execution
   - Configurable delay simulation
   - Used for development/testing

2. **LLM Nodes** (execution-mode: llm)
   - Real Claude API calls via LangChain4j
   - Dynamic content generation
   - Slower execution
   - API cost implications
   - Used for production

**Key Property**: Both implementations use the same `NodeAction` interface - completely interchangeable!

### Layer 4: Service Layer (New)

**ContextAssemblyService**:
- Assembles `OrchestrationContext` from repositories
- Loads story, tasks, reference files
- Applies context size limits
- Auto-creates PROJECT-PLANNING meta work item
- Returns domain object (NOT langgraph4j state)

**ArtifactOutputService**:
- Writes artifacts to filesystem
- Follows naming convention: `{name}__{role}__{timestamp}.md`
- Append-only (never overwrites)
- Adds artifacts to Story aggregate
- Logs artifact creation

**OrchestrationService** (Facade):
- Coordinates full orchestration flow
- Handles state conversion:
  - `OrchestrationContext` → `OrchestrationState` (for langgraph4j)
  - `OrchestrationState` → `OrchestrationResult` (from langgraph4j)
- Executes StateGraph
- Returns domain object to caller

### Layer 5: langgraph4j Integration (Future - STORY-008A+)

**StateGraph Definition**:
```java
StateGraph<OrchestrationState> graph = new StateGraph<>(OrchestrationState.class)
    .addNode("research", researchNodeAction)
    .addNode("planning", planningNodeAction)
    .addNode("design", designNodeAction)
    .addEdge("research", "planning")
    .addEdge("planning", "design")
    .setEntryPoint("research");

CompiledGraph<OrchestrationState> compiled = graph.compile();
OrchestrationState result = compiled.invoke(initialState);
```

**Appender Reducer**:
- Accumulates artifacts across nodes
- Each node returns updated state with new artifacts
- Final state contains all artifacts from all nodes

## Decoupling Strategy

**Critical Design Principle**: Services are decoupled from langgraph4j implementation details.

**Why?**
- Services can be tested independently
- langgraph4j can be swapped/upgraded without changing services
- Domain logic stays clean

**How?**
- `ContextAssemblyService` returns `OrchestrationContext` (domain object)
- `ArtifactOutputService` receives `OrchestrationResult` (domain object)
- Only `OrchestrationService` knows about `OrchestrationState` and StateGraph
- State conversion happens in `OrchestrationService`, not in helper services

## Execution Flow

```
User Request
    ↓
OrchestrationService.orchestrate(request)
    ↓
[1] ContextAssemblyService.assembleContext(request)
    → Returns OrchestrationContext (domain)
    ↓
[2] Convert: OrchestrationContext → OrchestrationState
    ↓
[3] StateGraph Execution
    → Node 1 (research): state' = researchNode.execute(state)
    → Node 2 (planning): state'' = planningNode.execute(state')
    → Node 3 (design): state''' = designNode.execute(state'')
    → Artifacts accumulated via Appender
    ↓
[4] Extract artifacts from final OrchestrationState
    ↓
[5] Convert: OrchestrationState → OrchestrationResult (domain)
    ↓
[6] ArtifactOutputService.writeArtifacts(request, result)
    ↓
[7] Return OrchestrationResult to caller
```

## Configuration

### Execution Mode Switching
```yaml
orchestration:
  execution-mode: template  # or "llm"
```

**Template Mode** (`execution-mode: template`):
- Uses template-based NodeAction implementations
- No LLM API calls
- Fast, deterministic output
- Zero API costs
- Perfect for testing and development

**LLM Mode** (`execution-mode: llm`):
- Uses LangChain4j-based NodeAction implementations
- Real Claude API calls
- Dynamic, context-aware output
- API costs apply
- Production use

### Spring Boot Configuration

**Conditional Bean Loading**:
```java
@Component
@ConditionalOnProperty(name = "orchestration.execution-mode", havingValue = "template")
public class ResearchTemplateNode implements NodeAction { ... }

@Component
@ConditionalOnProperty(name = "orchestration.execution-mode", havingValue = "llm")
public class ResearchLLMNode implements NodeAction { ... }
```

## Implementation Phases

### ✅ Phase 1: COMPLETED (STORY-001, STORY-002)
- Domain model (OrchestrationRequest, OrchestrationContext, OrchestrationResult, etc.)
- Service interfaces
- Exception hierarchy
- OrchestrationConfig with mode switching

### ✅ Phase 2: COMPLETED (STORY-003, STORY-004, STORY-005, STORY-007)
- Template node implementations (4 nodes)
- ContextAssemblyService implementation
- ArtifactOutputService implementation
- Configuration file (application.yml)

### 🔄 Phase 3: IN PROGRESS (STORY-006)
- OrchestrationService facade
- State conversion logic
- End-to-end template mode workflow

### 📋 Phase 4: PENDING (STORY-008A, STORY-008B, STORY-008C)
- Add langgraph4j dependency
- Implement StateGraph construction
- Integrate Appender reducer
- Update OrchestrationState to extend AgentState

### 📋 Phase 5: PENDING (STORY-009, STORY-010, STORY-011)
- LangChain4j configuration
- Prompt template service
- LLM node implementations
- Real Claude API integration

## Key Benefits of This Architecture

1. **Testability**: Template mode enables testing without LLM costs
2. **Flexibility**: Easy to swap between template and LLM modes
3. **Decoupling**: Services independent of langgraph4j implementation
4. **Scalability**: StateGraph supports complex multi-agent workflows
5. **Maintainability**: Clear separation of concerns across layers
6. **Extensibility**: Easy to add new nodes or agent roles

## Migration Path

### From LangChain4j-Only to langgraph4j + LangChain4j

**Before**:
```java
// Direct LLM calls
String result = chatModel.generate(prompt);
```

**After**:
```java
// Workflow orchestration with nodes
StateGraph<OrchestrationState> graph = ...;
OrchestrationState result = graph.compile().invoke(initialState);
```

**Benefits**:
- Multi-step workflows
- State management
- Artifact accumulation
- Conditional branching
- Parallel execution (future)

## References

- langgraph4j: Java library for stateful workflows
- LangChain4j: Java LLM framework
- NodeAction: Common interface for all node types
- OrchestrationService: Facade coordinating everything

---

**Last Updated**: 2026-03-30
**Architecture Version**: 2.0 (langgraph4j + LangChain4j)
