---
title: Source staging record
workflow: brownfield-analysis
trigger: user
date: '2026-11-19'
analysis_id: g4it-on-github
station: discover-repositories
agent: repository-analyzer
skill: [knowledge-plane, repo-analysis]
source_root: .
inputDocuments:
  - .github/skills/knowledge-plane/SKILL.md
  - outputs/runs/brownfield-analysis/20261119-162225-g4it-on-github-4d1e4282/workflow-state.md
version: "1.0"
status: complete
changeHistory:
  - date: '2026-11-19'
    description: "Discover-repositories station: root-project analysis in place, no staging."
    changes:
      - "Recorded user consent for in-place root analysis; no copy performed."
holisticQualityRating: not-rated
overallStatus: not-reviewed
trace_id: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
trace_scope: station-local-report
---
<!-- AI_DISCLAIMER v1.0 -->

# Source staging record

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Decision

| Field | Value |
|---|---|
| Mode | `root-project` (analyse in place) |
| Source root | `.` (workspace root), used unchanged |
| Staging target | None. No copy was made and no staging directory was created. |
| Staging command executed | None. |
| User consent | Obtained 2026-11-19. The user chose "analyse root project in place" after `repositories` returned `needs-staging`. |
| Trace ID | `4d1e4282-cc4b-4ea6-83da-14dd38c0b892` |

## Why staging was not needed

`source-code/` is absent. The workspace root holds no build manifest of its own. The build manifests sit one level down, at `services/backend/pom.xml` (Maven) and `services/frontend/package.json` (npm). The user chose to analyse the root project in place rather than copy it.

## Read-only discovery invocations

All three ran `kp_bootstrap.py repositories` from the workspace root. Each exited with code 0. None copied, moved or wrote source content.

| Invocation | Result |
|---|---|
| `repositories` (default root) | `allowed: true`, `source_root: source-code`, status `needs-staging`, note "source-code: directory not found" |
| `repositories --source-root .` | `allowed: true`, `source_root: .`, status `needs-staging`, 0 expected repositories, 0 problems, note "the workspace root holds no build manifest" |
| `repositories --source-root services/` (comparison only) | `allowed: true`, 8 expected roots, 0 problems. The roots are listed in `repository-inventory.md`. |

No size estimate was produced for `.` because no repository was expected there. The completeness of the source tree is therefore not established by the tool.

## Exclusions from code analysis

Excluded from code analysis: `outputs/`, `node_modules`, `.venv`, `target`, `build`, `dist`, `.github`, and `.git`.

Sensitive files were not opened. The check covered `.env` and `.env.*`, `*.pem`, `*.key`, `*.p12`, `*.pfx`, `*.p8`, `.aws/*`, `.ssh/*`, `secrets/*`, `credentials/*`, `credentials.json`, `secrets.json`, and `appsettings.*.json`. Two files outside that list were also not opened: `services/backend/settings.xml`, which may carry local build settings, and the Keycloak realm exports (`dev-realm-export.json` and `services/keycloak/imports/`).

## Repositories found

Per the tool at root `.`: 0 expected repositories. Units identified by manual inspection are listed in `repository-inventory.md`.
