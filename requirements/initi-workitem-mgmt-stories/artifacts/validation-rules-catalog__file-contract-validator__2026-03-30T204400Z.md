# Validation Rules Catalog

**Role**: file-contract-validator
**Story**: STORY-009
**Status**: Complete

## Directory Naming Validation

| Rule | Pattern | Valid Example | Invalid Example |
|------|---------|---------------|-----------------|
| Story Dir | `STORY-\d{1,}-[a-z0-9-]+` | `STORY-123-dashboard-ui` | `story-123-UI` (lowercase prefix, uppercase slug) |
| Task Dir | `TASK-\d{1,}-[a-z0-9-]+` | `TASK-001-api-service` | `TASK-1-API` |

## File Naming Validation

| File Type | Pattern | Example |
|-----------|---------|---------|
| Artifact | `[a-z0-9-]+__[a-z-]+__\d{4}-\d{2}-\d{2}T\d{6}Z\..+` | `mockup__designer__2026-03-30T141230Z.png` |
| Comment | `comment__[a-z-]+__\d{4}-\d{2}-\d{2}T\d{6}Z\.md` | `comment__coder__2026-03-30T141530Z.md` |
| Log | `[a-z-]+__\d{4}-\d{2}-\d{2}\.log` | `orchestrator__2026-03-30.log` |

## Metadata Validation

**story.md Required Fields**:
- id (matches directory name)
- type = "story"
- title (non-empty, max 200 chars)
- state (matches directory location)
- prioritization (matches directory)
- author (non-empty)
- summary (non-empty, max 500 chars)
- acceptance_criteria (list, min 1 item)

## State Consistency Validation

| Check | Rule | Error if |
|-------|------|----------|
| Story in backlog/ | `prioritization: backlog` | Mismatch |
| Story in todo/ | `state: todo`, `prioritization: prioritized` | Either mismatch |
| State field | Must be valid enum value | Invalid value |

## Test Design Outline
```java
@Test void validStoryDirectoryName_passes()
@Test void invalidStoryDirectoryName_fails()
@Test void validArtifactName_passes()
@Test void missingRequiredField_fails()
@Test void stateMismatchWithDirectory_fails()
```
