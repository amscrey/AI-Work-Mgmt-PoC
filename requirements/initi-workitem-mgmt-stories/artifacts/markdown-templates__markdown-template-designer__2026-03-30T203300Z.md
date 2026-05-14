# Markdown Templates

**Artifact Type**: Template Collection
**Role**: markdown-template-designer
**Timestamp**: 2026-03-30T20:33:00Z
**Related Story**: STORY-006
**Status**: Complete

---

## Overview

Complete markdown template files with YAML frontmatter for stories, tasks, comments, and artifacts. Three variants for each: minimal, standard, and comprehensive.

---

## Template Files Created

1. `story-template-minimal.md`
2. `story-template-standard.md`
3. `story-template-comprehensive.md`
4. `task-template-minimal.md`
5. `task-template-standard.md`
6. `task-template-comprehensive.md`
7. `comment-template.md`
8. `artifact-template.md`

---

## Story Template (Standard)

**File**: `templates/story-template-standard.md`

```markdown
---
id: STORY-XXX
type: story
title: <Human-Readable Title>
state: todo
prioritization: backlog
author: <author-username>
summary: <One-sentence description of what this story accomplishes>
acceptance_criteria:
  - <Criterion 1: Specific, testable condition for completion>
  - <Criterion 2: Another completion condition>
  - <Criterion 3: Another completion condition>
priority: medium
created_at: <YYYY-MM-DDTHH:MM:SSZ>
updated_at: <YYYY-MM-DDTHH:MM:SSZ>
tags: []
references: []
---

## Business Context

<Describe the business need or problem this story addresses>

## Dependencies

<List any dependencies on other stories, external systems, or prerequisites>

## Implementation Notes

<Technical notes, approaches, or considerations for implementation>

## Artifact Summary

<Summary of key artifacts created during this story>

## Approval Notes

<Notes from human approval/rejection of this story>
```

---

## Task Template (Standard)

**File**: `templates/task-template-standard.md`

```markdown
---
id: TASK-XXX
type: task
title: <Human-Readable Title>
parent_story: STORY-XXX
state: pending
author: <author-username>
objective: <What this task accomplishes>
completion_criteria:
  - <Criterion 1: How to know this task is done>
  - <Criterion 2: Another completion condition>
created_at: <YYYY-MM-DDTHH:MM:SSZ>
updated_at: <YYYY-MM-DDTHH:MM:SSZ>
dependencies: []
references: []
---

## Technical Details

<Technical implementation details specific to this task>

## Implementation Approach

<Approach or strategy for completing this task>

## Testing Approach

<How this task will be tested>

## Completion Notes

<Notes upon completion of this task>
```

---

## Comment Template

**File**: `templates/comment-template.md`

```markdown
---
comment_type: <status_update | question | suggestion | artifact_reference>
agent: <role-name>
timestamp: <YYYY-MM-DDTHH:MM:SSZ>
related_story: STORY-XXX
related_task: TASK-XXX  # Optional
---

## <Comment Title>

<Comment body goes here>

<If artifact_reference type, include:>
**Artifacts**:
- artifact-name__role__timestamp.ext
- another-artifact__role__timestamp.ext

<If question type, include:>
**Question**: <The specific question>

<If suggestion type, include:>
**Suggestion**: <The proposed change or improvement>
```

---

## Artifact Template

**File**: `templates/artifact-template.md`

```markdown
---
artifact_type: <research | analysis | suggestion | mockup | code | test_report | review | documentation>
agent: <role-name>
timestamp: <YYYY-MM-DDTHH:MM:SSZ>
related_story: STORY-XXX
related_task: TASK-XXX  # Optional
summary: <One-sentence description of this artifact>
status: draft
tags: []
---

# <Artifact Title>

## Objective

<What this artifact aims to accomplish or demonstrate>

## <Content Section Title>

<Main content of the artifact>

## Conclusions

<Key takeaways or conclusions from this artifact>

## Next Steps

<Recommended follow-up actions>
```

---

## Field Definitions

### Story Fields

| Field | Required | Type | Description | Example |
|-------|----------|------|-------------|---------|
| `id` | ✅ | String | Story identifier | `STORY-123-dashboard-ui` |
| `type` | ✅ | Enum | Must be "story" | `story` |
| `title` | ✅ | String | Human-readable title | `Dashboard UI Redesign` |
| `state` | ✅ | Enum | Workflow state | `todo`, `in-progress`, `awaiting-approval`, `done` |
| `prioritization` | ✅ | Enum | Prioritization state | `backlog`, `prioritized` |
| `author` | ✅ | String | Human curator | `john.doe` |
| `summary` | ✅ | String | One-sentence description | `Build responsive dashboard interface` |
| `acceptance_criteria` | ✅ | List | Completion criteria | List of testable conditions |
| `priority` | Optional | String | Priority level | `high`, `medium`, `low` |
| `parent_epic` | Optional | String | Epic reference | `EPIC-005` |
| `created_at` | Optional | ISO 8601 | Creation timestamp | `2026-03-30T10:00:00Z` |
| `updated_at` | Optional | ISO 8601 | Last update timestamp | `2026-03-30T14:30:00Z` |
| `tags` | Optional | List | Classification tags | `[frontend, ui, dashboard]` |
| `references` | Optional | List | External references | URLs, doc links |
| `artifact_path` | Optional | Path | Path to artifacts | `./artifacts` |
| `task_count` | Optional | Integer | Number of tasks | `3` |

### Task Fields

| Field | Required | Type | Description | Example |
|-------|----------|------|-------------|---------|
| `id` | ✅ | String | Task identifier | `TASK-001-chart-component` |
| `type` | ✅ | Enum | Must be "task" | `task` |
| `title` | ✅ | String | Human-readable title | `Implement Chart Component` |
| `parent_story` | ✅ | String | Parent story ID | `STORY-123` |
| `state` | ✅ | Enum | Task state | `pending`, `in-progress`, `completed` |
| `author` | ✅ | String | Human curator | `john.doe` |
| `objective` | ✅ | String | What this accomplishes | `Create reusable chart component` |
| `completion_criteria` | ✅ | List | How to know it's done | List of conditions |
| `created_at` | Optional | ISO 8601 | Creation timestamp | `2026-03-30T11:00:00Z` |
| `updated_at` | Optional | ISO 8601 | Last update timestamp | `2026-03-30T11:30:00Z` |
| `dependencies` | Optional | List | Task dependencies | `[TASK-002]` |
| `references` | Optional | List | External references | URLs, doc links |
| `artifact_path` | Optional | Path | Path to artifacts | `./artifacts` |

---

## Template Variants

### Minimal Templates
- Include ONLY required fields
- Use for quick story/task creation
- No optional sections in body

### Standard Templates (Recommended)
- Include required fields + common optional fields
- Include standard body sections
- Recommended for most use cases

### Comprehensive Templates
- Include all possible fields
- Include all body sections with examples
- Use for complex stories or reference

---

## Example Populated Story

```markdown
---
id: STORY-123-dashboard-ui
type: story
title: Dashboard UI Redesign
state: in-progress
prioritization: prioritized
author: john.doe
summary: Build responsive dashboard showing user activity metrics
acceptance_criteria:
  - Dashboard displays last 30 days of activity
  - Charts are interactive and responsive
  - Loading states are implemented
  - Error states are handled gracefully
priority: high
parent_epic: EPIC-005
created_at: 2026-03-30T10:00:00Z
updated_at: 2026-03-30T14:30:00Z
tags: [frontend, ui, dashboard]
artifact_path: ./artifacts
task_count: 3
---

## Business Context

Users need better visibility into their team's activity. Current dashboard
is static and doesn't provide real-time insights. This redesign will
provide interactive charts and responsive layout.

## Dependencies

- API endpoint for activity data (STORY-120)
- Chart library selection (STORY-115)

## Implementation Notes

Using React with D3.js for charts. Mobile-first responsive design.
Consider performance for large datasets.

## Artifact Summary

- Research: chart-library-comparison__researcher__2026-03-30T120000Z.md
- Design: dashboard-mockup__designer__2026-03-30T130000Z.png
- Code: Dashboard__coder__2026-03-30T140000Z.jsx

## Approval Notes

Approved by product team on 2026-03-30. Requested mobile view adjustment.
```

---

## Validation Rules

### YAML Frontmatter
- Must be valid YAML syntax
- Must be delimited by `---` at start and end
- All required fields must be present and non-empty
- Field values must match expected types
- Enum fields must use allowed values
- Timestamps must be ISO 8601 format
- IDs must match format patterns

### Body Content
- Required sections must be present (if defined)
- Section headings must use proper markdown (`##`, `###`)
- Lists must use consistent bullet style

---

## Usage Guide

1. **Creating a Story**: Copy `story-template-standard.md`, replace placeholders
2. **Creating a Task**: Copy `task-template-standard.md`, set `parent_story`
3. **Adding Comments**: Use `comment-template.md` for agent updates
4. **Creating Artifacts**: Use `artifact-template.md` for artifact markdown files

---

## Acceptance Criteria Verification

- ✅ All required fields defined with types and constraints
- ✅ Optional fields documented with usage guidance
- ✅ Frontmatter examples provided with realistic data
- ✅ Body sections defined with clear purposes
- ✅ Templates include helpful placeholder text
- ✅ Validation rules documented
- ✅ Both minimal and comprehensive variants created
- ✅ Example populated templates provided

---

**Templates are production-ready and can be used immediately.**
