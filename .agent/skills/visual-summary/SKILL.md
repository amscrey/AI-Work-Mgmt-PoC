---
name: visual-summary
description: DEPRECATED - Merged into output-formatting skill. Use output-formatting instead.
user-invocable: false
deprecated: true
superseded-by: output-formatting
---

# Visual Summary

> **DEPRECATED**: This skill has been merged into `output-formatting`. See `.agent/skills/output-formatting/SKILL.md` for the combined skill.

---

Cross-cutting skill for generating visually appealing ASCII-art summaries of any deliverable. Use this at the end of any step to present information in a way that is easy to scan and pleasant to read.

## Input
- Any completed deliverable (vision doc, tech strategy, backlog, plan, validation report, etc.)

## Output
- An ASCII visual summary printed to the user, highlighting the key information at a glance

## Process

### Phase 1: Decide When to Use
Call this skill **after completing a deliverable**, before presenting it to the user for gate approval. It makes the information scannable and gives a professional, polished feel.

### Phase 2: Build the Visual Summary

#### Step 1: Identify Key Data
Extract the 5-8 most important data points from the deliverable. Less is more.

#### Step 2: Choose a Layout
Pick the layout that best fits the data:

---

### Layout A: Dashboard Card (for overviews)

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

---

### Layout B: Comparison Table (for tech decisions)

```
╔══════════════════════════════════════════════════════════╗
║  🏗️ TECH STRATEGY SUMMARY                               ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  Stack: [Language] │ [UI Framework] │ [DB] │ [Pattern]    ║
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

---

### Layout C: Progress Board (for backlogs/plans)

```
╔══════════════════════════════════════════════════════════╗
║  📋 BACKLOG SUMMARY                    Total: 12 tickets ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  ┌─ MUST (MVP) ──────────────────────────── 5 tickets ┐  ║
║  │  EPIC1-01  Setup & config               S  ░░░░░░  │  ║
║  │  EPIC1-02  Order entry screen           M  ░░░░░░  │  ║
║  │  EPIC1-03  Product catalog              M  ░░░░░░  │  ║
║  │  EPIC2-01  Daily sales view             M  ░░░░░░  │  ║
║  │  EPIC2-02  Order total calculation      S  ░░░░░░  │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  ┌─ SHOULD ──────────────────────────────── 4 tickets ┐  ║
║  │  EPIC3-01  Customer list                S  ░░░░░░  │  ║
║  │  EPIC3-02  Customer search              S  ░░░░░░  │  ║
║  │  EPIC1-04  Edit existing order          M  ░░░░░░  │  ║
║  │  EPIC2-03  Weekly report                M  ░░░░░░  │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  ┌─ COULD ───────────────────────────────── 2 tickets ┐  ║
║  │  EPIC4-01  Dark mode                    S  ░░░░░░  │  ║
║  │  EPIC4-02  Export to PDF                L  ░░░░░░  │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  ┌─ WON'T (post-MVP) ───────────────────── 1 ticket  ┐  ║
║  │  EPIC5-01  Multi-language               XL ░░░░░░  │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

---

### Layout D: Sprint Timeline (for implementation plans)

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
║  │  EPIC1-03  Product catalog ............. M    6h      ║
║  │                                    Total: ~14h        ║
║  │                                                       ║
║  Sprint 2 ▸▸▸▸▸▸▸▸▸▸ [Reports + Edit]                  ║
║  │  EPIC2-01  Daily sales view ............ M    6h      ║
║  │  EPIC2-02  Order total calc ............ S    3h      ║
║  │  EPIC1-04  Edit existing order ......... M    6h      ║
║  │                                    Total: ~15h        ║
║  │                                                       ║
║  Sprint 3 ▸▸▸▸▸▸▸▸▸▸ [Customers + Polish]              ║
║  │  EPIC3-01  Customer list ............... S    3h      ║
║  │  EPIC3-02  Customer search ............. S    3h      ║
║  │  EPIC4-01  Dark mode ................... S    3h      ║
║  │                                    Total: ~9h         ║
║                                                          ║
║  ──────────────────────────────────────────────────────  ║
║  📊 Total: 12 tickets │ ~38h │ 3 sprints                ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

---

### Layout E: Validation Report (for QA results)

```
╔══════════════════════════════════════════════════════════╗
║  ✅ VALIDATION REPORT                                    ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  Feature: Order Entry Screen                             ║
║  Date:    2026-02-11                                     ║
║                                                          ║
║  ┌─ RESULTS ─────────────────────────────────────────┐  ║
║  │                                                    │  ║
║  │  Total: 8    ✅ Pass: 6    ❌ Fail: 1    ⚠️ : 1    │  ║
║  │                                                    │  ║
║  │  ████████████████████░░░░ 75% Pass                 │  ║
║  │                                                    │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  ┌─ BUGS ────────────────────────────────────────────┐  ║
║  │  🐛 BUG-001 [Major] Total not updating on edit    │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  Recommendation: ⚠️ APPROVE WITH CONDITIONS              ║
║  Fix BUG-001 before release                              ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

---

### Layout F: Code Review (for review results)

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
║  🟡 W1: Duplicated validation logic (OrderVM, CartVM)    ║
║  🟡 W2: Missing null check on customer.phone             ║
║  🔵 I1: Consider extracting PriceCalculator utility      ║
║  🔵 I2: Rename `doStuff()` → `calculateOrderTotal()`    ║
║  🔵 I3: Add KDoc to public functions                     ║
║                                                          ║
║  Decision: ✅ APPROVE WITH CONDITIONS                    ║
║  Fix W1 and W2 before next step                          ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

---

## Rules

### Visual Summary Rules

1. **ALWAYS** use double-line box (`╔═╗║╚═╝`) for the outer frame
2. **ALWAYS** include a title with an emoji in the header
3. **ALWAYS** use single-line box (`┌─┐│└─┘`) for inner sections
4. **KEEP IT SHORT**: Max 25 lines inside the box. Summarize, don't dump
5. **USE EMOJIS** strategically: ✅ ❌ ⚠️ 🐛 🎯 📋 🏗️ 📅 🔍
6. **ALIGN COLUMNS** when showing tables — use dots or spaces for alignment
7. **SHOW PROGRESS** with bars: `████░░░░` or status: `✅ ⬜ ⬜`
8. **ONE SUMMARY PER DELIVERABLE** — don't combine multiple deliverables
9. **Present BEFORE the gate** — the summary helps the user decide approval

## Layout Selection Guide

| Deliverable | Recommended Layout |
|-------------|-------------------|
| Product Vision | A: Dashboard Card |
| Tech Strategy | B: Comparison Table |
| Backlog | C: Progress Board |
| Implementation Plan | D: Sprint Timeline |
| Feature (after coding) | F: Code Review |
| Validation Report | E: Validation Report |
