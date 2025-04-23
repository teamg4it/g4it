---
title: "1.6. Visualize application footprint"
description: "This use case describes how to visualize the application's impact of my IS"
weight: 60
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

The use case allows a sustainable IT leader to visualize the application's impact of the Information system, which is
versioned to track their evolution over time.

By browsing an organization's application portfolio, you can quickly identify the application infrastructures with the
greatest environmental impact.
This empowered development teams, focuses on eco-design efforts, and identifies inconsistencies between infrastructure
sizing and actual application requirements.

More information about how indicators are produced here:
https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/blob/develop/docs/MoteurDeCalculG4IT_V1.1.adoc

**Navigation Path**

Input :

-   My Information System / Visualize my inventories / My inventory / Application button

Output :

-   My Information System / Visualize my inventories / My inventory / Application footprint page

**Access Conditions**  
The connected user must have the read role for that module one the selected organization.

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[List of inventory view] --> Decision1{First estimation is done \nand this inventory contains applications ?}
Decision1-->|Yes|Step2[Button 'Application' is available]
Decision1-->|No|Step3[Button 'Application' is not available]
Step2-->|Click on 'Application'
button|Step4[Multi criteria view about the impact\n of my IS application is displayed]-->|Click on one of a bar in the
graph representing one of the criteria|Step5[Specific view for this criteria is displayed by domain]-->|Click on one of
a bar in the graph representing one of the domain|Step6[Specific view for this criteria is displayed by subdomain]-->
|Click on one of a bar in the graph representing one of the
subdomain|Step7[Specific view for this criteria is displayed by application]-->|Click on one of a bar in the graph
representing one of the application|Step8[Visualize impact for an application by virtual equipment/environment]
{{< /mermaid >}}

## Mockup

### Multi criteria view about the impact of my IS application

![uc6_visualize_application_footprint_multicriteria.png](../images/uc6_visualize_application_footprint_multicriteria.png)

{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Reference | Group                      | Elements                  | Sub-Elements     | Type   | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| --------- | -------------------------- | ------------------------- | ---------------- | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Page Header                |                           |                  | group  |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| 1         |                            | My Information System -   |                  | title  | <li><u>initialization rules</u>: The title is concatenated with the current organization selected.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 2         |                            | IS version or name        |                  | label  | <li><u>initialization rules</u>: The IS version or name of the IS selected for which the user want to visualize equipment footprint. The type of visualization (Application) is also shown.                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| 3         |                            | Add a note                |                  | button | <li><u>action rules</u>: Details of the behaviour is described in xxx.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| 4         |                            | Export                    |                  | button | <li><u>action rules</u>: Data can be exported at any time . Details of the behaviour is described in [1.7. Export files](uc7_export_files.md).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| 5         |                            | Filter your visualization |                  | filter | <li><u>action rules</u>: Filters allow you to restrict the analysis to a particular country, entity, equipment type or equipment status. This filter applies to all graphs.<br>The corresponding values for each criteria depend on the equipment's of the selected inventory. <br> The filter icon is filled in when filters are selected. The type name of the active filters are shown.                                                                                                                                                                                                                                                   |
|           | Overall view of the impact |                           |                  | group  | Details of the behaviour and content are described in Multi-criteria View or in View dedicated for each criteria                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| 6         |                            | Criteria selection        |                  | tab    |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| 7         |                            | Graphical visualization   |                  | group  | Each bar chart provides an overall view of the impact of the information system according to the selected criteria.<br><br>On the **Multi criteria view**, the Bar chart representing the application's impact of my inventory by criteria in people eq.<br><br><li><u>_initialization rules_</u>: By overflying a bar, the impact on this specific criterion unit is displayed. <br><br><li><u>_action rules_</u>: Each tab represents the impact for one specific criterion. By clicking on a bar, the graph is updated with the lower level of granularity: by domain, then by subdomain, then by application, finally by virtual machine |
| 8         |                            |                           | Data consistency | button | <li><u>action rules</u>: Click to view graph of data consistency for the all criteria. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md)</li>                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 9         |                            | Context-sensitive help    |                  | group  | Context-sensitive help specifies certain elements of the display.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 10        |                            | Key indicators            |                  | group  | These key indicators contextualize the impacts observed and represent the main levers for action to reduce impacts.<br>For **Multi criteria view**, The number of applications for the inventory is displayed.                                                                                                                                                                                                                                                                                                                                                                                                                               |

{{% /expand %}}

### Chart by Domain

![uc6_visualize_application_footprint_by_criteria__level_Domain.png](../images/uc6_visualize_application_footprint_by_criteria__level_Domain.png)

{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Reference | Group                   | Elements         | Sub-Elements       | Type         | Description                                                                                                                                                                                                                                                                         |
| --------- | ----------------------- | ---------------- | ------------------ | ------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1         | Title                   |                  |                    |              | <li><u>_initialization rules_</u>: The title is the concatenation of the depth levels.<br><br><li><u>_action rules_</u>: By clicking on it, it allows you to go up one level.<br>                                                                                                   |
|           | Graphical visualization |                  |                    | group        |                                                                                                                                                                                                                                                                                     |
| 2         |                         | bar tooltips     |                    | label        | <li><u>_initialization rules_</u>: By overflying a bar, information is displayed :<br><br> - Domain's impact in people eq.<br><br> - Domain's impact in the unit of the chosen criteria<br><br> - Subdomain's number of the domain<br><br> - Application's number of the domain<br> |
| 3         |                         | bar chart        |                    | bar chart    | <li><u>_initialization rules_</u>: Bar chart representing the application's impact of my inventory for one criterion by domain in people eq..<br><li><u>_action rules_</u>: By clicking on a bar, the user is redirected to a graph representing this domain's impact.              |
|           | Key indicators          |                  |                    | group        | These key indicators contextualize the impacts observed and represent the main levers for action to reduce impacts.                                                                                                                                                                 |
| 4         |                         | Application      |                    |              |                                                                                                                                                                                                                                                                                     |
|           |                         |                  | Applications       | label        | <li><u>_initialization rules_</u>: Display the number of the applications taking into account the filters.                                                                                                                                                                          |
|           |                         |                  | Application impact | label        | <li><u>_initialization rules_</u>: Display the impact of the applications taking into account the unit selected.                                                                                                                                                                    |
|           |                         |                  | Unit               | Radio button | <li><u>_initialization rules_</u>: Allow to select the unit of the impact (people equivalent or kg CO2equivalent)                                                                                                                                                                   |
| 5         |                         | Lifecycle        |                    |              | <li><u>_initialization rules_</u>: Pie chart representing the impact of my application by lifecycle step                                                                                                                                                                            |
| 6         |                         | Data consistency |                    | button       | <li><u>_action rules_</u>: Click to view graph of data consistency for the selected criteria Domains.See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md)</li>                                                                                     |

{{% /expand %}}

### Chart By SubDomain

![uc6_visualize_application_footprint_by_criteria_level_subDomain.png](../images/uc6_visualize_application_footprint_by_criteria_level_subDomain.png)
{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Reference | Group                   | Elements         | Sub-Elements       | Type         | Description                                                                                                                                                                                                                                                                      |
| --------- | ----------------------- | ---------------- | ------------------ | ------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1         | Title                   |                  |                    |              | <li><u>_initialization rules_</u>: The title is the concatenation of the depth levels.<br><br><li><u>_action rules_</u>: By clicking on it, it allows you to go up one level.<br>                                                                                                |
|           | Graphical visualization |                  |                    | group        | Details of the behaviour and content are described in Multi-criteria View or in View dedicated for each criteria                                                                                                                                                                 |
| 2         |                         | bar tooltips     |                    | label        | <li><u>_initialization rules_</u>: By overflying a bar, information is displayed :<br><br> _ Subdomain's impact in people eq.<br><br> _ Subdomain's impact in the unit of the chosen criteria \* Application's number of the domain<br>                                          |
| 3         |                         | bar chart        |                    | bar chart    | <li><u>_initialization rules_</u>: Bar chart representing the application's impact of my inventory for one criterion by subdomain in people eq..<br><li><u>_action rules_</u>: By clicking on a bar, the user is redirected to a graph representing this subdomain's impact.<br> |
|           | Key indicators          |                  |                    | group        | These key indicators contextualize the impacts observed and represent the main levers for action to reduce impacts.                                                                                                                                                              |
| 4         |                         | Application      |                    |              |                                                                                                                                                                                                                                                                                  |
|           |                         |                  | Applications       | label        | <li><u>_initialization rules_</u>: Display the number of the applications taking into account the filters.                                                                                                                                                                       |
|           |                         |                  | Application impact | label        | <li><u>_initialization rules_</u>: Display the impact of the applications taking into account the unit selected.                                                                                                                                                                 |
|           |                         |                  | Unit               | Radio button | <li><u>_initialization rules_</u>: Allow to select the unit of the impact (people equivalent or kg CO2equivalent)                                                                                                                                                                |
| 5         |                         | Lifecycle        |                    |              | <li><u>_initialization rules_</u>: Pie chart representing the impact of my application by lifecycle step                                                                                                                                                                         |
| 6         |                         | Data consistency |                    | button       | <li><u>_action rules_</u>: Click to view graph of data consistency for the sub-domains of selected domain. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md) </li>                                                                           |

{{% /expand %}}

### Chart By Application

![uc6_visualize_application_footprint_by_criteria_level_Application.png](../images/uc6_visualize_application_footprint_by_criteria_level_Application.png)
{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Reference | Group                   | Elements         | Sub-Elements       | Type         | Description                                                                                                                                                                                                                                                                 |
| --------- | ----------------------- | ---------------- | ------------------ | ------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1         | Title                   |                  |                    |              | <li><u>_initialization rules_</u>: The title is the concatenation of the depth levels.<br><br><li><u>_action rules_</u>: By clicking on it, it allows you to go up one level.<br>                                                                                           |
|           | Graphical visualization |                  |                    | group        | Details of the behaviour and content are described in Multi-criteria View or in View dedicated for each criteria                                                                                                                                                            |
| 2         |                         | bar tooltips     |                    | label        | <li><u>_initialization rules_</u>: By overflying a bar, information is displayed :<br><br> _ Application's impact in people eq.<br><br> _ Application's impact in the unit of the chosen criteria<br><br>                                                                   |
| 3         |                         | bar chart        |                    | bar chart    | <li><u>_initialization rules_</u>: Bar chart representing the application's impact of my inventory for one criterion by application in people eq..<br><li><u>_action rules_</u>: By clicking on a bar, the user is redirected to a graph representing this domain's impact. |
|           | Key indicators          |                  |                    | group        | These key indicators contextualize the impacts observed and represent the main levers for action to reduce impacts.                                                                                                                                                         |
| 4         |                         | Application      |                    |              |                                                                                                                                                                                                                                                                             |
|           |                         |                  | Applications       | label        | <li><u>_initialization rules_</u>: Display the number of the applications taking into account the filters.                                                                                                                                                                  |
|           |                         |                  | Application impact | label        | <li><u>_initialization rules_</u>: Display the impact of the applications taking into account the unit selected.                                                                                                                                                            |
|           |                         |                  | Unit               | Radio button | <li><u>_initialization rules_</u>: Allow to select the unit of the impact (people equivalent or kg CO2equivalent)                                                                                                                                                           |
| 5         |                         | Lifecycle        |                    |              | <li><u>_initialization rules_</u>: Pie chart representing the impact of my application by lifecycle step                                                                                                                                                                    |
| 6         |                         | Data consistency |                    | button       | <li><u>_action rules_</u>: Click to view graph of data consistency for the application levels of selected sub-domain. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md) </li>                                                           |

{{% /expand %}}

### Chart By virtual machine

![uc6_visualize_application_footprint_by_criteria_level_VM.png](../images/uc6_visualize_application_footprint_by_criteria_level_VM.png)
{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Reference | Group                   | Elements         | Sub-Elements       | Type         | Description                                                                                                                                                                                                                                              |
| --------- | ----------------------- | ---------------- | ------------------ | ------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1         | Title                   |                  |                    |              | <li><u>_initialization rules_</u>: The title is the concatenation of the depth levels.<br><br><li><u>_action rules_</u>: By clicking on it, it allows you to go up one level.<br>                                                                        |
|           | Graphical visualization |                  |                    | group        | Details of the behaviour and content are described in Multi-criteria View or in View dedicated for each criteria                                                                                                                                         |
| 2         |                         | bar tooltips     |                    | label        | <li><u>_initialization rules_</u>: By overflying a bar, information is displayed :<br><br> - VM's impact in people eq.<br><br> - VM's impact in the unit of the chosen criterion<br><br> - Cluster<br><br> - Type of equipment<br><br> - Environment<br> |
| 3         |                         | bar chart        |                    | bar chart    | <li><u>_initialization rules_</u>: Bar chart representing the application's impact of my inventory by virtual machine and environment in people eq.                                                                                                      |
|           | Key indicators          |                  |                    | group        | These key indicators contextualize the impacts observed and represent the main levers for action to reduce impacts.                                                                                                                                      |
| 4         |                         | Application      |                    |              |                                                                                                                                                                                                                                                          |
|           |                         |                  | Applications       | label        | <li><u>_initialization rules_</u>: Display the number of the applications taking into account the filters.                                                                                                                                               |
|           |                         |                  | Application impact | label        | <li><u>_initialization rules_</u>: Display the impact of the applications taking into account the unit selected.                                                                                                                                         |
|           |                         |                  | Unit               | Radio button | <li><u>_initialization rules_</u>: Allow to select the unit of the impact (people equivalent or kg CO2equivalent)                                                                                                                                        |
| 5         |                         | Lifecycle        |                    |              | <li><u>_initialization rules_</u>: Pie chart representing the impact of my application by lifecycle step                                                                                                                                                 |
| 6         |                         | Environment      |                    |              | <li><u>_initialization rules_</u>: Pie chart representing the impact of my application by environment                                                                                                                                                    |
| 7         |                         | Data consistency |                    | button       | <li><u>_action rules_</u>: Click to view graph of data consistency for the VMs of selected application level. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md) </li>                                                |

{{% /expand %}}

### Chart By virtual machine in Case of Equipment is a Cloud Instance

![uc6_visualize_cloud_application_footprint_by_criteria_level_VM.png](../images/uc6_visualize_cloud_application_footprint_by_criteria_level_VM.png)
{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Reference | Group | Elements     | Sub-Elements | Type  | Description                                                                                                                                                                                                                                                    |
| --------- | ----- | ------------ | ------------ | ----- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1         |       | bar tooltips |              | label | <li><u>_initialization rules_</u>: By overflying a bar, information is displayed :<br><br> - VM's impact in people eq.<br><br> - VM's impact in the unit of the chosen criterion<br><br> - Cluster<br><br> - Name of Cloud provider <br><br> - Environment<br> |

{{% /expand %}}

### View dedicated for data consistency

![uc6_visualize_application_footprint_data_consistency.png](../images/uc6_visualize_application_footprint_data_consistency.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group | Elements         | Sub-Elements | Type   | Description                                                                                                                                                                                                                                                    |
| --------- | ----- | ---------------- | ------------ | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1         |       | Data consistency |              | button | <li><u>action rules</u>: Click to view graph of data consistency for the selected criteria. See [Global concepts/ Data consistency](../../global_concepts/uc1_dataconsistency.md)</li>                                                                         |
| 2         |       | Graph            |              | bar    | <li><u>initialization rules</u>: The axes of the bar correspond to the criteria or filters selected, and it displays results where the impact is calculated in blue and the impact that cannot be calculated in red. The value is represented as a percentage. |

{{% /expand %}}

## Sequence Diagram

### Show the Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant Database
RND ->> front: Click on "Application" button or one of the tab in the view or one the chart of multi-criteria tab
front ->> back: GET /api/{subscriber}/{organization}/inventories?inventoryId={inventory_id}
Database--> back: Get inventory information
back -->> front: Send the details of selected inventory Id on my view related to my view
front ->> back: GET /api/subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/applications
Database--> back: Get the indicators from out_application table
back -->> front: Indicators aggregated to be displayed
front --> RND: Display the list of filters related to my view
front ->> RND: Display filtered graphs related to my view

{{< /mermaid >}}
