Absolutely. Since you want this at a **higher level**, the right approach is to give Claude Code:

- a **directional architecture brief**
- not too many premature low-level constraints
- explicit encouragement to use **Java LangGraph**
- permission to plan for concerns even if the first implementation cannot fully realize them
- a requirement for a **dummy/mock orchestration implementation** that shares the same interface as the future real orchestration path

That way Claude won’t get boxed in by over-specific implementation details too early.

Below is a **first-pass higher-level instruction set** you can hand to Claude Code.

---

# High-Level Instructions for Claude Code: Evolve the Platform for LLM Orchestration

## Context

The project already implements **work item management** using an **external filesystem workspace**.

That existing work item management layer must remain the foundation of the system.

The next phase is to evolve the project into an **AI orchestration platform** that can coordinate LLM-assisted work against stories, tasks, artifacts, and reference content stored in the external workspace.

This next phase should be implemented at a **higher architectural level first**, with attention to extensibility, testing, and future orchestration needs.

---

## Primary Goal

Extend the platform so it can orchestrate AI/LLM-driven work over the existing filesystem-based work management model.

The orchestration layer should:

- operate on top of the existing story/task/artifact model
- use authoritative markdown work items from the external workspace
- produce non-authoritative outputs as append-only artifacts
- support future multi-step and multi-agent execution flows
- preserve provenance, auditability, and separation of concerns
- remain generic across managed projects
- support both:
    - a **real LLM orchestration implementation**
    - a **dummy/mock orchestration implementation** with a compatible interface for testing, local development, and proof-of-concept use

---

## Important Architectural Rule

Do not redesign the system around the LLM.

The LLM orchestration capability is an extension of the platform, not the platform’s source of truth.

The external filesystem workspace remains authoritative for:
- stories
- tasks
- work item state
- artifacts
- promoted reference content

LLM orchestration must operate through this model, not around it.

---

## Use Java LangGraph

When designing and implementing the orchestration layer, **strongly prefer using the Java LangGraph library** as the foundation for orchestration flow design.

Use it where it helps model:
- multi-step agent execution
- graph-based control flow
- branching logic
- conditional routing
- retries
- staged transformations
- future multi-agent coordination

### Guidance on LangGraph usage

- Treat LangGraph as the likely orchestration backbone for the real implementation
- Design the orchestration layer so LangGraph-based flows can be introduced cleanly and incrementally
- If LangGraph does not yet cleanly solve every concern in the first implementation, still design for those concerns explicitly
- Avoid overfitting the platform to only the simplest immediate use case
- Plan for graph-based orchestration even if the first implementation starts with a more limited execution path

### Important nuance

Some concerns in this project may not be fully handled by LangGraph out of the box, including for example:
- strict filesystem governance rules
- append-only artifact guarantees
- promotion workflows
- fine-grained write policies
- externalized work-item authority rules

That is acceptable.

Claude should:
- still use LangGraph where it is a good fit
- avoid forcing every concern into LangGraph if that creates poor design
- keep non-graph concerns in surrounding application/domain services
- design the boundaries so orchestration flow logic and workspace governance remain separate but compatible

---

## Dummy Orchestration Requirement

Implement a **dummy orchestration mode** whose interface is compatible with the eventual real LLM orchestration mode.

This is important.

The platform should support at least two interchangeable orchestration implementations:

1. **Dummy orchestration**
    - deterministic or semi-deterministic behavior
    - no real external LLM dependency required
    - useful for tests, local development, demos, and proof-of-concept runs
    - produces realistic artifact outputs, logs, and orchestration events

2. **Real LLM orchestration**
    - uses real provider-backed model execution
    - eventually may use LangGraph-backed flows and provider integrations
    - should conform to the same top-level orchestration contract as dummy mode

### Requirement
Claude should design a common interface/abstraction so callers do not need different workflows for dummy vs real orchestration.

The rest of the platform should be able to invoke orchestration without caring whether the underlying implementation is:
- mock
- simulated
- stubbed
- provider-backed
- LangGraph-backed

---

## High-Level Design Objective

Design the orchestration extension around **clear layers**.

Suggested separation:

### 1. Work Item Layer
Already exists or is assumed to exist:
- stories
- tasks
- artifacts
- reference content
- workspace validation
- state transitions

This remains authoritative.

### 2. Orchestration Domain Layer
Add concepts such as:
- orchestration request
- orchestration plan
- agent role
- run scope
- execution context
- result summary
- artifact output plan
- approval candidate
- execution outcome

This layer should be provider-agnostic.

### 3. Orchestration Engine Layer
This layer decides how a request is executed.

It may use:
- dummy orchestration implementation
- LangGraph-backed orchestration implementation
- future provider-specific execution adapters

### 4. Workspace Integration Layer
Responsible for:
- reading story/task/reference/artifact context
- writing append-only artifacts
- writing logs
- storing memory if supported
- enforcing filesystem rules

### 5. Provider/Model Layer
Responsible for:
- real model calls
- prompt submission
- result capture
- normalization of provider responses

Keep this separate from workflow/work-item concerns.

---

## Desired End State

Claude should evolve the project toward a system that can do things like:

- orchestrate a “research” run for a story
- orchestrate a “solution options” run for a story
- orchestrate a “task planning” run for a story
- orchestrate a “test strategy” run for a story or task
- orchestrate a “review preparation” run before approval
- fan out multiple agent roles against the same story
- collect outputs as append-only artifacts
- stage outputs for human review and promotion

The first implementation does not need to achieve the entire end state, but the architecture should point clearly toward it.

---

## What Claude Should Implement Next

After work item management is complete, Claude should make a first orchestration pass with these priorities:

### Priority 1: Define orchestration abstractions
Create high-level interfaces and domain types for:
- orchestration requests
- orchestration responses
- orchestration engine contract
- execution mode selection
- scoped story/task execution
- artifact-producing run results

The goal is to establish stable contracts before deep implementation.

---

### Priority 2: Implement dummy orchestration
Create a dummy/mock orchestration engine that:
- implements the same orchestration interface intended for real LLM orchestration
- accepts a story/task scope and requested operation
- reads workspace context
- produces one or more realistic append-only artifacts
- writes logs/events in a realistic way
- returns structured results
- can be used in tests and demos

This dummy implementation should be treated as a first-class part of the architecture, not a throwaway hack.

---

### Priority 3: Design for LangGraph-backed execution
Add a real orchestration implementation path that is intended to use **Java LangGraph**.

This implementation may initially be partial or skeletal, but Claude should:
- establish the integration points
- define where graph construction belongs
- define how graph nodes interact with workspace services
- define how graph execution results are converted into artifacts/logs
- keep the implementation compatible with the same orchestration interface used by the dummy mode

It is acceptable if the first LangGraph-backed implementation is incomplete, provided the architecture is sound.

---

### Priority 4: Add context assembly services
Implement services that prepare orchestration input from the external workspace.

These services should:
- gather story metadata
- gather optional task metadata
- gather relevant reference documents
- optionally gather selected artifacts
- prepare a bounded execution context
- avoid treating the entire workspace as prompt input

This context preparation should work for both dummy and real orchestration modes.

---

### Priority 5: Add execution logging and artifact persistence
Ensure orchestration runs produce:
- append-only work-item artifacts
- orchestration logs
- run summaries
- enough metadata to trace what happened

This should be done in a way that is compatible with both dummy and real orchestration.

---

## Planning for Important Concerns

Even if the first implementation cannot fully solve every concern, Claude should explicitly plan for them in the architecture.

These concerns include:

- provider abstraction
- multi-agent coordination
- graph-based execution flows
- retries and failure handling
- prompt/context composition
- output normalization
- approval workflows
- promotion of approved outputs to `reference/`
- auditability and provenance
- agent memory
- execution policies and write constraints
- testability without live LLMs
- future support for multiple model providers

Claude should not over-engineer the first implementation, but should make design choices that leave room for these concerns.

---

## Specific Guidance on Dummy vs Real Orchestration

Claude should make the dummy and real orchestration paths look the same to the rest of the application.

That means:
- same top-level interface
- same request/response shape
- same artifact-writing expectations
- same logging contract
- similar execution result structure

The difference should be internal implementation only.

### Dummy mode examples
Dummy mode can:
- generate canned markdown outputs
- simulate decision paths
- create deterministic artifact content based on story/task metadata
- simulate success/failure/retry scenarios
- support repeatable testing

### Real mode examples
Real mode can:
- use Java LangGraph for workflow execution
- call one or more real model providers
- route through graph nodes for staged reasoning or agent roles
- produce outputs that follow the same artifact contract

---

## Implementation Philosophy

Claude should optimize for:

- architectural clarity
- interchangeable orchestration implementations
- compatibility with existing filesystem work management
- testability
- future extensibility
- clear boundaries between orchestration logic and workspace governance

Claude should avoid:

- hardwiring orchestration directly into filesystem services
- tying the system to one LLM provider too early
- making dummy mode a dead-end implementation
- making graph orchestration responsible for every cross-cutting concern
- bypassing the append-only artifact model
- replacing filesystem truth with transient execution state

---

## Suggested High-Level Deliverables

Claude should produce or update:

- orchestration architecture documentation
- core orchestration interfaces
- dummy orchestration implementation
- initial LangGraph-backed orchestration integration design
- workspace context assembly services
- orchestration logging model
- artifact output handling for orchestration runs
- tests proving interchangeability of dummy and real orchestration interfaces

---

## Acceptance Criteria for This Next Step

This next phase is successful if:

1. the project has a clear orchestration abstraction layer
2. dummy orchestration is implemented and usable
3. dummy orchestration uses the same interface intended for real orchestration
4. the architecture explicitly plans for Java LangGraph usage
5. workspace context can be assembled for orchestration runs
6. orchestration outputs are written as append-only artifacts
7. orchestration logs are captured separately from work-item artifacts
8. the design preserves the existing authoritative/non-authoritative separation
9. the system remains generic across managed projects
10. the codebase is prepared for later real LLM integration without major redesign

---

## Final Direction

Claude should treat this phase as building the **orchestration foundation**, not the final orchestration capability.

The immediate objective is to:
- establish the right abstraction boundaries
- implement a useful dummy orchestration path
- prepare the codebase for a real Java LangGraph-based orchestration engine
- preserve the existing file-based work management model as the governing contract

---

If you want, I can next turn this into one of these more directly usable forms:

1. a **`CLAUDE.md` addendum**
2. a **single copy-paste prompt for Claude Code**
3. a **set of implementation stories/tasks**
4. a **Spring Boot package/class outline for dummy + LangGraph orchestration**

My recommendation is **#1 or #4** next.
