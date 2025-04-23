---
title: '3.1.3 Delete permissions and roles'
description: "This use case describes how to delete role and permission to a user on an organization in the administration module"
weight: 30
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

This use case allows an administrator to delete the link between a user and an organization

**Navigation Path**  
Administration panel / Manage users / Delete button from the users list

**Access Conditions**  
The connected user must have access to the previous page (Administration panel / Manage users)

## State Diagram

{{< mermaid >}}
graph TD;
Step1[View of the users list] --> |Click on Delete Button for one user|Decision1{Confirmation?}
Decision1 -->|Delete|Step2[Link between the user and the organization deleted] --> Step1
Decision1 -->|No|Step1
{{< /mermaid >}}

## Mockup

![uc3_deleteconfirm.png](../images/uc3_deleteconfirm.png)
1. Confirm deletion
2. Cancel deletion

## Behavior Rules

{{% expand title="Show the detail" expanded="false" %}}

| Reference                 | Elements            | Type    | Description                                                                                                                                                             |
|---------------------------|---------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Deletion confirmation** |                     | Section |                                                                                                                                                                         |
|                           | Information message | label   | <li><u>*initialization rules*</u>: The message display is "The user user selected>will no longer have access to this organization. Are-you sure you want to proceed ?". |
| 1                         | Delete              | Button  | <li><u>*action rules*</u>: A click on the Delete button remove the link between a user and an organization and close the message window.                                |
| 2                         | Cancel / Close      | Button  | <li><u>*action rules*</u>: A click on the cross don't proceed to the deletion and close the message window.                                                             |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}

sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on Delete Button
RND ->> front: Confirm deletion
front ->> back: DELETE /api/administrator/organizations/{OrganizationID}/users{UserId} 
back --> DataBase: delete user's permissions and role
back ->> front: Display the users in the suited list

{{< /mermaid >}}
