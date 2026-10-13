#!/bin/bash
# init-workspace.sh - Deterministic workspace initialization
# Usage: ./init-workspace.sh <workspace-root>
#
# Creates the standard directory structure for an AI Work Management workspace.
# This is a deterministic operation - same inputs always produce same structure.

set -euo pipefail

WORKSPACE="${1:?Usage: init-workspace.sh <workspace-root>}"

# Validate path is absolute
if [[ "$WORKSPACE" != /* ]]; then
  echo "ERROR: Workspace path must be absolute: $WORKSPACE" >&2
  exit 1
fi

# Check parent exists
PARENT_DIR=$(dirname "$WORKSPACE")
if [[ ! -d "$PARENT_DIR" ]]; then
  echo "ERROR: Parent directory does not exist: $PARENT_DIR" >&2
  exit 1
fi

# Create main workspace directory if needed
mkdir -p "$WORKSPACE"

# Create lifecycle directories
LIFECYCLE_DIRS=(
  "backlog"
  "prioritized"
  "done"
)

for dir in "${LIFECYCLE_DIRS[@]}"; do
  mkdir -p "$WORKSPACE/$dir"
  touch "$WORKSPACE/$dir/.gitkeep"
  echo "Created: $WORKSPACE/$dir/"
done

# Create activity log directory
mkdir -p "$WORKSPACE/activity-log"
touch "$WORKSPACE/activity-log/.gitkeep"
echo "Created: $WORKSPACE/activity-log/"

# Create README
cat > "$WORKSPACE/README.md" << 'EOF'
# AI Work Management Workspace

This directory is managed by the AI Work Management system.

## Structure

```
workspace/
├── backlog/           # Stories with prioritizationState: BACKLOG
├── prioritized/       # Stories with prioritizationState: PRIORITIZED
├── done/              # Stories with workflowState: DONE
└── activity-log/      # JSONL activity logs
```

## Rules

- Stories move between directories when their state changes
- Directory location = source of truth for prioritization state
- Activity logs are append-only JSONL files
EOF

echo "Created: $WORKSPACE/README.md"
echo ""
echo "Workspace initialized: $WORKSPACE"
