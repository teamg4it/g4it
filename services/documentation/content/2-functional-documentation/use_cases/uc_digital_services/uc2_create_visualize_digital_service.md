---
title: "2.2. Create or Visualize a digital service version"
description: "This use case describes how to create a new digital service version"
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

This usecase allows a user to create a digital service version.
It means that user can describe all terminals, networks and servers related to a digital service version to evaluate its environmental footprint

Users can create a new digital service version either by clicking on the "Evaluate New Service" button or by duplicating the existing version. Details of the behaviour is described in [2.9 Duplicate digital service version](uc9_duplicate_digital_service_version.md). 


**Navigation Path**

-   My Digital Services / Evaluate New Service
-   My Digital Services / Digital Service Version view / Duplicate version
-   My Digital Services / Digital Service Version view / Manage versions / Duplicate version

**Access Conditions**
The connected user must have the write access for that module on the selected workspace.

## State Diagram

{{< mermaid align="center" >}}
graph TD
Step1[List of digital services view] --> Decision2{Create new DS version or duplicate existing DS version?}
Decision2 --> |Click on 'Evaluate new service' button| Step2[Enter Digital Service Name & Active Version Name]
    
    Step2 -->|Click on 'Validate Creation' button| Step3[New service version view]

    Step3 --> Decision1{Which type of equipments?}

    Decision1 -->|Terminals| T1[Terminals list view]
    Decision1 -->|Network| N1[Networks list view]
    Decision1 -->|Private Infrastructures| PI1[Private Infrastructure list view]
    Decision1 -->|Public Clouds - IaaS| PC1[Public Clouds - IaaS list view]

    T1 -->|Click on Add| T2[Add terminal view]
    N1 -->|Click on Add| N2[Add network view]
    PI1 -->|Click on Add| PI2[Add non-cloud server view]
    PC1 -->|Click on Add| PC2[Add cloud server view]

Decision2 --> |Duplicate the existing DS version | Step4[Click on a digital service]

    Step4 --> Decision3{Do you want to duplicate active version?}

    Decision3 -->|Yes| Step5[Click on duplicate version icon]
    Decision3 -->|No| Step6[Click on manage versions icon]

    Step6 --> Step7[List of versions appear, click on duplicate version icon]
{{< /mermaid >}}

## Mockup

-   **Create a Digital Service**
    ![uc2_create_visualize_digital_service_visualize.png](../images/uc2_create_visualize_digital_service_visualize.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements                                                             | Type   | Description                                                                                                                                                                                                                                                                                                                                                                                                   |
|-----------|-------------|----------------------------------------------------------------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Page Header |                                                                      | group  |                                                                                                                                                                                                                                                                                                                                                                                                               |
| 1         |             | Digital Service name                                                 | label  | <li><u>_initialization rules_</u>: The name of the Digital Service selected for which the user want to visualize footprint.                                                                                                                                                                                                                                                                                   |
| 2         |             | Add a note                                                           | button |                                                                                                                                                                                                                                                                                                                                                                                                               |
| 3         |             | Import                                                               | button | Click to import the cloud services and private Infrastructure using files [2.3.3.2. Add Private Infrastructure by importing files](uc3_add_visualize_equipments%2Fuc3_add_visualize_noncloud-servers%2Fimport_nonCloud_servers_via_button.md),  [2.3.4.2. Add Public Cloud - IaaS  by importing files](uc3_add_visualize_equipments%2Fuc3_add_visualize_cloud_services%2Fimport_cloud_services_via_button.md) |
| 4         |             | Export                                                               | button | Data can be exported at any time after atleast first calculation is done. Details of the behaviour is described in [2.6 Export ](./uc6_export_digital_service.md).                                                                                                                                                                                                                                            |
| 5         |             | Version name                                                         | label  | Digital service version name.                                                                                                                                                                                                                                                                                                                                                                                 |
| 6         | Tab         | Visualize my Resources                                               | tab    | The Terminals, networks, private Infrastructure and cloud Services                                                                                                                                                                                                                                                                                                                                            |
| 7         | Tables      | Terminals / Networks / Private Infrastructures / Public Cloud - IaaS | group  | <li><u>_initialization rules_</u>: 4 tables are available : Terminals / Networks / Private Infrastructures / Public Cloud - IaaS . <br>Details of the behaviour is described in [2.3. Add or Visualize equipments](uc3_add_visualize_equipments/_index).                                                                                                                                                      |
| 8         |             | Calculate my Impact                                                  | button | Details of the behaviour is described in [2.4. Launch estimation](uc4_launch_estimation.md).                                                                                                                                                                                                                                                                                                                  |
| 9         | Tab         | Visualize my results                                                 | tab    | Details of the behaviour is described in [2.5. Visualize digital service's footprint](uc5_visualize_footprint/_index.md).                                                                                                                                                                                                                                                                                     |
| 10        |             | Share/Shared                                                         | button | Click to share a digital service even outside G4it. Details of the behaviour is described in [2.8. Share a digital service](uc8_share_digital_service.md).                                                                                                                                                                                                                                                    |
| 11        |             | Manage versions                                                      | button | Click to manage the versions of a digital service. Details of the behaviour is described in [2.9.3. Manage digital service versions](manage_digital-service_version.md).                                                                                                                                                                                                                                      |
| 12        |             | Duplicate version                                                    | button | Click to share a digital service even outside G4it. Details of the behaviour is described in [2.9.2 Duplicate digital service version](duplicate_digital_service_version.md).                                                                                                                                                                                                                                         |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on Evaluate New Service
front ->> back: POST /api/{organization}/{workspace}/digital-service-version
back ->> DataBase: Create the digital service version
back ->> front: /organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}
front ->> back: GET /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/physical-equipments
DataBase-->> back: Get indicators from in_physical_equipment table
front ->> back: GET /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/virtual-equipments
DataBase-->> back: Get indicators from in_virtual_equipment table
front ->> back: GET /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/datacenters
DataBase-->> back: Get datacenters from in_datacenter table
front ->> back: POST /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/datacenters
back -->> DataBase: Create default datacenter in in_datacenter table
front ->> back: GET /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/datacenters
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
