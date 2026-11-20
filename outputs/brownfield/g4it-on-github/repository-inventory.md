---
title: Repository inventory
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
      - "Replaced the services/-rooted inventory; source root is the workspace root (.)."
      - "Recorded discovery results from kp_bootstrap.py repositories for source roots '.' and 'services/'."
holisticQualityRating: not-rated
overallStatus: not-reviewed
trace_id: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
trace_scope: station-local-report
---
<!-- AI_DISCLAIMER v1.0 -->

# Repository inventory

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

Relationships below are static findings from bounded, read-only inspection, not runtime verification.

## Scope and discovery gate

- Source root: `.` (workspace root), analysed **in place** under the user's "analyse root project in place" choice (2026-11-19). No copy, staging, source edit, indexing or graph build was performed. See `source-staging.md`.
- Read-only discovery, each run from the workspace root with exit code 0:
  - `repositories` (default root `source-code/`): status `needs-staging`; `source-code/` is absent.
  - `repositories --source-root .`: allowed; **0 expected repositories**, 0 problems; status `needs-staging`. The tool notes the workspace root holds no build manifest. No size estimate was produced because nothing was expected.
  - `repositories --source-root services/` (comparison only): **8 expected roots**, 0 problems (listed below).
- The tool checks build manifests only at the root for `.`, so the two manifests under `services/` (`backend/pom.xml`, `frontend/package.json`) are not reported as expected repositories. The units below come from a manual, bounded inventory.
- Complete eligible-source estimate for `.`: **not established**.

## Detected repository units

Manual inventory of the root project. These are indexing and product units inside one Git repository (`teamg4it/g4it`), not independent repositories with their own history.

| Unit | Classification and responsibility | Stack/build signals | Evidence |
|---|---|---|---|
| `services/backend` | **Business service (primary).** REST backend for IT inventories, digital services, impact evaluation, indicators, referential data, workspaces and users. | Maven; Java 21; Spring Boot 4.0.7 parent; PostgreSQL and Liquibase; Keycloak JWT resource server; 38 controller classes. | `services/backend/pom.xml` (parent, properties, dependencies); `services/backend/README.md`; controller paths under `src/main/java` |
| `services/frontend` | **Business client (primary).** Angular single-page application for inventories, digital services, footprint dashboards, version comparison, administration and sharing. | npm; Angular 21; TypeScript 5.9; PrimeNG; ECharts; keycloak-angular; about 89 component files under `src/app/layout/`. | `services/frontend/package.json`; `services/frontend/README.md`; `src/app/layout/` listing |
| `services/documentation/schemacrawler` | **Supporting tool.** Generates per-table-group Markdown documentation from a database schema crawl. Not a business service. | PowerShell entry script; Python formatter; SchemaCrawler Docker image prerequisite. | `services/documentation/schemacrawler/README.md` |
| `services/documentation` | **Supporting content.** Hugo documentation website (functional, technical, getting-started and release material). | Hugo (`hugo.toml`); 216 Markdown files under `content/`; theme clones from external Git sources. | `services/documentation/README.md`; content listing |
| `services/keycloak` | **Supporting identity component.** Keycloak image and realm export for user authentication. No build manifest. | Dockerfile; GitLab CI; README. Realm export not read. | `services/keycloak/README.md`; file listing |
| `workspace/docker` | **Deployment composition, not a repository.** Compose definitions for the local stack. | Docker Compose v3. | `workspace/docker/docker-compose-all.yml` (read); `shared-docker-compose.yml` (listed, not read) |
| `workspace/ecomind-api` | **Sample payloads.** Two JSON output samples, not code. | JSON | File listing only; not opened |
| `tools/repo-graph` | **Harness tooling** (APM-managed knowledge-plane graph tools). Outside product scope. | Templates and manifests; `.apm-managed.json` present. | File listing only |

Root-level files (`README.md`, `DOCKER-DEPLOYMENT.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `LICENSE.txt`, `NOTICE.txt`, `allow-ci-failure.yml`, `hook-config.json`, `dev-realm-export.json`) are project documentation and configuration. `dev-realm-export.json` was not opened.

### Backend business capability areas (from controller packages)

| Package (under `services/backend/src/main/java/com/soprasteria/g4it/backend/`) | Capability |
|---|---|
| `apiinout` | Input and output data for applications, datacenters, physical and virtual equipment, and AI services (9 controllers). |
| `apiinventory`, `apiloadinputfiles` | Inventory management and CSV file loading with validation. |
| `apidigitalservice`, `apirenewservice`, `apisharedlink`, `apibusinesshours` | Digital services and versions, renewal, sharing links, business-hours reference. |
| `apievaluating`, `apiindicator` | Impact evaluation runs and indicator results. |
| `apireferential` | Referential import and export, workspace-specific referential data. |
| `apiworkspace`, `apiuser`, `apiadministrator`, `apiadministratoractions` | Workspaces, users and roles, organizations, release notes, administrator actions. |
| `apiaiservice`, `apiaiinfra`, `apiparameterai`, `apirecomandation` | AI-service estimation, AI infrastructure, AI parameters and recommendations. |
| `apifiles` | File system and template-file access. |

Excluded from the business view as technical: `apiversion` (version info) and `common/task` (task-status controller).

### Frontend business areas (from `src/app/layout/`)

Inventories and their footprint views; digital services, footprint (cloud, networks, servers, terminals, ecomind parameters), version management and comparison; administration panel (users, organizations, referential updates, release notes); shared digital-service landing page; welcome, landing and declarations pages.

## Repositories by comparison scan (`services/` root, comparison only)

| Expected root | Classification |
|---|---|
| `services/backend` | Business service |
| `services/frontend` | Business client |
| `services/documentation/schemacrawler` | Supporting tool |
| `services/documentation/public/js/d3`, `js-yaml`, `mermaid` | Third-party JavaScript libraries inside the documentation site output (name-based; not opened) |
| `services/documentation/themes/hugo-theme-relearn`, `hugo-video` | Third-party Hugo themes (the documentation README says they are cloned from external Git sources) |

Not reported as expected: `services/keycloak` and `services/documentation` (no build manifest).

## Detectable relationships

Static findings from manifests, environment settings in `workspace/docker/docker-compose-all.yml`, and file names. None is verified at runtime.

| From → to | Relationship | Evidence strength |
|---|---|---|
| Frontend → backend | Container sets the inventory API base URL to the backend on port 8080. | Compose `g4it-frontend` environment (`URL_INVENTORY`). |
| Frontend → Keycloak | Frontend has a Keycloak switch and URL; `keycloak-angular` and `keycloak-js` are dependencies. | Compose environment (`KEYCLOAK_ENABLED`, `KEYCLOAK_URL`); `package.json`. |
| Backend → PostgreSQL | JDBC datasource to the `postgresdb` service; PostgreSQL driver and Liquibase in the build. | Compose (`SPRING_DATASOURCE_URL`); `pom.xml` properties. |
| Backend → Keycloak | JWT issuer URI for resource-server validation; Keycloak adapter BOM and starter declared. | Compose (`SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`); `pom.xml` properties. |
| Backend → NumEcoEval APIs | Base URLs for the NumEcoEval referential and data-entry APIs; NumEcoEval version declared in the build. | Compose (`NUMECOEVAL_BASEURL`, `NUMECOEVALREFERENTIAL_BASEURL`); `pom.xml` (`numecoeval.version`). |
| Backend → Boavizta and EcoLogits APIs | Base URLs for cloud and AI impact calculations. | Compose (`BOAVIZTAPI_BASEURL`, `ECOLOGITS_BASEURL`). |
| Backend → EcoMind AI | AI model configuration class exists; endpoint not traversed. | `external/ecomindai/ConfigAiModel.java` (file name only). |
| Backend → file storage | Named volume mounted at `/app/storage`; local storage path configured. | Compose (`g4it_storage_data`, `FILESYSTEM_LOCAL_PATH`). |
| Backend → email | Azure Communication Email version declared; email service class exists. | `pom.xml` property; `common/utils/AzureEmailService.java` (file name only). |
| Schemacrawler tool → database | Reads the database schema using `db.properties.json` and produces Markdown. | `services/documentation/schemacrawler/README.md`. |

## Stack routing (mainframe check, bounded)

- This station did not run the mainframe scanner. The bounded check in this run globbed `*.cbl`, `*.cob`, `*.cpy` and `*.jcl` (any case) under `services/`, `workspace/`, `tools/` and the root. It found **no matches**. Root-level manifests were searched too; none are COBOL or mainframe build files.
- The cobol-inventory tool was not run. A negative result from this bounded search does not prove the absence of mainframe code anywhere in the tree.
- **Detected stack family: mixed** (Java/Spring Boot backend, Angular frontend, Hugo/Python/PowerShell tooling, Docker Compose). Not mainframe.

## Mainframe evidence and inspection limits

Bounded inspection only: under 50 files per batch and no deeper than five directory levels. Findings cover manifests, selected READMEs, controller and package listings, and the compose file. They are not a complete endpoint inventory or implementation review. No index, graph or workflow state was changed.

## Source evidence

- **[E1]** `services/backend/pom.xml` — parent `spring-boot-starter-parent` 4.0.7, group/artifact `com.soprasteria.g4it:backend`, description, and properties (Java 21, PostgreSQL, Liquibase, Keycloak, NumEcoEval, Azure email).
- **[E2]** `services/frontend/package.json` — name `frontend`, version 2.1.1, npm scripts, Angular 21 and TypeScript 5.9 dependencies, keycloak-angular and keycloak-js.
- **[E3]** `services/backend/README.md` — Spring Boot start, Java 21, Maven, PostgreSQL requirement.
- **[E4]** `services/frontend/README.md` — Angular start, mocked-backend option, PrimeNG and ECharts usage.
- **[E5]** `services/keycloak/README.md` — Keycloak role for authentication; realm export and image upgrade process.
- **[E6]** `services/documentation/README.md` — Hugo site and external theme clones.
- **[E7]** `services/documentation/schemacrawler/README.md` — database-documentation utility, prerequisites and output.
- **[E8]** `workspace/docker/docker-compose-all.yml` — service topology and environment-based integration URLs.
- **[E9]** `README.md` — product purpose and feature summary.
