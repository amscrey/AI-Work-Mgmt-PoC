# Dummy LLM File Bridge

CorrelationId: 78e1212a-5f92-4136-8ced-7c226e6a0ff7
Provider: dummy
Role: logician
Timestamp: 2026-04-08T15:48:16.678741Z

---

# Task Planning Prompt
Mode: llm
Work Intent: I would like to start a new effort.
We will need to collect requirements and engage in story planning to size out some stories.

The effort will focus on making a computer game from a slightly simplified version of the jaws board game.  This game will require 2 or 3 players.

I will like for this game to be implemented in java for the backend logic.
I would like suggestions about how to implement the from end.


Please engage the orchestrator to address these concerns in its reply:
1) please respond with the name of its role
2) please indicate what information would be most useful to provide on the next prompt, what questions do I need to answer to get us started to build initial requirements?
3) Please let me know which agents will be most useful in helping us to write and prioritize stories.

Constraints: No real LLM calls during automated tests.

## Story
Id: PROJECT-PLANNING
Title: Project Planning
Summary: Meta work item for project-level planning, research, and coordination activities.
Description: 

### Acceptance Criteria
- (none)

## Instructions
Break down the story into executable tasks with sequencing.
- Respond in Markdown with headings and bullet lists.
- Use short sections and avoid overly long paragraphs.
