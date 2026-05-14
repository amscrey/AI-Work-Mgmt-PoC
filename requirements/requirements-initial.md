lets # Claude Code Implementation Brief

Build a generic Java Spring Boot project that manages AI-orchestrated work items in an **external filesystem workspace**.

The external workspace is the system of record for work item management.
The core project must not store managed project work items internally.

**Document Version**: 1.1
**Last Updated**: 2026-03-30
**Status**: Initial requirements with clarifications

---

## Important Terminology

### Role vs Agent vs LLM

| Term | Definition | Example |
|------|------------|---------|
| **LLM** | Underlying AI model | Claude Sonnet 4.5, GPT-4, Gemini |
| **Agent** | Instance of an LLM performing work | A Claude session, a Copilot instance |
| **Role** | Responsibility/function an agent assumes | orchestrator, tester, designer |

**Critical Rule**: All file naming and logging uses **ROLE names**, not LLM names.

Examples:
- ✅ `api-research__researcher__2026-03-30T140512Z.md` (role: researcher)
- ✅ `comment__orchestrator__2026-03-30T141230Z.md` (role: orchestrator)
- ❌ `diagram__claude__2026-03-30T142015Z.svg` (wrong: uses LLM name)

### Defined Roles

The system supports the following core roles (see `.agent/roles/` for full definitions):
- **orchestrator**: Workflow coordination, work item management (only role that can modify story.md)
- **designer**: UX/UI design, mockups
- **logician**: Game rules, algorithms, Java code
- **creative-writer**: Ad copy, fiction, creative content
- **artist**: Image generation, visual assets
- **tester**: Testing, QA, bug reporting
- **reviewer**: Code/design review, quality assessment
- **researcher**: Requirements, analysis, investigation

---

## 1. Objective

Implement a generic orchestration platform that:

- manages work items using markdown files in an external filesystem workspace
- uses a story-centric workflow similar to Jira/Scrum
- treats stories as the main unit of delivery and state movement
- allows optional tasks under stories
- stores AI outputs as append-only artifacts under the relevant story or task
- supports multiple agents writing artifacts in parallel without collisions
- separates authoritative work item content from non-authoritative AI outputs
- supports promotion of approved content into a shared reference area
- keeps agent logs and memory separate from work item artifacts

---

## 2. Core architecture

There are 2 separate concerns:

### A. Core project
The Java Spring Boot application being built.

Responsibilities:
- define workflow logic
- manage external workspaces
- validate structure and metadata
- create/update/move work items
- create artifacts
- enforce rules
- provide services/APIs for orchestration

### B. Managed workspace
A filesystem root external to the core project repository.

Responsibilities:
- store work item directories
- store markdown files
- store artifacts
- store reference content
- store agent logs and memory

The managed workspace is authoritative for work item content.

---

## 3. External workspace structure

The managed workspace must use this exact top-level structure:

```text
<workspace-root>/
  work-items/
    backlog/
    prioritized/
    todo/
    in-progress/
    awaiting-approval/
    done/

  reference/
    technical/
    human-facing/
    media/

  agents/
    logs/
    memory/

  templates/
```

**Important**: `backlog/` and `prioritized/` represent **prioritization state** (orthogonal concept). The workflow states are `todo/`, `in-progress/`, `awaiting-approval/`, `done/`. These are separate concerns that may need to be tracked independently in metadata.

The application must be able to:
- create this structure
- validate this structure
- operate against this structure

---

## 4. Work item model

### Stories
- stories are the primary unit of work
- each story is a directory
- the story directory moves between lifecycle state folders
- each story must contain `story.md`
- each story must contain `artifacts/`
- each story may optionally contain `tasks/`

### Tasks
- tasks are optional
- tasks only exist under a story
- each task is a directory under `tasks/`
- each task must contain `task.md`
- each task may optionally contain `artifacts/`
- tasks do not move independently between top-level lifecycle directories

---

## 5. Directory naming rules

### Story directory format

```text
STORY-<id>-<slug>/
```

Examples:

```text
STORY-123-dashboard-ui/
STORY-014-login-endpoint/
```

### Task directory format

```text
TASK-<id>-<slug>/
```

Examples:

```text
TASK-001-chart-component/
TASK-002-add-validation/
```

Rules:
- prefixes must be uppercase: `STORY`, `TASK`
- slug must be lowercase kebab-case
- directory names must remain stable after creation

---

## 6. Lifecycle state model

Stories have two orthogonal state dimensions:

### Prioritization State (Backlog Management)
Stories can be in one of:
- `backlog`: captured but not yet prioritized
- `prioritized`: prioritized and sequenced

### Workflow State (Execution)
Stories can be in one of:
- `todo`: ready for execution
- `in-progress`: actively being worked
- `awaiting-approval`: work completed in draft/proposed form and waiting for approval
- `done`: completed and dispositioned

**Note**: A story can be in `backlog` OR `prioritized` (prioritization dimension) AND simultaneously be in one of the workflow states. The directory structure and metadata model must account for these orthogonal concerns.

---

## 7. State authority rule

For stories:
- the authoritative state is the parent lifecycle directory
- `story.md` must also contain a `state` field matching the directory location

Example:
- if a story is under `work-items/in-progress/`, then `story.md` must contain `state: in-progress`

The application must validate mismatches.

For tasks:
- task state is stored inside `task.md`
- tasks inherit context from the parent story
- tasks do not move between top-level lifecycle directories

---

## 8. Required story structure

Each story directory must contain:

```text
STORY-<id>-<slug>/
  story.md
  artifacts/
  comments/
```

Optional:

```text
  tasks/
```

### Story Comments
- The `comments/` directory contains agent-written comment files for concurrent updates
- Comment files are append-only and timestamped like artifacts
- Format: `comment__<agent>__<yyyy-mm-ddThhmmssZ>.md`
- Agents write comments here instead of directly modifying `story.md`
- Comments are referenced in agent logs
- Comments may be promoted into `story.md` by the authorized story manager agent

Example:

```text
work-items/in-progress/STORY-123-dashboard-ui/
  story.md
  artifacts/
  comments/
  tasks/
```

---

## 9. Required task structure

Each task directory must contain:

```text
TASK-<id>-<slug>/
  task.md
```

Optional:

```text
  artifacts/
```

Tasks must exist only inside:

```text
<story-dir>/tasks/
```

---

## 10. Authority model

### Authoritative files
- `story.md`
- `task.md`
- approved content in `reference/`

### Non-authoritative files
- all files under `artifacts/`
- all files under `agents/logs/`
- all files under `agents/memory/`

Artifacts are evidence, suggestions, drafts, or analysis.
Artifacts are not the final accepted truth.

---

## 11. Artifact model

Artifacts represent outputs such as:
- research (markdown)
- analysis (markdown)
- suggestions (markdown)
- draft designs (markdown, images, diagrams)
- generated media (images, videos, PDFs)
- implementation notes (markdown, code files)
- test notes (markdown, logs)
- story/task-specific agent traces (markdown, logs)

**Artifacts are not limited to markdown files** - they can be any file type (images, PDFs, videos, etc.).

Artifacts must live under either:

```text
<story-dir>/artifacts/
```

or

```text
<task-dir>/artifacts/
```

Optional subfolders under `artifacts/` are allowed, such as:
- `assets/`
- `images/`
- `notes/`
- `reports/`

All artifact creation must be:
- Referenced in agent logs (`agents/logs/`)
- Recorded in story comments (`<story-dir>/comments/`) when agents work on stories

---

## 12. Artifact naming rule

Each artifact filename must use this format:

```text
<descriptive-name>__<agent-name>__<yyyy-mm-ddThhmmssZ>.<ext>
```

The filename MUST contain three elements separated by double underscores:
1. **Descriptive name**: Human-readable description (kebab-case, e.g., "api-research", "dashboard-mockup")
2. **Agent name**: Identity of the agent that created it (e.g., "claude", "copilot", "qa-bot")
3. **Timestamp**: ISO 8601 UTC timestamp (format: yyyy-mm-ddThhmmssZ)

Examples:

```text
api-research__copilot__2026-03-30T214512Z.md
refactoring-suggestions__claude__2026-03-30T220102Z.md
dashboard-mockup__copilot__2026-03-30T215455Z.png
integration-test-report__qa-bot__2026-03-30T223015Z.md
architecture-diagram__claude__2026-03-30T220045Z.svg
user-flow__designer-bot__2026-03-30T221530Z.pdf
```

Rules:
- never overwrite existing artifacts
- artifacts are append-only
- artifacts are immutable after creation
- one artifact file corresponds to one output event or run
- include agent identity and UTC timestamp in filename
- descriptive name must be meaningful and kebab-case
- if needed, add an extra suffix for uniqueness (e.g., `-v2`)
- all artifact types supported (markdown, images, PDFs, videos, code files, etc.)

---

## 13. Parallel multi-agent rule

Multiple agents must be able to work on the same story or task at the same time.

Required behavior:
- agents write new artifact files only
- agents do not overwrite existing artifact files
- agents do not edit other agents' artifacts
- agents may read existing artifacts
- authoritative files must be updated through controlled application logic

---

## 14. Reference area

Reference content lives here:

```text
reference/
  technical/
  human-facing/
  media/
```

Rules:
- contains only approved, stable, reusable content
- is authoritative
- is not draft space
- may receive promoted content derived from stories/tasks
- stories may link to reference content
- reference content must stand on its own without requiring raw artifact files

---

## 15. Agents area

Agent-related operational data lives here:

```text
agents/
  logs/
  memory/
```

### `agents/logs/`

**Log File Naming**:
```
agents/logs/<role-name>__<yyyy-mm-dd>.log
```

Examples:
```
agents/logs/orchestrator__2026-03-30.log
agents/logs/tester__2026-03-30.log
agents/logs/designer__2026-03-30.log
```

**Log Format**: JSON Lines (one JSON object per line)

**Log Entry Structure**:
```json
{"timestamp":"2026-03-30T14:05:12Z","role":"orchestrator","activity":"story_created","story_id":"STORY-123","details":{"title":"Dashboard redesign","state":"todo","prioritization":"backlog"},"outcome":"success"}
```

**Required Fields**:
- `timestamp`: ISO 8601 UTC when activity occurred
- `role`: Role performing the activity (not LLM name)
- `activity`: Activity type (see below)
- `outcome`: `success` | `failure` | `partial`

**Optional Fields**:
- `story_id`: Story ID if applicable
- `task_id`: Task ID if applicable
- `details`: Activity-specific details (object)
- `error`: Error message if outcome=failure

**Standard Activity Types**:
- `story_created`, `story_updated`, `task_created`, `task_updated`
- `work_started`, `work_completed`
- `artifact_created`, `comment_created`
- `state_transition`, `prioritization_changed`
- `human_interaction`
- `role_delegated`
- `validation_performed`
- `error_occurred`

Use for:
- execution logs
- failure traces
- orchestration events
- diagnostics
- audit trail

### `agents/memory/`
Use for:
- long-lived agent memory
- learned constraints
- reusable patterns
- non-authoritative cross-workspace notes

Rules:
- no authoritative work item definitions here
- no story/task source-of-truth content here
- no approved reference documents here
- story/task-specific outputs belong under that story/task `artifacts/`

---

## 16. Terminology rule

Use `author`, never `owner`.

Meaning of `author`:
- the human responsible for curating and promoting content

Rules:
- agents are not authors
- do not introduce `owner` fields in templates or code unless explicitly needed for a different concept

---

## 17. Required `story.md` contract

Each `story.md` must include structured metadata with at least:

- `id`
- `type: story`
- `title` **(required)**
- `state` (workflow state: todo/in-progress/awaiting-approval/done)
- `prioritization` (backlog/prioritized)
- `author`
- `summary`
- `acceptance_criteria`

Recommended additional fields:
- `priority`
- `parent_epic`
- `created_at`
- `updated_at`
- `tags`
- `references`
- `artifact_path`
- `task_count`

The body may include:
- business context
- dependencies
- implementation notes
- approval notes
- artifact summary
- promotion decisions

**Story Modification Rules**:
- `story.md` should only be created and updated by one designated AI agent/role (the "story manager")
- Other agents provide updates via comment files in `<story-dir>/comments/`
- The story manager agent curates and incorporates approved comments into `story.md`

---

## 18. Required `task.md` contract

Each `task.md` must include structured metadata with at least:

- `id`
- `type: task`
- `title` **(required)**
- `parent_story`
- `state`
- `author`
- `objective`
- `completion_criteria`

Recommended additional fields:
- `created_at`
- `updated_at`
- `dependencies`
- `references`
- `artifact_path`

---

## 19. Artifact markdown contract

Artifact markdown files should include structured metadata with at least:

- `artifact_type`
- `agent`
- `timestamp`
- `related_story`
- `related_task` if applicable
- `summary`

Optional:
- `status`
- `tags`

Then include the content body.

---

## 20. Promotion rules

Promotion converts non-authoritative outputs into authoritative content.

Rules:
- agents write artifacts
- humans or controlled orchestration logic curate final content
- `story.md` and `task.md` may summarize approved conclusions
- reusable approved outputs may be promoted into `reference/`
- moving a story to `done/` should imply artifacts were reviewed and dispositioned

Artifact disposition should support:
- promoted
- summarized
- rejected
- informational-only

Reference content must not depend on raw artifact files to be understandable.

---

## 21. Epic support

Support epics as metadata references, but keep stories as the primary operational unit.

Rules:
- stories may include `parent_epic`
- epics may be added later as separate markdown files or directories
- do not make epics the moving lifecycle unit
- tasks remain nested under stories only

---

## 22. What to implement

Implement the system in phases.

### Phase 1: workspace contract
Implement:
- external workspace root configuration
- workspace structure creation
- workspace validation
- path resolution utilities
- naming convention validation

### Phase 2: story/task management
Implement:
- create story
- create task under story
- read story metadata
- update story metadata
- read task metadata
- update task metadata
- move story between state directories
- validate state consistency between directory and markdown metadata

### Phase 3: artifact management
Implement:
- create append-only artifact files
- validate artifact naming
- list artifacts by story/task
- support safe parallel artifact writes

### Phase 4: promotion/reference support
Implement:
- promote approved content into `reference/`
- record artifact disposition
- support `awaiting-approval` workflows

### Phase 5: AI orchestration support
Implement:
- agent-specific write policies
- logging to `agents/logs/`
- memory handling under `agents/memory/`
- orchestration coordination services

---

## 23. Claude Code behavioral rules

Claude Code may:
- create directories and markdown files according to this contract
- generate templates
- implement validators
- implement workspace services
- implement file movement logic
- implement artifact creation logic
- implement promotion support

Claude Code must not:
- overwrite artifact files
- place draft work in `reference/`
- create top-level tasks outside stories
- store managed work items inside the core project instead of the external workspace
- treat artifacts as authoritative truth

---

## 24. Acceptance criteria

The implementation is acceptable when it can:

1. initialize a valid external workspace
2. create a story directory with valid `story.md`, `artifacts/`, and `comments/`
3. create a task under a story with valid `task.md`
4. transition stories between all valid states: prioritize, move to backlog, update workflow status (todo/in-progress/awaiting-approval/done)
5. keep `story.md` state fields consistent with directory state
6. create append-only artifacts with the required naming convention (descriptive-name__agent__timestamp)
7. allow multiple agents to write artifacts to the same story/task without overwrites
8. distinguish authoritative vs non-authoritative content
9. promote approved content into `reference/`
10. keep agent logs and memory separate from work item artifacts
11. update story workflow state based on agent work (todo → in-progress → awaiting-approval)
12. update stories based on human input - including marking stories done that were awaiting-approval and approved by human

---

## 25. Non-negotiable constraints

- the managed workspace is external to the core project repository
- stories are the primary unit of work and movement
- tasks are optional and nested under stories
- `story.md` and `task.md` are authoritative
- artifacts are append-only and immutable
- every artifact filename includes agent identity and UTC timestamp
- `reference/` contains only approved stable content
- `agents/` is not a work item source-of-truth area
- use `author`, never `owner`
- the implementation must remain generic across managed projects

---

## 26. Initial implementation request

Start by implementing:

1. workspace configuration model
2. workspace initializer
3. workspace validator
4. story creation service
5. task creation service
6. story state transition service
7. artifact creation service
8. basic markdown template generation for:
   - `story.md`
   - `task.md`
   - artifact markdown

Prefer clean separation between:
- domain model
- filesystem services
- validation logic
- orchestration logic

Use implementation patterns appropriate for Java Spring Boot.

---
