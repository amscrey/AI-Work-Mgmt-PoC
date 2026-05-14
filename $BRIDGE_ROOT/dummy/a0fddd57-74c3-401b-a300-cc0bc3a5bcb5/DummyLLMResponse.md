# Tester AI Agent

## Role
- **Role Name:** Tester AI Agent
- **Function:** Support the Orchestrator AI Agent by defining a planning-stage test strategy, identifying validation checkpoints, and ensuring the requirements and story-planning outputs are testable, complete, and low-risk.

## Response for Orchestrator Use
This story is a **project-planning** effort rather than a feature-delivery story, so the primary test objective is not gameplay execution yet.
The test objective is to validate that planning outputs are:

- complete enough to proceed
- internally consistent
- testable
- sequenced appropriately
- traceable to the user’s stated intent

## Test Strategy Summary
The test plan for this story should verify that the planning process produces usable outputs for the next phase.

We should validate:

- requirement discovery coverage
- ambiguity reduction
- architecture-option clarity
- story-writing readiness
- prioritization readiness
- human-in-the-loop checkpoints

## Most Useful Information for the Next Prompt
From a testing and validation perspective, the most useful information to provide next is:

### Gameplay Definition
- What rules from the original Jaws board game are being kept?
- What rules are being simplified or removed?
- What are the player roles?
- What are the turn steps?
- What are the win/loss conditions?

### Mode Definition
- Will version 1 support:
  - 2 players only
  - 3 players only
  - both 2 and 3 players
- Will play be local, online, or both?
- Are AI players required?

### Technical Definition
- Is the target frontend:
  - web
  - desktop
  - undecided
- Is version 1 a prototype or MVP?
- Should the game allow save/load?

### Testability Definition
- Should the system enforce rules automatically?
- Should the game engine be separate from the UI?
- Are auditability or move history needed?

## Questions the Human Should Answer Next
To make future stories testable, the human should answer these questions first:

1. What exact simplifications are planned?
2. Are both 2-player and 3-player modes required in version 1?
3. Is the game local or online?
4. Do you want a web or desktop frontend first?
5. What is the minimum playable scope?
6. Will AI opponents be included?
7. Should rules be enforced entirely by the software?
8. Should game state be saved and resumed?

## Frontend Suggestions from a Testability View
### Preferred Default
- **Backend:** Java + Spring Boot
- **Frontend:** React
- **Why**
  - easier separation of concerns
  - supports API-level testing
  - supports UI testing independently from rules engine
  - fits well for multiplayer growth

### Alternative
- **Frontend:** JavaFX
- **Why**
  - simpler for a contained desktop prototype
  - easier end-to-end testing in a single-language stack for smaller scope
- **Tradeoff**
  - less flexible for web deployment and broader multiplayer access

## Agents Most Useful for Writing and Prioritizing Stories
From a test strategy perspective, the most useful agents are:

### Core Agents
- **Orchestrator AI Agent**
  - coordinates outputs and checkpoints
- **Business Analyst / Requirements Agent**
  - reduces ambiguity in requirements
- **Game Design Agent**
  - formalizes rules to make them testable
- **Product Owner Agent**
  - prioritizes MVP stories
- **Technical Architect Agent**
  - ensures a testable architecture
- **QA / Test Strategy Agent**
  - defines acceptance criteria and edge cases

### Helpful Supporting Agents
- **Scrum / Planning Agent**
  - prepares sizing-ready stories
- **UX/UI Agent**
  - helps validate interaction flows

## Test Plan for PROJECT-PLANNING
## Test Objectives
Verify that the project-planning story results in:

- a clear problem statement
- a usable set of discovery questions
- a list of unresolved assumptions
- a preliminary architecture recommendation
- a clear list of useful supporting agents
- a sequencing approach for story creation and prioritization

## Test Scope
### In Scope
- planning output quality
- completeness of discovery questions
- consistency of recommendations
- readiness for story decomposition
- identification of human decision points

### Out of Scope
- implementation correctness of gameplay
- runtime behavior of the game
- UI rendering
- networking performance
- persistence correctness

## Test Approach
### 1. Static Review Testing
Review planning outputs for:

- completeness
- logical consistency
- missing requirement categories
- contradiction detection

### 2. Traceability Testing
Validate that all outputs trace back to the original ask:

- new effort initiation
- requirements collection
- story planning
- Java backend preference
- frontend recommendation request
- 2 or 3 player support

### 3. Readiness Testing
Confirm the planning outputs are sufficient to begin:

- requirements drafting
- epic definition
- story writing
- prioritization
- rough sizing

### 4. Ambiguity Testing
Check whether major ambiguous phrases are explicitly flagged, such as:

- “slightly simplified”
- “2 or 3 players”
- “frontend suggestions”
- “start a new effort”

### 5. Sequencing Validation
Ensure the proposed order of work is sound:

- clarify requirements first
- select architecture second
- draft stories third
- prioritize and size after clarification

## Key Test Cases
## Test Case Group 1: Role and Response Coverage
### TC-PLN-001
- **Purpose:** Verify response identifies agent role
- **Expected Result:** Response explicitly states role name

### TC-PLN-002
- **Purpose:** Verify response identifies useful next information
- **Expected Result:** Clear list of information requested from the human

### TC-PLN-003
- **Purpose:** Verify response identifies helpful agents
- **Expected Result:** Story-supporting agents are listed with rationale

## Test Case Group 2: Requirements Discovery Quality
### TC-PLN-004
- **Purpose:** Verify gameplay questions are present
- **Expected Result:** Questions include rules, roles, turns, and win conditions

### TC-PLN-005
- **Purpose:** Verify platform questions are present
- **Expected Result:** Questions include web vs desktop and local vs online

### TC-PLN-006
- **Purpose:** Verify MVP questions are present
- **Expected Result:** Response asks about version 1 scope and out-of-scope items

## Test Case Group 3: Architecture Recommendation Quality
### TC-PLN-007
- **Purpose:** Verify frontend recommendations are actionable
- **Expected Result:** At least one practical frontend option is suggested

### TC-PLN-008
- **Purpose:** Verify recommendation aligns with Java backend constraint
- **Expected Result:** Suggested architecture preserves Java backend logic

### TC-PLN-009
- **Purpose:** Verify recommendation includes rationale
- **Expected Result:** Each frontend option includes tradeoff discussion

## Test Case Group 4: Story Planning Readiness
### TC-PLN-010
- **Purpose:** Verify outputs support future story drafting
- **Expected Result:** Response identifies likely epics or story categories

### TC-PLN-011
- **Purpose:** Verify prioritization preparation exists
- **Expected Result:** Response identifies MVP-first planning logic

### TC-PLN-012
- **Purpose:** Verify human checkpoints are included
- **Expected Result:** Response identifies decisions requiring human confirmation

## Test Case Group 5: Risk Identification
### TC-PLN-013
- **Purpose:** Verify major planning risks are identified
- **Expected Result:** Ambiguity, scope, and multiplayer complexity are noted

### TC-PLN-014
- **Purpose:** Verify testability risks are identified
- **Expected Result:** Response highlights unclear rules and acceptance-criteria risk

## Entry Criteria
The planning test effort can begin when:

- the planning prompt is available
- the user intent is captured
- the response format expectations are known

## Exit Criteria
The planning story is considered sufficiently validated when:

- role is explicitly named
- discovery questions are clear and actionable
- supporting agents are identified
- architecture directions are plausible
- planning risks are documented
- outputs are sufficient to begin requirements drafting

## Risks to Track
### Planning Risks
- requirements remain too vague to test
- 2-player and 3-player logic may require different designs
- MVP scope may be undefined

### Test Risks
- acceptance criteria cannot be written until rules are explicit
- frontend recommendation may be premature without platform goals
- hidden assumptions may contaminate later stories

## Recommended Human-in-the-Loop Checkpoints
The Orchestrator should request human confirmation at these points:

### Checkpoint 1
- confirm simplified rules
- confirm player modes
- confirm play mode

### Checkpoint 2
- confirm frontend direction
- confirm MVP scope

### Checkpoint 3
- confirm epic list and initial story priorities

## Immediate Next Step
The next prompt should ideally answer these five questions:

1. What exact gameplay simplifications are planned?
2. Do you need both 2-player and 3-player support?
3. Will the game be local or online?
4. Do you want web or desktop first?
5. What is the minimum playable version?

## Deliverables After Clarification
Once those answers are provided, I can help the Orchestrator validate:

- initial requirements completeness
- testable acceptance criteria
- story readiness for estimation
- risk-based prioritization
- QA considerations for the rules engine and UI
