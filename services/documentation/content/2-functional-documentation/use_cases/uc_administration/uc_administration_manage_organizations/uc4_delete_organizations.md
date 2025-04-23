---
title: '3.2.4 Delete organizations'
description: "This use case describes how to delete an organization in the administration module"
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

This use case allows an administrator to delete organizations in an administration
When a subscriber is chosen in the admin panel, administrator can edit the list with pencil button at the top right.
Then, each organization can be deleted.
When the administrator clicks on the trash button, a warning appears.
After confirmation of deletion, data are kept for 7 days, and during this period, administrator can cancel the deletion
at any time.

**Navigation Path**  
Administration panel / Manage organizations / Edit mode

**Access Conditions**  
The connected user must have the subscriber administrator role for at least one subscriber.

## State Diagram

{{< mermaid >}}
graph TD;
Step1[Organization Panel]-->|Click on edit button|Step2[Edition mode]
Step2 --> |Click on organization delete|Decision1{Are you sure?}
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
| 1         | Delete an organization                 | Button | <li><u>*action rules*</u>:: Display for the existing organizations and in Write mode.<br><li><u>*action rules*</u>: Open the Delete confirmation window.                                                                                     |
| 2         | Cancel the deletion of an organization | Button | <li><u>*action rules*</u>:: Display for the existing organizations for which the deletion have been requested and effective date is not achieved.<br><li><u>*action rules*</u>: The cancel request is deleted and the organization are kept. |
| 3         | Deletion request information message   | Label  | <li><u>*action rules*</u>:: Display the message 'Your organization's data will be deleted on' concatenate with the deletion planned date.                                                                                                    |

{{% /expand %}}

### Confirmation window
{{% expand title="Show the detail" expanded="false" %}}

| Reference | Elements                    | Type   | Description                                                                                                                                                                                                                                                                                                                                                                             |
|-----------|-----------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 4         | Delete confirmation         | Label  |                                                                                                                                                                                                                                                                                                                                                                                         |
| 5         | Delete confirmation message | Label  | <li><u>*initialization rules*</u>:: The message is displayed "After 7 days, all your organization's data will be deleted. are you sure you want to delete this organization ?"                                                                                                                                                                                                          |
| 6         | Close the window            | Button | <li><u>*action rules*</u>: Close the window. No deletion is performed.                                                                                                                                                                                                                                                                                                                  |
| 6         | Delete                      | Button | <li><u>*action rules*</u>: After confirmation of deletion, the window is closed and data are kept 7 days, and during this period, administrator can cancel the deletion at any time (on the organisations list, near the organization for which the deletion have been requested, Cancel deletion button and information message with the date of the effective deletion is displayed). |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Open Edition mode
RND ->> front: Click of Delete button of an organization
front ->> RND: Display a confirmation message
RND ->> front : Confirm the action
front ->> back : Parameter counter for deletion batch
front ->> RND: Display the list of organizations

{{< /mermaid >}}
