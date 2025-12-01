---
title: "2.1. Visualize My digital service version"
description: "This use case describes how to create a new digital service version"
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

This usecase allows a user to create a digital service version.
It means that user can describe all terminals, networks and servers related to a digital service version to evaluate its environmental footprint

**Navigation Path**
My Digital Services / Digital Service / Visualize my digital service version

**Access Conditions**
The connected user must have the write access for that module on the selected workspace.

## State Diagram

{{< mermaid align="center" >}}
graph TD
Step1[List of digital services view] -->|Click on 'Evaluate new service' button| Step2[Enter Digital Service Name & Active Version Name]
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
{{< /mermaid >}}


## Mockup

### Digital services global view

![uc1_visualize_digital_services_main.png](../images/uc1_visualize_digital_services_main.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group        | Elements               | Sub-Elements         | Type       | Description                                                                                                                                                                                                                                                         |
|-----------| ------------ | ---------------------- | -------------------- | ---------- |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Page Header  |                        |                      | group      |                                                                                                                                                                                                                                                                     |
| 1         |              | Title                  |                      | page Title | <li><u>_initialization rules_</u>: The title is concatenated with the current workspace selected: "My Digital Services -" + "_current workspace_"                                                                                                                   |
| 2         |              | Evaluation New Service |                      | button     | <li><u>_initialization rules_</u>: Display the button if the connected user have write right.<br><li><u>_action rules_</u>: Details of the behaviour is described in [2.2. Create or Visualize a digital service version](uc2_create_visualize_digital_service.md). |
|           | Page Content |                        |                      | group      |                                                                                                                                                                                                                                                                     |
| 3         |              | Digital Services       |                      | List       | <li><u>_initialization rules_</u>: Digital Services created are listed from the most recent.                                                                                                                                                                        |
| 4         |              |                        | Digital Service name | label      |                                                                                                                                                                                                                                                                     |
| 5         |              |                        | Delete               | button     | <li><u>_action rules_</u>: Details of the behaviour is described in [2.7. Delete digital service](uc7_delete_digital_service.md).                                                                                                                                   |

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
