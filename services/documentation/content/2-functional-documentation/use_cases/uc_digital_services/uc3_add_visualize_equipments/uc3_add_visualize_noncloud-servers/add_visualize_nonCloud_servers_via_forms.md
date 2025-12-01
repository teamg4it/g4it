---
title: "2.3.3.1. Add Private Infrastructures using the form"
description: "This use case describes how to add Private Infrastructures equipments to a digital service using the form"
weight: 20
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Private Infrastructure add](#private-infrastructure-add)

## Description

This use case allows a project team to add NPrivate Infrastructure equipment directly via form, into a digital service version previously created.

**Navigation Path**

-   My Digital Services / My Digital Service / Visualize my resources tab /Private Infrastructures / Add Server
-   My Digital Services / My Digital Service / Visualize my resources tab / Private Infrastructures / Edit Server

**Access Conditions**
The connected user must have the write access for that module on the selected workspace.

## Private Infrastructure visualization

![uc3_add_visualize_equipments_NCServersList.png](../../../images/uc3_add_visualize_equipments_NCServersList.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group      | Elements          | Type   | Description                                                                                                                                                                           |     |     |     |
| --------- | ---------- | ----------------- |--------| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --- | --- | --- |
|           | Tab Header |                   | group  |                                                                                                                                                                                       |     |     |     |
| 1         |            | Private Infrastructures | table  |                                                                                                                                                                                       |     |     |     |
| 2         |            | + Add    | button | <li><u>_initialization rules_</u>: That button is displayed if the connected user have the write right.<br><li><u>_action rules_</u>: That button open the window Add Server details. |     |     |     |
|           | Tab        |                   |        |                                                                                                                                                                                       |     |     |     |
| 3         |            | Name              | column |                                                                                                                                                                                       |     |     |     |
| 4         |            | Allocation        | column |                                                                                                                                                                                       |     |     |     |
| 5         |            | Type              | column |                                                                                                                                                                                       |     |     |     |
| 6         |            | Quantity (+VMs)   | column |                                                                                                                                                                                       |     |     |     |
| 7         |            | Host              | column |                                                                                                                                                                                       |     |     |     |
| 8         |            | Datacenter        | column |                                                                                                                                                                                       |     |     |     |
| 9         |            | Edit              | button | <li><u>_action rules_</u>: That button open the window Network details.                                                                                                               |     |     |     |
| 10        |            | Delete            | button | <li><u>_action rules_</u>: Delete the non cloud servers from the current Digital Service.<br>Note : The user must click on Calculate to update the footprint estimation.              |     |     |     |

{{% /expand %}}

## Private Infrastructure add

![uc3_add_visualize_equipments_NonCloudServer.png](../../../images/uc3_add_visualize_equipments_NonCloudServer.png)
{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Elements                  | Type         | Description                                                             |
| --------- | ------------------------- | ------------ | ----------------------------------------------------------------------- |
| 1         | New Server or Edit Server | title        |                                                                         |
| 2         | Name                      | label input  |                                                                         |
| 3         | Dedicated or Shared       | Radio button |                                                                         |
| 4         | Compute or Storage        | Radio button |                                                                         |
| 5         | Cancel                    | button       | <li><u>_action rules_</u>: That button open the window Network details. |
| 6         | Next                      | button       |                                                                         |

{{% /expand %}}

### Step 2—field depending on the "Dedicated/Shared" & "Compute/Storage" options

_example:_
![uc3_add_visualize_equipments_NCServerAdd_Step2.png](../../../images/uc3_add_visualize_equipments_NCServerAdd_Step2.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group                | Elements                             | Type                 | Dedicated / Compute | Shared / Compute | Shared/Storage | Dedicated/Storage | Description                                                             |
| --------- | -------------------- | ------------------------------------ | -------------------- | ------------------- | ---------------- | -------------- | ----------------- | ----------------------------------------------------------------------- |
|           | Header               |                                      |                      |                     |                  |                |                   |                                                                         |
| 1         |                      | Server name                          | title                | Yes                 | Yes              | Yes            | Yes               |                                                                         |
| 2         |                      | Server profile                       | sub-title            | Yes                 | Yes              | Yes            | Yes               |                                                                         |
| 3         |                      | Host                                 | Dropdown             | Yes                 | Yes              | Yes            | Yes               |                                                                         |
|           | Additional parameter |                                      |                      |                     |                  |                |                   |                                                                         |
| A         |                      | Datacenter                           | Dropdown             | Yes                 | Yes              | Yes            | Yes               |                                                                         |
|           |                      | Add Datacenter                       | button               | Yes                 | Yes              | Yes            | Yes               |                                                                         |
| C         |                      | Quantity                             | Entire number input  | Yes                 |                  |                | Yes               |                                                                         |
| E         |                      | Total vCPU                           | Entire number input  | Yes                 | Yes              |                |                   |                                                                         |
| F         |                      | Server lifespan (Year)               | Decimal number input | Yes                 | Yes              | Yes            | Yes               |                                                                         |
| D         |                      | Annual electricity consumption (kwH) | Entire number input  | Yes                 | Yes              | Yes            | Yes               |                                                                         |
| G         |                      | Annual Operating time (hour)         | Entire number input  | Yes                 |                  |                | Yes               |                                                                         |
| H         |                      | Total Disk (GB)                      | Entire number input  |                     |                  | Yes            | Yes               |                                                                         |
|           | Footer               |                                      |                      |                     |                  |                |                   |                                                                         |
| 4         |                      | Cancel                               | button               | Yes                 | Yes              | Yes            | Yes               | <li><u>_action rules_</u>: That button open the window Network details. |
| 5         |                      | Previous                             | button               | Yes                 | Yes              | Yes            | Yes               |                                                                         |
| 6         |                      | Create                               | button               | Yes                 |                  |                | Yes               |                                                                         |
| 6         |                      | Next                                 | button               |                     | Yes              | Yes            |                   |                                                                         |

{{% /expand %}}

### Step 3—for "Add Datacenter"

_example:_
![uc3_add_visualize_equipments_NCServerAdd_Step3.png](../../../images/uc3_add_visualize_equipments_NCServerAdd_Step3.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Elements       | Type                 | Description                                                             |
| --------- | -------------- | -------------------- | ----------------------------------------------------------------------- |
| 1         | Add Datacenter | title                |                                                                         |
| 2         | Name           | label input          |                                                                         |
| 3         | PUE            | Decimal number input |                                                                         |
| 4         | Country        | Dropdown             |                                                                         |
| 5         | Cancel         | button               | <li><u>_action rules_</u>: That button open the window Network details. |
| 6         | Add            | button               |                                                                         |

{{% /expand %}}

### Step 4—Only for "Shared" option - VM list

_example:_
![uc3_add_visualize_equipments_NCServerAdd_Step4.png](../../../images/uc3_add_visualize_equipments_NCServerAdd_Step4.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group      | Elements                     | Type      | Description                                                                                                                                                              |
|-----------| ---------- | ---------------------------- | --------- |--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Header     |                              | group     |                                                                                                                                                                          |
| 1         |            | Server Name                  | title     |                                                                                                                                                                          |
| 2         |            | Server options               | sub-title |                                                                                                                                                                          |
|           | Tab Header |                              |           |                                                                                                                                                                          |
|           |            | Virtual Machines             | title     |                                                                                                                                                                          |
| 3         |            | Add VM                       | button    |                                                                                                                                                                          |
|           | Tab        |                              |           |                                                                                                                                                                          |
| 4         |            | Name                         | column    |                                                                                                                                                                          |
| 5         |            | Quantity (+VMs)              | column    |                                                                                                                                                                          |
| 6         |            | vCPU                         | column    |                                                                                                                                                                          |
| 7         |            | Annual Operating time (hour) | column    |                                                                                                                                                                          |
| 8         |            |Electricity consumption (kwH) | column    | Optional                                                                                                                                                                 |
| 9         |            | Cancel                       | button    | <li><u>_action rules_</u>: That button open the window Servers details.                                                                                                  |
| 10        |            | Previous                     | button    | <li><u>_action rules_</u>: Come back to the previous screen                                                                                                              |
| 11        |            | Create                       | button    | <li><u>_action rules_</u>: Create the non cloud servers from the current Digital Service.<br>Note : The user must click on Calculate to update the footprint estimation. |

{{% /expand %}}

### Step 5—Only for "Shared" option—Add VM

_example:_
![uc3_add_visualize_equipments_NCServerAdd_Step5.png](../../../images/uc3_add_visualize_equipments_NCServerAdd_Step5.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group                 | Elements                     | Type                | Description                                                             |
|-----------| --------------------- | ---------------------------- | ------------------- | ----------------------------------------------------------------------- |
|           | Header                |                              | group               |                                                                         |
| 1         |                       | Add VM                       | title               |                                                                         |
| 2         |                       | Name                         | label input         |                                                                         |
| 3         |                       | Total vCPU                   | Entire number input |                                                                         |
|           | Additional Parameters |                              |                     |                                                                         |
| 4         |                       | Quantity                     | Entire number input |                                                                         |
| 5         |                       | Annual operating time (hour) | Entire number input |                                                                         |
| 6         |                       | Electricity consumption (kwH)| Entire number input |                                                                         |
| 7         |                       | Cancel                       | button              | <li><u>_action rules_</u>: That button open the window Network details. |
| 8         |                       | Add VM                       | button              |                                                                         |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as project team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Add" button in the digital service version's Private Infrastructures table
front ->> back: POST /api/organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/physical-equipments
back--> DataBase: Create non-cloud server record in the in_physical_equipment table
front ->> back: POST /api/organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/datacenters
back--> DataBase: Create non-cloud server record in the in_datacenter table
front ->> back: GET /api/organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/physical-equipments
DataBase-->> back: Get non-cloud servers from the in_physical_equipment table
front ->> back: POST /api/organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/virtual-equipments
back-->> DataBase: Create non-cloud server's vm record in the in_virtual_equipment table
front ->> back: GET /api/organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/virtual-equipments
DataBase-->> back: Get non-cloud servers' vms from the in_virtual_equipment table
front ->> back: GET /api/organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/inputs/datacenters
DataBase-->> back: Get non-cloud servers' datacenter from the in_datacenter table
back-->> front: Send the physical and virtual equipments for the non-cloud server view
front->> RND : Display the Private Infrastructure list view

{{< /mermaid >}}
