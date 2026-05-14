#!/usr/bin/env bash
set -euo pipefail

API_BASE=${API_BASE:-http://localhost:8080/api/v1}
REQUESTED_BY=${REQUESTED_BY:-human}
STORY_ID=${STORY_ID:-}
PROMPT_FILE=${PROMPT_FILE:-$(dirname "$0")/prompt.txt}
OUT_DIR=${OUT_DIR:-$(dirname "$0")}

if [ ! -f "$PROMPT_FILE" ]; then
  echo "Prompt file not found: $PROMPT_FILE" >&2
  exit 1
fi

URL="$API_BASE/orchestration/prompt?requestedBy=$REQUESTED_BY"
if [ -n "$STORY_ID" ]; then
  URL="$URL&storyId=$STORY_ID"
fi

RESPONSE_JSON="$OUT_DIR/response.json"
ASSISTANT_MD="$OUT_DIR/AssistantResponse.md"

curl -sS -X POST "$URL" \
  -H 'Content-Type: text/plain' \
  --data-binary "@${PROMPT_FILE}" \
  -o "$RESPONSE_JSON"

# Pass the response and assistant file paths as argv to the embedded python program
python3 - "$RESPONSE_JSON" "$ASSISTANT_MD" <<'PY'
import json
import sys
from pathlib import Path

response_path = Path(sys.argv[1])
assistant_path = Path(sys.argv[2])

try:
    data = json.loads(response_path.read_text())
except Exception:
    assistant_path.write_text("# Assistant Response\n\nFailed to parse response.json\n")
    raise

assistant = data.get("assistantResponse", {}) or {}
summary = assistant.get("summary") or "(no summary)"
format_hint = assistant.get("format") or "markdown"
highlights = assistant.get("highlights") or []
references = assistant.get("references") or []

lines = ["# Assistant Response", "", f"Format: {format_hint}", "", "## Summary", "", summary, ""]

if highlights:
    lines.extend(["## Highlights", ""])
    lines.extend([f"- {item}" for item in highlights])
    lines.append("")

if references:
    lines.extend(["## References", ""])
    for ref in references:
        ref_type = ref.get("type", "reference")
        story_id = ref.get("storyId") or ""
        ref_id = ref.get("id") or ""
        name = ref.get("name") or ""
        created_by = ref.get("createdBy") or ""
        created_at = ref.get("createdAt") or ""
        parts = [p for p in [ref_type, story_id, name, ref_id, created_by, created_at] if p]
        lines.append(f"- {' | '.join(parts)}")
    lines.append("")

assistant_path.write_text("\n".join(lines))
PY

echo "Saved response: $RESPONSE_JSON"
echo "Saved assistant response: $ASSISTANT_MD"
