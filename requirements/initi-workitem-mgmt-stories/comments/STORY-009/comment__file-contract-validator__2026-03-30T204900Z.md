---
role: file-contract-validator
story_id: STORY-009
timestamp: 2026-03-30T204900Z
comment_type: completion
---

# Validation Rules Catalog - Completion

## Artifacts Delivered
- `validation-rules-catalog__file-contract-validator__2026-03-30T204400Z.md`

## Summary
Cataloged all validation rules for filesystem-based workspace:

### Directory Naming Validation
- Story directories: `STORY-\d{1,}-[a-z0-9-]+`
- Task directories: `TASK-\d{1,}-[a-z0-9-]+`
- Examples: STORY-123-dashboard-ui, TASK-001-api-service

### File Naming Validation
- Artifacts: `[a-z0-9-]+__[a-z-]+__\d{4}-\d{2}-\d{2}T\d{6}Z\..+`
- Comments: `comment__[a-z-]+__\d{4}-\d{2}-\d{2}T\d{6}Z\.md`
- Logs: `[a-z-]+__\d{4}-\d{2}-\d{2}\.log`

### Metadata Validation
- **story.md Required Fields**: id, type, title, state, prioritization, author, summary, acceptance_criteria
- **Constraints**: title max 200 chars, summary max 500 chars, min 1 acceptance criterion

### State Consistency Validation
- Story in backlog/ → prioritization: backlog
- Story in todo/ → state: todo, prioritization: prioritized
- Directory location MUST match state fields

## Key Design Decisions
1. **Regex Patterns**: Strict patterns prevent filename collisions
2. **State Consistency**: Dual validation (file metadata + directory location)
3. **Required Fields**: Title now required (per user clarification)
4. **Test Coverage**: Positive and negative test cases for all rules

## Integration Points
- Used by ValidationService (STORY-003)
- Used by FileSystemRepository implementations (STORY-004)
- Enforces workspace structure (STORY-007)

## Status
✅ **Complete** - Ready for implementation in Phase 1
