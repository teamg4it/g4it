---
title: "1.2. Create or update an inventory (IS version or simulation)"
description: "This use case describes how to create a new version of my IS or simulate one"
weight: 20
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

The usecase allows a user to create or update an inventory, it can be a version of his IS or a simulation of one.

To track the impact over time, a version of the information system is a representation of the IS at a point in time.

The goal is to track it and verify that the action has some impact on the environmental footprint.

To simulate an action plan, it is possible to create a simulation to project the vision and visualize the possible reduction.
As described in the [PCR Information system](https://librairie.ademe.fr/consommer-autrement/6649-referentiel-methodologique-d-evaluation-environnementale-des-systemes-d-information-si.html), an information system is composed of physical devices, data centers, but also virtual devices and applications.

**Navigation Path**  
My Information System / Visualize my inventories / New Inventory button
My Information System / Visualize my inventories / My inventory / Add files

**Access Conditions**  
The connected user must have the write access for that module on the selected workspace.

## State Diagram

{{< mermaid align="center">}}

graph TD;
Step1[List of inventory view] --> |Click on 'New Inventory' button| Step2[New Inventory creation view] --> Decision1{Select IS version or Simulation?}
Decision1 --> |IS version| Step3[Select month and year]
Decision1 --> |Simulation| Step4[Enter a name]
Step3 --> Step5[Button 'Create' available]
Step4 --> Step5 --> Step6[Upload files or not] --> |Click on button 'Create'| Decision3{IS version or Simulation ?}
Decision3 --> |IS version| Step7[Inventory added to IS list]
Decision3 --> |Simulation| Step8[Inventory added to Simulation list]
Step2 --> |Click on 'Cancel' button|Step1

{{< /mermaid >}}

## Mockup
### Create a new inventory
![uc2_create_update_inventory__create.png](../images/uc2_create_update_inventory__create.png)

### Update an existing inventory
![uc2_create_update_inventory__update.png](../images/uc2_create_update_inventory__update.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group        | Elements           | Sub-Elements                   | Type           | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|-----------|--------------|--------------------|--------------------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Page Header  |                    |                                | Group          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 1         |              | My inventory       |                                | Label          | <li><u>*initialization rules*</u>: The title is "New Inventory" if the page opening is trigger by a click on the "New inventory" button on the "Visualize My Information System" page. Else the title is "Load files on" + <name of the inventory>                                                                                                                                                                                                                                                                                                                                     |
|           | Page Content |                    |                                | Group          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 2         |              | Inventory          |                                | Radio button   | <li><u>*initialization rules*</u>: The Radio button is displayed for both the creation of a new inventory and update of an existing inventory. <br> 2 choices are available: "IS version" and "Simulation". By default, "IS version" is selected. Radio button is disabled in case of update of an existing inventory                                                                                                                                                                                                                                                                  |
| 3         |              | Inventory name     |                                | Input field    | <li><u>*initialization rules*</u>: The input field is displayed only for both the creation of a new inventory and update of an existing inventory. <br>An inventory is characterized by a date for IS version allowing to monitor environmental footprint through time and by name for simulation. Therefore the date is mandatory to create the inventory (IS Version).                                                                                                                                                                                                               |
| 4         |              | File selection     |                                | Item           | <li><u>*initialization rules*</u>: By default, For Infrastructure tab: 2 items are displayed: one for each type (Datacenter and Physical Equipment), For End-user Devices tab: 1 item are displayed: one type (Physical Equipment), For Application Services tab: 2 items are displayed: one for each type (Virtual Equipment and Applications), For External Services tab: 2 items are displayed: one for each type (Virtual Equipment and AI Services).                                                                                                                              |
| 5         |              |                    | Item type                      | Drop down list | <li><u>*initialization rules*</u>: For Infrastructure tab: two types (Datacenter and Physical Equipment) of items can be loaded on an inventory, For End-user Devices tab: one type (Physical Equipment) of item can be loaded on an inventory, For Application Services tab: two types (Virtual Equipment and Applications) of items can be loaded on an inventory and For External Services tab: two types (Virtual Equipment and AI Services) of items can be loaded on an inventory. <br>It is not mandatory to select file: Inventory can be created without file.                |
| 6         |              |                    | "+ Choose a .csv file"         | button         | <li><u>*action rules*</u>: Click on it to open a window to select one file on the user computer.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| 7         |              |                    | Delete                         | button         | <li><u>*action rules*</u>: Click on it to remove the item (Item type, Choose button and Delete button).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| 8         |              | Add                |                                | button         | <li><u>*action rules*</u>: Click on it to add one item (Item type, Choose button and Delete button).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 9         | Starter pack |                    |                                | Group          | To help you start, template files and data model are available to be downloaded                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|           |              | All template files |                                | button         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|           |              |                    | Datacenter template file       | button         |  File available in Infrastructure tab only.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|           |              |                    | Physical equipment template    | button         |  File available in two tabs Infrastructure Tab and End-user devices tabs.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|           |              |                    | Virtual equipment template     | button         |  File available in two tabs Application Services Tab and External Services tabs.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|           |              |                    | Application equipment template | button         |  File available in Application Services tab only.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|           |              |                    | AI services template           | button         |  File available in External Services tab only.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|           |              |                    | Workspace Specific Settings    | button         |  File available in Inventory info tab in case workspace settings are uploaded in that workspace.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|           |              | DataModel          |                                | button         |  Available in all tabs except Inventory info tab                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|           | Page Footer  |                    |                                | Group          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 10        |              | Cancel             |                                | button         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 11        |              | Create/Update      |                                | button         | <li><u>*initialization rules*</u>: <br>The label button is "Create"  when the context is the creation of a new inventory; "Upload when the context is the update on an existing inventory).<br>   When the context is the creation of a new inventory, the button is activate when the Inventory name is filled in (for IS version, a month and a year or for Simulation, a name). Note : It is not mandatory to select file: Inventory can be created without file.<br><br><li><u>*action rules*</u>:  Details of the behaviour is described in [1.3. Load files](uc3_load_files.md). |
|           |              | Previous/Next      |                                | button         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 12        | Tabs         |                    |                                |                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|           |              | Inventory Info     |                                | tab            | Each version of the information system allows you to track the evolution of its impact over time. A simulation allows you to perform an impact assessment on a test inventory.                                                                                                                                                                                                                                                                                                                                                                                                         |
|           |              | Infrastructure     |                                | tab            | The infrastructure includes all physical components that host and support IT systems, such as data centers, servers, and network equipment.                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|           |              | End-user Devices   |                                | tab            | This category includes all devices used by employees in their daily work to access IT systems, such as laptops, smartphones, displays, and printers.                                                                                                                                                                                                                                                                                                                                                                                                                                   |
|           |              |Application Services|                                | tab            | This category covers applications and their associated runtime components. It includes, in particular, virtual machines, containers, databases, and other software services that contribute to delivering functionality to users and business processes.                                                                                                                                                                                                                                                                                                                               |
|           |              | External Services  |                                | tab            | This category includes services provided by external vendors, such as cloud computing and AI service providers. These services are managed outside the organization but are essential for the organization to deliver value; therefore, they must be accounted for.                                                                                                                                                                                                                                                                                                                    |

{{% /expand %}}

## Sequence Diagram

### Create new inventory without files

{{< mermaid align="center">}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on create in the New Inventory view
front ->> back: Get /api/organizations/{organization}/workspaces/{workspace}/is-workspace-specific
back ->> DataBase: Get boolean for if the workspace referential is present or not
back ->> front: Return the boolean
front ->> back: Post /api/{organization}/{workspace}/inventories
back ->> DataBase: Create the inventory
front ->> back: Get /api/{organization}/{workspace}/inventories
DataBase ->> back: Get the created inventory
back ->> front: Display the inventory in the suited list

{{< /mermaid >}}

### Create new inventory with files

{{< mermaid align="center">}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on create in the New Inventory view
front ->> back: Get /api/organizations/{organization}/workspaces/{workspace}/is-workspace-specific
back ->> DataBase: Get boolean for if the workspace referential is present or not
back ->> front: Return the boolean
front ->> back: Post /api/{organization}/{workspace}/inventories
back ->> DataBase: Create the inventory
front ->> back: Post /api/{organization}/{workspace}/inventories/{inventoryId}
back ->> DataBase: Load the files in the new Inventory
front ->> back: Get /api/{organization}/{workspace}/inventories
DataBase ->> back: Fetch the created inventory
back ->> front: Display the inventory in the suited list

{{</ mermaid >}}

### Download template files

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant azure as Azure file storage

RND ->> front: Click on any download template button in the loading files view
front ->> back: GET /api/{organization}/{workspace}/templates-files/{template}
back --> azure: Get the selected template
back ->> front: Send the select template

{{< /mermaid >}}
