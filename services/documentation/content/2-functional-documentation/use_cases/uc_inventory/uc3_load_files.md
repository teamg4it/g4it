---
title: "1.3. Load Files"
description: "This use case describes how to load files"
weight: 30
mermaid: true
---

## Table of contents

<!-- TOC -->
  * [Table of contents](#table-of-contents)
  * [Description](#description)
  * [State Diagram](#state-diagram)
  * [Mockup](#mockup)
    * [Loading History](#loading-history)
      * [Loading History](#loading-history-1)
      * [Loading control and process](#loading-control-and-process)
  * [Sequence Diagram](#sequence-diagram)
<!-- TOC -->

## Description

The use case allows a **Sustainable IT Leader** to upload new files into an inventory. The file types supported are *
*datacenter**, **physical equipment**, **virtual equipment**, and **application**.

The files should be in `.csv`, `.xlsx`, or `.ods` format. For **CSV files** specifically, both commas and semicolons are
supported as delimiters. Regarding encoding formats, **UTF-8**, **UTF-8 with BOM**, and **Windows-1252** are supported;
other encoding formats, while untested, may also work.

The loading of the files can have different statuses, such as pending, in progress, completed, completed with errors, or
failed, depending on the success of the loading process.

**Navigation Path**  
Input:

- My Information System > Visualize my inventories > New Inventory > Add button
- My Information System > Visualize my inventories > My inventory > Add files > Upload button  
  Output:
- My Information System > Visualize my inventories > My inventory > Loading history

**Access Conditions**  
The connected user must have write access to the selected organization for the module.

## State Diagram

The state diagram below illustrates the process flow of file uploads. It details the various steps from initiating the
file upload to determining the status of the file (pending, in progress, completed, etc.). This flow also includes
decisions regarding whether mandatory headers or validation errors are present during the file loading process.

{{< mermaid align="center">}}
graph TD;
Step1[Loading files] --> |Click on 'UPLOAD' button|Step2[Checking Files] -->
Step4[Display Pending:<br> 'To start process'] --> Decision2{Any task in progress?}
Decision2 --> |Yes|Step4
Decision2 --> |No|Step5[Display Progress:<br> 'In progress'] --> Decision3{Any mandatory header missing?}
Decision3 --> |Yes|Step6[Display Task as failed with a download button to display error details:<br> 'Failed']
Decision3 --> |No|Decision4{Any validation errors?}
Decision4 --> |Yes|Step8[Display Warning:<br> 'Completed with errors', with a download button]
Decision4 --> |No|Step9[Display Validated:<br>'Completed']
Decision4 --> |More than 50 000 lines with validation errors|Step10[Display Task as failed with a download button to display error details:<br> 'Failed']
{{</ mermaid >}}

## Mockup

### Loading History

![uc3_load_files_loading_history.png](../images/uc3_load_files_loading_history.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

#### Loading History

| Reference | Group           | Elements          | Sub-Elements | Type   | Description                                                                                                                                                                                                                                                                                                                                                                                                         |
|-----------|-----------------|-------------------|--------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Loading History |                   |              | button | Access inventory footprint for the application.<br><br><u>_initialization rules_</u>: The behavior is described in [1.2. Create or update an inventory (IS version or simulation)](uc2_create_inventory.md).                                                                                                                                                                                                        |
| 1         |                 | Load files        |              | list   | <li><u>_initialization rules_</u>: The list is ordered by try date, most recent first                                                                                                                                                                                                                                                                                                                               |
|           |                 | Loading files try |              | label  |                                                                                                                                                                                                                                                                                                                                                                                                                     |
| 2         |                 |                   | Status icon  | label  | <li><u>_initialization rules_</u>: 5 existing types: Pending (If any other inventory loading or estimation task is in progress), In Progress (Loading in progress), FAILED/Error (Loading could not be performed), Completed with errors (Loading is completed with errors) and Completed (Loading is completed).                                                                                                   |
| 3         |                 |                   | Try dates    | label  |                                                                                                                                                                                                                                                                                                                                                                                                                     |
| 4         |                 |                   | Download     | button | <li><u>_initialization rules_</u>: The button is displayed in case of "Completed with errors" which means some items could not be loaded on the inventory or in case of a 'Failed' task. Button with 'Completed with errors' trigger the download of a file containing the items in error. Items can be then corrected in the file and uploaded later. <br> Button with a 'Failed' task displays the error details. |

#### Loading control and process

| Management rules | Title                                | Rule description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|------------------|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1                | Datacenters consistency Check        | **Uniqueness Validation** : <br> All the duplicated **nomCourtDatacenter** records are rejected.                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| 2                | Physical Equipment consistency Check | **Uniqueness Validation** : <br> All the duplicated **nomEquipementPhysique** records are rejected.                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| 3                | Virtual equipment consistency check  | **Uniqueness Validation**: <br> All the duplicated **nomEquipementVirtuel** records are rejected. <br><br> **Physical Equipment Reference Check:** For the 'typeInfrastructure' non 'CLOUD_SERVICES', if the referenced **nomEquipementPhysique** does not exist in inventory or in the uploaded physical_equipment file/s, the line is rejected with message "The physical equipment {name} does not exist in the inventory." **typeInfrastructure** Field 'typeInfrastructure' supports only two values CLOUD_SERVICES and NON_CLOUD_SERVERS. |
| 4                | Application consistency check        | **Uniqueness Validation:** <br> All the duplicated records with the combination of 'nomApplication, typeEnvironnement and nomEquipementVirtuel' are rejected. <br><br> **Virtual equipment Reference Check:** If the referenced **nomEquipementVirtuel** does not exist in inventory or in the uploaded virtual_equipment file/s, the line is rejected with message "The Virtual equipment {name} does not exist in the inventory."                                                                                                             |

> [!WARNING]
> When uploading a file (datacenter, physical equipment, virtual equipment, or application) with more than 50,000 lines
> in error, the task status is set to "FAILED" and no data is inserted into the database for this file.

{{% /expand %}}

## Sequence Diagram

The sequence diagram below provides a step-by-step overview of how the file upload process occurs. It shows the
interactions between the **Sustainable IT Leader**, **G4IT Front-End**, **G4IT Back-End**, and the **Database** during
the file upload. It also highlights how the task progresses from being initiated to completion or failure, including
interactions for downloading error files if needed.

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "UPLOAD" button in the loading files view
front ->> back: POST /api/{subscriber}/{organization}/inventories/{inventory_id}/load-input-files
front -->> RND : Display the 'pending' button if another task is in progress
front -->> back: Resume loading once no other task is in progress
back -->> DataBase: Validate and load datacenters in the in_datacenter table
back -->> DataBase: Validate and load physical equipment in the in_physical_equipment table
back -->> DataBase: Validate and load virtual equipment in the in_virtual_equipment table
back -->> DataBase: Validate and load applications in the in_application table
back -->> front: Update the loading history
front ->> back: GET /api/{subscriber}/{organization}/inventories/{inventory_id}
back-->> front: Get the updated inventory
back ->> front: Display the updated loading history
front ->> RND : Display the 'completed' button if all the uploaded data is correct
front -->> RND : Display the 'failed' button if most of the uploaded data is incorrect or <br> mandatory headers missing
front ->> RND : Display the 'completed with errors' and 'download' button if some of the uploaded data is incorrect
RND ->> front : Click the 'completed with errors' button to download the rejected data
front -->> back: GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/output/{taskId}
back ->> front : Rejected files downloaded to the user's local machine
{{< /mermaid >}}
