# Quickstart

This quickstart shows how to send a single prompt to the orchestration API.

## Files

- `prompt.txt`: the prompt body to send
- `run-prompt.sh`: sends `prompt.txt` to the API
- `run-file-bridge.sh`: starts the app in dummy file-bridge mode
- `run-llm-gemini.sh`: starts the app in LLM mode (Gemini)
- `run-llm-groq.sh`: starts the app in LLM mode (Groq)

## Run the App

### Workspace Configuration

Set the workspace root if you want the app to write to a specific folder:

```bash
export WORKSPACE_ROOT=/path/to/workspace
export WORKSPACE_NAME=default
```

### Template Mode (deterministic, no LLM calls)

Template mode is the default. Start the app:

```bash
mvn spring-boot:run
```

### LLM Mode (real model calls)

Provide an API key and switch execution mode to `llm`:

```bash
export ANTHROPIC_API_KEY=your-key-here
export SPRING_APPLICATION_JSON='{"orchestration":{"execution-mode":"llm"}}'

mvn spring-boot:run
```

Notes:
- LLM mode requires provider configuration and API keys.
- You can use `GEMINI_API_KEY` or `GROQ_API_KEY` if those providers are configured.

### Dummy Modes (manual or deterministic)

Dummy modes are useful for low-cost testing and human-in-the-loop workflows.

Example: file-bridge dummy mode

```bash
export WORKSPACE_ROOT=/path/to/workspace
export BRIDGE_ROOT=$WORKSPACE_ROOT/dummy-bridge
export SPRING_APPLICATION_JSON='{"orchestration":{"execution-mode":"llm","llm":{"default-provider":"dummy","providers":{"dummy":{"model":"file-bridge"}}}}}'

mvn spring-boot:run
```

Note: `BRIDGE_ROOT` is the preferred way to configure the dummy bridge location. The application also supports the legacy system property `-Dorchestration.llm.dummy.bridge-root=/path/to/bridge` for backward compatibility, but `BRIDGE_ROOT` will be used when present.

See `etc/docs/dummy-llm-bridge.md` for file-bridge, clipboard, and fixture modes.

### Health Check

Verify the app is running:

```bash
curl http://localhost:8080/api/v1/health
```

## Run the Prompt Script

```bash
export API_BASE=http://localhost:8080/api/v1
export REQUESTED_BY=human
# Optional: export STORY_ID=STORY-123
./run-prompt.sh
```

The script reads `prompt.txt` and posts it to `/api/v1/orchestration/prompt`.
It also writes:
- `response.json` (full API response)
- `AssistantResponse.md` (Markdown-friendly summary + references)

## What to Inspect

### Startup

- Console output from `mvn spring-boot:run`
- Health endpoint: `GET /api/v1/health`

### Activity Logs

- `activity-log/*.jsonl`
- Entries include role, activity, storyId, outcome, and details.

### Orchestration Artifacts

- `{prioritization}/{storyId}/artifacts/*.md`
- `PROJECT-PLANNING` artifacts appear under `prioritized/PROJECT-PLANNING/artifacts/` when used.

### Story Data

- `{prioritization}/{storyId}/story.json`
- `{prioritization}/{storyId}/comments/*.md`

### Usage Summaries

- `GET /api/v1/stories/{storyId}/usage`
- Workspace usage files (if enabled): `{prioritization}/{storyId}/usage/`

## Expected Output

- JSON response with `executionId` and `status`.
- Artifacts saved under the workspace story directory.

### Startup Notes

If you see ClassCastException errors referencing `RestartClassLoader`, disable Spring Boot devtools restart:

```bash
export SPRING_APPLICATION_JSON='{"spring":{"devtools":{"restart":{"enabled":false}}}}'
```

Or set it in `application.yml` under `spring.devtools.restart.enabled`.

### Dummy File-Bridge Mode

```bash
./run-file-bridge.sh
```

The app writes:
- `${BRIDGE_ROOT}/{provider}/{correlationId}/DummyLLMPrompt.md`
- `${BRIDGE_ROOT}/{provider}/{correlationId}/DummyLLMResponse.md`

Defaults:
- Poll interval: 250ms (set `-Dorchestration.llm.dummy.poll-ms=2000` for 2s)
- Timeout: 900000ms (15 minutes)

Paste the response into `DummyLLMResponse.md` and the app will pick it up on the next poll.

### LLM Mode (Gemini)

```bash
export GEMINI_API_KEY=your-key-here
./run-llm-gemini.sh
```

### LLM Mode (Groq)

```bash
export GROQ_API_KEY=your-key-here
./run-llm-groq.sh
```
