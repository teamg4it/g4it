---
title: Code index summary
workflow: brownfield-analysis
trigger: user
date: '2026-11-19'
analysis_id: g4it-on-github
trace_id: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
station: build-codegraph
agent: repository-analyzer
skill: [knowledge-plane, brownfield-codegraph, repo-analysis]
source_root: services/backend, services/frontend
status: passed
inputDocuments:
  - outputs/brownfield/g4it-on-github/repository-inventory.md
  - outputs/brownfield/g4it-on-github/knowledge-plane/kp-state.json
  - .github/skills/knowledge-plane/SKILL.md
  - .github/skills/brownfield-codegraph/SKILL.md
context_manifest: outputs/runs/brownfield-analysis/20261119-162225-g4it-on-github-4d1e4282/
version: '1.0'
changeHistory:
  - date: '2026-11-19'
    description: 'Retry of build-codegraph after the user decision: coverage gate run separately for services/backend and services/frontend; both pass; fresh indexes reused, no rebuild, no --allow-empty.'
    changes:
      - 'Ran verify --cover-source-code with --source-root services/backend and with --source-root services/frontend; both returned allowed: true with no errors.'
      - 'Reused the backend, frontend and schemacrawler indexes unchanged (all fresh); no index deleted.'
      - 'Station status set to passed. Open notes (schemacrawler extra index, settings.xml hash, .angular component) remain undecided.'
  - date: '2026-11-19'
    description: 'Root-project build-codegraph re-run; replaces the services/-rooted 2026-11-16 summary.'
    changes:
      - 'Indexed services/backend and services/frontend as two separate projects in place, with no copy.'
      - 'Rebuilt the leftover schemacrawler index, which was incompatible.'
      - 'Coverage gate under source root "." fails because discovery finds no repositories; station not passed.'
holisticQualityRating: not-rated
overallStatus: not-reviewed
---
<!-- AI_DISCLAIMER v1.0 -->
# Code index summary

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Scope and outcome

**Status: passed. The source-coverage gate passes for both source roots.** Source mode is root-project in place, with no copy and no relocation. Following the user's decision, coverage was verified separately from `services/backend` and from `services/frontend`. Each gate returned `allowed: true` with no errors. The per-repository SQLite code indexes were built with `kp_bootstrap.py index` on 2026-11-19 and reused unchanged in this retry. All three are `fresh`, so no rebuild was needed.

The earlier gate under source root `.` failed with `No repositories found under the source root`, because the workspace root holds no build manifest. That result is superseded by the two per-root gates. `--allow-empty` was not used.

A leftover `services/documentation/schemacrawler` index was rebuilt in the earlier run and is `fresh` in this retry. It is outside the two-project gate scope. It appears only as an extra indexed repository under both source roots and is not a gate target. It is left in place and marked out of gate scope.

The [input inventory](repository-inventory.md) describes one Git repository (`teamg4it/g4it`) with two product units. The two indexes are separate units inside that repository, not separate Git histories.

## Index counts

Counts come from the 2026-11-19 `index` builds, reused unchanged in this retry. Each index is a separate SQLite artifact. The counts are not summed and do not describe a combined graph.

| Repository | Eligible files | Regex symbols | Manifest components | Notes |
|---|---:|---:|---:|---|
| services/backend | 896 | 642 | 1 | `pom.xml` (Maven); 0 internal manifest dependencies |
| services/frontend | 752 | 7,533 | 2 | `package.json` (npm); 0 internal dependencies; plus one generated `.angular` cache component (see exclusions) |
| services/documentation/schemacrawler | 6 | 6 | 0 | extra, outside the two-project scope |

The 2026-11-16 counts (backend 895 files and 642 symbols; frontend 546 files and 467 symbols) are superseded. The reason the frontend symbol count rose to 7,533 is not established.

## Dependency and entity evidence

Bounded `dep-graph` queries on each index returned untruncated results. External manifest entries include build and test tooling, so they are not counted as runtime integrations.

- **Backend (`pom.xml`)**: external entries cover Spring Boot, Keycloak, PostgreSQL, Liquibase, Azure and OpenAPI tooling, plus build plugins. No internal dependency.
- **Frontend (`package.json`)**: external entries cover Angular, PrimeNG, ECharts, keycloak-angular and test tooling. No internal dependency.

Bounded `grep` found `@Component(` decorators under `services/frontend/src/app`. The query was truncated at 60 hits, so no total is given. An anchored `^@RestController` search of the backend returned no hits, so no controller count is taken from this index. The inventory's controller figure was not re-verified in this run.

The relationships below are findings from the inventory's compose and pom evidence. They are not indexed edges. No function-call, inheritance, implementation, or API-call edges were extracted.

| From → to | Finding | Evidence |
|---|---|---|
| Frontend → backend | Inventory API base URL points to the backend on port 8080. | `workspace/docker/docker-compose-all.yml` (`URL_INVENTORY`) |
| Frontend → Keycloak | Keycloak switch and URL; keycloak-angular and keycloak-js dependencies. | Compose environment; `services/frontend/package.json` |
| Backend → PostgreSQL | JDBC datasource; PostgreSQL driver and Liquibase in the build. | Compose (`SPRING_DATASOURCE_URL`); `services/backend/pom.xml` |
| Backend → Keycloak | JWT issuer URI for resource-server validation. | Compose; `services/backend/pom.xml` |
| Backend → NumEcoEval, Boavizta, EcoLogits | Base URLs for impact calculations; NumEcoEval version in the build. | Compose (`NUMECOEVAL_BASEURL`, `BOAVIZTAPI_BASEURL`, `ECOLOGITS_BASEURL`); `pom.xml` |
| Backend → file storage | Local storage path on a named volume. | Compose (`FILESYSTEM_LOCAL_PATH`, `g4it_storage_data`) |

## Index fingerprints

Copied unchanged from the final passing `verify` runs. The `services/backend` and `services/frontend` gate runs returned identical values. Index paths are recorded in `knowledge-plane/kp-state.json`.

| Repository | `index_sha256` | `index_bytes` | `built_at` |
|---|---|---:|---|
| services/backend | `c9df33019d9387f6f5ed178dbe09d07547858e77fe7174bd9dda0981301042fa` | 667648 | `2026-11-19T16:50:42.846084+00:00` |
| services/frontend | `911b6dd653cc1051ae8333b8a9b346f62bcfbb824bc88ca6f43d16b096fd0f43` | 2674688 | `2026-11-19T16:51:02.522033+00:00` |
| services/documentation/schemacrawler (extra) | `39d9a520ef474017d5312e601b0238be8c296c81b87b614a7492b8b628317665` | 40960 | `2026-11-19T16:52:33.849338+00:00` |

## Index scope and exclusions

Applied by the tool's walker, with the same rules for every index:
- **Excluded at any depth:** `node_modules`, `target`, `build`, `dist`, `bin`, `obj`, `.venv`, `venv`, `outputs`, `coverage`, `.git`, `__pycache__`. The `dep-graph` output lists no manifest from `node_modules` or `target`, which supports this.
- **Denied by name:** `.env` files (for example `services/backend/volume_generation/.env`) and key or certificate files. They were not opened.
- **Binary suffixes** (for example images, archives, office files, fonts) are left out.
- **Documentation third-party code** (`services/documentation/public/js/{d3,js-yaml,mermaid}` and the Hugo themes) is not indexed, because `services/documentation` is outside the two-project scope.
- **`.github`:** not present under either service.

Gaps the tool does not close for non-root repositories:
- **Hidden directories are walked.** `services/frontend/.angular/cache/...` adds a generated `package.json` component to the frontend index, so the component count is two instead of one. Excluding it needs a tool change or a per-path skip; `skip` works only per repository. Not decided by the user; recorded as an open note, and the index was not changed.
- **`services/backend/settings.xml` is hashed.** It is not a denied name, so the indexer computes its SHA-256. Its bytes are not stored or parsed. The user asked that this file never be read. The user has not decided how to handle it, so it stays an open note. The file was not opened manually.

No source files were read manually. All evidence came from bounded port queries, index metadata and directory listings.

## Gate self-check, open questions and limits

| Check | Result |
|---|---|
| Gate 1: `verify --analysis-id g4it-on-github --cover-source-code --source-root services/backend` | `allowed: true`; errors `[]`; coverage expected `["services/backend"]`; skipped `{}`; extra indexed `services/documentation/schemacrawler`, `services/frontend`; backend `fresh` |
| Gate 2: `verify --analysis-id g4it-on-github --cover-source-code --source-root services/frontend` | `allowed: true`; errors `[]`; coverage expected `["services/frontend"]`; skipped `{}`; extra indexed `services/backend`, `services/documentation/schemacrawler`; frontend `fresh` |
| Index rebuilds in this retry | None. Backend, frontend and schemacrawler were reused and are all `fresh`. No index was deleted. |
| `--allow-empty` | Not used |
| Earlier gate under source root `.` (previous run) | `allowed: false`; error: no repositories found under the source root. Superseded by Gates 1 and 2. |

**Open notes (not decided by the user; recorded only, nothing changed):**
1. **schemacrawler extra index.** Left in place as indexed. Marked out of gate scope: it is an extra indexed repository under both source roots and is not a gate target.
2. **`services/backend/settings.xml` byte-hash.** The indexer computes its SHA-256 because the file is not a denied name. The file was not opened and its bytes are not stored or parsed. Decision pending.
3. **`.angular` cache component in the frontend index.** A generated `package.json` under `services/frontend/.angular/cache` is counted, so the frontend index shows 2 components instead of 1. No per-path skip is recorded. Decision pending.

**Limits.** There are three separate SQLite code indexes, not a unified cross-repository graph. Extraction is regex-based. The indexes hold file metadata, class and function definitions, and manifest components. The relationships above are static findings, not runtime verification. The shared document graph was not built or queried, and no `verify --graph` was run. Workflow state was not edited.
