# Documentation Directory

This directory contains technical documentation for the AI Work Management Platform.

---

## Project Mission

See `MISSION.md` for the project mission and primary goals.

---

## Documentation Structure

```
AI-Work-Mgmt/
├── README.md                 # This file
├── MISSION.md                # Mission and primary goals
└── etc/
    └── docs/
        ├── TESTING.md
        └── history/
            ├── PHASE-1A-COMPLETE.md
            ├── PHASE-1B-*.md
            ├── PHASE-1C-*.md
            └── ACTIVITY-LOGGER-COMPLETE.md
```

---

## Quick Links

### For Developers

📖 **[TESTING.md](etc/docs/TESTING.md)** - **START HERE**
- Test pyramid overview (see TESTING.md for current totals)
- Running tests (Maven commands)
- Understanding test results
- Integration test workspace configuration
- Writing new tests
- CI/CD integration

📖 **[quickstart/README.md](quickstart/README.md)** - **Starting the app**
- How to start with/without LLMs
- Template, LLM, and Dummy modes
- Environment variable setup (.env file)
- Health check and verification

📖 **[CURL-EXAMPLES.md](etc/docs/CURL-EXAMPLES.md)** - **Complete curl examples**
- All REST API endpoints with working curl commands
- Stories, tasks, comments, orchestration, usage
- Common workflows and error handling

📖 **[REST-API.md](etc/docs/REST-API.md)** - API conventions and error payloads

## Workspace Configuration

Set the workspace root if you want the app to write to a specific folder:

```bash
export WORKSPACE_ROOT=/path/to/workspace
```

### For Understanding Project History

📁 **[history/](etc/docs/history/)** - Phase completion documents
- Detailed implementation notes
- Test coverage details
- Performance metrics
- Technical decisions

---

## Key Documentation

### TESTING.md - Essential Reading

This is the **primary testing guide** for the project. It covers:

**Test Pyramid** (see TESTING.md for current totals):
- Unit Tests (Domain + Application layers)
- Integration Tests (Story, Task, Comment, ActivityLogger)
- E2E Tests (when enabled)

**Running Tests**:
```bash
# All tests
mvn test

# Integration tests only
mvn test -Dtest='*IT'

# Specific test
mvn test -Dtest=FileBasedStoryRepositoryIT

# With coverage
mvn clean test jacoco:report
```

**Integration Test Workspaces**:

All integration tests use isolated workspaces in `target/` directory:

| Test Suite | Workspace | Purpose |
|------------|-----------|---------|
| FileBasedStoryRepositoryIT | `target/test-workspace-story` | Story persistence |
| FileBasedTaskRepositoryIT | `target/test-workspace-task` | Task persistence |
| FileBasedCommentRepositoryIT | `target/test-workspace-comment` | Comment persistence |
| FileBasedActivityLoggerIT | `target/test-workspace-activity-log` | Activity logging |

**Why This Matters**:
- Tests run in isolation (clean workspace each test)
- No production data interference
- `mvn clean` removes all test data
- Not committed to git (target/ is ignored)

**Test Boundaries**:
- ✅ File system operations (create, read, update, delete)
- ✅ Persistence formats (JSON, Markdown, JSON Lines)
- ✅ Data integrity (fields, timestamps, state transitions)
- ✅ Repository operations (CRUD, queries, error handling)
- ❌ Business logic (covered by unit tests)
- ❌ Service coordination (covered by unit tests)
- ❌ API endpoints (future E2E tests)

---

## Phase Completion Documents (history/)

These documents provide detailed implementation notes for each phase:

### Phase 1A: Domain Layer
- **PHASE-1A-COMPLETE.md**
- Domain model (Story, Task, Comment, WorkflowState, etc.)
- Value objects (StoryId, TaskId, CommentId, RoleName)
- 100 unit tests

### Phase 1B: Application Layer
- **PHASE-1B-*.md** (multiple documents)
- Service implementations (StoryService, TaskService, CommentService)
- Command objects
- Repository interfaces
- 46 unit tests

### Phase 1C: Infrastructure Layer
- **PHASE-1C-*.md** (multiple documents)
- File-based repositories (Story, Task, Comment)
- JSON serialization (Jackson)
- Markdown with YAML frontmatter
- 53 integration tests (repositories)

### Activity Logger
- **ACTIVITY-LOGGER-COMPLETE.md**
- JSON Lines logging implementation
- Per-role log files
- 17 integration tests

---

## Important Notes for Developers

### ⚠️ Keep Documentation Updated

When you modify integration tests, **update TESTING.md** to reflect:
- New test workspaces
- Changed file system structures
- New test boundaries
- Additional verification scenarios

**Why**: Both human and AI developers rely on this documentation to understand test coverage and expectations.

### 📖 Integration Test Headers

All integration test files have comprehensive headers that:
- Specify the test workspace location
- Link to TESTING.md documentation
- Warn developers to keep TESTING.md updated
- List what the test suite verifies

**Example**:
```java
/**
 * Integration tests for FileBasedStoryRepository.
 *
 * <p><b>Test Workspace:</b> {@code target/test-workspace-story}</p>
 *
 * <p><b>For Developers (Human & AI):</b></p>
 * <ul>
 *   <li>📖 See {@code etc/docs/TESTING.md} for complete testing guide</li>
 *   <li>⚠️ When modifying this test class, update etc/docs/TESTING.md</li>
 * </ul>
 */
```

### 🔍 Finding Test Workspace Information

If you need to know where a test writes its files:

1. Check the test class header (comprehensive documentation)
2. Look for `private static final String TEST_WORKSPACE = "...";`
3. See TESTING.md section 3.2 (Integration Test Workspace Configuration)

---

## Test Statistics

**Current State** (see `etc/docs/TESTING.md` for latest totals):

```
✅ Total Tests:           see latest `mvn verify` output
✅ All Tests Passing:     see latest `mvn verify` output
✅ Test Coverage:         >= 80% (JaCoCo check)
✅ Execution Time:        varies by environment
```

**Performance**:
- Unit tests: varies by environment
- Integration tests: varies by environment
- Total: depends on suite selection

---

## Contributing

When adding new functionality:

1. **Write tests first** (TDD approach)
2. **Update TESTING.md** if adding integration tests
3. **Add comprehensive test headers** referencing TESTING.md
4. **Document workspace configuration** for integration tests
5. **Create phase completion doc** when completing major features

---

## Questions?

- See TESTING.md for test-related questions
- See history/ for implementation details
- Check test file headers for workspace information
- Look at existing tests for examples

---

**Last Updated**: 2026-04-05
**Total Tests**: see latest `mvn verify` output
**Documentation**: Comprehensive and up-to-date ✅
