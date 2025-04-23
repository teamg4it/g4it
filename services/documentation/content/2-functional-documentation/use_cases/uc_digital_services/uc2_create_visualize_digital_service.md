---
title: '2.2. Create or Visualize a digital service'
description: "This use case describes how to create a new digital service"
weight: 20
mermaid: true
---
## Table of contents
- [Table of contents](#table-of-contents)
- [Description](#description)
- [State Diagram](#state-diagram)
- [Mockup](#mockup)
- [Sequence Diagram](#sequence-diagram)

## Description

This usecase allows a user to create a digital service.

It means that user can describe all terminals, networks and servers related to a DS to evaluate its environmental footprint 

**Navigation Path**
- My Digital Services / Evaluate New Service
- My Digital Services / My Digital Service

**Access Conditions**
The connected user must have the write access for that module on the selected organization.

## State Diagram
{{< mermaid align="center">}}
graph TD;
Step1[List of digital services view] -->|Click on 'Evaluate new service' button| Step2[New service view] -->Decision1{Which type of equipments?}
Decision1 -->|Terminals| Step3[Terminals list view]
Decision1 -->|Network| Step4[Networks list view]
Decision1 -->|Non-Cloud Server| Step5[Non-Cloud Servers list view]
Decision1 -->|Cloud Services| Step51[Cloud Services list view]
Step3 -->|Click on Add Device|Step6[Add terminal view]
Step4 -->|Click on Add Network|Step7[Add network view]
Step5 -->|Click on Add Non-Cloud Server|Step8[Add non-cloud server view]
Step51 -->|Click on Add Cloud Services|Step81[Add Cloud Services view]
{{< /mermaid >}}

## Mockup

- **Create a Digital Service**
![uc2_create_visualize_digital_service_create.png](../images/uc2_create_visualize_digital_service_create.png)

- **Visualize an existing Digital Service**
![uc2_create_visualize_digital_service_visualize.png](../images/uc2_create_visualize_digital_service_visualize.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements                                                  | Type   | Description                                                                                                                                                                                                                                                                      |
|-----------|-------------|-----------------------------------------------------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Page Header |                                                           | group  |                                                                                                                                                                                                                                                                                  |
| 1         |             | My Digital Services -                                     | title  | <li><u>*initialization rules*</u>: The title is concatenated with the current organization selected.                                                                                                                                                                             |
| 2         |             | Digital Service name                                      | label  | <li><u>*initialization rules*</u>: The name of the Digital Service selected for which the user want to visualize footprint.                                                                                                                                                      |
| 3         |             | Add a note                                                | button |                                                                                                                                                                                                                                                                                  |
| 4         |             | User icon  / Share number                                 | button | <li><u>*initialization rules*</u>: That button is displayed only if the Digital Service have been shared, so not displayed during creation of the Digital Service. <br>Details of the behaviour is described in [2.8. Share digital service](uc8_share_digital_service.md).      |
| 5         |             | Copy Link                                                 | button | Details of the behaviour is described in [2.8. Share digital service](uc8_share_digital_service.md).                                                                                                                                                                             |
| 6         |             | Export                                                    | button | Data can be exported at any time . Details of the behaviour is described in [2.6 Export ](./uc6_export_digital_service.md).                                                                                                                                                      |
| 7         |             | Calculate                                                 | button | Details of the behaviour is described in [2.4. Launch estimation](uc4_launch_estimation.md).                                                                                                                                                                                     |
| 8         |             | Choose criteria                                           | button | Details of the behaviour is described in [3.2.5 Choose criteria](../uc_administration/uc_administration_manage_organizations/uc5_choose_criteria.md).                                                                                                                            |
| 9         |             | Delete                                                    | button | Details of the behaviour is described in [2.7. Delete digital service](uc7_delete_digital_service.md).                                                                                                                                                                           |
|           | Tabs        |                                                           |        |                                                                                                                                                                                                                                                                                  |
| 10        |             | Terminals / Networks / Non-Cloud Servers / Cloud Services | tab    | <li><u>*initialization rules*</u>: 4 tabs are available : Terminals / Networks / Non-Cloud Servers / Cloud Services. The selected by default is Terminals. <br>Details of the behaviour is described in [2.3. Add or Visualize equipments](uc3_add_visualize_equipments/_index). |
| 11        |             | Visualize                                                 | tab    | Details of the behaviour is described in [2.5. Visualize digital service's footprint](uc5_visualize_footprint.md).                                                                                                                                                               |

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

