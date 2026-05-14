# REST API Conventions

## Base Path

All endpoints use the base path:

- `/api/v1`

## Health Endpoint

- `GET /api/v1/health`
- Response: `{status, version, buildTime, uptimeSeconds}`

## DTO Conventions

- IDs are strings: `storyId`, `taskId`, `commentId`
- Enums serialized as uppercase names (`TODO`, `IN_PROGRESS`, etc.)
- Timestamps serialized as ISO-8601 UTC strings

## Error Payload

All errors use a consistent payload:

```json
{
  "timestamp": "2026-04-05T21:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Story not found: STORY-123",
  "path": "/api/v1/stories/STORY-123",
  "correlationId": "..."
}
```

## Correlation Id

If a request provides an `X-Correlation-Id` header, it is echoed in error responses.
Otherwise a new UUID is generated.

## Work Item Endpoints

### Stories
- `POST /api/v1/stories`
- `GET /api/v1/stories/{storyId}`
- `GET /api/v1/stories?state=&prioritization=`
- `PATCH /api/v1/stories/{storyId}`
- `POST /api/v1/stories/{storyId}/transition`

### Tasks
- `POST /api/v1/tasks`
- `GET /api/v1/tasks/{taskId}`
- `GET /api/v1/tasks?storyId=&state=`
- `PATCH /api/v1/tasks/{taskId}`
- `POST /api/v1/tasks/{taskId}/transition`
- `POST /api/v1/tasks/{taskId}/assign`
- `DELETE /api/v1/tasks/{taskId}`

### Comments
- `POST /api/v1/comments`
- `GET /api/v1/comments/{commentId}`
- `GET /api/v1/stories/{storyId}/comments`
- `DELETE /api/v1/comments/{commentId}`

## Orchestration Endpoints

- `POST /api/v1/orchestration/runs`
- `POST /api/v1/orchestration/prompt` (text/plain body, query params: requestedBy, optional storyId)
- `GET /api/v1/orchestration/runs/{executionId}`
- `GET /api/v1/stories/{storyId}/orchestration`

### assistantResponse

Orchestration responses include `assistantResponse` with `format: "markdown"`.
The `summary` and `highlights` fields are Markdown-capable strings.

## Usage Endpoints

- `GET /api/v1/stories/{storyId}/usage`
