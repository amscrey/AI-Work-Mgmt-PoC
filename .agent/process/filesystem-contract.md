---
name: filesystem-contract
---

# Filesystem Contract (Source-of-Truth)

This document is the single authoritative source that defines how work items are represented on disk and how directory locations map to workflow/prioritization states. Agent roles and skills (especially `orchestrator` and `agile-work-item-mgr`) MUST reference this file and keep it up to date.

## Purpose
- Make the `Filesystem = Source of Truth` principle explicit in one place.
- Describe canonical directories and allowed state mappings.
- Provide quick validation and movement commands for orchestrators.

## Canonical Directories
- `backlog/` — prioritizationState: BACKLOG. New stories MUST be created here.
- `prioritized/` — prioritizationState: PRIORITIZED. Work intended for implementation (TODO, IN_PROGRESS, BLOCKED) lives here.
- `done/` — workflowState: DONE. Completed work items MOVE HERE and must have evidence in their `story.json` and artifacts.

## Canonical Mapping (directory ↔ story.json)
- backlog/  ↔ `story.json.prioritizationState == "BACKLOG"`
- prioritized/ ↔ `story.json.prioritizationState == "PRIORITIZED"` (and `workflowState` may be `TODO` or `IN_PROGRESS`)
- done/ ↔ `story.json.workflowState == "DONE"`

## Rules (short)
- ALWAYS keep the directory location and `story.json` fields consistent.
- IMMEDIATELY move the directory after updating the relevant `story.json` fields.
- Validate dependencies before moving BACKLOG → PRIORITIZED.
- Do not mark a story `DONE` without evidence (completedBy, completedAt, completion comment, artifacts, tests).

## Validation commands (examples)
- Check a backlog story matches JSON:
  test -d backlog/STORY-XXX && grep -q '"prioritizationState"\s*:\s*"BACKLOG"' backlog/STORY-XXX/story.json

- Move a story after updating JSON:
  mv backlog/STORY-XXX prioritized/STORY-XXX

## Recommended referencing
- Add a link to this file from:
  - `.agent/roles/orchestrator/ROLE.md`
  - `.agent/skills/agile-work-item-mgr/SKILL.md`
  - `.agent/skills/story-lifecycle-manager/SKILL.md`
  - `etc/docs/WORKFLOW.md`



