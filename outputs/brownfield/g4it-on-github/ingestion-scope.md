---
title: Document ingestion scope
workflow: brownfield-analysis
trigger: user
date: '2026-11-19'
analysis_id: g4it-on-github
station: ingest-documents/scope-documents
skill: [ingestion-triage]
status: approved
inputDocuments:
  - .github/skills/ingestion-triage/SKILL.md
  - .github/skills/ingestion-batching/SKILL.md
  - outputs/brownfield/g4it-on-github/repository-inventory.md
  - .github/hooks/engine/source_ingestion.py (resolve_source_root and REPOSITORY_EXCLUDED_DIRECTORIES only)
  - outputs/ingestion/g4it-on-github/ingestion-params.json
changeHistory:
  - date: '2026-11-19'
    description: "Scope-documents re-run for the root-project analysis in place (trace 4d1e4282)."
    changes:
      - "Replaces the 2026-11-16 scope. Source root is the workspace root (.); include list is re-derived from repository-inventory.md."
      - "Superseded values from the 2026-11-16 file are not carried over: batch_size 10, max_file_size_mb 50, the supported_formats list (yaml, json, xml, java, ts), parallel_workers 3, and the .github/ source."
      - "Proposed parameters are recorded in ingestion-params.json with status proposed-pending-user-approval."
  - date: '2026-11-19'
    description: "User checkpoint decisions recorded; parameters approved (trace 4d1e4282)."
    changes:
      - "Approved: the five include directories, the 10-area grouping and batch_size 100. ingestion-params.json status changed to approved."
      - "Accepted as an explicit uncovered gap: root Markdown (README, CHANGELOG, CONTRIBUTING, DOCKER-DEPLOYMENT) and services/documentation/README.md. No root scope added."
      - "Confirmed out of document scope: workspace/docker, workspace/ecomind-api, tools/repo-graph."
      - "No legacy-encoded documents; legacy_codepage stays null and --legacy-codepage is not passed."
      - "Stale 2026-11-16 copy outputs/ingestion/g4it-on-github/ingestion-scope.md deleted by the orchestrator at user request; verified absent before this record."
holisticQualityRating: not-rated
overallStatus: approved
trace_id: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
context_manifest:
  - source-code (root-project analysis in place, user-consented 2026-11-19; third-party and secret-bearing paths excluded)
  - ingestion-parameters (approved by the user 2026-11-19; defaults derived from the repository, adjusted at checkpoint: none)
---
<!-- AI_DISCLAIMER v1.0 -->

# Document ingestion scope

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Scope status

**Approved by the user, 2026-11-19.** The checkpoint decisions below are recorded. The parameters are fixed for
triage. No triage, conversion, batching or extraction was run in this station. Counts come from read-only file
listing, not from `triage.py scan`, and will change once triage runs.

## Derived parameters (approved)

| Parameter | Approved value | Basis |
|-----------|----------------|-------|
| `mode` | `nested` | Station id carries the `ingest-documents/` prefix. |
| `source_root` | `.` (workspace root, analysed in place) | `repository-inventory.md`. The engine accepts the workspace root as source root (`resolve_source_root`: `resolved == root` is allowed). |
| `output_root` | `.` (workspace root) | Always the workspace. |
| `include` | 5 directories (below) | Approved 2026-11-19. |
| `batch_size` | `100` | Approved 2026-11-19 (allowed 1 to 1,000). |
| `grouping` | `area-map`, 10 areas (below) | Approved 2026-11-19. |
| `legacy_codepage` | `null` (none) | User confirmed 2026-11-19: no legacy-encoded documents. `--legacy-codepage` is not passed. |
| `analysis_id` | `g4it-on-github` | Brownfield run scope id. |
| Size limits | Engine defaults | Above 20 MiB: exclude only. Estimated text above 2,000,000 characters: split. Above 500 PDF pages: exclude. Declared XLSX dimension above 20,000 cells: include. Native batch limit 1,000 sources per request. |

Parameters file: `outputs/ingestion/g4it-on-github/ingestion-params.json` (status `approved`).


### Include directories (approved)

| Directory | Repository unit | Known contents (listing only) |
|-----------|-----------------|-------------------------------|
| `services/documentation/content` | Hugo documentation content | 233 files: 216 `.md`, 8 `.csv`, 5 `.pptx`, 4 `.xlsx`. Sections: getting-started 58, functional 84, technical 52, help 10, releases 28, section index 1. |
| `services/backend` | Business service | Not counted. Expected: `README.md` plus any document files. Java source is not expected to be a candidate document. |
| `services/frontend` | Business client | Not counted. Expected: `README.md` plus any document files. |
| `services/keycloak` | Identity component | Not counted. Expected: `README.md`. The realm export is sensitive and is never opened. |
| `services/documentation/schemacrawler` | Supporting tool | Not counted. Expected: `README.md`. Lies inside `services/documentation` but is reached only by this entry. |

Derivation note: the inventory lists eight units. Four are not included as whole units, for these reasons.
- `services/documentation` is replaced by its `content` subtree. That keeps out the third-party JavaScript and Hugo themes, and the build output under `public/`, which the context excludes by construction.
- `workspace/docker` is a compose definition, not a repository.
- `workspace/ecomind-api` holds two JSON output samples, not documents.
- `tools/repo-graph` is harness tooling outside product scope.

### Area map (approved grouping)

| Prefix (relative to `source_root`) | Batch prefix | Files (listing) |
|------------------------------------|--------------|-----------------|
| `services/documentation/content/1-getting-started` | `getstarted` | 58 |
| `services/documentation/content/2-functional-documentation` | `functional` | 84 |
| `services/documentation/content/3-technical-documentation` | `technical` | 52 |
| `services/documentation/content/4-help` | `help` | 10 |
| `services/documentation/content/5-releases` | `releases` | 28 |
| `services/documentation/content` (section index) | `docs` | 1 |
| `services/backend` | `backend` | not counted |
| `services/frontend` | `frontend` | not counted |
| `services/keycloak` | `keycloak` | not counted |
| `services/documentation/schemacrawler` | `schemacrawler` | not counted |

Unmatched paths go to `other`. Each area holds at most 100 sources at the approved batch size, so the five content sections
should each plan as one batch; `backend`, `frontend`, `keycloak` and `schemacrawler` are not counted and may split further.

### Exclusion proposals (for triage; nothing decided)

Exclusions are recorded later with `triage.py decide`. These are proposals for triage to present, not decisions.

- **Third-party and themes, out of scope by construction.** `services/documentation/public/js/d3`, `js-yaml`, `mermaid`, and the Hugo themes (`hugo-theme-relearn`, `hugo-video`). No include entry reaches them.
- **Sensitive, never opened.** Key or certificate files, `.env*`, credential or secret files, `settings.xml`, and Keycloak realm exports (including `dev-realm-export.json` at the root). Triage lists them under `sensitive`.
- **Office owner lock file.** `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/media/~$screenshot.pptx`. Proposed exclude, reason "Office owner lock file, not content". Triage decides.
- **Engine walk rules.** `.git`, `node_modules`, `target`, `dist`, `build`, `venv`, `outputs`, `.github` and similar trees are never walked.
- **Path heuristics.** Folders such as `test-data`, `fixtures`, `vendor`, `archive`, `samples` are recommended as `path-heuristic` exclusions by triage. Triage presents them.

### Not in scope (decided 2026-11-19)

- **Root Markdown** (`README.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `DOCKER-DEPLOYMENT.md`) and **`services/documentation/README.md`**.
  **Decision:** accepted as an explicit uncovered gap. No root-level scope is added. These files are not extracted, and
  the catalogue must state the gap.
- **`workspace/docker`**, **`workspace/ecomind-api`**, **`tools/repo-graph`**. **Decision:** confirmed out of document scope.
- **`.github/**`** (harness and SDLC skill documents). Outside product scope and never walked by the engine.

### Ingestion Categories

- **Technical Documentation**: architecture files, design docs, READMEs
- **Functional Documentation**: use cases, user journeys, release notes and getting-started material
- **Configuration and operations**: deployment, maintenance and how-to content in the documentation site

## Checkpoint decisions (recorded 2026-11-19)

The user approved the proposal with these answers. They are authoritative and were applied as follows.

1. **Approved:** the five `include` directories, the 10-area grouping and `batch_size` 100.
2. **Root Markdown and `services/documentation/README.md`:** accepted as an explicit uncovered gap. No root scope added.
3. **`workspace/docker`, `workspace/ecomind-api`, `tools/repo-graph`:** confirmed out of document scope.
4. **Legacy codepage:** none. No legacy-encoded documents. `legacy_codepage` stays `null`.
5. **Stale duplicate:** `outputs/ingestion/g4it-on-github/ingestion-scope.md` (2026-11-16) was deleted by the orchestrator at user request. Its absence was verified before this record.

`ingestion-params.json` now has status `approved`. Triage may run against these parameters.

## Attention Points

- **Source root is the workspace root.** The engine accepts it, but the walk never enters `outputs/`, `.github/`, `.git/` or `node_modules/`. Triage must confirm the scan result, and the `include` paths must stay inside the workspace.
- **Counts are listing counts only.** The 233 Hugo content files come from a file listing. Triage categories, sizes and recommendations are not yet known. Backend, frontend, keycloak and schemacrawler are not counted.
- **Sensitive material in scope roots.** `services/keycloak` holds a realm export, and `services/backend` may hold settings or credential-style files. Triage must list these as `sensitive`. They are never opened, converted or summarised.
- **Superseded earlier values.** The 2026-11-16 file set `batch_size` 10, a 50 MB limit, a format list including `java`, `ts` and `yaml`, `parallel_workers` 3, and `.github/` as a source. None of these is carried over. `.github/` is outside product scope and is excluded by the engine.
- **Stale duplicate, removed.** The 2026-11-16 copy `outputs/ingestion/g4it-on-github/ingestion-scope.md` was deleted by the orchestrator at user request. Only `ingestion-params.json` remains in that folder. Do not recreate it.
- **Explicit uncovered gap.** Root Markdown (`README.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `DOCKER-DEPLOYMENT.md`) and `services/documentation/README.md` are outside every include path. The user accepted this. The document catalogue must list them as an explicit gap, not as covered.
- **Lock file.** `~$screenshot.pptx` is an Office owner lock file. It is proposed for exclusion, pending triage.

## Production confidence

- **Include list (content):** high. It is consistent with the repository inventory and the file listing, and the user approved it.
- **Include list (backend, frontend, keycloak, schemacrawler):** medium. The contents are not yet counted. Triage will confirm.
- **Grouping and batch size:** approved by the user. Their fit to the real counts is still medium until triage runs.
- **Overall:** parameters approved 2026-11-19. `overallStatus` is `approved`. Triage counts, categories and the exclusion proposals are still unverified.
