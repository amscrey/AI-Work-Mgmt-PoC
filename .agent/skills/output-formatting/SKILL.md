---
name: output-formatting
description: Cross-cutting skill for formatting output to users. Includes copy-paste friendly code blocks and ASCII visual summaries for deliverables.
user-invocable: false
---

# Output Formatting

Cross-cutting skill combining copy-paste formatting rules and visual summary layouts. Use this when presenting deliverables to users.

## Input
- Any deliverable intended for user consumption (code, scripts, config, docs, reports)

## Output
- Copy-ready content in code blocks
- Visual ASCII summaries for deliverables

---

# Part 1: Copy-Paste Formatting

## The Golden Rule
**If the user needs to use it, put it in a code block.**

Content intended for the user to copy (code, scripts, text drafts, configuration) MUST be isolated in code blocks. Do not mix instructional text with the content the user needs to copy.

## Formatting Standards

### Code & Scripts
Always use a code block with the appropriate language tag.

**Bad:**
Here is the python script:
def hello():
    print("world")

**Good:**
```python
# script.py
def hello():
    print("world")
```

### Terminal Commands
Always use `bash` or `sh` blocks. Never use prompt symbols (`$`, `>`). The user should be able to copy the entire block and run it.

**Bad:**
$ npm install
$ npm start

**Good:**
```bash
npm install
npm start
```

### Documentation / Text Assets
Use `markdown` or `text` blocks for text drafts.

**Good:**
```markdown
# Project Update
- Status: Green
- Next: Deploy
```

## Response Structure
When delivering an artifact:
1. **Brief Context:** 1-2 lines explaining what this is
2. **The Artifact (Code Block):** The core content
3. **Action:** What to do with it (optional)

## Anti-Patterns
- **Inline Code for Long Strings:** Don't use `code` for multi-line content
- **Ambiguous Boundaries:** Don't let the artifact blend into commentary
- **Unnecessary chatter:** Don't explain line-by-line unless asked

---

# Part 2: Visual Summaries

Use ASCII visual summaries to present deliverables in a scannable, professional format.

## When to Use
Call this **after completing a deliverable**, before presenting it to the user for approval.

## Layout Selection Guide

| Deliverable | Layout |
|-------------|--------|
| Product Vision | Dashboard Card |
| Tech Strategy | Comparison Table |
| Backlog | Progress Board |
| Implementation Plan | Sprint Timeline |
| Code Review | Code Review Summary |
| Validation Report | Validation Report |

## Layout A: Dashboard Card (for overviews)

```
╔══════════════════════════════════════════════════════════╗
║  📋 PRODUCT VISION SUMMARY                              ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  🎯 Problem:  Order tracking is manual and error-prone   ║
║  👤 User:     Small bakery owner, non-technical          ║
║  📱 Platform: [as defined in vision]                     ║
║                                                          ║
║  ┌─ CORE FEATURES ────────────────────────────────────┐  ║
║  │  1. ✅ Order entry with product catalog             │  ║
║  │  2. ✅ Daily/weekly sales summary                   │  ║
║  │  3. ✅ Customer management                          │  ║
║  │  4. ⬜ Receipt printing (post-MVP)                  │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  🏁 MVP: Enter orders + view daily totals                ║
║  📅 Deadline: None                                       ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

## Layout B: Comparison Table (for tech decisions)

```
╔══════════════════════════════════════════════════════════╗
║  🏗️ TECH STRATEGY SUMMARY                               ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  Stack: [Language] │ [UI Framework] │ [DB] │ [Pattern]   ║
║                                                          ║
║  ┌──────────────┬────────────┬──────────┬────────────┐  ║
║  │ Feature      │ Component  │ Library  │ Complexity │  ║
║  ├──────────────┼────────────┼──────────┼────────────┤  ║
║  │ Order Entry  │ Screen     │ ORM      │ M          │  ║
║  │ Sales Report │ Screen     │ ORM      │ M          │  ║
║  │ Customer Mgmt│ Service    │ ORM      │ S          │  ║
║  └──────────────┴────────────┴──────────┴────────────┘  ║
║                                                          ║
║  ⚠️ Risks: None critical                                 ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

## Layout C: Progress Board (for backlogs)

```
╔══════════════════════════════════════════════════════════╗
║  📋 BACKLOG SUMMARY                    Total: 12 tickets ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  ┌─ MUST (MVP) ──────────────────────────── 5 tickets ┐  ║
║  │  EPIC1-01  Setup & config               S  ░░░░░░  │  ║
║  │  EPIC1-02  Order entry screen           M  ░░░░░░  │  ║
║  │  EPIC1-03  Product catalog              M  ░░░░░░  │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  ┌─ SHOULD ──────────────────────────────── 4 tickets ┐  ║
║  │  EPIC3-01  Customer list                S  ░░░░░░  │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

## Layout D: Sprint Timeline

```
╔══════════════════════════════════════════════════════════╗
║  📅 IMPLEMENTATION PLAN                                  ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  MVP = Sprint 1 + Sprint 2                               ║
║                                                          ║
║  Sprint 1 ▸▸▸▸▸▸▸▸▸▸ [Setup + Core]                    ║
║  │  EPIC1-01  Setup & config .............. S    2h      ║
║  │  EPIC1-02  Order entry screen .......... M    6h      ║
║  │                                    Total: ~8h         ║
║                                                          ║
║  Sprint 2 ▸▸▸▸▸▸▸▸▸▸ [Reports]                         ║
║  │  EPIC2-01  Daily sales view ............ M    6h      ║
║  │                                    Total: ~6h         ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

## Layout E: Validation Report

```
╔══════════════════════════════════════════════════════════╗
║  ✅ VALIDATION REPORT                                    ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  Feature: Order Entry Screen                             ║
║  Date:    2026-02-11                                     ║
║                                                          ║
║  ┌─ RESULTS ─────────────────────────────────────────┐  ║
║  │  Total: 8    ✅ Pass: 6    ❌ Fail: 1    ⚠️ : 1    │  ║
║  │  ████████████████████░░░░ 75% Pass                 │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  Recommendation: ⚠️ APPROVE WITH CONDITIONS              ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

## Layout F: Code Review Summary

```
╔══════════════════════════════════════════════════════════╗
║  🔍 CODE REVIEW SUMMARY                                 ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  Tests: ✅ 24/24 passing                                 ║
║                                                          ║
║  ┌─ FINDINGS ────────────────────────────────────────┐  ║
║  │  🔴 Critical: 0                                    │  ║
║  │  🟡 Warning:  2                                    │  ║
║  │  🔵 Info:     3                                    │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  Decision: ✅ APPROVE                                    ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

---

## Rules

### Copy-Paste Rules
1. **ALWAYS** place copy-target content inside fenced code blocks
2. **ALWAYS** use an appropriate language tag when possible
3. **NEVER** mix artifact lines with explanatory prose in the same block
4. **NEVER** require manual cleanup before the user can copy/run content

### Visual Summary Rules
1. **ALWAYS** use double-line box (`╔═╗║╚═╝`) for the outer frame
2. **ALWAYS** include a title with an emoji in the header
3. **ALWAYS** use single-line box (`┌─┐│└─┘`) for inner sections
4. **KEEP IT SHORT**: Max 25 lines inside the box
5. **USE EMOJIS** strategically: ✅ ❌ ⚠️ 🐛 🎯 📋 🏗️ 📅 🔍
6. **ALIGN COLUMNS** when showing tables
7. **ONE SUMMARY PER DELIVERABLE**
