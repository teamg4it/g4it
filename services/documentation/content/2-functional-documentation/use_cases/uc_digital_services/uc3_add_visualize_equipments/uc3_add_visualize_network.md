---
title: "2.3.2 Add or Visualize Network"
description: "This use case describes how to add network equipments to a digital service"
weight: 20
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Network visualization](#network-visualization)
-   [Network add / edit](#network-add--edit)

## Description

This usecase allows a project team to add network equipment into a digital service previously created.

**Navigation Path**

-   My Digital Services / My Digital Service / Visualize my resources tab / Networks / Add Network
-   My Digital Services / My Digital Service / Visualize my resources tab /Networks / Modify Network

**Access Conditions**
The connected user must have the write access for that module on the selected workspace.

## Network visualization

![uc3_add_visualize_equipments_networksTab.png](../../images/uc3_add_visualize_equipments_networksTab.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group      | Elements                        | Type   | Description                                                                                                                                                                                |
|-----------| ---------- |---------------------------------|--------| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
|           | Tab Header |  Visualize my resources         | Tab    |                                                                                                                                                                                            |
| 1         | Networks           |                         | Table  |                                                                                                                                                                                            |
| 2         |            | + Add                | button | <li><u>_initialization rules_</u>: That button is displayed if the connected user have the write right.<br><br><li><u>_action rules_</u>: That button open the window Network details.<br> |
|           | Tab        |                                 |        |      <br/>                                                                                                                                                                                      |
| 3         |            | Name                            | column |                                                                                                                                                                                           |
| 4         |            | Type                            | column |                                                                                                                                                                                            |
| 5         |            | Yearly quantity of GB exchanged | column |                                                                                                                                                                                            |
| 6         |            | Edit                            | button | <li><u>_action rules_</u>: That button open the window Network details.<br>                                                                                                                |
| 7         |            | Delete                          | button | <li><u>_action rules_</u>: Delete the network from the current Digital Service.<br> Note : The user must click on Calculate to update the footprint estimation.                            |

{{% /expand %}}

## Network add / edit

![uc3_add_visualize_equipments_NetworksAdd.png](../../images/uc3_add_visualize_equipments_NetworksAdd.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group           | Elements                                             | Type                 | Description                                                                                          |
|-----------| --------------- |------------------------------------------------------| -------------------- | ---------------------------------------------------------------------------------------------------- |
|           | Title           |                                                      |                      | <li><u>_initialization rules_</u>: Coming from "Add Network", the title is "New Network" else "xxx". |
| 1         |                 | New Network or Edit Network                          | label input          |                                                                                                    |
| 2         |                 | Name                                                 | label input          |                                                                                                    |
| 3         |                 | Type                                                 | dropdown             |                                                                                                      |
| 4         |                 | Total quantity of GB exchanged by year for all users | Decimal number input |                                                                                                      |
| 5         | Cancel          |                                                      | button               | <li><u>_action rules_</u>: That button close the window Network details.<br>                         |
| 6         | Create / Update |                                                      | button               |                                                                                                      |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram

actor RND as project team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Add" button in the digital service's networks table
front ->> back: POST /api/organizations/{organization}/workspaces/{workspace}/digital-services/{digitalServiceUid}/inputs/physical-equipments
back--> DataBase: Create network record in the in_physical_equipment table
front ->> back: GET /api/organizations/{organization}/workspaces/{workspace}/digital-services/{digitalServiceUid}/inputs/physical-equipments
DataBase -->> back: Get networks from the in_physical_equipment table of database
back-->> front: Send the Physical equipments for the network view
front->> RND : Display the network list view

{{< /mermaid >}}
