---
title: "3.2.1 Visualize workspaces"
description: "This use case describes how to visualize the workspaces in the administration module"
weight: 10
mermaid: true
---

## Table of contents

- [Table of contents](#table-of-contents)
- [Description](#description)
- [State Diagram](#state-diagram)
- [Mockup](#mockup)
- [Behavior Rules](#behavior-rules)
- [Sequence Diagram](#sequence-diagram)

## Description

This use case allows an administrator to visualize workspaces(workspaces) related to a organization

**Navigation Path**  
Administration panel / Manage workspaces

**Access Conditions**  
The connected user must have the administrator role for at least one organization or workspace.

## State Diagram

{{< mermaid >}}
graph TD;
Step1[Admin Panel] --> |Open workspace Tab|Step2[Workspace Panel] --> |Choose a organization|Step3[List of workspaces]

{{< /mermaid >}}

## Mockup

![uc1_visualize_orga.png](../images/uc1_visualize_orga.png)

## Behavior Rules

{{% expand title="Show the detail" expanded="false" center="true"%}}
| Reference  | Section           | Elements                               | Type     | Description                                                                                                                                                                                                                                                                             |
|------------|-------------------|----------------------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Header** |                   |                                        | Group    |                                                                                                                                                                                                                                                                                         |
| 1          | Organization's list |                                        | Dropdown | <li><u>*initialization rules*</u>: contain all the organization's for which the connected user have administrator role. By default, the selected value is empty.<br><li><u>*action rules*</u>: the change of the selected organization trigger the update of the workspace list contains |
| 2          | Edit              |                                        | Button   | <li><u>*initialization rules*</u>: display when the page is on Read mode, disable when the page is on Edit mode. Button is not display if the selected organization is empty.<br><li><u>*action rules*</u>: Switch on the page [3.2.2 Edit workspaces.](uc2_edit_workspaces.md).    |
| 3          | Configure         |                                        | Button   | <li>*action rules*: Open the "Choose the Criteria" to define the default ones of the selected workspace [3.2.5 Choose Criteria](uc5_choose_criteria.md). <br>Button is not display if the selected organization is empty.                                                              |
| **Main**   |                   |                                        | Section  | <li><u>*initialization rules*</u>: the section is displayed only when one organization is selected.                                                                                                                                                                                       |
| 4          | Workspace List |                                        | Group    | <li><u>*initialization rules*</u>: List the workspace of the selected Organizations                                                                                                                                                                                                    |
|            |                   | Workspace                           | Label    | <li><u>*initialization rules*</u>: Display the label of the workspace                                                                                                                                                                                                                |
|            |                   | Cancel the deletion of an workspace | Button   | <li><u>*initialization rules*</u>: Display for the existing workspaces for which the deletion have been requested and effective date is not achieved.<br><li><u>*action rules*<u/>: The cancel request is deleted and the workspace are kept.                                     |
|            |                   | Deletion request information message   | Label    | <li><u>*initialization rules*</u>: Display the message "Your workspace's data will be deleted on" concatenate with the deletion planned date.                                                                                                                                        |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Open Admin Panel and Workspaces Tab
front ->> back: GET /api/administrator/organization/{userId}
back ->> front: /api/administrator/organization
front ->> RND: Display the list of organizations
RND ->> front: Choose a organization
front ->>RND: Display the list of workspaces

{{< /mermaid >}}

