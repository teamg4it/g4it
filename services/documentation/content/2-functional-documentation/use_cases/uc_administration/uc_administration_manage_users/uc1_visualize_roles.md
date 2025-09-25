---
title: '3.1.1 Visualize roles'
description: "This use case describes how to visualize user's role and permission on a workspace in the administration module"
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
- [Sequence Diagram](#sequence-diagram)

## Description

As a Organization Administrator, user can view the list of Organization Administrators, Workspace Aministrators,
and all users who have access to the workspace.

As a Workspace Administrator, user can view workspace’s administrators and the users who have workspace access.

**Navigation Path**  
Administration panel / Manage users

**Access Conditions**  
The connected user must have the organization administrator role for at least one organization or workspace administrator role for at least one workspace(workspace).


## State Diagram

{{< mermaid >}}
graph TD;
Step1[Admin Panel] -->|Choose a workspace|Step2[View of page 1 of the list with 5 users per default] --> |Enter an
email and click on Search Button|Step3[View of the specific user]
Step2 --> |Click on Pages Button|Step4[View of page X of the list of users]
Step2 --> |Choose number of users per page|Step5[View of page 1 of the list with X users]
Step3 --> |Delete email and click on Search Button|Step2

{{< /mermaid >}}
## Mockup

### Workspace selection empty

![uc1_chooseorga.png](../images/uc1_chooseorga.png)

### One Workspace selected

![uc1_visualize_role.png](../images/uc1_visualize_role.png)

## Behavior Rules

{{% expand title="Show the detail" expanded="false" %}}

| Reference                        | Group              | Elements                  | Sub-Elements | Type        | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|----------------------------------|--------------------|---------------------------|--------------|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Select a workspace**           |                    |                           |              | Group       |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 1                                | Workspace's list   |                           |              | Dropdown    | <li><u>*initialization rules*</u>: Display all the workspace's for which the connected user have administrator role (the label displayed is on the following format "workspace label (organization label)". By default, the selected workspace is empty.<br><li><u>*action rules*</u>: the change of the selected organization trigger the update of the user list contains                                                                                                                                                                                                                                                                                                                                                               |
| 8                                | Configure          |                           |              | Dropdown    | <li><u>*action rules*</u>: Open the "Choose the Criteria" to define the default ones of the selected workspace ([3.2.5 Choose Criteria](../uc_administration_manage_workspaces/uc5_choose_criteria.md)).Button is not display if the selected workspace is empty.                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| **Search for a referenced user** |                    |                           |              | Group       | <li><u>*initialization rules*</u>: If none workspace is selected, the section is not displayed                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 2                                | Search input field |                           |              | Input field |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 3                                | Search             |                           |              | Button      | <li><u>*initialization rules*</u>: The button is activated only if the search input field contains at least 3 characters.<br><li><u>*action rules*</u>: Retrieve all users who correspond to the search input (the search is on first name, last name and email address of the users). To be retrieved, user must be registered as a member of G4IT (by login a first time in G4IT) and must have an email in a domain allows by the organization. If no user is found the error message is displayed "No referenced user found". <br> It is possible to retrieve the user for which the email is not in a domain allowed by the organization, but only if the email adress filled in matches exactly with the email of a member of G4IT. |
| 4                                | Users List         |                           |              | List        | <li><u>*initialization rules*</u>: If the logged-in user is a ***Organization Admin*** → List all users of the selected workspace, including both organization admins and workspace admins. <br> If the logged-in user is a ***Workspace Admin*** → List all users of the selected workspace, including workspace admins. (the title of the list is on the following format "workspace label - Number of users part of that workspace") (by default, 1 page contains 5 users)                                                                                                                                                                                                                                                             |
|                                  |                    | First name                |              | Label       | <li><u>*initialization rules*</u>: Display the First name of the user                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|                                  |                    | Last name                 |              | Label       | <li><u>*initialization rules*</u>: Display the Last name of the user                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|                                  |                    | Mail                      |              | Label       | <li><u>*initialization rules*</u>: Display the Email of the user                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|                                  |                    | Module Information System |              | Label       | <li><u>*initialization rules*</u>: Display the permission on Information System module for the selected user (none, Read, Write)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|                                  |                    | Module Digital service    |              | Label       | <li><u>*initialization rules*</u>: Display the permission on Digital Service module for the selected user (none, Read, Write)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
|                                  |                    | Role                      |              | Label       | <li><u>*initialization rules*</u>: Display the role of the selected user (none, User, Administrator) for that workspace                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
|                                  |                    | Actions                   |              | Label       | <li><u>*initialization rules*</u>: Display the possible actions on that user for the selected workspace                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 5                                |                    |                           | Add          | Button      | <li><u>*initialization rules*</u>: Display the Add action on the user not part of the selected workspace<br><li><u>*action rules*</u>: Details of the behavior is described in [3.1.2 Add permissions and roles to a user](uc2_add_roles.md). Note : Look at [4. Authentication](../../uc_authentification/_index.md) Activity Diagram to understand the process for the first connection.                                                                                                                                                                                                                                                                                                                                                |
| 6                                |                    |                           | Edit         | Button      | <li><u>*initialization rules*</u>: Display the Add action on the user not part of the selected workspace<br><li><u>*action rules*</u>: Details of the behaviour is described in [3.1.2 Add permissions and roles to a user](uc2_add_roles.md).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 7                                |                    |                           | Delete       | Button      | <li><u>*initialization rules*</u>: Display the Add action on the user not part of the selected workspace.<br><li><u>*action rules*</u>: Details of the behaviour is described in [3.1.3 Delete permissions and roles](uc3_delete_roles.md).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |



{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Open Admin Panel
front ->> back: GET /api/administrator/organization/{userId}
back ->> front: /administrator/organization/workspaces/{userId}
back ->> front: Display the list of workspaces
RND ->> front: Choose a workspace
front ->> back: GET /api/administrator/workspaces/{WorkspaceID}/users
back -->front: /administrator/workspaces/{WorkspaceID}/usersInfo
back ->> front: Display the users in the suited list
{{< /mermaid >}}

