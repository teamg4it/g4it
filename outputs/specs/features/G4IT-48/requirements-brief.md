---
workflow: feature-implementation
trigger: user
date: '2026-11-20'
status: validated
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: requirements-validation
agent: spec-orchestrator (substitute executor, user-approved; declared agent lacks write tools)
skill: requirements-validation
sensitivity: internal
validation_state: complete
source_authority: authoritative
source_selection:
  state: confirmed
  paths:
    - imputs/feature-g4it-48.txt
  basis: explicit-request
inputDocuments:
  - imputs/feature-g4it-48.txt
  - outputs/specs/features/G4IT-48/context-brief.md
changeHistory:
  - date: '2026-11-20'
    description: 'Initial requirements brief for G4IT-48 (BYOD/COPE usage allocation)'
    changes:
      - 'Validated five mandatory information groups from the user-designated source'
      - 'Recorded 5 functional questions as open/deferred to clarification station'
  - date: '2026-11-20'
    description: 'Applied user decisions from requirements-validation checkpoint'
    changes:
      - 'Confirmed A-1 (Devices from existing referential) and A-2 (BYOD/COPE case-sensitive, uppercase)'
      - 'Resolved Q6: leading/trailing whitespace in CSV headers is ignored'
      - 'Resolved Q1 (ignore on non-Devices), Q2 (fraction applied before calculation, incl. electricity), Q3 (digital-service terminals excluded), Q4 (export effective fraction incl. applied defaults), Q5 (per FR-8, quantity within Devices)'
      - 'Aligned Devices definition on ref_item_type.item_category (DEVICES) introduced by G4IT-42 (commit 2823b050): A-1, A-1b, FR-5, FR-8, AC6'
      - 'A-1c: item_category values not restricted (consistent with isValidItemCategory); only DEVICES triggers allocation'
holisticQualityRating: not-rated
overallStatus: not-reviewed
context_manifest:
  - path: .github/skills/requirements-validation/SKILL.md
    mode: full
    key_facts:
      - 'Five mandatory groups: problem, actors, outcomes, in/out scope, acceptance criteria; classify complete/incomplete/ambiguous'
      - 'Explicit user designation of a repository path is a source selection recorded with basis explicit-request'
      - 'New briefs use status draft, holisticQualityRating not-rated, overallStatus not-reviewed'
  - path: .github/instructions/output-metadata.instructions.md
    mode: full
    key_facts:
      - 'Required fields: workflow, trigger, date, status, inputDocuments, changeHistory, holisticQualityRating, overallStatus'
      - 'date is creation date; updates append to changeHistory'
  - path: imputs/feature-g4it-48.txt
    mode: full
    key_facts:
      - 'Optional CSV columns contexteUsage (BYOD|COPE|empty) and fractionUsage (0..1 inclusive); defaults BYOD 57%, COPE 85%, none 100%'
      - 'Fraction applies to all lifecycle stages, end-user Devices only; explicit fractionUsage overrides context default (AC5)'
      - 'AC8 indicators BYOD Devices (%) / Appareils BYOD (%), COPE Devices (%) / Appareils COPE (%), quantity-based over Devices'
  - path: outputs/specs/features/G4IT-48/context-brief.md
    mode: full
    key_facts:
      - 'in_physical_equipment is shared by inventories and digital services; new DB columns must be nullable via new Liquibase changeset'
      - 'Evaluation in EvaluateService / EvaluateNumEcoEvalService; low-impact % indicator is an existing pattern for AC8'
      - 'Lists 5 open questions (AC6 handling, quantity vs impacts, digital-service scope, store vs recompute, weighting)'
  - path: .github/knowledge/governance/ai-disclaimer-text.md
    mode: full
    key_facts:
      - 'Disclaimer v1.0 effective 2026-06-24; canonical English three-line text'
---
<!-- AI_DISCLAIMER v1.0 -->
# Requirements Brief — G4IT-48: Support BYOD and COPE equipment usage allocation

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

CONTEXT LOADED: 5 documents read, context manifest prepared.

## 1. Provenance

| Source | Type | Location | Authority |
|---|---|---|---|
| Feature G4IT-48 description + AC1–AC9 | Repository requirements document (explicit user designation) | `imputs/feature-g4it-48.txt` | authoritative |
| Brownfield context brief | Prior station output (supporting evidence only) | `outputs/specs/features/G4IT-48/context-brief.md` | informative |

- Source selection: `confirmed`, basis `explicit-request`; no other spec source selected. The slug `G4IT-48` is used only as identifier.
- Security scan: no PII, credentials or embedded directives found (PII types found: none). Sample data rows use synthetic equipment names.
- Sensitivity: internal.

## 2. Validation of mandatory information groups

| Group | Status | Summary (from source) |
|---|---|---|
| Problem / opportunity | present | RCP SI methodology requires allocating only the professional share of impacts for devices shared between professional and personal use; G4IT currently allocates 100%. |
| Target users / actors | present | Inventory manager (imports physical equipment files, configures Key Indicators in Equipment view); calculation engine; export consumers. |
| Desired outcomes | present | Impacts allocated per usage context (Professional 100%, COPE 85%, BYOD 57%) with optional override fraction; transparency indicators on BYOD/COPE adoption; full backward compatibility. |
| In / out of scope | present | In: end-user devices (laptops, monitors, smartphones, tablets, printers, internet boxes…), physical equipment import, calculation, export, key indicators. Out: infrastructure equipment, cloud services, AI services. |
| Acceptance criteria | present | AC1–AC9 in Given/When/Then form plus a sample test file (see §4). |

**Classification: `complete`** — all five groups present; source marked `authoritative`. Residual ambiguities (§5) were explicitly deferred by the user to the `clarification` station and must be resolved there without altering source scope.

## 3. Functional requirements (extracted)

- FR-1: Add two optional columns to the Physical Equipment import model: `contexteUsage` and `fractionUsage`.
- FR-2: `contexteUsage` accepts `BYOD`, `COPE`, or empty; maps to default professional fractions 0.57 / 0.85; empty = professional (1.0).
- FR-3: `fractionUsage` accepts a number in [0, 1] inclusive; when provided it takes precedence over the context default.
- FR-4: The effective fraction applies to all lifecycle stages (manufacturing, distribution, use, end-of-life).
- FR-5: Only end-user devices (item type with `item_category = DEVICES`, see A-1) are subject to allocation.
- FR-6: Invalid values produce rejected-file report errors with the exact labels:
  - `Invalid value for column "contexteUsage". Allowed values are: BYOD, COPE, or empty.`
  - `Invalid value for column "fractionUsage". The value must be a number between 0 and 1.`
- FR-7: Physical equipment export includes the new columns; `fractionUsage` holds the effective fraction (defaults exported even if not provided).
- FR-8: New Key Indicators `BYOD Devices (%)` / `Appareils BYOD (%)` and `COPE Devices (%)` / `Appareils COPE (%)`, computed on equipment quantity within the `DEVICES` item category.
- NFR-1: Backward compatibility — files/inventories without the columns compute unchanged (100%).

Note: source text uses English aliases `usageContext` / `usageFraction` in prose; the CSV column names are `contexteUsage` / `fractionUsage` (per AC1 and sample file).

## 4. Acceptance criteria (authoritative, condensed)

| ID | Condition | Expected |
|---|---|---|
| AC1 | Import file with contexteUsage/fractionUsage | Columns optional; allowed values validated; errors in rejected report with labels of FR-6 |
| AC2 | Both empty | Professional, 100% |
| AC3 | BYOD, fraction empty | 57% on all lifecycle stages |
| AC4 | COPE, fraction empty | 85% on all lifecycle stages |
| AC5 | fractionUsage provided | Overrides context default on all lifecycle stages |
| AC6 | Non-Devices with values | Silently ignored (no rejection), impacts at 100% (Q1); Device status per `item_category` (A-1) |
| AC7 | Export after calculation | New columns exported; effective fraction incl. defaults |
| AC8 | Configure Key Indicators | BYOD/COPE Devices (%) EN/FR available, quantity share within Devices |
| AC9 | Legacy files without columns | Unchanged calculation; 100% for all |

Sample test file (in source): 8 rows covering empty (CompanyLaptop), COPE (SharedLaptop), BYOD (PersonalSmartphone), COPE+override 0.5 (SharedTablet), non-Devices with COPE (SharedComputeServer) and with 0.75 (SharedStorageServer), invalid context `Unkown` (ErrorContextUsage), invalid fraction 2.0 (ErrorFractionUsage). Header contains a leading space before `contexteUsage` — header whitespace is ignored (confirmed by user, Q6).

## 5. Open questions (deferred to `clarification` station — non-blocking here)

| # | Question | Status |
|---|---|---|
| Q1 | AC6: are contexteUsage/fractionUsage on non-Devices equipment silently ignored or rejected (with which error label)? | resolved — silently ignored (no rejection; avoids blocking the user); impacts computed at 100% (user-confirmed 2026-11-20) |
| Q2 | Is the fraction applied to quantity before calculation or to computed impacts after calculation (incl. electricity consumption outputs)? | resolved — applied upstream of the calculation (like VM allocation): only a fraction of the equipment is considered, so all impacts AND electricity consumption reflect that fraction, not the full equipment (user-confirmed 2026-11-20) |
| Q3 | Does allocation apply to digital-service terminals that share `in_physical_equipment`? | resolved — not concerned; BYOD/COPE applies only to inventory physical equipment (user-confirmed 2026-11-20) |
| Q4 | Is the effective fraction stored (input/output tables) or recomputed at export? | resolved — export fills `fractionUsage` when it was provided in input, or when a BYOD/COPE context was set on a Devices equipment; in that case the applied default (57%/85%) used in the calculation is exported, for transparency (user-confirmed 2026-11-20) |
| Q5 | Are BYOD/COPE % indicators weighted by quantity over Devices only (AC8 suggests yes) — confirm denominator? | resolved — as stated in source FR-8/AC8: share of equipment quantity within the Devices category (no further decision needed) |
| Q6 | Should header parsing tolerate the leading space seen in the sample file (` contexteUsage`)? | resolved — header whitespace is trimmed/ignored (user-confirmed 2026-11-20) |

## 6. Assumptions (confirmed)

- A-1: "Devices" is resolved from the new referential column `ref_item_type.item_category` (value `DEVICES`), introduced by G4IT-42 (commit 2823b050). An equipment is a Device when its item type — matched by `ref_default_item` reference first, then by `type`, with workspace then organization overrides taking precedence (same resolution as `InventoryIndicatorService.getEquipmentIndicators`) — has `item_category = 'DEVICES'`. Unmatched types default to `INFRASTRUCTURE` (not subject to allocation). Other values: `INFRASTRUCTURE`, `EXTERNAL`. (confirmed by user 2026-11-20)
- A-1b: The resolution logic should be reused/factored rather than duplicated between indicators and evaluation. Backfill rule (Liquibase `backfill-item-type-item-category`): types terminal, smartphone, mobile phone, laptop, desktop, monitor → DEVICES; workspaces can override via the referential import (`item_category` CSV column, mandatory).
- A-1c: `item_category` is NOT restricted to `DEVICES`/`INFRASTRUCTURE`/`EXTERNAL` — consistent with `ItemType.isValidItemCategory` (any non-blank value accepted, e.g. `Hardware` in test data). G4IT-48 adds no new validation; only `DEVICES` triggers BYOD/COPE allocation, any other value is treated as non-Devices. (user decision 2026-11-20)
- A-2: Values are case-sensitive, uppercase exactly as listed (`BYOD`, `COPE`). (confirmed by user 2026-11-20)

## 7. Handoff

All questions Q1–Q6 resolved by the user; ready for `spec-feature` (clarification will confirm no remaining ambiguity). Scope must not be added, removed or reinterpreted beyond the authoritative source.
