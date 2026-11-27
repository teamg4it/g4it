---
title: "1.7. Export files"
description: "This use case describes how to export the impact of my IS"
weight: 70
mermaid: true
---
<!-- AI_DISCLAIMER v1.0 -->
> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Source versus evaluated usage values

### Choose the export for its intended use

- **Export spreadsheet (XLSX)** is the viewing format. It creates one worksheet
  for each CSV in the stored evaluation archive. Every cell, including numbers
  and formula-like text such as `=1+1`, is explicitly stored as text, never as
  a formula or hyperlink. Source formatting such as `0.5000` is preserved.
  Spreadsheet calculations require deliberately converting trusted values.
- **Raw CSV (ZIP)** is the unchanged machine-exchange format. Downloading it
  requires confirmation in the UI: imported text can contain formulas that a
  spreadsheet would execute. Do not open these CSV files directly in a
  spreadsheet. CSV quoting is not formula protection. Re-exporting the XLSX
  as CSV and opening that CSV can reintroduce the same risk.

The XLSX is generated on download from the same authorized stored task ZIP;
it does not reevaluate equipment, overwrite stored CSVs or fabricate metadata
for historical evaluations. Worksheet names have a numeric prefix and a safe,
possibly shortened artifact name. Each sheet retains its original headers
and data order. A non-CSV artifact, empty archive or Excel format limit
(1,048,576 rows including headers, 16,384 columns, 32,767 characters per cell)
causes an explicit error, never truncation or an automatic raw-CSV fallback.

The raw inventory export ZIP distinguishes two physical-equipment artifacts:

- `physical_equipment.csv` is the source/input export for that evaluation. Its
  optional `contexteUsage` and `fractionUsage` cells retain source text, for
  example `0.5000`. No default or effective fraction replaces a raw source value.
  Evaluation does not change source quantity or electricity.
- `ind_physical_equipment.csv` contains evaluated rows per source equipment,
  criterion and lifecycle stage, with the two usage columns appended. It uses
  persisted task/source-equipment metadata, not current input or recomputed
  defaults.

| Evaluation-time usage | Evaluated contexteUsage | Evaluated fractionUsage |
|-----------------------|-------------------------|-------------------------|
| Device, BYOD, no explicit fraction | BYOD | 0.57 |
| Device, COPE, no explicit fraction | COPE | 0.85 |
| Device, COPE, explicit 0.5000 | COPE | 0.5 |
| Device, no context, explicit 0.5 | empty | 0.5 |
| Device, both inputs empty | empty | empty |
| Non-Device, BYOD and 0.5000 supplied | BYOD | 0.5000 |

Device effective fractions use plain decimals without scientific notation or
percentage signs, including `0` and `1`. Even though an empty-usage Device
evaluates at factor 1, no fraction is invented in its export. Non-Devices keep
their provided usage text (including its original numeric formatting), with no
derived default and no allocation. Plain-decimal formatting of effective
fractions does **not** rewrite raw/non-Device source text.

The existing download is
`GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/export/download`
(under the application's API base). It selects the latest evaluation task by
creation date and streams that task's stored ZIP; this is not a new task-id
endpoint or a latest-successful-task selection. Historical ZIPs are not
regenerated or backfilled with usage columns. Digital-service evaluated export
columns are unchanged.

The distinct spreadsheet endpoint is
`GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/export/spreadsheet/download`.
It returns an `.xlsx` file and enforces the same inventory ownership,
authentication and export-role restrictions as the raw download. Existing
export access restrictions are not widened by this format.

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Global view](#global-view)
-   [State Diagram](#state-diagram)
-   [Sequence Diagram](#sequence-diagram)

## Description

This use case allows a sustainable IT leader to generate and download the previously uploaded files, used to create
datacenters, physical and virtual equipment or applications, as well as their
evaluated impacts. Use XLSX for spreadsheet viewing and raw CSV ZIP for machine
exchange.

**Restrictions :**  
The export controls are displayed for inventory readers. The backend additionally
requires the existing writer, administrator or default DEMO-workspace permission
to download, and checks that the inventory belongs to the authorized workspace.
Export files generated during export are kept for 7 days on Azure storage space and deleted afterward.  
When this allowed time expires, an error message appears when user tries to download.

## Global view

'Export' button on the top right of the equipment or application view:
![uc6_exportButton.png](../images/uc6_exportButton.png)

{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Management rules | Title            | Rule description                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| ---------------- | ---------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1                | Export controls  | Inventory readers see the primary spreadsheet (XLSX) action and the separate raw CSV (ZIP) action. Raw CSV requires explicit risk confirmation. Both controls are disabled while a download is in progress. Existing backend permissions still apply. |
| 2                | Export files     | The stored CSV ZIP contains inventory.csv, datacenter.csv, application.csv, physical_equipment.csv, virtual_equipment.csv and the evaluated indicator files ind_application.csv, ind_physical_equipment.csv and ind_virtual_equipment.csv. The separate XLSX presents these artifacts as text-only worksheets. Raw CSV requires formula-risk confirmation. |
| 3                | Retention period | Export files generated during export are kept for 7 days on Azure storage space and deleted afterwards.<br>When the files have been purged (after a period of 7 days), try to download them produce an error message. <br> When a user launch a new evaluation, the button is reset from ‘Download’ to ‘Export’.                                                                                                                                          |

{{% /expand %}}

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[Application or <br>Equipment view] -->|Export spreadsheet| Step2[Retrieve stored CSV ZIP]
Step2 --> Step3[Convert to text-only XLSX]
Step3 --> Step4[XLSX downloaded]
Step1 -->|Raw CSV and risk confirmation| Step5[Stored CSV ZIP downloaded unchanged]

{{</ mermaid >}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram

actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant azure as Azure storage

    RND ->> front: Choose spreadsheet export on the equipment or application view
    front ->> back: GET /api/organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/export/spreadsheet/download
   azure ->> back: Retrieve the stored zipped files
    back ->> back: Convert CSV cells to explicit text cells
    back ->> front: XLSX workbook
    front ->> RND: Spreadsheet-safe workbook

{{</ mermaid >}}
