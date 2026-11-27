---
workflow: feature-implementation
trigger: user
date: '2026-11-20'
status: validated
inputDocuments:
  - outputs/specs/constitution.md
  - outputs/specs/features/G4IT-48/spec.md
  - outputs/specs/features/G4IT-48/plan.md
  - .github/skills/spec-tasks/resources/task-breakdown-template.md
  - .github/skills/spec-tasks/resources/test-strategy-template.md
  - .github/instructions/output-metadata.instructions.md
  - .github/knowledge/governance/ai-disclaimer-text.md
changeHistory:
  - date: '2026-11-20'
    description: 'Created the dependency-ordered G4IT-48 task breakdown'
    changes:
      - 'Added pre-implementation contract and persistence verification gates'
      - 'Sequenced implementation, tests, accessibility, English Hugo documentation, CI and registry handoff'
  - date: '2026-11-23'
    description: 'Recorded explicit user decisions for publication evidence and evaluated trace option A'
    changes:
      - 'Deferred only feature-image publication-result evidence from TASK-007 to TASK-023 without claiming success or removing other TASK-007 checks'
      - 'Distinguished main-image publication from feature-image publication evidence'
      - 'Specified a source-equipment/task evaluated trace without changing existing grouping keys or aggregation'
  - date: '2026-11-23'
    description: 'Applied explicit documentation, compilation and accessibility boundaries'
    changes:
      - 'Remove feature-specific release-note and version-selection obligations; update existing English technical/functional Hugo documentation and CSV assets'
      - 'Defer local Docker image compilation; retain frontend/backend code compilation and applicable configured tests'
      - 'Perform manual frontend accessibility validation after implementation, without treating it as an implementation prerequisite'
  - date: '2026-11-23'
    description: 'Completed the remaining pre-implementation source and documentation inspections'
    changes:
      - 'Recorded existing REST contracts, evaluated export identity, option A persistence lifecycle and original-quantity semantics'
      - 'Identified all three CSV parser boundaries, conditional validation and rejected-row counting seams'
      - 'Inventoried existing English functional/technical pages and four maintained physical-equipment CSV assets'
      - 'Marked only evidence tasks complete and retained all implementation and runtime verification tasks as pending'
  - date: '2026-11-23'
    description: 'Implemented and verified the first backend foundation batch in the existing run'
    changes:
      - 'Completed TASK-008 optional raw usage fields, source mapping/export and nullable additive migration with focused tests'
      - 'Recorded partial TASK-009 resolver extraction and inventory-indicator integration without claiming pre-calculation integration'
      - 'Recorded successful backend compilation and 56 passing selected tests after correcting an unrelated test-fixture merge setup'
  - date: '2026-11-23'
    description: 'Started the independently ready existing REST contract task after TASK-008'
    changes:
      - 'Declared the two optional usage properties explicitly nullable in the existing OpenAPI schema'
      - 'Added typed nullable mapping and JSON/delegate/service/mapper tests for create, read, update and legacy payloads'
      - 'Kept TASK-013 in progress until contract regeneration and the combined regression result are available'
  - date: '2026-11-23'
    description: 'Verified the nullable REST contract and its dependent CSV mapping'
    changes:
      - 'Recorded the regenerated contract and corrected CSV builder type mismatch'
      - 'Completed TASK-013 with four existing-operation contract cases passing in the successful 60-test run'
      - 'Reverified TASK-008 and retained TASK-009 as partial, without completing the implementation station'
  - date: '2026-11-23'
    description: 'Started the independently ready task-owned evaluated-trace foundation'
    changes:
      - 'Added trace migration, entity, repository and focused migration test without changing aggregation keys'
      - 'Kept TASK-011 in progress until evaluation integration and actual tests are complete'
  - date: '2026-11-23'
    description: 'Verified the trace migration foundation and shared-category regressions'
    changes:
      - 'Recorded successful Java 21 compilation and 28 passing selected tests'
      - 'Kept evaluation writing, retry, repository integration and PostgreSQL verification pending'
  - date: '2026-11-23'
    description: 'Started extracting reusable evaluation reference selection for TASK-009'
    changes:
      - 'Moved the existing matched-model and default-item selection into a reusable referential service'
      - 'Added focused tests and kept category integration and regression verification pending'
  - date: '2026-11-23'
    description: 'Verified reusable reference selection against existing evaluation tests'
    changes:
      - 'Recorded successful Java 21 compilation and 52 passing selected tests'
      - 'Kept TASK-009 in progress until evaluation consumes the shared category lookup'
  - date: '2026-11-23'
    description: 'Continued category and evaluated-trace integration'
    changes:
      - 'Prepared inventory category lookup and task-owned snapshots before calculation'
      - 'Added factor-scaled calculation requests and recorded 57 passing focused tests after correcting an initial test-compilation failure'
      - 'Kept TASK-009, TASK-011 and TASK-012 in progress without claiming completion'
  - date: '2026-11-23'
    description: 'Verified category and evaluated-trace implementation; recorded grouped-electricity decision'
    changes:
      - 'Completed TASK-009 and TASK-011 with 81 passing targeted tests, including actual JPA retry on H2'
      - 'Kept TASK-012 open pending a decision on grouped electricity aggregation'
      - 'Recorded that PostgreSQL, actual evaluated export, full regression and manual accessibility remain unverified'
  - date: '2026-11-24'
    description: 'Resumed the existing implementation station with the approved targeted electricity correction'
    changes:
      - 'Permit quantity-weighted electricity for allocation-affected groups without changing grouping keys or splitting groups'
      - 'Retain historical unallocated and digital-service electricity semantics'
      - 'Record passing nine-suite targeted Maven verification, retaining TASK-012 coverage and acceptance-schema limits'
  - date: '2026-11-24'
    description: 'Implemented bounded TASK-010 CSV changes without claiming completion'
    changes:
      - 'Added header-only normalization across three boundaries, localized Device usage validation and distinct rejected-row reporting'
      - 'Added focused parser, bounds, localization, exemption, metadata and whole-row tests'
      - 'Recorded three unsuccessful offline verification attempts and the final bad-class-file test-compilation blocker with cause unverified'
      - 'Kept every TASK-010 acceptance and test checkbox unchecked, retaining acceptance-schema limits and prior task statuses'
  - date: '2026-11-24'
    description: 'Verified bounded TASK-010 troubleshooting in the same implementation station'
    changes:
      - 'Normal offline Maven execution did not reproduce the previous administrator bad-class-file diagnostic'
      - 'Corrected missing workspace identity in two synthetic test contexts after observing their exact null-pointer failures'
      - 'Recorded production/test recompilation and 97 passing tests across eight targeted suites'
      - 'Checked only verified eligible assertions and retained TASK-010 in progress with rejected acceptance entries and historical compiler cause unresolved'
  - date: '2026-11-24'
    description: 'Completed the final bounded TASK-010 acceptance review without advancing the existing station'
    changes:
      - 'Corroborated the recorded eight-suite 97-test success with current parser, validator, loader, localization and source-export code'
      - 'Retained TASK-010 in progress solely for rejected acceptance entries, with the eligible message and parser-test assertions still checked'
      - 'Separated TASK-018 coverage and broader integration gates from TASK-010 completion, and retained the compiler cause as historical only'
      - 'Confirmed the approved TASK-012 correction and focused test evidence, retaining its rejected acceptance entries and unverified downstream gates'
  - date: '2026-11-24'
    description: 'Applied user-authorized minimal acceptance wording normalization for TASK-010 and TASK-012'
    changes:
      - 'Replaced semicolons with sentence periods in seven rejected assertions and removed only the BYOD and COPE code delimiters'
      - 'Preserved task IDs, requirements, dependencies and all historical rejection records'
      - 'Reassessed each normalized plain-text pass condition against current source and targeted verification, without advancing the station'
      - 'Completed only TASK-010 and TASK-012 after a successful current thirteen-suite targeted Maven run, retaining all broader verification gates'
  - date: '2026-11-24'
    description: 'Implemented bounded TASK-014 evaluated export with persisted task/source traces'
    changes:
      - 'Appended inventory evaluated usage columns without changing raw export, digital-service columns or grouping'
      - 'Recorded successful production/test compilation and 206 passing tests across eighteen targeted suites after correcting five existing trace-less test fixtures'
      - 'Preserved TASK-009/010/011/012/013 done statuses after independent current verification'
      - 'Kept TASK-014 in progress because acceptance entries 1 and 2 contain rejected semicolon syntax, without rewriting them or advancing the station'
  - date: '2026-11-24'
    description: 'Completed the final bounded TASK-014 verification after user-authorized punctuation normalization'
    changes:
      - 'Replaced only the two rejected semicolon sentence separators with periods, without changing requirements'
      - 'Reviewed every requested export scenario, persisted task/source identity, raw preservation and no-recomputation assertions against current source and existing focused tests'
      - 'Kept the thirteen-suite acceptance run separate from the eighteen-suite TASK-014 serializer and persistence evidence'
      - 'Completed only TASK-014, preserving prior statuses and history without changing application code or workflow state'
  - date: '2026-11-24'
    description: 'Completed bounded TASK-015 diagnosis and backend verification in the existing station'
    changes:
      - 'Confirmed the external-client inputs and existing checked-in compatibility replacement are independent of the indicator schema changes'
      - 'Normal regeneration and production/test compilation succeeded without generator, dependency or generated-artifact edits'
      - 'Corrected two task-owned test-fixture assumptions after recording the actual 91-test failure result'
      - 'Normalized only acceptance entries 1 and 4 sentence punctuation and recorded the corrected five-suite 91-test success'
      - 'Completed only TASK-015 within its backend acceptance boundary, leaving TASK-016 and broader gates untouched'
  - date: '2026-11-24'
    description: 'Implemented bounded TASK-016 localized Equipment indicators in the existing station'
    changes:
      - 'Added the two existing-pattern cards with exact EN/FR labels, persisted share values and unchanged textual no-data rendering'
      - 'Recorded the corrected test-setup failure, final production build success and 35 passing focused tests'
      - 'Kept TASK-016 in progress for rejected acceptance punctuation and recorded manual accessibility as pending for later TASK-020'
      - 'Did not start later tasks or modify workflow state'
  - date: '2026-11-24'
    description: 'Completed the final bounded TASK-016 acceptance review in the existing implementation station'
    changes:
      - 'Applied explicitly authorized code-delimiter removal and sentence-separator normalization only to acceptance entries 1 and 2'
      - 'Corroborated exact labels, persisted response values, no-data rendering, semantic association and native keyboard/focus implementation against current source and focused tests'
      - 'Retained the verified production build exit 0 and 35 passing focused tests as prior evidence without claiming a new execution'
      - 'Recorded pending TASK-020 manual WCAG validation under the human accessibility reviewer role, with identity still to be assigned by the user'
      - 'Completed only TASK-016 without starting TASK-017 or changing workflow state'
  - date: '2026-11-24'
    description: 'Executed bounded TASK-017 existing English documentation and CSV updates'
    changes:
      - 'Reconfirmed twelve existing pages and four maintained CSV assets against prior evidence and current implementation'
      - 'Removed only rejected code delimiters from TASK-017 acceptance entries 1 and 2 under explicit authorization'
      - 'Recorded successful post-edit Hugo build, static CSV schema inspection and quality-checklist coverage'
      - 'Kept TASK-017 in progress because generated-page and link inspection is outside the current read boundary'
      - 'Preserved all other task statuses and workflow state, with CSV disclaimer limitation recorded as WARN'
  - date: '2026-11-24'
    description: 'Ran only TASK-018 full backend test goal in the existing implementation station'
    changes:
      - 'Recorded the unfiltered offline Maven test command and its actual exit code 0'
      - 'Kept aggregate and per-suite test counts unverified because oversized terminal output was spooled outside the declared read paths'
      - 'Recorded acceptance-schema rejections and unmeasured coverage without rewriting criteria or reading generated artifacts'
      - 'Kept TASK-018 in progress and preserved TASK-017 and every later task status'
  - date: '2026-11-24'
    description: 'Continued only TASK-018 evidence review within the existing implementation station'
    changes:
      - 'Confirmed the backend POM coverage profile and verify-phase JaCoCo reporting without a configured threshold check'
      - 'Refused normal generated report inspection at preflight without accessing the terminal spool or alternate paths'
      - 'Retained unknown aggregate and per-suite counts and unmeasured coverage, stopping before a verification rerun'
      - 'Preserved TASK-017 read blocker, all acceptance wording and workflow state'
  - date: '2026-11-24'
    description: 'Resumed measured rendered-documentation and backend verification in the existing station'
    changes:
      - 'Superseded the incorrect blanket generated-artifact access claim with successful direct rendered-page and report reads'
      - 'Recorded successful Hugo generation and feature-link/download inspection, with two rendered-content defects and missing htmltest'
      - 'Recorded 1378 backend tests, 1369 passing, zero failures/errors and nine explicit Azure-test skips'
      - 'Recovered JaCoCo reporting with isolated execution data and recorded allocation coverage of 37/38 lines and 21/24 branches'
      - 'Checked only TASK-018 behavioral assertions corroborated by current tests, retaining coverage and full verification as incomplete'
      - 'Recorded missing offline Failsafe dependency and Windows-only frontend esbuild as actual prerequisite failures'
      - 'Preserved completed implementation tasks, acceptance wording and the existing running workflow state'
  - date: '2026-11-24'
    description: 'Retried validation after the user installed WSL esbuild and htmltest'
    changes:
      - 'Confirmed htmltest 0.17.0 and recorded 52 site-root link/anchor errors in 223 generated documents'
      - 'Distinguished existing source targets in three touched pages from newly added feature links without fixing unrelated defects'
      - 'Recorded the new frontend version mismatch between esbuild host 0.27.3 and selected binary 0.27.2'
      - 'Retained the two feature rendering defects, prior backend coverage gap and all incomplete gates without source or dependency edits'
  - date: '2026-11-24'
    description: 'Resumed frontend, documentation and backend verification after esbuild correction'
    changes:
      - 'Recorded 1719 passing frontend CI tests and global coverage without claiming feature new-code coverage'
      - 'Corrected both feature documentation rendering defects and recorded the remaining site link failures'
      - 'Added focused allocation and reference tests and recorded the successful first and failed later full backend runs'
      - 'Kept coverage, integration, manual accessibility and station completion gates open'
  - date: '2026-11-24'
    description: 'Revalidated final backend tests and generated documentation links'
    changes:
      - 'Recorded two successful full backend test and isolated report runs after the earlier failure'
      - 'Added the empty-fraction default case and measured allocation at 38 of 38 lines and 24 of 24 branches'
      - 'Recorded 52 htmltest errors in 223 documents on the final generated site'
      - 'Kept complete feature coverage, full verify, integration and station completion open'
  - date: '2026-11-24'
    description: 'Verified focused export, contract and migration test evidence'
    changes:
      - 'Recorded 53 passing focused tests across six existing backend test classes'
      - 'Separated H2 migration and mocked controller evidence from PostgreSQL, HTTP authorization and complete download verification'
      - 'Kept TASK-019 and its acceptance criteria open pending end-to-end evidence'
  - date: '2026-11-24'
    description: 'Added download controller and ZIP tests and reran backend verify'
    changes:
      - 'Added direct-controller authorization and real ZIP component tests using mocked storage'
      - 'Recorded 22 passing targeted tests and 1384 backend tests with nine skips during verify'
      - 'Recorded verify failure on the missing offline Failsafe dependency and retained real HTTP and PostgreSQL gaps'
  - date: '2026-11-24'
    description: 'Confirmed backend verify after the user populated the Maven cache'
    changes:
      - 'Independently ran offline verify successfully with 1384 tests and nine skips'
      - 'Recorded zero completed Failsafe integration tests and retained real PostgreSQL and HTTP integration gaps'
      - 'Kept the complete feature coverage and other station gates open'
  - date: '2026-11-25'
    description: 'Reran focused H2 migration and snapshot tests'
    changes:
      - 'Recorded three errors on the initial run due to missing migration resources in the runtime classpath'
      - 'Recorded three passing tests on retry without cleaning or modifying source'
      - 'Retained real PostgreSQL and HTTP integration as unverified'
  - date: '2026-11-25'
    description: 'Added HTTP security filter-chain tests for the export download'
    changes:
      - 'Recorded 401/403/200 evidence through the real security filter chain; default test 1390 passed with 9 skips; integration profile 3 passed'
      - 'Documented why the full PostgreSQL evaluator-to-download test is blocked; TASK-019 remains in progress'
  - date: '2026-11-25'
    description: 'Corrected the two defects discovered by the authenticated local G4IT-48 flow'
    changes:
      - 'Added Jackson 3 nullable-response serialization and an unknown-type lookup guard with five focused regression tests'
      - 'Recorded 105 passing targeted tests and backend verify with 1395 tests, zero failures/errors and nine skips'
      - 'Resolved the quantity-count observation without changing aggregation and retained live retesting and station gates as pending'
  - date: '2026-11-25'
    description: 'Recorded user-supplied evidence from the restarted live backend'
    changes:
      - 'Confirmed D1 resolved live with numeric 30 percent BYOD/COPE shares on five criteria'
      - 'Recorded successful source/evaluated export and API cleanup without claiming independent execution'
      - 'Retained D2 live retest, impact-value verification, frontend accessibility and broader gates as pending'
  - date: '2026-11-25'
    description: 'Inspected the generated live-test CSV artifacts without rerunning the application'
    changes:
      - 'Established 80 Smartphone calculation errors and 20 successful Server results in the 100-row export'
      - 'Retained Device numerical impact-ratio verification and unknown-type live retest as pending'
  - date: '2026-11-25'
    description: 'Prepared local reference-data prerequisites for the live impact checks'
    changes:
      - 'Added an opt-in test SQL fixture for missing Smartphone factors and France electricity mix, without production migration changes'
      - 'Recorded six passing PostgreSQL tests in isolated schemas, including idempotency, preservation and rollback checks'
      - 'Kept application-database loading, cache restart and numerical live checks pending'
  - date: '2026-11-25'
    description: 'Relocated the local test fixture and verified its updated resource path'
    changes:
      - 'Moved the synthetic reference SQL alongside other test SQL resources and updated its test and README references'
      - 'Recorded six passing PostgreSQL integration tests and the fresh-local-database clarification'
  - date: '2026-11-25'
    description: 'Recorded the business-source checkpoint for missing Smartphone factors'
    changes:
      - 'Kept the synthetic local fixture separate from the common referential'
      - 'Retained live impact verification and all other open gates pending a validated source and declared read scope'
  - date: '2026-11-25'
    description: 'Resumed tests with the synthetic fixture limited to local testing'
    changes:
      - 'Recorded the decision not to change production reference-data Liquibase scripts'
      - 'Recorded a successful offline backend test command without inferring test counts'
      - 'Kept fixture loading, live validation and other unfinished gates open'
  - date: '2026-11-25'
    description: 'Recorded automatic test-fixture setup as an open prerequisite'
    changes:
      - 'Superseded the manual-loading handoff without changing production reference data'
      - 'Retained implementation and live verification as open pending a scoped dispatch'
  - date: '2026-11-25'
    description: 'Clarified autonomous H2 and one-command PostgreSQL test expectations'
    changes:
      - 'Kept unit tests autonomous and suitable for GitHub CI'
      - 'Required relevant local integration tests to run fixture setup automatically'
      - 'Retained implementation and verification as open'
  - date: '2026-11-25'
    description: 'Updated TASK-018 and TASK-019 verification status from the latest evidence'
    changes:
      - 'Recorded the successful full backend test and coverage verify counts while keeping unproven feature-wide coverage open'
      - 'Marked TASK-019 task-scope, HTTP authorization, REST-contract and PostgreSQL migration criteria as verified'
      - 'Kept TASK-019 in progress because the standalone test-report deliverable is still absent'
  - date: '2026-11-25'
    description: 'Recorded the implementation resumption reports without closing unproven acceptance criteria'
    changes:
      - 'Updated TASK-019 to reflect that its standalone test report now exists'
      - 'Kept TASK-018 coverage and TASK-019 broader integration requirements open'
  - date: '2026-11-25'
    description: 'Recorded newly executed G4IT-48 targeted verification'
    changes:
      - 'Retained TASK-018 open: the changed-line denominator and complete calculation-path matrix remain unproven'
      - 'Updated TASK-019 test evidence after 65 key-contract and 69 calculation/export tests passed'
      - 'Retained broader live/full verification limits without claiming a passing implementation gate'
  - date: '2026-11-26'
    description: 'Completed TASK-017 after authorized R01–R18 navigation repairs'
    changes:
      - 'Recorded fresh successful Hugo generation and zero local link errors in 223 documents against an eighteen-error baseline'
      - 'Closed only the remaining TASK-017 accuracy/rendering assertions using retained content evidence and current navigation verification'
      - 'Preserved later verification tasks and manual accessibility as open without changing workflow state'
  - date: '2026-11-27'
    description: 'Added the user-approved SAST-002 separate spreadsheet-export remediation'
    changes:
      - 'Recorded FR-019 scope without changing the source-faithful raw CSV contract'
      - 'Separated backend conversion/security and frontend safe-download/confirmation verification'
  - date: '2026-11-27'
    description: 'Implemented and locally verified TASK-024 without changing the registered SAST gate'
    changes:
      - 'Recorded 71 backend and 11 frontend passing tests, configured lint and production-build success'
      - 'Verified real HTTP 422 after correcting its exception mapping, and retained raw CSV risk explicitly'
      - 'Kept independent SAST reassessment, manual accessibility and backend Sonar reserve separate'
  - date: '2026-11-27'
    description: 'Corrected the task summary and dependency graph after the final quality gate'
    changes:
      - 'Counted TASK-024 in Phase 3 (7 tasks) and the total (24 tasks)'
      - 'Added TASK-024 (depends on TASK-014) to the dependency graph'
      - 'Recorded owner decisions: TASK-020 manual accessibility done during functional qualification; SAST-002 and dependency CVEs accepted for a follow-up PR; TASK-022 performance baseline formally deferred'
holisticQualityRating: not-rated
overallStatus: not-reviewed
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: tasks
agent: "general-purpose (user-approved substitute)"
agent_note: "The workflow-declared agent lacks file-write tools in this runtime; the user approved this substitute."
sensitivity: internal
context_manifest:
  - path: .github/skills/spec-tasks/SKILL.md
    mode: full
    key_facts:
      - "Task breakdowns sequence work by dependencies and keep implementation, testing, documentation, and rollout tasks distinct."
      - "Every task is typed, bounded, and includes acceptance criteria and test expectations."
      - "Accessibility work includes implementation semantics, keyboard/focus, QA scenarios, evidence, retest, and explicit handling of accepted risk."
  - path: .github/instructions/output-metadata.instructions.md
    mode: full
    key_facts:
      - "Every output file requires workflow, trigger, date, status, inputDocuments, changeHistory, holisticQualityRating, and overallStatus."
      - "New outputs start as draft, not-rated, and not-reviewed; only a reviewer may set quality ratings or review status."
      - "Update history is chronological and records creation with a description and individual changes."
  - path: outputs/specs/constitution.md
    mode: full
    key_facts:
      - "The backend is Java 21/Spring Boot with modular api* capabilities, PostgreSQL, and append-only Liquibase changesets."
      - "REST contracts are OpenAPI-first; CSV additions must be optional and preserve existing files."
      - "Targets include 80% coverage on new code, 100% on allocation paths, WCAG 2.2 AA, API p95 below 500 ms, and no evaluation regression over 20%."
  - path: outputs/specs/features/G4IT-48/spec.md
    mode: full
    key_facts:
      - "Only exact item_category DEVICES is eligible; explicit fractions in [0,1] override BYOD 0.57 and COPE 0.85 defaults."
      - "FR-015 persists the evaluation-time effective fraction; FR-009 applies it upstream to every lifecycle impact and electricity."
      - "FR-017 rejects and reports a whole invalid line; FR-018 preserves existing equipment quantity aggregation semantics."
      - "FR-014 requires existing English technical/functional Hugo content, empty optional template/sample columns and quality-gate verification; the user removed feature-specific release-note and version obligations on 2026-11-23."
  - path: outputs/specs/features/G4IT-48/plan.md
    mode: full
    key_facts:
      - "The planned shared category resolver preserves reference-before-type and workspace-before-organization precedence, with unmatched types treated as INFRASTRUCTURE."
      - "Effective fraction and original unallocated quantity must persist with task-scoped evaluated results without mutating source input."
      - "The evaluated artifact is planned as ind_physical_equipment.csv, but route, row key, and lifecycle must be verified before implementation."
      - "This repository owns CI and Docker registry publication only; deployment is performed by another repository."
      - "Source usage context and non-Device provided values are retained/exported; existing technical/functional Hugo documentation is English-only, without a feature-specific release note or release version."
  - path: .github/skills/spec-tasks/resources/task-breakdown-template.md
    mode: full
    key_facts:
      - "The structure groups tasks into phases and assigns each a stable TASK identifier."
      - "Each task records type, story/phase, dependencies, files, description, acceptance criteria, and tests."
      - "A dependency graph and phase summary make sequencing and blockers reviewable."
  - path: .github/skills/spec-tasks/resources/test-strategy-template.md
    mode: full
    key_facts:
      - "Tests should cover business rules, validation, error handling, API/database contracts, and end-to-end journeys as applicable."
      - "Compatibility checks and isolated synthetic fixtures are expected; production data is not test data."
      - "Quality gates include tests, coverage, contract compliance, performance, accessibility, and security checks."
  - path: .github/knowledge/governance/ai-disclaimer-text.md
    mode: full
    key_facts:
      - "The canonical English disclaimer is a three-line blockquote beginning 'This file has been created (totally or partially)'."
      - "The corresponding idempotency marker is AI_DISCLAIMER v1.0."
---
<!-- AI_DISCLAIMER v1.0 -->
# Task Breakdown: G4IT-48 — BYOD/COPE equipment usage allocation

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

CONTEXT LOADED: 8 documents read, context manifest prepared.

**Branch**: `G4IT-48` | **Date**: 2026-11-20  
**Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)  
**Trace**: `e6fa33d4-27cf-4d12-84a2-3c70d47d88da`

## Task Generation Rules

- Execute tasks in dependency order; the initial evidence tasks are independent and are all gates before implementation.
- Paths below identify expected seams; refine the exact classes/files from the evidence tasks before coding.
- Test coverage target is ≥80% for new code and 100% for allocation/category/evaluation impact paths.
- Do not add a deployment task to this repository. Deployment belongs to the separate repository that consumes the Docker registry.
- Compile frontend and backend code using their existing project commands and run applicable configured tests. Local Docker image compilation is deferred; no Dockerfile change is planned.
- Update existing English technical/functional Hugo documentation and maintained CSV assets. No feature-specific release note, changelog entry or release-version selection is required.
- Perform manual accessibility validation on the implemented frontend. It is not a prerequisite to implementing the feature; pending validation must remain explicit and must not be reported as passed.
- Continue on the current feature branch. This dispatch does not commit, publish or deploy anything.

## Phase 1: Pre-implementation evidence gates

All seven evidence tasks are complete by source/configuration inspection. The
current findings, source references and implementation constraints are recorded in
[the completed Phase 1 evidence record](implementation-log.md#phase-1-evidence-completed---2026-11-23).
Checked items below describe verified contracts and design boundaries, not
implemented feature behavior, passing tests or successful builds.

### TASK-001: Verify active REST parity and validation conventions

- **Type**: evidence
- **Story**: Plan Phase 1 / FR-001, NFR-T3
- **Depends on**: none
- **Files**: `services/backend/src/main/resources/swagger/**`; relevant `apiinout` controllers, services, and frontend API clients
- **Description**: Inventory active physical-equipment REST operations and their consumers. Establish whether physical-equipment create/update is an active input path, and compare its validation of existing comparable fields. Record the decision before implementation.
- **Acceptance Criteria**:
  - [x] List active operations and corresponding client use, or document that CSV is the only supported input path.
  - [x] If an active operation exists, identify its OpenAPI source and exact nullable optional-field mapping seam.
  - [x] REST validation is added only if comparable existing fields are validated there, using those conventions.
  - [x] No speculative endpoint, new validation policy, unrelated contract, or authorization change is proposed.
- **Tests**:
  - [x] Evidence checklist links each operation to its OpenAPI definition and implementation/client.
  - [x] If no active operation exists, acceptance evidence explicitly says no REST change is required. Not applicable: existing inventory CRUD operations are implemented.
- **Evidence result**: Existing inventory collection GET/POST and item GET/PUT/DELETE are supported contracts. The inspected Angular input client is digital-service-specific, not an inventory editor. Extend only the existing inventory contract/mapping with optional values and established validation conventions. Do not infer external API usage from the absence of an Angular inventory editor.

### TASK-002: Verify task-scoped evaluated export contract

- **Type**: evidence
- **Story**: Plan Phase 1 / FR-011, FR-015
- **Depends on**: none
- **Files**: `services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/**`; `apiinout` export mappings; download route/controller
- **Description**: Trace the actual download route from request to artifact and row serialization. Verify whether `ind_physical_equipment.csv` is the task-scoped evaluated artifact; identify its task identity, row key, retention/lifecycle, and distinction from source-faithful `physical_equipment.csv`.
- **Acceptance Criteria**:
  - [x] Record the exact route, artifact name, task scoping, row key, producer, and download lifecycle.
  - [x] Confirm the selected evaluated artifact can carry persisted source context and effective fraction.
  - [x] Keep raw input export source-faithful; do not silently substitute it for evaluated export.
  - [x] Resolve any artifact mismatch before export implementation; do not create an unapproved endpoint.
- **Tests**:
  - [x] Trace evidence covers route-to-artifact mapping and evaluated row identity.
- **Evidence result**: The existing GET indicators-export download selects the latest inventory evaluation task and streams its task-id ZIP. `ind_physical_equipment.csv` is serialized per source equipment and criterion/lifecycle result before database aggregation. Its equipment-name fields are labels, not a database primary key. Use the source entity id and task id internally to obtain the persisted trace for serialization, without splitting grouped results or adding an endpoint.

### TASK-003: Verify evaluated persistence lifecycle and quantity semantics

- **Type**: evidence
- **Story**: Plan Phase 1 / FR-015, FR-018, RISK-001/002
- **Depends on**: none
- **Files**: `services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/InPhysicalEquipment.java`; `OutPhysicalEquipment.java`; related repositories, mappers, and Liquibase changelogs
- **Description**: Trace input and task-result persistence across evaluation, re-evaluation, export, and existing result cleanup/retention. Determine where source values, effective fraction, and original unallocated quantity can be stored without mutating the input.
- **Approved decision — 2026-11-23, option A**: Persist an evaluated trace per source equipment and evaluation task, retaining source usage context, effective fraction, and original quantity. Keep existing grouping keys and aggregation unchanged. Do not split groups by context or fraction. This decision does not constitute implementation or persistence-test evidence.
- **Acceptance Criteria**:
  - [x] Record whether task-result row quantity is scaled, and identify the existing original-quantity aggregation source.
  - [x] Specify persistence for effective fraction and source `contexteUsage`/`fractionUsage`, including historical task behavior.
  - [x] Identify the minimal nullable append-only migration, if a schema change is required; never edit an applied changeset.
  - [x] Preserve the original quantity for adoption-indicator aggregation even when calculation inputs are scaled.
- **Tests**:
  - [x] Evidence includes the result lifecycle and a decision for re-evaluation and legacy results.
- **Evidence result**: Add nullable source usage fields and a separate evaluated trace keyed by task and source-equipment id. Preserve raw source usage values, original Double quantity, evaluation-time category and effective fraction. The trace is owned by the task, with task deletion cascading to it, and must survive source-row replacement. Re-evaluation creates a new task snapshot. Same-task retry must reset or replace that task's trace idempotently. Historical results/ZIPs are not backfilled with fabricated evaluation metadata. Existing count-card row counts and low-impact integer aggregation remain unchanged.

### TASK-004: Identify shared category-resolution seam

- **Type**: evidence
- **Story**: Plan Phase 1 / FR-007, G4IT-42 compatibility
- **Depends on**: none
- **Files**: `services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java`; `apireferential/**`; evaluation category matching in `apievaluating/**`
- **Description**: Trace current G4IT-42 item category resolution and identify one reusable application-level location callable before evaluation and by indicators.
- **Acceptance Criteria**:
  - [x] Record matching by default-item reference before type, workspace before organization.
  - [x] Record exact `item_category = DEVICES` eligibility and unmatched → `INFRASTRUCTURE` behavior.
  - [x] Preserve existing custom/non-Device categories and G4IT-42 category validation behavior.
- **Tests**:
  - [x] Evidence identifies existing precedence tests or the exact missing cases to add.
- **Evidence result**: The resolver seam and inspected precedence test were already recorded in the preceding implementation log. The current inspection confirms the same resolution order in `InventoryIndicatorService`. Runtime resolver tests remain part of TASK-009.

### TASK-005: Verify CSV import and rejected-line behavior

- **Type**: evidence
- **Story**: Plan Phase 1 / FR-001–FR-006, FR-017
- **Depends on**: none
- **Files**: `services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/**`; CSV header/parser utilities; `messages.properties`; `messages_fr.properties`
- **Description**: Trace physical-equipment CSV header mapping, numeric parsing, category availability, error accumulation, and rejected-file reporting. Determine where headers can be normalized and where whole-line rejection/counts are formed.
- **Acceptance Criteria**:
  - [x] Identify the parser boundary to trim header whitespace and handle normalized-name collisions deterministically.
  - [x] Confirm category is available before deciding whether usage values are applicable.
  - [x] Record the existing convention for accumulating multiple field errors and distinguishing rejected lines from message count.
  - [x] No source value on a non-Device may change calculation or be lost from required source/export context.
- **Tests**:
  - [x] Evidence identifies the import regression fixture and focused test command/path.
- **Evidence result**: Cover mandatory-header validation, metadata parsing and final loading with one consistent header-normalization rule. The shared CSV mapper is used before business validation in both loading passes, so it must retain the new raw values without eager numeric conversion. Resolve category from the existing context, model and type at the row-validation seam before applying Device-only rules. Accumulate all applicable errors, reject normalized non-empty header collisions explicitly, and count rejected rows by distinct file/line rather than message count or equipment quantity. The existing 50,000 coherence-message threshold is a separate behavior, not a rejected-row counter.

### TASK-006: Inventory existing English documentation, CSV samples, and site verification

- **Type**: evidence
- **Story**: FR-014
- **Depends on**: none
- **Files**: `services/documentation/content/**`; `services/backend/volume_generation/input_template/2-physical-equipment.csv`; maintained sample CSVs; `services/documentation/hugo.toml`; existing documentation build definition
- **Description**: Inventory existing English technical and functional Hugo pages affected by the implementation, including loading/validation, equipment indicators and exports (the applicable `uc3_load_files.md`, `uc5_visualize_equipment_footprint.md`, and `uc7_export_files.md` pages if present). Locate every maintained physical-equipment sample and record the existing Hugo build/output command.
- **Acceptance Criteria**:
  - [x] Inventory actual paths before any documentation edit; do not create a speculative page structure.
  - [x] Confirm that only existing English technical/functional documentation and maintained CSV assets are in scope.
  - [x] No feature-specific release note, changelog entry or release-version selection is required.
  - [x] Identify the existing documentation build or site-output verification.
- **Tests**:
  - [x] Evidence lists every in-scope existing Hugo page, CSV template/sample, and verification command.
- **Evidence result**: The log inventories the existing inventory loading/evaluation/equipment/export pages, physical-equipment methodology, two getting-started pages, three technical API pages, two database-model pages and four CSV assets. The fourth CSV is the linked `User_Devices_01_Laptop_Template.csv`. The existing site build runs Hugo into `public/`; TASK-021 retains the equivalent project-root command. No site rendering or binary spreadsheet update was performed by this inspection.

### TASK-007: Verify repository CI and registry boundary

- **Type**: evidence
- **Story**: Plan Phase 1 / workflow boundary
- **Depends on**: none
- **Files**: `.github/workflows/backend-build-test-analyze.yml`; `.github/workflows/frontend-build-test-analyze.yml`; existing image build/publish workflow
- **Description**: Confirm which existing CI jobs build/test/analyze and publish Docker images to the registry. Document only the repository-owned checks and handoff evidence.
- **Acceptance Criteria**:
  - [x] Identify existing CI verification and registry publication steps.
  - [x] State explicitly that another repository handles deployment.
  - [x] No deployment, environment mutation, or deployment-repository change is included here.
- **Tests**:
  - [x] Evidence links the expected merge/CI checks.
- **Publication-evidence deferral — explicitly approved by the user, 2026-11-23**: Only the feature-image registry publication result formerly required by this test is deferred to TASK-023, because the feature is not implemented yet. All other TASK-007 checks remain required. This deferral is not a successful build, CI run, or publication result. Publication on main is a separate reference and does not prove publication of the feature image.
- **Evidence result**: Retain the completed static CI inventory already recorded in the log. This inspection does not rerun CI or promote configuration into successful execution/publication evidence.

## Phase 2: Input and category foundation

### TASK-008: Add optional usage context and fraction to input handling

- **Type**: implementation
- **Status**: done - first foundation batch verified, see [implementation resumption](implementation-log.md#implementation-resumption---first-foundation-batch).
- **Story**: FR-001, FR-013
- **Depends on**: TASK-001, TASK-003, TASK-005
- **Files**: `services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/**`; `apiloadinputfiles/**`; MapStruct/import mappings; append-only Liquibase changeset only if needed
- **Description**: Carry optional `contexteUsage` and `fractionUsage` from physical-equipment input through the confirmed persistence seam while preserving source quantity and electricity values unchanged.
- **Acceptance Criteria**:
  - [x] Legacy files without either optional column continue to import unchanged.
  - [x] Persist source values separately from the derived evaluated fraction.
  - [x] Preserve provided non-Device usage context/fraction for later export; do not apply them to calculation.
  - [x] Any schema change is nullable, additive, and append-only.
- **Tests**:
  - [x] Input mapper/entity tests cover absent and populated optional fields without source quantity/electricity mutation.
  - [x] Migration tests cover an existing inventory/result and new nullable values.
- **Verification boundary**: Five new input/migration tests passed within a successful six-suite, 56-test Maven run. Source usage fraction is deliberately retained as text to avoid eager conversion on exempt rows. The migration test executes the real new changeset on legacy H2 PostgreSQL-mode rows. Whole-import regressions, actual PostgreSQL integration, conditional validation and REST boundary tests remain later tasks, not results claimed here.

### TASK-009: Implement one shared item-category resolver

- **Type**: implementation
- **Status**: done - shared evaluation and indicator category integration verified by focused tests.
- **Story**: FR-007, FR-010
- **Depends on**: TASK-004
- **Files**: `services/backend/src/main/java/com/soprasteria/g4it/backend/apireferential/**`; evaluator and indicator injection points
- **Description**: Implement or expose one resolver used by both pre-calculation evaluation and inventory indicators; preserve the G4IT-42 matching and precedence rules.
- **Acceptance Criteria**:
  - [x] Default-item reference wins before equipment type; workspace records win before organization records.
  - [x] Only exact `DEVICES` is eligible; unmatched types resolve as `INFRASTRUCTURE`.
  - [x] Resolver does not alter category validation or custom category behavior.
  - [x] Both pre-calculation evaluation and inventory indicators consume this shared resolver, as required by the task description.
- **Tests**:
  - [x] Unit tests cover precedence, exact-case eligibility, workspace override, custom non-Device, and unmatched type.
- **Verification boundary**: The successful 81-test run includes seven category cases, nineteen indicator cases, three reference-selection cases, three allocation cases and the evaluator wiring tests. The evaluator's integration test mocks its allocation collaborator; the allocation tests exercise the real shared resolver. Full application and PostgreSQL integration are not claimed.

### TASK-010: Normalize headers and validate complete CSV rows

- **Type**: implementation
- **Status**: done - all task-specific acceptance and parser-test assertions supported after minimal wording normalization and current targeted verification.
- **Story**: FR-002–FR-004, FR-006, FR-008, FR-017
- **Depends on**: TASK-005, TASK-008, TASK-009
- **Files**: `services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/**`; CSV utilities; `messages.properties`; `messages_fr.properties`
- **Description**: Trim leading/trailing whitespace from header keys once; validate applicable Device usage values, accumulate all field errors, and reject each invalid line as a whole. Preserve/export valid provided non-Device values while ignoring them for calculation and avoiding eager numeric parsing that can fail the file.
- **Acceptance Criteria**:
  - [x] Headers with surrounding whitespace map to the optional columns. Normalized duplicates are handled deterministically.
  - [x] Device context accepts only exact BYOD, COPE, or empty. Fraction accepts a number in inclusive [0,1] or empty.
  - [x] Emit the exact FR-006 English messages and matching localized French messages using existing localization conventions.
  - [x] A line with one or multiple invalid applicable fields is wholly rejected and reported. Rejected-line count is distinct from number of messages.
  - [x] Valid non-Device source values are retained/exportable and do not affect calculation. Legacy headers remain valid.
- **Tests**:
  - [x] Parser tests cover whitespace headers, both errors on one line, bounds 0/1, invalid values, valid non-Device values, and legacy files.
- **Verification boundary — 2026-11-24**: The earlier three offline failures remain historical evidence. This continuation's first eight-suite run passed compilation and ran 97 tests, with two errors caused by new test contexts lacking workspaceId when existing loader logging called Context.log. After correcting only those fixtures, normal offline Maven production/test recompilation and the same eight suites passed: 97 tests, zero failures/errors/skips. The previous AdministratorRoleService bad-class-file diagnostic was not reproduced; inspected source/test package and public class references agree, but its historical binary cause is not established. No administrator source, generated class or cleanup was modified manually. Criteria 1, 2, 4 and 5 remain rejected/unchecked under the acceptance schema. Coverage percentages and full-import/PostgreSQL verification remain unmeasured. See the bounded troubleshooting continuation in implementation-log.md; neither task nor station is complete.
- **Final acceptance review — 2026-11-24**: Current source and selected test assertions corroborate header normalization/collision rejection, exact Device validation and inclusive bounds, accumulated localized errors, whole-row save/replacement exclusion, distinct rejected-row reporting, raw non-Device preservation and legacy mapping. Criterion 3 and the parser-test assertion remain checked. Entries 1, 2, 4 and 5 remain rejected because of their existing semicolons and/or backticks, not a failing behavioral test; their text and semantics were not rewritten. No absent required behavior was identified in the reviewed seams, so no code edit or test rerun was needed. The unreproduced compiler cause is historical, not an outstanding compilation prerequisite. The specified coverage thresholds and full backend regression belong to TASK-018, not an additional TASK-010 acceptance condition. Actual PostgreSQL, full-import and evaluated-export integration remain broader verification, not results of these eight suites. See the final bounded acceptance review in implementation-log.md.
- **Authorized schema review — 2026-11-24 (current disposition)**: Only the semicolon sentence separators and BYOD/COPE code delimiters in the four rejected assertions were reformulated. All five acceptance conditions and the parser-test condition are supported by current source and passing targeted tests. The current thirteen-suite offline Maven run exited 0 and includes the original eight CSV suites plus source-export mapping, allocation, engine, grouping and H2 trace tests. Its oversized stdout does not establish a new aggregate test count. The earlier eight-suite 97-test result remains separate historical evidence. Source CSV mapping is verified, not an end-to-end export or full import. Earlier in-progress/rejection statements above describe their historical checkpoints and are superseded by this disposition. TASK-018 coverage/full regression and TASK-019 PostgreSQL/export integration remain unverified.

## Phase 3: Evaluation, persistence, export, and indicators

### TASK-011: Persist evaluated fraction and unallocated quantity

- **Type**: implementation
- **Status**: done - task-scoped trace, repository reload and retry verified in targeted tests; PostgreSQL and evaluated export remain later verification.
- **Story**: FR-015, FR-018
- **Depends on**: TASK-002, TASK-003, TASK-008
- **Files**: New evaluated-trace entity/repository under `services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/`; evaluation lifecycle/serializer consumers; new append-only changeset under `services/backend/src/main/resources/db/changelog/in-out/`. Preserve the existing grouped `OutPhysicalEquipment` model and grouping keys.
- **Description**: Store the task-scoped effective fraction at evaluation time with the source context needed for export. Preserve an unallocated/original equipment quantity in the evaluated result or a confirmed equivalent for existing indicator aggregation.
- **Approved persistence boundary — option A**: Store this metadata in an evaluated trace per source equipment and task. Preserve existing grouped results, grouping keys, and aggregation without adding context or fraction to grouping keys. The trace retains source context, effective fraction, and original quantity independently from calculation scaling.
- **Verified lifecycle constraint**: Input loading replaces source rows and inventory deletion removes inputs before tasks. Do not use a source-row cascading foreign key that would discard retained evaluation metadata. Use a task-owned trace, preserve source identity as snapshot data, and make same-task trace retry idempotent.
- **Acceptance Criteria**:
  - [x] Every evaluated equipment row stores effective fraction (including 1.0 for non-Devices) independently from provided source values.
  - [x] Original quantity remains available for adoption indicator aggregation regardless of calculation scaling.
  - [x] Historical tasks retain prior behavior and re-evaluation follows the verified lifecycle.
  - [x] No source input row is mutated.
- **Tests**:
  - [x] Persistence tests verify task isolation, reload/export availability, re-evaluation, and original quantity retention.
- **Verification boundary**: The actual JPA repository and real migration were tested with H2 in PostgreSQL mode, including reload and replacement in the same transaction. The migration test separately checks source deletion and task cascade. Evaluated export serialization is TASK-014 and PostgreSQL integration remains unverified; no full end-to-end evaluation or indicator implementation is claimed.

### TASK-012: Apply allocation to every calculation request

- **Type**: implementation
- **Status**: done - all task-specific acceptance and calculation-test assertions supported after minimal wording normalization and current targeted verification.
- **Story**: FR-005, FR-009, FR-010
- **Depends on**: TASK-009, TASK-011
- **Files**: `services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java`; input-to-engine mapper/calculation service
- **Description**: Resolve category before calculation, derive the effective fraction (explicit value, otherwise BYOD 0.57, COPE 0.85, otherwise 1.0), and pass a defensive calculation copy with fraction-scaled quantity and electricity to every lifecycle/criterion request.
- **Verification constraint**: Demonstrate exactly one application of the fraction in every lifecycle impact and total electricity result. The existing documented use-phase formula multiplies quantity by electricity consumption, so do not mechanically scale both multiplicative inputs and introduce a squared fraction. Verify the calculation request units and enforce the required output ratios in the calculation tests.
- **Approved correction — 2026-11-24, Option A**: For a group containing an effective fraction below 1, electricity must be the sum of each source per-unit consumption times source quantity times effective fraction. Keep grouping keys, groups, quantities, counts and other aggregates unchanged. Preserve historical electricity semantics for groups without allocation and for digital-service evaluation. Existing historical results are not rewritten.
- **Acceptance Criteria**:
  - [x] Explicit fraction overrides context, including 0 and 1. Empty Device context defaults to 1.0.
  - [x] Exact Device BYOD/COPE defaults are 0.57/0.85. Non-Devices and digital-service equipment remain at 1.0.
  - [x] All lifecycle impact stages and electricity use the fraction. Original input quantity/electricity remain unchanged.
  - [x] Effective fraction is persisted on the evaluated result, not recomputed later by export or indicators.
- **Tests**:
  - [x] Calculation tests compare unallocated and allocated values across lifecycle stages and electricity, including 0, explicit override, and non-Device cases.
- **Verification boundary — 2026-11-24**: Offline Maven production/test compilation and nine selected regression suites passed. Real NumEcoEval tests require non-null USING electricity and verify all four lifecycle ratios. Real grouping/output-mapping tests prove 60 for the reported case, unequal/fractional quantities, zero factors and quantities, and historical unallocated/digital-service semantics. Criteria 1–3 remain unchecked because their semicolons are rejected by the acceptance schema, not because their behavioral tests failed. Coverage targets, actual PostgreSQL/native-view execution, full-suite and cross-component gates remain unverified; this task and the implementation station are not marked complete.
- **Final status review — 2026-11-24**: Current source and test assertions agree with the already-approved Option A correction and recorded nine-suite success. The eight-suite TASK-010 run is not substituted for that separate TASK-012 evidence. Criterion 4 and the calculation-test assertion remain checked, while entries 1–3 remain rejected/unchecked without rewording. Coverage remains TASK-018 verification. The grouping test evaluates the view formula in Java with mocked calculation/persistence collaborators, not a native PostgreSQL query or end-to-end evaluation. No schema, PostgreSQL, export, coverage or full-suite success is newly claimed.
- **Authorized schema review — 2026-11-24 (current disposition)**: Only semicolon sentence separators in criteria 1–3 changed. All four acceptance conditions and the calculation-test condition now have supported plain-text pass conditions. The current successful thirteen-suite run includes allocation defaults/overrides/exemptions, real NumEcoEval lifecycle and non-null electricity ratios, grouped electricity regressions and actual JPA trace reload/retry on H2. It does not replace the separate historical nine-suite evidence or establish a new aggregate test count. Original quantities/electricity, grouping keys and persisted evaluation-time fractions are corroborated by source and tests. Earlier in-progress/rejection statements above are historical and superseded here. No coverage, native PostgreSQL/view, full-suite or end-to-end evaluated-export result is claimed.

### TASK-013: Extend only confirmed active REST input operations

- **Type**: implementation
- **Status**: done - nullable contract regenerated and existing-operation cases verified, see [REST contract continuation](implementation-log.md#implementation-continuation---existing-rest-contract).
- **Story**: FR-001, NFR-T3 (conditional)
- **Depends on**: TASK-001, TASK-008
- **Files**: Only the OpenAPI definition, generated DTOs, mappings, and existing active physical-equipment REST operation identified in TASK-001
- **Description**: If and only if TASK-001 confirms an active supported REST input path, add optional nullable fields to that operation and map them using established conventions. Apply validation only where comparable existing fields are validated. If no active path is found, make no REST changes.
- **Acceptance Criteria**:
  - [x] No new endpoint or speculative REST validation is introduced.
  - [x] Existing authorization and backward-compatible request handling are unchanged.
  - [x] OpenAPI remains the contract source and generated types are refreshed through existing build tooling if applicable.
- **Tests**:
  - [x] Conditional REST contract tests validate the existing operation; otherwise TASK-001's no-change decision is recorded as completion evidence.
- **Verification boundary**: Four JSON/delegate/service/generated-mapper cases pass in the successful 60-test run: create/read, update, absent usage input and explicitly null usage input. The existing route/authorization definitions and controllers are unchanged. The tests use mocked persistence; they do not start an HTTP server or claim authorization integration or p95 latency measurements. Inventory create/update do not currently run the comparable-field validation used by the digital-service path; no speculative validator or new endpoint is introduced.

### TASK-014: Export evaluated source context and effective fraction

- **Type**: implementation
- **Status**: done - all task-specific assertions supported after authorized punctuation normalization and final bounded evidence review.
- **Story**: FR-011, FR-015, FR-016
- **Depends on**: TASK-002, TASK-008, TASK-011, TASK-012
- **Files**: Verified evaluated artifact serializer/mapping for `ind_physical_equipment.csv`; preserve `physical_equipment.csv` source export
- **Description**: Extend only the task-scoped evaluated artifact confirmed in TASK-002. Serialize source `contexteUsage` and the required fraction from persisted evaluation data; keep raw input export faithful to source.
- **Acceptance Criteria**:
  - [x] Device export carries context and a plain-decimal explicit/default effective fraction. No derived fraction is invented when both inputs are empty.
  - [x] Valid non-Device context/fraction are exported as provided. No default is derived and these values do not affect calculation.
  - [x] Export uses persisted evaluation result values and preserves task scoping/row identity.
  - [x] Raw source export values remain unchanged.
- **Tests**:
  - [x] Export tests cover BYOD 0.57, COPE 0.85, explicit 0.5, empty values, and valid non-Device values.
- **Verification boundary — 2026-11-24**: Current offline Maven production/test compilation and eighteen selected suites passed: 206 tests, zero failures/errors/skips, exit 0. Sixteen serializer cases cover defaults, override, empty usage, exact-category exemptions, raw non-Device values, 0/1 and plain decimal output. A deliberately different stored default proves no export recomputation. Actual JPA reload/serialization on H2 proves task isolation and source-context independence. Evaluator tests prove save/reload/export wiring, unchanged grouping and failure on a missing trace. Raw source mapping and existing digital-service evaluated columns remain unchanged. Current CSV, resolver, indicator, engine and REST suites corroborate TASK-009/010/011/012/013 completion within their existing boundaries. Acceptance entries 1 and 2 contain semicolons and remain rejected/unchecked without rewriting, despite passing behavior evidence. No functional contract or dependency gap was found. Full ZIP/download integration, PostgreSQL, full-suite, coverage, performance, frontend and accessibility are not claimed. Surefire emitted a fork-JVM shutdown diagnostic despite successful test results and exit 0. Exact commands, earlier failures and limits are in implementation-log.md.
- **Final authorized review — 2026-11-24 (current disposition)**: Only the two semicolon sentence separators and following sentence capitalization changed in the acceptance text. All four plain-text pass conditions and the export-test condition are supported. Current source and existing tests cover BYOD 0.57, COPE 0.85, explicit 0.5, empty values, preserved non-Device values, scientific-to-plain decimal output and a stored BYOD 0.42 proving no recomputation. Actual H2 JPA reload/serialization and evaluator save/reload/export assertions establish task/source scoping independently from live usage fields. The retained thirteen-suite run includes compilation and exit 0 but does not select ImpactToCsvRecordTest. Serializer evidence instead comes from the separately recorded eighteen-suite TASK-014 run with 206 passing tests, including sixteen serializer cases. Both completed Maven session exit codes were reconfirmed without rerunning tests or inferring new counts. No task-specific evidence gap or absent behavior was identified, so no test or production-code edit was needed. Earlier rejected/in-progress statements above are historical and superseded by this disposition. Broader TASK-018/TASK-019 and station gates remain open, including actual PostgreSQL and full ZIP/download integration.

### TASK-015: Add BYOD/COPE quantity-share indicators

- **Type**: implementation
- **Status**: done - all task-specific backend assertions supported after authorized punctuation normalization, normal regeneration/compilation and 91 passing selected tests.
- **Story**: FR-012, FR-018
- **Depends on**: TASK-009, TASK-011, TASK-012
- **Files**: `services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java`; existing equipment indicator DTO/mapping
- **Description**: Calculate BYOD/COPE percentages among Devices by reusing the existing equipment quantity aggregation, using persisted evaluation context/fraction and the original unallocated quantity.
- **Acceptance Criteria**:
  - [x] Numerator and denominator use the same quantity field and aggregation as existing equipment indicators. Allocation scaling never changes adoption quantities.
  - [x] Indicator rounding matches existing percentage indicators.
  - [x] No Devices yields the established “no data” result without division errors.
  - [x] Category membership uses the shared resolver. Custom/non-Device categories are excluded.
- **Tests**:
  - [x] Service tests cover hand-computed shares, fractional/empty quantities, no Devices, and unchanged existing aggregation behavior.
- **Verification boundary — 2026-11-24**: Normal offline Maven contract regeneration ran, but production compilation exited 1 with six errors in three generated client ApiClient classes using HttpHeaders.containsKey and HttpHeaders.entrySet. The five selected suites were not reached, so no passing compilation, new test count or task completion is claimed. No generated client, POM, dependency or build configuration was manually changed to bypass this blocker. Acceptance entry 1 also remains rejected because it contains a semicolon, without unauthorized wording normalization. All TASK-015 acceptance/test boxes remain unchecked. Exact source/output paths, command and blockers are recorded in implementation-log.md. TASK-016 and later tasks were not started.
- **Bounded diagnosis and current verification — 2026-11-24**: The reported missing-method errors were not reproduced by either current normal offline lifecycle. The POM, three external-client input specs and templates are unchanged. Those client executions do not consume the GreenIT response schema. The pre-existing patch-openapi-webclient execution, introduced in checked-in commit 45c23191 before this task, ran on three files and production/test compilation succeeded without any generator, dependency, template or manual artifact edit. Why the historical attempt failed remains unestablished. The first current five-suite run reached all 91 tests and failed only two new test-fixture assumptions: Double versus the existing BigDecimal count type, and an overridden redundant paging stub. After correcting only those fixtures, the identical command passed all 91 tests with zero failures/errors/skips and exit 0. Only acceptance entries 1 and 4 sentence punctuation/capitalization changed, including the additional semicolon found in the final schema review. All four plain-text pass conditions and the service-test condition are supported, so only TASK-015 is now done. Earlier blocked/rejected statements above remain historical and are superseded by this disposition. Exact commands, diagnosis, failures and acceptance evidence are in implementation-log.md. TASK-016 is not started. No PostgreSQL, full-suite, coverage, frontend or accessibility result is claimed.

### TASK-016: Expose accessible localized Equipment indicators

- **Type**: implementation
- **Status**: done — implementation-specific acceptance and focused tests satisfied after authorized punctuation-only normalization. Manual WCAG validation remains pending for TASK-020.
- **Story**: FR-012, NFR-UX1, NFR-UX3
- **Depends on**: TASK-015
- **Files**: `services/frontend/**` Equipment view and existing Key Indicators components; EN/FR translation resources
- **Description**: Add the two indicators through existing selection/display patterns, with localized labels, semantic text values, and the existing textual no-data state.
- **Acceptance Criteria**:
  - [x] English labels are BYOD Devices (%) and COPE Devices (%). French labels are Appareils BYOD (%) and Appareils COPE (%).
  - [x] Keyboard operation and visible focus match existing indicators. Percentage value is associated with its label and not conveyed by color alone.
  - [x] Design/accessibility criteria, evidence owner, and status are recorded in the implementation QA evidence.
- **Tests**:
  - [x] Frontend tests cover labels, values, and no-data state in both languages.
- **Verification boundary — 2026-11-24**: Final production build exited 0. Final focused Karma run passed 35 tests with zero failures/skips, including thirteen new real-resource EN/FR label, value, no-data, association and focus cases plus twenty-two existing regressions. Only indicator-section and inventory-util spec bundles executed. The initial 11-failure nested-TestBed setup error was corrected and its diagnostic retained in implementation-log.md. The new Equipment cards reuse persisted inventory-wide shares, existing rounding, native focus-triggered tooltip controls and textual double-dash no-data rendering. Manual keyboard/visible-focus/contrast/screen-reader/200%-zoom validation is explicitly pending for later TASK-020, not an implementation prerequisite or a completed result. Entries 1 and 2 remain unchecked because their existing code delimiters and/or semicolons are rejected by the acceptance schema, not because behavior tests failed. No full frontend suite, coverage, E2E, automated accessibility audit, backend or PostgreSQL result is claimed. No task after TASK-016 or workflow-state change occurred.
- **Final authorized review — 2026-11-24 (current disposition)**: Only code delimiters and semicolon sentence separators in acceptance entries 1 and 2 were removed/replaced, with following sentence capitalization and no semantic change. All three plain-text pass conditions and the frontend-test condition are supported. Current EN/FR resources and real-template tests confirm the exact labels, 57.4/28.6 displayed as 57 %/29 %, zero and sub-one values, independent missing shares, and textual double-dash no-data states for null/absent/empty responses. The Equipment container uses the existing indicator group and unfiltered response shares, refreshes translations on language change, and preserves both response properties through mapping. The value group is named with its label and displayed text. Native tooltip buttons, focus-triggered tooltips, tabindex-zero values and the shared two-pixel focus-visible outline provide the existing interaction implementation. Programmatic focus tests pass, but do not establish manual keyboard-only or visible-focus compliance. The prior final production build exit 0 and focused 35-pass, zero-failed/skipped result are retained, not rerun or expanded into container-test/full-suite coverage. Manual WCAG 2.2 AA validation remains PENDING at TASK-020 under the human accessibility reviewer role, identity still to be assigned by the user. No manual result or accepted risk is implied, and this later validation is not an implementation prerequisite. Earlier rejected/in-progress statements above are historical and superseded here. Only TASK-016 is done. TASK-017 is not started and the implementation station remains in progress.

## Phase 4: Dedicated English Hugo documentation (FR-014)

### TASK-017: Update existing English Hugo pages, templates, and samples

- **Type**: documentation
- **Status**: done — existing feature content/assets and corrected rendering retain their recorded verification. The authorized R01–R18 repairs now pass a fresh Hugo build and local-only htmltest with zero errors in 223 documents.
- **Story**: FR-014 (dedicated, separately traceable)
- **Depends on**: TASK-006, TASK-010, TASK-012, TASK-014, TASK-016
- **Files**: Actual relevant existing English technical/functional pages under `services/documentation/content/` (including applicable `uc3_load_files.md`, `uc5_visualize_equipment_footprint.md`, `uc7_export_files.md`); `services/backend/volume_generation/input_template/2-physical-equipment.csv`; every maintained physical-equipment sample CSV
- **Description**: First inventory actual relevant existing English technical/functional Hugo pages and maintained downloadable/sample CSVs, then update only those in-scope assets to reflect the implemented changes. Do not add a feature-specific release note, changelog entry or release version. Do not edit/create French Hugo pages.
- **Acceptance Criteria**:
  - [x] English docs explain both optional columns contexteUsage and fractionUsage, header whitespace handling, allowed values/range, exact validation behavior, and whole-line rejection/reporting.
  - [x] Docs explain BYOD/COPE defaults 57%/85%, explicit fraction override, exact item_category = DEVICES eligibility and precedence, and exclusions for non-Devices/digital-service equipment.
  - [x] Docs explain preserved source usage context, evaluated effective-fraction export behavior, plain-decimal output, and both quantity-share indicators.
  - [x] Downloadable physical-equipment template and every maintained sample contain both new columns with empty values.
  - [x] Existing technical documentation reflects affected contracts or architecture where applicable, without a feature-specific release note or release version.
  - [x] Documentation remains English-only and matches the final implemented behavior.
- **Tests**:
  - [x] Build/render Hugo with the existing documentation command and inspect generated pages/links.
  - [x] Validate template and sample headers/empty new cells, and verify documentation quality-gate coverage.
- **Actual verification — 2026-11-24**: The existing project command hugo --source services/documentation --destination public exited 0 both before and after the edits, using Hugo 0.170.1. Deprecation warnings remain. All four source CSVs were inspected completely after editing: widths are 16, 15, 15 and 22 respectively for the backend template, fill-in CSV, example CSV and laptop CSV. Every data row retains its original cells plus two empty usage cells. This is static schema inspection, not a new import test. The changed-file whitespace check exited 0 before final evidence-record updates. Documentation coverage maps to the existing plan quality checklist and pending TASK-021/TASK-022; those gates were not run or marked complete. Generated artifact reads are excluded from the current read boundary, so generated-page/link inspection was refused at preflight and its test remains unchecked. No downstream CI, manual accessibility or rendered-link success is claimed. Canonical disclaimers are applied to all modified Markdown; CSV disclaimers are WARN because comment rows would compromise the import format. Only TASK-017 is in progress; exact inventory, commands, limitations and acceptance evidence are in implementation-log.md.
- **Measured resumption — 2026-11-24 (current disposition)**: The previous blanket read restriction was incorrect; direct native reads succeeded. A new Hugo build exited 0 and reported 326 pages. Feature sections in all twelve rendered pages, their inspected feature cross-link targets and all three generated CSV downloads were inspected. The four maintained assets retain both optional headers and blank usage cells, with the recorded widths unchanged. However, the rendered Load Files page removes significant spaces from its invalid space-padded BYOD example, making it look identical to a valid BYOD value. The methodology page turns the factor-1 sentence into an ordered-list marker. The final accuracy assertion is therefore reopened, and the complete rendering test is not passed. The existing CI checker htmltest is missing locally, so no exhaustive link/fragment/image audit is claimed. TASK-017 remains in_progress for these concrete findings, not for an access prohibition. No documentation source was changed during validation; details are in the measured resumption section of implementation-log.md.
- **WSL tools retry — 2026-11-24 (current disposition)**: htmltest 0.17.0 is now available. The existing site-root command, with the repository configuration and external checks disabled, exited 1 with 52 reported link/anchor errors in 223 documents. An initial single-file invocation's seven additional fragment diagnostics were discarded after direct ID inspection and the site-root rerun; they are not added to the 52. The root check reports one existing TOC fragment on Load Files, nine existing relative targets on the technical loading API page, and three existing relative targets on the technical inventory-indicators page. Their source targets/headings were confirmed in HEAD; the remaining 39 occurrences are on other pages outside the feature-edited set, without a complete baseline rendering comparison. No unrelated existing defects were fixed or waived. Both feature rendering defects were rechecked and remain. The missing-checker prerequisite is resolved, but the documentation gate is an actual executed failure, not passed or skipped. No new Hugo build or source edit occurred in this retry.
- **Implementation resumption — 2026-11-24 (current disposition)**: Reworded the whitespace-sensitive examples and kept factor 1 in the prose sentence. Hugo exited 0 after the final edit; generated output confirms both corrections. The site-root checker exited 1 with 52 errors in 223 documents after the first correction; a subsequent line-wrapping-only edit was rebuilt but not followed by another link check. Existing link failures and remaining verification stay open.
- **Verification continuation — 2026-11-24 (current disposition)**: The site-root htmltest command was rerun after the final Hugo rebuild and again exited 1 with 52 errors in 223 documents, external links skipped. The earlier baseline classification and unresolved site-wide link gate remain unchanged.

- **Authorized link-check completion — 2026-11-26 (current disposition)**: Fresh before/after Hugo builds exited 0. The complete local-only htmltest baseline independently reproduced eighteen missing-target occurrences in eleven affected documents, with zero fragment errors, across 223 documents. After the approved R01–R18 changes the same checker exited 0 with zero errors in 223 documents. Both real AI-methodology candidate documents were fully inspected and contain no dedicated FAQ section, so only the broken hyperlink was removed, preserving its sentence and visible text. The architecture link now selects the shared evaluation overview, not the Ecomind-specific child. Seventeen source-aware replacement links and one removed hyperlink, plus manually inserted canonical disclaimers in eleven existing English sources, are the only source corrections. Prior feature content, four CSV schemas/empty cells and rendered-example inspection remain historical evidence, not newly executed asset or visual tests. Those retained results and current passing navigation support only TASK-017 completion. TASK-021's dedicated content/asset quality review and the broader integration, human accessibility, performance and release tasks remain open. No acceptance text, other task status or workflow state was changed. Exact commands, sessions, counts and actual implementation gate self-check are in the latest implementation reports.

## Phase 5: Verification and readiness

### TASK-018: Verify backend import, persistence, and allocation regression suite

- **Type**: testing
- **Status**: done — final backend coverage verify ran 1,424 tests (1,415 passed, zero failures/errors, nine skipped). A single fresh report establishes 293/293 changed handwritten executable lines and 210/210 changed branch outcomes. Frontend and broader integration/readiness obligations remain separate and open.
- **Story**: FR-001–FR-010, FR-013, FR-015, FR-017
- **Depends on**: TASK-010, TASK-011, TASK-012
- **Files**: Backend tests in `services/backend/src/test/**`; relevant test fixtures
- **Description**: Add/run focused unit and integration coverage for validation, complete-line rejection, persistence, calculation, and unchanged legacy behavior.
- **Acceptance Criteria**:
  - [x] Invalid context and fraction errors accumulate and reject the entire row; rejected rows and validation messages are counted separately.
  - [x] Exercise defaults, explicit bounds/override, whitespace headers, valid non-Device source preservation, and legacy files.
  - [x] Prove every lifecycle stage and electricity are scaled while original source values and unallocated indicator quantity are preserved.
  - [x] Meet ≥80% new-code coverage and 100% category/allocation/calculation-path coverage.
- **Tests**:
  - [x] Run the unfiltered backend test suite and coverage/verification job; record results in `implementation-log.md`.
  - **Final backend coverage reconciliation — 2026-11-26 (current disposition)**: The identified local origin/main merge base is e33f2158b1ed56bdebf682532a3d1549cb255dce. The complete final-tree delta includes committed and local tracked changes plus seven untracked production Java files, with no staged source changes. The final isolated report independently measures all 26 changed executable backend sources at 293/293 lines and 210/210 branch outcomes; the other six paths change declarations only. Both formerly missing grouping and caller-exemption outcomes have focused regressions. All changed calculation/category/allocation families, exact source intersections and test attribution are retained in coverage-report.md. The final focused run passed 357 tests without failures/errors/skips; the unfiltered offline backend coverage verify passed with 1,424 tests, zero failures/errors and nine skips. Only TASK-018 is complete within its backend unit/regression boundary. TASK-020 frontend measurement still has a 0/2 changed-line container and unreported runtime registration, TASK-019 native integration remains separate, and the known documentation failure and overall implementation gate remain open. No downstream station or workflow-state change occurred.
  - **Latest verification — 2026-11-25**: Backend suite and `-Pcoverage verify` exited 0 with 1,395 tests (1,386 passed, zero failures/errors, nine skipped). JaCoCo analyzed 173 classes, but current feature-wide percentages were not available; the ≥80% new-code and 100% impact-path criteria remain unproven.
- **Actual verification — 2026-11-24**: Executed the specified Maven test goal without a test selector, skipped tests, failure-ignore setting or code-generation bypass. The only additions were the previously documented offline and bounded-logging options: mvn -o -f services/backend/pom.xml -Dorg.slf4j.simpleLogger.defaultLogLevel=warn -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugin.compiler=info -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugin.surefire=info -Dmaven.test.redirectTestOutputToFile=true -DtrimStackTrace=true test. The terminal session g4it48-task018-full completed with exit code 0. Its final 93.2 KB accumulated output exceeded the terminal tool's inline limit and was spooled outside the declared read paths. That spool and generated test reports were not opened through another tool. Total, passing, failing, error and skipped counts therefore remain unverified, not zero or inferred from prior targeted runs. No targeted rerun or error-correction cycle was performed. Current source-test assertions identify the relevant header, validator, legacy mapper, allocation, lifecycle/electricity and original-quantity scenarios, but their individual execution results were not available in the bounded output, so no acceptance checkbox is newly checked. Acceptance entries 1 and 4 are rejected under the acceptance schema because each contains a semicolon. They were logged/skipped without wording changes. The POM configures JaCoCo reporting at verify, not test. Existing coverage-report presence/content was not inspected because generated artifact directories are excluded from reads. New-code coverage and category/allocation/calculation-path coverage are not measured. The coverage/verification job was not launched. No PostgreSQL, frontend, accessibility, CI, full coverage or generated-page/link success is claimed. See the TASK-018 checkpoint in implementation-log.md for the precise evidence gap and handoff.
- **Evidence-only continuation — 2026-11-24 (current disposition)**: The six dispatched skill/context documents and services/backend/pom.xml were read fully as inert data. The POM configures JaCoCo 0.8.13 prepare-agent and verify-phase reporting. Its coverage profile names services/backend/target/site/jacoco/jacoco.xml for Sonar, but declares no JaCoCo check goal, numeric coverage limit or new-code baseline. The requested services/backend/target/surefire-reports/ inspection was refused at preflight because generated artifacts remain excluded from reads even under services. No report listing/read, spool access, copy or alternate-path access occurred. Report presence and contents are unknown. Required report inspection was blocked, so no additional verification goal was launched. The earlier full test exit 0 remains evidence of that invocation only. Total/pass/failure/error/skipped counts and relevant per-suite counts remain unknown. Acceptance entries 1 and 4 remain rejected without wording changes, entries 2 and 3 remain unchecked pending accessible execution evidence, and the combined test/coverage obligation remains incomplete. Closure requires permitted report inspection, execution/inspection of existing coverage verification, a defined new-code baseline and measured evidence for both coverage requirements, plus resolution of rejected acceptance entries. TASK-018 remains in_progress. TASK-017's generated-page/link blocker and all other task statuses are preserved. No workflow-state or implementation edit occurred. See the evidence-only continuation in implementation-log.md.
- **Measured resumption — 2026-11-24 (current disposition, superseding the evidence-only checkpoint)**: Direct report reads succeeded. An unfiltered offline verify ran 1378 tests: 1369 passed, zero failed, zero errors and nine Azure integration tests skipped. Production/test compilation passed. The lifecycle then exited 1 at Failsafe 3.2.5 integration-test because maven-surefire-common:3.2.5 is not cached for offline resolution. A standalone JaCoCo report on the existing shared data failed with Unknown block type c0. Without deleting that file or changing configuration, a new full test/report invocation using a confirmed-new execution-data filename exited 0, produced the same test counts and generated XML/CSV/HTML reports for 173 classes. Global reported line coverage is 6343/7058 (89.87 percent), not a new-code proxy. ItemCategoryResolver covers all nine lines and ten branches. PhysicalEquipmentAllocationService covers 37/38 lines (97.37 percent) and 21/24 branches (87.50 percent), with the invalid-context exception at line 59 unexecuted. The four new reported service classes cover 82/83 lines, but complete diff-based feature coverage and excluded layers remain unverified. Current suite results and inspected assertions support acceptance entries 1-3 without changing their text; entry 4 and the full verification obligation remain unchecked. TASK-018 remains in_progress for measured coverage and verification gaps, not for report-access or unknown-count blockers. No source, test, POM, dependency or workflow-state change was made.
- **Implementation resumption — 2026-11-24 (current disposition)**: Added invalid-context, empty-context and no-workspace reference tests. An initial complete offline test/report run passed 1380 tests with nine skips and measured allocation at 38/38 lines and 23/24 branches, reference resolution at 14/14 lines and 18/18 branches. That run also included a blank-fraction assertion subsequently removed because whitespace-only source fractions are invalid at ingestion. After adding the empty-context assertion, the next complete run exited 1 with 1377 tests, 18 failures, 578 errors and nine skips; both edited test classes passed four tests each. The log contains widespread class-not-found errors following reported compilation; a checked class was absent and later appeared in the shared build directory, but the cause is unknown. No report was generated by that failed run, and the final removal was not followed by another run, so final-source coverage is unmeasured. Neither this test goal nor the prior report is a successful full verify or PostgreSQL integration result.
- **Verification continuation — 2026-11-24 (current disposition)**: The complete offline test/report run subsequently passed without cleaning, with 1380 tests, 1371 passed, no failures/errors and nine skipped. Added a valid empty-fraction assertion for the COPE default and repeated the complete test/report using separate execution data; the same counts passed. JaCoCo now reports allocation 38/38 lines and 24/24 branches, ItemCategoryResolver 9/9 lines and 10/10 branches, and PhysicalEquipmentReferenceResolver 14/14 lines and 18/18 branches. The earlier missing-class failure did not recur, but its cause is unknown. Class-level metrics do not establish all evaluation impact paths or complete diff-based feature coverage; full verify and actual PostgreSQL integration remain open.
- **Verify after cache correction — 2026-11-24 (current disposition)**: The user reported successful online verify. An independent offline verify with fresh isolated coverage data exited 0: 1384 tests, 1375 passed, zero failures/errors and nine skipped; packaging and JaCoCo reporting succeeded. Allocation remains 38/38 lines and 24/24 branches. The Failsafe summary reports zero completed integration tests. The dependency blocker is resolved, but complete feature new-code and evaluation impact-path coverage, actual PostgreSQL integration and the repository CI/analysis evidence remain open.
- **Backend retry — 2026-11-25 (current disposition)**: The TASK-018 unfiltered offline Maven test goal exited 0. Terminal output exceeded the inline limit and was saved outside the declared read scope; generated reports were not inspected, so counts, skips and per-suite results are not newly claimed. No coverage report was generated by this test goal. The coverage threshold and combined verification obligation remain open.

### TASK-019: Verify evaluated export, REST parity, and migration contracts

- **Type**: testing
- **Status**: in_progress — task-scoped export, HTTP authorization, REST contract and PostgreSQL migration criteria are evidenced; the standalone test report now exists, while broader integration remains open.
- **Story**: FR-011, FR-015, NFR-C1, NFR-T3
- **Depends on**: TASK-013, TASK-014
- **Files**: Backend API/integration tests; OpenAPI definitions if TASK-013 applies; Liquibase migration tests
- **Description**: Verify the confirmed task-scoped evaluated route/artifact/row key, source-faithful input export, task-result persistence, and any REST contract/migration changes.
- **Acceptance Criteria**:
  - [x] Evaluated export uses persisted context/fraction and source export remains unchanged.
  - [x] Existing task scoping, authorization, and historical result behavior are intact.
  - [x] REST compatibility tests run only for a confirmed active REST operation; no speculative endpoint is added.
  - [x] Any migration is append-only, nullable, and compatible with existing rows.
- **Tests**:
  - [x] Run applicable backend integration/OpenAPI/migration tests and retain the test report.
  - **Latest verification — 2026-11-25**: The 65-test key-contract/security/migration suite and the 69-test calculation/export suite passed with zero failures, errors or skips (134 tests in two separate selections). Results are attributed separately and retained in `test-report.md` and `coverage-report.md`; the standalone report exists. Earlier PostgreSQL-profile and live-E2E evidence remain historical and broader full integration remains open.
- **Focused verification — 2026-11-24**: The existing Maven test goal ran offline with six selected classes: EvaluateServiceTest 28, ImpactToCsvRecordTest 16, PhysicalEquipmentUsageMigrationTest 1, InPhysicalEquipmentMapperTest 2, PhysicalEquipmentEvaluationTraceMigrationTest 2 and InPhysicalEquipmentInventoryContractTest 4. All 53 passed with zero failures, errors or skips; the separate run is not added to the full-suite count. Tests cover evaluated snapshot reload and CSV mapping, source value preservation, task/source mismatch handling, existing DTO/controller behavior, H2 migration and task snapshot isolation. H2 PostgreSQL mode is not PostgreSQL, the direct controller test uses mocked repositories and does not exercise HTTP/security filters, and the complete ZIP download journey was not exercised. Acceptance criteria and the combined integration obligation remain open. The report is in implementation-log.md and the local Maven log.
- **Final local integration checks — 2026-11-24**: Four new direct-controller tests verify writer, administrator and denied-user download behavior, latest-task selection, and a real ZIP containing source and evaluated CSV records generated by the actual mappers. The ZIP test uses mocked storage and a directly supplied trace; it does not exercise the evaluator, database, HTTP or security filters end to end. A focused run of this class with CSV and H2 trace tests passed 22 tests. Full offline Maven verify ran 1384 tests with zero failures/errors and nine skips but exited 1 when Failsafe could not resolve maven-surefire-common 3.2.5 from the offline cache. Actual PostgreSQL, HTTP/security filters and full evaluator-to-download integration remain open; no acceptance checkbox is newly marked complete.
- **H2 verification — 2026-11-25**: The two existing migration test classes were run with in-memory H2 in PostgreSQL compatibility mode. The first run failed with three errors because the changelog resources were missing from the runtime classpath despite Maven reporting resource copying; the source files existed and the cause remains unknown. The same command succeeded on retry: three tests passed, zero failures, errors or skips. This checks legacy input migration, trace isolation, repository reload, retry and deletion behavior, and evaluated CSV mapping on H2; it is not PostgreSQL or HTTP integration. No source was changed or build output cleaned. TASK-019 remains in progress.
- **PostgreSQL integration-test profile — 2026-11-25**: PhysicalEquipmentPostgresMigrationTest (tag postgres-integration, excluded by default) ran via `-P integration-test` against the local PostgreSQL container: 3 tests passed, zero failures, errors or skips, each in an isolated g4it48_t_<hex> schema dropped afterwards. This supports the append-only/nullable migration criterion on real PostgreSQL. HTTP/security filters and the full evaluator-to-download journey remain unverified, so TASK-019 stays in progress and no checkbox is marked.
- **Full backend verification — 2026-11-25**: After the POM profile change, offline `verify` ran 1384 tests, zero failures/errors, nine skips, BUILD SUCCESS (first attempt failed only at JaCoCo report on a corrupt shared `jacoco.exec` "Unknown block type c0"; passed after deleting that stale file). `-P integration-test test` ran 3 tests, zero failures/errors/skips. A read-only query on the local container found 0 remaining `g4it48_t_%` schemas. Backend README documents the profile. Status unchanged.
- **HTTP security verification — 2026-11-25**: InventoryIndicatorExportSecurityTest sends 6 requests through the real SecurityConfig JWT filter chain, AuthenticationFilter, method security and RestExceptionHandler (MockMvc; storage and repositories mocked). Results: missing or invalid token -> 401, ungranted workspace -> 401, no inventory role -> 403, read-only -> 403, writer -> 200 with ZIP. This evidences the authorization part of criterion 2 at the HTTP layer. Found a pre-existing inert inventory-to-workspace check in AuthService.checkUserRightForInventory; it is out of scope. Default test: 1390 tests, 0 failures/errors, 9 skips. `-P integration-test`: 3 tests, all passed. The full evaluator-to-download PostgreSQL test is blocked: no full-context PostgreSQL test harness, no referential seed data, and external calculation services are not running. Status remains in_progress.
- **Live authenticated E2E — 2026-11-25**: `services/backend/e2e/g4it48-live-e2e.sh` ran on the local stack (Keycloak direct grant, real HTTP and PostgreSQL). Steps: create inventory, load, evaluate, download the ZIP, delete. Evaluated `ind_physical_equipment.csv` has columns contexteUsage/fractionUsage with the expected values: BYOD 0.57 (qty 2 -> 1.14), COPE 0.85 (3 -> 2.55), explicit 0.5, empty for no context, and no derived fraction for a non-Device. Source `physical_equipment.csv` keeps the source values and quantities. An invalid row was rejected with both localized messages. The test inventories were deleted (0 remain). This evidences criterion 1 on the live stack. Two defects remain open: the indicator API serializes the BYOD/COPE percentages as `{"present":true}` (no JsonNullable module), and an unknown type plus a usage context fails the whole load. Impact values were not checked (local referential lacks factors). Status remains in_progress.
- **Live-check corrections — 2026-11-25 (current disposition)**: Corrected the unknown-type lookup that read the first element of an empty organization/global referential list. Real reference/category/usage validation now preserves the existing row-level type error even with BYOD, covered by a new regression. Registered Jackson 3 nullable-response serialization and verified numeric/null indicator values through real HTTP/security converters. The final six-suite run passed 105 tests; full offline backend verify passed 1395 tests with zero failures/errors and nine skips. The observed count of 11 equals source quantities 2 + 3 + 1 + 4 + 1, not five rows. Live-stack retesting requires the running backend to reload the changes; no new live result or PostgreSQL-profile execution is claimed. Status and checkboxes remain unchanged.
- **User-executed live retest — 2026-11-25 (current disposition)**: User-supplied terminal output after restarting the backend shows inventory 10, loading task 18 COMPLETED_WITH_ERRORS (one deliberately invalid row with both usage messages), evaluation task 19 COMPLETED, and HTTP 200 ZIP/source/evaluated downloads with expected raw values and 0.57/0.85/0.5 effective fractions. Cleanup returned 204 and zero inventories remaining with the test name. Numeric 30 percent BYOD/COPE shares confirm D1 resolved live. The output does not exercise an unknown equipment type (D2) or display impact values; neither is newly validated live. No independent rerun or database cleanup query is claimed; task status and checkboxes remain unchanged.
- **Generated artifact inspection — 2026-11-25 (current disposition)**: Direct read-only inspection of evaluation-batch-19 CSVs now establishes 100 result rows: 80 Smartphone ERREUR rows with characterization-factor lookup errors and 20 Server OK rows with numerical impacts. Source and reject CSVs corroborate the user's displayed source values and one rejected line. D1 remains resolved, but Device numerical impact ratios cannot be validated with this reference-data setup. D2 has no live fixture coverage yet. No application rerun, credential access, source change or task completion is claimed.
- **Local reference fixture — 2026-11-25 (current disposition)**: User-authorized TASK-018/019 fixture prerequisite implemented in `services/backend/src/test/resources/g4it48-local-reference.sql`, with three new cases in PhysicalEquipmentPostgresMigrationTest and loading/removal instructions in the backend README. This explicit-opt-in, transactional test-only SQL fills missing global Smartphone factors for the five live criteria and four stages, plus missing France mix criteria. It preserves existing factors and scoped overrides and never runs at application startup. Final targeted PostgreSQL run: 6 passed, zero failures/errors/skips in isolated schemas. Existing water-use Smartphone factors were found and preserved, refining the earlier diagnosis. Loading into the active local application database and restarting the backend remain user actions; neither was performed or claimed. Actual live ratios, D2 and the station gates remain open.
- **Fixture relocation — 2026-11-25 (current disposition)**: The user clarified the local database is recreated from scratch. The fixture now lives at `services/backend/src/test/resources/sql/g4it48-local-reference.sql`; the test lookup and README command were updated. The existing PostgreSQL test class passed all 6 tests after relocation. The fixture is still loaded explicitly after Liquibase creates the reference tables, not automatically at PostgreSQL initialization. The earlier location and already-instantiated-database concern are historical; live loading and impact verification remain open.
- **Business-source checkpoint — 2026-11-25 (current disposition)**: The existing evidence identifies Smartphone factor lookup failures but does not establish whether the common referential itself needs an update. No validated business source was identified in the reviewed run records; no repository-wide search was performed because the current dispatch does not declare a codebase read scope. Request the authoritative source and its provenance, version, units, criteria and lifecycle coverage, and a declared scope for inspecting the referential and tests. Synthetic test factors must not enter the common referential. No new tests were run; live ratios, D2 and the station gates remain open.
- **Test-only decision — 2026-11-25 (current disposition)**: The user declined production Liquibase reference-data changes and retained the synthetic fixture exclusively for tests. The user reports restarting local components, but has not confirmed loading the opt-in fixture after Liquibase initialization. No live API or database was accessed in this continuation. Live impact ratios and D2 remain open; no production data change or completion is claimed.
- **Automatic setup request — 2026-11-25 (current disposition)**: The user clarified that the fixture must be invoked automatically at the appropriate point in the test flow, not manually loaded. The current dispatch lacks a declared codebase read scope, so the integration point has not been inspected or implemented. Setup must remain test-only and account for Liquibase initialization, isolation and referential caching. Live verification and this task remain open.
- **Test execution expectations — 2026-11-25 (current disposition)**: The user requires autonomous H2 unit tests suitable for GitHub CI and local PostgreSQL integration tests runnable with one command after starting the components. Required test SQL must be applied automatically by the relevant tests. No test code was inspected or changed, and no new tests were run; this requirement remains open.

### TASK-020: Verify indicator behavior and accessibility

- **Type**: testing
- **Status**: in_progress — frontend CI passed 1719 tests; E2E and manual accessibility remain pending.
- **Story**: FR-012, FR-018, NFR-UX1, NFR-UX3
- **Depends on**: TASK-015, TASK-016
- **Files**: `services/frontend/**` Equipment/Key Indicators tests and accessibility evidence
- **Description**: Verify indicator arithmetic, localization and no-data rendering. Perform manual WCAG 2.2 AA validation after frontend implementation; it is not an implementation prerequisite. Record existing automated accessibility results separately where available.
- **Acceptance Criteria**:
  - [ ] Hand-computed BYOD/COPE values match existing quantity aggregation and rounding; no Devices shows “no data.”
  - [ ] Any existing automated accessibility checks are recorded separately from manual validation and do not substitute for it.
  - [ ] Manual evidence covers keyboard-only selection, visible focus, screen-reader label/value announcement, and 200% zoom.
  - [ ] Pending manual validation is explicit and is not a passing result or an implementation prerequisite. Retest after any accessibility fix; no risk is accepted without explicit human approval.
- **Tests**:
  - [ ] Run `npm --prefix services/frontend test -- --watch=false` and existing Equipment-view E2E/accessibility checks; record manual evidence owner and status.
- **Measured resumption — 2026-11-24**: The existing configured CI command was attempted without modifying scripts. The normal invocation exited 5 with a Windows UNC-directory warning. Native Node with the existing npm command still selected the Windows ng shim and exited 5. Directly invoking the existing Angular CLI through offline npm exec with native Linux Node produced the explicit prerequisite error: installed esbuild targets win32-x64, while this runtime requires linux-x64. That attempt exited 127 before tests. No current frontend test count, coverage, E2E or accessibility result is claimed, and no installation or dependency/lockfile edit was performed. The prior 35-test Windows result remains historical. Manual evidence still needs an assigned human reviewer.
- **WSL tools retry — 2026-11-24 (current disposition)**: The same native Linux/offline Angular CI invocation was retried after the user reported installing esbuild. Building exited 127 before tests with Host version 0.27.3 does not match binary version 0.27.2, followed by EPIPE. Installed esbuild metadata and @angular/build both require esbuild 0.27.3; the expected project-level @esbuild/linux-x64 package manifest is absent. The selected binary now executes, but must be version-aligned with the JavaScript host actually used by this project. Its selection path was not inferred by reading environment variables. No package installation, downgrade, manifest/lockfile/shim change, test execution or accessibility review occurred. Prior focused-test success remains historical; the station is not advanced.
- **Implementation resumption — 2026-11-24 (current disposition)**: Linux esbuild reports 0.27.3 and the native Linux Angular CI command exited 0. Chrome Headless executed 1719 tests successfully. Global coverage was 72.64% statements, 57.17% branches, 67.7% functions and 72.86% lines; feature new-code coverage was not measured. The build emitted duplicate-key warnings for existing English translations. No E2E or manual accessibility review was executed, so the task and combined test obligation remain incomplete.
- **Live indicator API check — 2026-11-25**: On the live stack, `GET …/indicators/equipments` returned `byodDevicesPercentage` and `copeDevicesPercentage` as `{"present":true}` with no numeric value; the expected value was 30%/30% for the fixture. The UI cannot render the shares, so the hand-computed criterion fails live (defect D1 in implementation-log.md). Manual WCAG is still pending. Status remains in_progress.
- **D1 correction — 2026-11-25 (current disposition)**: Added a Jackson 3 serializer for the generated nullable values without changing the OpenAPI contract or the calculations. The real Boot Jackson mapper and security/MVC chain now return numbers (including unrounded and zero values) or explicit null, covered by two added HTTP regressions. All eight HTTP/security tests and the full 1395-test backend verify passed (nine skips in the full run). Live UI/backend retesting and manual WCAG remain pending, so no task criterion is newly checked.
- **D1 live confirmation — 2026-11-25 (current disposition)**: The user reran the live script and supplied output showing HTTP 200 with numeric BYOD 30.0 and COPE 30.0 on five criteria, matching the fixture's original Device quantities (3/10 each). D1 is resolved on the restarted backend. This is user-executed API evidence, not an independently observed browser test. No-Device rendering, frontend rounding and manual WCAG remain pending; the combined acceptance condition and task remain unchecked/in progress.

### TASK-021: Verify documentation assets and site output

- **Type**: testing
- **Story**: FR-014
- **Depends on**: TASK-017
- **Files**: Hugo output for `services/documentation`; changed existing technical/functional pages, template, and samples
- **Description**: Check generated existing English technical/functional documentation and downloadable assets against FR-014. Do not produce French feature pages, a feature-specific release note, changelog entry or release version.
- **Acceptance Criteria**:
  - [ ] Generated output includes the updated English content and valid navigation/links.
  - [ ] Template and maintained sample files each contain both optional columns, blank in sample data.
  - [ ] Docs accurately cover validation, allocation/defaults, eligibility/exclusions, export context/fraction, and indicators.
  - [ ] Documentation reflects the implemented feature without requiring a feature-specific release note or release version.
- **Tests**:
  - [ ] Run `hugo --source services/documentation --destination public` (or the existing documented build command) and inspect generated output.
  - [ ] Run the repository's documentation/template quality-gate check, if configured.

### TASK-022: Run cross-component regression, performance, and quality gates

- **Type**: testing
- **Story**: NFR-T1, NFR-P1, FR-013
- **Depends on**: TASK-018, TASK-019, TASK-020, TASK-021
- **Files**: Existing backend/frontend CI workflows and relevant baseline/coverage reports
- **Description**: Run required repository CI and compare legacy import/evaluation behavior and performance to baseline, including indicator latency and coverage.
- **Acceptance Criteria**:
  - [ ] Legacy inventory/file results remain unchanged; no measurable import/evaluation regression and evaluation regression does not exceed 20%.
  - [ ] Indicator retrieval p95 remains below 500 ms; investigate regressions.
  - [ ] Backend/frontend build, tests, analysis, Sonar, dependency, and license gates pass.
  - [ ] Existing English technical/functional documentation and template/sample verification are included in the release-quality checklist.
- **Tests**:
  - [ ] Run existing backend/frontend CI-equivalent commands; attach baseline, coverage, and performance evidence.

### TASK-023: Publish CI image and hand off registry evidence

- **Type**: rollout
- **Story**: Plan Phase 5 / repository boundary
- **Depends on**: TASK-007, TASK-022
- **Files**: Existing image build/publish workflow and CI run evidence
- **Description**: After all gates pass, verify the existing CI workflow publishes the expected Docker image to the registry and record its image identity and validation evidence for the separate deployment repository.
- **Transferred evidence obligation — 2026-11-23**: This task now also owns the feature-image publication-result proof deferred from TASK-007 by explicit user decision. Record feature-specific CI and image identity evidence only when actually available. A publication on main is a distinct reference, not proof of feature publication. The deferral itself is not success evidence. Publication remains excluded from the current implementation-station dispatch.
- **Acceptance Criteria**:
  - [ ] Image publication is performed only by the existing CI workflow after required checks pass.
  - [ ] Registry image identity and passing validation evidence are available for the external deployment-repository handoff.
  - [ ] No task in this repository deploys the image, changes an environment, or modifies the deployment repository.
- **Tests**:
  - [ ] CI run and registry publication evidence are attached to the handoff; no deployment is performed here.

### TASK-024: Provide a distinct spreadsheet-safe inventory export

- **Type**: implementation
- **Status**: done - scoped implementation and automated verification complete; independent SAST reassessment and manual accessibility remain pending.
- **Story**: FR-019, NFR-S1, G4IT-48-SAST-002
- **Depends on**: TASK-014
- **Files**: InventoryIndicatorController and inventory-indicator OpenAPI contract; new InventorySpreadsheetExportService and focused tests; existing export-security tests and RestExceptionHandler for the new 422 response; FootprintDataService and inventory-header component/tests; EN/FR translations; existing English export/API documentation; implementation-log.md and sast-report.md.
- **Description**: Implement the user's option 1 decision. Convert the authorized stored task ZIP into a separate XLSX download without rewriting raw CSV or persisted inputs. Reuse the installed Apache POI dependency. Keep the existing raw download unchanged and present it explicitly as machine exchange, not a spreadsheet-safe artifact.
- **Acceptance Criteria**:
  - [x] XLSX cells retain CSV text but have string types and no formulas or hyperlinks, including formula-like prefixes and quoted separators or newlines.
  - [x] Existing raw CSV ZIP bytes, input values and evaluated fraction semantics remain unchanged.
  - [x] Both downloads enforce existing authentication, inventory ownership and role restrictions.
  - [x] Spreadsheet format limits and invalid archives fail explicitly without truncation or unsafe fallback, and temporary workbook files are cleaned up.
  - [x] The UI offers the spreadsheet download and requires explicit formula-risk confirmation for the separate raw CSV download, with EN and FR labels.
  - [x] Existing English functional and API documentation distinguishes spreadsheet safety from raw machine exchange.
- **Tests**:
  - [x] Run focused backend workbook/security/export regressions and frontend service/header tests, then configured frontend lint and production build.
- **Verification - 2026-11-27**: The five-suite offline Maven selection passed 71 tests with zero failures, errors or skips: workbook conversion 6, real security/MVC export 16, raw-export controller 4, ownership guard 29 and evaluated CSV mapper 16. The exact 32,767-character cell boundary and rejection at 32,768 and 16,385 columns were exercised; the 1,048,576-row guard is implemented, not million-row load-tested. Successful and exceptional temporary-file cleanup is implemented; no independent OS-level leak/load test is claimed. Eleven frontend tests passed, including explicit raw confirmation and no unsafe fallback. Configured lint, final production build and Hugo build exited 0 with documented build/theme warnings. An added HTTP regression first caught 422 being mapped to 500; the directly dependent handler was corrected and the same selection passed. An exact-client-header check then caught binary-only Accept blocking JSON error serialization. The client now accepts XLSX and JSON, and a separate final 16-case HTTP run verifies both success and the actual 422 JSON payload with that same header. Those sixteen cases are not added to the earlier 71-case total; frontend tests, lint and production build also passed again afterward. Raw CSV is still unsafe for direct spreadsheet opening by design, as explicitly warned. No independent SAST pass, coverage threshold, manual accessibility, live Excel/LibreOffice test, full suite or CI result is newly claimed.

## Dependency Graph

```
Independent evidence gates:
TASK-001  TASK-002  TASK-003  TASK-004  TASK-005  TASK-006  TASK-007
   |         |         |         |         |         |         |
   +---------+---------+---------+---------+---------+---------+
                             |
TASK-008  TASK-009  ->  TASK-010
   |         |           |
   +------ TASK-011 <- TASK-002, TASK-003
               |
          TASK-012 <- TASK-009
          /   |   \
TASK-013 TASK-014 TASK-015 -> TASK-016
             \        \        /
              +------ TASK-017 (also TASK-006, TASK-010, TASK-012)
                         |
TASK-018 (010/011/012)  TASK-019 (013/014)  TASK-020 (015/016)
                         TASK-021 (017)
                              \   |   /
                               TASK-022 -> TASK-023

Added after the initial breakdown (FR-019, SAST-002 option 1):
TASK-014 -> TASK-024 (spreadsheet-safe XLSX export)
```

## Summary

| Phase | Tasks | Blocked | Notes |
|---|---:|---:|---|
| Phase 1 — Evidence gates | 7 | 0 | Complete by source/configuration inspection, not runtime verification |
| Phase 2 — Input/category | 3 | 0 | TASK-008, TASK-009 and TASK-010 done within their task-specific verification boundaries |
| Phase 3 — Evaluation/export/UI | 7 | 0 | TASK-011 through TASK-016 and TASK-024 (FR-019 spreadsheet-safe export) done within task-specific boundaries; manual accessibility and broader verification remain pending |
| Phase 4 — Documentation | 1 | 0 | Dedicated FR-014 existing English technical/functional Hugo/template/sample task |
| Phase 5 — Verification/readiness | 6 | 0 | Regression, accessibility, site output, CI and registry handoff only |
| **Total** | **24** | **0** | No deployment task in this repository |

## Open Questions

- TASK-001 through TASK-007 now have the required source/configuration evidence recorded in implementation-log.md. Their completion does not complete any implementation, build, runtime test or release gate.
- No feature-specific release note or release version is required. Application version assignment belongs to the later merge/release process.
- Any conditional REST change is limited to a verified active operation and established comparable-field validation conventions.
- TASK-012 targeted grouped-electricity correction is approved without splitting groups. Verification evidence and remaining limits are recorded in implementation-log.md.
- TASK-014 punctuation normalization is now explicitly authorized and complete without semantic changes. No task-specific functional question remains. Broader integration and quality gates remain pending.
