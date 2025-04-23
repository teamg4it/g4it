---
title: "2.7. Delete digital service"
description: "This use case describes how to delete a digital service"
weight: 70
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Global view](#global-view)
-   [Sequence Diagram](#sequence-diagram)

## Description

This usecase allows a project team to delete one of its digital service

**Navigation Path**

-   My Digital Services / "Created by me" section / Delete button
-   My Digital Services / Digital Service view / Delete button

**Access Conditions**

-   The connected member must have the 'write' role for the digital service module one the selected organization.

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[DS List View] --> |Click on 'Delete' button of a specific DS| Decision1{Confirm the action?}
Decision1 -->|No|Step1
Decision1 -->|Yes|Step2[Digital Service deleted]
Step1 --> |Click on a DS| Step3[Specific DS View] --> |Click on 'Delete' button| Decision2{Confirm the action?}
Decision2 -->|No|Step3
Decision2 -->|Yes|Step4[Digital Service deleted] -->Step1
{{</ mermaid >}}

## Global View

-   'Delete' button on the right of each DS in DS list view :
    ![uc5_deletelistview.png](../images/uc5_deletelistview.png)

-   'Delete' button on the top right of DS view :
    ![uc5_deletedsview.png](../images/uc5_deletedsview.png)

-   On click of the delete button, a warning and confirmation message appears :
    ![uc5_confirmationmessage.png](../images/uc5_confirmationmessage.png)

| Management rules | Title  | Rule description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| ---------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1                | Delete | <li><u>_initialization rules_</u>: The button is displayed only if the digital service is on the section "Created with me".<br><li><u>_action rules_</u>: The confirmation message : "Are you sure you want to delete the digital service "Digital Service name" ? All information and associated footprint indicators will be definitely deleted." is displayed.<br> If the user click on "no", the window is closed and no change.<br>If the user click on "Yes", the digital service is deleted. The Digital service is not more displayed in the section "Created with me" and "Share with me" for the shared user. |

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Delete" button
front ->> back: DELETE /api/{subscriber}/{organization}/digital-services/{digitalServiceUid}
back -> DataBase: Delete the service, corresponding equipments and the indicator data
back ->> front: Remove the service in the suited list
{{</ mermaid >}}
