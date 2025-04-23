---
title: '3.2.2 Edit an organization'
description: "This use case describes how to edit an organization in the administration module"
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

This use case allows an administrator to edit organizations in an administration.
In the admin panel, when a subscriber is chosen, administrator can edit the list with pencil button at the top right.
Then, each organization can be renamed.
Administrator can save changes with tick button or cancel with cross-button.

**Navigation Path**  
Administration panel / Manage organizations

**Access Conditions**  
The connected user must have the subscriber administrator role for at least one subscriber.

## State Diagram
{{< mermaid >}}

graph TD;
Step1[Organization Panel]-->|Click on edit button|Step2[Edition mode] -->
Decision1{Save or Cancel?}
Decision1 -->|Save|Step3[Changes saved]-->Step1
Decision1 -->|Cancel|Step4[Changes discarded]-->Step1
Step2--> |Rename an organization|Decision1

{{< /mermaid >}}

## Mockup

![uc3_edit_orga.png](../images/uc2_edit_orga.png)

## Behavior Rules
{{% expand title="Show the detail" expanded="false" %}}

| Reference  | Section           | Elements                               | Sub-Elements        | Type        | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|------------|-------------------|----------------------------------------|---------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Header** |                   |                                        |                     | Group       |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| 1          | Subscriber's list |                                        |                     | Dropdown    | <li><u>*initialization rules*</u>: display all the subscriber's for which the connected user have administrator role<br><li><u>*action rules*</u>: the change of the selected subscriber trigger the update of the organization list contains                                                                                                                                                                                                                                                                                                      |
| 2          | Validate          |                                        |                     | Button      | <li><u>*initialization rules*</u>: display when the page is on Edit mode, disable when the page is on Read mode<br><li><u>*action rules*</u>: Save all the modifications and switch the page in Read mode(see detail on the [3.2.1 Visualize organizations](uc1_visualize_organizations.md))                                                                                                                                                                                                                                                       |
| 3          | Cancel            |                                        |                     | Button      | <li><u>*action rules*</u>: Cancel all the modifications and switch the page in Read mode(see detail on the Edit mode section "Read mode")                                                                                                                                                                                                                                                                                                                                                                                                          |
| 4          | Configure         |                                        |                     | Button      | <li><u>*action rules*</u>: Open the "Choose the Criteria" to define the default ones of the organization                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **Main**   |                   |                                        |                     | Group       |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| 5          | Organization List |                                        |                     | List        | <li><u>*initialization rules*</u>:: List the organization of the selected Subscribers and add as first line of the list "New Organization" blank entry                                                                                                                                                                                                                                                                                                                                                                                             |
|            |                   | Organization                           |                     | Input field | <li><u>*initialization rules*</u>:: Display the label of the organization<br><li><u>*action rules*</u>: The name can be filled in. If the name already exists, the error message "Organization already exist" is displayed. If the modification is done to add New Organization(first line of the list), the creation of the organization will be triggered on the "Add an organization" button. If the modification is done for an existing organization, the modified label will be saved when the connected user clicks on the validate button. |
| 6          |                   |                                        | Add an organization | Button      | <li><u>*initialization rules*</u>:: activate only when the New organization name is filled in with at least one character.<br><li><u>*action rules*</u>: If the name filled in already exists, the error message "Organization already exist" is displayed else consult the Details of the behaviour is described in [3.2.3 Create organization](uc3_create_organizations.md).                                                                                                                                                                     |
| 7          |                   | Delete an organization                 |                     | Button      | <li><u>*initialization rules*</u>:: Display for the existing organizations and in Write mode.<br><li><u>*action rules*</u>: Details of the behaviour is described in [3.2.4 Delete organization](uc4_delete_organizations.md).                                                                                                                                                                                                                                                                                                                     |
| 8          |                   | Cancel the deletion of an organization |                     | Button      | <li><u>*initialization rules*</u>:: Display for the existing organizations for which the deletion have been requested and effective date is not achieved.<br><li><u>*action rules*</u>: The cancel request is deleted and the organization are kept.                                                                                                                                                                                                                                                                                               |
| 9          |                   | Deletion request information message   |                     | Label       | <li><u>*initialization rules*</u>:: Display the message "Your organization's data will be deleted on" concatenate with the deletion planned date.                                                                                                                                                                                                                                                                                                                                                                                                  |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Open Edition mode
RND ->> front: Change the name of an organization
RND ->> front: Click on Confirm button
front ->> back: PUT /api/administrator/organizations
front ->> RND: Display the list of organizations

{{< /mermaid >}}
