---
title: "2.7. Delete digital service/ digital service version"
description: "This use case describes how to delete a digital service/ digital service version"
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

This use case allows a project team to delete one of its digital service or digital service version when it is no longer needed. Deleting a digital service/digital service version will remove all associated data and footprint indicators from the system.
Delete button will not be visible if the digital service version is in active state. It will only be visible for draft versions.
When user clicks on digital service version, always active version is opened by default. If user wants to delete a draft version, he has to navigate to manage versions and select the draft version.

**Navigation Path**

-   My Digital Services / Digital Service list view / Delete button
-   My Digital Services / Digital Service version view / Manage Versions/ Select draft version/ Delete button

**Access Conditions**

-   The connected member must have the 'write' role for the digital service module one the selected workspace.

## State Diagram

{{< mermaid align="center" >}}
graph TD;
Step1[DS List View] -->|Click on 'Delete' button of a specific DS| Decision1{Confirm the action?}
Decision1 -->|No| Step1
Decision1 -->|Yes| Step2[Digital Service deleted]

    Step1 -->|Click on a DS version| Step3[Specific DS Version View]
    Step3 --> DecisionState{versionType = draft?}
    
    DecisionState -->|No active| Step3A[Delete button not visible]
    Step3A --> Step3
    
    DecisionState -->|Yes draft| Step4[Click on 'Delete' button]
    Step4 --> Decision2{Confirm the action?}
    Decision2 -->|No| Step3
    Decision2 -->|Yes| Step5[Digital Service Version deleted]
    Step5 --> Step1
{{< /mermaid >}}



## Global View

-   'Delete' button on the right of each DS in DS list view :
    ![uc5_deletelistview.png](../images/uc5_deletelistview.png)

-   On click of the delete button, a warning and confirmation message appears :
    ![uc5_confirmationmessage.png](../images/uc5_confirmationmessage.png)

-    Digital Service Version with active status (Delete button not visible) :
    ![uc7_digitalserviceversion_active.png](../images/uc7_digitalserviceversion_active.png)

-   Select draft version from Manage Versions:
    ![uc7_select_draft_version.png](../images/uc7_select_draft_version.png)

-   Digital Service Version with draft status (Delete button visible) :
    ![uc7_digitalserviceversion_draft.png](../images/uc7_digitalserviceversion_draft.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Management rules | Title           | Rule description                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|------------------|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1                | Delete          | <li><u>_initialization rules_</u>: The button is displayed only if user has the write access. <br><li><u>_action rules_</u>: The confirmation message : "Are you sure you want to delete the digital service "Digital Service name" ? All information and associated footprint indicators will be definitely deleted." is displayed.<br> If the user click on "no", the window is closed and no change.<br>If the user click on "Yes", the digital service is deleted. |
| 2                | Version Type    | Type of digital service version: It can be active, draft or archived                                                                                                                                                                                                                                                                                                                                                                                                  |
| 3                | Manage Versions | List of all the versions respect to a single digital service                                                                                                                                                                                                                                                                                                                                                                                                          |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Delete" button
front ->> back: DELETE /api/{organization}/{workspace}/digital-services/{digitalServiceUid}
back -> DataBase: Delete the digital service, corresponding data from tables in_physical_equipment,<br> in_virtual_equipment, in_datacenter and tasks
back ->> front: Remove the digital service in the suited list
front ->> back: DELETE /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}
back -> DataBase: Delete the digital service version, corresponding data from tables in_physical_equipment,<br> in_virtual_equipment, in_datacenter and tasks
back ->> front: Remove the digital service version 
{{</ mermaid >}}
