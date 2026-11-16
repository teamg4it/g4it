---
title: Code index summary
workflow: brownfield-analysis
trigger: user
date: '2026-11-16'
analysis_id: g4it-on-github
trace_id: 8245daf0-c908-463d-8835-75cf5cb5f160
station: build-codegraph
agent: repository-analyzer
skill: [knowledge-plane, brownfield-codegraph, repo-analysis]
source_root: services/
status: validated
inputDocuments:
  - outputs/brownfield/g4it-on-github/repository-inventory.md
  - outputs/brownfield/g4it-on-github/knowledge-plane/kp-state.json
  - services/documentation/content/3-technical-documentation/2-software_architecture/_index.md
  - services/backend/pom.xml
  - services/frontend/src/constants.ts
  - services/documentation/schemacrawler/README.md
  - .github/skills/knowledge-plane/SKILL.md
  - .github/skills/brownfield-codegraph/SKILL.md
  - .github/skills/repo-analysis/SKILL.md
  - .github/knowledge/governance/ai-disclaimer-text.md
version: '1.0'
changeHistory:
  - date: '2026-11-16'
    description: 'Initial build-codegraph station summary.'
    changes:
      - 'Recorded existing index counts, exact verified fingerprints, bounded relationship evidence, and passing source coverage.'
holisticQualityRating: not-rated
overallStatus: not-reviewed
---
<!-- AI_DISCLAIMER v1.0 -->
# Code index summary

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Scope and outcome

**Code-index coverage passed for all 3 expected repository units.** The approved source root remains `services/`, used in place without copying or relocation. These are indexing units within the monorepo, not necessarily separate Git repositories.

Existing per-repository indexing was supplied as successfully completed with `kp_bootstrap.py index`; this continuation did not rebuild the indexes. Counts were checked against [index state](knowledge-plane/kp-state.json), dependency records queried independently, and freshness, fingerprints, and source coverage verified read-only. The dispatch supplied no `source_coverage` property; the explicit coverage verification below supplies the gate evidence.

The [input inventory](repository-inventory.md) identifies Green for IT's two main business capabilities: evaluating environmental impacts of IT inventories and digital services, and presenting the results.

## Indexed entities and relationships

These are actual per-repository index counts, not a semantic inventory of every class, endpoint, or relationship.

| Repository unit | Eligible files | Regex-extracted symbols | Manifest components | Indexed internal manifest dependencies |
|---|---:|---:|---:|---:|
| [Backend](../../../services/backend/) | 895 | 642 | 1 Maven | 0 |
| [SchemaCrawler utility](../../../services/documentation/schemacrawler/) | 6 | 6 | 0 | 0 |
| [Frontend](../../../services/frontend/) | 546 | 467 | 1 npm | 0 |
| **Sum of separate code-index records** | **1,447** | **1,115** | **2** | **0** |

File, symbol, and component counts come from the completed index outputs retained in [index state](knowledge-plane/kp-state.json). Three index-backed `dep-graph` queries returned untruncated results: one component each for backend and frontend, both with empty internal-dependency lists; SchemaCrawler returned no component record. The zero relationship count applies only to these indexed internal manifest records, **not** to cross-repository or runtime dependencies.

External manifest entries were returned for Maven and npm but are not counted as runtime integrations: parser output includes build/test tooling and other manifest artifact references. No dependency or coupling count across repositories was derived.

## Evidence-backed functional map

The bounded service inventory contains **2 business application components and 1 supporting documentation utility**, based on [architecture documentation, lines 13–45](../../../services/documentation/content/3-technical-documentation/2-software_architecture/_index.md#L13-L45), the [utility README, lines 1–20](../../../services/documentation/schemacrawler/README.md#L1-L20), and the [input inventory](repository-inventory.md). This classification is separate from the two manifest-component records above.

| Unit | Business responsibility |
|---|---|
| Frontend | Angular browser application for entering inventories and digital services and viewing impact results. |
| Backend | Spring Boot REST application for loading inventories and digital services and calculating their environmental impacts. |
| SchemaCrawler utility | Generates grouped database-table documentation; not a business API service. |

The following relationships are **analysis findings**, not mechanically indexed edges or an exhaustive dependency inventory.

The evidence-backed relationship table below contains **6 relationships**; this count is scoped only to that table.

| From → to | Supported relationship | Evidence |
|---|---|---|
| Frontend → backend | Browser workflows consume the product REST interface. | [Architecture, lines 13–22](../../../services/documentation/content/3-technical-documentation/2-software_architecture/_index.md#L13-L22); consumer-side business API path constants in [constants.ts, lines 68–92](../../../services/frontend/src/constants.ts#L68-L92). |
| Backend → PostgreSQL | Product data persistence. | [Architecture, line 27](../../../services/documentation/content/3-technical-documentation/2-software_architecture/_index.md#L27). |
| Backend → inventory/export file storage | Stores inventory inputs and produces data exports. | [Architecture, line 28](../../../services/documentation/content/3-technical-documentation/2-software_architecture/_index.md#L28). |
| Backend → NumEcoEval calculation library | Embedded environmental-impact calculations, not a separate discovered service. | [pom.xml, lines 129–155](../../../services/backend/pom.xml#L129-L155); [architecture, line 29](../../../services/documentation/content/3-technical-documentation/2-software_architecture/_index.md#L29). |
| Backend → Boavizta APIs | Documented cloud-service impact evaluation. | [Architecture, line 30](../../../services/documentation/content/3-technical-documentation/2-software_architecture/_index.md#L30); implementation traversal was outside this bounded inspection. |
| SchemaCrawler utility → database metadata / documentation | Generates Markdown documentation for groups of database tables. | [README, lines 1–20](../../../services/documentation/schemacrawler/README.md#L1-L20); diagram/formatter evidence is retained in [inventory E6–E7](repository-inventory.md#source-evidence). |

Inventory and digital-service API families are evidenced by frontend constants; they are not a verified server endpoint inventory. No endpoint, table, event, class-kind, or call-edge totals were assembled.

## Index fingerprints

Copied unchanged from the successful coverage verification. Each link identifies its separate SQLite artifact.

| Repository / index | `index_sha256` | `index_bytes` | `built_at` |
|---|---|---:|---|
| [services/backend](knowledge-plane/backend-8f95ef27/index.sqlite) | `1abb03fdf2708627e39e354ab92849cbfbc43828ea1210fef3510c48c218647c` | 667648 | `2026-11-16T13:52:52.920076+00:00` |
| [services/documentation/schemacrawler](knowledge-plane/schemacrawler-a71c4d32/index.sqlite) | `ef61f6fccb94d14addf40907846ac2f7df359cde7fdfafb228a2a18ccff3d6fa` | 40960 | `2026-11-16T13:53:01.649579+00:00` |
| [services/frontend](knowledge-plane/frontend-d957c6b7/index.sqlite) | `f7b93db6b2e45906fd23fb47137e9342988382ccbd2db926ef9c4788ef3bda06` | 376832 | `2026-11-16T13:52:57.434693+00:00` |

## Final source-coverage gate and exclusions

Command: `python .github/skills/knowledge-plane/tools/kp_bootstrap.py verify --analysis-id g4it-on-github --source-root services/ --cover-source-code`

- Exit status: **0**; `allowed: true`; `errors: []`.
- Expected roots: `services/backend`, `services/documentation/schemacrawler`, `services/frontend`; **3/3 fresh**, with the fingerprints above.
- Extra indexed roots: **none**. Skipped repositories: **none** (`skipped: {}`); no skip reasons exist.
- Discovery problems: **0**, as supplied in the [inventory](repository-inventory.md#scope-and-discovery-gate); verification reported no errors.
- Documentation content/templates/static areas and Keycloak supporting areas were classified by discovery as not requiring additional code repositories because no eligible code files were found there. These are coverage notes, not skipped repositories or unresolved failures; the nested SchemaCrawler utility is indexed.

## Interpretation limits and station boundary

- There are **three separate SQLite code indexes**, not a unified cross-repository graph. They store eligible file metadata, regex-extracted class/function definitions, and component dependencies parsed from build manifests. Indexed files are not limited to executable source.
- The indexes do **not** mechanically extract function-call, inheritance, interface-implementation, API-call, or data-access edges. This report does not claim a function-call graph, cross-repository dependency/coupling counts, cycle counts, or orphan counts.
- Source conclusions are limited to the four bounded Knowledge Plane reads cited above, the three manifest queries, and the supplied inventory evidence. They are static findings, not runtime verification. Manual inspection stayed below 50 files per batch and within five directory levels.
- Business relationships are summarized rather than deeply traced. Authentication and other non-functional concerns were not analyzed. Generated/binary exclusions and protected-file filtering limit index scope; complete coverage means every expected repository has a fresh eligible-file index, not that every file or semantic relationship was analyzed.
- The shared document graph is a different SQLite store belonging to a later station. No graph initialization, `verify --graph`, ingestion, or document-graph counting was performed; its totals are not merged with code-index counts.
- This continuation writes only this report. Source code, workflow state, and response-envelope/state files were not created or modified.
