#!/usr/bin/env bash
set -euo pipefail

if [ -z "${GROQ_API_KEY:-}" ]; then
  echo "GROQ_API_KEY is required" >&2
  exit 1
fi

WORKSPACE_ROOT=${WORKSPACE_ROOT:-$(pwd)/AI-Work-Mgmt-LLM-Orch-AddOn}
WORKSPACE_NAME=${WORKSPACE_NAME:-workspace}

export WORKSPACE_ROOT
export WORKSPACE_NAME

export SPRING_APPLICATION_JSON='{"orchestration":{"execution-mode":"llm","llm":{"default-provider":"groq"}},"spring":{"devtools":{"restart":{"enabled":false},"add-properties":false}}}'

mvn -Pllm-providers spring-boot:run

