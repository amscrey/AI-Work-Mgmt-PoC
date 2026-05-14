# Story Lifecycle Workflow

**Version**: 1.0
**Last Updated**: 2026-04-03

This document defines the complete story lifecycle workflow for the AI Work Management Platform. All agents and developers must follow these rules.

---

## Table of Contents

1. [Story States](#story-states)
2. [Workflow Rules](#workflow-rules)
3. [Agent Responsibilities](#agent-responsibilities)
4. [Code Examples](#code-examples)
5. [Common Patterns](#common-patterns)
6. [Troubleshooting](#troubleshooting)

---

## Story States

### Prioritization Pipeline

Stories move through a prioritization pipeline that determines whether they're ready for work:

| State | Description | Can Start Work? |
|-------|-------------|-----------------|
| **BACKLOG** | Story created but not yet approved for work | ❌ NO |
| **PRIORITIZED** | Story approved for work, ready to be assigned | ✅ YES |

**Key Rule**: Work can ONLY be performed on `PRIORITIZED` stories.

### Execution Pipeline

Once prioritized, stories move through the execution pipeline:

| State | Description | Next Valid States |
|-------|-------------|-------------------|
| **TODO** | Ready to start (default) | IN_PROGRESS |
| **IN_PROGRESS** | Work actively happening | AWAITING_APPROVAL, BLOCKED |
| **AWAITING_APPROVAL** | Work completed by implementer, awaiting human approval | DONE, IN_PROGRESS |
| **BLOCKED** | Waiting on dependency/decision | IN_PROGRESS, TODO |
| **DONE** | Work completed and verified | _(terminal)_ |

**State Transitions** (enforced by domain model):
```
TODO → IN_PROGRESS → DONE
     ↓              ↓
  BLOCKED ←--------┘
```

---

## Filesystem Structure

**CRITICAL: Directory location = Source of Truth**

Stories MUST reside in directories matching their state:

```
workspace/
├── backlog/         # prioritizationState: BACKLOG
│   └── STORY-XXX/
│       ├── story.json
│       ├── artifacts/
│       └── comments/
├── prioritized/     # prioritizationState: PRIORITIZED, workflowState: TODO|IN_PROGRESS|BLOCKED|AWAITING_APPROVAL
│   └── STORY-YYY/
│       ├── story.json
│       ├── artifacts/
│       └── comments/
└── done/            # workflowState: DONE
    └── STORY-ZZZ/
        ├── story.json
        ├── artifacts/
        └── comments/
```

### Directory Movement Rules

**When prioritizing a story**:
1. Update JSON: `"prioritizationState": "PRIORITIZED"`
2. **Move directory**: `mv backlog/STORY-XXX prioritized/STORY-XXX`
3. Log: `prioritization_changed`

**When completing implementation (implementer)**:
1. Update JSON: `"workflowState": "AWAITING_APPROVAL"`
2. Add completion comment and test results
3. Log: `work_completed`
4. **Do NOT move directory** (still stays in `prioritized/`)

**When approving completion (orchestrator + human)**:
1. Orchestrator requests human approval
2. On approval, update JSON: `"workflowState": "DONE"`, `"completedAt": "..."`
3. **Move directory**: `mv prioritized/STORY-XXX done/STORY-XXX`
4. Log: `state_transition`

**Validation**:
- Before starting work: Verify story is in `prioritized/`
- After any state change: Verify directory matches state
- Never have mismatched directory location and state

---

## Workflow Rules

### Rule 1: Story Creation (CRITICAL)

**All new stories MUST be created in BACKLOG state:**

```json
{
  "id": "STORY-XXX",
  "title": "...",
  "workflowState": "TODO",
  "prioritizationState": "BACKLOG"  // ← Always BACKLOG
}
```

**Why**: This ensures conscious prioritization decisions. Stories don't automatically become work items.

### Rule 2: Prioritization (CRITICAL)

**Before starting work on ANY story:**

1. Check: Is `prioritizationState` == `"PRIORITIZED"`?
2. If NO (still in BACKLOG):
   - Update story: `"prioritizationState": "PRIORITIZED"`
   - Add comment explaining why it was prioritized
   - Inform user/log the prioritization decision
3. If YES: Proceed with work

**Dependencies**:
- Stories may be prioritized in parallel, but workers MUST honor `dependencies` and `blocks`.
- If any dependency story is not DONE, mark the story BLOCKED and notify the orchestrator.

**Who can prioritize?**
- Humans (explicit request)
- Orchestrator agents (with clear justification)
- Planning agents (for dependency chains)

**Never prioritize without reason** - document why in a comment.

### Rule 3: Activity Logging (REQUIRED)

**All work must be logged via ActivityLogger:**

```java
// At work start
activityLogger.logSuccess(
    "agent-role",
    "work_started",
    storyId.getValue(),
    Map.of("task", "implementation", "agent", "orchestrator")
);

// During work (major milestones)
activityLogger.logSuccess(
    "agent-role",
    "milestone_reached",
    storyId.getValue(),
    Map.of("milestone", "tests_passing", "count", 385)
);

// At completion
activityLogger.logSuccess(
    "agent-role",
    "work_completed",
    storyId.getValue(),
    Map.of(
        "artifactsCreated", 2,
        "testsAdded", 13,
        "totalTests", 385
    )
);
```

**Standard activity types:**
- `work_started` - Beginning implementation
- `milestone_reached` - Significant progress
- `blocked` - Encountered blocker
- `blocker_resolved` - Blocker cleared
- `work_completed` - Finished implementation

### Rule 4: Story Comments (REQUIRED)

**Document your work via comments:**

```json
{
  "comments": [
    {
      "type": "NOTE",
      "text": "Starting implementation of OrchestrationServiceImpl facade",
      "author": "orchestrator",
      "timestamp": "2026-04-03T08:00:00Z"
    },
    {
      "type": "NOTE",
      "text": "Decision: Using manual node chaining for now. StateGraph integration deferred to STORY-008B.",
      "author": "orchestrator",
      "timestamp": "2026-04-03T10:00:00Z"
    },
    {
      "type": "NOTE",
      "text": "✅ Implementation complete. Created OrchestrationServiceImpl (389 lines) with 13 unit tests. All 385 tests passing.",
      "author": "orchestrator",
      "timestamp": "2026-04-03T12:00:00Z"
    }
  ]
}
```

**Comment types:**
- `NOTE` - Progress updates, decisions, summaries
- `QUESTION` - Blockers, need clarification
- `ANSWER` - Resolution to questions
- `SYSTEM` - Automated system notifications

**When to add comments:**
- At work start
- When making architectural decisions
- When encountering blockers
- When completing work

### Rule 5: Completion and Approval (REQUIRED)

**When finishing implementation (implementer):**

```json
{
  "workflowState": "AWAITING-APPROVAL",
  "completedAt": "2026-04-03T12:00:00Z",  // ← Add timestamp
  "comments": [
    {
      "type": "NOTE",
      "text": "✅ [Summary of what was accomplished]",
      "author": "agent-role",
      "timestamp": "2026-04-03T12:00:00Z"
    }
  ]
}
```

**Approval checklist:**
- [ ] All acceptance criteria met
- [ ] Tests passing
- [ ] `workflowState` updated to `"DONE"`
- [ ] `completedAt` timestamp added
- [ ] Final summary comment added
- [ ] Completion activity logged

---

## Agent Responsibilities

### Orchestrator Agent

**Primary Responsibilities:**
1. Create new stories in BACKLOG
2. Make prioritization decisions
3. Coordinate work across agents
4. Ensure workflow rules are followed
5. **Request human approval** for stories in `AWAITING_APPROVAL` before marking `DONE`

**Before Creating Stories:**
```java
// Always create in BACKLOG
Story story = storyService.createStory(
    new StoryId("STORY-XXX"),
    "Title",
    "Summary"
);
// Story starts with prioritizationState = BACKLOG (default)
```

**Before Assigning Work:**
```java
// Check and update prioritization
if (story.getPrioritizationState() == PrioritizationState.BACKLOG) {
    story.setPrioritizationState(PrioritizationState.PRIORITIZED);
    storyRepository.save(story);

    story.addComment(new Comment(
        CommentType.NOTE,
        "Prioritized for implementation - dependency of STORY-XXX",
        "orchestrator"
    ));
}
```

### Implementation Agent

**Primary Responsibilities:**
1. Verify story is PRIORITIZED before starting
2. Log work activities
3. Add comments documenting progress
4. **Set AWAITING_APPROVAL when work is complete**

**Workflow:**
```java
// 1. Verify prioritization
Story story = storyRepository.findById(storyId).orElseThrow();
if (story.getPrioritizationState() != PrioritizationState.PRIORITIZED) {
    throw new IllegalStateException(
        "Cannot work on BACKLOG story. Must be PRIORITIZED first."
    );
}

// 2. Log start
activityLogger.logSuccess("implementer", "work_started", storyId, ...);

// 3. Add initial comment
story.addComment(new Comment(
    CommentType.NOTE,
    "Starting implementation",
    "implementer"
));

// 4. DO THE WORK
// ...

// 5. Add final comment
story.addComment(new Comment(
    CommentType.NOTE,
    "✅ Implementation complete. [details]",
    "implementer"
));

// 6. Mark DONE
story.setWorkflowState(WorkflowState.DONE);
story.setCompletedAt(Instant.now());
storyRepository.save(story);

// 7. Log completion
activityLogger.logSuccess("implementer", "work_completed", storyId, ...);
```

### Tester Agent

**Primary Responsibilities:**
1. Verify acceptance criteria met
2. Run tests and report results
3. Document test coverage

**Workflow:**
```java
// Add test results as comment
story.addComment(new Comment(
    CommentType.NOTE,
    "✅ All acceptance criteria verified. 385 tests passing. Coverage: 92%",
    "tester"
));
```

---

## Code Examples

### Example 1: Creating a Story (Orchestrator)

```java
// Create story in BACKLOG
Story story = new Story(
    new StoryId("STORY-015"),
    "Add LLM Prompt Templates",
    "Implement prompt template service for LLM operations",
    WorkflowState.TODO,
    PrioritizationState.BACKLOG  // ← Always BACKLOG
);

story.setDescription("Create PromptTemplateService that builds prompts...");
story.addAcceptanceCriterion("PromptTemplateService builds prompts from context");
story.addAcceptanceCriterion("Separate templates for RESEARCH, DESIGN, etc.");

storyRepository.save(story);

// Log creation
activityLogger.logSuccess(
    "orchestrator",
    "story_created",
    "STORY-015",
    Map.of("state", "BACKLOG", "effort", "2 days")
);
```

### Example 2: Prioritizing and Starting Work

```java
// Read story
Story story = storyRepository.findById(new StoryId("STORY-015")).orElseThrow();

// Check if needs prioritization
if (story.getPrioritizationState() == PrioritizationState.BACKLOG) {
    // Move to PRIORITIZED
    story.setPrioritizationState(PrioritizationState.PRIORITIZED);

    // Document why
    story.addComment(new Comment(
        CommentType.NOTE,
        "Prioritized - blocking STORY-011 (LLM Nodes implementation)",
        "orchestrator"
    ));

    storyRepository.save(story);

    // Inform user
    System.out.println("✅ Moved STORY-015 from BACKLOG to PRIORITIZED");
}

// Now safe to start work
story.setWorkflowState(WorkflowState.IN_PROGRESS);
story.addComment(new Comment(
    CommentType.NOTE,
    "Starting implementation of PromptTemplateService",
    "implementer"
));

activityLogger.logSuccess("implementer", "work_started", "STORY-015", Map.of());
storyRepository.save(story);
```

### Example 3: Handling Blockers

```java
// Encountered blocker during work
story.setWorkflowState(WorkflowState.BLOCKED);

story.addComment(new Comment(
    CommentType.QUESTION,
    "Blocked: Should we use Mustache or custom template engine?",
    "implementer"
));

activityLogger.logSuccess(
    "implementer",
    "blocked",
    "STORY-015",
    Map.of("reason", "template_engine_choice")
);

storyRepository.save(story);

// Later: Blocker resolved
story.setWorkflowState(WorkflowState.IN_PROGRESS);

story.addComment(new Comment(
    CommentType.ANSWER,
    "Decision: Using String.format() for simplicity. No external dependency needed.",
    "orchestrator"
));

activityLogger.logSuccess(
    "implementer",
    "blocker_resolved",
    "STORY-015",
    Map.of("decision", "string_format")
);

storyRepository.save(story);
```

### Example 4: Completing Work (Implementation → Approval)

```java
// All work done
story.setWorkflowState(WorkflowState.DONE);
story.setCompletedAt(Instant.now());

story.addComment(new Comment(
    CommentType.NOTE,
    "✅ Implementation complete:\n" +
    "- Created PromptTemplateService with 4 template methods\n" +
    "- Added 15 unit tests (all passing)\n" +
    "- Integrated with OrchestrationContext\n" +
    "- Coverage: 95%",
    "implementer"
));

activityLogger.logSuccess(
    "implementer",
    "work_completed",
    "STORY-015",
    Map.of(
        "testsAdded", 15,
        "coverage", "95%"
    )
);

storyRepository.save(story);
```

---

## Common Patterns

### Pattern 1: Dependency Chain Prioritization

When Story B depends on Story A:

```java
// Before starting Story B, check if Story A is done
Story storyA = storyRepository.findById(new StoryId("STORY-008A")).orElseThrow();

if (storyA.getWorkflowState() != WorkflowState.DONE) {
    // Block Story B
    storyB.setWorkflowState(WorkflowState.BLOCKED);
    storyB.addComment(new Comment(
        CommentType.NOTE,
        "Blocked by STORY-008A (not yet complete)",
        "orchestrator"
    ));
} else {
    // Story A done, can prioritize Story B
    storyB.setPrioritizationState(PrioritizationState.PRIORITIZED);
    storyB.addComment(new Comment(
        CommentType.NOTE,
        "Dependency STORY-008A complete - ready for work",
        "orchestrator"
    ));
}
```

### Pattern 2: Batch Prioritization

When prioritizing multiple related stories:

```java
List<String> storyIds = List.of("STORY-008A", "STORY-008B", "STORY-008C");

for (String id : storyIds) {
    Story story = storyRepository.findById(new StoryId(id)).orElseThrow();

    if (story.getPrioritizationState() == PrioritizationState.BACKLOG) {
        story.setPrioritizationState(PrioritizationState.PRIORITIZED);
        story.addComment(new Comment(
            CommentType.NOTE,
            "Prioritized as part of langgraph4j integration milestone",
            "orchestrator"
        ));
        storyRepository.save(story);
    }
}
```

### Pattern 3: Progressive Documentation

Document as you go:

```java
// Initial comment
story.addComment(new Comment(CommentType.NOTE, "Starting work", "agent"));

// Decision points
story.addComment(new Comment(
    CommentType.NOTE,
    "Decision: Using approach X because [reason]",
    "agent"
));

// Milestones
story.addComment(new Comment(
    CommentType.NOTE,
    "✓ Milestone: All tests passing (385 total)",
    "agent"
));

// Completion
story.addComment(new Comment(
    CommentType.NOTE,
    "✅ Complete: [summary]",
    "agent"
));
```

---

## Troubleshooting

### Problem: "Cannot work on BACKLOG story"

**Error**: Trying to implement a story still in BACKLOG state.

**Solution**:
```java
story.setPrioritizationState(PrioritizationState.PRIORITIZED);
story.addComment(new Comment(
    CommentType.NOTE,
    "Prioritized because [reason]",
    "agent-role"
));
storyRepository.save(story);
```

### Problem: "Cannot transition from TODO to DONE"

**Error**: Trying to skip IN_PROGRESS state.

**Solution**: Follow proper state progression:
```java
story.setWorkflowState(WorkflowState.IN_PROGRESS);
storyRepository.save(story);
// ... do work ...
story.setWorkflowState(WorkflowState.DONE);
storyRepository.save(story);
```

### Problem: "Missing completedAt timestamp"

**Error**: Story marked DONE but no completion timestamp.

**Solution**:
```java
story.setWorkflowState(WorkflowState.DONE);
story.setCompletedAt(Instant.now());  // ← Don't forget this
storyRepository.save(story);
```

### Problem: "No activity logs"

**Error**: Work completed but no logged activities.

**Solution**: Log activities throughout:
```java
// At start
activityLogger.logSuccess("agent", "work_started", storyId, ...);

// During work
activityLogger.logSuccess("agent", "milestone_reached", storyId, ...);

// At end
activityLogger.logSuccess("agent", "work_completed", storyId, ...);
```

---

## Quick Reference Card

```
STORY LIFECYCLE QUICK REFERENCE

Creation:
  ✓ Always create in BACKLOG
  ✓ Add acceptance criteria
  ✓ Estimate effort
  ✓ Log creation activity

Before Work:
  ✓ Check: Is story PRIORITIZED?
  ✓ If BACKLOG → Move to PRIORITIZED
  ✓ Add comment explaining prioritization
  ✓ Set workflowState to IN_PROGRESS
  ✓ Log work_started

During Work:
  ✓ Add comments for decisions
  ✓ Log major milestones
  ✓ Use QUESTION comments if blocked

After Work:
  ✓ Set workflowState to AWAITING_APPROVAL
  ✓ Add final summary comment + test results
  ✓ Log work_completed
  ✓ Do NOT move directories

Approval:
  ✓ Orchestrator requests human approval
  ✓ Human approves explicitly
  ✓ Orchestrator sets workflowState to DONE
  ✓ Add completedAt timestamp
  ✓ Move directory to done/
  ✓ Log state_transition

States:
  BACKLOG → PRIORITIZED (prioritization decision)
  TODO → IN_PROGRESS → AWAITING_APPROVAL → DONE (execution flow)

Never:
  ✗ Work on BACKLOG stories
  ✗ Skip IN_PROGRESS state
  ✗ Mark DONE without human approval
  ✗ Move directories without orchestrator approval
```

---

## Related Documentation

- **Architecture**: `etc/docs/ARCHITECTURE.md`
- **Testing**: `etc/docs/TESTING.md`
- **Domain Model**: `src/main/java/.../domain/model/`
- **Agile Worker Skill**: `.claude/skills/agile-worker/prompt.md`

---

**Version History**:
- 1.0 (2026-04-03): Initial version with prioritization workflow

---

## Mode Switching Configuration

Use `orchestration.execution-mode` to switch node implementations:
- `template` uses template nodes (deterministic, no LLM calls)
- `llm` uses LLM nodes (LLMChatClient-backed)

Example:
```yaml
orchestration:
  execution-mode: template
```

Switching modes does not require code changes; only configuration updates.
