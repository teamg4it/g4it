---
title: "2.10. Manage digital service version"
description: "This use case describes how to manage the versions of a digital service"
weight: 100
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

This use case allows a user to manage and access different versions of a digital service in order to test various actions and hypotheses and evaluate their potential consequences. The user can view all available versions, along with relevant information such as their names and types, and can navigate between them as needed.

Access to version management is available through a dedicated button present on every digital service version. By using this feature, users can see a list of existing versions, identify their types, and open any version for further inspection or testing. This enables users to explore and compare multiple iterations of a service without impacting the original or active versions.

**Navigation Path**

-   My Digital Services / Digital Service Version view / Manage versions

## State Diagram

{{< mermaid align="center" >}}
graph TD

    Step1[List of digital services view] --> Step2[Click on a digital service]

    Step2 --> Decision1{Do you want to manage versions?}

    Decision1 -->|Yes| Step3[Click on manage version button]

    Step3 --> Step4[List of versions appear, click on version name label to access]
{{< /mermaid >}}


## Mockup


-   **Manage a Digital Service Version**
    ![uc10_manage_digital_service_version_button.png](../images/uc10_manage_digital_service_version_button.png)

-   **Visualize a Manage Digital Service Versions**
    ![uc10_manage_digital_service_version_screen.png](../images/uc10_manage_digital_service_version_screen.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements       | Type  | Description                                                                |
|-----------|-------------|----------------|-------|----------------------------------------------------------------------------|
|           | Page Header |                | group |                                                                            |
| 1         |             | Version status | label | Version status could be [active, draft, archived].                         |
| 4         |             | Version name   | label | Click on the version name to access the resources.                         |
| 5         |             | Actions        | icon  | Click on icon to duplicate the specific version of digital service version |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase


RND -->> front: Click on Manage Version
front -->> back: GET /organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/manage-versions
back -->> DataBase: fetch all digital service versions related to digital service id of digital service version
back -->> front: return all versions related to digital service id of digital service version
front -->> back: GET /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/physical-equipments
DataBase -->> back: Get indicators from in_physical_equipment table
front -->> back: GET /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/virtual-equipments
DataBase -->> back: Get indicators from in_virtual_equipment table
front -->> back: GET /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/datacenters
DataBase -->> back: Get datacenters from in_datacenter table
front -->> back: POST /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/datacenters
back -->> DataBase: Create default datacenter in in_datacenter table
front -->> back: GET /api/{organization}/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/datacenters
DataBase -->> back: Get datacenters from in_datacenter table
front -->> back: GET /api/{organization}/{workspace}/digital-services/network-type
DataBase -->> back: Get networks from ref_network_type table
front -->> back: GET /api/{organization}/{workspace}/digital-services/device-type
DataBase -->> back: Get networks from ref_device_type table
front -->> back: GET /api/{organization}/{workspace}/digital-services/server-host?type=Compute
DataBase -->> back: Get networks from ref_server_host table in which type is Compute
front -->> back: GET /api/{organization}/{workspace}/digital-services/server-host?type=Storage
DataBase -->> back: Get networks from ref_server_host table in which type is Storage
front -->> back: GET /api/referential/boaviztapi/countries
DataBase -->> back : Get referential countries from boaviztapi
back -->> front: Display the service in the suited list

{{< /mermaid >}}




