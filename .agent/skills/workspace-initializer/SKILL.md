---
name: workspace-initializer
description: Initialize and validate external filesystem workspace structures for AI-managed work items. Use when creating new managed workspaces, validating existing workspace integrity, or detecting structural violations.
---

# Workspace Initializer

Initializes and validates external filesystem workspace structures that serve as the system of record for AI-managed work items.

## Input
- Workspace root path (absolute filesystem path)
- Workspace configuration (optional - defaults to standard structure)
- Validation mode (strict, permissive, or repair)

## Output
- Initialized workspace directory structure
- Validation report (if validating existing workspace)
- Configuration file (`workspace-config.yml`)
- Status report with actionable remediation steps

---

## Process

### Phase 1: Validate Path and Permissions
1. Verify workspace root path is absolute
2. Check parent directory exists and is writable
3. Check workspace root does not conflict with core project repository
4. Verify no symlinks in path (security constraint)
5. Confirm user has read/write/execute permissions

### Phase 2: Create or Validate Structure
1. Create or verify top-level directories:
   - `work-items/`
   - `reference/`
   - `agents/`
   - `templates/`
2. Create or verify work-items lifecycle subdirectories:
   - `work-items/backlog/`
   - `work-items/prioritized/`
   - `work-items/todo/`
   - `work-items/in-progress/`
   - `work-items/awaiting-approval/`
   - `work-items/done/`
3. Create or verify reference subdirectories:
   - `reference/technical/`
   - `reference/human-facing/`
   - `reference/media/`
4. Create or verify agents subdirectories:
   - `agents/logs/`
   - `agents/memory/`

### Phase 3: Generate Configuration and Templates
1. Create `workspace-config.yml` with:
   - workspace version
   - creation timestamp
   - lifecycle states
   - naming conventions
   - validation rules
2. Create `.gitkeep` files in empty directories
3. Create `README.md` in workspace root explaining structure
4. Create `templates/story-template.md`
5. Create `templates/task-template.md`
6. Create `templates/artifact-template.md`

### Phase 4: Validate Integrity
1. Verify all required directories exist
2. Check for unexpected top-level directories
3. Validate workspace-config.yml is valid YAML
4. Check for permission issues
5. Generate validation report

---

## Validation Rules

A workspace is **valid** if:
- All required top-level directories exist
- All lifecycle state directories exist under `work-items/`
- All reference subdirectories exist
- All agent subdirectories exist
- `workspace-config.yml` exists and is valid
- No work items exist outside the managed workspace
- Workspace root is external to core project repository

A workspace is **invalid** if:
- Required directories are missing
- Unexpected top-level directories exist (warn only)
- Permission issues prevent read/write
- Workspace root is inside core project repository
- Symlinks are detected in critical paths

## Repair Mode

When validation fails and repair mode is enabled:
1. Create missing directories
2. Fix permissions (if possible)
3. Regenerate missing config files
4. Log all repairs to `agents/logs/workspace-repair-<timestamp>.log`
5. Report unrepairable issues

---

## Completeness Checklist
- □ All required directories created and verified?
- □ `workspace-config.yml` generated with complete metadata?
- □ Template files created in `templates/`?
- □ Validation report generated?
- □ User can create stories/tasks immediately after initialization?

## Rules
1. **ALWAYS** verify workspace root is external to core project repository
2. **ALWAYS** create all required directories atomically (rollback on failure)
3. **ALWAYS** log initialization details to `agents/logs/`
4. **ALWAYS** validate permissions before attempting writes
5. **NEVER** overwrite existing `workspace-config.yml` without user confirmation
6. **NEVER** delete existing work items during initialization or validation
7. **NEVER** create workspace inside core project repository
8. **NEVER** proceed if critical permission issues exist