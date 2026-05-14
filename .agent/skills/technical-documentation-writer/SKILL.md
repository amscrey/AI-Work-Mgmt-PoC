---
name: technical-documentation-writer
description: Write clear, accurate technical documentation including JavaDocs, API docs, README files, architecture docs, and how-to guides. Use when documenting code, APIs, system architecture, or creating developer-facing documentation.
---

# Technical Documentation Writer

Creates comprehensive, accurate technical documentation for developers, users, and maintainers. Focuses on clarity, precision, and searchability.

## Input
- Source code (Java, JavaScript, etc.)
- Architectural designs
- Domain models
- API specifications
- Configuration files
- Existing documentation to update
- User requirements

## Output
- JavaDoc comments
- API documentation (OpenAPI, Markdown)
- README files
- Architecture documentation
- How-to guides
- Configuration guides
- Troubleshooting guides
- Release notes
- Contributing guidelines

---

## Process

### Phase 1: Understand What to Document
1. Read source code to understand functionality
2. Read architectural designs
3. Read domain model definitions
4. Identify public APIs and interfaces
5. Identify configuration options
6. Understand user workflows
7. Note complex or non-obvious logic

### Phase 2: Plan Documentation Structure
1. Determine documentation type needed
2. Choose appropriate format (JavaDoc, Markdown, etc.)
3. Identify target audience (developers, end-users, operators)
4. Plan section organization
5. Identify code examples needed
6. Plan diagrams or screenshots needed

### Phase 3: Write Documentation
1. Start with overview/summary
2. Document purpose and usage
3. Add code examples
4. Document parameters and return values
5. Document exceptions and error cases
6. Add configuration examples
7. Include troubleshooting tips
8. Add cross-references

### Phase 4: Review and Refine
1. Check accuracy against code
2. Verify examples work
3. Check for completeness
4. Ensure clarity and readability
5. Fix grammar and spelling
6. Add missing links or references
7. Update table of contents

---

## Documentation Types

### JavaDoc Comments

**Purpose**: Document Java classes, methods, fields

**Format**:
```java
/**
 * Creates a new story in the workspace.
 *
 * <p>Stories are created in the {@code TODO} state and {@code BACKLOG}
 * prioritization by default. The story is validated before creation.
 *
 * @param command the story creation command containing title, summary,
 *                and acceptance criteria
 * @return the created story with generated ID
 * @throws ValidationException if the command is invalid
 * @throws WorkspaceException if the story cannot be saved
 * @see Story
 * @see CreateStoryCommand
 */
public Story createStory(CreateStoryCommand command)
    throws ValidationException, WorkspaceException {
    // implementation
}
```

**Key Elements**:
- Summary sentence (first sentence)
- Detailed description
- `@param` for each parameter
- `@return` for return value
- `@throws` for each exception
- `@see` for related classes/methods
- `@since` for version added
- `@deprecated` if deprecated

---

### API Documentation

**Purpose**: Document REST APIs, service interfaces

**Format** (Markdown):
```markdown
## Create Story

Creates a new story in the backlog.

**Endpoint**: `POST /api/stories`

**Request Body**:
```json
{
  "title": "Dashboard UI Redesign",
  "summary": "Build responsive dashboard interface",
  "author": "john.doe",
  "acceptanceCriteria": [
    "Dashboard displays last 30 days",
    "Charts are interactive"
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": "STORY-123-dashboard-ui",
  "title": "Dashboard UI Redesign",
  "state": "todo",
  "prioritization": "backlog",
  "createdAt": "2026-03-30T10:00:00Z"
}
```

**Errors**:
- `400 Bad Request`: Invalid request body
- `500 Internal Server Error`: Server error

**Example**:
```bash
curl -X POST http://localhost:8080/api/stories \
  -H "Content-Type: application/json" \
  -d @request.json
```
```

---

### README Files

**Purpose**: Project overview, setup, usage

**Structure**:
```markdown
# Project Name

Brief description of what this project does.

## Features

- Feature 1
- Feature 2
- Feature 3

## Installation

### Prerequisites

- Java 17+
- Maven 3.8+

### Setup

```bash
git clone https://github.com/user/repo.git
cd repo
mvn clean install
```

## Configuration

Edit `application.yml`:

```yaml
workspace:
  root: /path/to/workspace
```

## Usage

### Basic Example

```java
WorkspaceService service = new WorkspaceService(config);
Story story = service.createStory(command);
```

### Advanced Usage

[...]

## API Documentation

See [API.md](API.md) for full API reference.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## License

MIT License
```

---

### Architecture Documentation

**Purpose**: Explain system design and structure

**Structure**:
```markdown
# Architecture Overview

## System Context

[Diagram showing external systems]

## Component Architecture

```mermaid
graph TB
    A[Presentation Layer] --> B[Application Layer]
    B --> C[Domain Layer]
    B --> D[Infrastructure Layer]
```

## Domain Layer

Pure Java with no framework dependencies.

Contains:
- **Entities**: Story, Task, Artifact
- **Value Objects**: StoryId, TaskId
- **Enumerations**: WorkflowState, Role
- **Repository Interfaces**: StoryRepository

## Application Layer

Orchestrates use cases.

Contains:
- **Services**: StoryService, TaskService
- **Commands**: CreateStoryCommand
- **Transaction boundaries**

[...]
```

---

### How-To Guides

**Purpose**: Step-by-step task completion

**Structure**:
```markdown
# How to Transition a Story State

This guide explains how to move a story through workflow states.

## Prerequisites

- Story must exist in the workspace
- You must have appropriate role permissions

## Steps

### 1. Verify Current State

```java
Story story = storyRepository.findById(storyId).orElseThrow();
WorkflowState currentState = story.getState();
```

### 2. Check Transition Validity

```java
boolean canTransition = currentState.canTransitionTo(
    WorkflowState.IN_PROGRESS,
    Role.ORCHESTRATOR
);
```

### 3. Perform Transition

```java
story.transitionTo(WorkflowState.IN_PROGRESS, Role.ORCHESTRATOR);
storyRepository.save(story);
```

## Common Issues

**InvalidTransitionException**: The requested transition is not allowed.
- Check role permissions
- Verify current state
- Review transition rules

## Next Steps

- [How to Add Tasks to a Story](add-tasks.md)
- [How to Create Artifacts](create-artifacts.md)
```

---

## Technical Writing Principles

### Clarity

- Use simple, direct language
- Avoid jargon unless necessary
- Define terms on first use
- Use active voice
- Keep sentences short

❌ **Bad**: "The utilization of the aforementioned methodology facilitates..."
✅ **Good**: "Use this method to..."

---

### Accuracy

- Test all code examples
- Verify against current codebase
- Update docs when code changes
- Include version information
- Note platform-specific details

---

### Completeness

- Document all public APIs
- Include examples for common use cases
- Document error conditions
- Explain non-obvious behavior
- Provide troubleshooting tips

---

### Organization

- Use consistent heading structure
- Group related information
- Provide table of contents for long docs
- Use lists for multiple items
- Include cross-references

---

### Searchability

- Use descriptive headings
- Include keywords in text
- Tag appropriately
- Use consistent terminology
- Avoid synonyms (pick one term and stick with it)

---

## Code Example Best Practices

### Make Examples Runnable

❌ **Bad**:
```java
service.doSomething(params);
```

✅ **Good**:
```java
WorkspaceConfig config = new WorkspaceConfig(Paths.get("/workspace"));
StoryService service = new StoryServiceImpl(config);
CreateStoryCommand command = new CreateStoryCommand(
    "Dashboard UI",
    "Build dashboard",
    "john.doe",
    List.of("Criterion 1", "Criterion 2")
);
Story story = service.createStory(command);
```

---

### Show Error Handling

```java
try {
    story.transitionTo(WorkflowState.DONE, Role.ORCHESTRATOR);
} catch (InvalidTransitionException e) {
    System.err.println("Transition failed: " + e.getMessage());
    // Handle error appropriately
}
```

---

### Include Context

```java
// Create a story in the backlog
Story story = new Story(
    StoryId.generate(),
    "Dashboard UI",
    WorkflowState.TODO,
    PrioritizationState.BACKLOG,
    // ...
);

// Later, when work begins:
story.transitionTo(WorkflowState.IN_PROGRESS, Role.ORCHESTRATOR);
```

---

## Documentation Patterns

### The Four Types (Divio Documentation System)

1. **Tutorial**: Learning-oriented, step-by-step for beginners
2. **How-To Guide**: Task-oriented, specific problem solving
3. **Reference**: Information-oriented, technical description
4. **Explanation**: Understanding-oriented, context and design

Choose the right type for your audience and purpose.

---

## Completeness Checklist
- □ Code examples are tested and work?
- □ All public APIs documented?
- □ Parameters and return values explained?
- □ Error conditions documented?
- □ Configuration options listed?
- □ Cross-references included?
- □ Grammar and spelling checked?
- □ Headings follow consistent structure?
- □ Table of contents added (if needed)?
- □ Version/date information included?

## Rules
1. **ALWAYS** test code examples before including them
2. **ALWAYS** document parameters, return values, and exceptions
3. **ALWAYS** use clear, simple language
4. **ALWAYS** include examples for common use cases
5. **ALWAYS** update docs when code changes
6. **NEVER** copy outdated documentation
7. **NEVER** use jargon without explanation
8. **NEVER** assume knowledge - explain concepts
9. **NEVER** leave TODOs in published docs
10. **ALWAYS** verify accuracy against source code
11. **ALWAYS** use consistent terminology
12. **ALWAYS** structure docs for scanability
13. **ALWAYS** include troubleshooting for error messages
14. **NEVER** write marketing copy in technical docs (that's creative-writer's job)
15. **ALWAYS** target the appropriate audience (developer vs user vs operator)