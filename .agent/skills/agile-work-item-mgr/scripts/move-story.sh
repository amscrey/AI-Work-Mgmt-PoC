#!/bin/bash
# move-story.sh - Deterministic story directory movement
# Usage: ./move-story.sh <workspace> <story-id> <from-state> <to-state>
#
# Moves a story directory between lifecycle states (backlog/prioritized/done).
# This is a deterministic operation - same inputs always produce same result.

set -euo pipefail

WORKSPACE="${1:?Usage: move-story.sh <workspace> <story-id> <from> <to>}"
STORY_ID="${2:?Missing story-id}"
FROM="${3:?Missing from-state (backlog|prioritized|done)}"
TO="${4:?Missing to-state (backlog|prioritized|done)}"

# Validate states
VALID_STATES="backlog prioritized done"
if [[ ! " $VALID_STATES " =~ " $FROM " ]]; then
  echo "ERROR: Invalid from-state '$FROM'. Must be one of: $VALID_STATES" >&2
  exit 1
fi
if [[ ! " $VALID_STATES " =~ " $TO " ]]; then
  echo "ERROR: Invalid to-state '$TO'. Must be one of: $VALID_STATES" >&2
  exit 1
fi

SOURCE="$WORKSPACE/$FROM/$STORY_ID"
DEST="$WORKSPACE/$TO/$STORY_ID"

# Pre-flight checks
if [[ ! -d "$SOURCE" ]]; then
  echo "ERROR: Source not found: $SOURCE" >&2
  exit 1
fi

if [[ -d "$DEST" ]]; then
  echo "ERROR: Destination already exists: $DEST" >&2
  exit 1
fi

if [[ ! -d "$WORKSPACE/$TO" ]]; then
  echo "ERROR: Target directory does not exist: $WORKSPACE/$TO" >&2
  exit 1
fi

# Perform move
mv "$SOURCE" "$DEST"

echo "MOVED: $FROM/$STORY_ID -> $TO/$STORY_ID"
