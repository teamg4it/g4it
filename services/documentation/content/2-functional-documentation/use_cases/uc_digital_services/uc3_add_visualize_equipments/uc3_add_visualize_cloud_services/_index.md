---
title: "2.3.4 Add or Visualize Cloud Services"
description: "This use case describes how to add Cloud Services equipments to a digital service"
weight: 40
mermaid: true
---

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Cloud Services visualization](#cloud-services-visualization)
-   [Cloud Services add](#cloud-services-add)
-   [Cloud Services edit](#cloud-services-edit)

## Description

This usecase allows a project team to add Cloud Services equipment directly via form, into a digital service previously created.

**Navigation Path**

-   My Digital Services / My Digital Service view


**Access Conditions**
The connected user must have the write access for that module on the selected organization.

## Cloud services visualization

![uc3_add_visualize_equipments_CloudServiceTab.png](../../images/uc3_add_visualize_equipments_CloudServiceTab.png)
{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group      | Elements            | Type   | Description                                                                                                                                                                               |
|-----------| ---------- |---------------------| ------ |-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Tab Header |                     | group  |                                                                                                                                                                                           |
| 1         |            | Cloud Services      | title  |                                                                                                                                                                                           |
| 2         |            | + Add Cloud Service | button | <li><u>_initialization rules_</u>: That button is displayed if the connected user have the write right.<br><br><li><u>_action rules_</u>: That button open the window Device details.<br> |
|           | Tab        |                     |        |                                                                                                                                                                                           |
| 3         |            | Name                | column |                                                                                                                                                                                           |
| 4         |              |                     | Cloud provider                 | dropdown                                                                                                                                                                                  |                                                                                                                   |
| 5         |              |                     | Instance type                  | dropdown                                                                                                                                                                                  |                                                                                                                   |
|           |              | Instance usage      |                                | section                                                                                                                                                                                   |                                                                                                                   |
| 6         |              |                     | Quantity                       | Entire number input                                                                                                                                                                       |                                                                                                                   |
| 7         |              |                     | Location                       | dropdown                                                                                                                                                                                  |                                                                                                                   |
| 8         |              |                     | Annual usage duration (hour)   | Entire number input                                                                                                                                                                       |                                                                                                                   |
| 9         |              |                     | Average workload (% CPU usage) | Entire number input                                                                                                                                                                       |                                                                                                                   |
| 10        |            | Edit                | button | <li><u>_action rules_</u>: That button open the window Device details.<br>                                                                                                                |
| 11        |            | Delete              | button | <li><u>_action rules_</u>: Delete the device from the current Digital Service.<br> Note : The user must click on Calculate to update the footprint estimation.                            |
| 12        |            | Import              | button | <li><u>_action rules_</u>: Upload files to create cloud services or non-cloud services<br> Note : The user must click on Calculate to update the footprint estimation.                    |

{{% /expand %}}

## Cloud Services add
[2.3.1 Add or Visualize cloud service via form](add_visualize_cloud_services_via_forms.md)

[2.3.2 Add cloud services by importing files](import_cloud_services_via_button.md)

## Cloud Services edit

**Navigation Path**
-   My Digital Services / My Digital Service / Cloud Services / Modify Cloud Service
![uc3_add_visualize_equipments_CloudServiceAdd.png](../../images/uc3_add_visualize_equipments_CloudServiceAdd.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group        | Elements               | sub-Elements                   | Type                | Description                                                                                                       |
| --------- | ------------ | ---------------------- | ------------------------------ | ------------------- | ----------------------------------------------------------------------------------------------------------------- |
| 1         | Title        |                        |                                |                     | <li><u>_initialization rules_</u>: Coming from "Add Cloud Service", the title is "New Cloud Instance" else "xxx". |
| 2         |              | Name                   |                                | label input         |                                                                                                                   |
|           |              | Instance configuration |                                | section             |                                                                                                                   |
| 3         |              |                        | Cloud provider                 | dropdown            |                                                                                                                   |
| 4         |              |                        | Instance type                  | dropdown            |                                                                                                                   |
|           |              | Instance usage         |                                | section             |                                                                                                                   |
| 5         |              |                        | Quantity                       | Entire number input |                                                                                                                   |
| 6         |              |                        | Location                       | dropdown            |                                                                                                                   |
| 7         |              |                        | Annual usage duration (hour)   | Entire number input |                                                                                                                   |
| 8         |              |                        | Average workload (% CPU usage) | Entire number input |                                                                                                                   |
| 9         | Cancel       |                        |                                | button              | <li><u>_action rules_</u>: That button close the window Device details.<br>                                       |
| 10        | Add / Update |                        |                                | button              |                                                                                                                   |

{{% /expand %}}

