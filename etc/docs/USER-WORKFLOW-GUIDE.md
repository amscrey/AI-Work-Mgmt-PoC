# Workflow App Usage Guide

**Version**: 1.1
**Last Updated**: 2026-05-16

## Recommended Roles

- **Product designer / UX writer**: define the user journey, tasks, and terminology.
- **Solutions architect**: validate system boundaries, configuration, and integration points.
- **Technical writer**: produce the end-user doc and CLI/REST examples.
- **QA / tester**: validate the steps on a clean workspace and confirm outputs.

## Purpose

This guide shows how a user can run the workflow app, use PROJECT-PLANNING for initial requirements gathering, create sample stories, and trigger story work.

## Role Review Checklist

- **Product designer / UX writer**
  - Confirm the steps match a real user journey (happy path + recovery steps).
  - Check phrasing for clarity, brevity, and action-oriented language.
  - Ensure terms match the UI/CLI/REST labels.

- **Solutions architect**
  - Validate configuration steps and boundaries (execution mode, provider config).
  - Confirm API endpoints align with current service capabilities.
  - Note integration constraints (filesystem workspaces, local-only for now).

- **Technical writer**
  - Ensure commands are copyable and minimal.
  - Provide consistent terminology and headings.
  - Add references to key docs (`REST-API.md`, `dummy-llm-bridge.md`).

- **QA / tester**
  - Run the steps end-to-end on a clean workspace.
  - Confirm expected outputs are created in the workspace.
  - Verify error cases and recovery steps.

## Preconditions

- Java 17+
- Maven 3+
- A workspace folder (e.g., `my-project-workspace/`)
- REST API is available (see `etc/docs/REST-API.md`)
- Execution mode configured (`orchestration.execution-mode=template` recommended for first run)

## Terminology

> **`<workspace>`** in this document refers to your project's workspace directory path. This is a **placeholder** - replace it with your actual workspace name (e.g., `expandedStoryMgr`, `inventory-system`, `my-game-project`).
>
> **Do NOT create a directory literally named "workspace"** - use a descriptive name for your project.
>
> Configure your workspace path via:
> - Environment variable: `WORKSPACE_ROOT=/path/to/my-project-workspace`
> - Or in `application.yml`: `workmanagement.workspace-root: /path/to/my-project-workspace`

## Quick Start (REST)

### 1) Start the app

```bash
mvn spring-boot:run
```

> If you keep stories in a specific workspace folder, set it in your app configuration before starting.

### 2) (Optional) Project Planning Phase

**Use PROJECT-PLANNING for initial requirements gathering before creating stories.**

PROJECT-PLANNING is a special reserved story ID for project-level work:
- Initial requirements analysis
- Story ideation and backlog creation
- Architecture planning
- Scope definition

#### Send a project-level prompt:

```bash
curl -X POST 'http://localhost:8080/api/v1/orchestration/prompt?requestedBy=human' \
  -H 'Content-Type: text/plain' \
  --data 'Analyze requirements for an inventory management system.
Identify key user needs, technical constraints, and suggest initial stories
for the backlog.'
```

**What happens**:
- Request routes to PROJECT-PLANNING (no storyId provided)
- Orchestrator generates requirements analysis artifact
- Artifacts saved to `prioritized/PROJECT-PLANNING/artifacts/`
- You can review artifacts and create stories based on recommendations

#### Add human feedback to PROJECT-PLANNING:

You can add your own responses to refine the analysis:

```bash
# Create a human response artifact
echo "Based on the analysis, prioritize:
1. Basic inventory CRUD operations
2. Stock level tracking
3. Low stock alerts

Defer for later:
- Advanced reporting
- Integration with external systems" \
> <workspace>/prioritized/PROJECT-PLANNING/artifacts/human-response__human__$(date -u +%Y-%m-%dT%H%M%SZ).md
```

Then send another prompt to process the feedback:

```bash
curl -X POST 'http://localhost:8080/api/v1/orchestration/prompt?requestedBy=human' \
  -H 'Content-Type: text/plain' \
  --data 'I added a human response artifact to PROJECT-PLANNING.
Based on this feedback, create the initial story backlog.'
```

#### Using the quickstart script:

```bash
cd quickstart
./run-prompt.sh
```

This script:
- Reads `quickstart/prompt.txt`
- Sends it to `/api/v1/orchestration/prompt`
- Routes to PROJECT-PLANNING by default (no storyId)
- Writes response to `response.json` and `AssistantResponse.md`

### 3) Create sample stories

After reviewing PROJECT-PLANNING artifacts (or skipping project planning), create stories:

**Option A: Create stories manually via REST**:

```bash
curl -X POST http://localhost:8080/api/v1/stories \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-100",
    "title": "Basic inventory CRUD operations",
    "summary": "Implement create, read, update, delete operations for inventory items",
    "author": "human",
    "prioritization": "PRIORITIZED"
  }'
```

```bash
curl -X POST http://localhost:8080/api/v1/stories \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-101",
    "title": "Stock level tracking",
    "summary": "Add functionality to track current stock levels and update on transactions",
    "author": "human",
    "prioritization": "BACKLOG"
  }'
```

**Option B: Create stories from PROJECT-PLANNING artifacts**:

First, preview story suggestions extracted from PROJECT-PLANNING artifacts:

```bash
curl http://localhost:8080/api/v1/project-planning/suggestions
```

Then create stories from the suggestions:

```bash
curl -X POST 'http://localhost:8080/api/v1/project-planning/create-stories?requestedBy=human'
```

This parses PROJECT-PLANNING artifacts for story patterns (e.g., `## STORY-XXX: Title`, numbered lists with descriptions) and creates stories in the backlog.

**Option C: Let orchestrator create stories** (when using LLM mode with designer node):

The orchestrator can create stories automatically based on PROJECT-PLANNING artifacts.

### 4) (Optional) Add tasks

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "taskId": "TASK-100-A",
    "storyId": "STORY-100",
    "title": "Define inventory item data model"
  }'
```

### 5) Trigger orchestration for a story

Work on a specific story using the prompt endpoint:

```bash
curl -X POST 'http://localhost:8080/api/v1/orchestration/prompt?requestedBy=human&storyId=STORY-100' \
  -H 'Content-Type: text/plain' \
  --data 'Implement the basic CRUD operations for inventory items.
Include REST endpoints, service layer, and repository.
Write unit tests for all components.'
```

Or use the structured endpoint:

```bash
curl -X POST http://localhost:8080/api/v1/orchestration/runs \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-100",
    "workIntent": "Implement CRUD operations with tests",
    "requestedBy": "human"
  }'
```

> For deterministic outputs and zero API cost, keep `execution-mode=template`.

### 6) Check orchestration status

```bash
curl http://localhost:8080/api/v1/stories/STORY-100/orchestration
```

### 7) Retrieve usage summary (tokens)

```bash
curl http://localhost:8080/api/v1/stories/STORY-100/usage
```

**For PROJECT-PLANNING**:
```bash
curl http://localhost:8080/api/v1/stories/PROJECT-PLANNING/usage
```

## Expected Outputs

- **PROJECT-PLANNING artifacts**: `prioritized/PROJECT-PLANNING/artifacts/` contains:
  - Requirements analysis
  - Story recommendations
  - Architecture decisions
  - Human feedback responses
- **Story data**: Stored under workspace story directory (backlog/prioritized/done)
- **Story artifacts**: Created for each orchestration run under `{prioritization}/{storyId}/artifacts/`
- **Activity logs**: `activity-log/*.jsonl` tracks all orchestration events
- **Usage summaries**: Available via REST API and stored in `{prioritization}/{storyId}/usage/`

## Troubleshooting

- **400 Bad Request**: Verify required request fields are present and valid.
- **404 Not Found**: Confirm story IDs exist before running orchestration.
- **Orchestration errors**: Check server logs for context assembly or state graph errors.
- Use `GET /api/v1/health` to verify the server is running.

## Execution Modes (What They Are, Why They Matter)

- **template**: deterministic, fast, no LLM calls. Use for demos, tests, or predictable outputs.
- **llm**: real model calls via provider config. Use for production-quality generation.
- **dummy** (bridge modes): manual or deterministic handoff to humans or fixtures. Use for low-cost testing and audits.

### How to Set the Mode

Set the execution mode in configuration (example uses `application.yml`):

```yaml
orchestration:
  execution-mode: template
```

If you use dummy LLM modes, set a dummy provider and model via config:

```bash
export SPRING_APPLICATION_JSON='{"orchestration":{"execution-mode":"llm","llm":{"default-provider":"dummy","providers":{"dummy":{"model":"file-bridge"}}}}}'
```

See `etc/docs/dummy-llm-bridge.md` for file-bridge, clipboard, and fixture modes.

## Example Use Case: Mini Game Spec + Java Feature

This scenario builds a small game feature (basic inventory + crafting loop) and uses the workflow to refine requirements, plan stories, assign work, and approve results. It includes Java development and game design artifacts.

### Phase 0: Project Planning (PROJECT-PLANNING)

1) **Start with high-level project vision** using PROJECT-PLANNING:

```bash
curl -X POST 'http://localhost:8080/api/v1/orchestration/prompt?requestedBy=human' \
  -H 'Content-Type: text/plain' \
  --data 'We want to build a simple inventory and crafting system for a mini game.
Requirements:
- Players can collect items
- Items can be combined using recipes
- Limited inventory space
- Recipes unlock based on player level

Analyze these requirements and suggest initial stories for the backlog.'
```

2) **Review PROJECT-PLANNING artifacts**:
   - Check `prioritized/PROJECT-PLANNING/artifacts/`
   - Review `requirements-analysis__researcher__timestamp.md`
   - Review `story-suggestions__designer__timestamp.md`

3) **Add human feedback** if needed:
   - Create `human-response__human__timestamp.md` in PROJECT-PLANNING artifacts
   - Provide clarifications, priorities, or constraints
   - Send another prompt to incorporate feedback

4) **Verify**
   - Confirm PROJECT-PLANNING artifacts created
   - Check `activity-log/` for orchestration events

### Phase 1: Story Creation from PROJECT-PLANNING

1) **Based on PROJECT-PLANNING analysis**, create initial stories:
   - Story A: "STORY-100: Inventory model + API" (Java)
   - Story B: "STORY-101: Basic crafting rules" (game design)
   - Story C: "STORY-102: Recipe progression system" (game design)

2) **Stories start in BACKLOG** (default):
   - All stories created with `prioritizationState: BACKLOG`
   - Review and prioritize based on PROJECT-PLANNING recommendations

3) **Verify**
   - Check `backlog/STORY-100/`, `backlog/STORY-101/`, etc.
   - Confirm `story.json` files created

### Phase 2: Planning and Assignment

1) **Prioritize stories for work**:
   - Move STORY-100 and STORY-101 to PRIORITIZED
   - STORY-102 stays in BACKLOG for later

2) **Run orchestration on STORY-100**:
```bash
curl -X POST 'http://localhost:8080/api/v1/orchestration/prompt?requestedBy=human&storyId=STORY-100' \
  -H 'Content-Type: text/plain' \
  --data 'Design the inventory model and implement CRUD API.
Include:
- Item data model (id, name, type, stackSize)
- Inventory container (capacity limit)
- REST endpoints for item management
- Unit tests'
```

3) **Verify**
   - Check `prioritized/STORY-100/artifacts/` for design and implementation artifacts
   - Check `activity-log/` files for orchestration events

### Phase 3: Work Execution (Java + Game Design)

1) **Java story**: implement inventory model
   - Example files: `src/main/java/.../Inventory.java`, `Item.java`
   - Add unit tests under `src/test/java/...`

2) **Game design story**: craft rules and balancing notes
   - Artifact: `crafting-rules__designer__timestamp.md`

3) **Comments**
   - Add coder comments to explain design choices and test coverage.
   - Add reviewer comment with feedback.

4) **Verify**
   - Check `prioritized/STORY-101/artifacts/` for code design notes.
   - Check `prioritized/STORY-101/comments/` for usage blocks and metrics.
   - Inspect `target/site/jacoco/` for coverage reports.

### Phase 4: Review and Approval

1) **Human review**
   - Inspect `story.json` for workflow state.
   - Read comments for completion notes and tests run.

2) **Approve**
   - Move story to `done/` after approval.

3) **Verify**
   - Ensure `done/STORY-101/` contains artifacts + comments.

## What to Inspect at Each Step

### Project Planning Phase
- **PROJECT-PLANNING artifacts**: `prioritized/PROJECT-PLANNING/artifacts/`
  - `requirements-analysis__researcher__timestamp.md`
  - `story-suggestions__designer__timestamp.md`
  - `human-response__human__timestamp.md` (if you added feedback)
- **PROJECT-PLANNING usage**: `prioritized/PROJECT-PLANNING/usage/llm-usage-summary.json`

### Story Execution Phase
- **Stories**: `{prioritization}/{storyId}/story.json` (state, assignee, history)
- **Artifacts**: `{prioritization}/{storyId}/artifacts/*.md`
  - Design documents
  - Implementation plans
  - Test reports
- **Comments**: `{prioritization}/{storyId}/comments/*.md`
- **Activity logs**: `activity-log/*.jsonl` (all orchestration events)
- **Token usage**: `{prioritization}/{storyId}/usage/llm-usage-summary.json`
- **Test output**: `target/surefire-reports/`

### Quick Checks
```bash
# View PROJECT-PLANNING artifacts
ls -la <workspace>/prioritized/PROJECT-PLANNING/artifacts/

# View story artifacts
ls -la <workspace>/prioritized/STORY-100/artifacts/

# Check latest activity logs
tail -f <workspace>/activity-log/*.jsonl

# Check token usage
curl http://localhost:8080/api/v1/stories/PROJECT-PLANNING/usage
curl http://localhost:8080/api/v1/stories/STORY-100/usage
```

## Example: Basic Game Feature Ideas

- **Inventory system**: item stacking, capacity, and equip slots.
- **Crafting**: simple recipes (e.g., wood + iron = sword) and balance notes.
- **Progression**: unlock recipes by level.

These are small enough to fit into a single story each, and they generate both code and design artifacts.

## Complete Workflow Summary

The AI Work Management Platform supports a complete workflow from initial idea to completed work:

### 1. Project Initialization (PROJECT-PLANNING)
- **Purpose**: Define scope, gather requirements, identify stories
- **Tool**: `/api/v1/orchestration/prompt` (no storyId)
- **Artifacts**: Requirements analysis, story suggestions, architecture decisions
- **Location**: `prioritized/PROJECT-PLANNING/artifacts/`

### 2. Story Creation and Backlog Management
- **Purpose**: Create concrete work items based on PROJECT-PLANNING
- **Tool**: `/api/v1/stories` (POST)
- **Initial State**: BACKLOG (default)
- **Location**: `backlog/{storyId}/`

### 3. Story Prioritization
- **Purpose**: Select stories ready for work
- **Action**: Move from BACKLOG to PRIORITIZED
- **State Change**: `prioritizationState: BACKLOG` → `PRIORITIZED`
- **Location**: `backlog/{storyId}/` → `prioritized/{storyId}/`

### 4. Story Execution
- **Purpose**: Perform actual work (design, implementation, testing)
- **Tool**: `/api/v1/orchestration/prompt?storyId={storyId}`
- **Workflow States**: TODO → IN_PROGRESS → AWAITING_APPROVAL → DONE
- **Artifacts**: Design docs, code, test reports
- **Location**: `prioritized/{storyId}/artifacts/`

### 5. Approval and Completion
- **Purpose**: Review work and mark complete
- **Action**: Human approval of AWAITING_APPROVAL stories
- **Final State**: DONE
- **Location**: `prioritized/{storyId}/` → `done/{storyId}/`

### Example End-to-End Flow

```bash
# Phase 1: Project Planning
curl -X POST 'http://localhost:8080/api/v1/orchestration/prompt?requestedBy=human' \
  -H 'Content-Type: text/plain' \
  --data 'Analyze requirements for inventory system'

# Review: prioritized/PROJECT-PLANNING/artifacts/

# Phase 2: Create Stories
curl -X POST http://localhost:8080/api/v1/stories \
  -H 'Content-Type: application/json' \
  -d '{"storyId":"STORY-100","title":"Inventory CRUD",...}'

# Phase 3: Work on Story
curl -X POST 'http://localhost:8080/api/v1/orchestration/prompt?requestedBy=human&storyId=STORY-100' \
  -H 'Content-Type: text/plain' \
  --data 'Implement inventory CRUD with tests'

# Review: prioritized/STORY-100/artifacts/

# Phase 4: Approve and Complete
# Human reviews work, orchestrator marks DONE, moves to done/STORY-100/
```

## How This Example Meets the Requirements

- **Modes explained**: template/llm/dummy + how to set
- **PROJECT-PLANNING**: Special project initialization workflow
- **Requirements → planning → work → approval**: Full lifecycle walkthrough
- **Java development**: Inventory model + tests
- **Game design**: Crafting rules and balance notes
- **Verification**: Explicit file locations and logs to inspect

---

**Version History**:
- 1.1 (2026-05-16): Added PROJECT-PLANNING workflow and comprehensive examples
- 1.0 (2026-04-03): Initial draft
