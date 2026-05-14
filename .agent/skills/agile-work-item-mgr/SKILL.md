---
name: agile-work-item-mgr
description: Orchestrator-specific agile workflow management for creating, updating, and transitioning work items with proper filesystem synchronization
---
# Agile Work Item Manager Skill

> NOTE: Canonical filesystem mappings and movement rules are maintained in `.agent/process/filesystem-contract.md` (human) and `.agent/process/filesystem-contract.json` (machine-readable). Skills and roles should reference that file for authoritative rules.

**For orchestrator role only** - Manages creation, updates, transitions, and lifecycle of work items with proper filesystem synchronization.

## CRITICAL PRINCIPLE: Filesystem = Source of Truth

Directory location MUST always match work item state. Never leave a story in the wrong directory.

```
workspace/
├── backlog/       # prioritizationState: BACKLOG
├── prioritized/   # prioritizationState: PRIORITIZED
└── done/          # workflowState: DONE
```

---

## Creating Stories

**ALWAYS create new stories in backlog/ with BACKLOG state:**

### Story Creation Steps

1. **Create directory**: `backlog/STORY-XXX-title/`
2. **Create story.json** with:
   ```json
   {
     "id": "STORY-XXX",
     "title": "Story Title",
     "description": "Description",
     "workflowState": "TODO",
     "prioritizationState": "BACKLOG",
     "acceptanceCriteria": ["Acceptance criteria placeholder"],
     "estimatedEffort": "X days",
     "dependencies": [],
     "blocks": [],
     "tags": ["example"]
   }
   ```
3. **Create subdirectories**:
   - `backlog/STORY-XXX-title/artifacts/`
   - `backlog/STORY-XXX-title/comments/`
4. **Log**: `story_created` activity

**NEVER auto-prioritize** - stories start in BACKLOG until explicitly prioritized.

---

## Prioritizing Stories

**Moving from BACKLOG → PRIORITIZED:**

### Prioritization Steps

1. **Verify prerequisites**:
   - Dependencies are complete
   - Story has clear acceptance criteria
   - Justified prioritization reason

2. **Update story.json**:
   ```json
   {
     "prioritizationState": "PRIORITIZED"
   }
   ```

3. **Add comment (BEFORE move)**:
   ```json
   {
     "type": "NOTE",
     "text": "Prioritized for implementation - [reason]. Dependencies: [list]. Next in sequence: [context].",
     "author": "orchestrator",
     "timestamp": "2026-04-03T12:40:00Z"
   }
   ```

4. **CRITICAL: Move directory (AFTER comment)**:
   ```bash
   mv backlog/STORY-XXX prioritized/STORY-XXX
   ```

5. **Log**: `prioritization_changed` activity

6. **Verify**: Story now in `prioritized/` directory

**Validation**: After prioritization, verify `ls prioritized/STORY-XXX` succeeds.

---

## Assigning Stories

**Assigning work to a role:**

### Assignment Steps

1. **Verify story is PRIORITIZED** (in `prioritized/` directory)

2. **Update story.json**:
   ```json
   {
     "workflowState": "IN_PROGRESS",
     "assignedTo": "role-name"
   }
   ```

3. **Add work started comment**:
   ```json
   {
     "type": "NOTE",
     "text": "🚀 Work started - Assigned to [role] role. Will implement [summary]. All [N] existing tests must continue passing.",
     "author": "orchestrator",
     "timestamp": "2026-04-03T12:45:00Z"
   }
   ```

4. **Log**: `role_delegated` and `state_transition` activities

**Story remains in prioritized/** - only moves to done/ when DONE.

---

## Completing Stories

**When a role completes work:**

### Completion Steps

1. **Verify work is complete**:
   - All acceptance criteria met
   - Tests passing
   - Artifacts created

2. **Update story.json**:
   ```json
   {
     "workflowState": "DONE",
     "completedBy": "role-name",
     "completedAt": "2026-04-03T15:30:00Z"
   }
   ```

3. **Add completion comment (BEFORE move)**:
   ```json
   {
     "type": "COMPLETION",
     "text": "✅ Story completed successfully. [Summary of work]. Tests: [N] passing. Files: [list]. Architecture notes: [any important decisions].",
     "author": "role-name",
     "timestamp": "2026-04-03T15:30:00Z"
   }
   ```

4. **CRITICAL: Move directory to done/ (AFTER comment)**:
   ```bash
   mv prioritized/STORY-XXX done/STORY-XXX
   ```

5. **Log**: `work_completed` and `state_transition` activities

6. **Verify**: Story now in `done/` directory

**Validation**: After completion, verify `ls done/STORY-XXX` succeeds.

---

## Directory Movement Rules

**Every state change MUST trigger directory movement:**

| State Change | Directory Movement | Log Event |
|--------------|-------------------|-----------|
| Create story | → `backlog/` | `story_created` |
| BACKLOG → PRIORITIZED | `backlog/` → `prioritized/` | `prioritization_changed` |
| TODO → IN_PROGRESS | Stay in `prioritized/` | `state_transition` |
| IN_PROGRESS → DONE | `prioritized/` → `done/` | `state_transition` + `work_completed` |
| PRIORITIZED → BACKLOG (rare) | `prioritized/` → `backlog/` | `prioritization_changed` |

**CRITICAL**:
- Move happens IMMEDIATELY after JSON update and comment creation
- Never leave story in wrong directory
- Always verify move succeeded

---

## State Validation

**Before any operation:**

1. **Check directory matches state**:
   ```bash
   # If story.json says BACKLOG, verify:
   test -d backlog/STORY-XXX

   # If story.json says PRIORITIZED, verify:
   test -d prioritized/STORY-XXX

   # If story.json says DONE, verify:
   test -d done/STORY-XXX
   ```

2. **Fix mismatches immediately**:
   - If JSON says PRIORITIZED but story in backlog/: move it
   - If JSON says DONE but story in prioritized/: move it
   - Log the correction

---

## Common Workflows

### Workflow 1: Create and Prioritize Story

```bash
# 1. Create in backlog
mkdir -p backlog/STORY-015-new-feature/{artifacts,comments}
# Edit story.json with prioritizationState: BACKLOG

# 2. Prioritize
# Edit story.json: prioritizationState → PRIORITIZED
# Add comment with justification
mv backlog/STORY-015-new-feature prioritized/STORY-015-new-feature
```

### Workflow 2: Assign and Complete Story

```bash
# 1. Assign (story already in prioritized/)
# Edit story.json: workflowState → IN_PROGRESS, assignedTo → "coder"
# Add work started comment

# 2. Complete
# Edit story.json: workflowState → DONE, completedBy, completedAt
# Add completion comment
mv prioritized/STORY-015-new-feature done/STORY-015-new-feature
```

### Workflow 3: Scan for Work

```bash
# Find all prioritized stories ready to assign
find prioritized/ -name "story.json" -exec grep -l '"workflowState" : "TODO"' {} \;

# Find all in-progress stories
find prioritized/ -name "story.json" -exec grep -l '"workflowState" : "IN_PROGRESS"' {} \;
```

---

## Error Recovery

### Story in Wrong Directory

**Problem**: Story JSON says PRIORITIZED but story is in backlog/

**Fix**:
1. Verify JSON state is correct
2. Move directory to match: `mv backlog/STORY-XXX prioritized/STORY-XXX`
3. Log correction: `error_occurred` + `directory_corrected`
4. Add comment explaining the fix

### Multiple Stories with Same ID

**Problem**: STORY-XXX exists in both backlog/ and prioritized/

**Fix**:
1. Check timestamps in comments to determine which is current
2. Delete or rename the stale one
3. Log the issue and resolution

---

## Logging Requirements

**Always log these activities:**

- `story_created` - When creating new story in backlog/
- `prioritization_changed` - When moving backlog/ ↔ prioritized/
- `state_transition` - When changing workflowState
- `role_delegated` - When assigning work to a role
- `work_completed` - When marking story DONE
- `directory_moved` - When moving story directory
- `validation_performed` - When checking state/directory consistency
- `error_occurred` - When encountering mismatches or errors

---

## Orchestrator Authority

**As orchestrator, you CAN:**

- ✅ Create stories in backlog/
- ✅ Modify story.json (all fields)
- ✅ Move directories between backlog/, prioritized/, done/
- ✅ Transition all workflow states
- ✅ Prioritize/deprioritize stories
- ✅ Assign work to roles
- ✅ Add comments to stories
- ✅ Create/delete subdirectories
- ✅ Curate comments into story summaries

**Critical Constraints:**

- ⚠️ **NEVER** leave directory location mismatched with state
- ⚠️ **NEVER** skip directory movement after state change
- ⚠️ **NEVER** auto-prioritize without justification
- ⚠️ **ALWAYS** verify directory movement succeeded
- ⚠️ **ALWAYS** log state transitions
- ⚠️ **ALWAYS** maintain filesystem = source of truth

---

## Quick Reference

**Create Story**: backlog/ + BACKLOG state + log
**Prioritize**: PRIORITIZED + mv backlog→prioritized + comment + log
**Assign**: IN_PROGRESS + assignedTo + comment + log
**Complete**: DONE + completedBy/At + mv prioritized→done + comment + log

**Filesystem Structure**:
```
backlog/STORY-XXX/         # BACKLOG
prioritized/STORY-YYY/     # PRIORITIZED (TODO|IN_PROGRESS|BLOCKED)
done/STORY-ZZZ/            # DONE
```

**Always verify**: Directory location matches JSON state!
