---
title: "2.3.4.1. Add Public Cloud - IaaS using the form"
description: "This use case describes how to add Public Cloud - IaaS equipments to a digital service using a form"
weight: 20
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Public Cloud - IaaS add / edit](#public-cloud---iaas-addedit)

## Description

This use case allows a project team to add Public Cloud - IaaS equipment directly via form, into a digital service version previously created.

**Navigation Path**

-   My Digital Services / Digital Service Version view / Visualize my resources tab / Public Cloud - IaaS / Add Cloud Service

## Public Cloud - IaaS add/edit

![uc3_add_visualize_equipments_CloudServiceAdd.png](../../../images/uc3_add_visualize_equipments_CloudServiceAdd.png)


{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group        | Elements               | sub-Elements                   | Type                | Description                                                                                                       |
| --------- | ------------ | ---------------------- | ------------------------------ | ------------------- | ----------------------------------------------------------------------------------------------------------------- |
| 1         | Title        |                        |                                |                     | <li><u>_initialization rules_</u>: Coming from "Add", the title is "New Cloud Instance" else "xxx". |
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

RND ->> front: Click on "Add" button in the digital service version's Public Clouds - IaaS table
front ->> back: POST /api/organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/virtual-equipments
back--> DataBase: Create cloud record in the in_virtual_equipment table
front ->> back: GET /api/organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/virtual-equipments
DataBase -->> back: Get cloud services from the in_virtual_equipment table
back -->> front: Send the virtual equipments for the cloud service view
front->> RND : Display the cloud service list view

{{< /mermaid >}}
