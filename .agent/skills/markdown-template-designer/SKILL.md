---
name: markdown-template-designer
description: Design structured markdown templates with YAML frontmatter metadata for work items and artifacts. Use when creating story templates, task templates, artifact templates, or defining metadata contracts.
---

# Markdown Template Designer

Designs consistent, structured markdown templates with YAML frontmatter metadata for authoritative work items and non-authoritative artifacts.

## Input
- Template type (story, task, artifact, reference document)
- Required metadata fields
- Optional metadata fields
- Body section requirements
- Target audience (humans, AI agents, or both)
- Validation requirements

## Output
- Complete markdown template file
- Metadata schema documentation
- Field descriptions and constraints
- Example templates with realistic data
- Validation rules for template compliance

---

## Process

### Phase 1: Define Metadata Schema
1. Identify required fields (must be present and non-empty)
2. Identify optional fields (may be present)
3. Define field types (string, number, date, enum, list)
4. Define validation constraints (format, range, pattern)
5. Define field relationships (e.g., `parent_story` required if type is `task`)
6. Document field semantics and usage

### Phase 2: Design Frontmatter Structure
1. Use YAML frontmatter delimited by `---`
2. Order fields logically:
   - Identity fields first (id, type)
   - Classification fields (state, priority, tags)
   - Ownership fields (author)
   - Descriptive fields (title, summary)
   - Relational fields (parent_epic, dependencies)
   - Temporal fields (created_at, updated_at)
   - Technical fields (artifact_path, task_count)
3. Use consistent naming conventions:
   - snake_case for field names
   - ISO 8601 for timestamps
   - kebab-case for IDs and slugs
   - Enums in lowercase
4. Add inline comments for complex fields

### Phase 3: Design Body Sections
1. Define required sections (e.g., Acceptance Criteria, Context)
2. Define optional sections (e.g., Dependencies, Notes)
3. Use markdown headings for structure
4. Include placeholder text with instructions
5. Add examples where helpful
6. Keep sections focused and purposeful

### Phase 4: Create Template Variants
1. Create minimal template (required fields only)
2. Create standard template (required + common optional fields)
3. Create comprehensive template (all fields with examples)
4. Document when to use each variant

### Phase 5: Document Validation Rules
1. Define frontmatter validation (YAML parsing, required fields)
2. Define content validation (section presence, format)
3. Define cross-file validation (e.g., parent_story exists)
4. Define naming validation (filename matches ID)
5. Provide validation error messages

---

## Template Types

### Story Template
**Purpose**: Authoritative record of a deliverable unit of work

**Required Metadata**:
- `id`: Story identifier (e.g., "STORY-123")
- `type`: Must be "story"
- `state`: Lifecycle state matching directory location
- `author`: Human responsible for curation
- `summary`: One-sentence description
- `acceptance_criteria`: List of completion criteria

**Optional Metadata**:
- `title`: Human-readable title
- `priority`: Numeric or enum (high/medium/low)
- `parent_epic`: Epic identifier
- `created_at`: ISO 8601 timestamp
- `updated_at`: ISO 8601 timestamp
- `tags`: List of classification tags
- `references`: Related documents or links
- `artifact_path`: Relative path to artifacts directory
- `task_count`: Number of child tasks

**Body Sections**:
- Business Context
- Dependencies
- Implementation Notes
- Approval Notes
- Artifact Summary
- Promotion Decisions

---

### Task Template
**Purpose**: Authoritative record of work under a story

**Required Metadata**:
- `id`: Task identifier (e.g., "TASK-001")
- `type`: Must be "task"
- `parent_story`: Story identifier
- `state`: Task state (pending/in-progress/completed)
- `author`: Human responsible for curation
- `objective`: What this task accomplishes
- `completion_criteria`: How to know it's done

**Optional Metadata**:
- `title`: Human-readable title
- `created_at`: ISO 8601 timestamp
- `updated_at`: ISO 8601 timestamp
- `dependencies`: List of task IDs this depends on
- `references`: Related documents or links
- `artifact_path`: Relative path to artifacts directory

**Body Sections**:
- Technical Details
- Implementation Approach
- Testing Approach
- Completion Notes

---

### Artifact Template
**Purpose**: Non-authoritative AI output (research, analysis, suggestions)

**Required Metadata**:
- `artifact_type`: Type of artifact (research/analysis/suggestion/test-report)
- `agent`: Agent name that created this
- `timestamp`: ISO 8601 UTC timestamp
- `related_story`: Story ID this artifact belongs to
- `summary`: One-sentence description

**Optional Metadata**:
- `related_task`: Task ID if task-specific
- `status`: Draft/proposed/reviewed/rejected/promoted
- `tags`: Classification tags
- `promoted_to`: Path if promoted to reference

**Body Sections**:
- Objective
- Findings/Analysis/Suggestions (depends on artifact_type)
- Conclusions
- Next Steps

---

## Frontmatter Examples

### Story Example
```yaml
---
id: STORY-123
type: story
state: in-progress
author: john.doe
title: User Dashboard UI
summary: Build interactive dashboard showing user activity metrics
priority: high
parent_epic: EPIC-005
created_at: 2026-03-30T10:00:00Z
updated_at: 2026-03-30T14:30:00Z
tags: [frontend, ui, dashboard]
acceptance_criteria:
  - Dashboard displays last 30 days of activity
  - Charts are interactive and responsive
  - Loading states are implemented
  - Error states are handled gracefully
artifact_path: ./artifacts
task_count: 3
---
```

### Task Example
```yaml
---
id: TASK-001
type: task
parent_story: STORY-123
state: in-progress
author: john.doe
title: Implement chart component
objective: Create reusable chart component for activity visualization
completion_criteria:
  - Component accepts time-series data
  - Component is responsive
  - Component has unit tests
created_at: 2026-03-30T11:00:00Z
dependencies: []
artifact_path: ./artifacts
---
```

### Artifact Example
```yaml
---
artifact_type: research
agent: claude
timestamp: 2026-03-30T14:30:15Z
related_story: STORY-123
summary: Comparison of charting libraries for dashboard implementation
status: proposed
tags: [frontend, charting, research]
---
```

---

## Validation Rules

### Frontmatter Validation
1. Must be valid YAML syntax
2. Must be delimited by `---` at start and end
3. All required fields must be present and non-empty
4. Field values must match expected types
5. Enum fields must use allowed values
6. Timestamps must be ISO 8601 format
7. IDs must match format patterns

### Content Validation
1. Body must follow frontmatter
2. Required sections must be present (if defined)
3. Section headings must use proper markdown (`##`, `###`)
4. Lists must use consistent bullet style

### Cross-File Validation
1. `parent_story` must reference an existing story
2. `parent_epic` must reference an existing epic (if epics exist)
3. `state` must match parent directory for stories
4. `artifact_path` must exist if specified

---

## Completeness Checklist
- □ All required metadata fields defined with types and constraints?
- □ Optional fields documented with usage guidance?
- □ Frontmatter example provided with realistic data?
- □ Body sections defined with clear purposes?
- □ Template includes helpful placeholder text?
- □ Validation rules documented?
- □ Both minimal and comprehensive variants created?

## Rules
1. **ALWAYS** use YAML frontmatter delimited by `---`
2. **ALWAYS** use snake_case for metadata field names
3. **ALWAYS** use ISO 8601 format for timestamps
4. **ALWAYS** include `type` field to distinguish template types
5. **ALWAYS** document required vs optional fields
6. **ALWAYS** provide validation constraints for fields
7. **NEVER** use camelCase or PascalCase for field names
8. **NEVER** omit required fields from templates
9. **NEVER** use free-form dates (always ISO 8601)
10. **NEVER** create templates without example data