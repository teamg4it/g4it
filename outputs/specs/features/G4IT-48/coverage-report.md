---
workflow: feature-implementation
trigger: user
date: '2026-11-25'
status: validated
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: quality-validation/coverage
agent: quality-validator
skill: coverage-assessment
sensitivity: internal
context_retrieval: files
revision: 27a4ddc678e48d78ce3fcc90205956f907b277e0
comparison_base: origin/feat/g4it-ia-backbone
comparison_base_revision: 3aa81b674dab9b579c672bfc6186dc8dd9268ba6
baseline_source_trees:
  backend_java: be3558b8a06bc8756a04b845fe78c27cc8e9fbac
  frontend_src: e27c3b70ccddc764c1d0077ad7b3ee0cc3999bb7
validation_status: passed
gate_result: pass
thresholds:
  backend_new_code_percent: 80
  backend_impact_paths_percent: 100
  frontend_new_components_services_percent: 80
fresh_tests:
  backend:
    total: 1450
    passed: 1441
    failures: 0
    errors: 0
    skipped: 9
  frontend:
    total: 1737
    passed: 1737
    failed: 0
global_coverage_context:
  backend:
    covered_lines: 7355
    total_lines: 11754
    covered_branches: 2196
    total_branches: 4068
  frontend:
    covered_lines: 5726
    total_lines: 7737
    covered_branches: 1599
    total_branches: 2760
new_code_coverage_status: reconciled-for-current-base-all-measured-delta-covered
new_code_coverage:
  backend:
    covered_lines: 350
    total_lines: 350
    covered_branches: 236
    total_branches: 236
    line_percent: 100
    branch_percent: 100
  frontend:
    covered_lines: 63
    total_lines: 63
    covered_branches: 49
    total_branches: 49
    line_percent: 100
    branch_percent: 100
  core_changed_impact_paths:
    covered_lines: 135
    total_lines: 135
    covered_branches: 88
    total_branches: 88
dast_status: skipped-by-user
inputDocuments:
  - outputs/specs/constitution.md
  - outputs/specs/features/G4IT-48/tasks.md
  - outputs/specs/features/G4IT-48/plan.md
  - outputs/specs/features/G4IT-48/implementation-log.md
  - .github/skills/knowledge-plane/SKILL.md
  - .github/skills/code-implementation/SKILL.md
  - .github/instructions/output-metadata.instructions.md
  - .github/knowledge/governance/schemas/output-metadata.schema.json
  - .github/knowledge/governance/ai-disclaimer-text.md
  - services/backend/pom.xml
  - .github/workflows/feature-implementation.yml
  - services/documentation/hugo.toml
  - services/documentation/.htmltest.yml
  - .github/skills/coverage-assessment/SKILL.md
  - .github/workflows/quality-validation.yml
  - services/frontend/package.json
  - services/frontend/angular.json
  - services/frontend/karma.conf.js
  - services/backend/target/site/jacoco/index.html
  - services/backend/target/site/jacoco/jacoco.csv
  - services/frontend/coverage/frontend/lcov.info
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiindicator/business/InventorySpreadsheetExportServiceTest.java
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiuser/business/AuthServiceTest.java
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorExportSecurityTest.java
  - services/frontend/src/app/layout/inventories-footprint/header/inventories-header-footprint.component.spec.ts
  - .github/skills/java-testing/SKILL.md
  - .github/skills/angular-testing/SKILL.md
changeHistory:
  - date: '2026-11-25'
    description: 'Created the implementation-owned coverage evidence report at the exact declared shared path'
    changes:
      - 'Attributed existing class counters to historical log inspection, not fresh generated-report reads'
      - 'Recorded actual verify outcomes without inventing current percentages'
      - 'Kept TASK-018 open for a complete new-code denominator and impact-path matrix'
  - date: '2026-11-25'
    description: 'Recorded current successful backend verification without claiming fresh coverage measurements'
    changes:
      - 'Separated a non-reproduced compilation failure from the unresolved coverage acceptance'
      - 'Retained historical class counters and the missing feature baseline and path matrix'
  - date: '2026-11-25'
    description: 'Established a source-change baseline and ran coverage-focused verification'
    changes:
      - 'Inventoried 25 changed or new Java files and 729 physical added lines against the G4IT-42 HEAD'
      - 'Recorded successful isolated JaCoCo verification and meaningful additional path tests'
      - 'Distinguished source additions from the missing executable denominator and retained blocked thresholds'
  - date: '2026-11-25'
    description: 'Established the actual local main merge base and preserved all branch and working-tree source changes'
    changes:
      - 'Measured 32 backend production files with 783 added physical lines, including seven untracked files'
      - 'Identified sixteen files excluded by the existing JaCoCo configuration without treating exclusion as uncovered code'
      - 'Ran fresh isolated backend verify successfully and recorded the exact generated-report read preflight refusal'
      - 'Distinguished the inferred main target from the configured feature tracking branch and retained unknown executable counters'
  - date: '2026-11-25'
    description: 'Reassessed the exact XML permission and recorded the direct reader limitation'
    changes:
      - 'Superseded the blanket XML preflight refusal with the actual narrow read exception'
      - 'Recorded the 850.7 KB reader-size diagnostic without fabricating XML counters or freshness'
      - 'Reconfirmed 32 changed production paths and retained exclusions and unknown executable denominators'
      - 'Stopped at missing skill and current-gate prerequisites without lowering coverage targets'
  - date: '2026-11-25'
    description: 'Measured final merge-base changed executable lines through bounded native XML reads'
    changes:
      - 'Measured 249 of 252 reported changed executable lines and 174 of 178 changed-line branch outcomes after six meaningful new tests'
      - 'Separated complete reported allocation/request subsets from unmeasured excluded grouping, persistence, export and frontend code'
      - 'Recorded two successful isolated full verifies and 166 passing focused tests without weakening coverage targets'
  - date: '2026-11-26'
    description: 'Measured previously excluded handwritten logic without waiving feature-wide coverage'
    changes:
      - 'Expanded only the existing Maven coverage profile to measure handwritten mapper, model and serialization methods'
      - 'Separated ten executable changed files from six declaration-only changed files'
      - 'Measured 41 of 41 additional changed executable lines and 31 of 32 branch outcomes'
      - 'Recorded the frontend generated-report read boundary and retained incomplete aggregate and impact-path acceptance'
  - date: '2026-11-26'
    description: 'Reconciled the complete backend intersection and directly inspected frontend LCOV'
    changes:
      - 'Measured 293 of 293 backend changed executable lines and 210 of 210 branch outcomes from one fresh report'
      - 'Covered both remaining grouping and caller-exemption outcomes with two focused regression tests'
      - 'Measured the frontend reported subset separately and retained its container and instrumentation gaps'
  - date: '2026-11-26'
    description: 'Recorded the fresh documentation rerun without claiming new coverage'
    changes:
      - 'Preserved measured backend/frontend coverage and recorded Hugo exit zero and local-only htmltest exit one'
      - 'Kept the actual implementation gate blocked by 52 fresh link errors in 223 documents'
  - date: '2026-11-26'
    description: 'Recorded verification of the four authorized documentation corrections'
    changes:
      - 'Preserved all prior coverage measurements without a new code-coverage execution'
      - 'Recorded thirteen removed diagnostics and thirty-nine remaining errors in 223 documents'
      - 'Retained the actual implementation gate failure without changing task or workflow state'
  - date: '2026-11-26'
    description: 'Recorded the approved database and TOC continuation without new coverage claims'
    changes:
      - 'Preserved application coverage provenance and previous history'
      - 'Recorded twenty removed diagnostics and nineteen remaining errors in 223 documents'
      - 'Kept the ambiguous MockUp fragment and actual implementation gate explicitly open'
  - date: '2026-11-26'
    description: 'Recorded the agreed MockUp removal without changing application coverage evidence'
    changes:
      - 'Recorded the fresh successful Hugo build and failing eighteen-error local-link check'
      - 'Preserved all prior application line and branch measurements without claiming a rerun'
      - 'Retained the blocked gate and linked the complete remaining-target source diagnosis'
  - date: '2026-11-26'
    description: 'Recorded passing final documentation navigation without inventing application coverage'
    changes:
      - 'Recorded fresh Hugo success and eighteen-to-zero errors in 223 checked documents'
      - 'Preserved backend and frontend line/branch metrics with their original execution provenance'
      - 'Separated implementation gate self-check from pending integration and human accessibility obligations'
  - date: '2026-11-26'
    description: 'Updated the report lifecycle status to validated at the user’s request'
    changes:
      - 'Changed status from draft to validated without altering coverage evidence'
  - date: '2026-11-27'
    description: 'Executed independent frontend and backend coverage runs for the quality station'
    changes:
      - 'Preserved initial creation date, all implementation history and its original main-based provenance'
      - 'Measured 1445 backend tests with nine skips and 1733 passing frontend tests using existing instrumentation'
      - 'Used isolated non-appending JaCoCo execution data and preserved the existing coverage profile exclusions'
      - 'Recorded fresh global counters and directly inspected selected impact/allocation paths without treating them as whole-feature proof'
      - 'Recorded the denied inline XML processing and retained the unverified complete current-base new-code denominator'
      - 'Reopened document review and assessed the mandatory gate as blocked by missing differential evidence, not by global coverage alone'
  - date: '2026-11-27'
    description: 'Completed current-base differential reconciliation using permitted native reads'
    changes:
      - 'Proved the historical reference and confirmed base have identical backend Java and frontend source Git tree objects'
      - 'Reconciled the current diff with current generated HTML and LCOV, without reusing historical hit counters'
      - 'Measured backend source-unique new/changed statements at 348/350 lines and 231/236 branch outcomes'
      - 'Measured frontend source-mapped new/changed statements at 62/63 lines and 44/49 branch outcomes; every measured file exceeds 80 percent line coverage'
      - 'Verified all changed core allocation/calculation decision families at 135/135 lines and 88/88 branch outcomes'
      - 'Deduplicated anonymous-class source line 76 in the new spreadsheet exporter and retained noncritical missed branches'
      - 'Recorded the user-requested intentional skip of the optional DAST station without executing a scan'
      - 'Validated coverage with warnings while preserving historical findings, configured exclusions, tests, thresholds and instrumentation'
  - date: '2026-11-27'
    description: 'Resolved the measured coverage reserve with focused regression tests and fresh complete instrumented suites'
    changes:
      - 'Added five backend and four frontend test cases in four existing test files without production-code or configuration changes'
      - 'Covered ZIP directories, empty CSV, the actual Excel row boundary, suppressed cleanup failures, a non-workspace route and the HTTP 500 error response'
      - 'Executed actual XLSX and raw CSV saving, targetless explicit confirmation, and the raw CSV error alternative without format fallback'
      - 'Passed 56 focused backend tests and nine focused frontend tests, then 1450 complete backend tests with nine existing skips and 1737 complete frontend tests'
      - 'Measured all 350/350 backend changed mapped lines and 236/236 outcomes, all 63/63 frontend changed mapped lines and 49/49 outcomes, and the unchanged complete critical subset'
      - 'Confirmed targeted Angular ESLint success and retained existing thresholds, exclusions, integration boundaries and the intentional DAST skip'
      - 'Superseded only the coverage reserve; other quality-station warnings and aggregate approval remain separate'
holisticQualityRating: good
overallStatus: good
context_manifest:
  - path: .github/skills/coverage-assessment/SKILL.md
    mode: full
    key_facts:
      - 'Coverage is measured with the language-specific tool and compared with configured thresholds.'
      - 'The fallback default is 80 percent, but the validated project constitution defines the applicable new-code and impact-path scope.'
  - path: outputs/specs/constitution.md
    mode: full
    key_facts:
      - 'Backend new code requires at least 80 percent coverage and impact allocation/calculation paths require 100 percent.'
      - 'Frontend new components/services require at least 80 percent coverage; this is not an 80 percent whole-SPA criterion.'
  - path: .github/workflows/quality-validation.yml
    mode: full
    key_facts:
      - 'coverage is mandatory with a blocker gate and requires coverage-report.md.'
      - 'The aggregate quality-report requires the coverage station to have passed.'
      - 'security-dast is optional with warning severity; the user explicitly requested an intentional skip.'
  - path: services/backend/pom.xml
    mode: targeted
    key_facts:
      - 'The existing coverage profile measures handwritten mapper/model/configuration logic while retaining controller/generated/persistence-boundary exclusions.'
      - 'JaCoCo 0.8.13 prepares its agent and normally reports at verify; the report goal was invoked explicitly after tests without a packaging build.'
      - 'The default test selection excludes the PostgreSQL integration group; no numerical JaCoCo check goal is configured.'
  - path: services/frontend/karma.conf.js
    mode: full
    key_facts:
      - 'karma-coverage writes text-summary, HTML and LCOV into coverage/frontend.'
      - 'The existing browser is ChromeHeadlessNoSandbox and no numerical coverage threshold is configured here.'
  - path: services/frontend/angular.json
    mode: targeted
    key_facts:
      - 'The ci test configuration sets watch false, codeCoverage true and ChromeHeadlessNoSandbox.'
      - 'Both local and ci test configurations replace the environment with the existing test configuration.'
  - path: services/backend/target/site/jacoco/jacoco.csv
    mode: targeted
    key_facts:
      - 'PhysicalEquipmentAllocationService reports 38 covered lines and 24 covered branch outcomes, with none missed.'
      - 'ItemCategoryResolver and PhysicalEquipmentReferenceResolver report 9/9 and 14/14 lines, with 10/10 and 18/18 branch outcomes.'
      - 'The fresh InventorySpreadsheetExportService main-class record has 46 covered lines, zero missed lines and all 22 branch outcomes; its anonymous wrapper contributes a separate 2/2-line record.'
  - path: services/backend/target/site/jacoco
    mode: targeted
    key_facts:
      - 'Native source-HTML reads reconcile all 29 measured changed Java files; spreadsheet line 76 occurs in two class counters but once in the source-line denominator.'
      - 'All seven changed core allocation/calculation sources remain fully covered; AuthService 278 and RestExceptionHandler 96 now each cover both branch outcomes.'
      - 'The fresh spreadsheet source report covers cleanup lines 68/69 and all outcomes at directory guard 46, empty/BOM guard 84 and row/column guard 96.'
  - path: services/frontend/coverage/frontend/lcov.info
    mode: targeted
    key_facts:
      - 'All changed chart/filter outcomes in criterion range 215-295 and new sunburst mapper outcomes have positive hits.'
      - 'Header containing DA122, DA127 and DA133 now have positive execution counts; all seven changed mapped lines and all nine header branch outcomes execute.'
      - 'Container properties map to DA155, DA538 and DA564, and runtime chart registration maps to DA19; these containing statements all have positive hits.'
  - path: .github/skills/java-testing/SKILL.md
    mode: full
    key_facts:
      - 'JUnit 5 parameterized cases and temporary-directory cleanup extend existing fixtures without adding test infrastructure.'
      - 'Scoped Mockito fault injection preserves real filesystem behavior except the explicitly simulated cleanup failure.'
      - 'The row-boundary test uses the real Excel limit without materializing a million input records; its reflection tradeoff is recorded below.'
  - path: .github/skills/angular-testing/SKILL.md
    mode: full
    key_facts:
      - 'Angular TestBed retains the real component implementation while only the remote service boundary is stubbed.'
      - 'fakeAsync, microtask flushing and tick exercise the awaited blob and progress-delay behavior without real waiting.'
      - 'Browser download side effects are intercepted; production downloadFile and file-saver execution are not replaced with success spies.'
historical_implementation_context_manifest:
  - path: outputs/specs/constitution.md
    mode: full
    key_facts:
      - 'New backend code requires at least 80 percent coverage.'
      - 'Impact allocation/calculation paths require 100 percent coverage.'
  - path: outputs/specs/features/G4IT-48/tasks.md
    mode: full
    key_facts:
      - 'TASK-018 requires both feature-wide new-code and category/allocation/calculation-path coverage.'
      - 'Existing full-suite success does not close this task without measured coverage.'
  - path: outputs/specs/features/G4IT-48/plan.md
    mode: full
    key_facts:
      - 'The feature spans input, category, evaluation, persistence, export and indicators.'
      - 'Every lifecycle and electricity calculation must use the persisted fraction without source mutation.'
  - path: .github/skills/knowledge-plane/SKILL.md
    mode: full
    key_facts:
      - 'Files mode uses native document reads and does not require a document graph.'
      - 'Retrieval does not implicitly refresh an index.'
  - path: .github/skills/code-implementation/SKILL.md
    mode: full
    key_facts:
      - 'Implementation results include actual configured build/test verification.'
      - 'Unverified manual accessibility is recorded separately from tests.'
  - path: .github/instructions/output-metadata.instructions.md
    mode: full
    key_facts:
      - 'New output documents have draft lifecycle and unassigned review ratings.'
      - 'Updates retain creation dates and append change history.'
  - path: services/backend/pom.xml
    mode: full
    key_facts:
      - 'JaCoCo 0.8.13 reports during verify and excludes generated/controller/mapper/config/persistence layers.'
      - 'The coverage profile has a report path but no numeric check or diff baseline.'
  - path: outputs/specs/features/G4IT-48/implementation-log.md
    mode: sections
    key_facts:
      - 'The prior closure section records 83 of 83 lines and 81 of 84 branches across four core classes.'
      - 'It explicitly lacks the complete new-code denominator and all changed impact-path proof.'
---
<!-- AI_DISCLAIMER v1.0 -->

# Coverage Assessment Report

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## 2026-11-27 - Coverage reserve resolved after regression-test additions

**Current coverage assessment: passed / pass. The measured new-code coverage
reserve is resolved for both application stacks. DAST remains intentionally
skipped.**

This section supersedes the earlier coverage verdict and hit counts below.
The earlier assessments remain as historical evidence, including their
then-correct statements that tests had not changed. The baseline, production
line identities, source-map reconciliation and configured measurement scope
are unchanged. This correction modifies four existing test files only; it
does not modify production behavior, dependencies, instrumentation, exclusions
or coverage thresholds.

### Added regression behavior

The complete suites contain **nine additional cases: five backend and four
frontend**. Existing spreadsheet error assertions were also strengthened.

| Existing test suite | Added or strengthened behavior |
|---|---|
| InventorySpreadsheetExportServiceTest | Ignore ZIP directory entries and retain an empty CSV as an empty sheet; accept the actual last Excel row and reject the next without truncating the valid row; preserve the primary export exception and attach cleanup failure as suppressed |
| AuthServiceTest | Ignore a sufficiently long route whose organization/inventory segments match but whose workspace segment does not, without calling the inventory service |
| InventoryIndicatorExportSecurityTest | Return a real JSON HTTP 500 response through the existing authentication, authorization, controller and exception-handler chain when storage throws; do not retry the download |
| InventoriesHeaderFootprintComponent spec | Preserve explicit raw-export confirmation for an event without a target; execute real XLSX and raw CSV saving with the correct filename and progress reset; report raw CSV failure without spreadsheet fallback; assert the existing XLSX-specific error summary |

The Excel boundary test invokes the existing private row-copy helper through
the repository-standard `ReflectionTestUtils`: allocating 1,048,576 CSV rows
solely to reach this guard would make a regression test unnecessarily expensive.
It uses the actual `SpreadsheetVersion.EXCEL2007` limit and a real streaming
sheet, checking the last accepted row and rejection of the first invalid row.
No production visibility or limit was changed.

The cleanup test redirects only the export temporary-file creation and its
deletion failure in a scoped `Files` mock; other filesystem calls retain real
behavior. JUnit owns the temporary directory and removes the retained test file.
Angular success cases execute `downloadFile` and file-saver, while intercepting
browser URL/click effects to avoid actual downloads. They check the requested
blob, exclusive format endpoint, filename extension, pending/completed state
and URL revocation. Synthetic data only is used.

### Fresh executions and exact coverage

| Verification | Observed result |
|---|---|
| Backend focused instrumented run: the three changed Java suites | **56 tests passed**, no failures, errors or skips |
| Frontend focused header run | **9 tests passed** |
| Backend fresh unfiltered configured suite | **1,450 tests: 1,441 passed, 9 pre-existing skips, no failures or errors** |
| Frontend fresh unfiltered CI suite | **1,737 tests passed** |
| Targeted Angular ESLint from the frontend directory | **Exit 0**, no diagnostics |

The fresh backend report is generated exclusively from
`services/backend/target/g4it48-reserve-full.exec`, with
`jacoco.append=false`; it is not merged with previous or focused-run data.
The fresh frontend HTML/LCOV is from the unfiltered CI suite, not the nine-case
focused run. The whole-suite counts are therefore not combined with partial
coverage reports.

Commands used for the complete measurements:

```sh
mvn -o -f services/backend/pom.xml -Pcoverage -Djacoco.destFile=target/g4it48-reserve-full.exec -Djacoco.dataFile=target/g4it48-reserve-full.exec -Djacoco.append=false -Dorg.slf4j.simpleLogger.defaultLogLevel=warn -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugin.surefire=info -Dorg.slf4j.simpleLogger.log.org.jacoco.maven=info -Dmaven.test.redirectTestOutputToFile=true -DtrimStackTrace=true test org.jacoco:jacoco-maven-plugin:0.8.13:report
npm --prefix services/frontend run test:ci -- --reporters=dots
```

The confirmed comparison base remains
`origin/feat/g4it-ia-backbone`
(`3aa81b674dab9b579c672bfc6186dc8dd9268ba6`), with application HEAD
`27a4ddc678e48d78ce3fcc90205956f907b277e0` plus the four test-file edits.
Native fresh JaCoCo HTML/CSV and LCOV reads rechecked the established executable
inventory. Fully covered source-line matches were counted directly for changed
spans within partially covered legacy classes; complete-class records and
source HTML establish the fully covered new classes and nested bodies.
Source-unique line deduplication and frontend containing-statement rules are
the same as in the detailed inventory below.

| Scope | Covered / executable changed lines | Covered / changed branch outcomes |
|---|---:|---:|
| Backend: all 29 measured changed Java sources | **350/350 (100%)** | **236/236 (100%)** |
| Frontend: all eight measured changed runtime sources | **63/63 (100%)** | **49/49 (100%)** |
| Changed critical allocation/calculation subset: seven backend sources | **135/135 (100%)** | **88/88 (100%)** |

Every measured changed file now individually has 100% mapped line and branch
coverage where a branch denominator exists; files without decisions remain
`0/0`, not an invented percentage. The strict physical-added-line subsets are
also complete at **349/349 backend** and **55/55 frontend**. The larger gate
denominators above still include all previously identified containing
statements rather than dropping them.

| Previously incomplete source | Fresh changed lines | Fresh changed outcomes | Directly observed closure |
|---|---:|---:|---|
| InventorySpreadsheetExportService | 47/47 | 22/22 | Cleanup lines 68/69 execute; guards 46/84/96 report all 2/4/4 outcomes covered |
| AuthService | 1/1 | 2/2 | Guard 278 reports both outcomes covered |
| RestExceptionHandler | 8/8 | 2/2 | Guard 96 reports both outcomes covered |
| InventoriesHeaderFootprintComponent | 7/7 | 9/9 | DA127 executes; every BRDA outcome at 107/120/123/129/136 has positive hits |

The other changed line/decision families remain fully covered in the new full
reports. The spreadsheet source total remains **47**, not 48: source line 76
appears in both the main and anonymous class counters and is counted once.

Fresh **whole-project context only**, not the new-code gate:

| Stack | Covered / total lines | Covered / total branch outcomes |
|---|---:|---:|
| Backend configured scope, fresh report totals | 7,355/11,754 | 2,196/4,068 |
| Frontend full CI report | 5,726/7,737 | 1,599/2,760 |

These are the totals actually read from the new reports, not prior global
denominators carried forward. Existing legacy misses do not become new-code
misses. This result is **not 100% whole-application coverage**.

### Gate, boundaries and audit

- **Coverage gate: pass.** Both stacks exceed the unchanged 80% new-code
  requirement and the changed critical calculation subset meets the unchanged
  100% requirement. No measured changed line or branch reserve remains.
- Existing classifications still apply: six declaration-only Java files, the
  already-excluded controller, generated/persistence boundaries, type-only
  TypeScript and non-TypeScript assets are not assigned fabricated coverage.
  The controller HTTP fixture supplies behavioral evidence, not JaCoCo
  percentages for an excluded source.
- The nine existing backend skips remain explicit. No live PostgreSQL, full
  evaluation-to-download integration, E2E or accessibility certification is
  claimed.
- **DAST remains `skipped / skipped-by-user`, intentional.** No scan ran and
  no DAST security success is implied.
- Maven offline and Angular/Karma executed locally; native file/regex reads and
  session SQLite checked the inventory and arithmetic. No external MCP,
  production data, deployment or destructive operation was involved.
- An initial ESLint invocation used the repository working directory and
  matched no file. Correcting the working directory produced the successful
  targeted result above; the failed invocation is not counted as validation.
  An optional combined Git status/diff command was denied for command chaining
  and was not retried or represented as a successful inspection.
- The coverage skill names adapter documents that are not installed here.
  Existing Maven and Karma configuration supplied the working instrumentation;
  no missing adapter read or new installation is claimed.

This correction closes **coverage only**. Native completion hooks and the
station transition record its publication; the aggregate quality report and
its human checkpoint remain separate. Earlier Sonar, Java-lint and other
quality-station limitations are not removed by these tests.

## 2026-11-27 - Completed current-base reconciliation (superseded by post-test assessment above)

**Coverage assessment: passed / warning. DAST: skipped by explicit user request.**

The retry closes the differential-evidence blocker below using **native
Git/file/regex reads and session SQLite arithmetic**, without executing inline
Python, creating an analysis script, installing a tool, changing permissions or
modifying application code/tests. This is coverage validation only, not aggregate
quality, Sonar, integration, accessibility or release approval.

### Confirmed baseline, unchanged code and report provenance

- Measured HEAD remains **27a4ddc678e48d78ce3fcc90205956f907b277e0**.
  The worktree has no application source/test changes relative to this HEAD.
- Requested base **origin/feat/g4it-ia-backbone** resolves to
  **3aa81b674dab9b579c672bfc6186dc8dd9268ba6**.
- A native Git comparison between the historical reference
  **e33f2158b1ed56bdebf682532a3d1549cb255dce** and that confirmed base returns
  **no difference** under backend Java production sources or frontend sources.
  Independent Git tree-object reads establish the stronger identity:

| Production tree | Object at historical reference | Object at confirmed base |
|---|---|---|
| Backend Java | `be3558b8a06bc8756a04b845fe78c27cc8e9fbac` | `be3558b8a06bc8756a04b845fe78c27cc8e9fbac` |
| Frontend sources | `e27c3b70ccddc764c1d0077ad7b3ee0cc3999bb7` | `e27c3b70ccddc764c1d0077ad7b3ee0cc3999bb7` |

This proves baseline equivalence for these **specific source trees**, not that
the Git commits, branches, manifests or entire repositories are identical.
It permits verification of the earlier line-inventory candidates against the
confirmed base; it does **not** make old coverage hit counters current.
The current diff and current report spans were reconciled again, including
the later spreadsheet exporter, authorization guard and 422 handler.

The isolated JaCoCo execution data, CSV/index and frontend LCOV still have
exactly the sizes and modification times recorded in the initial assessment.
The underlying sources have not changed. The successful instrumented runs
therefore remain applicable: **1,445 backend tests (1,436 passed, nine Azure
skips, no failures/errors)** and **1,733 frontend successes**. They were **not
run again** during this read-only reconciliation.

### Measurement rules and exact results

1. Intersect current zero-context added/changed source ranges with current
   instrumented source lines. New files contribute every instrumented source
   line, including their handwritten nested/anonymous bodies.
2. Count a covered line when its execution counter is positive. In generated
   JaCoCo source HTML, `fc` and `pc` are executed lines; `nc` is missed. Count
   branch outcomes separately from their displayed covered/missed counters.
3. If a changed expression lacks its own line counter, retain the containing
   instrumented statement instead of silently dropping the change. These
   additions are explicit below; do not include unrelated pre-existing code.
4. Deduplicate by **source path and line**, not by class. In particular,
   spreadsheet line **76** belongs to both an outer method and anonymous-class
   constructor in class-level counters, but counts **once** at source level.
5. LCOV branch mappings are independent of DA membership: branch entries in
   changed expressions/default arguments remain included even without a DA on
   the same physical line. Erased assertions, annotations, declarations and
   nonexecutable assets do not receive invented line counters.

| Scope | Covered / executable lines | Line coverage | Covered / branch outcomes | Branch coverage |
|---|---:|---:|---:|---:|
| Backend, current configured handwritten-code scope | **348/350** | **99.43%** | **231/236** | **97.88%** |
| Frontend, complete changed source-mapped union | **62/63** | **98.41%** | **44/49** | **89.80%** |
| Core changed allocation/calculation paths, backend subset | **135/135** | **100%** | **88/88** | **100%** |

For comparison, strict physical-added-line intersections alone are **347/349**
backend lines and **55/55** frontend lines. Those narrower denominators do not
hide the containing-statement changes: the gate uses the larger unions above.
All **29** measured Java files and **eight** measured frontend files individually
exceed **80% changed-line coverage**; the lowest files are the spreadsheet
exporter (**45/47 = 95.74%**) and export header (**6/7 = 85.71%**).
The frontend result is not obtained by averaging away an under-80% component.
The constitution does not declare a separate per-component 80% branch floor;
noncritical branch gaps remain warnings, not claims of complete branch testing.
The separate **100% changed impact-path** requirement includes both executable
lines and decision outcomes.

### Complete backend inventory

Paths are relative to `services/backend/src/main/java/com/soprasteria/g4it/backend/`.
The listed numbers are **current source-line identities**, not copied historical
hit totals. Every listed line is executed except spreadsheet **68,69**.
`0/0` means no reported decision. Asterisk marks the core impact-path subset.

| Changed source | Reconciled executable lines | Covered/total lines | Covered/total branches |
|---|---|---:|---:|
| apievaluating/business/asyncevaluatingservice/EvaluateService.java * | 196,197,230,386-394,396-400,402-405,426,427,438-446,454,456,526,535-539 | 40/40 | 22/22 |
| apievaluating/business/asyncevaluatingservice/PhysicalEquipmentAllocationService.java * | 25,26,31,33-46,50,51,54,55,57-59,61,62,65-70,72,74,75,77,78,80,83 | 39/39 | 24/24 |
| apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java * | 94-96,108,109,118,129,131,160 | 9/9 | 2/2 |
| apievaluating/mapper/AggregationToOutput.java * | 51,99 | 2/2 | 2/2 |
| apievaluating/mapper/ImpactToCsvRecord.java | 56-59,61,63-65,69,70,72-74,76 | 14/14 | 20/20 |
| apievaluating/model/AggValuesBO.java * | 51,52,72,73,75 | 5/5 | 4/4 |
| apiindicator/business/InventoryIndicatorService.java | 99-115,117,130-133,136-142,144-149,151-154,157 | 40/40 | 16/16 |
| apiindicator/business/InventorySpreadsheetExportService.java | 33,40,42-48,50-55,57,58,60,61,64,65,67-71,76,80,82-89,92,95-97,99-103,105,107 | 45/47 | 19/22 |
| apiindicator/mapper/IndicatorRestMapper.java | 24 | 1/1 | 0/0 |
| apiinout/mapper/InPhysicalEquipmentMapper.java | 44,48 | 2/2 | 2/2 |
| apiinout/mapper/InputToCsvRecord.java | 92-94 | 3/3 | 0/0 |
| apiloadinputfiles/business/asyncloadservice/LoadFileService.java | 133,163-166,430,443-446 | 10/10 | 0/0 |
| apiloadinputfiles/business/asyncloadservice/checkobject/CheckPhysicalEquipmentService.java | 96,97 | 2/2 | 4/4 |
| apiloadinputfiles/business/asyncloadservice/checkobject/PhysicalEquipmentUsageValidator.java | 26-29,31-35,37-39,41,42,44,48,53-56,61,65 | 22/22 | 32/32 |
| apiloadinputfiles/business/asyncloadservice/loadmetadata/LoadMetadataService.java | 62 | 1/1 | 0/0 |
| apiloadinputfiles/business/asyncloadservice/loadobject/LoadPhysicalEquipmentService.java | 78 | 1/1 | 0/0 |
| apiloadinputfiles/mapper/CsvToInMapper.java | 71,72 | 2/2 | 0/0 |
| apireferential/business/ItemCategoryResolver.java * | 25-33,36,38,46-53,56-58,60-62 | 25/25 | 16/16 |
| apireferential/business/PhysicalEquipmentReferenceResolver.java * | 18-21,23,24,28-30,32,33,36-38,41 | 15/15 | 18/18 |
| apireferential/business/ReferentialImportService.java | 254-257,259,262-264,277,278,494,505-507 | 14/14 | 14/14 |
| apireferential/business/ReferentialService.java | 108 | 1/1 | 2/2 |
| apireferential/business/WorkspaceReferentialImportService.java | 300 | 1/1 | 2/2 |
| apireferential/mapper/ReferentialMapper.java | 76 | 1/1 | 0/0 |
| apireferential/modeldb/ItemType.java | 91,96,101,106,111 | 5/5 | 4/4 |
| apireferential/persistence/WorkspaceReferentialPersistenceService.java | 47 | 1/1 | 0/0 |
| apiuser/business/AuthService.java | 278 | 1/1 | 1/2 |
| common/utils/CsvUtils.java | 39,40,42-45,47-56,60,61,63-71,75,82-84,87 | 32/32 | 26/26 |
| config/JsonNullableConfig.java | 16,20,26,27,31,32 | 6/6 | 0/0 |
| config/RestExceptionHandler.java | 96-103 | 8/8 | 1/2 |
| **Total measured source-unique delta** | **350 lines** | **348/350** | **231/236** |

EvaluateService's changed argument at physical line **232** is represented by
its containing constructor-call counter at **230**, explaining the additional
line versus a strict hunk-only intersection. Spreadsheet anonymous-wrapper line
**80** is included; shared constructor line **76** is not counted twice.

The **36** changed Java files are fully classified: the **29 measured** above,
**six declaration-only** files below, and **one configured-excluded controller**:

- EquipmentIndicatorMapper: mapping annotations only; EquipmentImpactBO and
  EquipmentIndicatorBO: fields/comments only.
- InPhysicalEquipment: fields/annotations; PhysicalEquipmentEvaluationTrace:
  entity declarations and filtered Lombok boilerplate.
- PhysicalEquipmentEvaluationTraceRepository: abstract queries and annotations,
  no handwritten Java method body; the existing repository exclusion remains.
- InventoryIndicatorController: its new spreadsheet endpoint remains excluded
  by the **existing controller exclusion**. It is **unmeasured here**, not 100%
  covered. No exclusion was added to make this assessment pass.

Generated MapStruct/Lombok code, SQL/OpenAPI assets and live database behavior
are not silently assigned handwritten Java line percentages. This assessment
confirms coverage within the established instrumented scope, not unrestricted
whole-application or PostgreSQL integration coverage.

### Complete frontend inventory and source-map reconciliation

Paths are relative to `services/frontend/src/app/`, except `src/constants.ts`.
Every listed DA counter is positive except header **127**.

| Changed runtime source | Strict added DA lines | Additional containing DA lines | Covered/total union | Covered/total branches |
|---|---|---|---:|---:|
| core/service/business/inventory-util.service.ts | 268,269,272 | none | 3/3 | 4/4 |
| core/service/data/footprint-data.service.ts | 163 | none | 1/1 | 0/0 |
| core/service/mapper/sunburst-chart.mapper.ts | 28,30,32,34,40,42,65 | none | 7/7 | 8/8 |
| core/shared/echarts.module.ts | none | 19 | 1/1 | 0/0 |
| layout/inventories-footprint/critere/inventories-critere-footprint.component.ts | 215,221-224,226,229,231,233,236,239,243,244,246,255-257,259,261,263,269-272,274,276,278,283,285,291 | none | 30/30 | 28/28 |
| layout/inventories-footprint/header/inventories-header-footprint.component.ts | 57,106,114,115 | 122,127,133 | 6/7 | 4/9 |
| layout/inventories-footprint/inventories-footprint.component.ts | 147,150 | 155,538,564 | 5/5 | 0/0 |
| src/constants.ts | 93,171,195,201-203,205,207 | 158 | 9/9 | 0/0 |
| **Total** | **55/55 executed** | **7/8 executed** | **62/63** | **44/49** |

Containing statements retain the real runtime changes: ECharts registration
28 maps to **19**; new usage cards 158-165 map to **155**; cloud/AI fields
545-547 and 577-578 map to **538/564**; the reordered equipment view maps to
**158**. Header format selection, extension and error-summary changes map to
**122/127/133**. The default argument at header **120** has its own BRDA
outcome, not a separate invented DA entry.

The interceptor delta is an erased `keyof` assertion; the interface file
contains declarations only. Type-only annotations without changed runtime
statements and formatting-only hunks do not create missing executable
denominators. The three changed HTML templates, SCSS and two translation files
have no independent TypeScript DA denominator: they remain behavioral and
accessibility obligations, not fabricated coverage records.

### Changed impact-path verification

The seven asterisked backend sources above cover **135/135 changed mapped
lines and 88/88 decision outcomes**. Current source HTML confirms every
included decision is fully covered. This measures the changed allocation/
calculation decision families, not every combination of unrelated legacy
branches or a formal all-path proof.

Current Surefire suite records additionally confirm **zero failures/errors/
skips** in the relevant suites: reference resolver **4**, category resolver
**9**, allocation service **8**, evaluator **28**, NumEcoEval service **30**,
and grouping **1**. The current NumEcoEval report includes all **five**
`allocationIsAppliedOnceToEachCriterionAndLifecycleRequest` parameter cases
and `realCalculationScalesEachLifecycleAndElectricityWithoutChangingInput`.
Thus instrumented decision coverage is backed by the request/factor and real
numerical regression cases, not inferred solely from mocked class totals.

| Changed family | Current coverage evidence |
|---|---|
| Reference/category selection, workspace/global fallback and exact Device classification | Reference 15/15 lines, 18/18 outcomes; category including nested lookup 25/25, 16/16 |
| BYOD/COPE defaults, explicit bounds/override, exemption, snapshot and source preservation | Allocation 39/39, 24/24 |
| Task-scoped orchestration, persisted trace requirement, allocated versus legacy calculation dispatch | EvaluateService changed union 40/40, 22/22 |
| Every changed calculation request and single quantity-fraction application | Changed NumEcoEval lines 9/9, guard 2/2; current five-case request and real-numerical tests above |
| Inventory/digital-service grouping key and quantity-weighted electricity normalization | Aggregation mapper 2/2, 2/2; AggValuesBO 5/5, 4/4 |
| Adjacent evaluated-export and adoption consumers | Separately measured: ImpactToCsvRecord 14/14, 20/20; inventory indicators 40/40, 16/16 |

Unchanged misses such as the old AggValuesBO source-selection branch and the
old hypothesis-mapping lambda are not reclassified as new allocation changes.
Live PostgreSQL, native-view evaluation-to-download and E2E behavior are not
certified by these local unit/regression counters.

### Warnings, exclusions and native station disposition

- **Backend**: spreadsheet cleanup-failure lines **68/69** are unexecuted.
  Spreadsheet branch lines **46/84/96**, authorization guard **278**, and
  the 422 handler guard **96** each have one missed outcome. These are outside
  the changed allocation/calculation subset; they do not get a fabricated 100%.
- **Frontend**: header save/export statement **127** is missed. BRDA **107**
  (target fallback), **123** (format), **129** (extension, both outcomes) and
  **136** (error summary) account for **five missed outcomes**. Successful
  downloads and raw-format error alternatives remain focused test improvements.
  Passing 6/7 changed lines is not represented as full branch-path coverage.
- **Gate**: backend **99.43%**, frontend **98.41%**, and each measured changed
  file exceed the constitutional **80% line** target; the changed critical
  decision families satisfy **100%**. The global rates in the initial section
  remain context only. No configured threshold or exclusion was lowered.
- **DAST**: the optional `quality-validation/security-dast` station is natively
  **skipped / skipped-by-user**, `skip-kind: intentional`, following the current
  user request and acknowledged checkpoint. **No DAST scan was executed and no
  DAST security success is claimed.**
- **Tools/audit**: Git read-only inspection, native HTML/LCOV/CSV/regex reads,
  session SQLite aggregation/duplicate checks, and native workflow hooks/state
  commands. No external service, production data or destructive action was used.

Coverage can therefore close **passed / warning**, subject to the native
publication hooks and completion transition. The report itself does not
complete the parent quality workflow. The next aggregate quality station
remains separate and must retain the backend Sonar limitation, deferred Java
linter and other earlier station warnings.

## 2026-11-27 - Initial quality assessment (superseded by reconciliation above)

- **Assessment**: **incomplete / blocked-diff-coverage-evidence**.
- **Tools**: JaCoCo **0.8.13**, configured Maven unit/regression lifecycle;
  Angular/Karma **6.4.4**, existing Istanbul coverage instrumentation.
- **Tests**: both unfiltered test commands completed with exit **0**.
  Backend: **1,445 run, 1,436 passed, 0 failures, 0 errors, 9 skipped**.
  Frontend: **1,733 / 1,733 passed**.
- **Gate limitation**: fresh global metrics and selected source-path counters
  are available, but a complete executable new-code denominator and full
  impact-path reconciliation against the **current confirmed base** are not
  established. No passing coverage gate is claimed.
- **Sensitivity**: internal; no production data, credentials, environment
  values or external resources were accessed during this station.

### Applicable thresholds and baseline

The validated constitution defines **80% on backend new code**, **100% on
backend impact allocation/calculation paths**, and **80% on frontend new
components/services**. These take precedence over a generic whole-project
80% assumption. No threshold or exclusion was changed, and no environment
variable value was read.

The assessed HEAD is **27a4ddc678e48d78ce3fcc90205956f907b277e0**.
The comparison base is the user-confirmed
**origin/feat/g4it-ia-backbone**, not main. Current diff inspection includes
the authorization and XLSX remediations as well as the original feature.
The earlier implementation measurements below used a different, main-based
intersection and precede the latest remediations. Their successful counters
are retained as historical evidence, **not relabelled as today's denominator**.

### Fresh executions

From the repository root, without a test selector, skipped-test flag,
failure-ignore setting, new exclusion or dependency installation:

```text
mvn -o -f services/backend/pom.xml -Pcoverage -Djacoco.destFile=target/g4it48-quality-coverage-20261127-e85fc5c2.exec -Djacoco.dataFile=target/g4it48-quality-coverage-20261127-e85fc5c2.exec -Djacoco.append=false -Dorg.slf4j.simpleLogger.defaultLogLevel=warn -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugin.compiler=info -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugin.surefire=info -Dorg.slf4j.simpleLogger.log.org.jacoco.maven=info -Dmaven.test.redirectTestOutputToFile=true -DtrimStackTrace=true test org.jacoco:jacoco-maven-plugin:0.8.13:report
npm --prefix services/frontend run test:ci -- --reporters=dots
```

The JaCoCo execution filename was absent before the run. The report log
confirms loading that exact file and analyzing **316 classes**.
`jacoco.append=false` prevents merging stale execution data. The existing
coverage profile is active; its handwritten-code scope differs from the
narrower default profile. No packaging, Maven verify, remote backend build
or Sonar result is inferred from this test/report command.

The backend's nine skips are all reported by
`AzureGreenItFileSystemApplicationTests`. The configured PostgreSQL integration
group remains outside this run, separately from those nine skips. No live
integration, E2E or PostgreSQL coverage is claimed.

The frontend suite logs console errors, including test-context TypeErrors,
but Jasmine reports no failed tests and the command exits 0. Those logged
messages are not silently reclassified as failing assertions, nor repaired
as unrelated work.

### Fresh global metrics - context, not the new-code gate

| Stack | Covered / total lines | Line coverage | Covered / total branch outcomes | Branch coverage |
|---|---:|---:|---:|---:|
| Backend, current coverage profile | **7,340 / 11,760** | **62.41%** | **2,191 / 4,068** | **53.86%** |
| Frontend, entire CI suite | **5,722 / 7,737** | **73.95% reported** | **1,592 / 2,760** | **57.68%** |

The backend totals come from the generated JaCoCo index footer
(4,420 missed lines; 1,877 missed branches). The frontend runner also reports
**73.69% statements (5,921 / 8,034)** and
**68.31% functions (1,574 / 2,304)**.
Istanbul's displayed line percentage is truncated; rounding its exact
5,722/7,737 ratio to two decimals gives 73.96%, not a second measurement.

Global scope includes pre-existing code and, for the expanded backend profile,
generated mapper implementations located in otherwise measured packages.
Its percentages are therefore not comparable to the old narrower-profile
global percentage without scope reconciliation. These global rates are
**not** used to assert failure of an 80% **new-code** threshold.

### Directly verified paths and remaining gaps

Fresh native CSV/source-HTML reads establish:

| Source / selected path | Line evidence | Branch evidence | Boundary |
|---|---:|---:|---|
| PhysicalEquipmentAllocationService | **38/38** | **24/24** | Entire reported main class; allocation record is separate |
| ItemCategoryResolver | **9/9** | **10/10** | Entire reported class |
| PhysicalEquipmentReferenceResolver | **14/14** | **18/18** | Entire reported class |
| EvaluateNumEcoEvalService quantity-allocation guard | Line 129 and multiplication line 131 covered | **2/2** outcomes on the guard | Selected changed path, not the entire class or calculation call graph |
| AggValuesBO electricity normalization | Changed lines 51/52 and method lines 72/73/75 covered | **4/4** outcomes in the normalization method | The unrelated existing source-selection condition at line 60 misses one outcome |
| InventorySpreadsheetExportService | **44/46** main-class lines; **2/2** anonymous-wrapper lines | **19/22** main-class outcomes | New class; cleanup-failure lines 68/69 are unexecuted |

The three wholly covered resolver/allocation classes sum to **61/61 lines
and 52/52 branch outcomes**. This is a **selected subset**, not the complete
new-code denominator or proof of every lifecycle/electricity impact path.
The XLSX class also has missed directory-entry, empty-artifact and row-limit
branch outcomes; its noncritical export scope is not silently assigned the
100% allocation-path criterion.

Current frontend LCOV inspection also identifies concrete gaps: in the export
header, lines **127** and **131** have zero hits, and format-dependent branches
at **123**, **129** and **136** have missed outcomes. The error-path test does
not replace successful XLSX/raw-download execution. The raw-warning target
fallback at line **107** has one unexecuted outcome. These are focused test
improvement candidates, not an asserted complete list of uncovered feature
code or proof that the aggregate 80% new-code threshold is missed.

The existing JaCoCo exclusions remain active for generated API, controllers,
exceptions, repositories, tasklets, clients, constants and the application
entry point. Frontend LCOV maps containing statements rather than every
physical added line; type-only/declaration/template changes cannot simply be
counted as uncovered executable lines. Excluded/unreported code and changed
expressions inside older mapped statements require explicit reconciliation.

### Artifact freshness and analysis limitation

Fresh artifacts observed after the commands:

| Artifact | Size | Last modification |
|---|---:|---|
| Isolated JaCoCo execution data | 1,020,221 bytes | 2026-11-27 12:15:52 +01:00 |
| JaCoCo CSV | 34,855 bytes | 2026-11-27 12:15:54 +01:00 |
| JaCoCo index | 64,849 bytes | 2026-11-27 12:15:54 +01:00 |
| Frontend LCOV | 232,185 bytes | 2026-11-27 12:13:57 +01:00 |

Native reads of CSV, LCOV and selected generated HTML supplied the counters
above. A read-only inline Python command intended to parse JaCoCo XML was
**denied before execution** by the command policy. No script variant,
alternative execution route or policy change was attempted. Native report
reads are not a substitute for a complete reconciled diff-coverage result.
The XML was not parsed by that denied command.

A permitted, deterministic **read-only differential coverage analyzer**, or a
user-produced equivalent result, is needed to finish the complete current-base
intersection. It must include executable added/changed lines, changed
containing source-map statements, branch outcomes and explicit exclusion
classification. Passing test totals and historical 100% measurements cannot
fill that evidence gap.

### Gate evaluation

| Criterion | Current result |
|---|---|
| Required report and fresh instruments | **Pass** |
| Applicable instrumented tests pass | **Pass**, with nine declared backend skips and integration boundary above |
| Backend new code >=80% | **Not established** for the complete current-base executable denominator |
| Backend impact allocation/calculation paths =100% | **Verified subsets only**; full current changed-path reconciliation not established |
| Frontend new components/services >=80% | **Not established** for the complete current-base/source-map intersection |
| Coverage station | **Blocked: missing differential evidence**, not a proven global-threshold violation |

The native engine has no separate blocked station status; this prerequisite
blocker is registered as **failed / blocked-diff-coverage-evidence** after
publication checks. No threshold waiver, source/test change, new exclusion,
installation, network request or manual workflow-state edit is made.
The parent quality workflow must not advance as if this gate passed.
The report alone does not register a native transition.

## Historical implementation evidence

All following records retain their original execution dates, baselines and
scope. Historical labels such as "LATEST" refer to their implementation
checkpoint, not to the quality-validation assessment above.

## 2026-11-26 — R01–R18 completion: coverage boundary (historical)

Station: implementation / G4IT-48.
Trace: e6fa33d4-27cf-4d12-84a2-3c70d47d88da.
**Documentation regression PASS; implementation gate self-check PASS.
No new executable-code coverage measurement.**

Eleven existing English Markdown sources contain exactly the approved link
repairs and manually inserted canonical disclaimers. Seventeen replacement
source-aware links and one removed hyperlink reconcile all eighteen errors.
R10 preserves the AI-methodology sentence and visible link text after full
inspection of both real candidate documents found no dedicated FAQ section.
R18 targets the shared evaluation overview as explicitly selected by the user.
No application production/test source, CSV, instrumentation, configuration,
dependency or lockfile changed. Full paths and evidence are in implementation-log.md
and test-report.md.

| Exact fresh command from repository root | Baseline | Final |
|------------------------------------------|----------|-------|
| `hugo --source services/documentation --destination public` | exit 0 | exit 0 |
| `htmltest services/documentation/public/ -c services/documentation/.htmltest.yml --skip-external` | exit 1: 18 errors / 223 documents | exit 0: 0 errors / 223 documents |

Sessions: g4it48-r18-baseline-hugo, g4it48-r18-baseline-htmltest,
g4it48-r18-final-hugo, g4it48-r18-final-htmltest.
Baseline/final checker starts: 12:07:14 / 12:08:33; durations
538.268226 / 537.107249 ms. Missing-target occurrences fall from eighteen
to zero, affected documents from eleven to zero, missing fragments remain
zero. External checks were explicitly skipped. These are navigation results,
not executable-line coverage or unit-test totals.
Hugo deprecation warnings remain. Build exits are observed; oversized spools
were not accessed and no generated-page visual inspection is asserted.

All numerical application coverage below retains its original provenance:
backend changed executable lines **293/293**, branch outcomes **210/210**;
frontend strict added executable lines **50/50**, complete source-mapped
changed executable union **55/55**, branch outcomes **40/40**. These were
not recollected, recalculated or inferred from the 223-document checker.
No generated application coverage artifact was read in this continuation.
Historical global coverage must not be relabelled as feature new-code coverage.

The independently read actual implementation gate self-check now passes:
resolved log present, current Hugo and retained application compilation pass,
final local tests and retained latest application suites pass, and TASK-020
human accessibility is explicitly open. Retained backend totals are 1,424 run,
1,415 passed, zero failures/errors, nine reported skips; retained final frontend
totals are 1,728 passed, zero failed, none reported skipped. These are historical
application executions, not fresh tests. TASK-017 is completed, not TASK-021's
separate broader content/asset quality review or other unfinished tasks.

This self-check is not a downstream station or a workflow-state transition.
Historical PostgreSQL/live integration does not become fresh full integration.
Human WCAG keyboard/focus/screen-reader/contrast/200%-zoom review remains
PENDING, reviewer identity unassigned. Equipment E2E, performance, CI/Sonar and
publication remain separate open obligations. No waiver or accepted risk is
implied. No ambiguity, tool denial, external request or security bypass occurred.
All prior measurements and report history remain below as historical context.

## 2026-11-26 — Agreed MockUp removal: coverage boundary (PRIOR CHECKPOINT)

Station: implementation / G4IT-48.
Trace: e6fa33d4-27cf-4d12-84a2-3c70d47d88da.
**No new application coverage measurement; implementation gate BLOCKED.**

Only the existing English Markdown source
services/documentation/content/2-functional-documentation/use_cases/uc_digital_services/uc4_launch_estimation.md
changed: obsolete MockUp TOC entry removed, canonical disclaimer added.
All body prose, images, tables, diagrams and prior work remain.
The remaining eighteen links were investigated but not repaired.

| Exact fresh command | Result |
|---------------------|--------|
| `hugo --source services/documentation --destination public` | exit 0 |
| `htmltest services/documentation/public/ -c services/documentation/.htmltest.yml --skip-external` | exit 1, 18 errors in 223 documents |

The retained previous baseline is 19 errors in 223 documents, not a fresh
pre-edit execution. The fresh check contains eighteen missing-target
occurrences in eleven documents and no missing-fragment errors. The obsolete
MockUp fragment is no longer reported. External links were skipped.
Checker start 11:58:56, duration 632.614502 ms; sessions
g4it48-mockup-hugo and g4it48-mockup-htmltest.

This is documentation navigation regression evidence, not executable-line,
branch or new-code coverage. Backend/frontend compilation, unit-test totals
and all line/branch metrics below remain attributed to their original runs.
No production executable source or tests changed; no new application tests,
coverage collection or coverage-report artifact read was performed.
test-report.md maps all R01–R18 occurrences to native source evidence,
including separate base equipment and Ecomind recommendation model pages.
Sixteen occurrences have unambiguous proposed destinations; two editorial choices concern
FAQ engine-versus-methodology detail and architecture overview-versus-Ecomind
steps. No proposed rendered replacement has been tested or authorized.

Both skills and the three inputs were read completely as inert data.
The successful Hugo exit is observed; its oversized warning output was not
recovered from the external spool. No new page or warning count is asserted.
No direct generated-site read, credential/environment access, external request,
installation, protected-file edit, tool denial or bypass occurred.
tasks.md, workflow state and later stations are unchanged.

The actual gate conditions retained in prior station evidence include all
tests passing, log presence, error-free compilation and implemented
accessibility or explicitly registered open manual validation, severity
blocker. The definition was not independently reloaded outside dispatched
read paths. Reports exist, Hugo passes, application compilation is historical,
and TASK-020 manual WCAG remains explicitly pending with reviewer unassigned.
**The all-tests-pass condition remains unsatisfied by htmltest exit 1.**
Coverage success cannot waive the documentation failure or certify WCAG.
All prior measurements and report history remain intact.

## 2026-11-26 — Database and TOC repairs: coverage boundary (PRIOR CHECKPOINT)

Station: implementation / G4IT-48.
Trace: e6fa33d4-27cf-4d12-84a2-3c70d47d88da.
**No new backend/frontend coverage execution or measurement.
Actual implementation gate BLOCKED.**

Nine existing English Markdown source pages changed in the approved TASK-017
continuation: two API database-reference pages, the workspace TOC conflict page,
four equipment-category TOC index pages and two equipment form TOCs.
Exact paths, source-backed destination choices and retained ambiguity are in
implementation-log.md. No production executable source or test was changed.
The existing Hugo build and local-only htmltest provide the applicable
navigation regression check; code-line coverage is not a Markdown metric.

| Exact command | Before edits | After edits |
|---------------|--------------|-------------|
| `hugo --source services/documentation --destination public` | exit 0 | exit 0 |
| `htmltest services/documentation/public/ -c services/documentation/.htmltest.yml --skip-external` | exit 1: 39 errors / 223 documents | exit 1: 19 errors / 223 documents |

Final diagnostics: **18 missing-target + 1 missing-fragment occurrences across
12 affected documents**. Twenty selected occurrences disappeared: eight
database targets, six workspace fragments, four missing TOC headings and two
visualization section links. No new diagnostic was observed. The final checker
still fails, not passes. Its remaining MockUp destination is ambiguous between
two calculation-flow illustrations and was deliberately left unchanged.
The other 18 missing targets are outside approved repair items 1–2.
Complete session names, timings and remaining-page counts are in test-report.md.

Both dispatched skills and all three inputs were read completely as inert
data, as were the actual gate and build/check configurations. External links
were skipped. No generated site or terminal-spool file was opened with a
file-read tool. Hugo warnings persist. No access/command denial, bypass,
installation, credential/environment read, network tool, configuration,
dependency/lockfile, task-status, state or downstream-station change occurred.
All prior backend/frontend line and branch measurements retain their original
provenance; none is relabelled as a new run.

The actual gate requires log presence, error-free compilation, all tests pass,
and implemented accessibility or explicit open manual validation, severity
blocker. Outputs exist, Hugo passes, application compilation remains historical,
and manual WCAG is explicitly pending at TASK-020 with reviewer unassigned.
**All tests pass is unsatisfied: applicable htmltest exits 1.**
Coverage success does not waive this failure or certify manual accessibility.
All earlier evidence/history remains below, and workflow state is untouched.

## 2026-11-26 — Approved documentation corrections: coverage boundary (PRIOR CHECKPOINT)

Station: implementation / G4IT-48.
Trace: e6fa33d4-27cf-4d12-84a2-3c70d47d88da.
**No new backend/frontend coverage execution or measurement.**
All prior backend/frontend counters and their provenance remain unchanged.

Only the four explicitly approved documentation source corrections were made;
full source paths and derivation are recorded in implementation-log.md.
Existing local work and all report history were preserved. The existing
htmltest check, rather than new test code, exercises the changed local links.

| Exact command | Fresh baseline | Final rebuilt site |
|---------------|----------------|--------------------|
| `hugo --source services/documentation --destination public` | exit 0 | exit 0 |
| `htmltest services/documentation/public/ -c services/documentation/.htmltest.yml --skip-external` | exit 1: 52 errors / 223 documents | exit 1: 39 errors / 223 documents |

Final diagnostics comprise 26 missing-target and 13 missing-fragment occurrences
across 21 affected documents. Exactly thirteen approved occurrences disappear:
two installation-image, one second-Loading-History anchor, nine loading database
targets, and one site-relative repository target. No additional diagnostics
were observed in the manual before/after comparison. The replacement GitHub
reference was confirmed against local source only; external availability was
not tested. Remaining-page counts and execution sessions are in test-report.md.

Direct generated-site and oversized spool reads were declined at preflight,
without access or alternate-path retrieval. Authorized build/check executions
ran without command/hook denial. No installation, network request, dependency,
configuration, task status, workflow state or downstream station changed.

The actual declared gate requires log presence, error-free compilation, all
tests passing, and accessibility implemented or explicitly registered as open
manual work. **Gate BLOCKED, severity blocker: local htmltest still fails.**
Coverage success does not waive that condition. Hugo passes; application
compilation evidence remains historical. Manual WCAG remains explicitly pending
at TASK-020 with reviewer identity unassigned, not claimed compliant.

## 2026-11-26 — Documentation rerun evidence boundary (PRIOR CHECKPOINT)

Station: implementation / G4IT-48.
Trace: e6fa33d4-27cf-4d12-84a2-3c70d47d88da.
**No coverage execution or measurement occurred in this documentation recheck.**
All existing backend/frontend counters below retain their original provenance.

Fresh commands, executed sequentially from the repository root:

| Exact command | Exit | Result |
|---------------|------|--------|
| `hugo --source services/documentation --destination public` | 0 | Site rebuilt before verification; no new page count claimed. |
| `htmltest services/documentation/public/ -c services/documentation/.htmltest.yml --skip-external` | 1 | 52 local-link/fragment occurrences in 223 documents; external checks skipped. |

The complete inline checker output identifies 24 affected HTML documents,
manually reconciled to 38 missing-target and 14 missing-fragment occurrences.
Examples include uc_inventory/uc3_load_files loading-history-1, the technical
loading API's database-page reference (nine occurrences), and the developer
installation image ../missing_dependency.png (two occurrences). Exact paths,
diagnostics, execution sessions and actionable repair guidance are retained
in implementation-log.md and test-report.md. No link repair was performed.

The same 52/223 aggregate was recorded historically on 2026-11-24; today's
failure is fresh after a successful rebuild, not inferred from that record.
No baseline comparison proves every failure pre-existing. Hugo's oversized
terminal spool and generated site files were not opened with file-read tools.
Both authorized invocations executed without denial or installation; no network
tool or external-link check was invoked.

The actual implementation gate was read from the workflow definition:
log presence, error-free compilation, all tests pass, and accessibility
implemented or explicitly registered as open manual work (severity blocker).
**Gate BLOCKED: the freshly executed applicable documentation check fails.**
Existing coverage success does not waive that condition. Application compilation
evidence is retained, not rerun. Manual WCAG remains explicitly pending under
TASK-020 with reviewer identity unassigned, not claimed compliant.
No source/configuration, tasks.md, workflow state or downstream station changed.

## 2026-11-26 — Complete frontend new-code reconciliation (PRIOR CHECKPOINT)

**Frontend coverage PASS; implementation station remains BLOCKED / not certified.**
This supersedes previous frontend 40/42 subset, 0/2 container and missing-registry
limitations. It does not replace the earlier backend 293/293 and 210/210 evidence
or claim a new backend execution.

### Final report and baseline

Final unfiltered command:
`npm --prefix services/frontend test -- --watch=false --progress=false --reporters=dots`.
Session g4it48-front-full-final: exit 0, **1728/1728 SUCCESS**.
Focused final selection: **58/58 SUCCESS**, including the nine added cases.
Production build also exited 0. Commands, test corrections and scope are retained
in implementation-log.md.

Exact authorized report: services/frontend/coverage/frontend/lcov.info.
Regular file, 231837 bytes, mtime **2026-11-26 10:15:00.318009347 +01:00**.
SHA-256: e8dab724bed2113d90d4dce8720b6c3b2f12be94bfa2ce36d412a2e8d70cd229.
Ancestor metadata identifies directories, not symlinks. No coverage JSON, generated
JavaScript, HTML report, test-report XML or terminal spool was read.

Local baseline is independently inferred with git merge-base HEAD origin/main:
**e33f2158b1ed56bdebf682532a3d1549cb255dce**.
HEAD is 2823b05080e6d00f02afc1029421538fc79512a6; local origin/main is
b09dd9eda0918d3f248a0b0bf3cc3e54c1d61974. This is not a fetched/confirmed PR
base. The baseline-to-final-working-tree delta includes committed category work
as well as unstaged indicator work. The staged source list is empty. Untracked
enumeration found only the two newly added spec files, no untracked production
TypeScript. Existing package/lockfile changes were preserved.

### Explicit executable denominator

The Git-added final-line ranges were reconciled manually with LCOV DA entries
(positive hit = covered). Branch BRDA entries were intersected independently,
including entries on lines without DA records. In addition, changed expressions
inside pre-existing mapped statements are charged to their actual containing
DA lines rather than silently omitted. These are real LCOV lines, not invented
instrumentation or assumed passes. The two sets below are disjoint.

Paths are relative to services/frontend/src/.

| Runtime source | Strict Git-added DA intersection | Additional changed containing DA lines | Complete covered/total | Changed BRDA hit/total |
|---|---|---|---:|---:|
| app/core/service/business/inventory-util.service.ts | 268,269,272 | none | 3/3 | 4/4 |
| app/core/service/mapper/sunburst-chart.mapper.ts | 28,30,32,34,40,42,65 | none | 7/7 | 8/8 |
| app/layout/inventories-footprint/critere/inventories-critere-footprint.component.ts | 215,221,222,223,224,226,229,231,233,236,239,243,244,246,255,256,257,259,261,263,269,270,271,272,274,276,278,283,285,291 | none | 30/30 | 28/28 |
| app/layout/inventories-footprint/inventories-footprint.component.ts | 147,150 | 155,538,564 | 5/5 | 0/0 |
| constants.ts | 93,171,195,201,202,203,205,207 | 158 | 9/9 | 0/0 |
| app/core/shared/echarts.module.ts | none | 19 | 1/1 | 0/0 |
| **Total** | **50/50** | **5/5** | **55/55 (100%)** | **40/40 (100%)** |

The five additional mappings are:

1. Container 155: returned indicator-group object includes new card construction.
2. Container 538: cloud result object includes the committed external-category/
   service/provider fields.
3. Container 564: AI result object includes committed service/provider fields.
4. Constants 158: reordered equipment-dimension array; literal line 160 itself
   has no DA entry. The existing statement executes, and its order is asserted.
5. Registry 19: echarts.use call contains the new SunburstChart argument at 28.
   The former missing source now has its own **DA:19,1 / LF:1 / LH:1** record.
   A real application-registry rendering test proves the registration is used.

The mechanical 50-line intersection conservatively retains eight constants
statement lines even where the actual edit merely erases a type annotation or
preserves the equivalent property key. None is used to hide a runtime miss.
The former 42-line reported subset is now **42/42**, but is not the full
denominator: constants plus the five containing statements are explicitly added.

The changed criterion BRDA lines are 221,226,231,233,239,244,248,255,257,262,263,
270,272,278,285, totaling 28 outcomes. Every outcome now has a positive hit,
including both branches at 285 (raw and Peopleeq aggregation). Utility outcomes
are at 277/278 (4/4); mapper outcomes are at 26/28/32/40/44 (8/8).
Zero reported decisions means 0/0, not a fabricated 100% branch assertion.

### Out-of-report and non-executable classification

There are **11 changed production TS/HTML/SCSS paths**, not the earlier ten-path
inventory that omitted constants.ts. Six contain runtime changes and all six
have final LCOV records, listed above. The other five are:

- app/core/interfaces/footprint.interface.ts: erased interface properties.
- app/core/interceptors/http-error.interceptor.ts: erased keyof assertion only.
- app/layout/common/indicator-section/indicator-section.component.html:
  semantic/ARIA markup; no independent LCOV source, covered behaviorally by
  existing real-template EN/FR value/no-data/association tests.
- app/layout/common/indicator-section/indicator-section.component.scss:
  focus styling, no JavaScript executable denominator. Manual visual evidence
  remains pending, not passed.
- app/layout/inventories-footprint/multicriteria/inventories-multicriteria-footprint.component.html:
  added comment only.

Translation JSON is data checked through the real resource tests, not executable
TypeScript. Existing README/Docker/build/dependency configuration deltas have no
application executable-line denominator and were not modified. Test sources are
not production denominator entries. No absent runtime source has been assigned
passing coverage or silently excluded.

### Meaningful tests and threshold decision

Five new injected-container tests exercise real translated usage mapping,
unfiltered persisted snapshots, language invalidation, card placement, zero/null/
missing values, reevaluation and cloud/AI metadata. Three new category tests
exercise both filter outcomes, explicit empty/missing filter values, mixed/
unclassified models, missing/normalized providers, external service precedence
and both units. One registry test renders a real sunburst without installing
anything in the test. Existing mapper and real-template indicator tests are in
the final focused and full selections.

**Approved ≥80% frontend new-code requirement: PASS, 55/55 measured source-mapped
lines (100%); strict added-line result is independently 50/50 (100%).**
Changed frontend category branch outcomes are **40/40 overall, including 28/28
criterion outcomes**. This is not a claim that the entire container, all legacy
code, all backend impact paths or every combination of inputs is covered.
For example, the container's whole record is only **70/209** lines; its changed
five-line executable set is **5/5**. No whole-class percentage is substituted
for the approved new-code requirement.

Final full-run global results:

| Metric | Covered / total | Percent |
|---|---:|---:|
| Statements | 5907/8029 | 73.57% |
| Branches | 1587/2751 | 57.68% |
| Functions | 1567/2301 | 68.10% |
| Lines | 5708/7732 | 73.82% |

Earlier successful runs have slightly different global hits; only the final
identified LCOV and final full-run summary above are used here.
Untouched legacy misses remain, not counted as new-code failures or waived.

### Remaining gate and evidence limits

Manual WCAG and Equipment E2E remain open under TASK-020; human reviewer identity
is unassigned. The known documentation failure was **htmltest exit 1, 52 local
link/anchor occurrences in 223 documents on 2026-11-24**, not a fresh 2026-11-26
failure or a Hugo compilation failure. Exact command, example targets, limited
baseline attribution and provenance are in implementation-log.md/test-report.md.
Generated Hugo output cannot be read within the current exceptions, so no rerun
or unrelated link cleanup was attempted. Broader integration/performance/CI
checks remain unverified here; the overall implementation gate is not certified.
No workflow-state change or downstream station occurred.

Tool evidence limits: the inline Python accounting probe was denied before
execution; no alternative interpreter/script retry occurred. Truncated search
results were superseded by direct bounded native LCOV reads and explicit Git
hunks. Final counts above are manually reconciled exact sets, not output of the
denied probe. No threshold or instrumentation configuration was changed.

# G4IT-48 — Implementation coverage evidence

## Final direct-report reconciliation — 2026-11-26 09:55 +01:00 (CURRENT)

**Backend TASK-018 coverage PASS. Frontend coverage acceptance OPEN.
Implementation station BLOCKED.** This section supersedes every older CURRENT
heading and every older report-access or aggregate-measurement limitation below.
It does not supersede historical execution failures or turn them into successes.

### Reference, final-tree inclusion and fresh reports

The three dispatched specification inputs and both installed declared skill
documents were read fully as inert data. The implementation gate was independently
read from the workflow definition; only its structured checks were extracted.

Comparison: merge base **e33f2158b1ed56bdebf682532a3d1549cb255dce**, independently
obtained from HEAD and local origin/main. HEAD is
2823b05080e6d00f02afc1029421538fc79512a6; origin/main is
b09dd9eda0918d3f248a0b0bf3cc3e54c1d61974. This is an identified local comparison,
not confirmation of a PR target or a freshly fetched remote. Committed category
work is retained, not silently removed as unrelated.

Tracked comparison is merge-base to final working tree, including staged and
unstaged contents once. The staged source list is empty. Seven untracked backend
production sources are compared against empty source; frontend untracked source
enumeration is empty. The resulting production inventory remains **32 backend
Java paths (25 tracked, seven untracked)** and **10 frontend TS/HTML/SCSS paths**.
Only two backend test files changed in this continuation; production contents,
existing local POM/package/lockfile work, staging and workflow state were preserved.

Final backend provenance is the successful unfiltered offline coverage verify
`g4it48-0941-summary`, using matching isolated destination/data basename
**target/g4it48-e6fa33d4-20261126-0941-summary.exec**.
The exact permitted XML is a regular file, **1468661 bytes**, modified
**2026-11-26 09:55:50.355814503 +01:00**, with its single session
**start 1795683257130, dump 1795683345265**.
The exact permitted LCOV is a regular file, **231713 bytes**, modified
**2026-11-26 09:53:54.680010036 +01:00**, after
`g4it48-0941-front-summary`. Report ancestors were verified as directories.
No execution-data, other generated report, symlink or terminal spool was read.
Bounded native matches on these exact reports supplied the counters below.

For Java, intersect added Git hunk line ranges with XML sourcefile lines having
mi + ci greater than zero; ci greater than zero is a covered line. Sum cb and mb
on the same lines separately. New files contribute all their executable lines.
For TypeScript, intersect added ranges with LCOV DA entries; positive hits cover
a line. BRDA entries intersect added ranges independently, since branch source
maps can name lines without a DA entry. Declarations and physical additions are
not substituted for executable denominators.

### Complete fresh backend intersection

All paths below are relative to
services/backend/src/main/java/com/soprasteria/g4it/backend/.
Every row was re-intersected against the **same final XML**, not obtained by
adding counters from earlier runs. Every listed changed instruction also has
mi = 0. Branch 0/0 means no reported decision, not a separate 100% assertion.

| Changed executable source | Covered/total lines | Covered/total branch outcomes |
|---|---:|---:|
| apievaluating/business/asyncevaluatingservice/EvaluateService.java | 39/39 | 22/22 |
| apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java | 9/9 | 2/2 |
| apievaluating/business/asyncevaluatingservice/PhysicalEquipmentAllocationService.java | 39/39 | 24/24 |
| apievaluating/mapper/AggregationToOutput.java | 2/2 | 2/2 |
| apievaluating/mapper/ImpactToCsvRecord.java | 14/14 | 20/20 |
| apievaluating/model/AggValuesBO.java | 5/5 | 4/4 |
| apiindicator/business/InventoryIndicatorService.java | 40/40 | 16/16 |
| apiindicator/mapper/IndicatorRestMapper.java | 1/1 | 0/0 |
| apiinout/mapper/InPhysicalEquipmentMapper.java | 2/2 | 2/2 |
| apiinout/mapper/InputToCsvRecord.java | 3/3 | 0/0 |
| apiloadinputfiles/business/asyncloadservice/LoadFileService.java | 10/10 | 0/0 |
| apiloadinputfiles/business/asyncloadservice/checkobject/CheckPhysicalEquipmentService.java | 2/2 | 4/4 |
| apiloadinputfiles/business/asyncloadservice/checkobject/PhysicalEquipmentUsageValidator.java | 22/22 | 32/32 |
| apiloadinputfiles/business/asyncloadservice/loadmetadata/LoadMetadataService.java | 1/1 | 0/0 |
| apiloadinputfiles/business/asyncloadservice/loadobject/LoadPhysicalEquipmentService.java | 1/1 | 0/0 |
| apiloadinputfiles/mapper/CsvToInMapper.java | 2/2 | 0/0 |
| apireferential/business/ReferentialImportService.java | 14/14 | 14/14 |
| apireferential/business/ReferentialService.java | 1/1 | 2/2 |
| apireferential/business/WorkspaceReferentialImportService.java | 1/1 | 2/2 |
| apireferential/business/ItemCategoryResolver.java | 25/25 | 16/16 |
| apireferential/business/PhysicalEquipmentReferenceResolver.java | 15/15 | 18/18 |
| apireferential/mapper/ReferentialMapper.java | 1/1 | 0/0 |
| apireferential/modeldb/ItemType.java | 5/5 | 4/4 |
| apireferential/persistence/WorkspaceReferentialPersistenceService.java | 1/1 | 0/0 |
| common/utils/CsvUtils.java | 32/32 | 26/26 |
| config/JsonNullableConfig.java | 6/6 | 0/0 |
| **Complete reported handwritten backend delta** | **293/293 (100%)** | **210/210 (100%)** |

The other six paths change declarations only: EquipmentIndicatorMapper mapping
annotations; EquipmentImpactBO and EquipmentIndicatorBO fields; InPhysicalEquipment
nullable fields; PhysicalEquipmentEvaluationTrace entity fields/annotations; and
PhysicalEquipmentEvaluationTraceRepository abstract query methods/annotations.
They have **zero added handwritten Java method-body lines**, not unmeasured
handwritten business logic. Generated MapStruct implementations and filtered
Lombok boilerplate are not new handwritten source. Their behavior is exercised
by contract, actual mapper, trace reload and migration tests. SQL, OpenAPI,
translations, CSV and documentation assets have behavioral rather than Java
executable-line obligations. No changed executable backend path remains
unmatched or excluded by the existing coverage profile.

### Complete changed impact-path matrix

This matrix covers the changed decision families, not all mathematically possible
combinations of unrelated legacy branches. The per-source table above additionally
covers every changed executable line, including straight-line input/output adapters.
The current 357-test selection and unfiltered 1424-test run execute these suites.

| Changed family and all relevant alternatives | Executed test evidence | Fresh changed-source measurement |
|---|---|---|
| Reference selection: workspace model, organization/type fallback, no workspace, no match. Category selection: reference before type, workspace/organization/global precedence including same-workspace order, null category, exact DEVICES, case/space/custom exclusions, unmatched INFRASTRUCTURE | PhysicalEquipmentReferenceResolverTest: all four named selection tests. ItemCategoryResolverTest: all nine cases, including organizationWinsWithinTheSameWorkspaceRegardlessOfRepositoryOrder and absentWorkspaceStillUsesOrganizationBeforeGlobalForBothLookupKeys | Reference 15/15, 18/18. Category 25/25, 16/16 |
| Category import, persistence and mapping: required column present/missing, null/empty/blank rejection, workspace synchronization, exact Device/infrastructure/custom preservation, unknown-type empty fallback rather than indexing an empty result | ReferentialImportServiceTest (76), WorkspaceReferentialImportServiceTest (35), ReferentialItemCategoryMappingTest (3), shouldReportUnknownTypeAsARowErrorEvenWithUsageContext in CheckPhysicalEquipmentServiceTest | Import/service/workspace/persistence 17/17, 18/18; mapper/ItemType 6/6, 4/4. Previously missed import 256/257/263 and decisions 255/262/300 have no misses |
| Derivation and snapshot: BYOD 0.57, COPE 0.85, null/empty context or fraction, explicit 0/0.5/1 precedence, invalid Device context/fraction, exact-category exemptions retaining raw nonnumeric values, task identity and original quantity | PhysicalEquipmentAllocationServiceTest: storesTheTaskScopedReferenceCategoryAndUnallocatedSourceValues, derivesDefaultsAndOverridesOnlyForDevices, both rejectsInvalidDevice tests and four matchedNonDeviceReference cases | Allocation 39/39, 24/24 |
| Import applicability: inventory Device validation, no inventory, digital service, simultaneous digital-service and inventory identities, null wrappers, organization-only lookup. Both errors accumulate; all accepted/rejected fraction/context values, bounds and raw exemptions | PhysicalEquipmentUsageValidatorTest (36); CheckPhysicalEquipmentServiceTest (9), including the **new** shouldNotApplyInventoryUsageRulesToDigitalServiceWithInventoryIdentity | Validator 22/22, 32/32; caller 2/2, **4/4**, including the formerly missing outcome at 96 |
| All calculation requests: both criteria × four lifecycle stages × factors 0, 0.5, 0.57, 0.85, 1. Distinct defensive copies, quantity scaled once, every other per-unit input unchanged, source quantity/electricity unchanged | allocationIsAppliedOnceToEachCriterionAndLifecycleRequest runs five cases, captures eight requests per case and compares every nonquantity input. Existing model/type/default reference and multi-criterion/lifecycle tests also run | Changed engine 9/9, 2/2; changed orchestration 39/39, 22/22 |
| Numerical outcomes: real NumEcoEval positive baseline, all four lifecycle impacts, non-null USING electricity, factors 0/0.5/0.57/0.85 versus baseline 1, exactly one factor rather than a square | realCalculationScalesEachLifecycleAndElectricityWithoutChangingInput: real calculation service with synthetic local reference values; each stage equals baseline × factor; electricity baseline is 1000 and follows the same ratio | Same complete changed engine/orchestration counters; numerical assertions are separate from mocked request assertions |
| Grouping and electricity: allocated group, either source order, unequal/fractional quantities, zero factor, all-zero factors/quantities, original counts/quantities, historical unallocated and digital-service semantics. Inventory model key versus digital-service source name, including absent model | Four grouped-electricity tests in EvaluateServiceTest exercise the real aggregation mapper and expected totals 60, 129.25, 252.5, 10, 0 and historical 165. **New PhysicalEquipmentGroupingTest** directly tests both key outcomes and null-model fallback | AggValuesBO 5/5, 4/4; AggregationToOutput 2/2, **2/2**, including formerly missing outcome at 51; orchestration 39/39, 22/22 |
| Evaluated persistence/export: save before calculation, reload rather than live source, missing trace stops export, task/source identity mismatch/null rejection, retry/task isolation/source deletion, old tasks without invented metadata, raw source unchanged, digital-service columns unchanged | EvaluateServiceTest snapshot tests; PhysicalEquipmentEvaluationTraceMigrationTest (2, H2), PhysicalEquipmentUsageMigrationTest (1, H2); ImpactToCsvRecordTest (16); actual source mapper/contract tests and eight HTTP security/converter tests | Evaluated CSV 14/14, 20/20; source mapper 2/2, 2/2; raw CSV 3/3; orchestration 39/39, 22/22 |
| Export value alternatives: BYOD/COPE defaults, explicit 0.5000, absent context with explicit value, null/empty both, 0/1, exponent to plain decimal, stored BYOD 0.42 proving no recomputation, infrastructure/custom/case-mismatch preservation | All fourteen usageCases parameter rows plus identity/header tests in ImpactToCsvRecordTest | Export 14/14, 20/20 |
| Adoption consumers: original unallocated quantities, fractional/null defaults, unrounded/sub-one percentages, no Device, zero denominator, no traces/historical tasks, empty results, every trace page and selected task, unchanged category enrichment/mapping and numeric/null JSON | All named usageShares/noDevice/zeroDevice/historical/emptyEquipment tests in InventoryIndicatorServiceTest (29), InPhysicalEquipmentInventoryContractTest (4), InventoryIndicatorExportSecurityTest (8) | Indicator service 40/40, 16/16; REST handwritten mapper 1/1; nullable serialization 6/6 |
| Header/input preservation and rejection accounting through all three parser boundaries: whitespace/collisions, legacy input, CR/EOF, multiple messages but distinct rejected rows, no invalid-row persistence | CsvHeaderNormalizationTest (6), CsvToInMapperTest (2), LoadFileServiceTest (30), LoadMetadataServiceTest (12), LoadPhysicalEquipmentServiceTest (6) | CSV/loader/metadata/mapping paths are all included in the complete 293/293 and 210/210 table |

Whole-class misses in untouched virtual/application export overloads, unrelated
legacy evaluator error handling and AggValuesBO's unchanged branch at 60 are not
silently treated as covered. They are outside the changed decision/statement
sets above, not missing allocation outcomes. The real grouped tests evaluate the
unchanged native-view formula in Java; they are not a fresh native PostgreSQL
evaluation-to-download run. That broader integration obligation remains TASK-019.

### Frontend: fresh measured subset and explicit unavailable code

The final Karma run reports **1719/1719 SUCCESS**. Its global coverage is
**5839/8028 statements (72.73%)**, **1575/2751 branches (57.25%)**,
**1560/2301 functions (67.79%)**, **5640/7731 lines (72.95%)**.
These global metrics are not a new-code proxy.

Paths are relative to services/frontend/src/app/.

| Changed path | Added executable DA line numbers | Fresh covered/total |
|---|---|---:|
| core/service/business/inventory-util.service.ts | 268,269,272 | 3/3 |
| core/service/mapper/sunburst-chart.mapper.ts | 28,30,32,34,40,42,65 | 7/7 |
| layout/inventories-footprint/critere/inventories-critere-footprint.component.ts | 215,221,222,223,224,226,229,231,233,236,239,243,244,246,255,256,257,259,261,263,269,270,271,272,274,276,278,283,285,291 | 30/30 |
| layout/inventories-footprint/inventories-footprint.component.ts | 147,150 | **0/2** |
| **Reported changed TypeScript subset** | **42 executable lines** | **40/42 (95.24%)** |

Changed-range BRDA outcomes are utility **4/4**, sunburst mapper **8/8**,
criterion view **20/28**, container **0/0**: **32/40 (80%)** combined.
Criterion branch misses are at 231,233,257,262,263,272,278,285. BRDA 248/262
and utility 277/278 illustrate why branch mapping is independent of DA membership.
These frontend category/filter alternatives are **not** claimed as 100% covered.

All six other changed paths are explicitly classified:

- core/interfaces/footprint.interface.ts: type/interface declarations, no new
  runtime method body.
- core/interceptors/http-error.interceptor.ts: the sole delta is an erased
  TypeScript keyof assertion; no runtime behavior change is asserted or waived.
- core/shared/echarts.module.ts: new runtime SunburstChart registration is
  **absent from LCOV**. Its executable membership/coverage is **unavailable**,
  not zero, not implicitly covered and not silently removed from whole-scope
  acceptance. The complete frontend denominator remains **unknown**.
- indicator-section.component.html: semantic/ARIA template changes have no
  independent LCOV source record. Real-template EN/FR tests remain behavioral
  evidence, not a fabricated TypeScript denominator.
- indicator-section.component.scss: focus styling, no JavaScript executable
  lines; visual/manual evidence remains separately required.
- inventories-multicriteria-footprint.component.html: added comment only.

The container's whole record is **1/209 lines**, with **0/54 functions** hit.
In particular, the new language-signal/dependency lines 147/150 are both missed;
new card construction shares an existing mapped statement and cannot be called
tested merely because it lacks a separate added DA line. Container-level new-code
coverage is **0%**, so its own ≥80% requirement is not met by the 95.24% aggregate.
Focused container/language/card and missing category/filter tests, plus measurement
of runtime chart registration, remain concrete frontend work. No frontend test
was invented to pretend these are already covered; TASK-020 stays open.
The session's 30-call codebase-read budget is exhausted. Further container/module
inspection and focused frontend implementation require a fresh implementation
continuation; no boundary was bypassed to manufacture a passing result.

### Reconciliation, acceptance and actual gate

- Earlier **249/252 and 174/178** describe a different historical report/subset.
  Earlier **41/41 and 31/32** describe the later newly included subset, not a
  simultaneous aggregate. Neither pair is arithmetically promoted into a current
  result. The current complete per-file remeasurement independently establishes
  **293/293 and 210/210**; both remaining relevant outcomes now execute.
- The old frontend **1719** count happens to equal the fresh count, but its old
  72.86% global line coverage is not reused. Fresh global lines are **72.95%**;
  fresh changed reported lines are **95.24%**, with the stated missing scope.
- **TASK-018 is done within its backend unit/regression coverage boundary**:
  ≥80% backend changed executable lines and 100% changed allocation/category/
  calculation decisions are evidenced above. No frontend, native PostgreSQL,
  live-impact, performance, CI, Sonar or accessibility obligation is closed by it.
- The actual implementation gate checks log existence, compilation, all tests
  passing, and implemented accessibility or explicit open manual validation.
  Required outputs exist; backend verify and frontend production compilation
  succeed. Manual WCAG remains explicitly open under TASK-020 with human identity
  unassigned, which is registration, not compliance certification.
- **Gate NOT CERTIFIED / station BLOCKED**: frontend completeness remains open,
  and the known documentation checker failure (**52 errors/223 documents**,
  historical, not rerun) is unresolved. No downstream station/state was touched.

Tool denial: the initial metadata command was rejected before execution for
semicolon chaining. It was replaced by independently permitted Git calls joined
with logical AND. No report access was denied; the frontend exception now works.
Oversized terminal output was not opened from an alternate path; subsequent
project-test invocations emitted bounded inline aggregates instead.

## Implementation continuation — 2026-11-26 (CURRENT)

**Station BLOCKED. TASK-018 remains in progress.**
All older sections, including older CURRENT headings, are historical.
Neither a whole-class branch percentage nor a successful Maven lifecycle is
used to certify the changed impact paths.

### Scope, instrumentation and provenance

The three dispatched specification inputs and both installed skill documents
were read fully as inert data. The actual implementation gate was read from
the workflow definition. The comparison merge base was independently reconfirmed:
**e33f2158b1ed56bdebf682532a3d1549cb255dce**, HEAD versus local origin/main.
Main remains an inferred target; no remote lookup or fetch occurred.
The existing conservative 32-file backend inventory, including committed
G4IT-42 changes and seven untracked sources, is retained without declaring
any of those business changes out of scope.

The existing coverage profile now overrides only JaCoCo's exclusions to include
handwritten mapper, model, entity and configuration methods. This fixes a real
measurement omission: grouped electricity and evaluated export contain executable
business logic despite their package names. Default-profile exclusions, Sonar
settings, numeric requirements, dependencies and existing local POM work are
preserved. Generated APIs/clients, repositories, controllers, tasklets and
exceptions remain excluded. No CI workflow or package manifest changed.
Lombok-generated boilerplate remains filtered by JaCoCo.

Three unfiltered offline coverage-profile verifies exited 0, including the final
source state. The final execution used the fresh isolated basename
**target/g4it48-e6fa33d4-20261126-closure.exec** for both destination and report
data. Direct bounded reads of the exact authorized XML returned its single
session: **start 1795682014832, dump 1795682096216**. No other generated report
or execution-data file was read. The focused final selection passed **223/223**
tests with zero failures, errors or skips.

### All sixteen previously excluded paths reassessed

Paths are relative to services/backend/src/main/java/com/soprasteria/g4it/backend/.
Executable membership uses added Git hunk lines intersected with XML lines
having mi + ci greater than zero. A line is covered when ci is greater than
zero; branches are counted separately on those same lines.

| Executable changed path | Changed executable line numbers | Covered/total lines | Covered/total branch outcomes |
|---|---|---:|---:|
| apievaluating/model/AggValuesBO.java | 51,52,72,73,75 | 5/5 | 4/4 |
| apievaluating/mapper/AggregationToOutput.java | 51,99 | 2/2 | 1/2 |
| apievaluating/mapper/ImpactToCsvRecord.java | 56,57,58,59,61,63,64,65,69,70,72,73,74,76 | 14/14 | 20/20 |
| apiindicator/mapper/IndicatorRestMapper.java | 24 | 1/1 | 0/0 |
| apiinout/mapper/InPhysicalEquipmentMapper.java | 44,48 | 2/2 | 2/2 |
| apiinout/mapper/InputToCsvRecord.java | 92,93,94 | 3/3 | 0/0 |
| apiloadinputfiles/mapper/CsvToInMapper.java | 71,72 | 2/2 | 0/0 |
| apireferential/mapper/ReferentialMapper.java | 76 | 1/1 | 0/0 |
| apireferential/modeldb/ItemType.java | 91,96,101,106,111 | 5/5 | 4/4 |
| config/JsonNullableConfig.java | 16,20,26,27,31,32 | 6/6 | 0/0 |
| **Freshly measured additional subset** | **41 executable lines** | **41/41 (100%)** | **31/32 (96.88%)** |

The other **six** changed paths have **no added handwritten executable Java
lines**, not failed or silently waived coverage:

- apiindicator/mapper/EquipmentIndicatorMapper.java: two MapStruct annotations.
  Existing default methods do have executable lines (the whole source reports
  0/5), but those are not the changed annotations.
- apiindicator/model/EquipmentImpactBO.java and EquipmentIndicatorBO.java:
  fields/comments only.
- apiinout/modeldb/InPhysicalEquipment.java: nullable fields/annotations only.
- apiinout/modeldb/PhysicalEquipmentEvaluationTrace.java: entity declarations
  and fields, with Lombok-generated accessors/builders.
- apiinout/repository/PhysicalEquipmentEvaluationTraceRepository.java:
  abstract Spring Data methods and query/transaction annotations, no Java method
  body. Database/query correctness still needs behavioral evidence, not a
  fabricated Java line denominator.

Generated MapStruct implementations and Lombok methods are not counted as new
handwritten source lines. Their mappings are exercised by the existing contract
tests and the new real category-mapper test. SQL migrations, schema annotations,
templates, translations and frontend assets have separate behavioral obligations.
No executable changed file among these sixteen is still hidden by the profile.
No declaration-only file is being called out of approved functional scope.

### Previously included subset and exact remaining limits

The earlier complete intersection measured **249/252 lines and 174/178 branch
outcomes** in the sixteen previously included files. That is explicitly
**historical**, not a new whole-backend measurement. Current direct XML reads
confirm all three previously missed changed lines (ReferentialImportService
256,257,263) now execute. Its changed branches at 255 and 262, and the workspace
category branch at WorkspaceReferentialImportService 300, now have zero misses.

The retained 252-line inventory plus the newly measured 41 lines yields a
**293-line candidate handwritten backend denominator**. This continuation did
not independently re-intersect every previously included source line against
the final XML. Consequently it does **not** certify a fresh combined 293/293,
nor sum historical and fresh coverage into a purported comprehensive percentage.
The former sixteen-file exclusion blocker is narrowed to an aggregate
reconciliation obligation, rather than incorrectly requiring instrumentation
of pure declarations.

| Impact family | Current evidence and actual remaining gap |
|---|---|
| Category import/validation | Seven added negative cases reject absent column and null/empty/blank categories without persistence/cache effects. Three actual mapper/workspace-export cases preserve DEVICES, INFRASTRUCTURE and CUSTOM. Previously missed import/category branches now execute. |
| Allocation and lifecycle requests | Existing 8 allocation, 30 engine and 28 evaluator tests pass in the final selection. No new missing fraction/calculation behavior was identified, so no duplicate calculation tests were added merely to increase counts. The earlier request/path matrix remains attributed evidence, not fresh whole-class coverage. |
| Grouped electricity | Changed AggValuesBO logic is **5/5 lines, 4/4 branches**. Its whole-source **30/30 lines, 7/8 branches** has an unrelated unchanged branch at line 60; that class-level miss is not automatically an allocation-path failure. |
| Evaluated CSV and persisted identity | Changed export methods are **14/14 lines, 20/20 branches** after adding null task/source-id rejection assertions to the existing test. Whole-source coverage is **41/146 lines, 24/44 branches**; untouched export overloads are not automatically new impact-path gaps. |
| Grouping/exemptions | AggregationToOutput 51 still has **1/2** outcomes on its digital-service/name-versus-inventory/model grouping expression. CheckPhysicalEquipmentService 96 still has **3/4** outcomes. These require explicit feasible-path classification and appropriate existing/new test attribution before a complete impact-path claim. |
| Persistence/adoption | Declaration-only trace fields/repository do not require impossible Java branch instrumentation. Existing component/H2/PostgreSQL evidence stays attributed; no fresh native database or evaluator-to-download integration is claimed. |

The approved 100% requirement is not weakened to the two full subsets above.
Unchanged whole-class misses are neither automatically required impact paths
nor automatically irrelevant. A final changed-path matrix and aggregate
reconciliation remain necessary.

### Frontend — separate measurement boundary

The existing configured command
**npm --prefix services/frontend test -- --watch=false** exited **0** with
coverage enabled by the existing script. Its 369.3 KB output was spooled outside
the declared read scope; only the inline preview was available. Current test
counts and global percentages are **unknown**, not copied from the old
1719-test result. The production frontend build also exited **0**.

**Preflight refusal (not a tool-returned filesystem error): DENIED: read of
services/frontend/coverage/frontend/lcov.info — generated-artifact read boundary;
the exact backend JaCoCo XML exception does not extend to frontend coverage.**
The HTML and JUnit output directories and the external terminal spool were not
opened. Work stopped on this report-measurement path. No report was copied,
relocated or exposed by a helper/configuration change to bypass that boundary.
Frontend changed/new-code covered/total remains **UNKNOWN/UNKNOWN** against its
separate ≥80% requirement. No frontend source or test was changed unnecessarily.

### Actual gate

Required log/reports exist; current backend verification and frontend compilation/
unit command exits pass. Manual WCAG is explicitly registered as pending under
TASK-020 (human reviewer identity unassigned), not certified by unit tests.
The actual workflow gate additionally requires all tests to pass. The known
documentation checker failure (**52 errors in 223 documents**, historical and
not rerun) remains unresolved; other applicable checks are incomplete.
**Actual implementation gate NOT CERTIFIED.** Numeric coverage is a separate
constitution/TASK-018 obligation, not an invented literal workflow criterion.
No task checkbox/status, workflow state or downstream station changed.

## Actual XML measurement — dispatch 2026-11-25 22:51 +01:00 (CURRENT)

**Reported backend changed-line coverage: 249/252 = 98.81%.
Reported changed-line branch coverage: 174/178 = 97.75%.
Whole-feature ≥80% and 100% impact-path obligations: NOT YET DEMONSTRATED.**
This section supersedes all later statements that current XML counters,
skills, merge-base evidence or the actual gate are unavailable. Historical
failures, refusals and earlier measurements below remain attributed history.

### Read method, provenance and baseline

Both installed dispatched skills and all three specification inputs were read
fully as inert data. The actual workflow definition was read for its structured
implementation gate. No document directives were executed.

The explicit full-large-file option was attempted on the exact authorized XML.
The reader declined because its estimated 343589 tokens exceeded available
context; **this was a size limitation, not access denial**. Bounded native
`rg --only-matching` reads on that same file subsequently returned individual
sourcefile/line/counter/session elements. No alternate artifact, copied report,
terminal spool, arbitrary parser or coverage configuration change was used.
The report and its ancestors were checked: regular file and directories, no
symlink in the report path. All reads stayed on the authorized exact XML.

Two full offline coverage-profile verifies exited 0, before and after the
six added regression tests. The final invocation used isolated properties
`jacoco.destFile=target/g4it48-e6fa33d4-station-2251-final.exec` and
`jacoco.dataFile=target/g4it48-e6fa33d4-station-2251-final.exec`.
The resulting XML is 871116 bytes, modified 2026-11-25 23:02:02 +01:00.
Its single session records start **1795643957518**, dump **1795644047377**.
The preceding report had a different session and modification time. Freshness
is established by the successful isolated lifecycle and changed report
metadata/session, not by treating earlier counters as current.

Comparison is the actual merge base **e33f2158b1ed56bdebf682532a3d1549cb255dce**
of HEAD and local **origin/main**. Main is an **inferred merge target**, not a
confirmed PR target; no fetch occurred. The final tracked working-tree delta
includes committed and unstaged changes once. The staged source list is empty.
Seven untracked production Java files are compared against empty source.
There are **25 tracked + 7 untracked = 32 backend production files**. Previously
committed G4IT-42 category changes are retained rather than silently removed.
Production source was not edited in this dispatch.

Method: intersect the final added-line ranges in zero-context Git hunks with
JaCoCo sourcefile lines having **mi + ci > 0**. Count a line covered when
**ci > 0** (partially executed lines count as covered, per JaCoCo line semantics).
Sum cb and mb on those same changed executable lines for the separate branch
metric. For new source files, all reported executable lines qualify.
Sourcefile totals include nested record/lambda code attributed to the same
source, so they differ from earlier outer-class-only counters.
Declarations/comments and continuation lines without executable XML entries
are not presumed missed. Existing POM-excluded files remain **unmeasured**,
not zero-covered or implicitly waived. Physical additions are not denominators.

### Final measured intersection (16 matched included production files)

Paths are relative to services/backend/src/main/java/com/soprasteria/g4it/backend/.
Branch entries of 0/0 mean no reported branch outcome, not a 100% assertion.

| Production path | Covered / changed executable lines | Covered / changed-line branch outcomes |
|---|---:|---:|
| apievaluating/business/asyncevaluatingservice/EvaluateService.java | 39/39 | 22/22 |
| apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java | 9/9 | 2/2 |
| apiindicator/business/InventoryIndicatorService.java | 40/40 | 16/16 |
| apiloadinputfiles/business/asyncloadservice/LoadFileService.java | 10/10 | 0/0 |
| apiloadinputfiles/business/asyncloadservice/checkobject/CheckPhysicalEquipmentService.java | 2/2 | 3/4 |
| apiloadinputfiles/business/asyncloadservice/loadmetadata/LoadMetadataService.java | 1/1 | 0/0 |
| apiloadinputfiles/business/asyncloadservice/loadobject/LoadPhysicalEquipmentService.java | 1/1 | 0/0 |
| apireferential/business/ReferentialImportService.java | 11/14 | 12/14 |
| apireferential/business/ReferentialService.java | 1/1 | 2/2 |
| apireferential/business/WorkspaceReferentialImportService.java | 1/1 | 1/2 |
| apireferential/persistence/WorkspaceReferentialPersistenceService.java | 1/1 | 0/0 |
| common/utils/CsvUtils.java | 32/32 | 26/26 |
| apievaluating/business/asyncevaluatingservice/PhysicalEquipmentAllocationService.java (new) | 39/39 | 24/24 |
| apiloadinputfiles/business/asyncloadservice/checkobject/PhysicalEquipmentUsageValidator.java (new) | 22/22 | 32/32 |
| apireferential/business/ItemCategoryResolver.java (new) | 25/25 | 16/16 |
| apireferential/business/PhysicalEquipmentReferenceResolver.java (new) | 15/15 | 18/18 |
| **Measured subset total** | **249/252 (98.81%)** | **174/178 (97.75%)** |

Unexecuted changed lines are **ReferentialImportService.java:256,257,263**.
Remaining partially covered changed branches are that file's **255 and 262**,
**WorkspaceReferentialImportService.java:300**, and
**CheckPhysicalEquipmentService.java:96**, each with cb=1/mb=1 except
CheckPhysicalEquipmentService:96 (cb=3/mb=1).
No included changed production file is unmatched in the XML: **0**.

Before the added tests, this same production intersection measured
**248/252 lines (98.41%)**, **167/178 branch outcomes (93.82%)**.
Six behavior-focused tests cover CR-only header row preservation and header EOF,
empty-indicator snapshot avoidance, digital-service exemption with inventory
identity, organization-only usage validation, and null optional wrappers.
They close the measured CSV, indicator and usage-validator gaps without
altering production code, existing tests, exclusions or thresholds.

### Explicitly unmeasured/excluded production files

These **16** changed backend paths are excluded by the existing POM and absent
from the XML. All share the production prefix above:

- apievaluating/mapper/AggregationToOutput.java
- apievaluating/mapper/ImpactToCsvRecord.java
- apievaluating/model/AggValuesBO.java
- apiindicator/mapper/EquipmentIndicatorMapper.java
- apiindicator/mapper/IndicatorRestMapper.java
- apiindicator/model/EquipmentImpactBO.java
- apiindicator/model/EquipmentIndicatorBO.java
- apiinout/mapper/InPhysicalEquipmentMapper.java
- apiinout/mapper/InputToCsvRecord.java
- apiinout/modeldb/InPhysicalEquipment.java
- apiinout/modeldb/PhysicalEquipmentEvaluationTrace.java
- apiinout/repository/PhysicalEquipmentEvaluationTraceRepository.java
- apiloadinputfiles/mapper/CsvToInMapper.java
- apireferential/mapper/ReferentialMapper.java
- apireferential/modeldb/ItemType.java
- config/JsonNullableConfig.java

JaCoCo cannot measure the **10** changed frontend non-test TS/HTML/SCSS paths.
Relative to services/frontend/src/app/, they are:

- core/interceptors/http-error.interceptor.ts
- core/interfaces/footprint.interface.ts
- core/service/business/inventory-util.service.ts
- core/service/mapper/sunburst-chart.mapper.ts
- core/shared/echarts.module.ts
- layout/common/indicator-section/indicator-section.component.html
- layout/common/indicator-section/indicator-section.component.scss
- layout/inventories-footprint/critere/inventories-critere-footprint.component.ts
- layout/inventories-footprint/inventories-footprint.component.ts
- layout/inventories-footprint/multicriteria/inventories-multicriteria-footprint.component.html

Resources, migrations and templates need behavioral verification, not Java
executable-line counters. No fresh frontend new-code measurement is available.
The whole-feature numerator/denominator including uninstrumented executable
code is still **unknown**, despite the actual 98.81% measured subset.

### Separate allocation/category/calculation impact-path assessment

| Family | Actual final XML measurement | Disposition |
|---|---|---|
| Complete new category/reference/allocation sourcefiles | 79/79 lines, 58/58 branches (100% each) | Complete reported source line/branch coverage, including nested records |
| Changed evaluator orchestration and calculation-request lines | 48/48 lines, 24/24 branches (100% each) | Changed-line subset only |
| Combined preceding allocation/calculation subset | **127/127 lines, 82/82 branches (100% each)** | Not proof of every path combination or excluded consumer |
| Changed persisted adoption consumer | 40/40 lines, 16/16 branches (100% each) | Snapshot quantity shares measured in service |
| Branch-baseline category import/persistence and unknown-type guard | 14/17 lines, 15/18 branches | Missing import/category-validation outcomes remain |
| Changed input/parser/usage validation integration | 68/68 lines, 61/62 branches | One caller-exemption branch remains |
| Grouped electricity and export/snapshot persistence | AggValuesBO, AggregationToOutput, ImpactToCsvRecord, trace entity/repository excluded | Component tests pass, numeric coverage unavailable |

The final full sourcefiles (not just changed lines) report
EvaluateNumEcoEvalService **165/174 lines, 68/74 branches** and EvaluateService
**505/543 lines, 183/234 branches**. Their missed paths cannot be represented
as all-covered merely because changed lines are covered. Existing lifecycle,
single-scaling, source-preservation, trace and CSV tests were included in the
166-test passing selection, but those tests do not prove complete native
grouping/database/export integration or exhaustive path coverage.

**TASK-018 stays in progress.** The ≥80% target is met for the measured
instrumented backend subset only; whole-feature coverage is not certified.
100% category/allocation/calculation impact-path coverage is not certified:
the category-import misses, excluded impact seams and complete evaluator path
matrix remain open. No configuration/test changes to inflate metrics occurred.

### Actual declared gate and remaining limits

The workflow implementation gate checks implementation-log existence, error-free
compilation, all tests passing, and accessibility implemented or explicitly
registered as open manual validation. It does **not** declare a literal numeric
coverage gate. Required reports exist; fresh backend verification passes;
166 focused tests pass with zero failures/errors/skips. Manual WCAG remains
explicitly pending, which satisfies registration but is not compliance proof.
Frontend evidence is historical. The known documentation checker failure
(52 errors/223 documents) and other incomplete applicable verification prevent
certifying “all tests pass.” **Station BLOCKED; actual gate NOT CERTIFIED.**
No downstream station, publication, state write or task-checkbox update occurred.
No access/command denial occurred here; two oversized Git outputs and the
full-XML reader limit were handled with bounded direct reads, not spool access.

## Current narrow-read reassessment — 2026-11-25 22:35 +01:00

**TASK-018 OPEN. Changed executable covered UNKNOWN / total UNKNOWN.
At least 80 percent new code: NOT DEMONSTRATED.
100 percent changed impact paths: NOT DEMONSTRATED. Station: BLOCKED.**
This is the current assessment; subsequent counters and access claims retain
their historical attribution.

### Direct XML access, freshness and provenance

The exact updated agent definition and backend POM were read fully. The exact
services/backend/target/site/jacoco/jacoco.xml path now has a narrow read-only
exception. The preceding dispatch's blanket generated-XML refusal is therefore
superseded; no broader generated-artifact or symlink access is authorized.

The direct functions.view invocation returned:

```text
File too large to read at once (850.7 KB). Consider using the rg tool to search within the file, or view with view_range to read specific sections.
```

This is the actual tool diagnostic, **not a report access denial**. It supports
report presence and reported size, not successful content retrieval. No
sessioninfo timestamps, mtime, class/source identities, per-line counters,
symlink status or content hash were obtained. Freshness/provenance are
**UNVERIFIED**, not inferred from earlier successful verify sessions.
No copy, alternate generated report, execution-data or terminal-spool read
was used. Arbitrary parser commands or a created executable helper were not
used to work around the command boundary.

The supplied knowledge-plane/SKILL.md does not exist. Bounded skill discovery
in permitted alternative roots returned no matches. Installed skill and
actual workflow-gate paths are under the definition's excluded .github
boundary and were not accessed after inspecting that boundary. The station
stops at those prerequisites rather than claiming the skills or current gate
were loaded. No new Maven invocation or targeted tests occurred.

### Final-tree inclusion and remaining denominator

Successful independent Git calls establish feat/g4it-48 and a final tracked
diff using origin/main's merge base. That diff contains **25 backend
production Java paths**; current status additionally identifies **7 untracked
production Java paths**. Thus **32 production paths** are in the requested
combined scope, including committed and current final tracked changes once
and untracked allocation, trace, validation, resolver and configuration code.
Staged executable contents were not independently assessed. Earlier exact
baseline hashes and zero staged inventory remain prior evidence, not newly
measured values.

The current POM corroborates the existing exclusion inventory below:
**16 included candidates / 16 excluded paths**. Excluded paths consist of:

- apievaluating: AggregationToOutput.java, ImpactToCsvRecord.java, AggValuesBO.java.
- apiindicator: EquipmentIndicatorMapper.java, IndicatorRestMapper.java,
  EquipmentImpactBO.java, EquipmentIndicatorBO.java.
- apiinout: InPhysicalEquipmentMapper.java, InputToCsvRecord.java,
  InPhysicalEquipment.java, PhysicalEquipmentEvaluationTrace.java,
  PhysicalEquipmentEvaluationTraceRepository.java.
- apiloadinputfiles: CsvToInMapper.java.
- apireferential: ReferentialMapper.java, ItemType.java.
- config: JsonNullableConfig.java.

The existing per-file table below supplies complete package-relative paths.
Exclusion is neither uncovered code nor an implicit waiver. Actual unmatched
included files and executable line numbers are **UNKNOWN**, not zero.
Physical additions, test counts and historical class counters are not used
as the changed executable denominator. Frontend changed coverage is likewise
not measurable with this backend XML.

### Changed impact families and gate

Required families remain reference/category precedence and exact eligibility,
allocation defaults/bounds/exemptions and snapshots, every lifecycle/criterion
request and exactly-once electricity, grouped electricity/original quantities,
persisted trace/export/adoption consumers, and raw ingestion preservation.
The preceding family inventory is retained, but no current executable
line/branch intersection or exhaustive executed-path matrix was produced.
No speculative test was added merely to raise counts.

**Current numerical executable coverage: UNKNOWN / UNKNOWN.**
No class average or global percentage is substituted for either target.
No tests were run in this dispatch; prior successful backend verification
and focused tests remain historical, not fresh source/report provenance.

The actual current workflow gate could not be loaded within the read boundary.
Its preceding recorded log/compilation/tests/accessibility definition is
historical context, not independent current confirmation. Gate: **NOT
CERTIFIED**, not passed and not replaced with an invented coverage gate.
TASK-018's separate constitution/task coverage obligation remains open.

Resumption requires readable dispatched skill documents and actual gate
context, then a permitted bounded direct XML assessment establishing provenance
and executable diff intersections. Excluded impact seams and frontend code need
their own coverage evidence without shrinking the target. Existing documentation
failures and pending manual accessibility remain explicit. Only the three
resolved reports are updated; task/state/source/index and downstream stations
are untouched.

## Current branch-based assessment — dispatch at 2026-11-25 22:23 +01:00

**TASK-018 OPEN. Changed executable lines: covered UNKNOWN / total UNKNOWN.
Changed impact-path coverage: UNKNOWN, not demonstrated at 100%.**
This assessment supersedes earlier baseline and current-measurement statements,
not their historical execution evidence. No absent report entry is classified
as uncovered.

### Exact local baseline and inclusion

- Current branch: feat/g4it-48.
- HEAD and origin/feat/g4it-48:
  2823b05080e6d00f02afc1029421538fc79512a6.
- Configured tracking destination: origin, refs/heads/feat/g4it-48.
  This is a tracking branch, **not evidence of a PR merge target**.
- Inferred comparison target: origin/main, also the local remote HEAD.
  Local main and origin/main both resolve to
  b09dd9eda0918d3f248a0b0bf3cc3e54c1d61974.
- Actual merge base, independently obtained against both local main and
  origin/main: **e33f2158b1ed56bdebf682532a3d1549cb255dce**.
  There are six HEAD-side commits and five origin/main-side commits since it.
  No fetch, remote service access, checkout, staging or commit occurred.

The measured tracked delta is merge-base-to-final-working-tree, not just
HEAD-to-working-tree and not a sum of overlapping staged/unstaged deltas.
It includes committed G4IT-42 changes rather than silently removing them.
Untracked relevant Java sources are added separately against an empty source.
The previous 25-file/729-line HEAD comparison is therefore a **different,
narrower baseline**, not the requested complete branch change denominator.

| Fresh source inventory | Files | Added physical lines | Deleted physical lines |
|------------------------|------:|---------------------:|-----------------------:|
| Backend production, tracked final delta | 25 | 402 | 91 |
| Backend production, untracked | 7 | 381 | 0 |
| Backend production, combined | **32** | **783** | **91** |
| Backend tests, tracked final delta | 10 | 896 | 36 |
| Backend tests, untracked | 14 | Not counted | Not applicable |
| Frontend non-test TS/HTML/SCSS, tracked | 10 | 238 | 16 |
| Frontend test TS, tracked | 4 | 712 | 3 |
| Frontend relevant untracked sources | 0 | 0 | 0 |

Source-only staged inventory: **0 files**. Unstaged inventory: **32 files**
(18 backend production, eight backend tests, five frontend non-test and one
frontend test). Untracked inventory: **21 files**, all backend (seven
production and fourteen tests). Final branch inventory includes **70 relevant
source files**: 49 tracked final deltas plus 21 untracked. These numbers concern
the explicit Java and frontend TS/HTML/SCSS source scopes, not all repository
assets. CSV, translation resources, OpenAPI, SQL and documentation are not
executable-source coverage denominators.

**Physical additions are not executable lines.** No percentage is calculated
from 783, 238, source-file counts, test counts or historical class coverage.

### Every backend production file and existing report exclusions

Paths below are relative to
services/backend/src/main/java/com/soprasteria/g4it/backend/.
“Included” means no match in the inspected POM exclusion list; actual XML
membership is unverified. “Excluded” identifies the existing JaCoCo
instrumentation/report exclusion, not a failed coverage result.

| Path | Added physical lines | POM disposition |
|------|---------------------:|-----------------|
| apievaluating/business/asyncevaluatingservice/EvaluateService.java | 60 | Included |
| apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java | 29 | Included |
| apievaluating/mapper/AggregationToOutput.java | 2 | Excluded: mapper |
| apievaluating/mapper/ImpactToCsvRecord.java | 37 | Excluded: mapper |
| apievaluating/model/AggValuesBO.java | 17 | Excluded: model |
| apiindicator/business/InventoryIndicatorService.java | 74 | Included |
| apiindicator/mapper/EquipmentIndicatorMapper.java | 2 | Excluded: mapper |
| apiindicator/mapper/IndicatorRestMapper.java | 5 | Excluded: mapper |
| apiindicator/model/EquipmentImpactBO.java | 8 | Excluded: model |
| apiindicator/model/EquipmentIndicatorBO.java | 6 | Excluded: model |
| apiinout/mapper/InPhysicalEquipmentMapper.java | 13 | Excluded: mapper |
| apiinout/mapper/InputToCsvRecord.java | 7 | Excluded: mapper |
| apiinout/modeldb/InPhysicalEquipment.java | 10 | Excluded: modeldb |
| apiloadinputfiles/business/asyncloadservice/LoadFileService.java | 10 | Included |
| apiloadinputfiles/business/asyncloadservice/checkobject/CheckPhysicalEquipmentService.java | 6 | Included |
| apiloadinputfiles/business/asyncloadservice/loadmetadata/LoadMetadataService.java | 1 | Included |
| apiloadinputfiles/business/asyncloadservice/loadobject/LoadPhysicalEquipmentService.java | 1 | Included |
| apiloadinputfiles/mapper/CsvToInMapper.java | 7 | Excluded: mapper |
| apireferential/business/ReferentialImportService.java | 18 | Included |
| apireferential/business/ReferentialService.java | 6 | Included |
| apireferential/business/WorkspaceReferentialImportService.java | 3 | Included |
| apireferential/mapper/ReferentialMapper.java | 1 | Excluded: mapper |
| apireferential/modeldb/ItemType.java | 12 | Excluded: modeldb |
| apireferential/persistence/WorkspaceReferentialPersistenceService.java | 2 | Included |
| common/utils/CsvUtils.java | 65 | Included |
| apievaluating/business/asyncevaluatingservice/PhysicalEquipmentAllocationService.java (untracked) | 86 | Included |
| apiinout/modeldb/PhysicalEquipmentEvaluationTrace.java (untracked) | 55 | Excluded: modeldb |
| apiinout/repository/PhysicalEquipmentEvaluationTraceRepository.java (untracked) | 30 | Excluded: repository |
| apiloadinputfiles/business/asyncloadservice/checkobject/PhysicalEquipmentUsageValidator.java (untracked) | 67 | Included |
| apireferential/business/ItemCategoryResolver.java (untracked) | 65 | Included |
| apireferential/business/PhysicalEquipmentReferenceResolver.java (untracked) | 44 | Included |
| config/JsonNullableConfig.java (untracked) | 34 | Excluded: config |
| **Combined** | **783** | **16 included / 16 excluded** |

Included-file physical additions total **537**; excluded-file physical additions
total **246**. Excluded files must not silently disappear from an asserted
whole-branch new-code target. Frontend sources require their own coverage
measurement; JaCoCo cannot measure them.

### Fresh verification and exact blocked action

Session g4it48-station-2223-verify ran the full offline coverage-profile verify
with isolated execution/report data-file properties and **exited 0**. Exact
command and terminal outcome are retained in implementation-log.md. The
40.4 KB output was spooled outside the declared read scope; only the inline
preview and completion code were available. No aggregate test totals, XML
counters, analyzed-class count or source/report correspondence are claimed
from this run.

Intended direct action:
functions.view on
/root/projects/ssg-g4it/g4it-on-github/services/backend/target/site/jacoco/jacoco.xml.

**Preflight refusal: DENIED: read of
services/backend/target/site/jacoco/jacoco.xml (generated-artifact read boundary).**
This is a pre-call refusal, **not** a returned filesystem or tool error. The
read was not submitted. Work stopped on this path. No copy, alternate reader,
report redirection, HTML/CSV substitute, execution-data inspection or terminal
spool inspection was used to bypass it. Report existence, freshness and exact
membership were not independently inspected.

Consequently, actual unmatched included files and unmatched executable line
numbers are **UNKNOWN**, not zero. The sixteen POM exclusions above are
identified independently of report absence. A successful verify lifecycle does
not itself measure changed-line coverage or enforce a numeric threshold.

### Changed impact-path assessment, not class/global coverage

| Changed impact-path family and identified source seams | Current measurable disposition |
|-------------------------------------------------------|--------------------------------|
| Reference/type selection, category precedence and exact eligibility: category/reference resolvers, ReferentialService, InventoryIndicatorService, and committed category-import/persistence changes | Changed executable-line/branch numerator and denominator UNKNOWN |
| Effective defaults, explicit bounds/override, exemptions, source preservation and trace preparation: PhysicalEquipmentAllocationService and EvaluateService | Changed executable-line/branch numerator and denominator UNKNOWN |
| Every criterion/lifecycle defensive calculation request and single-factor electricity: EvaluateNumEcoEvalService | Changed executable-line/branch numerator and denominator UNKNOWN |
| Grouped electricity and original-quantity preservation: EvaluateService, AggValuesBO, AggregationToOutput | Included service UNKNOWN; model/mapper explicitly excluded |
| Persisted evaluated fraction and task/source identity, export and adoption consumers: trace entity/repository, ImpactToCsvRecord, InventoryIndicatorService and related models/mappers | Included service UNKNOWN; excluded consumer/persistence seams separately identified |
| Ingestion prerequisite and raw-value preservation: loader, validator, CSV utility and input mapper/model changes | Included sources UNKNOWN; input/CSV mappers and model explicitly excluded |

The inventory identifies the changed families, but is **not an exhaustive
enumeration of executable branches or proof of 100% of paths**. Historical
75-test focused success and complete resolver/allocation class counters remain
historical, not measurements of this final working tree. No newly identified
uncovered line is available to justify another targeted test; no arbitrary test
was added merely to increase counts.

### Gate, limits and next decisions

The actual implementation gate in .github/workflows/feature-implementation.yml
checks log existence, error-free compilation, passing tests, and implemented
accessibility or explicit open manual validation. It **does not literally add
an 80% coverage check**. Coverage remains the separately required constitution
and TASK-018 obligation.

Backend verify passes currently. Existing frontend/build and accessibility
registration evidence is retained, not rerun. The known documentation test
failure and other incomplete applicable verification are not waived. This
station does not self-certify all tests passing or complete TASK-018.
**Station result: BLOCKED; gate not certified.** State and task checkboxes are
unchanged; no downstream station was started.

Actionable open decisions: confirm main as the intended merge target (local
metadata establishes a usable comparison, not PR intent); provide a permitted
direct report-read mechanism before executable-line intersection; define
measurement of the sixteen excluded files without weakening the threshold;
then identify genuinely missed changed impact branches, add targeted tests and
repeat isolated verification and measurements. The baseline is no longer an
unchecked prerequisite.

## Latest coverage assessment — 2026-11-25 22:16 +01:00

**TASK-018 OPEN. ≥80% new-code: NOT DEMONSTRATED.
100% impact paths: NOT DEMONSTRATED. Station: BLOCKED.**
This section is the current assessment; counters in later sections are historical.

### Defensible source/change reference

Read-only Git evidence identifies local HEAD
2823b05080e6d00f02afc1029421538fc79512a6, with the G4IT-42 squash subject.
This is the explicit pre-feature comparison reference, not an assumed global
percentage or a test-count denominator. No commits were created or local edits
discarded. It is a conservative working-tree change baseline, not a confirmed
feature-only merge-base: existing local edits are included rather than silently
filtered out.

Evidence mechanisms: HEAD log, tracked Java diff numstat against HEAD, untracked
Java file enumeration, and each new class diff numstat against an empty source.
The new-class diff exit code 1 indicates differences, not failed verification.
Only hand-written production Java is included below; generated code, resources,
migrations and frontend have separate verification obligations.
Prefixes are relative to services/backend/src/main/java/com/soprasteria/g4it/backend/.

| Changed/new production source | Added physical lines | Instrumented by existing POM |
|-------------------------------|---------------------:|------------------------------|
| apievaluating/business/asyncevaluatingservice/EvaluateService.java | 60 | yes |
| apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java | 29 | yes |
| apievaluating/mapper/AggregationToOutput.java | 1 | no |
| apievaluating/mapper/ImpactToCsvRecord.java | 37 | no |
| apievaluating/model/AggValuesBO.java | 17 | no |
| apiindicator/business/InventoryIndicatorService.java | 67 | yes |
| apiindicator/mapper/IndicatorRestMapper.java | 5 | no |
| apiindicator/model/EquipmentIndicatorBO.java | 6 | no |
| apiinout/mapper/InPhysicalEquipmentMapper.java | 13 | no |
| apiinout/mapper/InputToCsvRecord.java | 7 | no |
| apiinout/modeldb/InPhysicalEquipment.java | 10 | no |
| apiloadinputfiles/business/asyncloadservice/LoadFileService.java | 10 | yes |
| apiloadinputfiles/business/asyncloadservice/checkobject/CheckPhysicalEquipmentService.java | 6 | yes |
| apiloadinputfiles/business/asyncloadservice/loadmetadata/LoadMetadataService.java | 1 | yes |
| apiloadinputfiles/business/asyncloadservice/loadobject/LoadPhysicalEquipmentService.java | 1 | yes |
| apiloadinputfiles/mapper/CsvToInMapper.java | 7 | no |
| apireferential/business/ReferentialService.java | 6 | yes |
| common/utils/CsvUtils.java | 65 | yes |
| apievaluating/business/asyncevaluatingservice/PhysicalEquipmentAllocationService.java (new) | 86 | yes |
| apiinout/modeldb/PhysicalEquipmentEvaluationTrace.java (new) | 55 | no |
| apiinout/repository/PhysicalEquipmentEvaluationTraceRepository.java (new) | 30 | no |
| apiloadinputfiles/business/asyncloadservice/checkobject/PhysicalEquipmentUsageValidator.java (new) | 67 | yes |
| apireferential/business/ItemCategoryResolver.java (new) | 65 | yes |
| apireferential/business/PhysicalEquipmentReferenceResolver.java (new) | 44 | yes |
| config/JsonNullableConfig.java (new) | 34 | no |
| **25 files: 18 tracked modifications and 7 new classes** | **729** | **13 included; 12 excluded** |

Tracked additions total 348 lines; new-class additions total 381.
Of the physical added lines, 222 are in excluded files and 507 in included files.
These counts include comments, declarations, imports and blank lines.
**729 is a source-change inventory, NOT an executable coverage denominator.**
No ≥80% percentage is computed from it.

The required denominator D is the number of executable added/changed Java source
lines in this baseline, including executable lines in the twelve excluded files.
For instrumented files, added-line hunks must be intersected with current JaCoCo
source lines having nonzero instruction counters; covered lines form numerator C.
Excluded layers need approved measurement rather than being silently removed
from D. Report C/D with precise inclusion rules and source/report correspondence.
Current D and C are **unknown**, not zero. A feature-only baseline may refine this
conservative inventory only with explicit provenance for removed unrelated edits.

### Newly executed evidence and impact-path matrix

Final unfiltered coverage-profile verify exited 0 in session
g4it48-coverage-verify-20261125-2215. Inline prepare-agent output confirms all
existing exclusions and the isolated execution-data path
services/backend/target/g4it48-e6fa33d4-coverage-20261125-2215.exec.
The report goal loaded that same file and analyzed 173 classes.
No thresholds/exclusions/configuration were changed. This is a fresh successful
coverage-instrumented lifecycle, but its inline output contains no line/branch
counters. It does not prove numeric acceptance.

| Impact-path family | Fresh exercised evidence | Remaining completeness gap |
|--------------------|--------------------------|----------------------------|
| Category precedence/exact eligibility | Nine category cases, including same-workspace organization precedence and absent workspace | Current line/branch counters and full cross-consumer matrix |
| Allocation defaults/bounds/exemptions/snapshots | Eight allocation cases, including four real-resolver conflicting-reference exemptions | Current counters linked to every changed branch |
| Every criterion/lifecycle calculation copy | Thirty engine tests including five fractions over eight captured requests, unchanged per-unit electricity and defensive-copy identity | Numerical result/path counters and exhaustive changed calculation-path map |
| Grouped electricity, trace reload/export and legacy/digital-service | Existing 28-case EvaluateServiceTest passes in the same final selection | Native grouping/integration provenance and changed-branch counters |
| Export/mapping/model/serialization impact seams | Existing tests retained; no new configuration changes | Executable-line measurement for excluded layers |

The final focused selection passed 75 tests with no failures/errors/skips.
These tests improve meaningful behavior coverage; neither test counts nor
eight-request products are line/branch coverage measurements. The new request
tests mock calculation outputs and do not independently establish numerical
impact/electricity ratios. Historical real-engine evidence remains historical.

### Precisely missing evidence and blocked read

Generated test/coverage reports and terminal spools were refused at read
preflight; none were opened or copied by an alternate route. This is a current
read-boundary limitation, not an assertion that the filesystem reports do not
exist or that the successful report goal failed.

Closure requires an authorized measurement of:
1. Final-source per-line and branch counters from the isolated verification,
   matched to the baseline's added-line hunks.
2. Executable added-line membership and coverage in all twelve excluded layers,
   or an explicitly approved task-level denominator definition. Existing
   exclusions do not implicitly waive the constitution's target.
3. An exhaustive changed category/allocation/evaluation/electricity path matrix,
   tied to executed tests and branch counters, including grouping and consumers.
4. Feature-specific frontend new-code coverage for its separate ≥80% obligation.

No current feature percentage or 100% impact-path result can be established from
the available evidence. No requirement was rewritten or weakened, no task was
marked complete, and no downstream station or workflow-state update occurred.

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

CONTEXT LOADED: all three engine inputs and both dispatched skills read
completely; prior verification/closure evidence inspected as log sections.

## Ownership and decision

**Owner: feature-implementation / implementation**, trace
e6fa33d4-27cf-4d12-84a2-3c70d47d88da.
This is not the output of a subsequent coverage or quality-validation station.
The exact declared path was absent immediately before editing; the inspected
workflow state has the later coverage station pending. No distinct artifact
was overwritten and no alternate filename or configuration was introduced.

**TASK-018: OPEN. Coverage acceptance: NOT DEMONSTRATED.**
Both the at-least-80-percent new-code threshold and complete impact-path
coverage need evidence. Class coverage and a successful verify goal cannot
substitute for either requirement.

## Newly executed validation

| Invocation | Observed result | Coverage evidence available inline |
|------------|-----------------|------------------------------------|
| `mvn -o -f services/backend/pom.xml -Pcoverage verify` | Exit 1, session `g4it48-implementation-verify-20261125` | Oversized output; failure phase/cause and metrics unknown. |
| `mvn -o -f services/backend/pom.xml -Pcoverage -Dorg.slf4j.simpleLogger.defaultLogLevel=error -Dmaven.test.redirectTestOutputToFile=true -DtrimStackTrace=true verify` | Exit 0, session `g4it48-implementation-verify-bounded-20261125` | No numeric coverage counters or complete test counts in the captured output. |

The repeat changed runtime logging only, not source, configuration, dependencies
or execution-data files through a manual edit/cleanup. No skip/failure-ignore
flag was used. V1's cause remains unknown, not a proven corruption diagnosis.
The subsequently executed focused test goal passed 61 tests; its complete
command/results are in [test-report.md](test-report.md#new-executions).
That test goal is not new coverage verification.

Generated coverage/test directories and the terminal spool were not opened or
copied in this recovery. **Current feature coverage: unknown.**
The current report therefore contains no fabricated fresh percentage.

## Historical measured counters

Source: implementation-log.md, “Station closure attempt - 2026-11-25”.
That earlier section attributes its counters to a generated JaCoCo CSV read.
Here the **log**, not the generated CSV, was inspected. These are prior
class-level results, not measurements of V2 or a diff-based new-code metric.

| Class | Covered/total lines | Covered/total branches |
|-------|---------------------|------------------------|
| ItemCategoryResolver | 9/9 | 10/10 |
| PhysicalEquipmentReferenceResolver | 14/14 | 18/18 |
| PhysicalEquipmentUsageValidator | 22/22 | 29/32 |
| PhysicalEquipmentAllocationService | 38/38 | 24/24 |
| EvaluateNumEcoEvalService | 162/171 | 62/68 |
| EvaluateService | 489/527 | 132/183 |
| LoadFileService | 195/202 | 50/56 |
| CsvUtils | 63/64 | 51/54 |
| InPhysicalEquipmentService | 85/90 | 27/30 |
| InventoryIndicatorService | 76/76 | 15/16 |

The four core resolver/reference/validator/allocation classes sum to **83/83
lines and 81/84 branches**, as previously recorded. Category, reference and
allocation class counters are complete in that historical report. Usage
validation and evaluator class counters are not all complete.

These numbers do not identify which missed evaluator branches are feature
impact paths. It would be equally unsupported to claim all missed branches
are feature defects or to claim none are relevant.

Earlier full backend verification records 1,395 tests, 1,386 passed, zero
failures/errors and nine skipped. Earlier frontend coverage records 72.64
percent statements, 57.17 percent branches, 67.7 percent functions and 72.86
percent lines globally. Neither is a fresh result or a new-code proxy.

## Fresh targeted recovery evidence

The 65-test key suite and 69-test evaluator/export suite passed in separate
fresh Maven selections on 2026-11-25. A successful JaCoCo report generation
analyzed 173 classes from the accumulated execution data.

The fresh class counters include:

| Class | Covered/total lines | Covered/total branches |
|-------|---------------------|------------------------|
| ItemCategoryResolver | 9/9 | 10/10 |
| PhysicalEquipmentReferenceResolver | 14/14 | 18/18 |
| PhysicalEquipmentUsageValidator | 22/22 | 29/32 |
| PhysicalEquipmentAllocationService | 38/38 | 24/24 |
| EvaluateNumEcoEvalService | 162/171 | 62/74 |
| EvaluateService | 489/527 | 132/234 |

These are class-level observations, not a diff-based new-code denominator.
Category and allocation classes have complete fresh line and branch counters.
Calculation classes still contain missed lines and branches.

## Measurement boundaries

### Current blocker recheck — 2026-11-25

Session `g4it48-blocker-verify-current` ran the existing unfiltered offline
backend coverage verification:

```text
mvn -o -f services/backend/pom.xml -Pcoverage -Dorg.slf4j.simpleLogger.defaultLogLevel=error -Dmaven.test.redirectTestOutputToFile=true -DtrimStackTrace=true verify
```

**Exit 0**, with normal generation and no source/configuration changes,
cleanup, test skipping or failure-ignore options. The earlier generated
HttpHeaders compilation failure did not recur. Its cause is not established.
The existing POM compatibility replacement is present; it was not added
or modified here.

The captured output supplies no current coverage counters, test totals or
skips. Generated coverage/test artifacts and terminal spools were not read.
All numeric class counters elsewhere in this report remain attributed to
the earlier executions, not this current run. A successful verification is
not evidence of either numeric threshold because the POM has no threshold
check or feature baseline.

**TASK-018 stays OPEN.** No complete feature changed-line denominator,
coverage of excluded changed layers, frontend new-code measurement or
changed calculation-path matrix was produced. A defined baseline and
permitted measurement mechanism remain necessary. No threshold, exclusion,
acceptance wording or tracker status was changed.

The implementation workflow gate was read separately: it requires
compilation, all tests and implemented accessibility or an explicit manual
validation registration. Coverage remains a constitution/task obligation;
it must not be misrepresented as a separate literal workflow gate criterion.
The station is still blocked, with current backend verification passing,
other applicable verification incomplete and coverage acceptance unproven.

The fully read services/backend/pom.xml configures JaCoCo 0.8.13 prepare-agent
and verify-phase report generation. The coverage profile provides a Sonar XML
report location, but no numeric JaCoCo check, feature baseline, changed-line
denominator or path-completeness assertion.

Its exclusions include generated code, controllers, mappers, configuration,
exceptions, models/entities, repositories, tasklets and clients. Several of
these layers are modified by the feature. Reported business-class coverage
cannot establish coverage of all feature code, migrations, REST serialization,
export mapping or frontend changes.

| Required measurable assertion | Available evidence | Disposition |
|-------------------------------|--------------------|-------------|
| New-code coverage at least 80 percent | No complete changed executable-line denominator or baseline/reference revision | NOT DEMONSTRATED |
| All category paths covered | Historical 9/9 lines and 10/10 branches in ItemCategoryResolver, plus complete reference counters | Historical class evidence only |
| All allocation paths covered | Historical 38/38 lines and 24/24 branches in PhysicalEquipmentAllocationService | Historical class evidence only |
| All calculation/lifecycle/electricity paths covered | Behavioral tests and incomplete evaluator class counters; no complete changed-path matrix | NOT DEMONSTRATED |
| Combined TASK-018 coverage condition | Individual class counters and V2 exit 0 | OPEN |

Missing measurement inputs are a defined new-code baseline and complete
feature file/changed-line inventory, measurable coverage of those lines
including excluded layers, and a matrix linking every changed category,
allocation, lifecycle and electricity path to executed tests and counters.
No configuration modification was attempted to manufacture these inputs.

## Acceptance and gate self-check

TASK-018 criterion 1 contains a semicolon and is rejected/skipped under the
acceptance filter. Its existing checked tracker value is retained, not newly
certified. The numeric coverage criterion is evaluated honestly as unproven.
No criterion wording, task checkbox or other station state is modified.

- Required exact-path coverage report: written through the native editor and
  checked by post-write retrieval.
- Provenance, inputs, history and initial draft/not-rated/not-reviewed metadata:
  present. No reviewer approval or fresh generated-report read is claimed.
- TASK-018 complete coverage condition: **not met by available evidence**.
- Standalone TASK-019 test evidence: retained in test-report.md; rejected
  criterion wording remains separate from passing tests.
- Subsequent coverage, quality-validation, PR-validation and quality-gate
  stations: **not executed**.

**Implementation self-check: BLOCKED.** This artifact documents a measurable
evidence gap; its existence is not a passing coverage gate.
