---
title: "2.1. Visualize My digital services"
description: "This use case describes how to create a new digital service"
weight: 10
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Digital services global view](#digital-services-global-view)
-   [State Diagram](#state-diagram)
-   [Sequence Diagram](#sequence-diagram)

## Description

This usecase allows a user to create a digital service.
It means that user can describe all terminals, networks and servers related to a DS to evaluate its environmental footprint

**Navigation Path**
My Digital Services / Visualize my digital services

**Access Conditions**
The connected user must have the write access for that module on the selected organization.

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[List of digital services view] -->|Click on 'Evaluate new service' button| Step2[New service view] -->Decision1{Which type of equipments?}
Decision1 -->|Terminals| Step3[Terminals list view]
Decision1 -->|Network| Step4[Networks list view]
Decision1 -->|Non-Cloud Server| Step5[Non-Cloud Servers list view]
Decision1 -->|Cloud Services| Step9[Cloud Services list view]
Step3 -->|Click on Add Device|Step6[Add terminal view]
Step4 -->|Click on Add Network|Step7[Add network view]
Step5 -->|Click on Add Non-Cloud Server|Step8[Add non-cloud server view]
Step9 -->|Click on Add Cloud Services|Step81[Add Cloud Services view]
{{< /mermaid >}}

## Mockup

### Digital services global view

![uc1_visualize_digital_services_main.png](../images/uc1_visualize_digital_services_main.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group         | Elements                       | Sub-Elements               | Type           | Description                                                                                                                                                                                                                                                                                                                                                                                     |
| --------- | ------------- | ------------------------------ | -------------------------- | -------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Page Header   |                                |                            | group          |                                                                                                                                                                                                                                                                                                                                                                                                 |
| 1         |               | Title                          |                            | page Title     | <li><u>_initialization rules_</u>: The title is concatenated with the current organization selected: "My Digital Services -" + "_current organization_"                                                                                                                                                                                                                                         |
| 2         |               | Evaluation New Service         |                            | button         | <li><u>_initialization rules_</u>: Display the button if the connected user have write right.<br><li><u>_action rules_</u>: Details of the behaviour is described in [2.2. Create or Visualize a digital service](uc2_create_visualize_digital_service.md).                                                                                                                                     |
|           | Page Content  |                                |                            | group          |                                                                                                                                                                                                                                                                                                                                                                                                 |
| 3         | Created by me |                                |                            | Section        | <li><u>_initialization rules_</u>: The section title is the concatenation of "Created by me (" + "_number of Digital Services created by me_" +")"                                                                                                                                                                                                                                              |
| 4         |               | Digital Services created by me |                            | List           | <li><u>_initialization rules_</u>: Digital Services created by the user are listed from the most recent.                                                                                                                                                                                                                                                                                        |
| 7         |               |                                | Digital Service name       | label          |                                                                                                                                                                                                                                                                                                                                                                                                 |
| 8         |               |                                | Share number               | button + label | <li><u>_initialization rules_</u>: The button is concatenation with "(" + "_the number of users with whom the current user shared the Digital Service_" + ")". If the user didn't share the digital service with any other user, the button is not visible."<br><li><u>_action rules_</u>: Details of the behaviour is described in [2.8. Share digital service](uc8_share_digital_service.md). |
| 9         |               |                                | Copy Link                  | button         | <li><u>_action rules_</u>: Details of the behaviour is described in [2.8. Share digital service](uc8_share_digital_service.md).                                                                                                                                                                                                                                                                 |
| 10        |               |                                | Add a note                 | button         |                                                                                                                                                                                                                                                                                                                                                                                                 |
| 11        |               |                                | Delete                     | button         | <li><u>_action rules_</u>: Details of the behaviour is described in [2.7. Delete digital service](uc7_delete_digital_service.md).                                                                                                                                                                                                                                                               |
| 5         | Share with me |                                |                            | Section        | <li><u>_initialization rules_</u>: The section title is the concatenation of "Shared with me (" + "_number of Digital Services share with me_" + ")"                                                                                                                                                                                                                                            |
| 6         |               | Digital Services share with me |                            | List           | <li><u>_initialization rules_</u>: Digital Services share with the user are listed from the most recent.                                                                                                                                                                                                                                                                                        |
| 7         |               |                                | Digital Service name       | label          |                                                                                                                                                                                                                                                                                                                                                                                                 |
| 8         |               |                                | Share number               | button + label | <li><u>_initialization rules_</u>: The button is concatenation with "(" + "_the number of users with whom the creator user shared the Digital Service_" + ")"<br><li><u>_action rules_</u>: Details of the behaviour is described in [2.8. Share digital service](uc8_share_digital_service.md).                                                                                                |
| 9         |               |                                | Copy Link                  | button         | <li><u>_action rules_</u>: Details of the behaviour is described in [2.8. Share digital service](uc8_share_digital_service.md).                                                                                                                                                                                                                                                                 |
| 10        |               |                                | Add a note / Note attached | button         |                                                                                                                                                                                                                                                                                                                                                                                                 |
| 11        |               |                                | Hide                       | button         | <li><u>_action rules_</u>: Details of the behaviour is described in [2.8. Share digital service](uc8_share_digital_service.md).                                                                                                                                                                                                                                                                 |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on Evaluate New Service
front ->> back: POST /api/{subscriber}/{organization}/digital-services
back ->> DataBase: Create the service
back ->> front: /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}
back ->> front: Display the service in the suited list

{{< /mermaid >}}
