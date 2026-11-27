---
workflow: brownfield-analysis
trigger: extract-and-verify
date: 2026-11-20
status: blocked
inputDocuments:
  - outputs/brownfield/g4it-on-github/batch-plan.md
  - outputs/ingestion/g4it-on-github/batch-plan.json
  - outputs/ingestion/requests/g4it-on-github/*.json
changeHistory:
  - date: 2026-11-20
    change: Replaced placeholder; extracted and verified all 14 batches with office-common repository mode (10 passed, 4 blocked)
  - date: 2026-11-20
    change: "User decision (Option B): 10 unreadable XLSX/PPTX sources excluded via repository-exclude with the user's reason; 6 batches re-extracted and re-verified (11 passed, 3 blocked on 80 pending PNG image decisions only); images.py handoff allowed for 3 batches; aggregate not run"
holisticQualityRating: unassessed
overallStatus: blocked
trace_id: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
station: ingest-documents/extract-and-verify
agent: document-ingestion
skill: office-common
sensitivity: confidential
context_manifest:
  - path: .github/skills/office-common/SKILL.md
    key_facts:
      - "Repository batches run via extract_sources.py repository --manifest <request> --analysis-id <id>; outputs under outputs/ingestion/repository/<analysis-id>/"
      - "Unsupported formats, missing dependencies and unreadable sources block completion; no automatic package installation"
      - "images.py handoff permits only unresolved image gaps to reach the image station; unreadable text still blocks"
  - path: .github/skills/ingestion-batching/SKILL.md
    key_facts:
      - "Each batch is extracted as office-common analysis id <analysis-id>-<batch-id>"
      - "Batch states: planned, running, passed (verify-result allowed true), blocked, failed"
      - "aggregate is allowed only when every batch passed and nothing is unresolved"
  - path: outputs/brownfield/g4it-on-github/batch-plan.md
    key_facts:
      - "14 batches, 541 sources in batches, 178 in-request exclusions, 363 in scope"
      - "1 sensitive .env source outside requests (never read)"
      - "Grouping mode area-map, max batch size 100, no conversions"
  - path: outputs/ingestion/g4it-on-github/batch-plan.json
    key_facts:
      - "extraction_options.source_root is null (workspace root); no legacy_codepage"
  - path: .github/skills/ingestion-triage/SKILL.md
    key_facts:
      - "decide --path records per-source decisions with tool-computed hashes; a later category decision never overwrites a path decision"
      - "Standalone images are provisionally discovered beside candidate documents; planning removes them if no document in that folder remains in scope"
  - path: outputs/brownfield/g4it-on-github/extraction-verification.md
    key_facts:
      - "Prior run: 10/14 batches passed; 10 unreadable XLSX/PPTX (missing pypdf/openpyxl/defusedxml); 80 PNG gaps pending"
---
<!-- AI_DISCLAIMER v1.0 -->
# Extraction and Verification — g4it-on-github

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

CONTEXT LOADED: 6 documents read, context manifest prepared.

## Current state (after user decision, 2026-11-20)

**User decision (Option B):** the 10 unreadable sources below are excluded with reason
"Excluded by user decision 2026-11-20: office-common parser dependencies unavailable; non-essential XLSX
templates / PPTX screenshot decks". No packages were installed.

**How it was recorded.**

1. First attempt: `triage.py decide --path … --decision exclude`, then `batching.py plan --rewrite`. The
   re-plan moved **77 standalone PNGs** out of the requests (`outside_requests`, reason "no in-scope document
   in the image folder"), because the excluded PPTX decks were the only documents in those image folders.
   That would have omitted the screenshots, which is not allowed. **Reverted:** the 10 paths were set back to
   `include` with `triage.py decide --path` (the reason records the reversal), and `plan --rewrite` restored the
   original 14 batches (541 sources in requests, 1 outside requests).
2. Recorded instead in the batch requests with `extract_sources.py repository-exclude` (one call per path;
   the tool computed each sha256): backend-02 (2), functional-02 (2), getstarted-01 (5), getstarted-02 (1).
   Effect: these are **post-triage exclusions**. `batching.py aggregate` counts them as **unresolved** (not
   triage-agreed), so aggregate cannot report `allowed: true` until the workflow accepts that or they are resolved
   another way.
3. Re-ran `repository` + `repository-verify` for backend-01, backend-02, functional-01, functional-02,
   getstarted-01, getstarted-02. backend-01 and functional-01 were rewritten by the plan round-trip, so
   they needed re-extraction. Their content did not change.

| Batch | State | Verify result | Nonvisual unresolved | Image gaps pending |
|---|---|---|---:|---:|
| backend-01 | passed | allowed: true | 0 | 0 |
| backend-02 | passed | allowed: true (19 read, 6 excluded) | 0 | 0 |
| functional-01 | passed | allowed: true (54 read) | 0 | 0 |
| functional-02 | **blocked** | allowed: false (28 read, 2 excluded, 23 partial) | 0 | 23 |
| getstarted-01 | **blocked** | allowed: false (19 read, 6 excluded, 33 partial) | 0 | 33 |
| getstarted-02 | **blocked** | allowed: false (32 read, 1 excluded, 24 partial) | 0 | 24 |
| other 8 batches | passed | unchanged | 0 | 0 |
| **Total** | **11 passed / 3 blocked** | | **0** | **80** |

The 3 remaining blocks come only from pending PNG image decisions (`image:whole-file`). No unreadable text is left.

**Image handoff:** `images.py handoff` returned `allowed: true` for functional-02 (23), getstarted-01 (33) and
getstarted-02 (24). `final_verification_allowed` stays false until image decisions are made. The 80 PNGs are kept for
`interpret-images`; none were omitted. The canonical lists are in each batch's `pending-image-members.json`.

**Aggregate:** not run, because not every batch passed (3 are blocked on image decisions).

## Initial run (superseded) — Environment

`extract_sources.py check` → `pypdf`, `openpyxl`, `defusedxml` **missing** (exit 1). Text sources were read
directly; every XLSX/PPTX source failed as `unreadable` ("Missing parser dependency; provision office-common
extraction requirements"). Packages were not installed (operator action required).

## Coverage per batch

Per batch: `extract_sources.py repository --manifest outputs/ingestion/requests/g4it-on-github/<batch>.json --workers 4 --analysis-id g4it-on-github-<batch>`, then `repository-verify` and `batching.py status`.

| Batch | State | Sources | Extracted/read | Excluded (triage) | Image gaps pending | Nonvisual failures |
|---|---|---:|---:|---:|---:|---:|
| backend-01 | passed | 100 | 21 | 79 | 0 | 0 |
| backend-02 | **blocked** | 25 | 19 | 4 | 0 | 2 |
| docs-01 | passed | 1 | 1 | 0 | 0 | 0 |
| frontend-01 | passed | 98 | 4 | 94 | 0 | 0 |
| frontend-02 | passed | 3 | 3 | 0 | 0 | 0 |
| functional-01 | passed | 54 | 54 | 0 | 0 | 0 |
| functional-02 | **blocked** | 53 | 28 | 0 | 23 | 2 |
| getstarted-01 | **blocked** | 58 | 19 | 1 | 33 | 5 |
| getstarted-02 | **blocked** | 57 | 32 | 0 | 24 | 1 |
| help-01 | passed | 10 | 10 | 0 | 0 | 0 |
| keycloak-01 | passed | 1 | 1 | 0 | 0 | 0 |
| releases-01 | passed | 28 | 28 | 0 | 0 | 0 |
| schemacrawler-01 | passed | 1 | 1 | 0 | 0 | 0 |
| technical-01 | passed | 52 | 52 (1 extracted, 51 direct) | 0 | 0 | 0 |
| **Total** | 10 passed / 4 blocked | **541** | **273** | **178** | **80** | **10** |

Verified = extracted/read count for passed batches (190); blocked batches are not verified.

## Nonvisual failures (blocking)

All 10 are `unreadable` — missing parser dependency:

| Batch | Source |
|---|---|
| backend-02 | `services/backend/storage/--INTERNAL-G4IT--/0/templates/G4IT_Datamodel_and_reference_data_v3.2.0.xlsx` |
| backend-02 | `services/backend/storage/--INTERNAL-G4IT--/0/templates/G4IT_Datamodel_workspace_reference_data_v1.0.0.xlsx` |
| functional-02 | `.../use_cases/uc_navigation_menu/images/Welcome page.pptx` |
| functional-02 | `.../use_cases/uc_navigation_menu/uc2_header/images/Create new organization.pptx` |
| getstarted-01 | `.../1-module-digital-service/media/screenshot.pptx` |
| getstarted-01 | `.../2-module-information-system/documents/application_toFillIn.xlsx` |
| getstarted-01 | `.../2-module-information-system/documents/datacenter_toFillIn.xlsx` |
| getstarted-01 | `.../2-module-information-system/documents/physicalEquipment_toFillIn.xlsx` |
| getstarted-01 | `.../2-module-information-system/documents/virtualEquipment_toFillIn.xlsx` |
| getstarted-02 | `.../2-module-information-system/images/ScreenShot_ISModule.pptx` |

## Images handed to the image stage

**None.** `images.py handoff --analysis-id g4it-on-github-functional-02` returned `allowed: false`
(`final_verification_allowed: false`) because nonvisual failures remain. 80 standalone image gaps
(`image:whole-file`) are pending: functional-02 (23), getstarted-01 (33), getstarted-02 (24); canonical lists
are in each batch's `pending-image-members.json`.

## Continuation needs

1. Operator provisions `requirements-extraction.txt` (pypdf, openpyxl, defusedxml, python-pptx) in the project venv.
2. Re-run `repository` + `repository-verify` for backend-02, functional-02, getstarted-01, getstarted-02.
3. Or record user exclusions (`repository-exclude` with reason) / accessible exports for the 10 sources.
4. Then `images.py handoff` for the 3 image-bearing batches; `batching.py aggregate` was not run.
