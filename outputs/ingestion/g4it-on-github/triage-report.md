---
workflow: "document-ingestion"
station: "ingest-documents/triage"
trigger: "triage-scan"
date: "2026-11-19T17:31:23.971312+00:00"
status: "draft"
inputDocuments: ["."]
changeHistory: []
holisticQualityRating: "unassessed"
overallStatus: "pending"
sensitivity: "confidential"
analysis_id: "g4it-on-github"
---
<!-- AI_DISCLAIMER v1.0 -->
# Triage Report: g4it-on-github

> This file has been created (totally or partially) with the assistance of artificial intelligence tools. All content has been generated under the direct supervision of a named individual, and under the AI.Backbone Orchestrator Compliance framework

- Source root: `.`
- Sources: 542 (natively extractable: 369)
- Discovery: triage-walk
- Legacy codepage tested: cp1252; pypdf: missing

Paths, sizes and categories only; no source text is reproduced.

## Categories

| Category | Sources | Recommended decision | Meaning |
|---|---:|---|---|
| `sensitive` | 1 | exclude | Key material, secret paths or possible customer data; never read, hashed or converted. |
| `empty` | 4 | exclude | Zero-byte file. |
| `path-heuristic` | 4 | exclude | Path suggests third-party, archived, backup, sample or test content. |
| `zip-invalid` | 1 | exclude | Corrupt or partial OOXML archive. |
| `unknown-needs-dependency` | 2 | include | An optional triage dependency is missing; install it and re-scan, or let extraction decide. |
| `image-file` | 80 | interpret | Standalone image beside a scoped document; interpret its visual content. |
| `encoding-utf16-bom` | 1 | include | UTF-16 with BOM; office-common decodes it natively. |
| `encoding-utf8-bom` | 3 | include | UTF-8 with BOM; office-common decodes it natively. |
| `html` | 92 | convert | HTML; convert to structured text first. |
| `ready` | 354 | include | Natively extractable. |

## Sensitive sources (never read or converted)

- `services/backend/volume_generation/.env`

## Examples

### `sensitive` (1)

- `services/backend/volume_generation/.env`

### `empty` (4)

- `services/backend/src/test/resources/common/filesystem/azure/test-upload.txt`
- `services/backend/src/test/resources/common/filesystem/local/work/20230511-1815/DATACENTER/datacenter.txt`
- `services/backend/src/test/resources/common/filesystem/local/work/20230511-1815/DATACENTER/datacenter2.txt`
- `services/backend/src/test/resources/common/filesystem/local/work/20231904-1808/DATACENTER/datacenter.txt`

### `path-heuristic` (4)

- `services/backend/src/main/resources/samples/inputs/1-datacenter.csv`
- `services/backend/src/main/resources/samples/inputs/2-physicalEquipments.csv`
- `services/backend/src/main/resources/samples/inputs/3-virtualEquipments.csv`
- `services/backend/src/main/resources/samples/inputs/4-applications.csv`

### `zip-invalid` (1)

- `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/media/~$screenshot.pptx`

### `unknown-needs-dependency` (2)

- `services/frontend/src/assets/pdf/declarations/rgesn_2024_outil_declaration_ecoconception_V0_EN.pdf`
- `services/frontend/src/assets/pdf/declarations/rgesn_2024_outil_declaration_ecoconception_V1.pdf`

### `image-file` (80)

- `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/media/01_ds_module_access.png`
- `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/media/02_create_a_ds.png`
- `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/media/03_edit_name_create_device.png`
- `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/media/04_add_device.png`
- `services/documentation/content/1-getting-started/1-getting-started-as-a-user/1-module-digital-service/media/05_edit_device.png`

### `encoding-utf16-bom` (1)

- `services/documentation/content/3-technical-documentation/4-backend_documentation/db_documentation/access_management/common_references_tables/_index.md`

### `encoding-utf8-bom` (3)

- `services/backend/src/main/resources/db/changelog/data/data_for_ref_item_impact_water_use.csv`
- `services/documentation/content/1-getting-started/1-getting-started-as-a-user/2-module-information-system/documents/datacenter_Sample.csv`
- `services/documentation/content/1-getting-started/1-getting-started-as-a-user/2-module-information-system/documents/virtualEquipment_Sample.csv`

### `html` (92)

- `services/frontend/nginx/error.html`
- `services/frontend/src/app/app.component.html`
- `services/frontend/src/app/layout/about-us/declarations/declarations.component.html`
- `services/frontend/src/app/layout/about-us/useful-information/useful-information.component.html`
- `services/frontend/src/app/layout/administration/administration-panel/administration-panel.component.html`

### `ready` (354)

- `services/backend/README.md`
- `services/backend/spec/applicationViewOptimizationFeature/solution.md`
- `services/backend/spec/applicationViewOptimizationFeature/specification.md`
- `services/backend/src/main/resources/db/changelog/data/ref_network_type.csv`
- `services/backend/src/main/resources/db/changelog/data/ref_server_host.csv`
