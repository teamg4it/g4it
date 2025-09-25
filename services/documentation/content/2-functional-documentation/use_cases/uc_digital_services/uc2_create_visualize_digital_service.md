---
title: "2.2. Create or Visualize a digital service"
description: "This use case describes how to create a new digital service"
weight: 20
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

This usecase allows a user to create a digital service.

It means that user can describe all terminals, networks and servers related to a DS to evaluate its environmental footprint

**Navigation Path**

-   My Digital Services / Evaluate New Service
-   My Digital Services / My Digital Service

**Access Conditions**
The connected user must have the write access for that module on the selected workspace.

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[List of digital services view] -->|Click on 'Evaluate new service' button| Step2[New service view] -->Decision1{Which type of equipments?}
Decision1 -->|Devices table| Step3[Terminals table view]
Decision1 -->|Networks table| Step4[Networks table view]
Decision1 -->|Private Infrastructures table| Step5[Private Infrastructures table view]
Decision1 -->|Public Cloud - IaaS  table| Step9[Public Cloud - IaaS table view]
Step3 -->|Click on Add|Step6[Add terminal view]
Step4 -->|Click on Add|Step7[Add network view]
Step5 -->|Click on Add|Step8[Add non-cloud server view]
Step9 -->|Click on Add|Step10[Add Cloud Services view]
{{< /mermaid >}}

## Mockup

-   **Create a Digital Service**
    ![uc2_create_visualize_digital_service_create.png](../images/uc2_create_visualize_digital_service_create.png)

-   **Visualize an existing Digital Service**
    ![uc2_create_visualize_digital_service_visualize.png](../images/uc2_create_visualize_digital_service_visualize.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements                                                  | Type   | Description                                                                                                                                                                                                                                                                                                                                                                                  |
|-----------|-------------|-----------------------------------------------------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Page Header |                                                           | group  |                                                                                                                                                                                                                                                                                                                                                                                              |
| 1         |             | Digital Service name                                      | label  | <li><u>_initialization rules_</u>: The name of the Digital Service selected for which the user want to visualize footprint.                                                                                                                                                                                                                                                                  |
| 2         |             | Add a note                                                | button |                                                                                                                                                                                                                                                                                                                                                                                              |
| 3         |             | Import                                                    | button | Click to import the cloud services and private Infrastructure using files [2.3.3.2. Add Private Infrastructure by importing files](uc3_add_visualize_equipments%2Fuc3_add_visualize_noncloud-servers%2Fimport_nonCloud_servers_via_button.md),  [2.3.4.2. Add Public Cloud - IaaS  by importing files](uc3_add_visualize_equipments%2Fuc3_add_visualize_cloud_services%2Fimport_cloud_services_via_button.md) |
| 4         |             | Export                                                    | button | Data can be exported at any time after atleast first calculation is done. Details of the behaviour is described in [2.6 Export ](./uc6_export_digital_service.md).                                                                                                                                                                                                                           |
| 5         |             | Delete                                                    | button | Details of the behaviour is described in [2.7. Delete digital service](uc7_delete_digital_service.md).                                                                                                                                                                                                                                                                                       |
| 6         | Tab         | Visualize my Resources                                    | tab    | The Terminals, networks, private Infrastructure and cloud Services                                                                                                                                                                                                                                                                                                                                |
| 7         | Tables      | Terminals / Networks / Private Infrastructures / Public Cloud - IaaS  | group  | <li><u>_initialization rules_</u>: 4 tables are available : Terminals / Networks / Private Infrastructures / Public Cloud - IaaS . <br>Details of the behaviour is described in [2.3. Add or Visualize equipments](uc3_add_visualize_equipments/_index).                                                                                                                                                 |
| 8         |             | Calculate my Impact                                       | button | Details of the behaviour is described in [2.4. Launch estimation](uc4_launch_estimation.md).                                                                                                                                                                                                                                                                                                 |
| 9         | Tab         | Visualize my results                                      | tab    | Details of the behaviour is described in [2.5. Visualize digital service's footprint](uc5_visualize_footprint/_index.md).                                                                                                                                                                                                                                                                    |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on Evaluate New Service
front ->> back: POST /api/{organization}/{workspace}/digital-services
back ->> DataBase: Create the service
back ->> front: /organizations/{organization}/workspaces/{workspace}/digital-services/{digitalServiceUid}
front ->> back: GET /api/{organization}/{workspace}/digital-services/{digitalServiceUid}/inputs/physical-equipments
DataBase-->> back: Get indicators from in_physical_equipment table
front ->> back: GET /api/{organization}/{workspace}/digital-services/{digitalServiceUid}/inputs/virtual-equipments
DataBase-->> back: Get indicators from in_virtual_equipment table
front ->> back: GET /api/{organization}/{workspace}/digital-services/{digitalServiceUid}/inputs/datacenters
DataBase-->> back: Get datacenters from in_datacenter table
front ->> back: POST /api/{organization}/{workspace}/digital-services/{digitalServiceUid}/inputs/datacenters
back -->> DataBase: Create default datacenter in in_datacenter table
front ->> back: GET /api/{organization}/{workspace}/digital-services/{digitalServiceUid}/inputs/datacenters
DataBase-->> back: Get datacenters from in_datacenter table
front ->> back: GET /api/{organization}/{workspace}/digital-services/network-type
DataBase-->> back: Get networks from ref_network_type table
front ->> back: GET /api/{organization}/{workspace}/digital-services/device-type
DataBase-->> back: Get networks from ref_device_type table
front ->> back: GET /api/{organization}/{workspace}/digital-services/server-host?type=Compute
DataBase-->> back: Get networks from ref_server_host table in which type is Compute
front ->> back: GET /api/{organization}/{workspace}/digital-services/server-host?type=Storage
DataBase-->> back: Get networks from ref_server_host table in which type is Storage
front ->> back: GET /api/referential/boaviztapi/countries
DataBase --> back : Get referential countries from boaviztapi
back ->> front: Display the service in the suited list

{{< /mermaid >}}
