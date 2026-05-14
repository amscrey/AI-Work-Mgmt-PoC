## 1) Keep prompts signal-rich

**Goal:** maximize **signal / token**, not “fewest tokens possible.”

### Principles
- Be **clear, specific, and relevant**.
- Keep only context that **earns its place**.
- Prefer **token count** as the real measure; chars are a rough proxy.
- Don’t use random abbreviations, dropped words, or misspellings to save tokens.
- Standard abbreviations are fine if unambiguous: `API`, `JSON`, `SQL`, `auth`, `env`, `repo`.

### What to include
- **Exact task / goal**
- **Constraints** (performance, compatibility, security, style)
- **Relevant facts only**
- **Scope / non-goals**
- **Desired output format**
- **Minimal necessary artifacts** (narrow code excerpt, exact error lines, specific doc section)

### What to cut
- Repetition
- Long preambles / throat-clearing
- Emotional filler
- Broad background that won’t change the answer
- Huge files/logs when a narrow excerpt or reference will do

### Structure
Use short labeled sections:
- **Task**
- **Context**
- **Constraints**
- **Output**
- **Artifacts in scope**

### Context management
Use **minimum sufficient context**:
- Keep **working context**, not full historical context
- Periodically **summarize and replace** long threads
- Pass **references** (file/method/line range) instead of re-pasting when the tool can access them
- **Slice narrowly**: only the relevant code/log/doc fragment
- **Break work into phases**
- **Externalize stable facts** (project summary, conventions, architecture notes)
- Use **handoff summaries** between sessions
- Drop **dead branches** (ruled-out hypotheses, old alternatives)
- Ask for **delta-oriented** continuation (“only update based on new error”)

---

## 2) Encourage efficient outputs

**Goal:** get the right answer in the fewest useful tokens.

### Ask for
- **Answer first**
- **Bullets, not prose**
- **Specific length limit**
- **Fixed structure**
- **Only actionable items**
- **No restating the question**
- **No preamble or summary unless requested**

### Good instructions
- “Be concise.”
- “Give the answer first.”
- “Use bullets.”
- “Limit to 5 bullets.”
- “Max 120 words.”
- “No intro or recap.”
- “Only include decision-critical details.”
- “Stop after the recommended solution.”

### For code tasks
- “Return only the patch.”
- “Return only the function.”
- “No explanation unless asked.”
- “List issues first, then minimal fix.”
- “Keep comments minimal.”

### Caution
Don’t force brevity so hard that you lose:
- assumptions
- caveats
- confidence
- edge cases

A good default:
> **Be concise, but include all decision-critical details.**

---

## 3) Reusable prompt snippet library for concise outputs

Copy/paste and adapt.

### A. Universal concise-output snippet
```text
Be concise. Give the answer first.
Use bullets, not prose.
Include only decision-critical details.
Do not restate the question.
No intro or summary.
If uncertain, say so briefly.
```

### B. Short-answer with bounded length
```text
Answer in <= 120 words.
Start with the single best answer.
Then give up to 3 supporting bullets.
No background unless essential.
```

### C. Minimal-next-step snippet
```text
Given the context above, provide only the next best step.
1-3 bullets max.
No recap of prior discussion.
```

### D. Delta-only continuation
```text
Update the answer only based on the new information above.
Do not repeat prior conclusions unless they changed.
Keep to 5 bullets max.
```

---

### Coding

#### 1. Minimal implementation plan
```text
Propose the smallest implementation plan.
Output:
- 3 to 5 bullets
- most important step first
- mention only files/functions likely to change
No explanation beyond what is needed to act.
```

#### 2. Patch-only
```text
Return only the patch.
No explanation.
Keep the change minimal.
Preserve existing behavior unless explicitly stated otherwise.
```

#### 3. Code review, concise
```text
Review this code.
Output:
1. Top 3 issues
2. Why each matters (1 sentence each)
3. Minimal fix for each
No praise, no summary, no rewritten code unless necessary.
```

#### 4. API/design recommendation
```text
Recommend one approach.
Format:
- Recommendation
- Why
- Main tradeoff
Max 100 words.
No alternatives unless the recommendation is weak.
```

#### 5. Refactor guidance
```text
Suggest a behavior-preserving refactor.
Output:
- goal
- smallest safe change
- risks
- tests to run
Max 6 bullets.
```

---

### Debugging

#### 1. Root-cause triage
```text
Identify the 3 most likely causes.
For each, give:
- why it fits
- the fastest verification step
Use one bullet per cause.
No preamble.
```

#### 2. Fastest-debug path
```text
Give the fastest path to isolate the bug.
Format:
1. first check
2. second check
3. likely fix if confirmed
Max 100 words.
```

#### 3. Error-log compression
```text
Extract only the actionable signals from this log.
Output:
- probable failure point
- strongest evidence
- next diagnostic step
No paraphrase of the whole log.
```

#### 4. Regression analysis
```text
Assume this is a regression.
List:
- most likely recent change categories
- what to diff first
- one quick rollback test
Max 5 bullets.
```

---

### Research

#### 1. Quick synthesis
```text
Summarize the key findings.
Output:
- 3 main points
- 2 uncertainties
- 1 recommendation
Keep it compact and decision-focused.
```

#### 2. Compare options
```text
Compare these options in a compact table with columns:
Option | Best for | Main risk | Recommendation
Limit to the top 3 options.
```

#### 3. Source-aware answer
```text
Answer using only the provided material.
Output:
- answer
- evidence bullets
- unknowns
Do not speculate beyond the sources.
```

#### 4. Decision memo, tiny
```text
Write a mini decision memo.
Format:
- Decision
- Why
- Risks
- Next step
Max 150 words.
```

---

### Planning

#### 1. Minimal plan
```text
Create a minimal plan.
Output:
- objective
- 3 to 5 steps
- key dependency
- biggest risk
No extra commentary.
```

#### 2. Prioritization
```text
Prioritize these items.
Return a ranked list with:
- item
- why now
- what can wait
One line per item.
```

#### 3. Sprint/task breakdown
```text
Break this into small executable tasks.
Each task must be:
- independently testable
- <= 1 day of work if possible
Return only the task list.
```

#### 4. Next-action planning
```text
Given the current status, propose only the next 3 actions.
Most leverage first.
No long-term roadmap.
```

---

## 4) How to use agile methodology/stories for efficient AI agentic engagement

### Core idea
Agile stories are excellent **context containers**.

They compress what the AI needs:
- **goal**
- **scope**
- **business value**
- **acceptance criteria**
- **constraints**

That reduces context sprawl and makes session resets easier.

### Why stories help
A good story narrows:
- what problem is being solved
- what counts as done
- what is out of scope

This is ideal for AI. It keeps the model focused and reduces the need to replay broad project history.

### Use a 3-layer context model

#### A. Stable story context
Reusable across sessions:
- Story title / statement
- Business purpose
- Acceptance criteria
- Constraints / non-goals

#### B. Working implementation context
Updated during the work:
- Current status
- Files/services in scope
- Decisions made
- Hypotheses / blockers
- Test status

#### C. Artifact context
Inject only as needed:
- code snippets
- failing logs
- schema fragments
- specific doc excerpts

This keeps prompts lean:
- **A stays stable**
- **B gets summarized**
- **C is fetched selectively**

### What makes a story good for AI
- Small
- Testable
- Concrete
- Scoped
- Outcome-oriented
- Includes acceptance criteria

### What makes a story bad for AI
- Epic-sized
- Vague
- Multiple deliverables mashed together
- Missing success criteria
- Mixed business and technical debates in one blob

### Good AI-oriented story pattern
```text
Story:
As a <user>, I want <capability>, so that <outcome>.

Acceptance criteria:
- ...
- ...
- ...

Constraints:
- ...
- ...

Current state:
- ...
- ...

Artifacts in scope:
- file/service/test names

Next task:
- one immediate ask
```

### How to engage AI efficiently with stories
- Start each session with the **story + acceptance criteria**
- Add a **compact current-state summary**
- Include only the **artifact needed right now**
- Ask for one immediate deliverable:
  - smallest implementation plan
  - root-cause analysis
  - test list
  - patch
  - review

### Example
```text
Story:
As an admin, I want failed webhook deliveries to retry automatically so transient outages do not lose events.

Acceptance criteria:
- retry up to 3 times with exponential backoff
- log each retry attempt
- mark permanently failed after final retry
- no duplicate event processing

Current state:
- retries not implemented
- failures are logged but retry count is not persisted

Task:
propose the smallest implementation plan

Output:
5 bullets max
```

### Story-based handoff / reset pattern
At the end of a session, store:
- story
- acceptance criteria
- what was ruled out
- current hypothesis
- files touched
- tests status
- next 3 steps

Then start a fresh session with that compact summary instead of the full thread.

### Practical agile advice for AI use
- Keep stories **small enough to fit in working context**
- Split large epics into story-sized units before engaging the AI
- Use acceptance criteria as **prompt constraints**
- Use Definition of Done to evaluate outputs
- Keep a short technical handoff note per story
- Treat each story as a **bounded context window**

---

## Bottom line

### For prompts
Use **clear, scoped, structured, minimum-sufficient context**.

### For outputs
Ask for **answer-first, bounded, structured, no-padding responses**.

### For agile/stories
Use stories as **compact context containers** with:
- story statement
- acceptance criteria
- constraints
- current state
- narrow artifacts
- one immediate task

If you want, next I can turn this into:
1. a **one-page cheat sheet**, or  
2. a **set of reusable templates** for story cards, handoff notes, and concise AI prompts.
