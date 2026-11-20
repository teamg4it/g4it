# Workflow State: brownfield-analysis

**Feature**: g4it-on-github
**Started**: 2026-11-19T16:22:25Z
**Status**: failed
**Trace ID**: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
**Workflow Directory**: .github/workflows

| Station | Status | Started | Completed | Gate |
|---------|--------|---------|-----------|------|
| discover-repositories | passed | 2026-11-19T16:25:48Z | 2026-11-19T16:35:11Z | pass |
| mainframe-inventory | skipped | 2026-11-19T16:45:55Z | 2026-11-19T16:45:55Z | — |
| build-codegraph | passed | 2026-11-19T16:47:19Z | 2026-11-19T17:07:10Z | pass |
| ingest-documents | failed | 2026-11-19T17:10:09Z | 2026-11-20T09:29:32Z | fail |
| ingest-documents/scope-documents | passed | 2026-11-19T17:10:09Z | 2026-11-19T17:29:02Z | pass |
| ingest-documents/triage | passed | 2026-11-19T17:29:36Z | 2026-11-19T17:35:21Z | pass |
| ingest-documents/convert | passed | 2026-11-19T17:47:56Z | 2026-11-19T17:49:47Z | pass |
| ingest-documents/plan-batches | passed | 2026-11-20T08:08:44Z | 2026-11-20T08:31:02Z | pass |
| ingest-documents/extract-and-verify | passed | 2026-11-20T08:36:21Z | 2026-11-20T09:02:48Z | pass |
| ingest-documents/interpret-images | skipped | 2026-11-20T09:10:18Z | 2026-11-20T09:10:25Z | — |
| ingest-documents/aggregate-catalogue | failed | 2026-11-20T09:21:14Z | 2026-11-20T09:29:32Z | fail |
| build-document-graph | pending | — | — | — |
| batch-flow | pending | — | — | — |
| online-flow | pending | — | — | — |
| data-model-lineage | pending | — | — | — |
| business-rules | pending | — | — | — |
| reverse-backlog | pending | — | — | — |
| actor-catalogue | pending | — | — | — |
| reverse-stories | pending | — | — | — |
| user-journeys | pending | — | — | — |
| as-is-hld | pending | — | — | — |
| c4-architecture | pending | — | — | — |
| traceability-and-gaps | pending | — | — | — |
| reader-pack | pending | — | — | — |
| quality-gate | pending | — | — | — |

Skip applicability: mainframe-inventory; reason=config.stack_family == 'mainframe' is false (stack_family=mixed from repository-classification.md)

Skip applicability: ingest-documents/interpret-images; reason=User chose to omit image interpretation; no native image-reading capability is available in this session. The 80 PNG images remain uninterpreted.
