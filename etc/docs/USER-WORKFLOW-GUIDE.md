# Workflow App Usage Guide (Draft)

## Recommended Roles

- **Product designer / UX writer**: define the user journey, tasks, and terminology.
- **Solutions architect**: validate system boundaries, configuration, and integration points.
- **Technical writer**: produce the end-user doc and CLI/REST examples.
- **QA / tester**: validate the steps on a clean workspace and confirm outputs.

## Purpose

This guide shows how a user can run the workflow app, create sample stories, and trigger story work.

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
- A workspace folder (e.g., `AI-Work-Mgmt-LLM-Orch-AddOn/`)
- REST API is available (see `etc/docs/REST-API.md`)
- Execution mode configured (`orchestration.execution-mode=template` recommended for first run)

## Quick Start (REST)

### 1) Start the app

```bash
mvn spring-boot:run
```

> If you keep stories in a specific workspace folder, set it in your app configuration before starting.

### 1a) (Optional) Run the quickstart prompt script

```bash
cd quickstart
./run-prompt.sh
```

This script reads `quickstart/prompt.txt` and sends it to `/api/v1/orchestration/prompt`.

### 2) Create sample stories

Create two stories via REST (example payloads):

```bash
curl -X POST http://localhost:8080/api/v1/stories \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-100",
    "title": "Onboarding checklist",
    "summary": "Create onboarding checklist for new engineers",
    "author": "human",
    "prioritization": "PRIORITIZED"
  }'
```

```bash
curl -X POST http://localhost:8080/api/v1/stories \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-101",
    "title": "Database backup plan",
    "summary": "Draft database backup and restore plan",
    "author": "human",
    "prioritization": "PRIORITIZED"
  }'
```

### 3) (Optional) Add tasks

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "taskId": "TASK-100-A",
    "storyId": "STORY-100",
    "title": "Outline onboarding sections"
  }'
```

### 4) Trigger orchestration for a story

```bash
curl -X POST http://localhost:8080/api/v1/orchestration/runs \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-100",
    "workIntent": "Generate onboarding checklist and key steps",
    "requestedBy": "human"
  }'
```

> For deterministic outputs and zero API cost, keep `execution-mode=template`.

### 5) Check orchestration status

```bash
curl http://localhost:8080/api/v1/stories/STORY-100/orchestration
```

### 6) Retrieve usage summary (tokens)

```bash
curl http://localhost:8080/api/v1/stories/STORY-100/usage
```

## Expected Outputs

- Story data stored under the workspace story directory (backlog/prioritized/done).
- Artifacts created for each orchestration run under `{prioritization}/{storyId}/artifacts/`.
- Usage summaries available via REST.

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

### Phase 1: Requirements Draft

1) **Create a requirement artifact** (human-authored):
   - Write a short markdown doc with goals and constraints.
   - Save to workspace reference files (e.g., `reference/game-requirements.md`).

2) **Create a story for initial planning**:
   - Story: "Game MVP - requirements & outline"
   - Intent: "Refine requirements and outline the MVP"

3) **Verify**
   - Check `prioritized/STORY-100/` or `backlog/STORY-100/` folder.
   - Confirm `story.json` created and `comments/` is empty.

### Phase 2: Planning and Assignment

1) **Run orchestration in template mode** to create:
   - Requirements refinement artifact
   - MVP scope outline

2) **Create/assign follow-up stories** (human or orchestrator role):
   - Story A: "Inventory model + API" (Java)
   - Story B: "Basic crafting rules" (game design)

3) **Verify**
   - Check `prioritized/STORY-100/artifacts/` for new artifacts.
   - Check `activity-log/` files for orchestration events.

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

- **Stories**: `{prioritization}/{storyId}/story.json` (state, assignee, history)
- **Artifacts**: `{prioritization}/{storyId}/artifacts/*.md`
- **Comments**: `{prioritization}/{storyId}/comments/*.md`
- **Activity logs**: `activity-log/*.jsonl`
- **Token usage**: `{prioritization}/{storyId}/usage/llm-usage-summary.json`
- **Test output**: `target/surefire-reports/`

## Example: Basic Game Feature Ideas

- **Inventory system**: item stacking, capacity, and equip slots.
- **Crafting**: simple recipes (e.g., wood + iron = sword) and balance notes.
- **Progression**: unlock recipes by level.

These are small enough to fit into a single story each, and they generate both code and design artifacts.

## How This Example Meets the Requirements

- **Modes explained**: template/llm/dummy + how to set.
- **Requirements → planning → work → approval**: a full lifecycle walkthrough.
- **Java development**: inventory model + tests.
- **Game design**: crafting rules and balance notes.
- **Verification**: explicit file locations and logs to inspect.
