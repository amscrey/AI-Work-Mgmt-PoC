# Logician AI Agent

## Role
- **Role Name:** Logician AI Agent
- **Function:** Provide the Orchestrator AI Agent with a logically sequenced task breakdown, identify prerequisite decisions, and ensure story planning begins from explicit assumptions rather than unresolved ambiguity.

## Response for Orchestrator Use
The requested effort is suitable for structured planning, but the work must be sequenced carefully because key requirement premises are still missing.

Known inputs:

- digital game based on a simplified **Jaws** board game concept
- supports **2 or 3 players**
- **Java** preferred for backend logic
- frontend approach still undecided
- current goal is **requirements collection** and **story planning**

## Most Useful Information for the Next Prompt
Before task execution can move from discovery into defined backlog creation, the most useful information to collect next is:

### Gameplay Clarification
- Which original mechanics must remain?
- What does “slightly simplified” specifically mean?
- What are the player roles?
- What are the win and loss conditions?
- What actions can players take on a turn?

### Player and Mode Clarification
- Must the game support both 2-player and 3-player play?
- How does gameplay differ between those modes?
- Is multiplayer local, online, or both?
- Are AI-controlled players required?

### Platform Clarification
- Is the first release intended for:
  - web
  - desktop
  - both
- Is a prototype acceptable for version 1?

### Delivery Scope Clarification
- What is the minimum playable product?
- What is out of scope for the first release?
- How much visual polish is expected initially?

## Questions the Human Should Answer Next
To build valid initial requirements, the human should answer these questions first:

1. What exact simplifications should be made from the original board game?
2. Do you want support for both 2-player and 3-player modes in version 1?
3. Will the game be local multiplayer, online multiplayer, or both?
4. Do you want a web frontend or a desktop frontend first?
5. Is the goal a prototype, MVP, or polished release?
6. Should the game include AI opponents?
7. Should players be able to save and resume games?
8. Should the software enforce all rules automatically?

## Frontend Suggestions
Given the Java backend preference, the most reasonable frontend options are:

### Option 1: Web Frontend
- **Frontend:** React
- **Backend:** Java + Spring Boot
- **Recommended when:**
  - online play is likely
  - browser access is preferred
  - long-term flexibility matters

### Option 2: JavaFX Desktop Frontend
- **Frontend:** JavaFX
- **Backend/Game Logic:** Java
- **Recommended when:**
  - rapid prototyping is preferred
  - local play is acceptable
  - a pure Java stack is desirable

### Default Recommendation
If no platform choice has been made yet, the strongest default is:

- **Backend:** Java + Spring Boot
- **Frontend:** React
- **API style:** REST initially
- **Realtime:** add WebSockets only if needed later

## Agents Most Useful for Writing and Prioritizing Stories
The following agents are the most useful for this planning effort:

### Core Agents
- **Orchestrator AI Agent**
  - coordinates the workflow
  - manages handoffs and human checkpoints

- **Business Analyst / Requirements Agent**
  - gathers and structures requirements
  - resolves ambiguity

- **Game Design Agent**
  - formalizes game mechanics and simplifications
  - defines player roles and turn flow

- **Product Owner Agent**
  - prioritizes stories
  - defines MVP boundaries

- **Technical Architect Agent**
  - recommends architecture
  - separates backend, frontend, and game engine concerns

- **QA / Test Strategy Agent**
  - creates testable acceptance criteria
  - identifies edge cases

### Supporting Agents
- **Scrum / Planning Agent**
  - helps with sizing and sequencing
- **UX/UI Agent**
  - helps define screens and interactions

## Task Breakdown with Sequencing
Below is the recommended executable breakdown for **PROJECT-PLANNING**.

## Phase 1: Discovery Setup
### Task 1.1: Confirm planning goals
- capture the purpose of the project
- determine whether the target is:
  - prototype
  - MVP
  - polished product

### Task 1.2: Gather baseline game assumptions
- document current understanding of the original board game adaptation
- identify known and unknown mechanics
- record assumptions explicitly

### Task 1.3: Prepare clarification questionnaire
- compile the minimum decision set for the human
- group questions by:
  - gameplay
  - player count
  - platform
  - scope
  - multiplayer

**Dependency:** none

## Phase 2: Requirements Elicitation
### Task 2.1: Define simplified game rules
- identify preserved mechanics
- identify removed or modified mechanics
- define turn structure
- define victory conditions
- define failure conditions

### Task 2.2: Define player model
- determine whether 2-player and 3-player are both required
- define player roles per mode
- define role assignment rules

### Task 2.3: Define play model
- decide local vs online
- determine whether AI opponents are needed
- determine whether save/load is required

### Task 2.4: Define version 1 scope
- describe minimum playable experience
- identify explicit out-of-scope items
- identify deferred capabilities

**Dependency:** Phase 1 completion

## Phase 3: Solution Framing
### Task 3.1: Recommend frontend direction
- compare:
  - React web app
  - Vue web app
  - JavaFX desktop app
- evaluate fit against:
  - multiplayer mode
  - accessibility
  - implementation speed

### Task 3.2: Define backend approach
- propose Java domain model for game rules
- propose Spring Boot for service/API layer
- identify whether session state is local or server-managed

### Task 3.3: Separate core engine from presentation
- define game engine boundaries
- define UI responsibilities
- define API/interface needs

**Dependency:** Phase 2 completion

## Phase 4: Story Preparation
### Task 4.1: Identify epics
Likely epics include:

- game rules engine
- game setup and session creation
- player turns and actions
- board/user interface
- multiplayer/session management
- persistence/save-load
- testing and validation

### Task 4.2: Draft initial user stories
- convert requirements into story statements
- include acceptance criteria where possible
- flag stories blocked by unresolved decisions

### Task 4.3: Identify dependencies between stories
- isolate foundational stories
- mark architecture prerequisites
- identify stories that can run in parallel

**Dependency:** Phase 3 completion

## Phase 5: Prioritization and Sizing Preparation
### Task 5.1: Define MVP priority rules
- prioritize playable core loop first
- defer polish features
- defer optional infrastructure until justified

### Task 5.2: Prepare stories for estimation
- ensure stories are small enough
- ensure stories are testable
- ensure assumptions are documented

### Task 5.3: Build recommended delivery sequence
Suggested order:

1. rules engine
2. game setup
3. turn handling
4. move validation
5. basic UI
6. multiplayer support
7. save/load
8. polish features

**Dependency:** Phase 4 completion

## Proposed Execution Order
The recommended overall sequence is:

1. confirm project goals
2. collect gameplay clarifications
3. define player and multiplayer model
4. define MVP scope
5. choose frontend direction
6. frame architecture
7. identify epics
8. draft stories
9. prioritize backlog
10. prepare for sizing

## Risks to Track During Task Planning
### Requirement Risks
- simplification scope remains undefined
- player-count support may imply multiple rule paths
- MVP boundary may drift

### Architecture Risks
- frontend choice may be made before platform decisions are settled
- multiplayer complexity may be underestimated
- rule engine and UI may become tightly coupled

### Planning Risks
- stories may be too large for estimation
- acceptance criteria may be weak if rules are unclear
- prioritization may happen before dependencies are mapped

## Recommended Human-in-the-Loop Checkpoints
The Orchestrator should pause for human confirmation at these points:

### Checkpoint 1
After gameplay clarification:
- confirm simplified rules
- confirm player modes
- confirm win conditions

### Checkpoint 2
After scope clarification:
- confirm MVP definition
- confirm out-of-scope items

### Checkpoint 3
After architecture recommendation:
- confirm web vs desktop direction
- confirm frontend stack preference

### Checkpoint 4
After story drafting:
- confirm story priorities
- confirm estimation readiness

## Immediate Next Step
The next prompt should ideally answer these five questions first:

1. What exact simplifications are being made to the Jaws board game?
2. Do you want both 2-player and 3-player support in version 1?
3. Is the game local, online, or both?
4. Do you want web or desktop first?
5. What is the minimum playable version?

## Output Available After Clarification
Once those answers are provided, I can help the Orchestrator produce:

- an initial requirements structure
- epic decomposition
- executable story candidates
- dependency-aware prioritization
- sizing-ready planning inputs
