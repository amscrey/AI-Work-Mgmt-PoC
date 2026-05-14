---
role: file-contract-validator
story_id: STORY-010
timestamp: 2026-03-30T205500Z
comment_type: completion
---

# File/Directory Naming Validators Design - Completion

## Artifacts Delivered
- `validator-class-design__file-contract-validator__2026-03-30T205200Z.md`

## Summary
Designed complete set of validator classes with regex patterns for all naming conventions:

### Validators Designed
1. **StoryDirectoryNameValidator**: `STORY-\d{1,}-[a-z0-9]+(-[a-z0-9]+)*`
2. **TaskDirectoryNameValidator**: `TASK-\d{1,}-[a-z0-9]+(-[a-z0-9]+)*`
3. **ArtifactFileNameValidator**: `[a-z0-9-]+__[a-z-]+__\d{4}-\d{2}-\d{2}T\d{6}Z\..+`
4. **CommentFileNameValidator**: `comment__[a-z-]+__\d{4}-\d{2}-\d{2}T\d{6}Z\.md`
5. **LogFileNameValidator**: `[a-z-]+__\d{4}-\d{2}-\d{2}\.log`

## Key Design Decisions
1. **Class Hierarchy**: Abstract `RegexNamingValidator<T>` base class for reusability
2. **Validation Result**: Structured result with success/failure and error messages
3. **Metadata Extraction**: Validators can parse filenames to extract metadata (name, role, timestamp)
4. **Generic Interface**: `NamingValidator<T>` allows validators for different input types (Path, String)

## Test Design Coverage
- **Positive cases**: All valid patterns accepted
- **Negative cases**: All invalid patterns rejected (uppercase, missing parts, wrong format)
- **Edge cases**: Empty strings, null inputs, special characters, unicode
- **Metadata parsing**: Extract StoryId, ArtifactMetadata from filenames

## Integration Points
- Used by ValidationService (STORY-003)
- Implements patterns from validation rules catalog (STORY-009)
- Validates filesystem structure created by repositories (STORY-004)

## Status
✅ **Complete** - Ready for implementation in Phase 1
