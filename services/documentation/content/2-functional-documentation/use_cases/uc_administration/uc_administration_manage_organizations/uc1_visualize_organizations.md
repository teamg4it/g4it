---
title: '3.2.1 Visualize organizations'
description: "This use case describes how to visualize the organizations in the administration module"
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

This use case allows an administrator to visualize organizations related to a subscriber

**Navigation Path**  
Administration panel / Manage organizations

**Access Conditions**  
The connected user must have the administrator role for at least one subscriber.

## State Diagram

{{< mermaid >}}
graph TD;
Step1[Admin Panel] --> |Open Organization Tab|Step2[Organization Panel] --> |Choose a subscriber|Step3[List of organizations]

{{< /mermaid >}}

## Mockup

![uc1_visualize_orga.png](../images/uc1_visualize_orga.png)

## Behavior Rules

{{% expand title="Show the detail" expanded="false" center="true"%}}
| Reference  | Section           | Elements                               | Type     | Description                                                                                                                                                                                                                                                                             |
|------------|-------------------|----------------------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Header** |                   |                                        | Group    |                                                                                                                                                                                                                                                                                         |
| 1          | Subscriber's list |                                        | Dropdown | <li><u>*initialization rules*</u>: contain all the subscriber's for which the connected user have administrator role. By default, the selected value is empty.<br><li><u>*action rules*</u>: the change of the selected subscriber trigger the update of the organization list contains |
| 2          | Edit              |                                        | Button   | <li><u>*initialization rules*</u>: display when the page is on Read mode, disable when the page is on Edit mode. Button is not display if the selected subscriber is empty.<br><li><u>*action rules*</u>: Switch on the page [3.2.2 Edit organizations.](uc2_edit_organizations.md).    |
| 3          | Configure         |                                        | Button   | <li>*action rules*: Open the "Choose the Criteria" to define the default ones of the selected organization [3.2.5 Choose Criteria](uc5_choose_criteria.md). <br>Button is not display if the selected subscriber is empty.                                                              |
| **Main**   |                   |                                        | Section  | <li><u>*initialization rules*</u>: the section is displayed only when one subscriber is selected.                                                                                                                                                                                       |
| 4          | Organization List |                                        | Group    | <li><u>*initialization rules*</u>: List the organization of the selected Subscribers                                                                                                                                                                                                    |
|            |                   | Organization                           | Label    | <li><u>*initialization rules*</u>: Display the label of the organization                                                                                                                                                                                                                |
|            |                   | Cancel the deletion of an organization | Button   | <li><u>*initialization rules*</u>: Display for the existing organizations for which the deletion have been requested and effective date is not achieved.<br><li><u>*action rules*<u/>: The cancel request is deleted and the organization are kept.                                     |
|            |                   | Deletion request information message   | Label    | <li><u>*initialization rules*</u>: Display the message "Your organization's data will be deleted on" concatenate with the deletion planned date.                                                                                                                                        |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Open Admin Panel and Organizations Tab
front ->> back: GET /api/administrator/subscriber/{userId}
back ->> front: /api/administrator/subscriber
front ->> RND: Display the list of subscribers
RND ->> front: Choose a subscriber
front ->>RND: Display the list of organizations

{{< /mermaid >}}

