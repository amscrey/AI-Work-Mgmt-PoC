#!/bin/bash
# scaffold-role.sh - Deterministic role directory scaffolding
# Usage: ./scaffold-role.sh <role-name> [roles-dir]
#
# Creates a new role directory with ROLE.md template.
# Role name must be kebab-case (lowercase letters, numbers, hyphens).

set -euo pipefail

ROLE_NAME="${1:?Usage: scaffold-role.sh <role-name> [roles-dir]}"
ROLES_DIR="${2:-.agent/roles}"

# Validate kebab-case: lowercase, numbers, hyphens, no leading/trailing/double hyphens
if [[ ! "$ROLE_NAME" =~ ^[a-z][a-z0-9]*(-[a-z0-9]+)*$ ]]; then
  echo "ERROR: Role name must be kebab-case (e.g., 'orchestrator', 'code-reviewer')" >&2
  echo "  - Must start with lowercase letter" >&2
  echo "  - Only lowercase letters, numbers, and single hyphens" >&2
  echo "  - No leading, trailing, or consecutive hyphens" >&2
  exit 1
fi

ROLE_PATH="$ROLES_DIR/$ROLE_NAME"

if [[ -d "$ROLE_PATH" ]]; then
  echo "ERROR: Role already exists: $ROLE_PATH" >&2
  exit 1
fi

# Create directory
mkdir -p "$ROLE_PATH"

# Create ROLE.md with template
cat > "$ROLE_PATH/ROLE.md" << EOF
---
name: $ROLE_NAME
description: TODO - describe this role's primary responsibility and authority level
---

# ${ROLE_NAME//-/ }

Brief description of this role.

## Responsibilities
- Primary responsibility 1
- Primary responsibility 2

## Authority
- **Can modify**: Which authoritative files
- **Can create**: Which file types
- **Can transition**: Which state changes
- **Cannot**: Explicit limitations

## Skills Used
- \`skill-name\` - How this role uses it

## File Naming
- Artifacts: \`<name>__${ROLE_NAME}__<timestamp>.<ext>\`
- Comments: \`comment__${ROLE_NAME}__<timestamp>.md\`

## Interaction Patterns
- **With humans**: How this role interacts with humans
- **With other roles**: Coordination patterns
- **With workspace**: Read/write patterns

## Logging Requirements
Must log:
- Activity type 1
- Activity type 2

## Example Session
1. Trigger event
2. Role action
3. Result
EOF

echo "Created: $ROLE_PATH/ROLE.md"
echo ""
echo "Next steps:"
echo "  1. Edit $ROLE_PATH/ROLE.md"
echo "  2. Update the description in frontmatter"
echo "  3. Define specific authority boundaries"
