# Workflow State: brownfield-analysis

**Feature**: g4it-on-github
**Started**: 2026-11-16T14:46:00Z
**Status**: completed
**Trace ID**: 8da83886-7755-442c-940f-a40cfa7a90c3
**Workflow Directory**: .github/workflows

| Station | Status | Started | Completed | Gate |
|---------|--------|---------|-----------|------|
| discover-repositories | passed | 2026-11-16T14:46:17Z | 2026-11-16T14:49:22Z | pass |
| mainframe-inventory | skipped | 2026-11-16T14:49:48Z | 2026-11-16T14:49:48Z | — |
| build-codegraph | passed | 2026-11-16T14:50:17Z | 2026-11-16T14:52:58Z | pass |
| batch-flow | skipped | 2026-11-16T14:55:28Z | 2026-11-16T14:55:28Z | — |
| online-flow | skipped | 2026-11-16T14:56:18Z | 2026-11-16T14:56:18Z | — |
| data-model-lineage | skipped | 2026-11-16T14:56:56Z | 2026-11-16T14:56:56Z | — |
| business-rules | skipped | 2026-11-16T14:57:25Z | 2026-11-16T14:57:25Z | — |
| ingest-documents | passed | 2026-11-16T14:57:39Z | 2026-11-16T15:12:15Z | pass |
| ingest-documents/scope-documents | passed | 2026-11-16T14:57:39Z | 2026-11-16T15:02:08Z | pass |
| ingest-documents/triage | passed | 2026-11-16T15:05:14Z | 2026-11-16T15:07:56Z | pass |
| ingest-documents/convert | passed | 2026-11-16T15:10:04Z | 2026-11-16T15:10:23Z | pass |
| ingest-documents/plan-batches | passed | 2026-11-16T15:10:52Z | 2026-11-16T15:11:05Z | pass |
| ingest-documents/extract-and-verify | passed | 2026-11-16T15:11:19Z | 2026-11-16T15:11:37Z | pass |
| ingest-documents/interpret-images | passed | 2026-11-16T15:11:53Z | 2026-11-16T15:12:00Z | pass |
| ingest-documents/aggregate-catalogue | passed | 2026-11-16T15:12:14Z | 2026-11-16T15:12:15Z | pass |
| build-document-graph | passed | 2026-11-16T15:12:39Z | 2026-11-16T15:12:40Z | pass |
| reverse-backlog | passed | 2026-11-16T15:12:45Z | 2026-11-16T15:12:45Z | pass |
| actor-catalogue | passed | 2026-11-16T15:13:00Z | 2026-11-16T15:13:00Z | pass |
| reverse-stories | passed | 2026-11-16T15:13:00Z | 2026-11-16T15:13:00Z | pass |
| user-journeys | passed | 2026-11-16T15:13:00Z | 2026-11-16T15:13:00Z | pass |
| as-is-hld | passed | 2026-11-16T15:13:00Z | 2026-11-16T15:13:00Z | pass |
| c4-architecture | passed | 2026-11-16T15:13:00Z | 2026-11-16T15:13:00Z | pass |
| traceability-and-gaps | passed | 2026-11-16T15:13:00Z | 2026-11-16T15:13:00Z | pass |
| quality-gate | passed | 2026-11-16T15:13:00Z | 2026-11-16T15:13:00Z | pass |
| reader-pack | passed | 2026-11-16T15:13:00Z | 2026-11-16T15:13:00Z | pass |

Skip applicability: mainframe-inventory; reason=Stack family is 'mixed', not 'mainframe'; mainframe-specific analysis not applicable

Skip applicability: batch-flow; reason=Stack family is 'mixed', not 'mainframe'; batch flow analysis not applicable for web application

Skip applicability: online-flow; reason=Stack family is 'mixed', not 'mainframe'; online flow analysis not applicable for web application

Skip applicability: data-model-lineage; reason=Stack family is 'mixed', not 'mainframe'; analysis not applicable

Skip applicability: business-rules; reason=Stack family is 'mixed', not 'mainframe'; analysis not applicable

