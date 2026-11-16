---
title: Repository classification
analysis_id: g4it-on-github
station: discover-repositories
source_root: services/
stack_family: mixed
generated_on: 2026-11-16
generated_by: repository-analyzer
version: "1.0"
status: validated
trace_id: discover-repositories-g4it-on-github-20261116
trace_scope: station-local-report
---

# Repository classification

> AI-generated classification from static evidence; subject to human review. Declared dependencies and documented integrations are not proof of runtime usage.

## Overall classification

**Mixed-stack web-product monorepo subtree:** a Java business backend, a TypeScript browser application, and a Python/PowerShell documentation utility. Green for IT supports environmental-impact evaluation of information-system inventories and digital services, with result visualization.

The architecture describes one backend rather than a microservice decomposition: `services/documentation/content/3-technical-documentation/2-software_architecture/_index.md:13-45,70-71`.

| Detected root                          | Repository class                                | Stack family             | Decisive signals                                                                                                                                                                                                                |
| -------------------------------------- | ----------------------------------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `services/backend`                     | Business REST application / calculation backend | `java`                   | Java 21 and Spring Boot 4.0.7 in `services/backend/pom.xml:5-16,30-32`; web/data dependencies at lines 129-155; contract-driven Spring API generation at lines 454-515.                                                         |
| `services/frontend`                    | Business SPA / presentation client              | `node`                   | Angular 21 packages, npm build scripts and TypeScript 5.9 in `services/frontend/package.json:4-15,19-51,54-70`. Node/npm is the build ecosystem; the application runs in the browser.                                           |
| `services/documentation/schemacrawler` | Database-documentation generation utility       | `python` with PowerShell | Utility purpose and PowerShell invocation at `services/documentation/schemacrawler/README.md:1-16`; Python formatter identified at lines 62-64 and implemented in `markdown.py:12-35,46-106`. Not a standalone product service. |

`mixed` reflects distinct product/runtime and supporting-tool stacks; it is not based on a source-line majority calculation. Supporting Hugo documentation and Keycloak customization do not add expected repository roots: `services/documentation/README.md:1-3`, `services/keycloak/README.md:1-3`, and repository-discovery stdout notes.

## Business and relationship signals

* **Inventory and digital-service assessment:** product architecture at `services/documentation/content/3-technical-documentation/2-software_architecture/_index.md:13-17,39-45`. Frontend consumer path fragments include inventories, digital services, reference data, workspaces, and sharing at `services/frontend/src/constants.ts:68-92`; these are not verified provider endpoints or HTTP-method contracts.
* **Frontend → backend:** documented SPA/REST split in the architecture file, lines 18-22.
* **Backend → calculation/data dependencies:** NumEcoEval calculations are a library dependency (`services/backend/pom.xml:129-134`), while PostgreSQL is a runtime dependency (lines 217-221). Inventory-file storage and exports are documented in the architecture file, lines 27-29.
* **Backend → external cloud-impact evaluation:** Boavizta integration is documented in the architecture file, line 30; no claim of implementation verification.
* **Additional generated-client signals:** Maven declares NumEcoEval input/reference API contracts and an EcoMind AI contract (`services/backend/pom.xml:541-545,584-588,628-632`). These establish build-time integration signals, not separate repositories or confirmed live service calls.
* **Schema utility → database/documentation:** documented database prerequisite and generated table-group reports (`services/documentation/schemacrawler/README.md:1-16,52-64`), corroborated by Mermaid/table formatting functions (`markdown.py:12-35,46-106`).

## Mainframe routing decision

The required scan was already performed by the caller and was **not rerun**. Evidence: `C:\Users\gtemplier\.copilot\session-state\d242aa08-0bae-4ab7-9e7c-f85f2cb00044\files\services-mf-inventory.json:1-17`.

| Routing evidence        | Finding                                                                                                   |
| ----------------------- | --------------------------------------------------------------------------------------------------------- |
| Scan scope / files      | Supplied `services/` scan; 4,655 files.                                                                   |
| Classified results      | archive 4; config 413; diagram 385; doc 456; empty 15; excluded-sensitive 1; script 8; unknown 3,373.     |
| Typed mainframe signals | No reported COBOL programs, JCL, copybooks, or other typed mainframe classifications; COBOL code lines 0. |
| Skill threshold         | Requires at least one COBOL program, or both at least one JCL file and one copybook. **Not met.**         |
| Routing result          | `stack_family: mixed`; mainframe-specific stations are not indicated by this evidence.                    |

The scan's classified ratio is 0.2754. Its unknown count limits completeness claims; do not interpret this specialized scanner's unknown files as unsupported application code.

## Evidence quality and gate

* **Discovery gate satisfied:** `python .github/skills/knowledge-plane/tools/kp_bootstrap.py repositories --source-root services/` exited 0, returned allowed/ready, found exactly the three supplied roots, and reported no problems.
* **Source preparation consistent:** complete filtered estimate of 10,500,908 bytes / 1,453 files matches the supplied preparation result. Different scan populations explain why this is not the mainframe inventory's file count.
* **Version discrepancy:** the architecture page describes Spring Boot 3 and Angular 12 (lines 39 and 57); current manifests declare Spring Boot 4.0.7 and Angular 21. Classification uses manifests for versions.
* **Scope boundary:** bounded manual discovery only, below 50 files per batch and no deeper than five directory levels. No index/graph verification, exhaustive endpoint assessment, runtime testing, or workflow-state update was attempted or implied.