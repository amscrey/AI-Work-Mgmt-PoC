#!/usr/bin/env bash
set -euo pipefail

WORKSPACE_ROOT=${WORKSPACE_ROOT:-$(pwd)/AI-Work-Mgmt-LLM-Orch-AddOn}
WORKSPACE_NAME=${WORKSPACE_NAME:-workspace}
BRIDGE_ROOT=${BRIDGE_ROOT:-$WORKSPACE_ROOT/dummy-bridge}

export WORKSPACE_ROOT
export WORKSPACE_NAME
export BRIDGE_ROOT

export SPRING_APPLICATION_JSON='{"orchestration":{"execution-mode":"llm","llm":{"default-provider":"dummy","providers":{"dummy":{"model":"file-bridge"}}}},"spring":{"devtools":{"restart":{"enabled":false},"add-properties":false}}}'

# Prefer environment variable BRIDGE_ROOT (DummyChatClient now prefers env BRIDGE_ROOT).
mvn spring-boot:run

