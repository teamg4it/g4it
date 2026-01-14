---
title: "1.7. Export files"
description: "This use case describes how to export the impact of my IS"
weight: 70
mermaid: true
---

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
    front ->> back: POST /api/organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/export/download
   azure ->> back: Retrieve the stored zipped files
    back ->> front: Files downloaded in user's local
    front ->> RND: Exported zipped CSV files

{{</ mermaid >}}
