---
title: "2.5. Visualize digital service's footprint"
description: "This use case describes how to visualize the impact of a digital service"
weight: 50
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Sequence Diagram](#sequence-diagram)

## Description

The use case allows a project team to visualize the impacts of terminals, networks and servers of a digital service.
The key indicators displayed on the radar graph are terminal, network and server equipment.
The results can be filtered by the type of equipment.
It is also possible to display results for a single criteria only.

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

### Global view

![uc5_visualize_footprint_visualize_Footprint.png](../images/uc5_visualize_footprint_visualizeFootprint.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group                      | Elements                                      | Sub-Elements     | Type   | Description                                                                                                                                                                                        |
| --------- | -------------------------- | --------------------------------------------- | ---------------- | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Page Header                |                                               |                  | group  |                                                                                                                                                                                                    |
| 1         |                            | My Digital Services - (selected organization) |                  | title  | <li><u>_initialization rules_</u>: The title is concatenated with the current organization selected.                                                                                               |
| 2         |                            | The digital service name                      |                  | label  | <li><u>_initialization rules_</u>: Name of the selected digital service for which the user want to visualize footprints. The type of visualization (Equipment) is also shown.                      |
| 3         |                            | Add a note                                    |                  | button | <li><u>_action rules_</u>: Details of the behaviour is described in _to complete_.                                                                                                                 |
| 4         |                            | Copy Link                                     |                  | button | <li><u>_action rules_</u>: Copy the link to share with other members . Details of the behaviour is described in [2.8. Share a digital service](./uc8_share_digital_service.md).                    |
| 5         |                            | Export                                        |                  | button | <li><u>_action rules_</u>: Data can be exported at any time . Details of the behaviour is described in [2.6 Export files](./uc6_export_digital_service.md).                                        |
| 6         |                            | Calculate                                     |                  | button | <li><u>_action rules_</u>: Update the digital service to enable the button.                                                                                                                        |
| 7         |                            | Criteria selection                            |                  | button | <li><u>_action rules_</u>: Update the criteria for digital service to calculate estimates upon.                                                                                                    |
| 8         |                            | Delete digital service                        |                  | button | <li><u>_action rules_</u>: Delete the digital service                                                                                                                                              |
|           | Overall view of the impact |                                               |                  | group  |                                                                                                                                                                                                    |
| 9         |                            | Raw value or People equivalent view           |                  | tab    | <li><u>_action rules_</u>: Each tab represents the impact for one specific criterion with the people eq.                                                                                           |
| 10        |                            | Criteria selection                            |                  | tab    | <li><u>_action rules_</u>: Each tab represents the impact for one specific criterion with the raw values.                                                                                          |
| 11        |                            | Graphical visualization                       |                  | group  | Radar graph representing the equipment’s impact by criteria in people eq.s **criteria**.                                                                                                           |
| 12        |                            |                                               | Equipment type   | button | <li><u>_action rules_</u>: Click to view graph of the impact for each equipment type i.e terminal, network, non- cloud server or cloud services.</li>                                              |
| 13        |                            |                                               | Graph            | radar  | <li><u>_action rules_</u>: By overflowing, the user can collect data corresponding to certain terminals, networks and servers. A click on a specific criteria redirects to view by criteria. </li> |
| 14        |                            |                                               | Data Consistency | button | <li><u>_action rules_</u>: Click to view graph of data consistency for all criteria. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md) </li>                   |
| 15        | Information card           |                                               |                  | group  | Quick description of what the graph represents.                                                                                                                                                    |

{{% /expand %}}

### View by criteria

![uc5_visualize_footprint_criteria_view.png](../images/uc5_visualize_footprint_criteria_view.png)

### View by equipment

![uc5_visualize_footprint_view_byEquipment.png](../images/uc5_visualize_footprint_view_byEquipment.png)

### View by equipment and criteria

![uc5_visualize_footprint_equipment_and_criteria_view.png](../images/uc5_visualize_footprint_equipment_and_criteria_view.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group                          | Elements      | Sub-Elements | Type         | Description                                                                 |
| --------- | ------------------------------ | ------------- | ------------ | ------------ | --------------------------------------------------------------------------- |
| 1         | View by criteria and equipment |               |              | group        |                                                                             |
| 2         |                                | Graph by type |              | radio button | <li><u>_action rules_</u>: Click the radio button to see the filtered view. |

{{% /expand %}}

### Data consistency view

![uc5_visualize_footprint_dataConsistency.png](../images/uc5_visualize_footprint_dataConsistency.png)
{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group | Elements      | Sub-Elements | Type         | Description                                                                                                                                                       |
| --------- | ----- | ------------- | ------------ | ------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1         |       | Graph by type |              | radio button | <li><u>_action rules_</u>: Click the radio button to see the filtered view. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md) |

{{% /expand %}}

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
