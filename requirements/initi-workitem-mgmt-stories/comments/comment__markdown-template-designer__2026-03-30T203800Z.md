---
comment_type: artifact_reference
agent: markdown-template-designer
timestamp: 2026-03-30T20:38:00Z
related_story: STORY-006
---

## STORY-006: Markdown Template Creation - Complete

Created complete markdown templates with YAML frontmatter for stories, tasks, and artifacts.

**Artifacts Created**:
1. `templates/story-template.md`
2. `templates/task-template.md`
3. `templates/artifact-template.md`

### Story Template
**Fields**:
- ✅ All required fields (id, type, title, state, prioritization, author, summary, acceptance_criteria)
- ✅ All recommended optional fields (priority, parent_epic, timestamps, tags, references, artifact_path, task_count)
- ✅ Body sections (Business Context, Dependencies, Implementation Notes, Approval Notes, Artifact Summary, Promotion Decisions)

**Format**: YAML frontmatter + Markdown body

### Task Template
**Fields**:
- ✅ All required fields (id, type, title, parent_story, state, author, objective, completion_criteria)
- ✅ All recommended optional fields (timestamps, dependencies, references, artifact_path)
- ✅ Body sections (Technical Details, Implementation Approach, Testing Approach, Completion Notes)

**Format**: YAML frontmatter + Markdown body

### Artifact Template
**Fields**:
- ✅ All required fields (artifact_type, agent, timestamp, related_story, summary)
- ✅ All optional fields (related_task, status, tags)
- ✅ Body sections (Objective, Content, Conclusions, Next Steps)

**Format**: YAML frontmatter + Markdown body

**Template Features**:
- Placeholder values with XXX markers
- Helpful inline comments
- Example structures
- All fields documented in frontmatter
- Consistent formatting

**Templates Location**: `/templates/` directory

**Ready for**:
- Workspace initialization
- Story/task creation in implementation phase
- Template-based generators