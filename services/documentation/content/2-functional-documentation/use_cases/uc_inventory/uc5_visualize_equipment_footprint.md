---
title: "1.5. Visualize equipment footprint"
description: "This use case describes how to visualize the equipment's impact of my IS"
weight: 50
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Mockup](#mockup)
-   [State Diagram](#state-diagram)
-   [Sequence Diagram](#sequence-diagram)

## Description

The use case allows a sustainable IT leader to visualize the inventory's impact of the Information system, which is versioned to track their evolution over time.
The key indicators displayed on the radar graph are data center and equipment.
The results can be filtered by a number of criteria, such as the country of the equipment/data center.
It is also possible to display results for a single criteria only.

More information about how indicators are produced here:
https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/blob/develop/docs/MoteurDeCalculG4IT_V1.1.adoc

1. Radar graph representing the equipment's impact by criteria in people eq.
2. By overflowing, the user can collect data corresponding to a certain phase in the life cycle of the equipment, a click on a specific criteria redirects to view by criteria
3. Each tab represents the impact for one specific criterion
4. Filter's menu. On this view, filters available are :
    - Country
    - Entity
    - Equipment
    - Status
5. Each button allows seeing the impact for one specific filter
6. Quick description of what people eq. means
7. Key indicators about a Data center
8. Key indicators about Equipment

**Navigation Path**

Input :

-   My Information System / Visualize my inventories / My inventory / Equipment button

Output :

-   My Information System / Visualize my inventories / My inventory / Equipment footprint page

**Access Conditions**  
The connected user must have the read role for that module one the selected organization.

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[List of inventory view] --> Decision1{First estimation is done ?}
Decision1-->|Yes|Step2[Button 'Equipment' is available]
Decision1-->|No|Step3[Button 'Equipment' is not available]
Step2-->|Click on 'Inventory' button|Step4[Multi criteria view about the impact\n of my IS inventory is displayed]-->|Click on a filter in the radar graph bar menu|Step7[Specific filter view is displayed]
Step4-->|Click on one of the criteria impact in the bar menu|Step5[Specific view for this criteria is displayed]
Step7-->|New filters selected|Step6[View is updated according to the filters]
Step5-->|New filters selected|Step6[View is updated according to the filters]
{{< /mermaid >}}

## Mockup

### Global view

![uc5_visualize_equipment_footprint.png](../images/uc5_visualize_equipment_footprint.png)
{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group                      | Elements                                        | Sub-Elements     | Type   | Description                                                                                                                                                                                                                                                                                                                                                                                    |
| --------- | -------------------------- | ----------------------------------------------- | ---------------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Page Header                |                                                 |                  | group  |                                                                                                                                                                                                                                                                                                                                                                                                |
| 1         |                            | My Information System - <selected organization> |                  | title  | <li><u>_initialization rules_</u>: The title is concatenated with the current organization selected.                                                                                                                                                                                                                                                                                           |
| 2         |                            | IS version or name                              |                  | label  | <li><u>_initialization rules_</u>: The IS version or name of the IS selected for which the user want to visualize equipment footprint. The type of visualization (Equipment) is also shown.                                                                                                                                                                                                    |
| 3         |                            | Add a note                                      |                  | button | <li><u>_action rules_</u>: Details of the behaviour is described in _to complete_.                                                                                                                                                                                                                                                                                                             |
| 4         |                            | Download                                        |                  | button | <li><u>_action rules_</u>: Data can be exported at any time . Details of the behaviour is described in [1.7 Export files](./uc7_export_files.md).                                                                                                                                                                                                                                              |
| 5         |                            | Filter your visualization                       |                  | filter | <li><u>_action rules_</u>: Filters allow you to restrict the analysis to a particular country, entity, equipment type or equipment status. This filter applies to all graphs. <br> The corresponding values for each criteria depend on the equipment's of the selected inventory. <br> The filter icon is filled in when filters are selected. The type name of the active filters are shown. |
|           | Overall view of the impact |                                                 |                  | group  | Details of the behaviour and content are described in [Multi-criteria View](#multi-criteria-view) or in [View dedicated for each criterion](#view-dedicated-for-each-criterion)                                                                                                                                                                                                                |
| 6         |                            | Criteria selection                              |                  | tab    |                                                                                                                                                                                                                                                                                                                                                                                                |
| 7         |                            | Graphical visualization                         |                  | group  | Each pie chart provides an overall view of the impact of the information system according to the selected **criteria**.                                                                                                                                                                                                                                                                        |
| 8         |                            |                                                 | Data Consistency | button | <li><u>_action rules_</u>: Click to view graph of data consistency for all criteria. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md)</li>                                                                                                                                                                                                                |
| 9         |                            | Context-sensitive help                          |                  | group  | **Context-sensitive help** specifies certain elements of the display.                                                                                                                                                                                                                                                                                                                          |
| 10        |                            | Key indicators                                  |                  | group  | These **key indicators** contextualize the impacts observed and represent the main levers for action to reduce impacts.                                                                                                                                                                                                                                                                        |

{{% /expand %}}

### Multi-criteria view

![uc5_visualize_equipment_footprint_multi.png](../images/uc5_visualize_equipment_footprint_multi.png)
{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group                      | Elements               | Sub-Elements              | Type   | Description                                                                                                                                                                                        |
| --------- | -------------------------- | ---------------------- | ------------------------- | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Global Vision (People eq.) |                        |                           | group  |                                                                                                                                                                                                    |
| 1         |                            | Filters                |                           | list   | <li><u>_initialization rules_</u>: The possible values are 'Lifecycle', 'Country', 'Entity', 'Equipment' and 'Status'. By default, 'Lifecycle' is selected.                                        |
| 2         |                            | Graph                  |                           | radar  | <li><u>_initialization rules_</u>:The axes of the radar correspond of the criteria selected for inventory for the last estimate. Value based on the selected filters.                              |
| 3         |                            | Legend                 |                           | list   | <li><u>_initialization rules_</u>: Items are the values available for the selected filter for the inventory. <br>Each item is clickable and filter the graph. Value based on the selected filters. |
| 4         |                            | Data consistency       |                           | button | <li><u>_action rules_</u>: Click to view graph of data consistency for all criteria. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md)</li>                    |
| 5         |                            | Context-sensitive help |                           | group  | **Context-sensitive help** specifies certain elements of the display.                                                                                                                              |
|           | Key indicators             |                        |                           |        |                                                                                                                                                                                                    |
|           |                            | Data center            |                           |        |                                                                                                                                                                                                    |
| 6         |                            |                        | Number of datacenters     | label  |                                                                                                                                                                                                    |
| 7         |                            |                        | Avg. PUE                  | label  |                                                                                                                                                                                                    |
|           |                            | Cloud Services         |                           |        |                                                                                                                                                                                                    |
| 8         |                            |                        | Number of Cloud Instances | label  |                                                                                                                                                                                                    |
|           |                            | Equipment              |                           |        |                                                                                                                                                                                                    |
| 9         |                            |                        | Number of equipments      | label  |                                                                                                                                                                                                    |
| 10        |                            |                        | Avg. lifespan             | label  | The Lifespan for each equipment is calculated as(date_retrait - date_achat)/365.0 years, where date_achat defaults to current date if empty.                                                       |
| 11        |                            |                        | Low impact                | label  |                                                                                                                                                                                                    |
| 12        |                            |                        | Total power consumption   | label  |                                                                                                                                                                                                    |

{{% /expand %}}

### View dedicated for each criterion

![uc5_visualize_equipment_footprint_one.png](../images/uc5_visualize_equipment_footprint_one.png)
{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group                      | Elements                            | Sub-Elements            | Type   | Description                                                                                                                                                                                        |
| --------- | -------------------------- | ----------------------------------- | ----------------------- | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Global Vision (People eq.) |                                     |                         | group  |                                                                                                                                                                                                    |
| 1         |                            | Filters                             |                         | list   | <li><u>_initialization rules_</u>: The possible values are 'Lifecycle', 'Country', 'Entity', 'Equipment' and 'Status'. By default, 'Lifecycle' is selected.                                        |
| 2         |                            | Graph                               |                         | radar  | <li><u>_initialization rules_</u>:The axes of the radar correspond of the criteria selected for inventory for the last estimate. Value based on the selected filters.                              |
| 3         |                            | Legend                              |                         | list   | <li><u>_initialization rules_</u>: Items are the values available for the selected filter for the inventory. <br>Each item is clickable and filter the graph. Value based on the selected filters. |
| 4         |                            | Data consistency                    |                         | button | <li><u>_action rules_</u>: Click to view graph of data consistency for the selected criteria. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md)</li>           |
|           | Key indicators             |                                     |                         |        |                                                                                                                                                                                                    |
|           |                            | _If Particulate matter is selected_ |                         |        |                                                                                                                                                                                                    |
| 5         |                            |                                     | Particulate matter      | label  | <li><u>_initialization rules_</u>: Display the value of the impact for the selected criteria according the unit selected.                                                                          |
| 6         |                            |                                     | Unit selection          | radio  | <li><u>_initialization rules_</u>: The possible values are Disease incident (selected by default) and People eq.                                                                                   |
| 7         |                            |                                     | Fine particle emissions |        | <li><u>_initialization rules_</u>: **Context-sensitive help** concerning the Fine particle emissions.                                                                                              |
|           |                            | _If Ionising radiation is selected_ |                         |        |                                                                                                                                                                                                    |
|           |                            |                                     |                         |        |                                                                                                                                                                                                    |
|           |                            | _If Acidification is selected_      |                         |        |                                                                                                                                                                                                    |
|           |                            |                                     |                         |        |                                                                                                                                                                                                    |
|           |                            | _If Climate change is selected_     |                         |        |                                                                                                                                                                                                    |
|           |                            |                                     |                         |        |                                                                                                                                                                                                    |
|           |                            | _If Resource use is selected_       |                         |        |                                                                                                                                                                                                    |
|           |                            |                                     |                         |        |                                                                                                                                                                                                    |
|           |                            | ...                                 |                         |        |                                                                                                                                                                                                    |

{{% /expand %}}

### View dedicated for data consistency

![uc5_visualize_equipment_footprint_data_consistency.png](../images/uc5_visualize_equipment_footprint_data_consistency.png)
{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group | Elements         | Sub-Elements | Type   | Description                                                                                                                                                                                                                                                     |
| --------- | ----- | ---------------- | ------------ | ------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1         |       | Data consistency |              | button | <li><u>_action rules_</u>: Click to view graph of data consistency for the selected criteria. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md)</li>                                                                        |
| 2         |       | Graph            |              | bar    | <li><u>_initialization rules_</u>:The axes of the bar correspond to the criteria or filters selected, and it displays results where the impact is calculated in blue and the impact that cannot be calculated in red. The value is represented as a percentage. |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant Database
RND ->> front: Click on "Equipment" button or one of the tab in the view or one the chart of multi-criteria tab
front ->> back: GET /api/{subscriber}/{organization}/inventories?inventoryId={inventory_id}
Database--> back: Get inventory information
back -->> front: Send the details of selected inventory Id on my view related to my view
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/inventories/{inventory_id}/outputs/virtual-equipments
Database--> back: Get indicators from out_virtual_equipment table
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/inventories/{inventory_id}/inputs/virtual-equipments
Database--> back: Get virtual equipments from in_virtual_equipment table
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/inventories/{inventory_id}/indicators/equipments
Database--> back: Get indicators from out_physical_equipment table
back -->> front: Indicators aggregated to be displayed
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/physicalEquipmentsElecConsumption
Database --> back : Get total power consumption of the equipments
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/physicalEquipmentsLowImpact
Database --> back : Get the number of physical equipments which are in low impact
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/physicalEquipmentsAvgAge
Database --> back : Get average Age of physical equipments
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/datacenters
Database --> back : Get number of datacenters
front ->> back: GET /api/referential/boaviztapi/countries
Database --> back : Get referential countries from boaviztapi
front --> RND: Display the list of filters related to my view
front ->> RND: Display filtered graphs and key indicators related to the view

{{< /mermaid >}}
