---
title: "2.3.4.1. Add Cloud Services using the form"
description: "This use case describes how to add Cloud Services equipments to a digital service using a form"
weight: 20
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Cloud Services add / edit](#cloud-services-add--edit)

## Description

This usecase allows a project team to add Cloud Services equipment directly via form, into a digital service previously created.

**Navigation Path**

-   My Digital Services / My Digital Service / Visualize my resources tab / Cloud Services / Add Cloud Service

## Cloud Services add

![uc3_add_visualize_equipments_CloudServiceAdd.png](../../../images/uc3_add_visualize_equipments_CloudServiceAdd.png)


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

RND ->> front: Click on "Add" button in the digital service's Cloud Servers table
front ->> back: POST /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/inputs/virtual-equipments
back--> DataBase: Create cloud record in the in_virtual_equipment table
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/inputs/virtual-equipments
DataBase -->> back: Get cloud services from the in_virtual_equipment table
back -->> front: Send the virtual equipments for the cloud service view
front->> RND : Display the cloud service list view

{{< /mermaid >}}
