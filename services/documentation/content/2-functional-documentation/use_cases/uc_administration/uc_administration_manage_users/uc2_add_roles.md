---
title: '3.1.2 Add permissions and roles to a user'
description: "This use case describes how to add permission and roles to a user on an organization in the administration module"
weight: 20
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

This use case allows an administrator to add permissions and role to a user in the admin panel

**Navigation Path**  
Administration panel / Manage users / Add button from the user's list
Administration panel / Manage users / Edit button from the user's list

**Access Conditions**  
The connected user must have access to the previous page (Administration panel / Manage users)

## State Diagram

{{< mermaid >}}
graph TD;
Step1[View of the user list] --> |Enter an email and click on Search Button|Step2[View of the specific user] --> |Click
Add Button|Step3[Rights form] --> |Choose permissions and role to the
user|Step4[Permissions and role in a state suitable] -->|Click on Save Button|Step5[Parameters saved]--> Step1
Step4 -->|Click on Cancel Button|Step1

{{< /mermaid >}}

## Mockup

![uc2_addrole.png](../images/uc2_addrole.png)

## Behavior Rules

{{% expand title="Show the detail" expanded="false" %}}

| Reference  | Elements                  | Type          | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|------------|---------------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Header** |                           | Group         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| 1          | Organization              | label         | <li><u>*initialization rules*</u>: Display the organization on which the access and role we want to modify the information for the selected user.                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| 2          | User email                | label         | <li><u>*initialization rules*</u>: Display the email of the selected user.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Access** |                           | Group         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| 3          | Module Information System | Dropdown list | <li><u>*initialization rules*</u>: The list contains the values "Read" and "Write". By default, the value selected is the one saved for the user if the selected user is already part else it is empty.<br><li><u>*action rules*</u>: A click on the cross, empty the field.                                                                                                                                                                                                                                                                                                                     |
| 4          | Module Digital Service    | Dropdown list | <li><u>*initialization rules*</u>: The list contains the values "Read" and "Write". By default, the value selected is the one saved for the user if the selected user is already part else it is empty.<br><li><u>*action rules*</u>: A click on the cross, empty the field.                                                                                                                                                                                                                                                                                                                     |
| **Role**   |                           | Group         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| 5          | Role                      | Dropdown list | <li><u>*initialization rules*</u>: The list contains the values "User" and "Administrator". By default, the value selected is the one saved for the user if the selected user is already part else "User". <br> It is not possible to become "Administrator" for the user which the email is not in a domain allowed by the subscriber. An informative message will be displayed below the Role dropdown. <br><li><u>*action rules*</u>: When the "Administrator" role is selected, the access for Module Information System and Module Digital Service is set to "Write" and can't be modified. |
| **Footer** |                           | Group         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| 6          | Cancel                    | Button        | <li><u>*action rules*</u>: All modifications are canceled and the previous page is opened.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| 7          | Add / Save                | Button        | <li><u>*initialization rules*</u>: The button label is set to "Add" if the selected user is already part of the selected organization else the button label is set to "Save".<br><li><u>*action rules*</u>: : All modifications are saved and the previous page is opened.                                                                                                                                                                                                                                                                                                                       |


{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Click on "Search"
front ->> back: GET /api/administrator/organizations/{OrganizationID}/users?name=username
back ->>front: /administrator/organizations/{OrganizationID}/usersInfo
RND ->> front: Click on "Add"
front ->> RND: Display user form
RND ->> front: Modify a user's permissions and role
front ->> back: POST /api/administrator/organizations/{OrganizationID}/users
front ->> RND: Display the users in the suited list
{{< /mermaid >}}
