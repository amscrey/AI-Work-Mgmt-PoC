# Planning: Token/Context Awareness and Efficiency

This planning doc converts the advice guidance and mission goals into story-sized work items. It is intended to seed new stories in `backlog/`.

## Inputs

- `etc/docs/Advice - token and context tracking.md`
- `etc/docs/Advice - token efficiency.md`
- Mission goals in `MISSION.md`

## Planning Principles

- Prefer per-call usage metadata when providers expose it.
- Estimate tokens when metadata is unavailable.
- Track usage at both **call** and **story** levels (story lifecycle totals).
- Track **project-wide** totals by summing all story interactions.
- Keep prompts signal-rich and outputs concise.
- Align AI work to Jira stories and acceptance criteria.
- Start by **logging context size in agent comments/logs**; decide later how to surface it elsewhere.

## Proposed Story Set

### EPIC A: Token and Context Instrumentation (Foundational)

1) **Story A1: Per-Story Token Accounting**
- Capture token usage per story engagement (per orchestration execution).
- Track tokens per LLM provider/model per story.
- Store per-call usage and aggregate into story totals.

2) **Story A2: Token Estimation Fallback**
- Add tokenizer-based estimation when provider metadata is missing.
- Track context size at prompt assembly time (for agent reporting).
- Support per-provider tokenizer configuration.

3) **Story A3: Agent Usage Reporting**
- Require agents to report token usage and context size in comments and logs.
- Include per-agent totals for each story engagement.
- Ensure reporting is available even when using token estimates.

4) **Story A4: Project-Wide Aggregation**
- Aggregate token usage across all stories for project-wide totals.
- Provide per-LLM/provider rollups across the project.
- Prepare for later time-based reporting (defer to future story).

### EPIC B: Budget Awareness and Warnings

5) **Story B1: Context Window Warnings**
- Warn at 70/85/95% context occupancy.
- Reserve output budget for each call.
- Provide guidance on summarization triggers.

6) **Story B2: Quota/Reset Tracking**
- Track tool/provider reset windows at the project level.
- Surface usage rate and predicted exhaustion.

### EPIC C: Prompt and Output Efficiency

7) **Story C1: Prompt Assembly Guardrails**
- Enforce minimal necessary context by policy (max files, max chars).
- Support references instead of re-pasting content.

8) **Story C2: Output Constraints**
- Add system prompts or policies for concise output.
- Allow task-specific output limits (max words/bullets).

9) **Story C3: Handoff Summaries**
- Provide summary generation to replace long history.
- Maintain compact summaries per story/session.

### EPIC D: Visibility and Reporting

10) **Story D1: Runtime Usage Summary Endpoint**
- Provide live usage totals per story and project.
- Include last-call usage and warnings.

11) **Story D2: Startup Report Extension**
- Add token/usage configuration and policy summary to startup report.
- List enabled tracking features and ignored config entries.

### EPIC E: Jira Story Alignment

12) **Story E1: Story-Centric Usage Reports**
- Link usage totals to story id and acceptance criteria.
- Highlight token-heavy phases (planning/search/edit/test).

13) **Story E2: Scope Enforcement**
- Validate that requests include story id and work intent.
- Block or warn on context that is out-of-scope.

## Suggested Acceptance Criteria Patterns

- Per-call usage records include provider/model/latency/token counts.
- Story-level totals include per-LLM breakdown and aggregate tokens.
- Project-level totals sum all story interactions.
- Agent comments/logs include token usage and context size per story engagement.
- Warnings fire when context occupancy exceeds configured thresholds.
- Output policies reduce verbosity without losing decision-critical detail.
- Reports are accessible at runtime and in startup logs.

## Next Step

Convert the above into story folders in `backlog/`, prioritize based on dependencies (per-story accounting before warnings), and assign to appropriate roles (coder, logician, tester).
