---
title: '3.2.4 Delete workspaces'
description: "This use case describes how to delete a workspace in the administration module"
weight: 40
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

This use case allows an administrator to delete workspaces in a administration
When a organization is chosen in the admin panel, administrator can edit the list with pencil button at the top right.
Then, each workspace can be deleted.
When the administrator clicks on the trash button, a warning appears.
After confirmation of deletion, data are kept for 7 days, and during this period, administrator can cancel the deletion
at any time.

**Navigation Path**  
Administration panel / Manage workspaces / Edit mode

**Access Conditions**  
The connected user must have the organization administrator role for at least one organization
or workspace administrator role for at least one workspace.

## State Diagram

{{< mermaid >}}
graph TD;
Step1[Workspace Panel]-->|Click on edit button|Step2[Edition mode]
Step2 --> |Click on workspace delete|Decision1{Are you sure?}
Decision1 -->|Yes|Step3[7 days counter start before deletion]-->Step1
Decision1 -->|No|Step1
Step1 --> |Click on cancel delete|Step4[Deletion counter is stopped and reset to null]-->Step1
Step2 --> |Click on cancel delete|Step4

{{< /mermaid >}}

## Mockup

![uc4_delete_orga.png](../images/uc4_delete_orga.png)

## Behavior Rules

### Main page
{{% expand title="Show the detail" expanded="false" %}}

| Reference | Elements                               | Type   | Description                                                                                                                                                                                                                                  |
|-----------|----------------------------------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1         | Delete a workspace                 | Button | <li><u>*action rules*</u>:: Display for the existing workspaces and in Write mode.<br><li><u>*action rules*</u>: Open the Delete confirmation window.                                                                                     |
| 2         | Cancel the deletion of a workspace | Button | <li><u>*action rules*</u>:: Display for the existing workspaces for which the deletion have been requested and effective date is not achieved.<br><li><u>*action rules*</u>: The cancel request is deleted and the workspace are kept. |
| 3         | Deletion request information message   | Label  | <li><u>*action rules*</u>:: Display the message 'Your workspace's data will be deleted on' concatenate with the deletion planned date.                                                                                                    |

{{% /expand %}}

### Confirmation window
{{% expand title="Show the detail" expanded="false" %}}

| Reference | Elements                    | Type   | Description                                                                                                                                                                                                                                                                                                                                                                             |
|-----------|-----------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 4         | Delete confirmation         | Label  |                                                                                                                                                                                                                                                                                                                                                                                         |
| 5         | Delete confirmation message | Label  | <li><u>*initialization rules*</u>:: The message is displayed "After 7 days, all your workspace's data will be deleted. are you sure you want to delete this workspace ?"                                                                                                                                                                                                          |
| 6         | Close the window            | Button | <li><u>*action rules*</u>: Close the window. No deletion is performed.                                                                                                                                                                                                                                                                                                                  |
| 6         | Delete                      | Button | <li><u>*action rules*</u>: After confirmation of deletion, the window is closed and data are kept 7 days, and during this period, administrator can cancel the deletion at any time (on the organisations list, near the workspace for which the deletion have been requested, Cancel deletion button and information message with the date of the effective deletion is displayed). |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Open Edition mode
RND ->> front: Click of Delete button of a workspace
front ->> RND: Display a confirmation message
RND ->> front : Confirm the action
front ->> back : Parameter counter for deletion batch
front ->> RND: Display the list of workspaces

{{< /mermaid >}}
