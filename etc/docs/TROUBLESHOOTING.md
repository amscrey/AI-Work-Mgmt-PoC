# Troubleshooting

## App Startup

### Missing Spring Beans

**Symptom**
- Startup fails with `UnsatisfiedDependencyException` (e.g., missing `CommentService`).

**Checks**
- Ensure Spring config wires repositories and services.
- Verify workspace settings are set (`WORKSPACE_ROOT`, `WORKSPACE_NAME`).

### Port Already in Use

**Symptom**
- Startup fails with a message about port 8080 being in use.

**Fix**
- Stop the process using port 8080 or run on a different port:
  - `SPRING_APPLICATION_JSON='{"server":{"port":8081}}' mvn spring-boot:run`

## API Calls

### 400 Bad Request

**Symptom**
- API returns 400 with validation errors.

**Fix**
- Confirm required fields are present.
- Check enums (`WorkflowState`, `TaskState`, `CommentType`) use uppercase names.

### 404 Not Found

**Symptom**
- Story, task, or comment not found.

**Fix**
- Verify the ID exists in the workspace (`backlog/{storyId}`, `prioritized/{storyId}`, or `done/{storyId}`).

## Orchestration

### Orchestration Fails Immediately

**Symptom**
- Orchestration returns error quickly.

**Checks**
- Validate `orchestration.execution-mode` matches your intended mode.
- In LLM mode, confirm provider API keys are set.
- In dummy modes, confirm the expected file paths exist (see `dummy-llm-bridge.md`).

### No Artifacts Created

**Symptom**
- Orchestration returns success but no files appear.

**Checks**
- Verify the workspace root and story directory exist.
- Check logs for `ArtifactOutputService` messages.

### RestartClassLoader ClassCastException

**Symptom**
- Logs show `ClassCastException` between the same class with different classloaders.

**Fix**
- Disable Spring Boot devtools restart:
  - Set `spring.devtools.restart.enabled=false` in `application.yml`
  - Or run with `SPRING_APPLICATION_JSON='{"spring":{"devtools":{"restart":{"enabled":false}}}}'`

## Logging and Outputs

### Activity Logs Missing

**Symptom**
- No activity logs appear under `activity-log/`.

**Checks**
- Confirm the workspace root is set correctly.
- Verify the app has permissions to write to the workspace.

### Usage Summaries Missing

**Symptom**
- `GET /api/v1/stories/{id}/usage` returns empty or 404.

**Checks**
- Ensure LLM nodes recorded usage (template mode may generate only estimates).
- Confirm the story exists and has orchestration activity under `backlog/`, `prioritized/`, or `done/`.

## Where to Look

- Health check: `GET /api/v1/health`
- Logs: console output + `activity-log/*.jsonl`
- Artifacts: `{prioritization}/{storyId}/artifacts/*.md`
- Usage: `{prioritization}/{storyId}/usage/llm-usage-summary.json`
