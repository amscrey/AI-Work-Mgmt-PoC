---
name: role-definer
description: Define workflow roles for AI agents in work management systems. Use when creating role definitions, mapping responsibilities to roles, defining role authorities, or establishing role-based file naming conventions.
---

# Role Definer

Meta-skill for defining workflow roles that AI agents can assume during work orchestration. Establishes clear boundaries between roles, responsibilities, authorities, and the skills each role uses.

## Input
- Role name and purpose
- Responsibilities and authority level
- Skills the role uses
- Interaction patterns with other roles
- File naming requirements

## Output
- Complete role directory with `ROLE.md`
- Placed in `.agent/roles/<role-name>/`
- Role-specific logging requirements
- Example session flows

---

## Process

### Phase 1: Interview (Understand the Role)

Ask these questions **one at a time**. Wait for each answer before continuing.

1. **What is this role's name?** (kebab-case, e.g., "orchestrator", "code-reviewer")
2. **What is this role's primary responsibility?** (1-2 sentences)
3. **What authority does this role have?** (Can it modify story.md? Create work items? Approve changes?)
4. **Which skills does this role use?** (List existing skills from `.agent/skills/`)
5. **Can this role work independently or does it need coordination?**
6. **What files does this role create?** (Artifacts, comments, logs, authoritative files)
7. **How does this role interact with humans?** (Direct instructions, approvals, reports?)
8. **How does this role interact with other roles?** (Delegates, coordinates, reports to?)

After all questions are answered, confirm with the user:
> "Here's what I understood: [summary]. Shall I create it?"

### Phase 2: Scaffold (Generate the Role)

Create the directory and `ROLE.md`:

```
.agent/roles/<role-name>/
└── ROLE.md              # Required
```

#### Frontmatter Rules

| Field | Required | Rules |
|-------|----------|-------|
| `name` | ✅ | kebab-case, 1-64 chars, lowercase only, must match directory name |
| `description` | ✅ | 1-1024 chars, describe primary responsibility and authority |

#### Body Structure

Follow this pattern for consistency:

```markdown
# Role Name

Brief description of the role.

## Responsibilities
- Primary responsibility 1
- Primary responsibility 2

## Authority
- **Can modify**: Which authoritative files
- **Can create**: Which file types
- **Can transition**: Which state changes
- **Can access**: Which workspace areas

## Skills Used
- `skill-name` - How this role uses it

## File Naming
- Artifacts: `<name>__<role-name>__<timestamp>.<ext>`
- Comments: `comment__<role-name>__<timestamp>.md`
- Logs: `agents/logs/<role-name>__<yyyy-mm-dd>.log`

## Interaction Patterns
- **With humans**: How this role interacts
- **With other roles**: Coordination patterns
- **With workspace**: Read/write patterns

## Logging Requirements
Must log:
- Activity 1
- Activity 2

## Example Session
1. Trigger event
2. Role action
3. Result
```

### Phase 3: Validate (Completeness Checklist)

Before delivering, verify:

- □ `name` field is kebab-case and matches directory name?
- □ `description` field clearly states responsibility and authority?
- □ Responsibilities are specific and measurable?
- □ Authority boundaries are explicit (what role CAN and CANNOT do)?
- □ Skills are listed with existing skill names?
- □ File naming follows convention: `<desc>__<role>__<timestamp>`?
- □ Interaction patterns cover humans, other roles, and workspace?
- □ Logging requirements are specific?
- □ Example session demonstrates typical workflow?

### Phase 4: Role Authority Matrix (Optional)

If defining multiple roles, create a role authority matrix:

```markdown
# Role Authority Matrix

| Role | Modify story.md | Create Stories | State Transitions | Artifacts | Comments |
|------|----------------|----------------|-------------------|-----------|----------|
| orchestrator | ✅ Yes | ✅ Yes | ✅ All | ✅ Yes | ✅ Yes |
| developer | ❌ No | ❌ No | ❌ No | ✅ Yes | ✅ Yes |
| tester | ❌ No | ❌ No | ❌ No | ✅ Yes | ✅ Yes |
```

---

## Role vs Skill vs LLM

**Critical Distinction**:

| Concept | Definition | Example |
|---------|------------|---------|
| **LLM** | Underlying AI model | Claude Sonnet 4.5, GPT-4, Gemini |
| **Agent** | Instance of an LLM performing work | A Claude session, Copilot instance |
| **Role** | Responsibility/function an agent assumes | orchestrator, tester, designer |
| **Skill** | Capability/procedure for accomplishing tasks | work-item-manager, java-spring-architect |

**Key Principle**:
- **Roles** are "who does what" (responsibility, authority)
- **Skills** are "how to do it" (procedures, capabilities)
- A role uses multiple skills
- A skill can be used by multiple roles
- Files are named by **role**, not LLM

---

## Standard Role Categories

### Coordination Roles
- **orchestrator**: Manages workflow, coordinates other roles
- **project-manager**: Plans sprints, manages backlog

### Creative Roles
- **designer**: UX/UI design, mockups
- **artist**: Image generation, visual assets
- **creative-writer**: Ad copy, fiction, lyrics

### Technical Roles
- **developer**: Code implementation
- **architect**: System design
- **logician**: Rules, puzzles, algorithms

### Quality Roles
- **tester**: Testing, QA
- **reviewer**: Code/design review
- **analyst**: Data analysis, metrics

### Research Roles
- **researcher**: Requirements, discovery
- **documenter**: Documentation creation

---

## File Naming Convention

All role-created files follow:

```
<descriptive-name>__<role-name>__<yyyy-mm-ddThhmmssZ>.<ext>
```

Examples:
- `api-design__architect__2026-03-30T140512Z.md`
- `dashboard-mockup__designer__2026-03-30T141230Z.png`
- `test-results__tester__2026-03-30T142015Z.pdf`
- `comment__orchestrator__2026-03-30T140530Z.md`

---

## Logging Convention

Each role writes to daily log file:

```
agents/logs/<role-name>__<yyyy-mm-dd>.log
```

Format: JSON Lines (one JSON object per line)

```json
{"timestamp":"2026-03-30T14:05:12Z","role":"orchestrator","activity":"story_created","story_id":"STORY-123","details":{"title":"Dashboard redesign"},"outcome":"success"}
```

---

## Role Authority Levels

Define clear authority boundaries:

### Level 1: Read-Only
- Can read workspace
- Cannot modify any files
- Can generate artifacts and comments

### Level 2: Contributor
- Can create artifacts and comments
- Cannot modify authoritative files (story.md, task.md)
- Cannot transition states

### Level 3: Manager (e.g., orchestrator)
- Can modify authoritative files
- Can create work items
- Can transition states
- Can coordinate other roles

### Level 4: Administrator
- All Level 3 permissions
- Can modify workspace structure
- Can create/delete lifecycle directories

---

## Completeness Checklist
- □ Role name is kebab-case and unique?
- □ Primary responsibility clearly stated?
- □ Authority boundaries explicit (can/cannot)?
- □ Skills used are listed and exist?
- □ File naming convention documented?
- □ Logging requirements specified?
- □ Interaction patterns cover all dimensions (humans, roles, workspace)?
- □ Example session provided?

## Rules
1. **ALWAYS** use kebab-case for role names
2. **ALWAYS** define explicit authority boundaries (what role CAN and CANNOT do)
3. **ALWAYS** list specific skills the role uses
4. **ALWAYS** document file naming with role name (not LLM name)
5. **ALWAYS** specify logging requirements
6. **ALWAYS** include interaction patterns (humans, other roles, workspace)
7. **NEVER** use LLM names in role definitions or file naming
8. **NEVER** create roles without clear responsibility boundaries
9. **NEVER** skip the validation checklist
10. **NEVER** create overlapping authority without justification
11. **ALWAYS** distinguish between roles (who) and skills (how)
12. **ALWAYS** make role definitions implementation-agnostic (any LLM can assume the role)