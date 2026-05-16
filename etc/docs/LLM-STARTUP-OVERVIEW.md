# LLM Startup and Prompt Routing Overview

This document summarizes how the app starts in LLM mode, how it decides which model/provider to use, how usage is tracked, and how prompts reach either a real LLM or the dummy file-bridge.

---

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
├─ Role discovery (.agent/roles/*.md)
│  └─ Writes logs/role-model-startup-report.md
├─ LLM configuration snapshot
│  └─ Writes logs/llm-startup-report.md
└─ OrchestrationService ready (StateGraph compiled)
```

---

## 2) Provider Configuration

### Available Providers

The platform supports multiple LLM providers configured in `src/main/resources/application.yml`:

```yaml
orchestration:
  llm:
    default-provider: anthropic
    providers:
      anthropic:
        api-key: ${ANTHROPIC_API_KEY:}
        base-url: ${ANTHROPIC_BASE_URL:}
        model: ${MODEL_ANTHROPIC:claude-sonnet-4-5-20250514}
      gemini:
        api-key: ${GEMINI_API_KEY:}
        base-url: ${GEMINI_BASE_URL:}
        model: ${MODEL_GEMINI:gemini-2.0-flash}
      groq:
        api-key: ${GROQ_API_KEY:}
        base-url: ${GROQ_BASE_URL:}
        model: ${MODEL_GROQ:llama-3.3-70b-versatile}
      dummy:
        model: ${MODEL_DUMMY:file-bridge}
    dummy-providers: [dummy]
```

### Real vs Stubbed Clients

**The platform automatically chooses the right client type**:

| Condition | Client Type | Behavior |
|-----------|-------------|----------|
| ✅ API key provided | **Real LLM Client** | Makes actual API calls to LLM provider |
| ⚠️ API key missing/blank | **Stubbed Client** | Returns fake response: `"stubbed-response:provider:model"` |
| 🔧 Dummy provider | **Dummy Client** | File-bridge/clipboard/fixture mode |

**Examples**:

```bash
# Real Anthropic API calls
export ANTHROPIC_API_KEY=sk-ant-your-key-here
./quickstart/run-llm-anthropic.sh
# → Uses RealLlmChatClient with AnthropicChatModel

# Stubbed responses (no API key)
./quickstart/run-llm-anthropic.sh
# → Uses StubbedProviderChatClient
# → Returns: "stubbed-response:anthropic:claude-sonnet-4-5-20250514"
```

### Model Environment Variables

Model names and base URLs are now configurable via environment variables:

| Environment Variable | Default Value | Description |
|---------------------|---------------|-------------|
| `MODEL_ANTHROPIC` | `claude-sonnet-4-5-20250514` | Anthropic Claude model |
| `MODEL_GEMINI` | `gemini-2.0-flash` | Google Gemini model |
| `MODEL_GROQ` | `llama-3.3-70b-versatile` | Groq-hosted model |
| `MODEL_DUMMY` | `file-bridge` | Dummy mode type |
| `ANTHROPIC_BASE_URL` | (none - uses official API) | Custom Anthropic API endpoint |
| `GEMINI_BASE_URL` | (none - uses official API) | Custom Gemini API endpoint |
| `GROQ_BASE_URL` | `https://api.groq.com/openai/v1` | Custom Groq API endpoint |

**Example usage**:
```bash
export MODEL_ANTHROPIC=claude-opus-4-6-20250514
export MODEL_GEMINI=gemini-2.0-flash-lite
export MODEL_GROQ=llama-3.1-8b-instant
./quickstart/run-llm-anthropic.sh
```

**Corporate proxy example**:
```bash
export ANTHROPIC_API_KEY=your-proxy-key
export ANTHROPIC_BASE_URL=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud
export MODEL_ANTHROPIC=claude-sonnet-4-5-20250514
./quickstart/run-llm-anthropic.sh
```

---

## 3) How the App Chooses the LLM Provider/Model

Decision order:

### Step 1: Execution Mode
- `template` → template nodes (no LLM calls)
- `llm` → LLM nodes

### Step 2: Provider Resolution
- Default: `orchestration.llm.default-provider` (e.g., anthropic, gemini, groq, dummy)
- Override: Role mappings can override per-role provider

### Step 3: Model Selection
- Provider-specific model from environment variable or default
- Role-specific model override (if configured in role mappings)

### Step 4: LLM Routing
- `RoutingLlmChatClient` resolves provider per role
- Selected provider client handles the call

---

## 4) Role-to-Model Mapping

### Default Behavior (No Role Mappings)

**All 12 roles use the default provider and model**:
- Default Provider: `anthropic`
- Default Model: `claude-sonnet-4-5-20250514`

### Available Roles

Discovered at startup from `.agent/roles/`:

| Role | Description |
|------|-------------|
| `orchestrator` | Workflow coordination, work item management |
| `researcher` | Research, requirements gathering, analysis |
| `designer` | UX/UI design, wireframes, prototypes |
| `coder` | Code implementation |
| `tester` | Testing, QA, bug reporting |
| `documenter` | Technical documentation |
| `logician` | Rules, algorithms, logic |
| `creative-writer` | Marketing copy, content |
| `artist` | Image generation, visual assets |
| `art-designer` | UX/UI + AI visuals |
| `game-rules-designer` | Game mechanics, rules |
| `reviewer` | Code review, quality assessment |

### Configuring Role-Specific Mappings

Add a `role-mappings` section in `application.yml` to assign different models to different roles:

```yaml
orchestration:
  llm:
    default-provider: anthropic

    role-mappings:
      # High-complexity roles → Claude Sonnet 4.5
      orchestrator:
        provider: anthropic
        model: claude-sonnet-4-5-20250514
      coder:
        provider: anthropic
      logician:
        provider: anthropic

      # Medium-complexity roles → Gemini Flash
      researcher:
        provider: gemini
      designer:
        provider: gemini
      creative-writer:
        provider: gemini

      # Fast tasks → Groq
      tester:
        provider: groq
        fallback-providers: [anthropic]
```

**Resolution Order**:
1. Role-specific mapping (if defined)
2. Default provider + provider's model
3. Warnings logged for unmapped roles

---

## 5) Tracking and Logging

| Concern | Where it is tracked | Notes |
|---|---|---|
| Execution mode | `OrchestrationConfig.validate()` | Logs resolved mode at startup |
| Provider selection | `RoutingLlmChatClient` | Uses role mappings and defaults |
| Token usage (story) | `StoryTokenUsageService` | Writes `llm-usage.jsonl` + summary |
| Token usage (project) | `ProjectTokenUsageService` | Aggregates story usage |

### Startup Reports

Two reports are generated at startup in `logs/`:

**1. Role Model Startup Report** (`logs/role-model-startup-report.md`):
- All discovered roles (12 total)
- Role descriptions and keywords
- Work type associations

**2. LLM Startup Report** (`logs/llm-startup-report.md`):
- Active providers and their models
- **Masked API keys** (first 5 + last 4 chars for verification)
- Base URLs (for corporate proxy verification)
- Role-to-provider mappings
- Configuration diagnostics

### Usage Files (per story)

- `{prioritization}/{storyId}/usage/llm-usage.jsonl`
- `{prioritization}/{storyId}/usage/llm-usage-summary.json`

---

## 6) Prompt Routing (LLM vs Dummy)

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

---

## 7) Running in LLM Mode

### Quick Start Scripts (Recommended)

**Anthropic (Claude)**:
```bash
export ANTHROPIC_API_KEY=your-key-here
./quickstart/run-llm-anthropic.sh

# Optional: Override model
export MODEL_ANTHROPIC=claude-opus-4-6-20250514
./quickstart/run-llm-anthropic.sh
```

**Gemini**:
```bash
export GEMINI_API_KEY=your-key-here
./quickstart/run-llm-gemini.sh

# Optional: Override model
export MODEL_GEMINI=gemini-2.0-flash-lite
./quickstart/run-llm-gemini.sh
```

**Groq**:
```bash
export GROQ_API_KEY=your-key-here
./quickstart/run-llm-groq.sh

# Optional: Override model
export MODEL_GROQ=llama-3.1-8b-instant
./quickstart/run-llm-groq.sh
```

### Manual Setup

```bash
# 1. Set API key
export ANTHROPIC_API_KEY=your-key-here

# 2. Optional: Override model
export MODEL_ANTHROPIC=claude-opus-4-6-20250514

# 3. Optional: Set workspace
export WORKSPACE_ROOT=/path/to/workspace
export WORKSPACE_NAME=myproject

# 4. Run with LLM mode enabled
export SPRING_APPLICATION_JSON='{"orchestration":{"execution-mode":"llm"}}'
mvn spring-boot:run
```

---

## 8) Dummy File-Bridge Mode

Expected behavior:

- Prompt file written:
  - `{WORKSPACE_ROOT}/dummy-bridge/{provider}/{correlationId}/DummyLLMPrompt.md`
- Response file expected:
  - `{WORKSPACE_ROOT}/dummy-bridge/{provider}/{correlationId}/DummyLLMResponse.md`
- The app polls for the response until timeout.

Defaults:
- Poll interval: 250ms (`-Dorchestration.llm.dummy.poll-ms`)
- Timeout: 900000ms / 15 minutes (`-Dorchestration.llm.dummy.timeout-ms`)

**Running file-bridge mode**:
```bash
./quickstart/run-file-bridge.sh
```

---

## 9) Diagnostics Checklist

### Verifying LLM Configuration

After startup, check:

1. **Console logs** for provider initialization
2. **`logs/llm-startup-report.md`** for:
   - Active providers and models
   - Role mappings (if configured)
   - Configuration warnings
3. **`logs/role-model-startup-report.md`** for:
   - Discovered roles (should be 12)
   - Role descriptions

### LLM Mode Logs

- Startup log:
  - `Orchestration execution mode resolved: llm`
- Provider log:
  - `LLM provider registered: anthropic (model: claude-sonnet-4-5-20250514)`

### File-Bridge Mode Logs

- Dummy bridge log:
  - `Dummy bridge ready: mode=file-bridge, root=.../dummy-bridge`
- Request log:
  - `Dummy file-bridge request created: provider=..., correlationId=..., promptPath=..., responsePath=...`

If these do not appear, LLM routing is likely not hitting the correct client (mode or provider mismatch).

---

## 10) Common Misconfigurations

| Symptom | Likely cause | Fix |
|---|---|---|
| No dummy bridge directory | Not in `llm` mode or provider != `dummy` | Set `execution-mode=llm` and default provider to `dummy` |
| Immediate response without file | Dummy mode not active | Confirm dummy provider and mode |
| Timeout waiting for response | Response file not created | Write `DummyLLMResponse.md` in the same correlation directory |
| Wrong model being used | Environment variable not exported | Export `MODEL_*` before running script |
| All roles use same model | No role mappings configured | Add `role-mappings` section in `application.yml` |
| Provider not found error | Gemini/Groq without profile | Run with `-Pllm-providers` Maven profile |

---

## 11) Corporate Proxy / Custom Base URLs

### Why You Might Need Custom Base URLs

In enterprise environments, you may route LLM API calls through a **corporate proxy or gateway** instead of calling the official API directly.

**Common reasons**:
- Authentication/authorization management
- Auditing and compliance logging
- Rate limiting and cost controls
- Network security policies

### Configuring Custom Base URLs

**Anthropic Example (Corporate Proxy)**:
```bash
# Your corporate proxy endpoint
export ANTHROPIC_BASE_URL=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud
export ANTHROPIC_API_KEY=your-proxy-auth-key
export MODEL_ANTHROPIC=claude-sonnet-4-5-20250514

./quickstart/run-llm-anthropic.sh
```

**How it works**:
1. Platform creates `AnthropicChatModel` with custom `baseUrl`
2. All API calls go to your proxy instead of `https://api.anthropic.com`
3. Your proxy handles authentication, logging, and routing to real Anthropic API

### Default Base URLs (When Not Specified)

| Provider | Default Base URL |
|----------|------------------|
| Anthropic | `https://api.anthropic.com/v1/messages` |
| Gemini | `https://generativelanguage.googleapis.com` |
| Groq | `https://api.groq.com/openai/v1` |

**You only need to set `*_BASE_URL` if using a custom endpoint.**

---

## 12) Cost Optimization

### Mixed Provider Strategy

Use cheaper/faster models for simple tasks:

```yaml
role-mappings:
  # Complex reasoning → Claude Sonnet 4.5 (most capable)
  orchestrator:
    provider: anthropic
  coder:
    provider: anthropic

  # Simple tasks → Gemini Flash (cheaper, faster)
  researcher:
    provider: gemini
  creative-writer:
    provider: gemini

  # Very fast tasks → Groq (fastest inference)
  tester:
    provider: groq
    fallback-providers: [anthropic]
```

### Model Selection by Task Complexity

- **High complexity**: Claude Opus 4.6 or Sonnet 4.5
- **Medium complexity**: Claude Sonnet 3.5 or Gemini 2.0 Flash
- **Low complexity**: Groq Llama 3.1 8B or Gemini Flash Lite

---

**Last Updated**: 2026-05-14
**Related Docs**:
- `quickstart/README.md` - Quick start guide
- `CLAUDE.md` - Development guide
- `etc/docs/TESTING.md` - Testing guide
- `etc/docs/dummy-llm-bridge.md` - Dummy mode details
