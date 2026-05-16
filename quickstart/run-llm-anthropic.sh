#!/usr/bin/env bash
set -euo pipefail

# Source .env file if it exists (for local development with secrets)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "$SCRIPT_DIR/.env" ]; then
  echo "Loading environment variables from .env..."
  source "$SCRIPT_DIR/.env"
fi

if [ -z "${ANTHROPIC_API_KEY:-}" ]; then
  echo "ERROR: ANTHROPIC_API_KEY is required" >&2
  echo "" >&2
  echo "To set up your API key:" >&2
  echo "  1. Copy the template: cp quickstart/.env.template quickstart/.env" >&2
  echo "  2. Edit quickstart/.env and add your API key" >&2
  echo "  3. Run this script again" >&2
  echo "" >&2
  echo "OR set the environment variable directly:" >&2
  echo "  export ANTHROPIC_API_KEY=your-key-here" >&2
  exit 1
fi

WORKSPACE_ROOT=${WORKSPACE_ROOT:-$(pwd)/AI-Work-Mgmt-LLM-Orch-AddOn}
WORKSPACE_NAME=${WORKSPACE_NAME:-workspace}
MODEL_ANTHROPIC=${MODEL_ANTHROPIC:-claude-sonnet-4-5-20250514}

export WORKSPACE_ROOT
export WORKSPACE_NAME
export MODEL_ANTHROPIC

# Optional: Set custom base URL for corporate proxy
# export ANTHROPIC_BASE_URL=https://ai-model-proxy.aks-ur-prd-internal.8451.cloud
if [ -n "${ANTHROPIC_BASE_URL:-}" ]; then
  export ANTHROPIC_BASE_URL
  echo "Using custom Anthropic base URL: $ANTHROPIC_BASE_URL"
fi

export SPRING_APPLICATION_JSON='{"orchestration":{"execution-mode":"llm","llm":{"default-provider":"anthropic"}},"spring":{"devtools":{"restart":{"enabled":false},"add-properties":false}}}'

mvn spring-boot:run
