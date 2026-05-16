# REST API Curl Examples

Complete curl examples for all REST API endpoints in the AI Work Management Platform.

**Prerequisites:**
- App running on `http://localhost:8080`
- See `quickstart/README.md` for startup instructions

**Quick Navigation:**
- [Health Check](#health-check)
- [Stories](#stories)
- [Tasks](#tasks)
- [Comments](#comments)
- [Orchestration](#orchestration)
- [Usage](#usage)

---

## Health Check

### Check application health
```bash
curl http://localhost:8080/api/v1/health
```

**Response:**
```json
{
  "status": "UP",
  "version": "1.0.0",
  "buildTime": "2026-05-14T12:00:00Z",
  "uptimeSeconds": 3600
}
```

---

## Stories

### Create a story
```bash
curl -X POST http://localhost:8080/api/v1/stories \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-001",
    "title": "Implement user authentication",
    "summary": "Add OAuth 2.0 login functionality",
    "description": "Users should be able to log in using Google or GitHub OAuth",
    "author": "product-owner",
    "prioritization": "PRIORITIZED",
    "estimatedEffort": 13
  }'
```

**Response:**
```json
{
  "storyId": "STORY-001",
  "title": "Implement user authentication",
  "summary": "Add OAuth 2.0 login functionality",
  "workflowState": "TODO",
  "prioritizationState": "PRIORITIZED",
  "author": "product-owner",
  "createdAt": "2026-05-14T12:00:00Z",
  "updatedAt": "2026-05-14T12:00:00Z"
}
```

### Get a story by ID
```bash
curl http://localhost:8080/api/v1/stories/STORY-001
```

### List stories with filters
```bash
# All prioritized stories
curl "http://localhost:8080/api/v1/stories?prioritization=PRIORITIZED"

# All stories in TODO state
curl "http://localhost:8080/api/v1/stories?state=TODO"

# Combine filters
curl "http://localhost:8080/api/v1/stories?prioritization=PRIORITIZED&state=IN_PROGRESS"
```

### Update a story
```bash
curl -X PATCH http://localhost:8080/api/v1/stories/STORY-001 \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Implement user authentication with MFA",
    "estimatedEffort": 21
  }'
```

### Transition story state
```bash
curl -X POST http://localhost:8080/api/v1/stories/STORY-001/transition \
  -H 'Content-Type: application/json' \
  -d '{
    "targetState": "IN_PROGRESS",
    "role": "AGENT"
  }'
```

**Valid state transitions:**
- `TODO` → `IN_PROGRESS`
- `IN_PROGRESS` → `IN_REVIEW`
- `IN_REVIEW` → `DONE` or `IN_PROGRESS`

---

## Tasks

### Create a task
```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "taskId": "TASK-001",
    "storyId": "STORY-001",
    "title": "Setup OAuth provider configuration",
    "description": "Configure OAuth client ID and secret for Google and GitHub",
    "assignedTo": "developer",
    "estimatedEffort": 5
  }'
```

### Get a task by ID
```bash
curl http://localhost:8080/api/v1/tasks/TASK-001
```

### List tasks by story
```bash
curl "http://localhost:8080/api/v1/tasks?storyId=STORY-001"
```

### List tasks by state
```bash
curl "http://localhost:8080/api/v1/tasks?state=IN_PROGRESS"

# Combine filters
curl "http://localhost:8080/api/v1/tasks?storyId=STORY-001&state=PENDING"
```

### Update a task
```bash
curl -X PATCH http://localhost:8080/api/v1/tasks/TASK-001 \
  -H 'Content-Type: application/json' \
  -d '{
    "description": "Configure OAuth client ID and secret for Google, GitHub, and Azure AD"
  }'
```

### Transition task state
```bash
curl -X POST http://localhost:8080/api/v1/tasks/TASK-001/transition \
  -H 'Content-Type: application/json' \
  -d '{
    "targetState": "IN_PROGRESS"
  }'
```

**Valid task states:**
- `PENDING` → `IN_PROGRESS`
- `IN_PROGRESS` → `COMPLETED` or `BLOCKED`
- `BLOCKED` → `IN_PROGRESS`

### Assign a task
```bash
curl -X POST http://localhost:8080/api/v1/tasks/TASK-001/assign \
  -H 'Content-Type: application/json' \
  -d '{
    "assignee": "senior-developer"
  }'
```

### Delete a task
```bash
curl -X DELETE http://localhost:8080/api/v1/tasks/TASK-001
```

---

## Comments

### Create a comment
```bash
# Question comment
curl -X POST http://localhost:8080/api/v1/comments \
  -H 'Content-Type: application/json' \
  -d '{
    "commentId": "comment__agent__2026-05-14T120000Z.md",
    "storyId": "STORY-001",
    "author": "agent",
    "commentType": "QUESTION",
    "content": "Should we support Azure AD in addition to Google and GitHub?"
  }'

# Feedback comment
curl -X POST http://localhost:8080/api/v1/comments \
  -H 'Content-Type: application/json' \
  -d '{
    "commentId": "comment__product-owner__2026-05-14T121000Z.md",
    "storyId": "STORY-001",
    "author": "product-owner",
    "commentType": "FEEDBACK",
    "content": "Yes, Azure AD is required for enterprise customers."
  }'
```

**Comment types:**
- `QUESTION` - Requires response
- `FEEDBACK` - Review or guidance
- `NOTE` - Informational

### Get a comment by ID
```bash
curl http://localhost:8080/api/v1/comments/comment__agent__2026-05-14T120000Z.md
```

### List all comments for a story
```bash
curl http://localhost:8080/api/v1/stories/STORY-001/comments
```

### Delete a comment
```bash
curl -X DELETE http://localhost:8080/api/v1/comments/comment__agent__2026-05-14T120000Z.md
```

---

## Orchestration

### Trigger orchestration with work intent
```bash
curl -X POST http://localhost:8080/api/v1/orchestration/runs \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-001",
    "workIntent": "Research OAuth 2.0 best practices and create implementation plan",
    "requestedBy": "product-owner"
  }'
```

**Response:**
```json
{
  "executionId": "exec-123e4567-e89b-12d3-a456-426614174000",
  "storyId": "STORY-001",
  "status": "COMPLETED",
  "assistantResponse": {
    "format": "markdown",
    "summary": "Researched OAuth 2.0 best practices...",
    "highlights": [
      "Created security checklist artifact",
      "Documented provider setup steps"
    ],
    "references": [
      {
        "type": "artifact",
        "storyId": "STORY-001",
        "id": "oauth-security-checklist__researcher__2026-05-14T120000Z.md",
        "name": "OAuth Security Checklist",
        "createdBy": "researcher",
        "createdAt": "2026-05-14T12:00:00Z"
      }
    ]
  }
}
```

### Send a plain text prompt
```bash
curl -X POST "http://localhost:8080/api/v1/orchestration/prompt?requestedBy=human&storyId=STORY-001" \
  -H 'Content-Type: text/plain' \
  --data-binary "Research OAuth 2.0 security best practices and create a checklist artifact"
```

### Get orchestration execution details
```bash
curl http://localhost:8080/api/v1/orchestration/runs/exec-123e4567-e89b-12d3-a456-426614174000
```

### List all orchestration runs for a story
```bash
curl http://localhost:8080/api/v1/stories/STORY-001/orchestration
```

---

## Usage

### Get token usage summary for a story
```bash
curl http://localhost:8080/api/v1/stories/STORY-001/usage
```

**Response:**
```json
{
  "storyId": "STORY-001",
  "totalPromptTokens": 1500,
  "totalCompletionTokens": 750,
  "totalTokens": 2250,
  "totalCost": 0.0225,
  "byProvider": {
    "anthropic": {
      "model": "claude-sonnet-4-5-20250514",
      "promptTokens": 1500,
      "completionTokens": 750,
      "totalTokens": 2250,
      "estimatedCost": 0.0225
    }
  },
  "byRole": {
    "researcher": {
      "callCount": 1,
      "promptTokens": 1500,
      "completionTokens": 750,
      "totalTokens": 2250
    }
  }
}
```

---

## Common Workflows

### Complete workflow: Create story → Add tasks → Orchestrate → Review

```bash
# 1. Create a story
curl -X POST http://localhost:8080/api/v1/stories \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-100",
    "title": "Database migration plan",
    "summary": "Plan migration from MySQL to PostgreSQL",
    "author": "tech-lead",
    "prioritization": "PRIORITIZED"
  }'

# 2. Add tasks
curl -X POST http://localhost:8080/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "taskId": "TASK-100-1",
    "storyId": "STORY-100",
    "title": "Analyze schema differences",
    "assignedTo": "database-admin"
  }'

curl -X POST http://localhost:8080/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "taskId": "TASK-100-2",
    "storyId": "STORY-100",
    "title": "Create migration scripts",
    "assignedTo": "database-admin"
  }'

# 3. Trigger orchestration
curl -X POST http://localhost:8080/api/v1/orchestration/runs \
  -H 'Content-Type: application/json' \
  -d '{
    "storyId": "STORY-100",
    "workIntent": "Research PostgreSQL migration best practices and create detailed migration plan",
    "requestedBy": "tech-lead"
  }'

# 4. Check orchestration results
curl http://localhost:8080/api/v1/stories/STORY-100/orchestration

# 5. Review token usage
curl http://localhost:8080/api/v1/stories/STORY-100/usage

# 6. Transition story to in-progress
curl -X POST http://localhost:8080/api/v1/stories/STORY-100/transition \
  -H 'Content-Type: application/json' \
  -d '{
    "targetState": "IN_PROGRESS",
    "role": "AGENT"
  }'
```

---

## Error Handling

All errors return consistent JSON:

```json
{
  "timestamp": "2026-05-14T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Story not found: STORY-999",
  "path": "/api/v1/stories/STORY-999",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Common error codes:**
- `400` - Bad Request (invalid JSON, missing required fields)
- `404` - Not Found (story/task/comment doesn't exist)
- `409` - Conflict (invalid state transition)
- `500` - Internal Server Error

---

## Using Scripts

### Quickstart script for orchestration
```bash
cd quickstart

# Edit prompt.txt with your request
echo "Research microservices best practices" > prompt.txt

# Run the script
export API_BASE=http://localhost:8080/api/v1
export REQUESTED_BY=human
export STORY_ID=STORY-001
./run-prompt.sh

# Check outputs
cat response.json
cat AssistantResponse.md
```

---

## Tips

1. **Use jq for pretty JSON:**
   ```bash
   curl http://localhost:8080/api/v1/stories/STORY-001 | jq '.'
   ```

2. **Save responses to files:**
   ```bash
   curl http://localhost:8080/api/v1/stories/STORY-001 > story.json
   ```

3. **Set base URL once:**
   ```bash
   export API_BASE=http://localhost:8080/api/v1
   curl $API_BASE/health
   ```

4. **Add correlation ID for tracking:**
   ```bash
   curl -H 'X-Correlation-Id: my-request-123' \
     http://localhost:8080/api/v1/stories/STORY-001
   ```

---

## See Also

- **Startup Guide**: `quickstart/README.md` - How to start the app with/without LLMs
- **API Reference**: `etc/docs/REST-API.md` - Complete endpoint documentation
- **LLM Configuration**: `etc/docs/LLM-STARTUP-OVERVIEW.md` - Provider setup and troubleshooting
- **Testing**: `etc/docs/TESTING.md` - Running tests and understanding test coverage
