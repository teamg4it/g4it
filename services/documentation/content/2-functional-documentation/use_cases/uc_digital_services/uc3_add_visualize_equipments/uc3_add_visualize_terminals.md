---
title: "2.3.1 Add or Visualize Terminals"
description: "This use case describes how to add Terminals equipments to a digital service"
weight: 10
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Terminals visualization](#terminals-visualization)
-   [Terminals add / edit](#terminals-add--edit)

## Description

This usecase allows a project team to add Terminal equipment into a digital service previously created.

**Navigation Path**

-   My Digital Services / My Digital Service / Terminals / Add Device
-   My Digital Services / My Digital Service / Terminals / Modify Device

**Access Conditions**
The connected user must have the write access for that module on the selected organization.

## Terminals visualization

![uc3_add_visualize_equipments_terminalsTab.png](../../images/uc3_add_visualize_equipments_terminalsTab.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group      | Elements                        | Type   | Description                                                                                                                                                                               |
| --------- | ---------- | ------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Tab Header |                                 | group  |                                                                                                                                                                                           |
| 1         |            | Devices                         | title  |                                                                                                                                                                                           |
| 2         |            | + Add Device                    | button | <li><u>_initialization rules_</u>: That button is displayed if the connected user have the write right.<br><br><li><u>_action rules_</u>: That button open the window Device details.<br> |
|           | Tab        |                                 |        |                                                                                                                                                                                           |
| 3         |            | Type of device                  | column |                                                                                                                                                                                           |
| 4         |            | Country                         | column |                                                                                                                                                                                           |
| 5         |            | Number of unique user           | column |                                                                                                                                                                                           |
| 6         |            | Yearly usage time per user      | column |                                                                                                                                                                                           |
| 7         |            | Average device lifespan (years) | column |                                                                                                                                                                                           |
| 8         |            | Edit                            | button | <li><u>_action rules_</u>: That button open the window Device details.<br>                                                                                                                |
| 9         |            | Delete                          | button | <li><u>_action rules_</u>: Delete the device from the current Digital Service.<br> Note : The user must click on Calculate to update the footprint estimation.                            |

{{% /expand %}}

## Terminals add / edit

![uc3_add_visualize_equipments_terminalsAdd.png](../../images/uc3_add_visualize_equipments_terminalsAdd.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group           | Elements                        | Type                 | Description                                                                                        |
| --------- | --------------- | ------------------------------- | -------------------- | -------------------------------------------------------------------------------------------------- |
|           | Title           |                                 |                      | <li><u>_initialization rules_</u>: Coming from "Add Device", the title is "New Device" else "xxx". |
| 3         |                 | Type of device                  | dropdown             |                                                                                                    |
| 4         |                 | Country                         | dropdown             |                                                                                                    |
| 5         |                 | Number of unique user           | Entire number input  |                                                                                                    |
| 6         |                 | Yearly usage time per user      | Decimal number input |                                                                                                    |
| 7         |                 | Average device lifespan (years) | Decimal number input |                                                                                                    |
| 8         | Cancel          |                                 | button               | <li><u>_action rules_</u>: That button close the window Device details.<br>                        |
| 9         | Create / Update |                                 | button               |                                                                                                    |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram

actor RND as project team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Add" button in the digital service terminal view
front ->> back: POST /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/inputs/physical-equipments
back--> DataBase: Create terminal record in the in_physical_equipment table
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/inputs/physical-equipments
DataBase -->> back: Get terminals from the in_physical_equipment table of database
back -->> front: Send the Physical equipments for the terminal view
front->> RND : Display the terminal list view

{{< /mermaid >}}
