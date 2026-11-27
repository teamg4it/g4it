---
workflow: feature-implementation
trigger: user
date: '2026-11-27'
status: review
feature: G4IT-48
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: quality-validation/dependency-audit
agent: quality-validator
skill: dependency-audit
sensitivity: internal
context_retrieval: files
revision: 27a4ddc678e48d78ce3fcc90205956f907b277e0
comparison_base: origin/feat/g4it-ia-backbone
validation_status: passed
gate_result: warning
coverage_status: frontend-lockfile-and-backend-pom-resolved-graph
frontend_status: passed-with-noncritical-findings
backend_status: passed-with-medium-findings
frontend_vulnerability_packages:
  critical: 0
  high: 0
  moderate: 20
  low: 1
  info: 0
  total: 21
backend_vulnerabilities:
  critical: 0
  high: 0
  medium: 3
  low: 0
  unknown: 0
  total: 3
backend_graph:
  root: 1
  direct: 33
  indirect: 215
  total_package_records: 249
scanner: trivy
scanner_version: 0.76.2
advisory_db_version: 2
advisory_db_updated_at: '2026-11-27T07:06:44.674751291Z'
backend_pom_sha256: fc0967f6db389cd05966c166db0af79dc3dc64ef56fd650157ce8bf52f85e2cd
frontend_lock_sha256: cace2e9e7173d14038b9468b9444a351865c0094f43b2693afba720ccead9485
inputDocuments:
  - .github/skills/dependency-audit/SKILL.md
  - .github/workflows/quality-validation.yml
  - .github/agents/quality-validator.agent.md
  - .github/hooks/config/command-allowlist.yml
  - .github/instructions/output-metadata.instructions.md
  - .github/knowledge/governance/schemas/output-metadata.schema.json
  - .github/knowledge/governance/ai-disclaimer-text.md
  - services/frontend/package.json
  - services/frontend/package-lock.json
  - services/backend/pom.xml
context_manifest:
  - path: .github/skills/dependency-audit/SKILL.md
    mode: full
    key_facts:
      - 'Preferred scanner order is Snyk, OWASP Dependency-Check, then Trivy.'
      - 'Known critical dependency CVEs fail the gate; missing scanners yield a skipped result.'
      - 'The declared file-mode output is dependency-report.md.'
  - path: .github/workflows/quality-validation.yml
    mode: full
    key_facts:
      - 'dependency-audit requires a report and has the blocker criterion No known critical CVEs in dependencies.'
      - 'The aggregate quality-report checks that dependency-audit passed; a partial skipped audit is not proof of that condition.'
  - path: services/frontend/package.json
    mode: full
    key_facts:
      - 'The frontend has Angular runtime dependencies, ECharts ^5.5.1 and separate development tooling.'
      - 'esbuild ^0.27.3 and angular-eslint ^21.0.0 are development dependencies.'
      - 'The manifest is private and is not changed by this audit.'
  - path: services/frontend/package-lock.json
    mode: targeted
    key_facts:
      - 'The v3 lockfile resolves ECharts 5.6.0, esbuild 0.27.3, Angular CLI 21.2.31 and angular-eslint 21.4.0.'
      - 'All 917 resolved entries use the public npm registry; targeted checks found no linked, local or Git resolution entries.'
      - 'Selected glob 13.0.7 and minimatch 10.2.6 entries are marked dev.'
  - path: services/backend/pom.xml
    mode: targeted
    key_facts:
      - 'The Java 21 application uses Spring Boot parent 4.0.7, Commons CSV 1.14.1 and Apache POI OOXML 5.4.1.'
      - 'The branch manifest delta changes test and coverage profiles, not dependency versions.'
      - 'Declared version overrides and remediation comments do not establish current transitive CVE coverage.'
  - path: services/backend/pom.xml
    mode: scanner-resolved-graph
    key_facts:
      - 'Trivy 0.76.2 detected one POM result containing 249 package records: the root, 33 direct dependencies and 215 indirect dependencies.'
      - 'The all-severity result contains three MEDIUM CVEs and no HIGH or CRITICAL entry; the separate critical-threshold command exited 0.'
      - 'The matching database was updated on 2026-11-27; no project metadata was uploaded for matching.'
changeHistory:
  - date: '2026-11-27'
    description: 'Created the dependency audit report with separate frontend and backend coverage'
    changes:
      - 'Detected no installed Snyk, Dependency-Check or Trivy executable'
      - 'Audited the frontend lockfile with npm 11.21.3, including development dependencies and then production only'
      - 'Recorded zero critical/high frontend findings, twenty moderate and one low affected-package entries'
      - 'Kept backend counts unknown and the station skipped with warning rather than reporting a complete clean audit'
      - 'Verified current HEAD and unchanged clean dependency manifests after the prior station revision'
  - date: '2026-11-27'
    description: 'Recorded the native mandatory-station constraint without bypassing the gate'
    changes:
      - 'Native registration rejected skipped because dependency-audit is not optional'
      - 'Preserved backend scan status as skipped for unavailable tooling, distinct from detected vulnerabilities'
      - 'Changed the mandatory gate disposition to blocked-missing-scanner; it must not be registered passed'
  - date: '2026-11-27'
    description: 'Reassessed backend dependencies after the user installed Trivy'
    changes:
      - 'Verified Trivy 0.76.2 and downloaded its current public vulnerability database'
      - 'Scanned the current POM resolved graph locally with telemetry, dependency-identification APIs and ignore files disabled'
      - 'Recorded three medium backend CVEs with zero critical/high findings and a successful explicit critical-threshold check'
      - 'Disclosed the unavailable offline Maven inventory and zero-package JAR attempt rather than treating them as successful coverage'
      - 'Superseded the missing-scanner blocker technically with passed / warning; native registration remains engine-owned'
holisticQualityRating: adequate
overallStatus: warning
---
<!-- AI_DISCLAIMER v1.0 -->
# Dependency Audit Report

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Result

- **Tools**: Trivy **0.76.2** for the backend POM resolved graph; npm
  **11.21.3**, audit report format **2**, for the unchanged frontend lockfile.
- **Assessment**: **passed / warning** for the no-known-critical criterion
  within the analyzed dependency graphs. No critical or high finding was
  returned by either scanner. This is not a zero-vulnerability result.
- **Backend**: **3 medium CVEs**, across 248 dependency records plus the
  project root. The explicit critical-threshold command completed with exit 0.
- **Summary**: no critical or high frontend advisories were returned. The full
  frontend graph has **20 moderate** and **1 low** affected-package entries.
  The earlier backend missing-scanner blocker is superseded by the current
  POM scan; historical execution failures are retained below.
- **Sensitivity**: internal; no production data, source code, Java artifacts
  or credentials were submitted to external systems.

The current revision is **27a4ddc678e48d78ce3fcc90205956f907b277e0**.
The three audited/inventoried manifests have no worktree modifications and no
changes relative to the preceding station's HEAD **c65fbfc9**. The comparison
base remains **origin/feat/g4it-ia-backbone**, not main. The audit evaluates the
current frontend lockfile, not just newly introduced dependencies.
The backend POM SHA-256 is
`fc0967f6db389cd05966c166db0af79dc3dc64ef56fd650157ce8bf52f85e2cd`;
the frontend lockfile SHA-256 is
`cace2e9e7173d14038b9468b9444a351865c0094f43b2693afba720ccead9485`.

## Tools and coverage

| Area | Tool / availability | Outcome |
|---|---|---|
| Preferred multi-stack scanner | Trivy **0.76.2**, installed manually by the user after the initial blocker | Version verified; current public advisory DB downloaded successfully |
| Frontend runtime and development graph | Earlier same-day npm audit, lockfile only, scripts disabled | Completed, exit **0**, critical threshold satisfied; lockfile unchanged |
| Frontend production-only advisories | Earlier same-day command with `--omit=dev` | Completed, exit **0**, one moderate ECharts entry |
| Backend Java project graph | Trivy POM analysis, **249 records**, including the root, 33 direct and 215 indirect dependencies | Completed, exit **0**; **3 medium**, no critical/high findings |
| Backend critical threshold | Separate Trivy POM command, same DB, `--severity CRITICAL --exit-code 1` | Completed, exit **0**, one analyzed POM result and zero critical findings |
| Backend independent Maven inventory | Offline `dependency:tree` | **Unavailable**: dependency plugin not cached; no download or build attempted |
| Existing packaged backend JAR | Supplemental `trivy fs` attempt | **Not usable as coverage**: zero language-specific files and no package results |

Initially none of the preferred scanners was available, so npm supplied
frontend-only results. The user subsequently installed the verified official
Trivy binary manually. Trivy is now the available preferred scanner for the
backend. No referenced adapter implementation exists under the local skill;
the installed native CLI is used directly. The frontend was not rescanned
during this backend retry; its unchanged lockfile retains the earlier same-day
npm evidence.

npm reports a graph total of **917** dependencies, including **53 production**
and **864 development** dependencies. Its optional/peer classifications
overlap those counts and are not added to the total. The production-only
response repeats that graph metadata; it does not mean all development
advisories were included in that filtered result. The audit reads manifest
and lockfile metadata, not 917 source trees.

## Findings

### Frontend severity summary

| npm affected-package severity | Full graph | Production-only result |
|---|---:|---:|
| Critical | 0 | 0 |
| High | 0 | 0 |
| Moderate | 20 | 1 |
| Low | 1 | 0 |
| Informational | 0 | 0 |
| Total | **21** | **1** |

These are npm **affected-package counts**, including propagated dependency
effects, not 21 distinct CVEs. Four advisory identifiers appear as underlying
advisory objects. The responses do not supply CVE identifiers, so none are
invented. Exit 0 reflects `--audit-level=critical`, **not zero vulnerabilities**.

### Underlying advisories returned

| ID | Package / locked version | Severity | Reported issue and scope | Reported upgrade boundary |
|---|---|---|---|---|
| GHSA-fgmj-fm8m-jvvx | ECharts **5.6.0** | Moderate, CVSS 6.1 | XSS advisory, CWE-79; appears in the production-only result | npm reports affected versions below **6.1.0** and proposes **6.1.0**, a major upgrade |
| GHSA-g7r4-m6w7-qqqr | esbuild **0.27.3** | Low, CVSS 2.5 | Development-server file-read advisory on Windows, CWE-22; development tooling | npm reports affected versions from **0.27.3** to below **0.28.1** and proposes **0.28.2** |
| GHSA-3g3m-2fjr-6pw4 | glob **13.0.7**, nested development copies | Moderate | Cancellation/resource-consumption advisory, CWE-400; CLI dependency chain | Returned affected range is **>=9.0.0 <=14.0.1**; no safe in-place target was validated |
| GHSA-g3hv-75wg-m249 | minimatch **10.2.6**, nested development copies | Moderate | Pattern-amplification denial-of-service advisory, CWE-405; CLI dependency chain | Returned affected range is **>=7.0.0 <11.0.1**; compatibility of a transitive update was not tested |

Identifiers and ranges above are taken from npm's audit response. Advisory
web pages were **not** opened, and exploitability was not independently
demonstrated. In particular, the ECharts package finding remains open even
though an earlier bounded SAST review found no new custom raw-HTML formatter.
Those are different evidence types; source observations do not waive a known
package advisory.

The 19 additional moderate package entries, beyond ECharts, are:
`@angular-eslint/builder`, `@angular-eslint/schematics`, `@angular/cli`,
`@npmcli/package-json`, `@npmcli/run-script`, `@sigstore/sign`,
`@sigstore/tuf`, `@tufjs/models`, `angular-eslint`, `cacache`, `glob`,
`ignore-walk`, `make-fetch-happen`, `minimatch`, `npm-packlist`,
`npm-registry-fetch`, `pacote`, `sigstore`, and `tuf-js`.
They do not represent 19 independent root advisories.

### Backend CVEs returned by Trivy

| CVE | Package / resolved version | Severity | Reported issue | Fixed version returned |
|---|---|---|---|---|
| CVE-2026-49844 | `org.apache.logging.log4j:log4j-api` **2.25.4** | Medium | Non-finite floating-point values can cause malformed JSON in affected MapMessage serialization paths | **2.25.5, 2.26.1** |
| CVE-2026-6501 | `org.jopendocument:jOpenDocument` **1.3** | Medium | XML external entity restriction issue, CWE-611 | None in the scan response |
| CVE-2026-1180 | `org.keycloak:keycloak-adapter-core` **25.0.3** | Medium | Keycloak OIDC dynamic-client-registration SSRF advisory, CWE-918 | None in the scan response |

These are **three distinct CVE/package findings**, all with severity source
`ghsa`. The all-severity JSON contains no other severity entries. No
`--ignore-unfixed`, severity filter, VEX suppression or repository ignore file
was applied to that scan. The advisory's status `fixed` for Log4j means an
upstream fixed version is known, not that the installed **2.25.4** is fixed.

Applicability is not independently established by package matching. The
jOpenDocument record flags **1.3**, while its description names **1.5**; this
version applicability discrepancy needs review and is not silently suppressed.
The Keycloak advisory describes a server registration feature; detection in
the adapter dependency alone does not demonstrate that this application's
routes expose that feature. The Log4j issue has specific serialization/logging
prerequisites. All three remain open as scanner findings pending review or
authorized remediation, without a claim of a demonstrated deployed exploit.

### Backend method, freshness and limitations

The POM analyzer detected one language-specific result of type **pom** and
**249 package records**: one project root and **248 dependencies**. This
includes the direct POI/CSV libraries, Keycloak and transitive dependencies;
it is not merely a scan of version comments or direct declarations. The
successful POM scan emitted no unresolved-dependency warning.

Trivy downloaded vulnerability database schema **2** from its default public
OCI mirror. `trivy --version --format json` reported:

| Database field | Value |
|---|---|
| UpdatedAt | `2026-11-27T07:06:44.674751291Z` |
| DownloadedAt | `2026-11-27T11:04:54.025110791Z` |
| NextUpdate | `2026-11-28T07:06:44.674750884Z` |

Dependency identification API calls and telemetry were disabled; vulnerability
matching remained local. `--offline-scan` does **not** disable the initial
public advisory database download. No Java-index database download was needed
for the successful POM analysis.

Two supplemental checks were **not** successful evidence:

- Offline Maven `dependency:tree` exited **1** because the configured dependency
  plugin was not cached. Plugin-prefix resolution also emitted other
  unavailable-plugin warnings. No packages were installed, no project build
  ran and no independent Maven tree was obtained. This does not invalidate
  the scanner's resolved graph, but limits independent reconciliation.
- The existing packaged JAR predates this retry. A `trivy fs` invocation on that
  file exited **0** but reported **zero language-specific files** and no package
  results. It is explicitly **not** a clean JAR scan, not a counted dependency
  result, and not proof of deployed-artifact coverage.

The backend verdict rests on the **current POM graph actually resolved by
Trivy**, not those two checks. Offline/local-cache resolution is not an
independently reconciled build or deployed inventory. Test/plugin dependency
coverage, shaded artifacts, OS/container packages and runtime exploitability
are not certified by this report. The external backend build/Sonar issue was
not investigated.

## Commands and execution boundary

Initial installed-tool detection, before manual installation:

```text
command -v snyk dependency-check dependency-check.sh trivy npm mvn
```

Initially only npm and Maven paths were returned; each preferred scanner was
absent. The npm version came from `npm --version`. After the user's manual
installation, `trivy --version` confirmed **0.76.2**.

The two completed frontend commands, retained from the earlier audit, were:

```text
npm --prefix services/frontend audit --package-lock-only --ignore-scripts --include=dev --json --audit-level=critical --fetch-retries=0 --fetch-timeout=60000 --progress=false --update-notifier=false
npm --prefix services/frontend audit --package-lock-only --ignore-scripts --omit=dev --json --audit-level=critical --fetch-retries=0 --fetch-timeout=60000 --progress=false --update-notifier=false
```

Those frontend commands used the package-registry audit exception and no arbitrary
external URL argument. Public dependency identifiers/versions were processed
by the registry audit service. No external MCP tool, advisory-page fetch,
scanner installation during that initial audit, `audit fix`, lifecycle script
or manifest edit was used. Each audit completed within the per-tool timeout
without retry.
Existing non-npm suppression files were not applied to these results.

The completed backend POM commands in this reassessment were:

```text
trivy fs --scanners vuln --pkg-types library --offline-scan --disable-telemetry --skip-version-check --config '' --ignorefile '' --no-progress --parallel 2 --timeout 5m --format json --exit-code 0 services/backend/pom.xml
trivy fs --scanners vuln --pkg-types library --offline-scan --disable-telemetry --skip-version-check --config '' --ignorefile '' --no-progress --parallel 2 --timeout 5m --skip-db-update --severity CRITICAL --format table --exit-code 1 services/backend/pom.xml
```

The first command gathers **all severities** and exits 0 by configuration;
its exit status alone is not the gate. The second explicitly fails on a
critical finding and also exited **0**, with one analyzed POM result and
zero critical entries. Both commands disabled configuration and ignore-file
loading explicitly. No source, dependency manifest or infrastructure file
was modified and no scanner installation was attempted in this retry.

The supplemental commands with unavailable/non-evidence outcomes were:

```text
mvn -o -f services/backend/pom.xml dependency:tree -Dscope=runtime -Dorg.slf4j.simpleLogger.defaultLogLevel=info
trivy fs --scanners vuln --pkg-types library --offline-scan --disable-telemetry --skip-version-check --config '' --ignorefile '' --no-progress --parallel 2 --timeout 5m --skip-db-update --format json --exit-code 0 services/backend/target/backend-2.1.1-SNAPSHOT.jar
```

One public upstream Java-coverage documentation fetch returned **404** and
provided no coverage evidence; no instructions from fetched content were run.
No external MCP tool, remote build, DAST or secret scan was used. No project
source, dependency graph or artifact was sent to an external matching API.
Only public database content was downloaded by the scanner.

Local file/search/Git tools inspected the declared manifests and relevant
rules; native engine calls manage workflow state and publication hooks.
Hooks inspect the session scope manifest and output, not every dependency's
source. Publication/governance risk scores are separate from vulnerability
severities.

## Gate evaluation

| Criterion | Result |
|---|---|
| Required dependency report | **Pass**: this report persists the measured findings and limitations |
| No known critical frontend dependency advisories | **Pass** for npm's returned lockfile evidence: zero critical |
| No known critical backend dependency CVEs | **Pass** for the Trivy-resolved POM graph: zero critical, explicit threshold command exit 0 |
| Noncritical findings | **Warning**: frontend 20 moderate / 1 low affected-package entries; backend 3 medium CVE/package entries |
| Frontend/backend dependency station assessment | **Passed / warning** within the stated graph coverage; not a clean-audit or release claim |

### Historical registration and current disposition

Initially backend scanner absence yielded incomplete technical coverage.
Native registration rejected a skip because this station is mandatory, so
the engine recorded **failed / blocked-missing-scanner**. That historical
outcome represented missing evidence, not a detected critical CVE.

The installed scanner now supplies current POM advisory matching and the
critical threshold passes. This reassessment supersedes that technical blocker
without deleting its history or waiving any finding. Native state is updated
only through the engine after publication checks; report text alone does not
register a transition. The aggregate quality workflow is **not** complete:
coverage, DAST and the aggregate report remain separate stations. No force
flag, definition change or manual state edit is used.

## Follow-up

1. Review the three medium backend CVEs for version applicability and reachable
   features, then remediate through an authorized maintenance change.
   The initial missing-scanner prerequisite is now resolved. Independently
   reconcile the graph or scan a supported current packaged/deployed artifact
   when that evidence is required; the zero-package JAR attempt is not it.
2. Plan an ECharts update with Angular/ngx-echarts and chart behavior/security
   regression checks; npm's suggested major upgrade is not automatically
   applied by this validation station.
3. Address the noncritical development chain advisories in a separate
   authorized dependency-maintenance change. npm proposes a downgrade to
   Angular CLI **16.2.16** for several propagated entries; that suggestion is
   **not** accepted as a safe remediation for this Angular 21 application.
4. Preserve Java PMD deferral, the backend Sonar reserve and all unrelated
   worktree changes. No source or dependency remediation was performed here.
