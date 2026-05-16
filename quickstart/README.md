# Quickstart

This quickstart shows how to send a single prompt to the orchestration API.

## Files

- `prompt.txt`: the prompt body to send
- `run-prompt.sh`: sends `prompt.txt` to the API
- `run-file-bridge.sh`: starts the app in dummy file-bridge mode
- `run-llm-anthropic.sh`: starts the app in LLM mode (Anthropic Claude)
- `run-llm-gemini.sh`: starts the app in LLM mode (Gemini)
- `run-llm-groq.sh`: starts the app in LLM mode (Groq)
- **`.env.template`**: template for environment variables (safe to commit)
- **`.env`**: your actual secrets (gitignored, never committed)

## Setup (First Time)

### Option 1: Using .env File (Recommended for Security)

**This prevents accidentally committing API keys to git.**

```bash
# 1. Copy the template
cd quickstart
cp .env.template .env

# 2. Edit .env and add your API keys
nano .env  # or use your favorite editor

# 3. The .env file is already gitignored - it will NEVER be committed
```

**What to configure in `.env`**:
- `ANTHROPIC_API_KEY` - Your Anthropic API key (or corporate proxy key)
- `ANTHROPIC_BASE_URL` - Optional: Your corporate proxy URL
- `WORKSPACE_ROOT` - Where to store work items
- Other optional provider keys (Gemini, Groq)

### Option 2: Manual Environment Variables

If you prefer not to use the `.env` file:

```bash
export WORKSPACE_ROOT=/path/to/workspace
export WORKSPACE_NAME=default
export ANTHROPIC_API_KEY=your-key-here
export ANTHROPIC_BASE_URL=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud  # Optional
```

---

## Run the App

**All run scripts automatically load `.env` if it exists.**

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
- Model names can be customized via environment variables:
  - `MODEL_ANTHROPIC` (default: `claude-sonnet-4-5-20250514`)
  - `MODEL_GEMINI` (default: `gemini-2.0-flash`)
  - `MODEL_GROQ` (default: `llama-3.3-70b-versatile`)
  - `MODEL_DUMMY` (default: `file-bridge`)
- Custom base URLs for corporate proxies:
  - `ANTHROPIC_BASE_URL` (optional - for corporate proxy)
  - `GEMINI_BASE_URL` (optional - for corporate proxy)
  - `GROQ_BASE_URL` (optional - for corporate proxy)

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

---

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

---

## What to Inspect

### Startup

- Console output from `mvn spring-boot:run`
- Health endpoint: `GET /api/v1/health`
- **LLM Startup Report**: `logs/llm-startup-report.md` (includes masked API keys for verification)

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

---

## Expected Output

- JSON response with `executionId` and `status`.
- Artifacts saved under the workspace story directory.

---

## Quick Start Examples

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

### LLM Mode (Anthropic Claude)

**Using .env file (recommended)**:
```bash
# After setting up .env with ANTHROPIC_API_KEY:
./run-llm-anthropic.sh
```

**Using environment variables**:
```bash
export ANTHROPIC_API_KEY=your-key-here
./run-llm-anthropic.sh

# Optional: override model
export MODEL_ANTHROPIC=claude-opus-4-6-20250514
./run-llm-anthropic.sh

# Optional: use corporate proxy
export ANTHROPIC_BASE_URL=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud
export ANTHROPIC_API_KEY=your-proxy-key
./run-llm-anthropic.sh
```

### LLM Mode (Gemini)

**Using .env file (recommended)**:
```bash
# After setting up .env with GEMINI_API_KEY:
./run-llm-gemini.sh
```

**Using environment variables**:
```bash
export GEMINI_API_KEY=your-key-here
./run-llm-gemini.sh

# Optional: override model
export MODEL_GEMINI=gemini-2.0-flash-lite
./run-llm-gemini.sh
```

### LLM Mode (Groq)

**Using .env file (recommended)**:
```bash
# After setting up .env with GROQ_API_KEY:
./run-llm-groq.sh
```

**Using environment variables**:
```bash
export GROQ_API_KEY=your-key-here
./run-llm-groq.sh

# Optional: override model
export MODEL_GROQ=llama-3.1-8b-instant
./run-llm-groq.sh
```

---

## Troubleshooting

### API Key Errors

If you see `ANTHROPIC_API_KEY is required`:

1. **Check if .env exists**: `ls quickstart/.env`
2. **If missing**: `cp quickstart/.env.template quickstart/.env`
3. **Edit .env**: Add your API key
4. **Run again**: `./run-llm-anthropic.sh`

### Startup Errors

If you see ClassCastException errors referencing `RestartClassLoader`, disable Spring Boot devtools restart:

```bash
export SPRING_APPLICATION_JSON='{"spring":{"devtools":{"restart":{"enabled":false}}}}'
```

Or set it in `application.yml` under `spring.devtools.restart.enabled`.

### Security Note

**Never commit your `.env` file!**

The `.gitignore` already protects:
- `quickstart/.env`
- `*.env`
- `**/*-secrets.sh`

Your API keys are safe as long as you use the `.env` file.
