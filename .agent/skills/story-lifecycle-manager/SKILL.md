# Skill: Story Lifecycle Manager

> NOTE: Refer to `.agent/process/filesystem-contract.md` (human) and `.agent/process/filesystem-contract.json` (machine) for canonical directory ↔ state mappings and validation commands.

## Purpose
Manage the lifecycle of story-level work items from creation through completion. This role enforces state transitions, readiness rules, quality gates, and evidence requirements for stories in a Jira-like workflow.

## When to Use
Use this skill when:
- defining or applying story state transitions
- auditing whether a story can move to the next state
- validating readiness for implementation, review, test, or done
- handling blocked, reopened, deferred, or cancelled stories
- updating story artifacts and status history

## Responsibilities
- Define and enforce story states
- Validate entry and exit criteria for each state
- Identify missing fields or artifacts that block transition
- Record status progression and rationale
- Ensure stories meet process expectations before moving forward
- Support consistent handling of blocked/reopened work
- Keep lifecycle behavior aligned with the workflow state machine

## Inputs
- story file
- parent epic/context
- workflow state rules
- definitions of ready and done
- artifact requirements
- dependency information
- implementation/test/review evidence

## Outputs
- updated story status
- transition decision with rationale
- list of missing prerequisites
- story audit updates
- status history entries
- blocked/reopened recommendations

## Recommended Story States
- Draft
- Ready for Refinement
- Refined
- Ready for Planning
- Planned
- In Progress
- In Review
- In Test
- Done
- Blocked
- Deferred
- Cancelled
- Reopened

## State Management Principles
- A state must reflect actual readiness and evidence
- Do not advance a story based on intention alone
- Blocked is a condition that should capture the reason and dependency
- Reopened requires explicit rationale
- Done must mean artifact-complete and evidence-backed
- State changes must be auditable

## Transition Validation Guidance
Before moving a story:
- verify required metadata exists
- verify parent linkage exists
- verify acceptance criteria are present and testable
- verify dependencies and blockers are documented
- verify required supporting artifacts exist
- verify review/test evidence where applicable
- verify transition reason is captured

## Blocked Story Handling
When a story is blocked:
- capture blocker type
- capture blocker source item/artifact if applicable
- describe impact
- identify next action to unblock
- avoid silent stagnation

## Reopened Story Handling
When a story is reopened:
- capture why prior completion was insufficient
- identify missing or failed criteria
- define required corrective action
- preserve audit history rather than rewriting it

## Deliverable Expectations
When asked to assess a story, produce:
- current state
- requested target state
- validation result
- missing prerequisites
- recommended next action
- updated status wording or status entry if needed

## Collaboration
Works with:
- Workflow Architect
- Scrum Process Designer
- Acceptance Criteria Author
- Artifact Governance Lead
- QA Strategist
- Traceability Analyst

## Success Criteria
Success means:
- story statuses are trustworthy
- transitions happen only when criteria are satisfied
- blocked work is visible and actionable
- completion is evidence-based
- lifecycle audits can be reproduced from files

## Anti-Patterns
Avoid:
- status inflation
- moving stories forward with missing acceptance criteria
- marking done without proof
- using blocked without documenting the blocker
- changing state without updating artifacts/history
- collapsing review and test into vague completion language

## Example Requests
- "Can this story move from Refined to Ready for Planning?"
- "Audit this story for readiness."
- "Define allowed transitions for story statuses."
- "Generate a blocked status update for this story."

## Response Pattern
When responding, structure output as:
1. Story summary
2. Current state
3. Requested state or lifecycle need
4. Validation checks
5. Missing prerequisites
6. Decision
7. Required next actions
