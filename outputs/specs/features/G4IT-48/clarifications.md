---
workflow: feature-implementation
trigger: user
date: '2026-11-20'
status: validated
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: clarification
agent: spec-orchestrator (substitute executor, user-approved; one-shot, no user channel)
skill: spec-clarify, spec-clarify-interactive
sensitivity: internal
inputDocuments:
  - outputs/specs/features/G4IT-48/spec.md
  - outputs/specs/features/G4IT-48/requirements-brief.md
changeHistory:
  - date: '2026-11-20'
    author: spec-orchestrator (substitute)
    changes: 'Initial clarification pass; 4 open questions recorded as pending with recommended answers'
  - date: '2026-11-20'
    author: spec-orchestrator (substitute)
    changes: 'User answers applied: CQ-1..CQ-4 resolved as recommended; AS-2 confirmed (any invalid field rejects the whole line); CLQ-013/014/019/020 declared out of scope; no pending questions'
holisticQualityRating: not-rated
overallStatus: not-reviewed
context_manifest:
  - path: .github/skills/spec-clarify/SKILL.md
    mode: full
    key_facts:
      - 'Classify each item as decision, assumption or open question; never invent user answers'
      - 'CLQ-013..CLQ-021 (errors/edge cases, integrations) are open questions unless explicitly addressed'
      - 'Minimum 3 open questions threshold triggers re-evaluation'
  - path: outputs/specs/features/G4IT-48/spec.md
    mode: full
    key_facts:
      - 'FR-001..FR-014; defaults BYOD 0.57, COPE 0.85, else 1.0; Devices = item_category DEVICES'
      - 'Q-NEW-1 (non-Devices export as provided) and Q-NEW-2 (no Devices -> no data) resolved'
      - 'FR-014 adds Hugo documentation update in FR and EN'
  - path: .github/knowledge/governance/ai-disclaimer-text.md
    mode: full
    key_facts:
      - 'Disclaimer v1.0, three-line English canonical text'
---
<!-- AI_DISCLAIMER v1.0 -->
# Clarifications: G4IT-48 — BYOD/COPE usage allocation

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

CONTEXT LOADED: 3 documents read, context manifest prepared.

## Decisions (already resolved — not re-asked)

Q1–Q6, A-1, A-1b, A-1c, A-2 (requirements brief), Q-NEW-1, Q-NEW-2 and FR-014 (spec.md). They cover scope (CLQ-001/002), validation (CLQ-011), business rules (CLQ-022), boundary values for fractions and no-Devices inventories (CLQ-015), and error labels (CLQ-017).

## Assumptions (stated, low risk)

| ID | Assumption | Checklist |
|---|---|---|
| AS-1 | Roles and authorization are unchanged: the existing inventory roles import, evaluate and export. | CLQ-004/005 |
| AS-2 | **Confirmed by user 2026-11-20.** A line with ANY invalid field (e.g. invalid `contexteUsage` or `fractionUsage` on a Devices line) is rejected entirely and reported in the rejected-file report. Other lines import as usual. Values on non-Devices lines are not invalid fields: they remain silently ignored (D-Q1). | CLQ-016 |
| AS-3 | No new external integration. The fraction is applied to the input before the existing calculation engine runs, so engine contracts are unchanged. | CLQ-018..021 |
| AS-4 | `fractionUsage` follows the same decimal parsing as the other numeric import columns. | CLQ-011 |
| AS-5 | Digital-service physical equipment that carries these columns is ignored, not rejected (D-Q3). | CLQ-002 |
| AS-6 | There are no notifications, retention changes or new data volumes. | CLQ-010/012/030 |

## Clarification questions (resolved by user 2026-11-20)

| ID | Question | Answer | Status |
|---|---|---|---|
| CQ-1 | When is the effective fraction (and Device status) set and stored: at import or at evaluation? | At evaluation time; persisted with the evaluated equipment result. Export and indicators reuse it. | resolved |
| CQ-2 | How should the indicators and exported fractions be rounded and formatted? | Indicators: same rounding as existing percentage Key Indicators. Export: plain decimal (0.57, 0.85, 0.5). | resolved |
| CQ-3 | Should the downloadable physical-equipment CSV template and the sample files include the two new columns? | Yes, both columns added empty; handled within the FR-014 documentation task. | resolved |
| CQ-4 | How are empty/decimal quantities counted in the BYOD/COPE % indicators? | Same quantity field and aggregation as existing equipment indicators; no special case. | resolved |

## Out of scope (user decision 2026-11-20)

| Checklist | Topic | Decision |
|---|---|---|
| CLQ-013 | External service unavailability | Out of scope for this feature; existing behaviour unchanged. |
| CLQ-019/020 | Integration resilience (retries, timeouts, fallbacks) | Out of scope; existing behaviour unchanged. |
| CLQ-014 | Concurrent edits | Out of scope; existing behaviour unchanged. |

## Pending questions

None.

Sensitivity: internal. No PII processed.
