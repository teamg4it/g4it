---
workflow: feature-implementation
trigger: user
date: '2026-11-26'
status: review
feature: G4IT-48
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: quality-validation/security-sast
agent: quality-validator
skill: security-scan
sensitivity: internal
context_retrieval: files
execution_mode: agent-review-fallback
automated_scan_status: skipped-no-local-adapter
validation_status: passed
gate_result: warning
revision: c65fbfc9
comparison_base: origin/feat/g4it-ia-backbone
revision_scope: local-worktree-including-untracked-spreadsheet-service-and-test
remediation_status: resolved-by-source-review-and-focused-regressions
remediation_finding: G4IT-48-SAST-001
remediation_revision: worktree-on-c65fbfc9
csv_remediation_status: mitigated-by-separate-text-typed-xlsx-with-medium-residual
csv_residual_risk: raw-csv-remains-unsafe-for-spreadsheets
findings:
  critical: 0
  high: 0
  medium: 1
  low: 0
inputDocuments:
  - .github/skills/security-scan/SKILL.md
  - .github/workflows/quality-validation.yml
  - .github/instructions/output-metadata.instructions.md
  - .github/knowledge/governance/ai-disclaimer-text.md
  - services/backend/pom.xml
  - services/frontend/package.json
  - services/frontend/tsconfig.json
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiuser/business/AuthService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/config/AuthenticationFilter.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/config/SecurityConfig.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinventory/business/InventoryService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/controller/InPhysicalEquipmentInventoryController.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/business/InPhysicalEquipmentService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/InPhysicalEquipmentRepository.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/modeldb/InPhysicalEquipment.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/mapper/InputToCsvRecord.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/mapper/ImpactToCsvRecord.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/business/asyncloadservice/checkobject/PhysicalEquipmentUsageValidator.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/common/utils/CsvUtils.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/common/filesystem/business/local/CsvFileService.java
  - services/backend/src/main/resources/swagger/greenit/api-in-out/inventory-input-items.yml
  - services/backend/src/main/resources/swagger/greenit/templates/api.mustache
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiuser/business/AuthServiceTest.java
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorExportSecurityTest.java
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorExportControllerTest.java
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiinout/controller/InPhysicalEquipmentInventoryContractTest.java
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiloadinputfiles/business/asyncloadservice/checkobject/PhysicalEquipmentUsageValidatorTest.java
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiloadinputfiles/mapper/CsvHeaderNormalizationTest.java
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apievaluating/mapper/ImpactToCsvRecordTest.java
  - services/frontend/src/app/layout/common/indicator-section/indicator-section.component.html
  - services/frontend/src/app/layout/inventories-footprint/critere/inventories-critere-footprint.component.ts
  - services/frontend/src/app/layout/inventories-footprint/inventories-footprint.component.ts
  - services/frontend/src/app/core/service/mapper/sunburst-chart.mapper.ts
  - outputs/specs/features/G4IT-48/spec.md
  - outputs/specs/features/G4IT-48/tasks.md
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventorySpreadsheetExportService.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java
  - services/backend/src/main/java/com/soprasteria/g4it/backend/config/RestExceptionHandler.java
  - services/backend/src/main/resources/swagger/greenit/inventory-indicator.yml
  - services/backend/src/test/java/com/soprasteria/g4it/backend/apiindicator/business/InventorySpreadsheetExportServiceTest.java
  - services/frontend/src/app/layout/inventories-footprint/header/inventories-header-footprint.component.ts
  - services/frontend/src/app/layout/inventories-footprint/header/inventories-header-footprint.component.html
  - services/frontend/src/app/layout/inventories-footprint/header/inventories-header-footprint.component.spec.ts
  - services/frontend/src/app/core/service/data/footprint-data.service.ts
  - services/frontend/src/app/core/service/data/footprint-data.service.spec.ts
  - services/frontend/src/assets/i18n/en.json
  - services/frontend/src/assets/i18n/fr.json
  - outputs/specs/features/G4IT-48/implementation-log.md
  - .github/skills/test-execution/SKILL.md
context_manifest:
  - path: .github/skills/security-scan/SKILL.md
    mode: full
    key_facts:
      - 'Missing local adapters select agent-based OWASP review.'
      - 'A high or critical vulnerability fails the SAST gate.'
  - path: .github/workflows/quality-validation.yml
    mode: full
    key_facts:
      - 'security-sast requires sast-report.md and has a blocker gate.'
      - 'The required criterion is no high or critical vulnerabilities.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiuser/business/AuthService.java
    mode: targeted
    key_facts:
      - 'The corrected checkUserRightForInventory recognizes workspaces at URL segment 3.'
      - 'For real inventory routes it calls inventoryExists with organization, workspace and inventory identifiers; invalid numeric IDs are rejected.'
  - path: services/backend/src/test/java/com/soprasteria/g4it/backend/apiuser/business/AuthServiceTest.java
    mode: targeted
    key_facts:
      - 'Fixtures now use actual workspace routes and verify the ownership lookup.'
      - 'Foreign inventory and malformed IDs are rejected; collection and unrelated routes retain their behavior.'
  - path: services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventorySpreadsheetExportService.java
    mode: full
    key_facts:
      - 'ZIP entries are streamed as semicolon-delimited CSV, not extracted using entry names.'
      - 'Every cell is created as STRING; the converter creates no formulas, hyperlinks or external links.'
      - 'SXSSF uses a 100-row window; Excel row, column and text limits produce explicit 422 errors, and temporary output is cleaned on failure or stream close.'
  - path: services/backend/src/test/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorExportSecurityTest.java
    mode: full
    key_facts:
      - 'Sixteen cases use the actual JWT/filter/AuthService security chain with mocked persistence and storage.'
      - 'Foreign inventory is rejected before task, indicator or file access on raw ZIP, XLSX and equipment routes.'
      - 'The XLSX client Accept header negotiates both successful binary output and a JSON 422 error.'
  - path: services/frontend/src/app/layout/inventories-footprint/header/inventories-header-footprint.component.ts
    mode: full
    key_facts:
      - 'The primary download selects XLSX; raw CSV requires a separate confirmation callback.'
      - 'XLSX failure shows an error without automatically invoking the raw CSV download.'
  - path: .github/skills/test-execution/SKILL.md
    mode: full
    key_facts:
      - 'Reexecuting implementation tests supplies independent validation evidence.'
      - 'Test counts, failures and skipped cases must be reported without installing missing runners.'
changeHistory:
  - date: '2026-11-26'
    description: 'Submitted the bounded source security review after the user requested publication retry'
    changes:
      - 'Recorded one high pre-existing authorization defect and one medium CSV export risk'
      - 'Separated frontend and backend coverage and automated-scanner absence'
      - 'Preserved static-only evidence limitations, deferred PMD and the backend Sonar reserve'
      - 'Retried only the declared report path; native workflow state remains authoritative'
  - date: '2026-11-26'
    description: 'Recorded successful report creation and the remaining native execution limitation'
    changes:
      - 'The declared report was created through the native file-writing tool on retry'
      - 'Post-hooks and the native gate update were not executed because this tool profile exposes no terminal/execution tool'
      - 'Verified that the authoritative station state still reads failed / blocked-by-hook'
  - date: '2026-11-27'
    description: 'Completed publication checks and persisted the actual failing security gate'
    changes:
      - 'Verified unchanged HEAD c65fbfc9 and no frontend/backend worktree changes'
      - 'Reexecuted pre-hooks on the explicit nonempty scope manifest without blocking findings'
      - 'Executed post-hooks successfully with publication risk 0, distinct from application vulnerability severities'
      - 'Native engine confirmed security-sast failed / fail, replacing the former blocked-by-hook publication outcome'
      - 'Preserved both open findings, static-only evidence, deferred PMD and the backend Sonar reserve'
  - date: '2026-11-27'
    description: 'Recorded the user-authorized SAST-001 implementation and local regression evidence'
    changes:
      - 'Corrected the inventory guard to recognize actual workspaces routes'
      - 'Recorded nine expected failures before the fix and 39 passing tests after it'
      - 'Verified HTTP rejection before task, indicator or storage access'
      - 'Preserved the last registered failed assessment pending security reassessment; CSV remains open'
  - date: '2026-11-27'
    description: 'Implemented the user-selected distinct spreadsheet-safe export for SAST-002'
    changes:
      - 'Preserved raw CSV bytes and provided a separate explicit-text XLSX endpoint and primary UI download'
      - 'Added EN/FR raw-CSV risk confirmation without claiming raw CSV is safe'
      - 'Recorded 71 backend and 11 frontend passes, configured lint/build success and Hugo generation'
      - 'Kept residual machine-CSV risk and independent security reassessment explicit; no native gate change'
  - date: '2026-11-27'
    description: 'Completed exact-client-header regression verification for the spreadsheet error response'
    changes:
      - 'Corrected the new client to accept JSON errors alongside XLSX downloads'
      - 'Confirmed sixteen HTTP/security cases with the actual client header and the 422 JSON payload'
      - 'Retained the earlier 71-case backend result separately, with final eleven frontend cases and lint/build passing'
  - date: '2026-11-27'
    description: 'Reassessed the current frontend/backend remediation worktree at the user request'
    changes:
      - 'Resolved SAST-001 after source review and independent real-route regression verification'
      - 'Verified explicit STRING XLSX output and kept SAST-002 open at medium severity for raw CSV'
      - 'Independently reran 51 backend tests and 11 frontend tests, all passing without skips'
      - 'Assessed the no-high-or-critical gate as passed with warning, without claiming an automated scan or global quality completion'
holisticQualityRating: adequate
overallStatus: warning
---
<!-- AI_DISCLAIMER v1.0 -->
# SAST Security Review Report

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Result

- **Mode**: agent-based OWASP source review, using the security-scan skill's fallback when local adapters are absent.
- **Reassessment**: **passed / warning**: **0 critical**, **0 high**, **1 medium**, **0 low** open findings in the bounded review.
- **Current remediation**: SAST-001 is **resolved** in the local worktree on **c65fbfc9**. SAST-002 is **mitigated with a medium residual risk**, not closed: the distinct XLSX export uses explicit text cells, while raw CSV remains unsafe for direct spreadsheet opening.
- **Evidence level**: source review plus independently rerun local tests: **51 backend** and **11 frontend**, all passing. No deployed exploitation test was performed.
- **Automated scanner**: **skipped / no local adapter**. No Checkmarx scanner or scanner version was used. This is the documented agent-review fallback, not an automated clean-scan result.
- **Sensitivity**: internal; no production data, credentials or external services accessed.
- **State ownership**: this report does not itself change workflow state. A successful native transition and the engine-authored audit trace are authoritative.

## Current reassessment - 2026-11-27

The user requested reassessment after both remediations. The assessed revision
is the **current uncommitted worktree**, including the untracked spreadsheet
service and its test, on HEAD
**c65fbfc9ccd48614928b24c87b2a461cca0a53e3**. The comparison base remains
**origin/feat/g4it-ia-backbone**. This does not attest a clean commit, remote CI
result or a fresh review of every file in the original branch inventory.

The current scope comprises the **20 explicit paths** in the session scope
manifest: backend guard/filter/ownership lookup, export controller/converter,
exception handler and OpenAPI; focused backend tests; frontend header, download
service, their tests and EN/FR export messages; and the feature specification,
tasks and implementation evidence. Larger existing files and localization
files were reviewed at relevant sections/hunks, not fully.

### Findings disposition

| Finding | Current disposition | Evidence and remaining boundary |
|---|---|---|
| SAST-001, originally high | **Resolved** | The guard recognizes actual workspace routes and performs scoped ownership lookup before the filter chain. Foreign-inventory HTTP cases return 403 before task, indicator or storage access; authorized same-workspace exports still succeed. |
| SAST-002, medium | **Mitigated; medium residual remains open** | XLSX cells are explicitly STRING, including formula-like input. The primary UI action selects XLSX, raw CSV needs confirmation, and errors do not fall back to raw CSV. Existing raw API exports deliberately retain formula-capable text. |

The XLSX route retains JWT, inventory ownership and generated read-role
requirements, together with the existing controller's export restrictions.
There is no new anonymous endpoint or user-supplied output path. ZIP names are
used only for sanitized, numbered sheet labels, not filesystem extraction.
The output attachment name uses the numeric inventory identifier.

CSV text is not evaluated, made into a formula or assigned a hyperlink.
The converter strips only the leading artifact BOM, preserves source text,
enforces Excel cell/column/row limits and uses the existing 422 error payload.
The client negotiates XLSX success and JSON errors explicitly. Streaming and
cleanup reduce memory and temporary-file exposure; these are code-review
observations, not measured capacity or operating-system fault-injection tests.
There is no independently verified global uncompressed-byte, sheet-count or
concurrency quota. The source archive is generated by the authorized server
export path, not uploaded directly to this endpoint. Large-archive/parallel
load hardening remains unmeasured; no exploitable high-severity issue was
established from that limitation alone.

Raw CSV remains inappropriate for direct spreadsheet opening. Confirmation
is a UI control, not an API restriction or formal acceptance of every residual
risk. Saving XLSX back to CSV removes its cell-type protection. This warning
does not convert the raw export into a safe spreadsheet format.

### Independent verification

| Runner / selection | Total | Passed | Failures | Errors | Skipped | Exit |
|---|---:|---:|---:|---:|---:|---:|
| Offline Maven / AuthServiceTest | 29 | 29 | 0 | 0 | 0 | 0 |
| Offline Maven / InventorySpreadsheetExportServiceTest | 6 | 6 | 0 | 0 | 0 | 0 |
| Offline Maven / InventoryIndicatorExportSecurityTest | 16 | 16 | 0 | 0 | 0 | 0 |
| Angular / Karma 6.4.4, two frontend specs | 11 | 11 | 0 | 0 | 0 | 0 |

The three backend classes ran in **one 51-test Maven selection**, not three
separate builds. The exact commands were:

```text
mvn -o -f services/backend/pom.xml -Dtest=AuthServiceTest,InventorySpreadsheetExportServiceTest,InventoryIndicatorExportSecurityTest -Dorg.slf4j.simpleLogger.defaultLogLevel=warn -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugin.compiler=info -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugin.surefire=info -Dmaven.test.redirectTestOutputToFile=true -DtrimStackTrace=true test
npm --prefix services/frontend run test:ci -- --include=src/app/core/service/data/footprint-data.service.spec.ts --include=src/app/layout/inventories-footprint/header/inventories-header-footprint.component.spec.ts --reporters=dots
```

Maven/Java versions were not re-queried for this rerun. Workbook tests inspect
cell types and exact text, including formula-like prefixes, control/leading
whitespace, decimals and quoted separators/newlines; they also check
multi-artifact exports, the streaming window and cell/column-limit errors.
The million-row boundary is checked in source, not exercised by a million-row
test. HTTP tests use real security logic with mocked persistence/storage, not a
deployed database. Frontend tests exercise primary XLSX selection, raw
confirmation, exact Accept negotiation and no unsafe fallback. Their
partial-selection coverage is not a project coverage-gate measurement.

Earlier 71-test backend runs, frontend lint/build and Hugo results below are
historical implementation evidence, not additional tests run in this
reassessment. No full test suite, dependency advisory audit, DAST, PMD or
backend Sonar rerun is claimed.

### Execution and publication boundary

Local file/diff tools reviewed the explicit scope; offline Maven and the
existing Angular/Karma runner supplied supporting regressions. Karma used a
local headless browser, not a deployed application. Native hooks checked the
scope manifest for publication. No source, test, dependency manifest or
infrastructure file was changed during this reassessment, and no scanner,
dependency installation, external MCP service, production data or credential
was used. The declared report is the only repository deliverable edited.

The native engine remains authoritative for station registration. Passing
this bounded SAST criterion neither approves a release nor marks the parent
quality workflow complete. Dependency auditing, coverage, DAST and aggregate
quality remain separate stations; Java PMD is deferred and the external
backend Sonar/build reserve remains open without investigation.

## Historical scope and method - original assessment

The original reviewed revision was **c65fbfc9**, compared with the user-confirmed base **origin/feat/g4it-ia-backbone**, not main. That application diff inventoried **106 changed files** across frontend and backend. This is a historical inventory count, not a claim that every file was fully reviewed or that this entire inventory was rescanned now.

The local security-scan skill directory contains neither a Checkmarx nor a ZAP adapter. The documented agent-review fallback was selected.

Review used local Git diffs, bounded explicit source reads and targeted searches. All 32 changed backend production Java paths were covered at changed-hunk level. For larger existing classes, security-relevant surrounding portions were read, not every line of the whole class.

| Area | Coverage |
|---|---|
| Frontend | Changed runtime TypeScript logic, indicator templates, sunburst grouping/default tooltip, category/model/provider propagation, inventory statistics and dependency/build declarations |
| Backend evaluation | Allocation bounds/finiteness, task/source trace matching, reference/category scoping and parameterized repository operations |
| Backend ingestion/export | Device-only validation, exempt raw values, normalized header duplicate rejection, source/evaluated CSV sinks and actual CSV printer |
| Authorization | Real routes and role-generation template; authentication filter, role lookup and inventory guard; input controller/service/repository and relevant tests |
| Contracts | New usage/trace/category migrations and API/CSV field changes |
| Tests/helpers | Export security/controller, inventory input contract, validator, header and CSV mapper tests inspected as source; local E2E helper and account-parser tests read, never executed |

Review stopped short of an exhaustive repository audit after corroborating the blocking authorization defect. Other changed tests and fixtures, full localization/README content, the full lockfile and the binary workbook were **not exhaustively reviewed**. No workbook extraction or binary verdict is claimed. Foundational agent assets were outside this station's application scope.

## G4IT-48-SAST-001 — Originally high, now resolved — Inventory ownership guard

- **Category**: OWASP A01 Broken Access Control; CWE-639 / CWE-862.
- **Application**: backend.
- **Origin**: **pre-existing**, not introduced by the feature branch.
- **Current disposition**: resolved by the current bounded reassessment and 51-test backend selection above.
- **Confidence**: high for the source defect; deployed reachability and runtime exploitation unverified.
- **Primary location**: [AuthService.java](../../../../services/backend/src/main/java/com/soprasteria/g4it/backend/apiuser/business/AuthService.java#L271), particularly line 274.

### Original evidence before remediation

1. The declared inventory input route in [inventory-input-items.yml](../../../../services/backend/src/main/resources/swagger/greenit/api-in-out/inventory-input-items.yml#L131) uses `/organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/inputs/physical-equipments`. Segment 3 is **workspaces**.
2. [AuthenticationFilter.java](../../../../services/backend/src/main/java/com/soprasteria/g4it/backend/config/AuthenticationFilter.java#L71) resolves roles for the organization/workspace in the URL, then calls the inventory ownership guard.
3. `checkUserRightForInventory` requires segment 3 to equal **organizations**. For the actual route, it returns before invoking `inventoryService.inventoryExists(organization, workspaceId, inventoryId)`.
4. The operation's generated role check is `hasRole('INVENTORY_READ')` or `hasRole('INVENTORY_WRITE')`. The [generation template](../../../../services/backend/src/main/resources/swagger/greenit/templates/api.mustache#L243) adds no inventory-owner condition.
5. [InPhysicalEquipmentInventoryController.java](../../../../services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/controller/InPhysicalEquipmentInventoryController.java#L39) forwards only inventory id to the inventory input service. The [service](../../../../services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/business/InPhysicalEquipmentService.java#L178) reads by inventory id; creation uses `inventoryRepository.findById(inventoryId)`. No organization/workspace predicate was found in the reviewed downstream repository/entity path.
6. Existing [AuthServiceTest.java](../../../../services/backend/src/test/java/com/soprasteria/g4it/backend/apiuser/business/AuthServiceTest.java#L289) inventory-guard fixtures also use **organizations** at segment 3 and therefore do not exercise the real route.

A targeted Git diff of the guard, filter, input controller/service and guard tests against the comparison base exited 0 with no changes, establishing the pre-existing origin. At the original publication retry, before remediation, the guard still contained the mismatched literal.

### Impact

A user with an inventory role in an allowed workspace can satisfy that workspace's role check while supplying an inventory identifier belonging elsewhere. In the reviewed input path, no subsequent ownership check ties the identifier back to the authorized workspace. This creates a cross-workspace read/write authorization risk, including the feature's usage fields.

This does not mean anonymous access is allowed, production JWT signatures are bypassed, or a live breach was observed. It does not imply every indicator endpoint is affected: the changed equipment-indicator service separately obtains its task through a workspace-scoped inventory lookup.

### Required remediation and tests

Recognize the actual route structure and enforce inventory ownership before input reads/writes, or enforce a scoped inventory lookup in the service boundary. Authentication, workspace roles and inventory ownership are separate requirements.

Add negative tests using the real workspace route: an authorized user in workspace A, an inventory in workspace B, and assertions that foreign data is neither read nor changed. Assert that the inventory ownership lookup actually runs for valid route shapes, and retain authorized same-workspace coverage.

At the original assessment, the export-security tests covered JWT presence/validity, roles and workspace membership, but not this mismatched-inventory scenario. They used mocked persistence and were not rerun during that assessment.

### Authorized implementation and validation - 2026-11-27

The user explicitly authorized correcting this blocking authorization defect.
The local change replaces the segment-3 literal with **workspaces**, retaining
the existing organization/workspace/inventory lookup and 403 behavior.
No new authorization policy or API route was introduced.

- [AuthServiceTest.java](../../../../services/backend/src/test/java/com/soprasteria/g4it/backend/apiuser/business/AuthServiceTest.java):
  corrected real-route fixtures; rejected foreign inventory and malformed
  workspace/inventory IDs; verified ownership checks for authorized inventory,
  input and export routes; preserved collection and unrelated-route behavior.
- [InventoryIndicatorExportSecurityTest.java](../../../../services/backend/src/test/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorExportSecurityTest.java):
  a writer authorized in the URL workspace receives 403 for an inventory not
  belonging to it, on both export and equipment-indicator routes. Neither task,
  indicator nor file access occurs. Authorized ZIP delivery remains successful
  and now verifies the ownership lookup.

The identical offline Maven test selection ran first with the new tests and
original guard (**39 tests, 9 assertion failures, no errors or skips, exit 1**),
then after the production fix (**39 passed, no failures, errors or skips,
exit 0**). Final suite counts are **29 AuthServiceTest** and
**10 InventoryIndicatorExportSecurityTest**. The pre-fix HTTP cases returned
500/200 instead of 403; both now reject before downstream access.
The exact command and full red/green counts are recorded in
[implementation-log.md](implementation-log.md).

This verifies the local correction and regression, with real filter/service
security logic but mocked persistence. It is not a deployed exploit test or
an exhaustive backend regression run. **Disposition: implemented and locally
verified; independent security reassessment and native gate update pending
at that implementation checkpoint.** The current reassessment resolves it.
The prior failed assessment is intentionally retained rather than silently
converted to a passing quality gate.

## G4IT-48-SAST-002 — Medium — Raw usage columns reach spreadsheet-facing CSV exports

- **Category**: OWASP A03 Injection; CWE-1236.
- **Application**: backend.
- **Origin**: new export sinks reuse the pre-existing raw string-printing helper.
- **Current disposition**: mitigated by the separate XLSX path; medium residual remains open for raw CSV.
- **Confidence**: high for source-to-CSV propagation; spreadsheet behavior was not executed.
- **Locations**: [InputToCsvRecord.java](../../../../services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/mapper/InputToCsvRecord.java#L93), [ImpactToCsvRecord.java](../../../../services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/mapper/ImpactToCsvRecord.java#L49), [CsvUtils.java](../../../../services/backend/src/main/java/com/soprasteria/g4it/backend/common/utils/CsvUtils.java#L137).

The nullable usage fields are strings. [PhysicalEquipmentUsageValidator.java](../../../../services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/business/asyncloadservice/checkobject/PhysicalEquipmentUsageValidator.java#L35) deliberately permits arbitrary values for non-Device equipment. Source exports append both strings verbatim; evaluated non-Device exports also retain raw values. `CsvUtils.print(String)` returns the original non-null string. The reviewed CSV printer provides CSV quoting, but no spreadsheet-formula neutralization.

A harmless synthetic value such as `=1+1` in an exempt usage field can therefore reach an exported cell as formula text. A spreadsheet may interpret formula-prefixed cells on opening the export. Import/write access and recipient interaction are prerequisites; no server-side code execution is claimed.

Preserve the required raw-storage and machine-readable round-trip contract. Decide explicitly how spreadsheet-facing exports neutralize formula-capable cells or provide a separate safe format, instead of silently changing stored values. Add focused export tests for formula prefixes and exempt equipment; ordinary CSV quoting is not this protection.

### User-selected separate safe format - 2026-11-27

The user chose **option 1**, retaining raw machine CSV while adding a distinct
spreadsheet-safe export. FR-019 and TASK-024 record that approved scope.
No apostrophe prefix or validation policy was silently added to source values.

The new authenticated inventory spreadsheet endpoint converts the same stored
task archive into an XLSX workbook. Apache POI SXSSF creates every cell with
explicit **STRING** type, not a formula or hyperlink. It does not construct
external links or embed the raw CSV archive. Original cell text is preserved
after removal of the per-artifact header BOM. All CSV artifacts, not just the
two new columns, pass through this text-cell boundary.

The frontend primary action requests XLSX. The separate raw CSV action requires
an explicit EN/FR warning/confirmation explaining the spreadsheet-formula risk.
Existing raw API clients and bytes are unchanged. Both paths retain the existing
ownership, JWT and export-role restrictions. Conversion errors never trigger
an automatic raw-CSV download.

**Measured verification:** the final offline Maven selection passed **71 tests**
with no failures, errors or skips. This includes six workbook cases and sixteen
real HTTP/security-chain cases, plus existing authorization, raw-export and
evaluated-value regressions. Formula-like prefixes, leading whitespace/control
characters, quoted separators/newlines, original decimals, BOM handling and
multiple artifacts retain STRING cell types and text; no hyperlinks/external
links are added. The cell/column limits and actual HTTP 422 error are checked.
Eleven frontend tests, configured lint, production build and Hugo generation
also passed. Exact commands, earlier discovered failures and limits are in
[implementation-log.md](implementation-log.md).
The final exact-client-header recheck additionally passed all sixteen
HTTP/security cases after correcting binary-only Accept to negotiate JSON
errors as well as XLSX success. It asserts the actual 422 JSON status and is
not added to the earlier 71-case total. Frontend tests, lint and production
build passed again on that corrected client.

**Residual risk:** raw CSV is intentionally still unsafe for direct spreadsheet
opening. A UI warning does not prevent existing API consumers from opening it,
and exporting the safe XLSX back to CSV removes the cell-type protection.
This is a user-selected mitigation and format separation, not a complete
elimination of formula-capable raw text or formal risk acceptance.
**Disposition: implemented and locally verified; independent security
reassessment of the mitigation and residual exposure was still required at
that implementation checkpoint.** The current reassessment verifies the
mitigation while retaining the medium raw-CSV finding.

## Other OWASP observations and limits

| Category | Bounded assessment |
|---|---|
| A01 | Originally failed; **SAST-001 now resolved** by the real-route ownership guard and supporting regressions. This is not exhaustive proof of all application authorization paths. |
| A02 | No new cryptographic implementation identified in application deltas. No secret files or environment values inspected; no production-secret scan attested. |
| A03 | SAST-002. New trace deletion uses a named JPA parameter. The new sunburst uses default tooltip generation; inspected installed ECharts markup code encodes names, with no new custom raw-HTML formatter identified. |
| A04 | Device fraction bounds/finiteness checked at evaluation. XLSX cell/column errors tested and row limit reviewed; no global size/concurrency or resource-exhaustion tests performed. |
| A05 | No production authentication/CORS configuration delta identified. Test-only JWT decoder/profile fixtures are not production authentication bypass evidence. |
| A06 | **not established**: no advisory database or registry contacted. Frontend Angular declarations move from the 21.2.12 range to 21.2.25; ESLint tooling and explicit esbuild declaration are added. Backend POM changes are test/coverage profiles, not new dependency versions. CVEs require the separate dependency-audit station. |
| A07 | Existing JWT resource-server and role path inspected, with no live identity-provider verification. The principal finding is authorization, not forged production JWT acceptance. |
| A08 | Manifests inspected; full lockfile provenance, integrity and supply-chain verification not completed. |
| A09 | No production logs or identities processed. E2E helper never executed and no identity artifacts created. |
| A10 | No new outbound URL/client sink identified in reviewed production deltas. Existing external clients and configurable E2E endpoints not exercised. |

No high/critical issue was identified in the frontend changes reviewed; this is not exhaustive frontend certification or a clean automated scanner result.

## Original assessment execution and publication

- The user acknowledged lint continuation and explicitly deferred Java PMD.
- Pre-hooks on a nonempty session scope manifest returned not blocked, no PII/injection findings and policy checks P-02/P-03 passed. This is not a recursive source-content governance scan.
- Two large diff outputs were subsequently read in bounded sections; successful diff commands alone were not treated as source review.
- No scanner installation, dependency download, network request, Maven build, test execution or DAST request was performed. Tests are source evidence only.
- No frontend/backend source, dependency, migration, configuration or CI file was modified.
- Initial report publication was refused by the station-isolation hook. The native engine recorded `failed / blocked-by-hook` and propagated failure to the quality parent. The user then requested retry with the newly selected agent.
- **Historical publication retry (2026-11-26)**: the native file-writing tool created this report, but that tool profile lacked terminal execution. Post-hooks and the native gate update were not executed in that attempt; the state remained `failed / blocked-by-hook`.
- **Completed resumption (2026-11-27)**: execution tools are available. HEAD is still `c65fbfc9`, with no frontend/backend worktree changes. Pre-hooks on the explicit nonempty scope manifest were not blocked; post-hooks exited successfully with **risk 0 (low)**. That score concerns publication/governance risk, not the severity of the application findings. The native engine confirmed **security-sast failed / gate fail**, replacing the former publication blocker. No application vulnerability was corrected or waived.
- This report's publication and post-check results must be distinguished from the technical findings. Do not claim a native gate transition unless the engine confirms it.
- The backend Sonar reserve remains open; frontend Sonar success does not waive SAST-001. No external backend build investigation was attempted.

The source/test execution statements above describe the original assessment
and publication, before the separately authorized remediations. The SAST-001
change touched three Java files and two evidence documents. The subsequent
SAST-002 implementation additionally changed the declared export backend,
OpenAPI, frontend, specs and English documentation, with results itemized above.
Neither implementation updated native workflow state.

## Reassessed gate evaluation

| Criterion | Result |
|---|---|
| Required report | **pass**: reassessment and evidence persisted in this declared report; publication is checked separately by native hooks |
| No high or critical vulnerabilities | **pass** within the bounded review: 0 open high/critical; SAST-001 resolved |
| Raw-CSV spreadsheet risk | **warning**: one medium finding remains open; distinct text-typed XLSX is the viewing path |
| Supporting regressions | **pass**: 51 backend and 11 frontend tests independently rerun, none failed or skipped |
| Automated scanner | **skipped / no local adapter**; documented fallback used, no automated-clean claim |
| All OWASP categories proven safe | **not established**; limitations itemized above |
| Technical station assessment | **passed / gate warning** |

The original **failed / fail** assessment remains in the historical record;
it is superseded technically by this reassessment, not erased or waived.
Native station state must be registered through the engine, never inferred
from this Markdown. Deferred PMD and the backend Sonar reserve remain explicit.
This report does not approve an exception, global quality completion or release.
