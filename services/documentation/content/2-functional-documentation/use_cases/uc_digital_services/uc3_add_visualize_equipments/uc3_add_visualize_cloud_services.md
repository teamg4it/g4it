---
title: "2.3.4 Add or Visualize Cloud Services"
description: "This use case describes how to add Cloud Services equipments to a digital service"
weight: 40
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Cloud Services visualization](#cloud-services-visualization)
-   [Cloud Services add / edit](#cloud-services-add--edit)

## Description

This usecase allows a project team to add Cloud Services equipment into a digital service previously created.

**Navigation Path**

-   My Digital Services / My Digital Service / Cloud Services / Add Cloud Service
-   My Digital Services / My Digital Service / Cloud Services / Modify Cloud Service

**Access Conditions**
The connected user must have the write access for that module on the selected organization.

## Cloud Services visualization

![uc3_add_visualize_equipments_CloudServiceView.png](../../images/uc3_add_visualize_equipments_CloudServiceView.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group      | Elements                       | Type   | Description                                                                                                                                                                              |
| --------- | ---------- | ------------------------------ | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Tab Header |                                | group  |                                                                                                                                                                                          |
| 1         |            | Cloud Services                 | title  |                                                                                                                                                                                          |
| 2         |            | + Add Cloud Service            | button | <li><u>_initialization rules_</u>: That button is displayed if the connected user have the write right.<br><li><u>_action rules_</u>: That button open the window Cloud Service details. |
|           | Tab        |                                |        |                                                                                                                                                                                          |
| 3         |            | Name                           | column |                                                                                                                                                                                          |
| 4         |            | Cloud provider                 | column |                                                                                                                                                                                          |
| 5         |            | Instance type                  | column |                                                                                                                                                                                          |
| 6         |            | Quantity                       | column |                                                                                                                                                                                          |
| 7         |            | Location                       | column |                                                                                                                                                                                          |
| 8         |            | Annual usage duration (hour)   | column |                                                                                                                                                                                          |
| 9         |            | Average workload (% CPU usage) | column |                                                                                                                                                                                          |
| 10        |            | Edit                           | button | <li><u>_action rules_</u>: That button open the window Cloud Services details.                                                                                                           |
| 11        |            | Delete                         | button | <li><u>_action rules_</u>: Delete the network from the current Digital Service.<br>Note : The user must click on Calculate to update the footprint estimation.                           |

{{% /expand %}}

## Cloud Services add / edit

![uc3_add_visualize_equipments_CloudServiceAdd.png](../../images/uc3_add_visualize_equipments_CloudServiceAdd.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group        | Elements               | sub-Elements                   | Type                | Description                                                                                                       |
| --------- | ------------ | ---------------------- | ------------------------------ | ------------------- | ----------------------------------------------------------------------------------------------------------------- |
| 1         | Title        |                        |                                |                     | <li><u>_initialization rules_</u>: Coming from "Add Cloud Service", the title is "New Cloud Instance" else "xxx". |
| 2         |              | Name                   |                                | label input         |                                                                                                                   |
|           |              | Instance configuration |                                | section             |                                                                                                                   |
| 3         |              |                        | Cloud provider                 | dropdown            |                                                                                                                   |
| 4         |              |                        | Instance type                  | dropdown            |                                                                                                                   |
|           |              | Instance usage         |                                | section             |                                                                                                                   |
| 5         |              |                        | Quantity                       | Entire number input |                                                                                                                   |
| 6         |              |                        | Location                       | dropdown            |                                                                                                                   |
| 7         |              |                        | Annual usage duration (hour)   | Entire number input |                                                                                                                   |
| 8         |              |                        | Average workload (% CPU usage) | Entire number input |                                                                                                                   |
| 9         | Cancel       |                        |                                | button              | <li><u>_action rules_</u>: That button close the window Device details.<br>                                       |
| 10        | Add / Update |                        |                                | button              |                                                                                                                   |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as project team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Add" button in the digital service Cloud Server view
front ->> back: POST /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/inputs/virtual-equipments
back--> DataBase: Create cloud record in the in_virtual_equipment table
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/inputs/virtual-equipments
DataBase -->> back: Get cloud services from the in_virtual_equipment table
back -->> front: Send the virtual equipments for the cloud service view
front->> RND : Display the cloud service list view

{{< /mermaid >}}
