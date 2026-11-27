---
title: Repository classification
analysis_id: g4it-on-github
station: discover-repositories
source_root: .
stack_family: mixed
generated_on: 2026-11-19
generated_by: repository-analyzer
workflow: brownfield-analysis
trigger: user
date: '2026-11-19'
skill: [knowledge-plane, repo-analysis]
inputDocuments:
  - .github/skills/knowledge-plane/SKILL.md
  - .github/skills/repo-analysis/SKILL.md
  - services/backend/pom.xml
  - services/frontend/package.json
  - services/backend/README.md
  - services/frontend/README.md
  - services/keycloak/README.md
  - services/documentation/README.md
  - services/documentation/schemacrawler/README.md
  - README.md
  - workspace/docker/docker-compose-all.yml
version: "1.0"
status: complete
changeHistory:
  - date: '2026-11-19'
    description: "Discover-repositories re-run for root-project analysis in place."
    changes:
      - "Replaced the services/-rooted classification with a root-project classification from this run's evidence."
      - "Removed claims from the earlier run that relied on a supplied scan not re-verified here."
holisticQualityRating: not-rated
overallStatus: not-reviewed
trace_id: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
trace_scope: station-local-report
---

<!-- AI_DISCLAIMER v1.0 -->

# Repository classification

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

Classification from static evidence; subject to human review. Declared dependencies and documented integrations are not proof of runtime usage.

## Overall classification

**Mixed-stack product monorepo (root project, analysed in place):** a Java/Spring Boot business backend, an Angular business client, and supporting tooling (Hugo documentation, a schema-documentation utility, a Keycloak identity image and Docker Compose). Stack family: **mixed**, not mainframe.

| Detected unit | Class | Stack family | Decisive signals |
|---|---|---|---|
| `services/backend` | Business REST service | `java` | `pom.xml`: Java 21, Spring Boot 4.0.7 parent, PostgreSQL, Keycloak adapter; 38 controller classes. |
| `services/frontend` | Business SPA client | `node` | `package.json`: Angular 21, TypeScript 5.9, PrimeNG, ECharts, keycloak-angular. Runs in the browser. |
| `services/documentation/schemacrawler` | Supporting documentation utility | `python` with PowerShell | README: PowerShell entry script, Python formatter, SchemaCrawler Docker prerequisite. Not a product service. |
| `services/documentation` | Supporting content (Hugo site) | `hugo` / Markdown | `hugo.toml`; 216 Markdown files under `content/`. Not an expected repository. |
| `services/keycloak` | Supporting identity component | `container` | Dockerfile, GitLab CI, realm export (not read). No build manifest. Not an expected repository. |
| `workspace/docker` | Deployment composition | `docker-compose` | `docker-compose-all.yml` (read). Not a repository. |

`mixed` reflects several distinct runtime and tooling stacks. It is not a source-line majority calculation. Discovery for root `.` returned no expected repositories, so these classes come from manual inspection.

## Business and relationship signals

* **Business purpose:** the root README describes G4IT as a platform for assessing, defining and managing plans to reduce digital-technology impact at organisational scale, including IS component views, impact assessments and plan management (`README.md`).
* **Backend capability packages:** inventory input/output, digital services, referential data, indicators and evaluation, workspaces/users/administration, AI services, and files. Detail is in `repository-inventory.md`.
* **Frontend → backend:** the compose file sets the frontend's inventory API URL to the backend (`workspace/docker/docker-compose-all.yml`, `g4it-frontend`).
* **Backend runtime dependencies (compose):** PostgreSQL datasource; Keycloak JWT issuer; NumEcoEval, Boavizta and EcoLogits base URLs; file-storage volume (same compose file).
* **Build-time signals:** NumEcoEval and Azure Communication Email version properties in `services/backend/pom.xml`. These are declarations, not verified live calls.

## Stack routing (mainframe check)

This station did not run the mainframe scanner (it belongs to the separate `mainframe-inventory` station), and no figures from any earlier scan are used here.

| Routing evidence | Finding |
|---|---|
| Scope of this run | Bounded globs for `*.cbl`, `*.cob`, `*.cpy` and `*.jcl` (any case) under `services/`, `workspace/`, `tools/` and the root: **no matches**. Root-level manifests contain no COBOL or mainframe build files. |
| Skill threshold | Requires at least one COBOL program, or both a JCL file and a copybook. **Not met** by the bounded check. |
| Routing result | `stack_family: mixed`. This evidence does not indicate mainframe-specific stations. |

A negative bounded search does not prove the absence of mainframe code anywhere in the tree.

## Evidence quality and gate

- **Discovery:** `kp_bootstrap.py repositories --source-root .` returned allowed, 0 expected repositories and 0 problems (status `needs-staging`). The comparison run with `--source-root services/` returned 8 expected roots, including third-party JavaScript and Hugo theme folders. The root-project classification is therefore manual.
- **Source staging:** none. In-place analysis under user consent of 2026-11-19 (`source-staging.md`).
- **Exclusions applied:** `outputs/`, `node_modules`, `.venv`, `target`, `build`, `dist`, `.github` and `.git`. Sensitive and credential-like files were not opened, including `services/backend/settings.xml` and the Keycloak realm exports.
- **Scope boundary:** bounded manual inspection (under 50 files per batch, no deeper than five directory levels). No index, graph, endpoint or runtime verification, and no workflow-state update.
- **Open point:** root-level discovery returns 0 repositories. `build-codegraph` needs an explicit decision on whether to index `.` as one project or per service root (`services/backend`, `services/frontend`).