---
title: "2.3.1.1 Add Terminal using form"
description: "This use case describes how to add Terminals equipments to a digital service  using a form"
weight: 10
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Terminals visualization](#terminals-visualization)
-   [Terminals add / edit](#terminals-add--edit)

## Description

This use case allows a project team to add Terminal equipment into a digital service version directly via form, previously created.

**Navigation Path**

-   My Digital Services / Digital Service Version view / Visualize my resources tab / Terminals / Add Device
-   My Digital Services / Digital Service Version view / Visualize my resources tab / Terminals / Modify Device

**Access Conditions**
The connected user must have the write access for that module on the selected organization.

## Terminals add / edit

![uc3_add_visualize_equipments_terminalsAdd.png](../../../images/uc3_add_visualize_equipments_terminalsAdd.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group           | Elements                     | Type                 | Description                                                                                        |
|-----------| --------------- |------------------------------|----------------------| -------------------------------------------------------------------------------------------------- |
|           | Title           |                              |                      | <li><u>_initialization rules_</u>: Coming from "Add Device", the title is "New Device" else "xxx". |
| 1         |                 | New Device or Edit Device    | title                |                                                                                                    |
| 2         |                 | Name                         | label input          |                                                                                                    |
| 3         |                 | Type of device               | dropdown             |                                                                                                    |
| 4         |                 | Country                      | dropdown             |                                                                                                    |
| 5         |                 | Number of unique user        | Entire number input  |                                                                                                    |
| 6         |                 | Yearly usage time per user   | Decimal number input |                                                                                                    |
| 7         |                 | Average device lifespan (years) | Decimal number input |                                                                                                    |
| 8         | Cancel          |                              | button               | <li><u>_action rules_</u>: That button close the window Device details.<br>                        |
| 9         | Create / Update |                              | button               |                                                                                                    |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram

actor RND as project team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Add" button in the digital service's terminal table
front ->> back: POST /api/subscribers/{subscriber}/organizations/{organization}/digital-service-version/{digitalServiceVersionUid}/inputs/physical-equipments
back--> DataBase: Create terminal record in the in_physical_equipment table
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/digital-service-version/{digitalServiceVersionUid}/inputs/physical-equipments
DataBase -->> back: Get terminals from the in_physical_equipment table of database
back -->> front: Send the Physical equipments for the terminal view
front->> RND : Display the terminal list view

{{< /mermaid >}}
