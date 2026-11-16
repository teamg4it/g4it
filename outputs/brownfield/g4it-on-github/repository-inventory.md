---
title: Repository inventory
workflow: brownfield-analysis
trigger: user
date: '2026-11-16'
analysis_id: g4it-on-github
station: discover-repositories
agent: repository-analyzer
skill: [knowledge-plane, repo-analysis, cobol-inventory]
source_root: services/
inputDocuments:
  - services/documentation/content/3-technical-documentation/2-software_architecture/_index.md
  - services/backend/pom.xml
  - services/frontend/package.json
  - services/frontend/src/constants.ts
  - services/documentation/schemacrawler/README.md
  - services/documentation/schemacrawler/markdown.py
  - services/documentation/README.md
  - services/keycloak/README.md
version: "1.0"
status: validated
changeHistory:
  - date: '2026-11-16'
    description: "Initial station report; compliance metadata and disclaimer normalized from existing findings."
    changes:
      - "Normalized compliance metadata and disclaimer from existing findings."
  - date: '2026-11-16'
    description: "User-requested inventory validation; no quality review performed."
    changes:
      - "Set status to validated and updated trace_id for this authorized station run."
holisticQualityRating: not-rated
overallStatus: not-reviewed
trace_id: b00185bc-fbd9-4285-a4b6-e0acc4ee9f8f
trace_scope: station-local-report
---
<!-- AI_DISCLAIMER v1.0 -->

# Repository inventory

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

Relationships below are static findings from bounded, read-only inspection, not runtime verification.

## Scope and discovery gate

- Selected source: `services/`, used unchanged, in place, relative to `C:\Users\gtemplier\Documents\Git_Repo\ssg-g4it\g4it-on-github`.
- Source preparation was supplied as **ready**. No staging, copying, source edits, indexing, ingestion, graph builds, or workflow-state changes were performed.
- Read-only discovery command: `python .github/skills/knowledge-plane/tools/kp_bootstrap.py repositories --source-root services/`.
- Command completed with **exit code 0**, permission result **allowed**, and discovery status **ready**.
- Exactly **3 expected repository roots**, matching the supplied expectation; **0 problems**.
- Complete eligible-source estimate: **10,500,908 bytes / 1,453 files**. This is the discovery tool's filtered estimate, not the mainframe scanner's all-file count.

## Detected repository units

These are source/indexing units within the selected monorepo subtree, not a claim that each has an independent Git history.

| Root | Classification and responsibility | Stack/build signals | Evidence |
|---|---|---|---|
| `services/backend` | Business backend: load IT inventories and digital services, calculate environmental impacts, and expose results through REST APIs. | Maven; Java 21; Spring Boot 4.0.7; JPA/PostgreSQL; NumEcoEval calculation library; OpenAPI-based API generation. | [E1], [E2], [E3] |
| `services/frontend` | Browser application for inventories, digital services, and impact-result visualization. | npm/Angular CLI; Angular 21; TypeScript 5.9; PrimeNG, ECharts, Elf state management. | [E1], [E4], [E5] |
| `services/documentation/schemacrawler` | Supporting utility that generates grouped database-table documentation and relationship diagrams; not a business API service. | PowerShell entry script; Python report formatter; SchemaCrawler container prerequisite. | [E6], [E7] |

## Supporting areas, not additional expected repositories

| Area | Role | Discovery treatment / evidence |
|---|---|---|
| `services/documentation` | Hugo documentation website with functional and technical material. | Parent content/template areas were noted as not requiring a code repository; the nested SchemaCrawler utility is separately expected. `services/documentation/README.md:1-3`; discovery stdout notes. |
| `services/keycloak` | Supporting product customization for the login experience; not a separate business capability. | Discovery noted this area as not requiring a code repository. Role described at `services/keycloak/README.md:1-3` and [E1], lines 59-62. No access-management deep dive performed. |

## Detectable relationships

| From → to | Relationship | Evidence strength |
|---|---|---|
| Frontend → backend | SPA consumes the product's REST interface for inventory and digital-service workflows. | Architecture documentation [E1], lines 13-22; consumer-side business API path constants [E5]. |
| Backend → PostgreSQL / inventory files | Persists product data and uses file storage for inventory inputs and exports. | Documented architecture [E1], lines 27-28; PostgreSQL runtime dependency [E3]. |
| Backend → NumEcoEval | Environmental-impact calculations use an embedded calculation library; do not model this library as a separate repository. | Manifest declaration [E2]; architecture [E1], line 29. |
| Backend → Boavizta APIs | Cloud-service environmental-impact evaluation. | Documented relationship [E1], line 30; implementation not traversed in this discovery station. |
| SchemaCrawler utility → database metadata → documentation | Produces grouped table descriptions and entity-relationship diagrams. | Usage/prerequisites [E6]; formatter functions `printDiagram`, `printColumnList`, and `printTableList` [E7]. |

## Mainframe evidence and inspection limits

The already supplied scan summary was reused; **no scan was rerun**. The temporary scan JSON has been removed; the figures below are retained from that supplied summary.

The scan records **4,655 files**: archive 4, config 413, diagram 385, doc 456, empty 15, excluded-sensitive 1, script 8, unknown 3,373. Classified ratio: **0.2754**; COBOL code lines: **0**. No typed mainframe classifications are reported. This does not meet the mainframe routing rule; unknown files are not proof of a mainframe estate or proof of its absence.

Manual inspection remained below 50 files per batch and within five directory levels relative to `services/`. Findings cover manifests, selected documentation, frontend route/constants files, and the schema formatter—not a complete implementation or endpoint inventory. Sensitive files were excluded. No code or document graph was built.

## Source evidence

- **[E1]** `services/documentation/content/3-technical-documentation/2-software_architecture/_index.md:13-45` — product purpose, frontend/backend split, data stores, calculation dependencies, single-backend architecture.
- **[E2]** `services/backend/pom.xml:5-16,30-32,129-155` — backend identity, framework/language versions, calculations and web/data dependencies.
- **[E3]** `services/backend/pom.xml:217-229,454-515` — database/migration dependencies and API contract merge/generation.
- **[E4]** `services/frontend/package.json:4-15,19-51,54-70` — build scripts, UI framework/libraries, TypeScript.
- **[E5]** `services/frontend/src/constants.ts:68-92` — consumer-side inventory, digital-service, reference-data, workspace, and sharing API path fragments.
- **[E6]** `services/documentation/schemacrawler/README.md:1-16,52-64` — database-documentation utility, prerequisites, invocation and output.
- **[E7]** `services/documentation/schemacrawler/markdown.py:12-35,46-106` — schema-to-Markdown and Mermaid transformation.
