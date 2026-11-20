---
title: Document triage summary
workflow: brownfield-analysis
trigger: user
date: '2026-11-19'
analysis_id: g4it-on-github
station: ingest-documents/triage
skill: [ingestion-triage, office-common]
status: pending-review
inputDocuments:
  - outputs/brownfield/g4it-on-github/ingestion-scope.md
  - outputs/ingestion/g4it-on-github/ingestion-params.json
  - outputs/ingestion/g4it-on-github/triage-report.json
  - outputs/ingestion/g4it-on-github/triage-report.md
  - outputs/ingestion/g4it-on-github/triage-decisions.json
  - .github/skills/ingestion-triage/SKILL.md
changeHistory:
  - date: '2026-11-19'
    description: "Triage station run (trace 4d1e4282). Replaces the minimal placeholder, which performed no triage."
    changes:
      - "Scan over the five approved include directories: 542 candidate sources, discovery by triage walk."
      - "One category decision recorded per category (10 categories), plus one path-level exclusion for an Office owner lock file. All 542 sources decided."
      - "Two sensitive-named files outside the scan's candidate formats could not be recorded (see Attention Points)."
  - date: '2026-11-19'
    description: "Checkpoint corrections applied (trace 4d1e4282), user decisions of 2026-11-19. Conversion, batching and extraction not run."
    changes:
      - "Frontend HTML (92 sources under services/frontend) recorded as exclude by category (UI source code, not documentation); replaces the recommended convert."
      - "Backend test fixtures under services/backend/src/test/ (75 include sources) recorded as exclude by path. The 6 reference-data CSVs under services/backend/src/main/resources/db/changelog/data/ stay included."
      - "Two RGESN declaration PDFs (services/frontend/src/assets/pdf/declarations/) recorded as exclude by path, reason 'pypdf not provisioned — accepted gap'. Explicit unprocessed gap."
      - "Proposed exclusion reasons (sensitive, empty, path-heuristic, zip-invalid, lock file) accepted by the user; no reason changed."
      - "Dependency check (extract_sources.py check --converters) skipped by user decision; recorded as accepted skip."
      - "Totals after corrections: include 283, exclude 179, interpret 80, convert 0; undecided 0."
holisticQualityRating: not-rated
overallStatus: pending-review
trace_id: 4d1e4282-cc4b-4ea6-83da-14dd38c0b892
context_manifest:
  - source-code (root-project analysis in place, user-consented 2026-11-19; third-party and secret-bearing paths excluded)
  - ingestion-parameters (approved by the user 2026-11-19)
  - triage-decisions (recorded by ingestion-triage decide; checkpoint corrections of 2026-11-19 applied, user decisions authoritative)
---
<!-- AI_DISCLAIMER v1.0 -->

# Document triage summary

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Status

Triage is complete for the approved scope after the 2026-11-19 checkpoint corrections: `undecided_categories` is empty and
`undecided_sources` is 0. Every decision is recorded by `triage.py decide` in
`outputs/ingestion/g4it-on-github/triage-decisions.json`, with hashes computed by the tool. The user accepted the
exclusion reasons and the corrections listed in the change history. No conversion, batching or extraction was run. Only
paths, sizes, categories and counts were read; no source text was opened.

## Scope and discovery

- Source root: `.` (workspace root). Output root: `.`. Analysis id: `g4it-on-github`. Mode: nested.
- Include directories (approved): `services/documentation/content`, `services/backend`, `services/frontend`,
  `services/keycloak`, `services/documentation/schemacrawler`.
- Legacy codepage: none (`legacy_codepage: null`, `--legacy-codepage` not passed).
- Discovery: `triage-walk`. `inventory.json` was not produced because the office-common discovery was not used.
- Candidate sources: 542. Sources with a natively extractable format: 369. Of these, 9 are excluded by category
  (`empty` 4, `path-heuristic` 4, `zip-invalid` 1), 75 backend test fixtures and 2 RGESN PDFs are excluded by path.
  The 92 `html` sources are excluded by category.
- Explicit uncovered gap (accepted by the user): root Markdown (`README.md`, `CHANGELOG.md`, `CONTRIBUTING.md`,
  `DOCKER-DEPLOYMENT.md`) and `services/documentation/README.md` are outside every include path. The catalogue must
  state this gap.

## Category decisions

| Category | Sources | Decision recorded | Reason (basis) | Source of decision |
|----------|--------:|-------------------|----------------|--------------------|
| `sensitive` | 1 | exclude | sensitive path or key material; never read | Recommended default, reason accepted 2026-11-19 |
| `empty` | 4 | exclude | empty or binary file; no readable text | Recommended default, reason accepted 2026-11-19 |
| `path-heuristic` | 4 | exclude | third-party, archived or sample content by path | Recommended default, reason accepted 2026-11-19 |
| `zip-invalid` | 1 | exclude | corrupt or partial Office archive (lock file, see path decision below) | Recommended default, reason accepted 2026-11-19 |
| `unknown-needs-dependency` | 2 | exclude by path (category default include overridden) | pypdf not provisioned — accepted gap | User checkpoint decision 2026-11-19 |
| `image-file` | 80 | interpret | standalone image beside a scoped document | Recommended default |
| `encoding-utf16-bom` | 1 | include | UTF-16 with BOM | Recommended default |
| `encoding-utf8-bom` | 3 | include | UTF-8 with BOM | Recommended default |
| `html` | 92 | exclude (replaces recommended convert) | UI source code (Angular templates, nginx error page), not documentation | User checkpoint decision 2026-11-19 |
| `ready` | 354 | include (category); 75 excluded by path | 75 backend test fixtures under `services/backend/src/test/` excluded; 279 remain included, including the 6 reference-data CSVs | Recommended default for category; path exclusions by user decision 2026-11-19 |

Path-level decisions (`origin: path`):
- Office owner lock file `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/media/~$screenshot.pptx`
  excluded, reason "Office owner lock file, not content" (from the approved scope).
- 75 backend test fixtures under `services/backend/src/test/` (previously `include`, category `ready`, plus 4 `empty` fixtures
  already excluded), excluded with reason "Backend test fixture, not documentation; user decision 2026-11-19".
- 2 RGESN declaration PDFs under `services/frontend/src/assets/pdf/declarations/`, excluded with reason
  "pypdf not provisioned — accepted gap".

Totals: include 283, exclude 179, interpret 80, convert 0. Sum 542.

## Sensitive sources (never opened, hashed or converted)

- `services/backend/volume_generation/.env`: classified `sensitive` by the scan and excluded.
- `services/backend/settings.xml` and `services/keycloak/imports/g4it-realm-export.json`: named in the approved scope as
  sensitive. The scan did not classify them (they are not candidate formats), and `decide` refused them because they are
  not in the report. They are listed here by name only, from a file listing. They were never opened.

## Counts by decision

| Decision | Sources |
|----------|--------:|
| include | 283 |
| exclude | 179 |
| interpret | 80 |
| convert | 0 |
| undecided | 0 |

Breakdown of exclude (179): `sensitive` 1, `empty` 4, `path-heuristic` 4, `zip-invalid` 1, `html` 92, backend test fixtures 75, RGESN PDFs 2.

## Attention Points

- **Frontend HTML (92): resolved.** Excluded by category, user decision 1 of 2026-11-19. These are Angular templates and the nginx
  error page (UI source code, not documentation). The recommended `convert` is no longer used, so no conversion is needed for them.
- **Backend test fixtures: resolved.** The 75 `include` sources under `services/backend/src/test/` are excluded by path, user
  decision 2 of 2026-11-19. The 6 reference-data CSVs under `services/backend/src/main/resources/db/changelog/data/` stay included.
- **Two sensitive-named files: confirmed out of scope.** `services/backend/settings.xml` and
  `services/keycloak/imports/g4it-realm-export.json` are not in `triage-report.json` and have no entry in `triage-decisions.json`,
  because `decide` cannot record them. User decision 4 of 2026-11-19 confirms they stay out of every extraction request. No later
  station may add them. They were never opened.
- **pypdf not provisioned: accepted gap.** The two RGESN declaration PDFs under `services/frontend/src/assets/pdf/declarations/`
  are excluded by path, reason "pypdf not provisioned — accepted gap" (user decision 5 of 2026-11-19). They are an explicit
  unprocessed gap: the catalogue must state it, and they are not claimed as read.
- **Dependency check skipped: accepted skip.** `extract_sources.py check --converters` was not run (user decision 6 of 2026-11-19).
  It is also not in the commandAllowlist of the loaded skills. The scan's own output still reports the pypdf gap, which is accepted above.
- **Image sources (80, `interpret`).** Recorded as `interpret`. Whether an image-capable cheap model is available is confirmed
  in the `interpret-images` station, not here.
- **No discovery manifest.** The triage walk, not office-common discovery, produced the candidate list. Batching and extraction
  must reconcile against `triage-report.json` and `triage-decisions.json`.
- **Exclusion reasons accepted.** The user accepted the proposed reasons for `sensitive`, `empty`, `path-heuristic`, `zip-invalid`
  and the lock file (user decision 3 of 2026-11-19). No reason was changed.

## Open questions (resolved at checkpoint, 2026-11-19)

1. **Frontend HTML (92).** Resolved: excluded as UI source code (decision 1).
2. **Backend test fixtures and reference data.** Resolved: 75 test fixtures excluded; the 6 reference-data CSVs stay included (decision 2).
3. **Exclusion reasons.** Resolved: proposed reasons accepted (decision 3).
4. **Sensitive-named files.** Resolved: `services/backend/settings.xml` and `services/keycloak/imports/g4it-realm-export.json` stay out of every extraction request (decision 4).
5. **pypdf and dependency check.** Resolved: pypdf not provisioned; the two RGESN PDFs are an accepted gap (decision 5). The dependency check is an accepted skip (decision 6).

No triage question remains open.

## Production confidence

- **Source inventory and counts:** high. They come from the scan report and were checked against `triage-report.json`
  and `triage-decisions.json` after the corrections (include 283, exclude 179, interpret 80, convert 0, undecided 0).
- **Decisions recorded:** high. Every correction was recorded with `triage.py decide` and the hashes are tool-computed.
  The reasons and the category decisions are user-accepted.
- **Sensitive coverage:** medium. `services/backend/settings.xml` and `services/keycloak/imports/g4it-realm-export.json` are
  outside the scan's candidate formats and cannot be recorded. Their exclusion rests on user decision 4 and on the
  batching station enforcing it.
- **Gaps:** explicit. Root Markdown and `services/documentation/README.md` are outside the include paths. The two RGESN PDFs
  are unprocessed (pypdf not provisioned).
- **Overall:** triage station complete after checkpoint corrections. The conversion station has no `convert` or `split`
  sources and should offer a single confirm.
