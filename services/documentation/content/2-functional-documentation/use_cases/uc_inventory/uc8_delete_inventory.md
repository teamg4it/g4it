---
title: '1.8. Delete inventory'
description: "This use case describes how to delete an inventory"
weight: 80
mermaid: true
---
## Table of contents
- [Table of contents](#table-of-contents)
- [Description](#description)
- [Global view](#global-view)
- [State Diagram](#state-diagram)
- [Sequence Diagram](#sequence-diagram)


## Description

This usecase allows a user with write access role to delete an inventory, it can be a version of Information System or a simulation of one.

**Navigation Path**

My Information System / Visualize my inventories / My inventory / Delete button

**Access Conditions**  
The connected user must have the read role for that module one the selected workspace.

## Global view

'Delete' button on the bottom left of IS or Simulation view :
![uc7_delete_button.png](../images/uc7_deletebutton.png)

On click of the delete button, a warning and confirmation message appears : 
![uc7_warning_confirmation.png](../images/uc7_warningconfirmation.png)

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[IS or Simulation view] --> |Click on 'Delete' button| Step2[Confirm the action] -->|No|Step1
Step2 -->|Yes|Step3[Inventory deleted]
{{</ mermaid >}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Delete" button
front ->> back: DELETE /api/{organization}/{workspace}/inventories/{inventory_id}/indicators
back -> DataBase: Delete the corresponding indicators
front ->> back: DELETE /api/{organization}/{workspace}/inventories/{inventory_id}
back -> DataBase: Delete the inventory and corresponding data from tables in_physical_equipment,<br> in_virtual_equipment, in_datacenter, in_application and tasks
back ->> front: Remove the inventory in the suited list
{{</ mermaid >}}
