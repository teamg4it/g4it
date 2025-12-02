---
title: "2.9. Duplicate digital service version"
description: "This use case describes how to duplicate the resources of a digital service"
weight: 90
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

This use case allows a user to manage the versions of a digital service. When a user duplicates a version, the resources are copied into a new version.
Users can see and access any versions and have informations on their names and type.

As a user, I want to be able to manage my versions of a digital service in order to test different actions and hypothesis.
I need to be able to see and access any versions and have informations on their names and type.

**Specifications**
SP1 : Version management access

User can access the version management page through a button on every digital service version.

**Navigation Path**

-   My Digital Services / Digital Service Version view / Duplicate version
-   My Digital Services / Digital Service Version view / Manage versions / Duplicate version

**Access Conditions**
The connected user must have the write access for that module on the selected workspace.

## State Diagram

{{< mermaid align="center" >}}
graph TD

    Step1[List of digital services view] --> Step2[Click on a digital service]

    Step2 --> Decision1{Do you want to duplicate active version?}

    Decision1 -->|Yes| Step3[Click on duplicate version icon]
    Decision1 -->|No| Step4[Click on manage versions icon]

    Step4 --> Step5[List of versions appear, click on duplicate version icon]
{{< /mermaid >}}


## Mockup

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase


RND -->> front: Click on Duplicate Version
front -->> back: POST /organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/duplicate
back -->> DataBase: Create duplicated digital service version
back -->> front: /organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}
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

