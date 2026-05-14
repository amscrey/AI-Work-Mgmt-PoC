---
name: agile-worker
description: Enforces proper agile workflow for story lifecycle management. Ensures stories are prioritized before work begins, activities are logged, and progress is documented via comments.
---

# Agile Worker Skill

Enforces proper agile workflow for the AI Work Management Platform.

## CRITICAL WORKFLOW RULES

### Filesystem = Source of Truth

**Directory structure MUST match state:**
- `backlog/STORY-XXX/` → `prioritizationState: "BACKLOG"`
- `prioritized/STORY-XXX/` → `prioritizationState: "PRIORITIZED"`
- `done/STORY-XXX/` → `workflowState: "DONE"`

### Before Starting ANY Work on a Story

**ALWAYS check prioritization state:**

1. **Read story.json** to check `prioritizationState`
2. **If BACKLOG**:
   - ❌ STOP - Do not start work
   - ✅ Move to PRIORITIZED first:
     - Update JSON: `"prioritizationState": "PRIORITIZED"`
     - **Move directory**: `mv backlog/STORY-XXX prioritized/STORY-XXX`
     - Add comment explaining why prioritized
   - 📢 Inform user: "Moved STORY-XXX from BACKLOG to PRIORITIZED"
3. **If PRIORITIZED** AND assignedTo role-name: ✅ Proceed

**ALWAYS check dependencies before work:**
- Read `dependencies` and `blocks` in `story.json`.
- If any dependency story is not DONE, STOP and notify orchestrator.
- If blocked, add a `QUESTION` comment and log `blocked`.

### During Work - Required Activities

**1. Log Activities**
- `work_started` - When beginning
- `milestone_reached` - Major progress
- `blocked` - When stuck
- `work_completed` - When complete

**2. Add Story Comments**
```json
{
  "type": "NOTE",
  "text": "Starting implementation of X",
  "author": "role-name",
  "timestamp": "2026-04-03T..."
}
```

**Comment Types**:
- `NOTE` - Progress, decisions
- `QUESTION` - Blockers, need help
- `ANSWER` - Resolutions

**3. Document Key Decisions**
- Why you chose an approach
- Trade-offs considered
- Deviations from acceptance criteria

### After Completing Work

**1. Update Story State to awaiting-approval/**
```json
{
  "workflowState": "AWAITING-APPROVAL",
  "completedBy": "role-name",
  "completedAt": "2026-04-03T12:00:00Z"
}
```
**CRITICAL: Move directory to done/**
```bash
mv prioritized/STORY-XXX done/STORY-XXX
```

**2. Add Final Comment**
```json
{
  "type": "COMPLETION",
  "text": "✅ Complete: [summary of work]. Tests: X passing. Files: Y created.",
  "author": "role-name"
}
```

**3. Log Completion**
- Activity: `work_completed`
- Include: artifacts created, tests added, coverage

## Story Creation (Orchestrator/Planning)

**NEVER create story, ONLY ORCHESTRATOR CREATES:**

## State Transitions

```
Prioritization: BACKLOG → PRIORITIZED (conscious decision)
Execution: TODO → IN_PROGRESS → AWAITING-APPROVAL
```

## Quick Checklist

**Before implementing:**
- [ ] Story is PRIORITIZED (not BACKLOG)
- [ ] Added initial comment
- [ ] Logged work_started

**During:**
- [ ] Adding comments for decisions
- [ ] Logging milestones

**After:**
- [ ] workflowState = DONE
- [ ] completedAt timestamp added
- [ ] Final summary comment added
- [ ] Logged work_completed
- [ ] Notify Orchestrator story is ready for review
---

**Reference**: See `etc/docs/WORKFLOW.md` for comprehensive documentation
