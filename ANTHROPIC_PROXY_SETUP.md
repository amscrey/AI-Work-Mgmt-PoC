# Anthropic Corporate Proxy Configuration

## The Problem

When using a corporate proxy for Anthropic API calls, you may see a `404 Not Found` error even though your curl command works.

## Root Cause

LangChain4j's `AnthropicChatModel` appends `/messages` to the `baseUrl`, **NOT** `/v1/messages`.

### URL Construction

**Standard Anthropic API:**
- Base URL: `https://api.anthropic.com/v1`
- LangChain4j calls: `https://api.anthropic.com/v1/messages` ✅

**Corporate Proxy - WRONG Configuration:**
- Your curl: `$ANTHROPIC_BASE_URL/v1/messages`
- Set `ANTHROPIC_BASE_URL=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud`
- LangChain4j calls: `https://ai-model-proxy.aks-ur-prd-internal.8451.cloud/messages` ❌
- Result: **404 Not Found**

**Corporate Proxy - CORRECT Configuration:**
- Your curl: `$ANTHROPIC_BASE_URL/messages` (note: no /v1 in curl now)
- Set `ANTHROPIC_BASE_URL=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud/v1`
- LangChain4j calls: `https://ai-model-proxy.aks-ur-prd-internal.8451.cloud/v1/messages` ✅
- Result: **200 OK**

## Solution

Set your environment variable to **include** the `/v1` path:

```bash
export ANTHROPIC_BASE_URL=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud/v1
```

## Verification

After setting the correct base URL, check the application startup logs:

```
INFO  - Creating Anthropic ChatModel: model=claude-4.5-sonnet, baseUrl=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud/v1
INFO  - Anthropic API will call: POST https://ai-model-proxy.aks-ur-prd-internal.8451.cloud/v1/messages
```

If you see a warning like this, your configuration is wrong:
```
WARN  - WARNING: baseUrl does not end with '/v1'. Anthropic API calls go to '{baseUrl}/messages'...
```

## Testing with curl

After updating your environment variable, test with curl:

```bash
# Note: URL now ends with /messages (no /v1 before it)
curl -i "$ANTHROPIC_BASE_URL/messages" \
  --header "x-api-key: $ANTHROPIC_API_KEY" \
  --header "anthropic-version: 2023-06-01" \
  --header "content-type: application/json" \
  --data '{
    "model": "claude-4.5-sonnet",
    "max_tokens": 64,
    "messages": [
      {"role": "user", "content": "Reply with the single word OK"}
    ]
  }'
```

This should match exactly what LangChain4j will call.
