---
workflow: feature-implementation
trigger: user
date: '2026-11-20'
status: review
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: architecture-review
agent: "general-purpose substitute for architecture-governance (user-approved)"
agent_note: "Authorized substitute; declared architecture-governance agent could not write in this runtime."
inputDocuments:
  - .github/skills/architecture-guardrails/SKILL.md
  - .github/skills/architecture-guardrails/resources/java-guardrails.md
  - .github/skills/architecture-guardrails/resources/monolith-guardrails.md
  - outputs/specs/features/G4IT-48/spec.md
  - outputs/specs/features/G4IT-48/clarifications.md
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/mapper/InternalToNumEcoEvalCalculs.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/InPhysicalEquipment.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/OutPhysicalEquipment.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apireferential/modeldb/ItemType.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apireferential/business/ReferentialImportService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apireferential/business/WorkspaceReferentialImportService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/mapper/CsvToInMapper.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/business/asyncloadservice/LoadFileService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/common/utils/CsvUtils.java
  - services/backend/src/main/resources/swagger/greenit/api-in-out/components-in-out.yml
  - services/backend/src/main/resources/swagger/greenit/api-in-out/inventory-input-items.yml
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/mapper/InputToCsvRecord.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/common/filesystem/model/CsvFileMapperInfo.java
  - services/backend/volume_generation/input_template/2-physical-equipment.csv
  - services/documentation/content/2-functional-documentation/use_cases/uc_inventory/uc3_load_files.md
  - services/documentation/content/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md
  - services/documentation/content/2-functional-documentation/use_cases/uc_inventory/uc7_export_files.md
  - .github/knowledge/governance/ai-disclaimer-text.md
changeHistory:
  - date: '2026-11-20'
    description: 'Initial architecture review of the G4IT-48 feature specification against architecture guardrails and current code.'
    changes:
      - 'Reviewed category resolution, evaluation and persistence seams, CSV ingestion/export, REST contracts, and Hugo documentation impacts.'
      - 'Recorded findings and independent verification candidates without editing the specification or implementation.'
holisticQualityRating: adequate
overallStatus: warning
sensitivity: internal
context_manifest:
  - path: .github/skills/architecture-guardrails/SKILL.md
    key_facts:
      - 'The review must identify the architecture style and primary stack, load matching guardrail resources, then evaluate the spec against them.'
      - 'The required output is an architecture guardrail assessment included in the review.'
  - path: outputs/specs/features/G4IT-48/spec.md
    key_facts:
      - 'Only exact item_category DEVICES qualifies; A-1c says any nonblank category is accepted, and A-1b requires shared resolution.'
      - 'BYOD/COPE defaults are 0.57/0.85; FR-009 applies the fraction before calculation and FR-015 persists the evaluated effective fraction.'
      - 'FR-012/018 define indicator ratios using the existing equipment quantity aggregation; FR-014 requires Hugo docs and empty columns in templates/sample files.'
  - path: outputs/specs/features/G4IT-48/clarifications.md
    key_facts:
      - 'CQ-1 puts effective fraction persistence at evaluation time and says export/indicators reuse it.'
      - 'AS-2 requires whole-line rejection for any invalid field; valid values on non-Devices are ignored.'
      - 'CQ-3 adds both columns empty to CSV templates and samples; CQ-4 preserves the existing quantity and aggregation behavior.'
  - path: .github/skills/architecture-guardrails/resources/java-guardrails.md
    key_facts:
      - 'The Java guardrails separate Domain, Application, Infrastructure and API/Web dependencies.'
      - 'REST request DTOs should use Bean Validation; schema evolution should use managed migrations, and application/domain tests should cover the change.'
  - path: .github/skills/architecture-guardrails/resources/monolith-guardrails.md
    key_facts:
      - 'A modular monolith should preserve module boundaries and expose module behavior through public interfaces.'
      - 'Cross-cutting shared-kernel code should remain limited; avoid direct access to other modules’ internals and circular dependencies.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apireferential/modeldb/ItemType.java
    key_facts:
      - 'item_category is a required persisted ItemType field.'
      - 'isValidItemCategory accepts any non-null, nonblank string; it does not enforce a fixed category enumeration.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java
    key_facts:
      - 'Equipment indicators are obtained before the service enriches their impacts with item type and item category.'
      - 'Category resolution prefers refDefaultItem, then equipment type; workspace overrides are ordered ahead of organization defaults.'
      - 'An unmatched item type is assigned INFRASTRUCTURE after indicator calculation.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java
    key_facts:
      - 'The evaluator resolves matching model and item type, then calls the calculation engine for each lifecycle step and criterion.'
      - 'The category-enrichment logic shown in InventoryIndicatorService is not a shared pre-calculation resolver in this path.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/InPhysicalEquipment.java
    key_facts:
      - 'The inventory input entity persists quantity and electricityConsumption along with physical equipment attributes.'
      - 'It currently has no usage-context or effective-fraction property.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/OutPhysicalEquipment.java
    key_facts:
      - 'The evaluated output entity stores impact, quantity and electricity-related result fields per task.'
      - 'It currently has no usage-context or effective-fraction property.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/mapper/CsvToInMapper.java
    key_facts:
      - 'Physical-equipment CSV columns are mapped by named header lookup into generated REST DTOs.'
      - 'Existing numeric input conversion uses Double parsing, so new range checks and row-level parse errors need an explicit validation path.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/business/asyncloadservice/LoadFileService.java
    key_facts:
      - 'CSV ingestion parses and checks header names before dispatching physical-equipment records to row loading.'
      - 'Rejected rows are grouped by line number and written with accumulated error messages to a rejected-file CSV.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/common/utils/CsvUtils.java
    key_facts:
      - 'CsvUtils.read requires the requested header to be mapped and reads values without normalizing header names.'
      - 'readDouble delegates to Double.parseDouble and does not itself produce a localized LineError.'
  - path: services/backend/src/main/resources/swagger/greenit/api-in-out/components-in-out.yml
    key_facts:
      - 'InPhysicalEquipmentRest is defined by the OpenAPI schema and currently has no contexteUsage or fractionUsage properties.'
      - 'Existing numeric input properties express constraints directly in the schema, such as minimum and maximum values.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/mapper/InputToCsvRecord.java
    key_facts:
      - 'The input physical_equipment CSV export is assembled from persisted input equipment fields.'
      - 'The current input CSV row mapping does not include usage context or effective fraction.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/common/filesystem/model/CsvFileMapperInfo.java
    key_facts:
      - 'CSV column names are configured separately by file type, including physical-equipment inputs and impact indicators.'
      - 'Input and evaluated-indicator exports therefore have distinct header mappings.'
  - path: services/backend/volume_generation/input_template/2-physical-equipment.csv
    key_facts:
      - 'The current downloadable inventory template has a fixed physical-equipment header and sample row without the two proposed usage columns.'
  - path: services/documentation/content/2-functional-documentation/use_cases/uc_inventory/uc3_load_files.md
    key_facts:
      - 'The loading page documents supported formats, status flow, validation errors and the rejected-file download.'
      - 'Physical equipment row validation is described in the upload process documentation.'
  - path: services/documentation/content/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md
    key_facts:
      - 'The Equipment view page documents existing equipment key indicators and their data context.'
  - path: services/documentation/content/2-functional-documentation/use_cases/uc_inventory/uc7_export_files.md
    key_facts:
      - 'The export page distinguishes inventory input CSVs, including physical_equipment.csv, from evaluated indicator CSVs.'
  - path: .github/knowledge/governance/ai-disclaimer-text.md
    key_facts:
      - 'The canonical English disclaimer is a three-line blockquote beginning with the AI-generated-content statement.'
---
<!-- AI_DISCLAIMER v1.0 -->
# Architecture Review: G4IT-48 — BYOD/COPE usage allocation

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

CONTEXT LOADED: 21 source/evidence entries read, context manifest prepared.

**Review scope:** Architecture guardrails, feature specification and clarifications, and current backend/API/CSV/documentation seams. This is a design review; it does not assert that the feature implementation exists or is defective.

**Execution note:** This general-purpose executor is the user-approved substitute for the declared architecture-governance agent, which could not write in this runtime. No code, workflow-state file, or engine CLI was changed or run. All file/search tools were local; no external resource was accessed.

## Architecture baseline

- **Style:** Spring Boot backend organized as capability packages (`apiinout`, `apiloadinputfiles`, `apievaluating`, `apiindicator`, `apireferential`), with Angular frontend and Hugo documentation. The relevant backend integration is an in-process application flow; a new service boundary is not indicated.
- **Stack:** Java 21 / Spring Boot 4 backend; OpenAPI schemas generate REST DTOs. Liquibase changelogs manage database schema.
- **Applicable guardrails:** Keep the feature inside existing capability boundaries; put shared category resolution in a reusable application-level seam rather than copying rules into the evaluator and indicator services. Propagate contract changes through OpenAPI, generated DTOs, persistence and mappers. Use explicit DTO validation and tests at import/evaluation/export seams.

## Findings and recommendations

| ID / severity | Finding | Actionable recommendation |
|---|---|---|
| AR-01 / **High** | **Evaluation-time category resolution is not an existing shared pre-calculation seam.** `InventoryIndicatorService` currently enriches already-calculated indicators; `EvaluateNumEcoEvalService` separately resolves matching model and item type while assembling engine requests. The spec requires category-based allocation before calculation and shared resolution (A-1b). | Define one resolver used before evaluating inventory equipment, with the specified order: `ref_default_item`, then type; workspace override before organization; unmatched means `INFRASTRUCTURE`. Have both evaluation and indicator logic consume that resolved identity/category instead of implementing parallel precedence rules. |
| AR-02 / **High** | **FR-015’s persistence boundary is underspecified.** The current input entity stores source quantity/electricity, while evaluated output is task-scoped and stores impact/quantity fields; neither currently stores usage context or effective fraction. The spec does not name which record is authoritative across re-evaluation or how it remains aligned with exported impact rows. | Specify the evaluated result’s persistence model and lifecycle (including re-evaluation and legacy inventories). Preserve source `InPhysicalEquipment` values; persist the effective fraction alongside the evaluated result rather than overwriting the user’s input. Add nullable fields through append-only migrations if the selected design needs schema changes. |
| AR-03 / **High** | **Allocation and indicator denominator can conflict if upstream scaling changes source quantity.** FR-009 calls for scaling impacts and electricity; FR-012/018 require adoption ratios using the existing quantity field/aggregation. A single scaled quantity cannot safely represent both allocated impact and total device count. | Keep an unallocated equipment quantity for denominator and BYOD/COPE numerator; carry the effective fraction as a separate value and apply it only to calculation inputs/impact quantities and electricity as methodologically required. Specify zero, fractional and empty quantities in tests against existing aggregation semantics. |
| AR-04 / **Medium** | **CSV header normalization and row-level validation need an explicit import design.** Current header lookup is exact (`CsvUtils.read`); the loader checks parsed headers before row loading. Physical numeric parsing can throw before a `LineError` is created. FR-002 and FR-003/004 instead require whitespace-tolerant headers and per-row errors. | Normalize headers once at the CSV parser boundary while preserving the original row for rejected-file output. Validate context and fraction before persistence; return all applicable errors for a row and reject that entire row without turning malformed numeric text into a whole-file failure. Test leading/trailing header spaces, both invalid columns, and one rejected row with two messages. |
| AR-05 / **Medium** | **“Physical equipment export” can refer to distinct products.** Current evaluated downloads distinguish the raw/input `physical_equipment.csv` from `ind_physical_equipment.csv`; the former is mapped from input entities and the latter contains calculated indicators. FR-011 and Q-NEW-1 do not identify which artifact exposes the effective fraction. | Name the export file/API contract explicitly. Define whether provided values, derived defaults, and ignored non-Device values appear in the raw input export, evaluated indicator export, or both; align headers, schema, serialization, and acceptance tests. |
| AR-06 / **Medium** | **REST parity is undecided.** Physical-equipment inventory inputs have CRUD endpoints and an OpenAPI `InPhysicalEquipmentRest` schema, but FR-001 is phrased as a CSV import-model change and NFR-T3 leaves API impact conditional. | Explicitly choose CSV-only or REST parity. If REST supports these fields, add nullable schema properties, validate case-sensitive `BYOD`/`COPE` and numeric `[0,1]` at the request boundary, update generated DTO/mappers and persistence, and cover GET/POST/PUT plus OpenAPI validation. Keep non-Devices’ valid values governed by Q1 without weakening DTO bounds. |
| AR-07 / **Medium** | **Error message exactness and bilingual requirements need reconciliation.** FR-006 specifies exact English labels, while NFR-UX3 requires EN and FR. Existing import validation uses `MessageSource`/locale-aware errors. | Define stable message keys with exact English text and French translations; state whether exact-text acceptance tests run in English only or in each locale. Verify the locale used for rejected-file reports. |
| AR-08 / **Medium** | **FR-014 is verifiable but not yet an implementation map.** Current Hugo sources include inventory load, equipment visualization and export pages; the template is separately maintained. FR-014 asks for FR/EN where both pages exist, CSV templates/samples and a release note. | Make the plan/task inventory the actual Hugo source pages and language variants, plus generated/downloadable templates and sample CSVs. Include the release note and quality-gate checks for each in-scope artifact; avoid assuming a language variant exists without checking the Hugo configuration/content tree. |

### G4IT-42 category compatibility

The spec’s category rule matches the current referential contract: `ItemType.isValidItemCategory` accepts any nonblank value and does not impose a closed enumeration. Preserve that behavior. Only an exact `DEVICES` value should activate allocation; values such as `Devices`, other valid custom category strings, and unmatched types must remain non-Devices (with unmatched resolution treated as `INFRASTRUCTURE`). Do not reuse the BYOD/COPE context validation as an item-category validator.

## Independent verification candidates (not conclusions)

1. Confirm the exact persistence target for the effective fraction and prove evaluation does not mutate source input.
2. Verify upstream allocation leaves the equipment quantity denominator and original electricity value available for indicators/audit.
3. Confirm whether the current post-calculation category enrichment can be factored into a shared pre-calculation resolver without changing G4IT-42 precedence.
4. Confirm which named export receives the effective fraction and how non-Device values are represented.
5. Decide whether REST CRUD supports these fields; if so, validate constraints in OpenAPI and generated request DTOs, not only in CSV handling.
6. Reconcile FR-006 exact labels with EN/FR localization and specify locale-sensitive test expectations.
7. Verify whole-line rejection and rejected-report semantics, including error-message count versus rejected-row count when two fields fail.
8. Inventory the Hugo pages, templates, samples, release-note location and actual FR/EN language variants.
9. Centralize default fractions, accepted contexts and category sentinel/qualification values so evaluation and indicators cannot drift.
10. Verify the pre-feature and no-Devices paths retain their existing empty/no-data behavior, including before any successful evaluation.

## STATION RESULT

- **Status:** warning — architecture review complete; resolve AR-01–AR-03 before implementation design is finalized.
- **Output:** `outputs/specs/features/G4IT-48/architecture-review.md` — architecture findings, recommendations, G4IT-42 compatibility and verification candidates.
- **Gate self-check:** Required skill/spec/clarifications read; relevant Java/modular-boundary guardrails and current code/contracts reviewed; metadata, trace, context manifest and AI disclaimer included. No code/spec/workflow-state edits; no engine CLI or external tools used.
- **Open questions:** Effective-fraction persistence target and export artifact; REST parity; bilingual error-label behavior; concrete Hugo page/language/template inventory; how denominator quantities survive fraction application.
