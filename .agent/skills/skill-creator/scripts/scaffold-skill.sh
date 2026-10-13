#!/bin/bash
# scaffold-skill.sh - Deterministic skill directory scaffolding
# Usage: ./scaffold-skill.sh <skill-name> [skills-dir]
#
# Creates a new skill directory with SKILL.md template.
# Skill name must be kebab-case (lowercase letters, numbers, hyphens).

set -euo pipefail

SKILL_NAME="${1:?Usage: scaffold-skill.sh <skill-name> [skills-dir]}"
SKILLS_DIR="${2:-.agent/skills}"

# Validate kebab-case: lowercase, numbers, hyphens, no leading/trailing/double hyphens
if [[ ! "$SKILL_NAME" =~ ^[a-z][a-z0-9]*(-[a-z0-9]+)*$ ]]; then
  echo "ERROR: Skill name must be kebab-case (e.g., 'my-skill', 'code-review')" >&2
  echo "  - Must start with lowercase letter" >&2
  echo "  - Only lowercase letters, numbers, and single hyphens" >&2
  echo "  - No leading, trailing, or consecutive hyphens" >&2
  exit 1
fi

SKILL_PATH="$SKILLS_DIR/$SKILL_NAME"

if [[ -d "$SKILL_PATH" ]]; then
  echo "ERROR: Skill already exists: $SKILL_PATH" >&2
  exit 1
fi

# Create directory
mkdir -p "$SKILL_PATH"

# Create SKILL.md with template
cat > "$SKILL_PATH/SKILL.md" << EOF
---
name: $SKILL_NAME
description: TODO - describe what this skill does AND when to use it
---

# ${SKILL_NAME//-/ }

Brief description of this skill.

## Input
- What the skill needs

## Output
- What the skill produces

## Process

### Phase 1: [Name]
1. Step 1
2. Step 2

### Phase 2: [Name]
1. Step 1
2. Step 2

## Completeness Checklist
- [ ] Checkpoint 1
- [ ] Checkpoint 2

## Rules
1. **ALWAYS** ...
2. **NEVER** ...
EOF

echo "Created: $SKILL_PATH/SKILL.md"
echo ""
echo "Next steps:"
echo "  1. Edit $SKILL_PATH/SKILL.md"
echo "  2. Update the description in frontmatter"
echo "  3. Add templates/ or scripts/ directories if needed"
