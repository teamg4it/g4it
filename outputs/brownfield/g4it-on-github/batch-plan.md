---
workflow: brownfield-analysis
trigger: batch-plan
date: 2026-11-20
status: draft
inputDocuments:
  - outputs/brownfield/g4it-on-github/conversion-log.md
  - outputs/ingestion/g4it-on-github/batch-plan.json
changeHistory:
  - date: 2026-11-20
    change: Human-readable batch plan derived from batch-plan.json
holisticQualityRating: unassessed
overallStatus: pending
trace_id: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
station: ingest-documents/plan-batches
agent: document-ingestion
skill: ingestion-batching
sensitivity: confidential
---
<!-- AI_DISCLAIMER v1.0 -->
# Batch Plan — g4it-on-github

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Summary

| Metric | Value |
|---|---|
| Sources inventoried | 542 |
| Triage decisions | 542 |
| Conversions (derived copies) | 0 |
| Batches | 14 |
| Max batch size | 100 |
| Grouping mode | area-map |
| Sources in batches | 541 |
| Sources outside requests | 1 |
| Excluded (total) | 179 (178 in batches + 1 outside) |
| In scope | 363 |
| Stale / obsolete requests | 0 / 0 |
| Warnings | none |

Source plan: `outputs/ingestion/g4it-on-github/batch-plan.json` (not regenerated).

## Batches

| Batch | Area | Sources | Exclusions | In scope | State |
|---|---|---:|---:|---:|---|
| backend-01 | backend | 100 | 79 | 21 | planned |
| backend-02 | backend | 25 | 4 | 21 | planned |
| docs-01 | docs | 1 | 0 | 1 | planned |
| frontend-01 | frontend | 98 | 94 | 4 | planned |
| frontend-02 | frontend | 3 | 0 | 3 | planned |
| functional-01 | functional | 54 | 0 | 54 | planned |
| functional-02 | functional | 53 | 0 | 53 | planned |
| getstarted-01 | getstarted | 58 | 1 | 57 | planned |
| getstarted-02 | getstarted | 57 | 0 | 57 | planned |
| help-01 | help | 10 | 0 | 10 | planned |
| keycloak-01 | keycloak | 1 | 0 | 1 | planned |
| releases-01 | releases | 28 | 0 | 28 | planned |
| schemacrawler-01 | schemacrawler | 1 | 0 | 1 | planned |
| technical-01 | technical | 52 | 0 | 52 | planned |
| **Total** | | **541** | **178** | **363** | |

Request files: `outputs/ingestion/requests/g4it-on-github/<batch>.json`.

## Exclusions

- 178 exclusions recorded inside batch requests: backend 83, frontend 94, getstarted 1.
- 1 source outside requests: a `.env` file under `services/backend/volume_generation/` (category `sensitive`; never read).
- No unbatched exclusions.
