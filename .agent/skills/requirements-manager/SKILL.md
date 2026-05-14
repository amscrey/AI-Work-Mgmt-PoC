---
name: requirements-manager
description: Gather, analyze, refine, and validate requirements with human collaboration. Use when starting new projects, clarifying ambiguous requirements, identifying concerns, mapping requirements to agents, or documenting refined specifications.
---

# Requirements Manager

Facilitates collaborative requirements gathering and refinement between humans and AI agents. Analyzes requirements for completeness, consistency, and feasibility, then maps validated requirements to appropriate agent skills.

## Input
- Initial requirements document (any format: brief, spec, user story, Claude Code implementation brief)
- Human feedback and clarifications
- Existing project context (if available)
- Constraints and non-negotiables

## Output
- Requirements summary document (`requirements-summary.md`)
- Concerns and risks document (`requirements-concerns.md`)
- Refined requirements document (`requirements-refined.md`)
- Agent assignment map (`requirements-agent-map.md`)
- Approval record with human sign-off
- Implementation readiness report

---

## Process

### Phase 1: Read and Parse Initial Requirements
1. Read the supplied requirements document in full
2. Identify document format and structure
3. Extract key sections:
   - Objectives and goals
   - Scope boundaries (in-scope, out-of-scope)
   - Functional requirements
   - Non-functional requirements (performance, security, etc.)
   - Technical constraints
   - Data contracts and file contracts
   - Acceptance criteria
   - Non-negotiable constraints
4. Extract implicit requirements (read between the lines)
5. Identify ambiguous or unclear statements
6. Note missing typical requirements (error handling, logging, testing, etc.)

### Phase 2: Summarize to Human
1. Create executive summary (3-5 bullet points)
2. Organize requirements into logical categories:
   - **Core Functionality**: What the system must do
   - **Architecture**: How the system is structured
   - **Data Contracts**: File formats, schemas, naming conventions
   - **Workflow**: State transitions, lifecycle management
   - **Quality Attributes**: Performance, security, maintainability
   - **Constraints**: Technology choices, boundaries, non-negotiables
3. Present summary to human using structured formats:
   - **Use tables** for comparing options, listing fields, showing mappings
   - **Use lists** (bulleted/numbered) for sequences, rules, criteria
   - **Use tree diagrams** for hierarchies, directory structures, relationships
   - **Use mermaid diagrams** for workflows, state machines, architecture
   - Keep text concise and scannable
4. Ask: "Does this summary accurately capture your intent?"

### Phase 3: Identify and Present Concerns
1. Analyze requirements for potential issues:
   - **Ambiguity**: Requirements open to multiple interpretations
   - **Conflicts**: Requirements that contradict each other
   - **Feasibility**: Requirements that may be difficult or impossible
   - **Scope Creep**: Requirements that expand beyond stated goals
   - **Missing Details**: Critical information not specified
   - **Risk Areas**: High complexity, external dependencies, unknowns
   - **Over-Engineering**: Unnecessary complexity or premature optimization
   - **Under-Specification**: Insufficient detail for implementation
2. Categorize concerns by severity:
   - **Blocker**: Must resolve before proceeding
   - **High**: Should resolve before proceeding
   - **Medium**: Should clarify during implementation
   - **Low**: Nice to clarify but not critical
3. Present concerns to human with:
   - Clear description of the issue
   - Why it's concerning
   - Suggested resolution or question to clarify
   - Impact if left unresolved
4. Ask targeted questions to resolve high-priority concerns
5. Document human responses

### Phase 4: Refine and Validate Requirements
1. Incorporate human feedback from Phase 3
2. Expand ambiguous requirements into concrete specifications
3. Resolve conflicts with human guidance
4. Fill gaps in requirements with human approval
5. Add derived requirements based on core requirements:
   - Error handling patterns
   - Logging and observability
   - Testing strategy
   - Documentation needs
   - Validation rules
6. Organize refined requirements into implementation-ready format
7. Create traceability between initial and refined requirements
8. Present refined requirements to human
9. Ask: "Are these refined requirements accurate and complete?"
10. Obtain explicit approval before proceeding

### Phase 5: Map Requirements to Agents
1. Analyze each requirement cluster
2. Identify which agent skills are needed:
   - **workspace-initializer**: Workspace structure, validation, initialization
   - **artifact-governance-lead**: Artifact catalog, naming conventions, traceability
   - **java-spring-architect**: Application architecture, package structure, layering
   - **markdown-template-designer**: Template schemas, metadata contracts
   - **workflow-architect**: State model, transitions, lifecycle rules
   - **story-lifecycle-manager**: Story creation, state transitions
   - **domain-model-designer**: Entities, value objects, aggregates
   - **file-contract-validator**: Validation rules, contract enforcement
   - Custom skills as needed
3. Create agent assignment map showing:
   - Requirement ID or category
   - Assigned agent skill(s)
   - Dependencies between agents
   - Execution order recommendations
4. Identify requirements that need new skills created
5. Flag requirements that need human involvement (decisions, approvals)

### Phase 6: Persist Refined Requirements
1. Create `/requirements/` directory in project
2. Write `requirements-summary.md`:
   - Executive summary
   - Key objectives
   - Scope boundaries
   - High-level architecture
3. Write `requirements-refined.md`:
   - Complete refined requirements
   - Organized by category
   - Numbered for traceability
   - Acceptance criteria for each
4. Write `requirements-concerns.md`:
   - All concerns raised
   - Resolution status
   - Human responses
   - Remaining open issues
5. Write `requirements-agent-map.md`:
   - Agent assignments
   - Requirement mapping
   - Dependencies
   - Execution order
6. Write `requirements-approval.md`:
   - Approval timestamp
   - Approver name
   - Approved version/hash
   - Changes since approval (if any)
7. Create `requirements-changelog.md` for future updates

### Phase 7: Generate Implementation Readiness Report
1. Assess completeness (all requirements clear and approved?)
2. Assess feasibility (all requirements achievable?)
3. Assess agent coverage (all requirements mapped to agents?)
4. Identify blockers requiring resolution
5. Identify risks requiring mitigation
6. Recommend next steps:
   - Immediate: Start with these agents/requirements
   - Short-term: Follow-up work
   - Deferred: Future enhancements
7. Present readiness report to human

---

## Concern Categories

### Ambiguity Patterns
- "The system should be fast" (What is fast? Quantify.)
- "Handle errors appropriately" (What is appropriate?)
- "Support multiple users" (How many? Concurrent? Roles?)
- "Easy to use" (For whom? What metrics?)

### Conflict Patterns
- "Must be stateless" vs "Must maintain session"
- "All data in files" vs "Use a database"
- "Simple implementation" vs "Highly flexible and configurable"

### Feasibility Concerns
- Unrealistic performance targets
- Technology choices incompatible with requirements
- Insufficient resources or time
- External dependencies with unknown availability

### Missing Critical Details
- No error handling strategy
- No security requirements
- No testing requirements
- No deployment strategy
- No data migration strategy
- No versioning strategy

### Scope Creep Indicators
- "Also should support..."
- "In the future we might..."
- "It would be nice if..."
- Feature lists growing beyond core objectives

---

## Requirement Refinement Patterns

### From Ambiguous to Concrete

**Before**: "The system should manage work items efficiently"
**After**:
- System must create a story directory in < 100ms
- System must transition story state in < 50ms
- System must validate workspace structure in < 500ms
- System must support 1000+ stories per workspace

**Before**: "Use markdown files"
**After**:
- All work items stored as markdown with YAML frontmatter
- Frontmatter uses snake_case field names
- Timestamps use ISO 8601 format
- Story filename format: `story.md` (always)
- Artifact filename format: `<type>__<agent>__<timestamp>.<ext>`

**Before**: "Support multiple agents"
**After**:
- Agents write append-only artifact files only
- Artifact filenames include agent name and UTC timestamp
- No agent overwrites another agent's artifacts
- Concurrent writes must not cause data corruption
- Agent identity tracked in artifact metadata

---

## Agent Assignment Patterns

### Workspace Structure Requirements → workspace-initializer
- Initialize workspace
- Validate directory structure
- Create configuration files
- Generate initial templates

### Artifact Requirements → artifact-governance-lead
- Define artifact catalog
- Define naming conventions
- Define folder structure
- Define traceability rules

### Application Architecture → java-spring-architect
- Design package structure
- Define layer boundaries
- Design dependency injection
- Plan configuration strategy

### Template Requirements → markdown-template-designer
- Design story.md template
- Design task.md template
- Design artifact templates
- Define metadata schemas

### Workflow Requirements → workflow-architect
- Define lifecycle states
- Define state transitions
- Define transition rules
- Define quality gates

### Domain Model → domain-model-designer (if created)
- Identify entities
- Identify value objects
- Design aggregates
- Define domain services

---

## Approval Protocol

Before proceeding to implementation, obtain explicit approval on:

1. **Summary Accuracy**: "Does this summary capture your intent?"
2. **Concerns Addressed**: "Have we resolved all blocker concerns?"
3. **Refined Requirements**: "Are these requirements accurate and complete?"
4. **Agent Assignments**: "Does this agent mapping make sense?"

Record approval with:
- Timestamp
- Approver name
- What was approved (specific document/version)
- Any caveats or conditions

---

## Completeness Checklist
- □ Initial requirements fully read and parsed?
- □ Summary presented to human and confirmed?
- □ All concerns identified and categorized?
- □ Blocker concerns resolved with human?
- □ Refined requirements documented and approved?
- □ Agent assignments mapped with dependencies?
- □ All requirements documents persisted to `/requirements/`?
- □ Implementation readiness report generated?
- □ Human approval obtained and recorded?

## Rules
1. **ALWAYS** read the complete initial requirements before summarizing
2. **ALWAYS** present summary to human for validation before deep analysis
3. **ALWAYS** use tables, lists, tree diagrams, and mermaid diagrams when presenting information to humans
4. **ALWAYS** identify and surface concerns before proceeding
5. **ALWAYS** obtain explicit human approval on refined requirements
6. **ALWAYS** map requirements to specific agent skills
7. **ALWAYS** persist refined requirements to `/requirements/` directory
8. **ALWAYS** record human approval with timestamp and version
9. **NEVER** assume or infer requirements without human confirmation
10. **NEVER** proceed to implementation without human approval
11. **NEVER** ignore blocker-level concerns
12. **NEVER** create requirements that contradict non-negotiable constraints
13. **NEVER** let scope creep into refined requirements without explicit human approval
14. **NEVER** assign requirements to agents without understanding agent capabilities
15. **ALWAYS** ask clarifying questions for ambiguous requirements
16. **ALWAYS** call out missing critical requirements (security, error handling, testing)
17. **ALWAYS** make information scannable and structured for human consumption