#!/usr/bin/env bash
set -euo pipefail

# Source .env file if it exists (for local development with secrets)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "$SCRIPT_DIR/.env" ]; then
  echo "Loading environment variables from .env..."
  source "$SCRIPT_DIR/.env"
fi

if [ -z "${GROQ_API_KEY:-}" ]; then
  echo "ERROR: GROQ_API_KEY is required" >&2
  echo "" >&2
  echo "To set up your API key:" >&2
  echo "  1. Copy the template: cp quickstart/.env.template quickstart/.env" >&2
  echo "  2. Edit quickstart/.env and add your API key" >&2
  echo "  3. Run this script again" >&2
  exit 1
fi

WORKSPACE_ROOT=${WORKSPACE_ROOT:-$(pwd)/AI-Work-Mgmt-LLM-Orch-AddOn}
WORKSPACE_NAME=${WORKSPACE_NAME:-workspace}
MODEL_GROQ=${MODEL_GROQ:-llama-3.3-70b-versatile}

export WORKSPACE_ROOT
export WORKSPACE_NAME
export MODEL_GROQ

export SPRING_APPLICATION_JSON='{"orchestration":{"execution-mode":"llm","llm":{"default-provider":"groq"}},"spring":{"devtools":{"restart":{"enabled":false},"add-properties":false}}}'

mvn -Pllm-providers spring-boot:run

