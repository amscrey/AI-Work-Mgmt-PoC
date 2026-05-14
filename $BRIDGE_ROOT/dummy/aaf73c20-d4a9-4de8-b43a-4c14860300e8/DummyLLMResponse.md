# Orchestrator AI Agent

## Role
- **Role Name:** Orchestrator AI Agent
- **Function:** Coordinate discovery, requirements gathering, story planning, and agent collaboration with human-in-the-loop decision points.

## Initial Response
You want to start a new effort to build a **2–3 player computer game** based on a **slightly simplified version of the Jaws board game**.
You already have two key implementation preferences:

- **Backend logic:** Java
- **Frontend:** You want recommendations

My role is to help structure the work, identify the right next questions, and route tasks to the most useful specialist agents so we can produce clear requirements and prioritized stories.

## Most Useful Information for Your Next Prompt
To get started on **initial requirements**, the most useful information you can provide next is:

- **Game rules overview**
  - What parts of the original Jaws board game should remain?
  - What parts should be simplified, changed, or removed?
- **Player setup**
  - Will the game support exactly 2 players, or both 2 and 3 players?
  - If 3 players are supported, how are roles divided?
- **Platform target**
  - Web app, desktop app, or mobile-first?
- **Play mode**
  - Local hot-seat, online multiplayer, or both?
- **Game flow**
  - Turn-based only, or any real-time elements?
- **AI support**
  - Do you want computer-controlled opponents, or only human players?
- **Frontend expectations**
  - Simple functional UI, polished game board UI, or animated experience?
- **Persistence**
  - Should players be able to save/load games?
- **Authentication**
  - Anonymous local play, login accounts, or lobby/invite system?
- **Delivery expectations**
  - Prototype first, then production version?
  - Any timeline or milestone expectations?

## Key Questions You Should Answer Next
Please answer as many of these as you can to help us build the initial requirements.

### Product Vision
- What is the main goal of this project?
- Is this a prototype, hobby game, classroom exercise, or product-quality release?
- Who are the expected players?

### Game Design
- Which mechanics from Jaws are essential?
- What exact simplifications do you want?
- What constitutes winning or losing?
- How long should a typical game last?
- Should the game enforce rules automatically?

### Player and Multiplayer Model
- What are the player roles?
- How does the game work with 2 players?
- How does it differ with 3 players?
- Should players join a lobby or start instantly?

### Technical Direction
- Do you want:
  - **Java backend + web frontend**
  - **Java desktop app**
  - **Java backend + mobile frontend**
- Do you prefer any frontend technologies already?

### UX / Visuals
- Should the board look like the physical board game, or be a digital interpretation?
- Do you want drag-and-drop interactions?
- Do you want animations, sound, and status indicators?

### Scope / Constraints
- What is explicitly out of scope for version 1?
- Do you want online matchmaking?
- Do you need chat, reconnect, or spectators?
- Any constraints on hosting, budget, or tools?

## Frontend Suggestions
Given your preference for **Java backend logic**, here are the strongest frontend options.

### Option 1: Web Frontend
**Recommended for most cases**

- **Frontend:** React, Vue, or Angular
- **Backend:** Java with Spring Boot
- **Why it fits**
  - Easy multiplayer support
  - Accessible from browser
  - Good for turn-based game interfaces
  - Easier to demo and test

**Best choice if:**
- You want online play
- You want broad accessibility
- You may expand later

### Option 2: JavaFX Desktop App
**Recommended for a simpler all-Java implementation**

- **Frontend:** JavaFX
- **Backend/game logic:** Java
- **Why it fits**
  - Entire stack can stay in Java
  - Faster for a contained prototype
  - Good for local play or a lightweight desktop experience

**Best choice if:**
- You want a desktop-only game
- You want less frontend framework overhead
- You want to move quickly in one language

### Option 3: Web Frontend with Lightweight UI
**Recommended for fast prototyping**

- **Frontend:** HTML/CSS/JavaScript or TypeScript
- **Backend:** Java with Spring Boot
- **Why it fits**
  - Good for proving gameplay first
  - Lower initial complexity
  - Easy to evolve later into a richer UI

**Best choice if:**
- You want to validate rules and flows before investing in polish

## Frontend Recommendation
My current recommendation is:

- **Backend:** Java + Spring Boot
- **Frontend:** React
- **Realtime/turn sync:** REST initially, WebSocket later if needed

This gives the best balance of:

- maintainability
- multiplayer readiness
- UI flexibility
- future expansion

If your priority is **speed and simplicity**, then:

- **Java backend/game logic + JavaFX frontend**

is a very reasonable alternative.

## Agents Most Useful for Writing and Prioritizing Stories
The following agents would be most useful in this workflow.

### 1. Product Owner Agent
- Helps define scope
- Converts goals into epics and user stories
- Clarifies acceptance criteria
- Prioritizes work by value

### 2. Business Analyst / Requirements Agent
- Elicits detailed requirements
- Identifies ambiguities and missing decisions
- Documents gameplay rules and flows
- Builds functional requirement drafts

### 3. Game Design Agent
- Translates board game mechanics into digital systems
- Proposes simplifications
- Defines turn structure, actions, roles, and win conditions
- Helps balance 2-player vs 3-player play

### 4. Technical Architect Agent
- Recommends system architecture
- Splits backend, frontend, and multiplayer concerns
- Identifies technical risks
- Guides implementation strategy

### 5. Scrum / Delivery Planning Agent
- Breaks epics into stories
- Helps estimate and size stories
- Organizes backlog for incremental delivery
- Supports sprint planning readiness

### 6. UX / UI Agent
- Defines user flows and screen needs
- Helps identify frontend story requirements
- Proposes interaction patterns for digital board gameplay

### 7. QA / Test Strategy Agent
- Writes testable acceptance criteria
- Identifies edge cases
- Ensures stories are implementation-ready
- Supports rule validation and regression strategy

## Recommended Story Planning Team
For your specific effort, I recommend this core group:

- **Orchestrator AI Agent** — coordinates workflow
- **Product Owner Agent** — prioritization and story framing
- **Business Analyst Agent** — requirements gathering
- **Game Design Agent** — gameplay mechanics
- **Technical Architect Agent** — Java/backend/frontend decisions
- **QA Agent** — acceptance criteria and testability

Optional but very valuable:

- **UX/UI Agent** — if visual interaction matters early
- **Scrum Planning Agent** — if you want formal sizing and sprint planning

## Suggested Next Step
On your next prompt, please provide:

- a summary of the simplified rules
- target platform
- whether play is local or online
- whether AI opponents are needed
- whether you prefer web or desktop
- desired scope for version 1

## Human-in-the-Loop Decision Request
To move into requirements drafting, I need your input on these priority decisions first:

1. **Web or desktop?**
2. **Local multiplayer or online multiplayer?**
3. **2-player only, or 2 and 3 players?**
4. **Exact simplifications from the original board game?**
5. **Prototype-focused or polished release-focused?**

Once you answer those, I can orchestrate the next phase:
- initial requirements draft
- recommended architecture
- first epic breakdown
- starter story backlog with rough sizing
