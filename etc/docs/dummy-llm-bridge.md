# Dummy LLM File Bridge

## Purpose

The dummy file-bridge mode allows a human to supply LLM responses without direct API integration. The app writes a prompt file to disk and waits for a response file to appear.

## Paths

Default bridge root:
- `${WORKSPACE_ROOT}/dummy-bridge` (falls back to `${user.dir}/workspaces/dummy-bridge`)

Override with environment variable (preferred):
- `BRIDGE_ROOT=/path/to/bridge`

Legacy fallback with system property (supported for compatibility):
- `-Dorchestration.llm.dummy.bridge-root=/path/to/bridge`

Per request:
- `{bridgeRoot}/{provider}/{correlationId}/DummyLLMPrompt.md`
- `{bridgeRoot}/{provider}/{correlationId}/DummyLLMResponse.md`

## Prompt Format

The prompt file is Markdown with a small metadata header, followed by the prompt body.

## Response Format

The response file is plain text or Markdown. The full file content is returned as the LLM response.

## Timeouts

Defaults:
- Poll interval: 250ms (`-Dorchestration.llm.dummy.poll-ms`)
- Timeout: 900000ms (15 minutes) (`-Dorchestration.llm.dummy.timeout-ms`)

When the response file appears, the app reads it on the next poll interval and proceeds.

## Token Usage

Token usage is estimated from word counts in the prompt and response. The usage payload is marked as estimated and includes a `contextTokens` entry in raw metadata.

## Mode Toggle

Dummy behavior is selected via the provider model value:
- `file-bridge` (default): write prompt/response files and wait for human response.
- `instant`: return a deterministic dummy response without waiting (useful for automated tests).
- `clipboard`: write a copy/paste friendly prompt and wait for a response file.
- `fixture`: read deterministic responses from fixtures.

## Clipboard Mode

Clipboard mode writes prompt/response files under:
- `{bridgeRoot}/{provider}/clipboard/DummyLLMPrompt.md`
- `{bridgeRoot}/{provider}/clipboard/DummyLLMResponse.md`

Copy the prompt text into your tool of choice and paste the response into `DummyLLMResponse.md`.
