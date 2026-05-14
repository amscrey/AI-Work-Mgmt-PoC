# LLM Startup and Prompt Routing Overview

This document summarizes how the app starts in LLM mode, how it decides which model/provider to use, how usage is tracked, and how prompts reach either a real LLM or the dummy file-bridge.

## 1) Startup Flow (LLM Mode Focus)

```
App start
├─ Read config (application.yml + env + system properties)
├─ OrchestrationConfig.validate()
│  └─ Logs execution mode: template | llm
├─ LLM provider config loaded (OrchestrationLlmProperties)
├─ LLM registry built (DefaultLlmProviderRegistry)
│  └─ Creates LLMChatClient per provider
├─ RoutingLlmChatClient wired as primary
└─ OrchestrationService ready (StateGraph compiled)
```

## 2) How the App Chooses the LLM Provider/Model

Decision order:

1. Execution mode:
   - `template` -> template nodes (no LLM calls)
   - `llm` -> LLM nodes
2. Provider resolution:
   - `orchestration.llm.default-provider` (e.g., anthropic, gemini, groq, dummy)
   - role mappings can override per-role provider
3. LLM routing:
   - `RoutingLlmChatClient` resolves provider per role
   - selected provider client handles the call

## 3) Tracking and Logging

| Concern | Where it is tracked | Notes |
|---|---|---|
| Execution mode | `OrchestrationConfig.validate()` | Logs resolved mode at startup |
| Provider selection | `RoutingLlmChatClient` | Uses role mappings and defaults |
| Token usage (story) | `StoryTokenUsageService` | Writes `llm-usage.jsonl` + summary |
| Token usage (project) | `ProjectTokenUsageService` | Aggregates story usage |

Usage files (per story):
- `{prioritization}/{storyId}/usage/llm-usage.jsonl`
- `{prioritization}/{storyId}/usage/llm-usage-summary.json`

## 4) Prompt Routing (LLM vs Dummy)

```
POST /api/v1/orchestration/prompt
└─ OrchestrationController
   └─ OrchestrationServiceImpl.orchestrate()
      ├─ ContextAssemblyService (story + tasks + refs)
      ├─ OrchestrationState + StateGraph
      └─ LLM Nodes (if llm mode)
         └─ RoutingLlmChatClient
            └─ Provider client
               ├─ Real provider (Anthropic/Gemini/Groq)
               └─ DummyChatClient (file-bridge/clipboard/fixture/instant)
```

## 5) Dummy File-Bridge Mode

Expected behavior:

- Prompt file written:
  - `{WORKSPACE_ROOT}/dummy-bridge/{provider}/{correlationId}/DummyLLMPrompt.md`
- Response file expected:
  - `{WORKSPACE_ROOT}/dummy-bridge/{provider}/{correlationId}/DummyLLMResponse.md`
- The app polls for the response until timeout.

Defaults:
- Poll interval: 250ms (`-Dorchestration.llm.dummy.poll-ms`)
- Timeout: 900000ms / 15 minutes (`-Dorchestration.llm.dummy.timeout-ms`)

## 6) Diagnostics Checklist (File-Bridge)

Use these logs to confirm correct routing:

- Startup log:
  - `Orchestration execution mode resolved: llm`
- Dummy bridge log:
  - `Dummy bridge ready: mode=file-bridge, root=.../dummy-bridge`
- Request log:
  - `Dummy file-bridge request created: provider=..., correlationId=..., promptPath=..., responsePath=...`

If these do not appear, LLM routing is likely not hitting the dummy client (mode or provider mismatch).

## 7) Common Misconfigurations

| Symptom | Likely cause | Fix |
|---|---|---|
| No dummy bridge directory | Not in `llm` mode or provider != `dummy` | Set `execution-mode=llm` and default provider to `dummy` |
| Immediate response without file | Dummy mode not active | Confirm dummy provider and mode |
| Timeout waiting for response | Response file not created | Write `DummyLLMResponse.md` in the same correlation directory |

