---
workflow: feature-implementation
trigger: user
date: '2026-11-20'
status: validated
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: specification
agent: spec-orchestrator (substitute executor, user-approved; declared agent lacks write tools)
skill: spec-feature
sensitivity: internal
inputDocuments:
  - outputs/specs/constitution.md
  - outputs/specs/features/G4IT-48/requirements-brief.md
  - outputs/specs/features/G4IT-48/context-brief.md
changeHistory:
  - date: '2026-11-20'
    description: 'Initial feature specification for G4IT-48 (BYOD/COPE usage allocation)'
    changes:
      - 'Derived scope, user stories, FR/NFR and success criteria from the authoritative requirements brief (AC1–AC9, Q1–Q6, A-1/A-1b/A-1c/A-2)'
      - 'Aligned Devices classification on item category introduced by G4IT-42 (commit 2823b050); context brief treated as partly stale'
  - date: '2026-11-20'
    description: 'User confirmed assumptions for Q-NEW-1 and Q-NEW-2'
    changes:
      - 'Q-NEW-1: non-Devices lines export contexteUsage/fractionUsage as provided'
      - 'Q-NEW-2: no Devices in inventory -> existing empty-indicator ("no data") convention'
  - date: '2026-11-20'
    description: 'Added FR-014: update Hugo documentation (final spec) from this spec'
    changes:
      - 'Dedicated documentation task required in plan/tasks, verified at quality-gate'
  - date: '2026-11-20'
    description: 'Integrated clarification decisions (clarifications.md)'
    changes:
      - 'FR-014 extended (CSV templates/samples); added FR-015 to FR-018 (CQ-1..CQ-4, whole-line rejection)'
      - 'Edge cases updated; resilience items CLQ-013/014/019/020 out of scope'
  - date: '2026-11-20'
    description: 'Applied user decisions on CI/deployment boundary, REST validation and documentation language'
    changes:
      - 'Deployment is handled by a separate repository from the Docker registry; this repository owns CI only'
      - 'REST validation follows existing-field validation conventions only'
      - 'Hugo documentation and release notes are English-only'
  - date: '2026-11-23'
    description: 'Applied explicit implementation-boundary clarifications'
    changes:
      - 'Update existing English technical and functional Hugo documentation without a feature-specific release note or release version'
      - 'Defer local Docker image compilation; frontend and backend code compilation remain required, with no Dockerfile change planned'
      - 'Perform manual frontend accessibility validation after implementation, not as an implementation prerequisite'
  - date: '2026-11-27'
    description: 'User selected a distinct spreadsheet-safe export for SAST-002'
    changes:
      - 'Preserve raw CSV exchange values and the existing ZIP download'
      - 'Add an explicitly text-typed XLSX download and make it the spreadsheet-oriented UI action'
      - 'Keep raw CSV available with an explicit formula-risk warning rather than silently changing source values'
holisticQualityRating: not-rated
overallStatus: not-reviewed
context_manifest:
  - path: .github/skills/spec-feature/SKILL.md
    mode: full
    key_facts:
      - 'Brief declared as input must have validation_state complete before writing spec.md'
      - 'Standard feature uses resources/feature-spec-template.md; UI work requires an accessibility section (WCAG 2.2 AA, EN 301 549)'
      - 'Distinguish confirmed requirements from assumptions; testable acceptance criteria'
  - path: outputs/specs/constitution.md
    mode: full
    key_facts:
      - 'Status validated; allocation rules must be explicit, default-driven with optional override'
      - 'Schema changes only through new append-only changesets; i18n EN/FR for user-facing messages'
      - 'Coverage 80% new code / 100% impact paths; WCAG 2.2 AA; API p95 < 500ms'
  - path: outputs/specs/features/G4IT-48/requirements-brief.md
    mode: full
    key_facts:
      - 'validation_state complete, authoritative; defaults BYOD 0.57, COPE 0.85, empty 1.0; fractionUsage in [0,1] overrides'
      - 'Q1 non-Devices values silently ignored at 100%; Q2 fraction applied upstream (impacts and electricity); Q3 digital services excluded; Q4 export effective fraction'
      - 'A-1 Devices = item type item_category DEVICES (ref_default_item then type, workspace > organization, unmatched → INFRASTRUCTURE); A-2 values case-sensitive uppercase'
  - path: outputs/specs/features/G4IT-48/context-brief.md
    mode: partial (header + manifest + architecture overview)
    key_facts:
      - 'Physical equipment input records are shared by inventories and digital services'
      - 'Written before G4IT-42 merge: open questions there are superseded by the brief'
  - path: git show 2823b050 --stat + InventoryIndicatorService.java
    mode: partial (targeted)
    key_facts:
      - 'G4IT-42 added ItemType.itemCategory, a changelog adding/backfilling item_category, and EquipmentImpactBO.itemCategory'
      - 'getEquipmentIndicators sets itemCategory to INFRASTRUCTURE when no item type matches'
  - path: .github/knowledge/governance/ai-disclaimer-text.md
    mode: full
    key_facts:
      - 'Disclaimer v1.0, canonical English three-line text'
---
<!-- AI_DISCLAIMER v1.0 -->
# Feature Specification: G4IT-48 — Support BYOD and COPE equipment usage allocation

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

CONTEXT LOADED: 6 documents read, context manifest prepared.

**Feature Branch**: `G4IT-48`
**Created**: 2026-11-20
**Status**: Validated
**Input**: Authoritative requirements brief `outputs/specs/features/G4IT-48/requirements-brief.md` (source `imputs/feature-g4it-48.txt`)

**has_screens**: true (new Key Indicators in the inventory Equipment view; no new screen)
**has_batches**: false
**has_notifications**: false

## Summary

Inventory managers can declare, per physical equipment line of an inventory, whether an end-user device is used under a BYOD (Bring Your Own Device) or COPE (Corporate Owned, Personally Enabled) context, and optionally an explicit professional usage fraction. When the equipment is a Device (item category `DEVICES`), the calculation allocates only the professional share of the equipment (default 57% BYOD, 85% COPE, 100% otherwise, or the explicit fraction) across all lifecycle stages, as required by the RCP SI methodology. The effective fraction is exported for transparency, and two new Key Indicators show BYOD and COPE adoption among Devices. Existing files and inventories without these columns behave exactly as today.

## Scope

### In Scope

- Two optional columns in the physical equipment import file: `contexteUsage` and `fractionUsage` (header whitespace ignored).
- Validation of these values with explicit rejected-file report errors.
- Allocation of the effective professional fraction, upstream of the calculation, for inventory physical equipment whose item category is `DEVICES`, on all lifecycle stages and on electricity consumption.
- Export of both columns in the physical equipment export, with the effective fraction.
- Key Indicators `BYOD Devices (%)` / `Appareils BYOD (%)` and `COPE Devices (%)` / `Appareils COPE (%)` in the inventory Equipment view.
- Full backward compatibility for files and inventories without the columns.

### Out of Scope

- Infrastructure equipment, external/cloud services, AI services (any item category other than `DEVICES`).
- Digital-service terminals and digital-service physical equipment (Q3).
- Any change to item category values, their validation, or the item-category referential (A-1c; owned by G4IT-42).
- Any new usage context other than `BYOD` and `COPE`, and any case-insensitive matching (A-2).
- Resilience to external service unavailability, slow integrations/retries, and concurrent edits (CLQ-013, CLQ-014, CLQ-019, CLQ-020) — existing behaviour unchanged (user decision 2026-11-20).

## Confirmed decisions and assumptions

| ID | Decision | Source |
|---|---|---|
| D-Q1 | `contexteUsage`/`fractionUsage` on non-Devices are silently ignored (no rejection); impacts at 100% | Brief Q1 |
| D-Q2 | Fraction applied upstream of the calculation (like VM allocation): all impacts and electricity consumption reflect the fraction | Brief Q2 |
| D-Q3 | Digital-service terminals not concerned | Brief Q3 |
| D-Q4 | Export `fractionUsage` = provided value, or applied default (0.57/0.85) when a BYOD/COPE context was set on a Device | Brief Q4 |
| D-Q5 | BYOD/COPE % = share of equipment quantity within Devices | Brief Q5 / AC8 |
| D-Q6 | Leading/trailing whitespace in CSV headers is ignored | Brief Q6 |
| A-1 | Device ⇔ resolved item type has `item_category = DEVICES`; resolution by default-item reference first, then by type, workspace override before organization; unmatched → `INFRASTRUCTURE` (same rule as the existing equipment indicators introduced by G4IT-42) | Brief A-1 |
| A-1b | The item-type/category resolution is shared between indicators and evaluation (single rule, not duplicated) | Brief A-1b |
| A-1c | Any non-blank item category is accepted; only exactly `DEVICES` triggers allocation | Brief A-1c |
| A-2 | `BYOD`, `COPE` case-sensitive, uppercase | Brief A-2 |

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Allocate professional share of shared Devices (Priority: P1)

As an inventory manager, I import physical equipment with a usage context and/or fraction so that the footprint of BYOD/COPE Devices reflects only their professional use.

**Why this priority**: Core methodological compliance (RCP SI); without it impacts are overestimated.

**Independent Test**: Import the sample file, run the evaluation, and compare each Device's impacts and electricity consumption with the same equipment at 100%.

**Acceptance Scenarios**:

1. **Given** a Device with both columns empty, **When** the inventory is evaluated, **Then** it is professional and 100% of impacts are counted (AC2).
2. **Given** a Device with `contexteUsage=BYOD` and empty fraction, **When** evaluated, **Then** 57% of impacts on every lifecycle stage (manufacturing, distribution, use, end-of-life) and 57% of electricity consumption are counted (AC3, D-Q2).
3. **Given** a Device with `COPE` and empty fraction, **When** evaluated, **Then** 85% is applied on every lifecycle stage (AC4).
4. **Given** a Device with `COPE` and `fractionUsage=0.5`, **When** evaluated, **Then** 50% is applied on every lifecycle stage (AC5).
5. **Given** a Device with empty context and `fractionUsage=0.3`, **When** evaluated, **Then** 30% is applied (AC5).
6. **Given** equipment whose item category is not `DEVICES` (or unmatched) with `COPE` or `0.75`, **When** imported and evaluated, **Then** the line is not rejected, the values are ignored and 100% is counted (AC6, D-Q1).

---

### User Story 2 — Get clear validation errors (Priority: P1)

As an inventory manager, I get explicit errors in the rejected-file report when usage values are invalid.

**Why this priority**: Data quality; prevents silent mis-allocation.

**Independent Test**: Import lines with `Unkown`, `byod`, `2.0`, `-0.1`, `abc` and check the rejected report.

**Acceptance Scenarios**:

1. **Given** `contexteUsage` not in {`BYOD`, `COPE`, empty} (e.g. `Unkown`, `byod`), **When** imported, **Then** the line is reported with `Invalid value for column "contexteUsage". Allowed values are: BYOD, COPE, or empty.` (AC1, A-2).
2. **Given** `fractionUsage` not a number or outside [0, 1] (e.g. `2.0`), **When** imported, **Then** the line is reported with `Invalid value for column "fractionUsage". The value must be a number between 0 and 1.` (AC1).
3. **Given** a header ` contexteUsage` with leading space, **When** imported, **Then** the column is recognized (D-Q6).

---

### User Story 3 — Export effective fraction (Priority: P2)

As an export consumer, I see the usage context and the effective fraction actually applied.

**Why this priority**: Transparency and auditability.

**Independent Test**: Export after evaluating the sample file and inspect the two columns.

**Acceptance Scenarios**:

1. **Given** a Device with `BYOD` and no fraction, **When** exported, **Then** `contexteUsage=BYOD` and `fractionUsage=0.57` (AC7, D-Q4).
2. **Given** a Device with `COPE` and `0.5`, **When** exported, **Then** `fractionUsage=0.5`.
3. **Given** a line where both were empty, **When** exported, **Then** both columns are present and empty.
4. **Given** a non-Device line, **When** exported, **Then** `contexteUsage` and a provided `fractionUsage` are exported as provided; no default is derived. *(Q-NEW-1, confirmed)*

---

### User Story 4 — Monitor BYOD/COPE adoption (Priority: P2)

As an inventory manager, I add Key Indicators showing the share of BYOD and COPE among my Devices.

**Why this priority**: Visibility on adoption; not required for correct impacts.

**Independent Test**: Configure the indicators on an evaluated inventory and check values against quantities.

**Acceptance Scenarios**:

1. **Given** the Equipment view, **When** configuring Key Indicators, **Then** `BYOD Devices (%)` / `Appareils BYOD (%)` and `COPE Devices (%)` / `Appareils COPE (%)` are available per UI language (AC8).
2. **Given** Devices quantities 10 total, 2 BYOD, 3 COPE, **When** displayed, **Then** values are 20% and 30% (D-Q5).
3. **Given** no Devices in the inventory, **When** displayed, **Then** no division error occurs and the indicator shows the existing "no data" convention. *(Q-NEW-2, confirmed)*

---

### User Story 5 — Backward compatibility (Priority: P1)

**Acceptance Scenarios**:

1. **Given** a legacy file without the columns, **When** imported and evaluated, **Then** results are identical to pre-feature results (100% for all) (AC9, NFR-1).
2. **Given** an existing inventory evaluated before the feature, **When** re-evaluated without new data, **Then** results are unchanged.

---

### Edge Cases

- `fractionUsage = 0` on a Device → valid; zero impact and zero electricity counted.
- `fractionUsage = 1` with `BYOD` → 100% (explicit override wins).
- Decimal separator: values parsed with the same numeric conventions as other numeric import columns.
- Device whose item type resolves via workspace override to a non-`DEVICES` category → treated as non-Device (A-1).
- Item category with a different case (e.g. `Devices`) → non-Device (A-1c: only exactly `DEVICES`).
- Physical equipment belonging to a digital service carrying the values → ignored (D-Q3).
- Both an invalid context and an invalid fraction on one line → both errors reported; the whole line is rejected (FR-017).
- A Devices line with any invalid field → entire line rejected and listed in the rejected-file report (FR-017).
- Empty or decimal quantity → counted in BYOD/COPE % exactly as in existing equipment indicators (FR-018).
- Fraction displayed in indicators vs exported → indicators use existing percentage rounding; export writes plain decimal (FR-016).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The physical equipment import model MUST accept two optional columns `contexteUsage` and `fractionUsage`; files without them MUST remain valid.
- **FR-002**: Header matching MUST ignore leading/trailing whitespace.
- **FR-003**: `contexteUsage` MUST accept only `BYOD`, `COPE` or empty (case-sensitive); otherwise the line MUST be rejected with the exact FR-006 label.
- **FR-004**: `fractionUsage` MUST accept only empty or a number in [0, 1] inclusive; otherwise rejected with the exact FR-006 label.
- **FR-005**: The effective fraction for a Device MUST be: provided `fractionUsage` if present; else 0.57 for `BYOD`, 0.85 for `COPE`; else 1.0.
- **FR-006**: Error labels (rejected-file report) MUST be exactly: `Invalid value for column "contexteUsage". Allowed values are: BYOD, COPE, or empty.` and `Invalid value for column "fractionUsage". The value must be a number between 0 and 1.`
- **FR-007**: Device status MUST be determined by the shared item-type resolution rule (A-1, A-1b): only `item_category = DEVICES` qualifies; unmatched types are non-Devices.
- **FR-008**: For non-Devices, valid `contexteUsage`/`fractionUsage` values MUST be ignored silently for calculation (effective fraction 1.0), without rejection.
- **FR-009**: The effective fraction MUST be applied upstream of the calculation so every lifecycle stage impact and the electricity consumption reflect it.
- **FR-010**: Allocation MUST apply only to inventory physical equipment, never to digital-service equipment.
- **FR-011**: The physical equipment export MUST include `contexteUsage` and `fractionUsage`, with `fractionUsage` holding the provided value or, for a Device with BYOD/COPE context and no value, the applied default.
- **FR-012**: The Equipment view MUST offer the Key Indicators `BYOD Devices (%)` and `COPE Devices (%)` (FR labels `Appareils BYOD (%)`, `Appareils COPE (%)`), computed as quantity of Devices with that context ÷ total quantity of Devices.
- **FR-013**: Existing inventories and files without the columns MUST yield unchanged results.
- **FR-014** (user requests 2026-11-20 and 2026-11-23): The existing English technical and especially functional Hugo documentation (`services/documentation/content/`), which is the final functional specification, MUST reflect the implemented changes: new CSV columns `contexteUsage`/`fractionUsage` and validation rules, BYOD/COPE allocation rules and defaults (57%/85%, Devices only via `item_category = DEVICES`), export behaviour, and new key indicators. `plan.md`/`tasks.md` MUST contain a dedicated documentation task, and quality-gate MUST verify it. Downloadable CSV templates and sample files MUST also gain the two new columns (empty) (CQ-3). No feature-specific release note, changelog entry, or release-version selection is required. Application version assignment happens later through the normal merge/release process. Do not create or update French Hugo pages for this feature.
- **FR-015** (CQ-1): The effective fraction MUST be determined at evaluation time and persisted with the evaluated equipment result; export and indicators MUST reuse this persisted value.
- **FR-016** (CQ-2): Indicators MUST use the same rounding as existing percentage indicators; the export MUST write the plain decimal value (e.g. 0.57, 0.85, 0.5).
- **FR-017** (AS-2 confirmed): A line with any invalid field MUST be rejected entirely and reported in the rejected-file report. Values on non-Devices lines are ignored (Q1), not invalid.
- **FR-018** (CQ-4): BYOD/COPE % indicators MUST use the same quantity field and aggregation as existing equipment indicators, with no special case.
- **FR-019** (user decision 2026-11-27, SAST-002 option 1): Provide a separate XLSX export of the stored inventory CSV artifacts, with every cell explicitly typed as text, never a formula or hyperlink. Preserve cell text, persisted evaluation values and existing raw CSV ZIP bytes. Offer XLSX as the spreadsheet-oriented UI action and retain raw CSV for machine exchange behind a formula-risk confirmation. Apply the same authentication, inventory ownership and role restrictions to both downloads. Unsupported or oversized spreadsheet content must fail explicitly rather than truncate values or fall back to unsafe CSV.

### Key Entities

- **Physical equipment (inventory)**: gains an optional usage context (BYOD/COPE/none) and an optional professional usage fraction.
- **Item type / item category**: existing referential (G4IT-42); category `DEVICES` identifies end-user devices. Not modified.
- **Effective usage fraction**: derived value (provided, default, or 1.0) applied to the equipment and exported.
- **BYOD/COPE Key Indicators**: quantity shares among Devices of an inventory.

### Non-Functional Requirements *(Constitution-Mandated)*

- **NFR-T1**: Coverage ≥ 80% on new code, 100% on the allocation/impact path (FR-005, FR-007–FR-009).
- **NFR-T2**: Integration tests cover import (valid, rejected, legacy), evaluation with fractions, export, and indicators, using the sample file rows.
- **NFR-T3**: API contract changes (if any) validated against the API definitions.
- **NFR-P1**: No measurable regression of import/evaluation duration; API p95 < 500 ms for indicator retrieval.
- **NFR-C1**: Schema evolution only through new, append-only changes with nullable new attributes.
- **NFR-UX1**: WCAG 2.2 AA for indicator labels/values (see Accessibility).
- **NFR-UX3**: Error messages and indicator labels available in EN and FR.
- **NFR-Q1**: Cyclomatic complexity ≤ 10 per method; calculation rule documented.
- **NFR-S1**: No PII in logs; existing organization/workspace authorization unchanged.

## Accessibility

- **Impact classification**: low — new labelled values in an existing Key Indicators component; no new interaction pattern.
- **Baseline**: WCAG 2.2 AA; EN 301 549 relevant (web content, clause 9).
- **Relevant criteria**: 1.3.1 Info and Relationships, 1.4.3 Contrast, 2.4.6 Headings and Labels, 3.1.2 Language of Parts (EN/FR labels), 4.1.2 Name, Role, Value.
- **Patterns/keyboard/focus**: reuse the existing Key Indicators selection and display; selection reachable and operable by keyboard with visible focus, as for existing indicators.
- **Semantics**: percentage value exposed with its label as text (not colour-only); "no data" state conveyed in text.
- **Error handling**: rejected-file report messages are textual and explicit (FR-006).
- **Test scenarios**: keyboard-only selection of new indicators; screen-reader announcement of label + value; 200% zoom.
- **Evidence**: manual accessibility validation of the implemented frontend, including screen-reader verification. Existing automated checks, where available, are supplementary and reported separately.
- **Open manual review items**: manual review follows frontend implementation and is not a prerequisite to starting or implementing the feature. Until it is performed, record it as pending and do not claim full accessibility compliance.

## Implementation verification boundary

- Compile frontend and backend code using their existing project commands and run the applicable configured tests.
- Local Docker image compilation is deferred; no Dockerfile change is planned for this feature. An image build is not a prerequisite to implementation.
- Work continues on the current feature branch. This implementation dispatch does not commit, publish, or deploy anything.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For the 8-row sample file, 2 lines are rejected with the exact labels; 6 lines are accepted.
- **SC-002**: Evaluated impacts and electricity for accepted Devices equal 100% / 85% / 57% / 50% of the unallocated values; non-Devices equal 100%.
- **SC-003**: 100% of legacy regression inventories produce identical results before/after.
- **SC-004**: Export of the sample contains the effective fractions 0.85, 0.57 and 0.5 for the corresponding Devices.
- **SC-005**: BYOD/COPE indicators match hand-computed quantity shares on test inventories.

## Open Questions

- **Q-NEW-1** (resolved, user-confirmed 2026-11-20): non-Devices lines export the provided `contexteUsage`/`fractionUsage` as-is (US3-4).
- **Q-NEW-2** (resolved, user-confirmed 2026-11-20): an inventory with no Devices shows the existing empty-indicator ("no data") convention.
