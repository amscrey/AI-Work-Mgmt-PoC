#!/bin/bash
# validate-story-location.sh - Validate story directory matches JSON state
# Usage: ./validate-story-location.sh <workspace> <story-id>
#
# Checks that a story exists in exactly one location and reports its state.

set -euo pipefail

WORKSPACE="${1:?Usage: validate-story-location.sh <workspace> <story-id>}"
STORY_ID="${2:?Missing story-id}"

LOCATIONS=("backlog" "prioritized" "done")
FOUND_IN=""
FOUND_COUNT=0

for loc in "${LOCATIONS[@]}"; do
  if [[ -d "$WORKSPACE/$loc/$STORY_ID" ]]; then
    FOUND_IN="$loc"
    ((FOUND_COUNT++))
  fi
done

if [[ $FOUND_COUNT -eq 0 ]]; then
  echo "ERROR: Story not found in any location: $STORY_ID" >&2
  exit 1
fi

if [[ $FOUND_COUNT -gt 1 ]]; then
  echo "ERROR: Story exists in multiple locations: $STORY_ID" >&2
  for loc in "${LOCATIONS[@]}"; do
    if [[ -d "$WORKSPACE/$loc/$STORY_ID" ]]; then
      echo "  - $loc/$STORY_ID" >&2
    fi
  done
  exit 2
fi

echo "VALID: $STORY_ID is in $FOUND_IN"
