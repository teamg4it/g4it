---
title: "2.5. Visualize digital service's footprint"
description: 'This use case describes how to visualize the impact of a digital service'
weight: 50
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

The use case allows a project team to visualize the impacts of terminals, networks and servers of a digital service.
The key indicators displayed on the radar graph are terminal, network and server equipment.

**Navigation Path**

-   My Digital Services / My Digital Service view / Visualize button

- **Access Conditions**
  The connected user must have the read access for that module on the selected organization.

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[Digital Service view] --> Decision1{First Calculation is done?}
Decision1-->|Yes|Step2[Button 'Visualize' is enabled]
Decision1-->|No|Step3[Button 'Visualize' is not enabled]
Step2-->|Click on 'Visualize' button|Step4[Multi criteria view about the impacts of my DS is displayed]-->|Click on one of the criteria impacts in the bar menu, or on the graph|Step5[Specific view for this criteria is displayed]-->|New filters selected|Step8
Step8[View is updated according to the filters]
Step8-->|Click on 'Global Vision' button|Step4
{{< /mermaid >}}

## Mockup

[2.5.1. Visualize digital service's terminal footprint](visualize_terminal_footprint.md)

[2.5.2. Visualize digital service's network footprint](visualize_network_footprint.md)

[2.5.3. Visualize digital service's non-cloud server footprint](visualize_non-cloud-server_footprint.md)

[2.5.4. Visualize digital service's cloud service footprint](visualize_cloud-service_footprint.md)

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as project team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Visualize" button in the digital service view
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/outputs/physical-equipments
DataBase-->> back: Get indicators from out_physical_equipment table
back-->> front: Send the physical equipment indicators for the multi-criteria view
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/outputs/virtual-equipments
DataBase-->> back: Get indicators from out_virtual_equipment table
back-->> front: Send the virtual equipment indicators for the multi-criteria view
front->> RND : Display the indicators by equipment type to display on my view related to my view

{{< /mermaid >}}





