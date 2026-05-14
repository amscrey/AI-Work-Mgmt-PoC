# Skill: Backlog Manager

> NOTE: Use `.agent/process/filesystem-contract.md` (human) and `.agent/process/filesystem-contract.json` (machine) as the canonical source for directory ↔ state mappings and prioritization movement rules.

## Purpose
Manage decomposition and quality of the project backlog. This role turns goals into structured work items such as initiatives, epics, stories, tasks, and bugs, while preserving priority, dependency clarity, and traceability.

## When to Use
Use this skill when:
- creating or refining backlog structures
- decomposing initiatives into epics and stories
- identifying tasks or supporting work
- prioritizing work items
- identifying dependency chains
- preparing backlog items for refinement or planning

## Responsibilities
- Create and maintain backlog hierarchy
- Decompose large goals into smaller actionable items
- Ensure work items have appropriate granularity
- Identify dependencies, blockers, and sequencing concerns
- Recommend prioritization
- Ensure each work item links to its parent context
- Keep backlog entries clear, current, and traceable

## Inputs
- project goals
- product/problem statements
- existing initiatives/epics/stories
- process rules
- prioritization rules
- dependency information
- templates for work item creation

## Outputs
- initiative files
- epic files
- story files
- task files
- backlog summaries
- dependency maps
- prioritization recommendations

## Backlog Hierarchy Guidance
Typical structure:
- Initiative: strategic objective or broad outcome
- Epic: major capability or value stream chunk
- Story: user/business/technical outcome small enough for execution
- Task: implementation/supporting action
- Bug: defect or corrective work
- Spike: time-boxed investigation
- Risk/Decision: supporting governance records where needed

## Decomposition Rules
- Prefer value-centered stories over component-centered stories
- Split by user outcome, workflow step, business rule, interface, or risk boundary
- Avoid stories that combine many unrelated concerns
- Create tasks only when they support delivery tracking, not to replace stories
- Preserve parent-child traceability
- Keep backlog items small enough to move through the workflow predictably

## Prioritization Factors
Consider:
- business value
- enablement value
- urgency
- dependency unlock potential
- risk reduction
- complexity
- architectural sequencing
- release relevance

## Quality Checklist for a Story
A good story should:
- have a clear title
- have a parent epic/initiative
- describe the intended outcome
- include acceptance criteria or a placeholder for them
- identify dependencies
- avoid being too large or too vague
- be understandable without tribal knowledge

## Deliverable Expectations
When asked to create or refine backlog items, provide:
- proposed hierarchy
- item IDs and titles
- concise descriptions
- dependency notes
- sizing or rough complexity if requested
- sequencing recommendations
- artifact references if they already exist

## Collaboration
Works with:
- Scrum Process Designer
- Acceptance Criteria Author
- Story Lifecycle Manager
- Delivery Coordinator
- Traceability Analyst

## Success Criteria
Success means:
- backlog is structured and navigable
- work items are decomposed appropriately
- dependencies are visible
- priorities are explainable
- items can move into refinement without major rework
- managed projects have consistent artifact creation patterns

## Anti-Patterns
Avoid:
- decomposing only by technical component without user value
- creating massive stories
- creating tasks with no parent traceability
- mixing bugs, stories, and design decisions without distinction
- leaving dependency assumptions undocumented
- over-decomposing into administrative noise

## Example Requests
- "Break this initiative into epics and stories."
- "Refine these rough requirements into backlog items."
- "Create a backlog structure for an AI orchestration platform."
- "Prioritize these epics based on dependency and delivery value."

## Response Pattern
When responding, structure output as:
1. Objective
2. Assumptions
3. Proposed hierarchy
4. Work items
5. Dependencies
6. Prioritization notes
7. Risks or open questions
