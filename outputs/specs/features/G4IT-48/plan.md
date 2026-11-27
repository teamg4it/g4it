---
workflow: feature-implementation
trigger: user
date: '2026-11-20'
status: validated
inputDocuments:
  - outputs/specs/constitution.md
  - outputs/specs/features/G4IT-48/spec.md
  - outputs/specs/features/G4IT-48/clarifications.md
  - outputs/specs/features/G4IT-48/architecture-review.md
  - .backbone-settings.json
  - services/documentation/hugo.toml
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/InPhysicalEquipment.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/OutPhysicalEquipment.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java
  - services/backend/volume_generation/input_template/2-physical-equipment.csv
  - .github/skills/spec-plan/resources/implementation-plan-template.md
  - .github/skills/spec-plan/resources/risk-template.md
  - .github/skills/spec-plan/resources/rollout-template.md
changeHistory:
  - date: '2026-11-20'
    description: 'Initial implementation plan for G4IT-48'
    changes:
      - 'Mapped existing evaluation, category resolution, persistence, CSV, API, indicator, and Hugo seams'
      - 'Recorded architecture decisions, unresolved gates, risk controls, rollout, and a dedicated documentation workstream'
  - date: '2026-11-20'
    description: 'Applied user decisions on CI/deployment boundary, REST validation, export context, non-Device values and documentation language'
    changes:
      - 'Only CI is in this repository; deployment is handled by another repository using the Docker registry populated by CI'
      - 'REST validation follows existing-field validation conventions only'
      - 'Export preserves source usage context; non-Device values are retained and exported'
      - 'Hugo documentation and release notes are English-only'
  - date: '2026-11-20'
    description: 'Reclassified resolved plan questions and isolated remaining implementation-time checks'
    changes:
      - 'Removed CI/deployment, non-Device value preservation, and documentation-language choices from unresolved gates'
      - 'Retained only concrete code/contract inspections needed before implementation'
  - date: '2026-11-23'
    description: 'Aligned the plan with explicit implementation-boundary clarifications'
    changes:
      - 'Limit documentation work to existing English technical/functional Hugo content and maintained CSV assets, without a feature release note or version'
      - 'Defer local Docker image compilation and keep frontend/backend code compilation and applicable tests'
      - 'Schedule manual frontend accessibility validation after implementation, without making it an implementation prerequisite'
  - date: '2026-11-27'
    description: 'Aligned the plan with FR-019 and the no-release-note decision after the final quality gate'
    changes:
      - 'Added the FR-019 spreadsheet-safe XLSX export component, key design decision 10 and Phase 3 delivery item (TASK-024)'
      - 'Removed remaining release-note references from the component table and Phase 1/Phase 5 checklists'
holisticQualityRating: not-rated
overallStatus: not-reviewed
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: plan
agent: general-purpose substitute
agent_note: 'User-approved substitute; the workflow-declared agent lacks write tools in this runtime.'
sensitivity: internal
context_manifest:
  - path: .github/skills/spec-plan/SKILL.md
    mode: full
    key_facts:
      - 'The plan must cover architecture, component sequencing, risks, rollout and observability.'
      - 'This repository owns CI only; deployment is managed in another repository using the Docker registry populated by CI.'
      - 'Technical stack defaults come from validated context or brownfield code evidence before settings or a user question.'
  - path: .github/instructions/output-metadata.instructions.md
    mode: full
    key_facts:
      - 'Every file under outputs requires workflow, trigger, date, status, inputDocuments, changeHistory, holisticQualityRating and overallStatus.'
      - 'New output files start with draft, not-rated and not-reviewed.'
      - 'Only a reviewing agent owns quality-rating and overall-status changes.'
  - path: outputs/specs/constitution.md
    mode: full
    key_facts:
      - 'The brownfield backend is Java 21/Spring Boot 4 with modular api* capabilities, PostgreSQL and append-only Liquibase schema changes.'
      - 'OpenAPI defines REST contracts; source CSV columns remain optional for backward compatibility; EN/FR is required for user-facing messages.'
      - 'New code coverage target is 80%, allocation paths 100%, WCAG 2.2 AA, API p95 under 500 ms and evaluation regression no greater than 20%.'
  - path: outputs/specs/features/G4IT-48/spec.md
    mode: full
    key_facts:
      - 'BYOD and COPE defaults are 0.57 and 0.85; explicit fractions in [0,1] override; only exact item_category DEVICES qualifies.'
      - 'FR-015 requires evaluation-time persistence of the effective fraction; FR-009 applies the fraction before impact and electricity calculations.'
      - 'FR-014 requires existing English technical/functional Hugo documentation, template/sample CSV columns and a quality-gate check; the user removed feature-specific release-note and version obligations on 2026-11-23.'
      - 'FR-017 rejects a whole invalid line; FR-018 preserves existing equipment-indicator quantity and aggregation semantics.'
  - path: outputs/specs/features/G4IT-48/clarifications.md
    mode: full
    key_facts:
      - 'CQ-1 places effective-fraction derivation at evaluation time and requires export and indicators to reuse persisted evaluation data.'
      - 'CQ-2 requires existing indicator rounding but plain decimal export; CQ-3 adds both columns empty to templates and samples.'
      - 'AS-2 requires whole-line rejection for invalid fields while non-Device usage values are ignored; CQ-4 keeps existing quantity aggregation.'
      - 'CLQ-013/014/019/020 resilience and concurrent-edit behaviors are explicitly out of scope.'
  - path: outputs/specs/features/G4IT-48/architecture-review.md
    mode: full
    key_facts:
      - 'AR-01 calls for one pre-calculation category resolver shared by evaluation and indicators, with reference-before-type and workspace-before-organization precedence.'
      - 'AR-02/03 require evaluated-result persistence without input mutation and preservation of original quantity for adoption denominators.'
      - 'AR-04/07 require normalized headers, accumulated row errors, whole-line rejection and localized messages.'
      - 'AR-05/06/08 flag the evaluated export artifact, REST parity, and a concrete Hugo language/page/template/release-note inventory for explicit decisions.'
  - path: .backbone-settings.json
    mode: full
    key_facts:
      - 'The project is classified as brownfield with Java and Angular technical-stack declarations.'
      - 'The repository scope includes CI; deployment belongs to a separate repository consuming its Docker registry.'
      - 'Accessibility is marked as required.'
  - path: services/documentation/hugo.toml
    mode: full
    key_facts:
      - 'Hugo sets languageCode to en-us.'
      - 'The configuration shown does not declare a second Hugo language.'
      - 'The documentation site uses the Relearn theme and relative URLs.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/InPhysicalEquipment.java
    mode: full
    key_facts:
      - 'The persistent input equipment entity currently stores source quantity and electricityConsumption.'
      - 'It has no usage-context or usage-fraction field today.'
      - 'It is the source record whose values must remain unchanged during allocation.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/OutPhysicalEquipment.java
    mode: full
    key_facts:
      - 'The output entity is task-scoped and stores quantity, impact and electricity-related result values.'
      - 'It currently has no usage-context or effective-fraction field.'
      - 'It is a candidate persistence seam for evaluated export data, subject to confirming row identity and lifecycle.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java
    mode: full
    key_facts:
      - 'The evaluator resolves matching model and item type before invoking the physical-equipment calculation service.'
      - 'The service loops lifecycle steps and criteria to construct calculation requests.'
      - 'The existing input-to-engine mapper is the point where a defensive, fraction-adjusted calculation copy can be supplied.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java
    mode: full
    key_facts:
      - 'Current equipment indicators are calculated before item category enrichment.'
      - 'Category lookup prefers a matching default-item reference, then equipment type, with workspace records ordered before organization records.'
      - 'An unmatched item type is currently represented as INFRASTRUCTURE.'
  - path: services/backend/volume_generation/input_template/2-physical-equipment.csv
    mode: full
    key_facts:
      - 'The downloadable physical-equipment template has a fixed semicolon-delimited header and sample row.'
      - 'Neither contexteUsage nor fractionUsage is present in the current template.'
      - 'The documentation workstream must update this source template and find any separately maintained sample files.'
  - path: .github/skills/spec-plan/resources/implementation-plan-template.md
    mode: full
    key_facts:
      - 'The plan structure includes technical context, constitution check, architecture, data changes, sequencing, risks, rollout, observability and complexity.'
      - 'CI and deployment responsibilities are separated: this repository builds/publishes the Docker image; another repository deploys from the registry.'
  - path: .github/skills/spec-plan/resources/risk-template.md
    mode: full
    key_facts:
      - 'Each risk should carry category, probability, impact, score, mitigation, contingency, owner and status.'
      - 'The risk matrix requires high-impact risks to have mitigation and residual-risk treatment.'
  - path: .github/skills/spec-plan/resources/rollout-template.md
    mode: full
    key_facts:
      - 'The rollout plan should state phases, success and promotion criteria, monitored metrics and rollback steps.'
      - 'Database rollback planning must address backward compatibility and data preservation.'
  - path: .github/knowledge/governance/ai-disclaimer-text.md
    mode: full
    key_facts:
      - 'The canonical English AI disclaimer is the three-line blockquote beginning with the artificial-intelligence-tools statement.'
      - 'The v1.0 marker is AI_DISCLAIMER v1.0.'
---
<!-- AI_DISCLAIMER v1.0 -->
# Implementation Plan: G4IT-48 — BYOD/COPE equipment usage allocation

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

CONTEXT LOADED: 17 source documents read, context manifest prepared.

**Branch**: `G4IT-48` | **Date**: 2026-11-20 | **Spec**: [spec.md](spec.md)

## Summary

Add optional usage context and fraction values to inventory physical equipment, derive and persist the effective allocation at evaluation time, apply it before all lifecycle and electricity calculations, and expose transparent export values and BYOD/COPE adoption indicators. Keep source inventory inputs unchanged and preserve existing behavior for legacy files and inventories.

This is a brownfield extension of the existing Spring Boot modular monolith, not a new service. The plan follows existing `apiinout`, `apiloadinputfiles`, `apievaluating`, `apiindicator` and `apireferential` seams, with the Angular Equipment view and Hugo documentation updated alongside backend delivery.

## Technical Context

**Language/Version**: Java 21 / Spring Boot 4.0.x; Angular 21 / TypeScript  
**Primary Dependencies**: Maven, Spring, OpenAPI Generator, MapStruct, JPA, Liquibase, PostgreSQL; Angular and existing Key Indicators components  
**Storage**: PostgreSQL; nullable schema additions only if required, delivered through a new append-only Liquibase changeset  
**Testing**: JUnit and JaCoCo; existing backend integration tests; Karma/Jasmine; Cypress; manual frontend accessibility validation after implementation, with existing automated checks reported separately where available  
**Target Platform**: Existing Azure deployment and Dockerized services (brownfield code/settings evidence)  
**CI and Deployment Boundary**: This repository owns CI, which builds and publishes the Docker image to the registry. Deployment is managed by another repository using that registry; this feature plan makes no deployment-repository changes.  
**Project Type**: Web application (Angular SPA + Spring backend + Hugo documentation)  
**Performance Goals**: No measurable import/evaluation regression; evaluation batch regression ≤20%; API p95 <500 ms; UI TTI <3 s  
**Constraints**: Backward-compatible optional fields; exact `DEVICES` eligibility; defaults 0.57/0.85/1.0; preserve original source quantity/electricity; EN/FR user-facing messages; no new external calculation integration  
**Scale/Scope**: Inventory physical equipment only; excludes digital-service equipment, infrastructure and other non-`DEVICES` categories

## Constitution Check

**Code quality and boundaries**

- [ ] Keep changes in the existing capability modules; put cross-module category resolution behind one narrow public application service.
- [ ] Keep each method at cyclomatic complexity ≤10; document the non-trivial allocation rule and public contract changes.
- [ ] Use append-only Liquibase changesets and backward-compatible nullable data; never alter an applied changeset.
- [ ] Preserve organization/workspace authorization on existing REST paths; do not introduce a new endpoint without an approved contract.
- [ ] Keep logs free of equipment names, user data and raw CSV values; use existing correlation-friendly logging.

**Testing (non-negotiable)**

- [ ] Write targeted tests before or alongside implementation.
- [ ] Reach ≥80% coverage on new code and 100% coverage on fraction/category/evaluation impact paths.
- [ ] Cover import, persistence, evaluation, output serialization, indicators, API contracts if impacted, and migration behavior.
- [ ] Prove legacy input and no-context behavior remain 100% allocation and regression-equivalent.

**Performance and user experience**

- [ ] Measure import/evaluation against the pre-change baseline; investigate any regression, with 20% as the maximum permitted evaluation-batch regression.
- [ ] Keep indicator retrieval p95 below 500 ms and UI TTI below 3 s.
- [ ] Implement WCAG 2.2 AA semantics and behavior; obtain manual keyboard, contrast, zoom and screen-reader evidence after frontend implementation. Pending manual review does not block implementation and must not be reported as passed.
- [ ] Provide localized backend validation messages and frontend indicator labels in EN and FR.

**Workflow and release**

- [ ] Backend and frontend build/test/analyze pipelines, Sonar gate, dependency/licence checks and peer review pass.
- [ ] OpenAPI validation runs if any existing REST contract changes.
- [ ] Existing English technical/functional Hugo documentation and downloadable templates/samples are included in the quality gate. No feature-specific release note or release version is required.
- [ ] Compile frontend and backend code and run applicable tests. Local Docker image compilation is deferred; no Dockerfile change is planned.
- [ ] Keep changes to this repository's CI only; deployment remains the responsibility of the separate repository consuming the Docker registry.

## Architecture Approach

### Component Breakdown

| Component | Responsibility | Type |
|---|---|---|
| `apiinout` input model / mapper | Carry optional source usage context and fraction without changing existing quantity or electricity values | Persistence and mapping |
| `apiloadinputfiles` / CSV utilities | Normalize header whitespace at parser boundary; perform row-level validation and accumulate rejected-line errors | Import |
| `apireferential` shared category resolver | Apply reference-before-type and workspace-before-organization precedence; return exact category or unmatched `INFRASTRUCTURE` | Reusable application service |
| `apievaluating` | Resolve category before allocation; calculate effective fraction; pass a defensive, scaled calculation input to each lifecycle/criterion request; persist evaluated metadata | Evaluation |
| `apiinout` output persistence and `apiindicator` export | Persist evaluation-time values with task results and serialize the chosen evaluated artifact | Output/export |
| `apiindicator` | Calculate BYOD/COPE quantity shares from the existing unallocated equipment quantity aggregation and resolved category | Indicator service |
| Angular Equipment view | Make the two localized indicators available using existing selection/display patterns | Frontend |
| Hugo content and CSV fixtures | Update existing English functional/technical documentation and template/sample CSVs (no feature release note) | Documentation |
| `apiindicator` spreadsheet export (FR-019) | Convert the authorized stored task ZIP into a separate text-typed XLSX download; keep raw CSV ZIP unchanged behind a formula-risk confirmation | Output/export |

### Key Design Decisions

1. **Do not mutate source data.** Persist `contexteUsage` and any valid provided fraction with the input row so future evaluations can reproduce the result. At evaluation, derive the effective fraction and persist it with the task-scoped evaluated equipment result. Make a calculation copy (not an in-place mutation) for the calculation engine.
2. **Keep the denominator independent of allocation.** Preserve original, unallocated equipment quantity as the indicator numerator/denominator basis using the existing equipment-indicator aggregation. Scale only the calculation input quantity and electricity consumption; keep the original source values intact. If output-row fields are scaled by the engine, store or retain the original quantity separately in the evaluated task snapshot.
3. **Use one category resolver.** Factor resolution into a shared, public application-level service in the referential capability and use it in evaluation and indicators. Resolve default-item reference before type, workspace override before organization, and treat unmatched types as `INFRASTRUCTURE`. Only exact `item_category = DEVICES` enables allocation; do not change G4IT-42 category validation or accepted custom categories.
4. **Persist factor and export value as distinct concepts.** Store the calculated `effectiveFraction` for every evaluated equipment result (including 1.0 for non-Devices) independently from source `contexteUsage` / `fractionUsage`. Export the effective fraction for a Device when it was provided or derived from BYOD/COPE; for non-Devices export the provided values without deriving a default; leave both columns empty when neither was provided. The source input export must remain source-faithful.
5. **Name the evaluated export artifact.** Plan to add the two columns to the evaluated physical-equipment artifact `ind_physical_equipment.csv`, because it is task-scoped and can carry the persisted result. Confirm the current download route and artifact mapping before implementation; do not silently substitute the raw input artifact `physical_equipment.csv` or create an API that does not exist.
6. **Make REST parity evidence-based.** CSV loading is the required path. Before coding, inventory existing OpenAPI operations and clients that create/update physical equipment. If an existing public REST path is an active input path, extend that contract with optional nullable properties and mapping. Add REST validation only where the repository already validates comparable existing fields, reusing those conventions; do not introduce a new validation policy. Otherwise keep this feature CSV-only. Do not invent a new endpoint. Record the outcome as a design decision before implementation.
7. **Honor localized and row-level errors.** Use stable message keys with the exact English FR-006 text and French translations. Normalize headers once, detect collisions after trimming, and accumulate all applicable field errors before rejecting the entire line. Report accepted/rejected line counts separately from the number of validation messages.
8. **Respect the resolved non-Device rule.** Resolve category before feature-specific value validation. Per AS-2, non-Device lines are not rejected for usage values and those values do not affect calculations. The implementation design must retain source values needed for Q-NEW-1 export without eager numeric parsing that turns an ignored non-Device value into a file-level failure. Confirm the persisted representation during the REST/CSV contract inventory.
9. **Documentation is a required English-only delivery workstream.** The current Hugo configuration declares `en-us`; update the existing technical and especially functional English pages, the physical-equipment template, and every separately maintained sample. Do not create a feature-specific release note, changelog entry, release version, or French Hugo page. Application version assignment belongs to the later merge/release process.
10. **Offer a separate spreadsheet-safe export (FR-019, user decision 2026-11-27, SAST-002 option 1).** Add a distinct XLSX download built from the authorized stored task ZIP, with every cell typed as text (no formula or hyperlink), reusing the installed Apache POI dependency. Keep the raw CSV ZIP bytes unchanged for machine exchange and require an explicit formula-risk confirmation before it is downloaded from the UI. Both downloads apply the same authentication, inventory ownership and role restrictions. Spreadsheet format limits or invalid archives fail explicitly (HTTP 422), never truncate values or fall back to unsafe CSV, and temporary workbook files are always cleaned up.

### Data Model and Contract Changes

- Add optional source context/fraction to input persistence and import/export mapping. Keep source `quantity` and `electricityConsumption` unmodified.
- Add the task-evaluated context, effective fraction, and original quantity (or an equivalent unallocated quantity reference) to the evaluated snapshot if existing output rows cannot preserve these values and indicator denominators. Add only nullable, append-only schema changes; preserve compatibility for historical tasks.
- Store a decimal fraction with sufficient precision for provided values and export it as a plain decimal (for example 0.57, 0.85, 0.5). The exact column/property names and migration scope are a design-time decision to align with local naming conventions.
- If REST parity is confirmed, update the existing OpenAPI schema first, regenerate DTOs through the existing build, and map to/from persisted values. Apply validation only if comparable existing fields are validated on that REST path, following the same established conventions; do not broaden or alter an unrelated endpoint.
- Preserve `physical_equipment.csv` as an input/source export. The evaluated artifact planned for the effective fraction is `ind_physical_equipment.csv`; verify its exact route, lifecycle and row key before writing the tasks.

## Sequencing

### Phase 1: Resolve contracts and persistence boundary

- Confirm CI responsibilities only; deployment is external to this repository and consumes the Docker registry populated by CI.
- Inventory the active REST CRUD routes/clients, existing validation conventions, current CSV input/output contracts, evaluated export mapping, task-result identity, and English Hugo/template/sample assets.
- Approve the persistence/export representation for source values, effective factor, and unallocated quantity, including legacy reevaluation behavior.
- Preserve non-Device source context and fraction values for export without applying them to calculation or rejecting those values.
- **Depends on**: Validated spec, clarifications and this plan.
- **Delivers**: Written decision record in downstream tasks; no implementation until blocking data/API semantics are clear.

### Phase 2: Input and shared category foundation

- Add optional persistence/DTO/mapper fields required by the approved input contract; add a new nullable Liquibase changeset only if the existing schema needs it.
- Normalize CSV header keys once; handle duplicate normalized headers deterministically.
- Add shared category resolution in the referential capability and test reference/type and workspace/organization precedence, unmatched behavior and exact category matching.
- Implement localized validation collection and whole-line rejection without breaking legacy headers/files.
- **Depends on**: Phase 1 decisions.
- **Delivers**: Backward-compatible input, validation, and one shared category-resolution rule.

### Phase 3: Evaluation, persistence, export and indicators

- Resolve Device category before calculation; compute effective fraction as explicit value, else BYOD 0.57, COPE 0.85, otherwise 1.0.
- Apply the factor upstream to every lifecycle-stage calculation and electricity consumption through a defensive input copy; never change persisted source input.
- Persist evaluation-time context/factor and retain unallocated quantity for existing indicator aggregation and denominator.
- Extend the selected evaluated export artifact with context and the prescribed output fraction; keep raw input export source-faithful.
- Add BYOD/COPE indicators, labels and no-Devices “no data” behavior using existing percentage rounding and quantity aggregation.
- Add the separate text-typed XLSX inventory export and the raw-CSV formula-risk confirmation in the UI (FR-019, TASK-024).
- **Depends on**: Phase 2; artifact and REST decisions from Phase 1.
- **Delivers**: Functional allocation, reproducible evaluated results, transparent export and Equipment indicators.

### Phase 4: Dedicated documentation workstream

- Inventory relevant English Hugo pages before editing; update `uc3_load_files.md` (validation/import), `uc5_visualize_equipment_footprint.md` (indicators) and `uc7_export_files.md` (evaluated export). Documentation scope is English only.
- Document `contexteUsage` and `fractionUsage`, whitespace-tolerant headers, allowed values/range, rejected-row semantics, BYOD/COPE 57%/85% defaults, exact `DEVICES` eligibility, non-Device and digital-service exclusions, effective export fraction and the two quantity-share indicators.
- Add both new columns, empty, to `services/backend/volume_generation/input_template/2-physical-equipment.csv` and all maintained downloadable/sample CSV fixtures.
- Update existing technical documentation where the implementation changes documented contracts or architecture. Do not add a feature-specific release note or changelog entry, or request a release version.
- **Depends on**: Phase 1 documentation inventory and final behavior/export contracts; may proceed in parallel with Phase 3 once wording is stable.
- **Delivers**: Updated existing English technical/functional Hugo documentation and files/templates. This task must remain a dedicated, separately traceable task in the downstream `tasks.md`.

### Phase 5: Verification and release readiness

- Run backend unit/integration and migration tests, frontend unit/E2E/accessibility checks, API contract checks if relevant, and legacy regression scenarios.
- Verify docs/template/sample completeness through a deterministic quality-gate checklist (no feature release note is required).
- Perform manual frontend accessibility validation after implementation, covering keyboard, screen reader and 200% zoom. Report any existing automated accessibility results separately; manual validation is not an implementation prerequisite and remains pending until evidence is available.
- Run and verify this repository's existing CI, including Docker image publication to the registry; do not perform deployment, which is handled by the separate deployment repository.
- **Depends on**: Phases 2–4.
- **Delivers**: Evidence-backed CI and Docker registry publication handoff for the separate deployment repository.

## Risks and Decisions

Risk score uses likelihood × impact on a 1–3 scale (Low=1, Medium=2, High=3).

| ID | Category / risk | Probability | Impact | Score | Mitigation | Contingency | Owner / status |
|---|---|---:|---:|---:|---|---|---|
| RISK-001 | Data: scaled result quantity is reused as indicator denominator, distorting BYOD/COPE percentages | High | High | 9 (High) | Keep original quantity separate and test numerator/denominator against existing equipment indicators for integer, fractional, empty and zero quantities | Block release; restore denominator from source/task snapshot and rerun evaluation | Backend lead / Open |
| RISK-002 | Technical: source input is overwritten or evaluated factor is not reproducible across task results/re-evaluation | Medium | High | 6 (High) | Separate source values from effective factor; persist factor on task result; test re-evaluation and legacy inventories | Roll back application version while retaining additive nullable columns; re-evaluate after fix | Backend lead / Open |
| RISK-003 | Integration: evaluation and indicators resolve category differently from G4IT-42 precedence | Medium | High | 6 (High) | One shared resolver with tests for reference-first, workspace overrides, exact `DEVICES`, custom categories and unmatched types | Disable new allocation behavior for affected records and correct resolver before release | Referential/evaluation owners / Open |
| RISK-004 | Data quality: parser eagerly parses usage fields or fails to accumulate errors, producing partial line import or inaccurate rejected counts | Medium | High | 6 (High) | Resolve category before feature validation; normalize headers once; collect all row errors and reject whole row; separate row and message totals | Stop import release; return to prior parser behavior and correct validation mapping | Import owner / Open |
| RISK-005 | Contract: raw input and evaluated exports or REST CRUD are mistaken for one another | Medium | High | 6 (High) | Name `ind_physical_equipment.csv` as the planned evaluated artifact; inventory current routes/contracts and preserve source export; no invented endpoint | Hold API/export implementation until product/API owner resolves artifact and parity | API owner / Open |
| RISK-006 | Governance/operations: CI image publication or documentation scope is misunderstood | Medium | Medium | 4 (Medium) | Verify existing CI image/registry behavior and update existing English technical/functional Hugo documentation without a feature release note or version | Keep unverified CI behavior explicit; do not imply this repository deploys the image or requires a release version before implementation | Product/release owner / Open |
| RISK-007 | Accessibility/localization: new labels or rejected-file errors are not accessible or translated consistently | Low | High | 3 (Medium) | EN/FR message and label tests, automated checks plus keyboard/screen-reader/zoom validation | Hold release until manual review is complete or an authorized reviewer records an explicit accepted risk | Frontend/accessibility owner / Open |

### Implementation-Time Decisions and Verifications

The following user decisions are resolved and are not blockers: this repository handles CI/registry publication only; deployment belongs to a separate repository; preserve and export source usage context and non-Device values; update existing English technical/functional Hugo documentation without a feature release note or version; and REST validation follows existing-field validation conventions only. Local Docker image compilation is deferred in favor of frontend/backend code compilation and applicable tests. Manual frontend accessibility validation follows implementation and is not an implementation prerequisite.

Before implementation, record the results of these bounded code/contract inspections in `tasks.md`:

- **REST parity**: Inspect active physical-equipment OpenAPI operations and clients. Extend only an existing supported input path; add validation only if comparable existing fields are validated there. Do not create a speculative endpoint or new validation policy.
- **Evaluated export contract**: Verify the task-scoped download route, artifact identity, row key and persistence lifecycle. Keep source input export faithful; the evaluated export must carry source context and the effective fraction.
- **Evaluation persistence and denominator**: Confirm the concrete evaluated-result fields/schema and re-evaluation behavior for the effective fraction, while preserving source values and unallocated quantity for existing indicator aggregation.
- **Category resolver**: Identify the reusable application-level seam for a single resolver shared by pre-calculation evaluation and indicators, preserving G4IT-42 precedence.
- **English documentation**: Inventory applicable existing technical/functional Hugo pages, templates, samples and their verification command. No feature-specific release note, changelog entry, release version or French Hugo page is in scope.
- **CI image publication**: Verify the existing CI build and registry publication steps; deployment remains external.

### Residual Risk

The denominator and result-persistence risks remain release blockers until output-row semantics and artifact identity are verified. No residual risk is accepted by this plan; any accessibility exception or behavior change requires explicit human approval. CLQ-013/014/019/020 remain out of scope and retain existing behavior. This repository's delivery boundary is CI and registry publication; deployment is performed by the separate deployment repository.

## Rollout and Rollback Strategy

**Approach**: Use this repository's existing CI to build and publish the Docker image to the registry. Deployment to environments is managed by a separate repository and is out of scope here. Allocation remains opt-in through the new input values; legacy files continue at 1.0. No new feature flag is assumed because the default path preserves prior behavior.

| Phase | Target | Success criteria |
|---|---|---|
| 1 — CI validation | Existing repository CI | Schema migration and tests pass; legacy imports/evaluations match baseline; sample accepts/rejects expected lines; no source mutation; lifecycle/electricity cases and indicators pass; API p95 <500 ms; accessibility evidence captured |
| 2 — Image publication | Docker registry populated by CI | CI publishes the expected image after required checks pass; release owner receives image identity and validation evidence for the separate deployment repository |
| 3 — Documentation readiness | English Hugo site and downloadable assets | Existing English technical/functional documentation, templates and sample files are updated and verified; no feature release note or version is required; deployment itself remains external |

**Feature flags**: None planned. If existing deployment controls support a safe switch without creating a new contract, the implementation team may propose one at design review; it is not a prerequisite or assumed capability.

**Monitoring and promotion**:

- Compare import accepted/rejected line counts and validation-message counts; investigate unexpected change from the sample/baseline.
- Monitor evaluation duration, calculation failures, indicator retrieval p95, and Actuator health/metrics.
- Track the number of evaluated Device results by effective fraction using non-identifying aggregates only if existing metrics support it; do not log equipment names or raw CSV values.
- Do not promote if source inputs change during evaluation, denominators differ from existing quantity semantics, effective fractions are missing from results, or the evaluated artifact is ambiguous.

**Rollback**:

1. Stop promotion and redeploy the previous application version using the existing release process if allocation, import, export, or indicator behavior is incorrect.
2. Retain the additive nullable schema changes during application rollback; do not run a destructive down migration that could discard user-provided or evaluated values. The previous application must ignore the new nullable fields.
3. Keep source input quantities/electricity unchanged so re-evaluation with the prior version preserves historical behavior. Preserve task results and investigate before any data repair.
4. Verify health, baseline import/evaluation behavior, and no data loss; rerun affected evaluations only after the corrected version is approved.
5. Document the incident and update operational guidance if rollback occurs.

## Observability

- **Logging**: Existing SLF4J logging with correlation-friendly context. Log validation error codes/counts and task/evaluation outcomes only; never log raw row values or personal data.
- **Metrics**: Use existing Actuator and application metrics for evaluation duration/failures, import accepted/rejected rows, indicator retrieval latency and health. Keep rejected-row count separate from accumulated error-message count.
- **Audit**: Use the existing audit-event capability for inventory data changes where the normal update path records them; do not create a parallel audit mechanism.
- **Tracing**: Distributed tracing is not required by the constitution; logs and metrics suffice.
- **Alerting**: Follow existing thresholds and escalation. At minimum, investigate sustained import/evaluation failure increases or API p95 ≥500 ms; do not invent an alerting platform as part of this feature.

## Quality-Gate Verification Checklist

The downstream quality gate must verify all items below, not merely the existence of a documentation commit:

- [ ] Allocation derives 0.57 for BYOD, 0.85 for COPE, explicit [0,1] overrides, and 1.0 otherwise; exact `DEVICES` only; no digital-service allocation.
- [ ] Original input quantity/electricity are not mutated; original equipment quantity still drives indicator numerator and denominator; factor is persisted at evaluation and used for all lifecycle steps and electricity.
- [ ] Shared resolver matches G4IT-42 precedence and unmatched behavior; custom non-`DEVICES` categories remain valid and ineligible.
- [ ] Optional CSV headers load with and without whitespace; invalid applicable fields accumulate messages and reject the whole row; report distinguishes rejected lines from message count; EN/FR messages verified.
- [ ] Evaluated artifact name/route is verified; export includes context and the required plain-decimal value, source-faithful non-Device values, and no derived non-Device default; input export remains source-faithful.
- [ ] No-Devices indicator uses existing “no data”; indicators use existing quantity aggregation and percentage rounding; frontend labels/keyboard behavior pass EN/FR and accessibility checks.
- [ ] Backward-compatibility, migration, REST contract (if applicable), coverage, performance and baseline checks pass.
- [ ] **Dedicated Hugo documentation workstream is complete**: update the applicable existing English technical/functional pages; accurately document both CSV columns, header/validation and whole-line rejection, defaults/eligibility/exclusions, effective fraction export and indicators; add both empty columns to the downloadable template and every maintained sample; verify changed pages and downloadable files in the rendered/site build or existing documentation check. No French feature page, feature-specific release note, changelog entry or release version is required.
- [ ] Manual accessibility review after frontend implementation records keyboard-only selection, screen-reader label/value announcement, text no-data behavior and 200% zoom; existing automated checks are reported separately where available. Pending manual validation does not block implementation, is not a passing result and does not silently waive accessibility risk.

## Complexity Tracking

| Dimension | Assessment | Notes |
|---|---|---|
| Technical complexity | High | Crosses import, shared referential resolution, upstream evaluation, persistence, export and indicator boundaries |
| Integration complexity | Medium | Existing calculation engine remains unchanged; OpenAPI changes are conditional on current REST usage |
| Data migration | Low–Medium | Additive nullable fields only if existing input/output entities require persistence changes; historical results retain default behavior |
| Testing complexity | High | Requires calculation-path, whole-line import, result persistence, denominator, legacy, accessibility and artifact coverage |
| Rollout risk | Medium | Defaults preserve legacy behavior, but output persistence/denominator and export identity must be resolved before release |
