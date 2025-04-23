---
title: '2.3. Add or Visualize equipments'
description: "This use case describes how to add equipments to a digital service"
weight: 30
mermaid: true
---
## Table of contents
- [Table of contents](#table-of-contents)
- [Description](#description)
- [State Diagram](#state-diagram)
- [Mockup](#mockup)
- [Sequence Diagram](#sequence-diagram)

## Description

This usecase allows a project team to add equipment into a digital service previously created.
It means that user can describe all terminals, networks, non-cloud servers, cloud services related to a DS to evaluate its environmental footprint, regardless of whether the service is newly created or an older one

**Navigation Path**

My Digital Services / My Digital Service /

**Access Conditions**

The connected user must have write access to that module in the selected organization.

## State Diagram
{{< mermaid >}}
graph TD;
Step1[Service view] -->Decision1{Choose an equipment tab}
Decision1 -->|Terminals| Step2[Terminals list view]
Decision1 -->|Network| Step3[Networks list view]
Decision1 -->|Non-Cloud Servers| Step4[Servers list view]
Decision1 -->|Cloud Services| Step12[Cloud Service list view]
Step2 -->|Click on Add Device|Step5[Add terminal view] --> |Fill the information then press 'Add'|Step2
Step3 -->|Click on Add Network|Step6[Add network view] --> |Fill the information then press 'Add'|Step3
Step12 -->|Click on Add Cloud Service|Step13[Add cloud service view] --> |Fill the information then press 'Add'|Step12
Step4 -->|Click on Add Server|Step7[Add server view] --> |Fill the information then press 'Next'|Decision2{Which type of server?}
Decision2 -->|Dedicated|Step8[Add server 2nd view] -->|Fill the information then press 'Add'|Step4
Decision2 -->|Shared|Step9[Add server shared specific view] -->|Fill the information then press 'Next'|Step10[Virtual Machines View] -->|Click on Add VM|Step11[ADD VM view] -->|Fill the information then press 'Add'|Step10 -->|Click on 'Create'|Step4

{{< /mermaid >}}

## Mockup

[2.3.1 Add or Visualize Terminals](uc3_add_visualize_terminals.md)

[2.3.2 Add or Visualize Network](uc3_add_visualize_network.md)

[2.3.3 Add or Visualize Non Cloud Servers](uc3_add_visualize_noncloud_servers.md)

[2.3.4 Add or Visualize Cloud Services](uc3_add_visualize_cloud_services.md)

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase
participant BoaviztApi

RND ->> front: Choose a type of equipment
RND ->> front: Click on 'Add device'
front ->> back: GET /api/{subscriber}/{organization}/digital-services/device-type
back ->> front: List of device types
front ->> back: GET /api/{subscriber}/{organization}/digital-services/country
back ->> front: List of countries
RND ->> front: Click on 'Add network'
front ->> back: GET /api/{subscriber}/{organization}/digital-services/network-type
back ->> front: List of network types
RND ->> front: Click on 'Add server'
front ->> back: GET /api/{subscriber}/{organization}/digital-services/server-host
back ->> front: List of hosts

RND ->> front: Click on 'Add cloud service'
front ->> BoaviztApi: GET /api/referential/boaviztapi/countries
BoaviztApi ->> front: List of countries in Boaviztapi
front ->> BoaviztApi: GET /api/referential/boaviztapi/cloud/providers
BoaviztApi ->> front: List of cloud providers in Boaviztapi
front ->> BoaviztApi: GET /api/referential/boaviztapi/cloud/providers/instances
BoaviztApi ->> front: List of instances in Boaviztapi

RND ->> front: Fill the form
RND ->> front: Click on 'Create' button
front ->> back: PUT /api/{subscriber}/{organization}/digital-services/{digitalServiceUid}

back ->> DataBase: Update the service
back ->> front: GET /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}
back ->> front: Display the service in the suited list

{{< /mermaid >}}

