---
title: "1.4. Launch an estimate"
description: "This use case describes how to launch an estimate"
weight: 40
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

The use case allows a sustainable IT leader to launch the calculation for the assessment of the impact of the application of the information system.
The calculation is based on different indicators that contextualize the impacts observed.

More information about the production of indicators can be found here:
https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/blob/develop/docs/MoteurDeCalculG4IT_V1.1.adoc

**Navigation Path**
Input :

-   My Information System / Visualize my inventories / My inventory / Estimate history / ("Estimate" or "Update Estimate")
    Output :
-   My Information System / Visualize my inventories / My inventory / Estimate history

**Access Conditions**  
The connected user must have the write access for that module one the selected organization.

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[List of inventory view] --> Step2[Load files in the inventory] --> Decision1{Is there valid <br>physical equipment file or<br> virtual equipment file ?}
Decision1 -->|No|Step3[Button 'Launch estimate'<br> disable]
Decision1 -->|Yes|Decision2{Is there already a<br> loading running ?}
Decision2 -->|Yes|Step3
Decision2 -->|No|Decision3{Is there already an<br> estimation running ?}
Decision3 -->|Yes|Step3
Decision3 -->|No|Step4[Button 'Launch estimate'<br> enable] -->|Click on 'Launch estimate'<br> button|Step5[Confirm the action] -->|No|Step4
Step5 -->|Yes|Step6[Estimation pending] --> Decision4{Any task in progress ?}
Decision4 -->|No|Step7[Estimation in progress] --> |Completed|Step8[Display Validated :<br> 'Completed']
Step7 --> |Error|Step11[Display Error :<br> 'Error'] --> Step4
Step8 --> |Estimation done|Step9['Update estimate' button<br>  available and estimate<br> history  is updated] -->Step10['Equipment' and <br>'Application' buttons<br> enabled]
Decision4 -->|Yes|Step6
{{< /mermaid >}}

## Mockup

### Loading History

![uc4_launch_estimation_estimate_history](../images/uc4_launch_estimation_estimate_history.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

#### Estimate History

| Reference | Group                             | Elements  | Sub-Elements   | Type   | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| --------- | --------------------------------- | --------- | -------------- | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Estimate History                  |           |                | button | Launch/ Update inventory footprint estimate. [Environmental Footprint Assessment methodology](../../global_concepts/environmental_footprint_assessment_methodology/_index.md)<br>_NB_ : Before launching the first estimate, check that the referential data available for your organization suit your needs. <br><br><li><u>_initialization rules_</u>: The button is available only if a valid physical equipment file is already upload and an integration isn't already running and an estimation isn't already running.<br><br><li><u>_action rules_</u>: Trigger the request of an estimate. |
| 1         |                                   | Estimates |                | List   | <li><u>_initialization rules_</u>: The list is ordered by try date desc                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 2         |                                   |           | Status icon    | Label  | <li><u>_initialization rules_</u>: 4 existing status : <br>Pending (If any other inventory loading or estimation task is in progress)<br> In Progress (Estimation is in progress),<br> Error (Estimation could not be performed),<br> Completed (Inventory have been successfully submitted to NumEcoEval for estimation but it does not mean that the estimation is ready, wait around 10 min to let the compute finish).<br>                                                                                                                                                                                                                                                                                                |
| 3         |                                   |           | Estimate dates | button |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| 4         | Choose criteria for the inventory |           |                | button | Details of the behaviour is described in [3.2.5 Choose criteria](../uc_administration/uc_administration_manage_organizations/uc5_choose_criteria.md)                                                                                                                                                                                                                                                                                                                                                                                                                                               |

#### Estimate control and process

| Management rules | Title | Rule description |
| ---------------- | ----- | ---------------- |
| 1                |       |                  |
| 2                |       |                  |
| 3                |       |                  |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant NumEcoEval
participant BOAVIZTAPI
participant DataBase
participant Azure storage

RND ->> front: Click on "Launch estimate" button
front ->> back: POST /api/{subscriber}/{organization}/inventories/{inventory_id}/evaluating
front -->> RND: Display 'pending' button if other task is already in progress
front -->> back: Resume estimation once no other task is in progress
back ->> BOAVIZTAPI: Estimate the impact of the cloud services via POST /api/v1/cloud/instance
BOAVIZTAPI ->> DataBase: Send cloud indicators data in out_virtual_equipment table
back -> NumEcoEval: Estimate the impact of the inventory via POST /api/entrees/calculs/soumission
NumEcoEval ->> DataBase: Send indicators data in out_physical_equipment table
NumEcoEval ->> DataBase: Send indicators data in out_virtual_equipment table
NumEcoEval ->> DataBase: Send cloud indicators data in out_application table
back -->> Azure storage: Create a zipped file of uploaded data files <br>and their indicators in csv file format and store in the Azure storage
front ->> back: GET /api/{subscriber}/{organization}/inventories/{inventory_id}
DataBase ->> back: Get the inventory
front-->> RND: Display that the estimation is completed
front->> RND: The 'Equipment' and 'Application' buttons enabled to view footprints

{{< /mermaid >}}
