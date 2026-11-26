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

The inventory export ZIP distinguishes two physical-equipment artifacts:

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

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Global view](#global-view)
-   [State Diagram](#state-diagram)
-   [Sequence Diagram](#sequence-diagram)

## Description

This use case allows a sustainable IT leader to generate and download the previously uploaded files, used to create
datacenters, physical and virtual equipment or applications, as well as their evaluated impacts, in .csv format.

**Restrictions :**  
This is for users with "read" role access for the inventory module to access it.  
Export files generated during export are kept for 7 days on Azure storage space and deleted afterward.  
When this allowed time expires, an error message appears when user tries to download.

## Global view

'Export' button on the top right of the equipment or application view:
![uc6_exportButton.png](../images/uc6_exportButton.png)

{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Management rules | Title            | Rule description                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| ---------------- | ---------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1                | Export button    | The button is displayed for users with “read” role access on inventory module. The label of the button depends of the state of the export request. <br> If no request have been triggered, label is "Export".<br> If request have been triggered but not completed, label is "Preparing your results".<br> If request have been triggered and completed, label is "Download".<br> When a user launch a new evaluation, the button is reset from ‘Download’ to ‘Export’. |
| 2                | Export files     | The files produced are :<br> - 5 files which represent the inventory (inventory.csv, datacenter.csv, application.csv, physical_equipment.csv, virtual_equipment.csv), <br> - 3 files which represent the impact indicator of the inventory (ind_application.csv, ind_physical_equipment.csv, ind_virtual_equipment.csv).                                                                                                                                  |
| 3                | Retention period | Export files generated during export are kept for 7 days on Azure storage space and deleted afterwards.<br>When the files have been purged (after a period of 7 days), try to download them produce an error message. <br> When a user launch a new evaluation, the button is reset from ‘Download’ to ‘Export’.                                                                                                                                          |

{{% /expand %}}

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[Application or <br>Equipment view] -->|Click on 'Export' button| Step2
Step2[Retrieve  CSV files stored<br> in azure stoage] --> Step3[Files downloaded]

{{</ mermaid >}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram

actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant azure as Azure storage

    RND ->> front: Click on 'export' button on the equipment or application view
    front ->> back: GET /api/organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/export/download
   azure ->> back: Retrieve the stored zipped files
    back ->> front: Files downloaded in user's local
    front ->> RND: Exported zipped CSV files

{{</ mermaid >}}
