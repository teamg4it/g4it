---
title: "2.4. Launch estimation"
description: "This use case describes how to launch an estimation to assess an digital service's impact"
weight: 40
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [MockUp](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

The use case allows a project team to launch the calculation for the estimation of impacts of the Digital Service. The calculation is based on different indicators that contextualize the impacts observed.

More information about the production of indicators can be found here: [MoteurDeCalculG4IT_V1.1.adoc](https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/blob/develop/docs/MoteurDeCalculG4IT_V1.1.adoc)

## Launch Estimation via 'Calculate My Impact' button
### State Diagram

{{< mermaid align="center">}}
graph TD;

Step1[Digital service view]--> Decision1{Has terminal, network, non-cloud server or cloud service equipment?}
Decision1 -->|Yes|Step2['Calculate my impact' button visible]
Decision1 -->|No|Step3['Calculate my impact' button invisible] --> |add terminal, network, non-cloud server or cloud service|Step2-->|Click on 'Calculate my impact' button|Step4[Footprints evaluated]-->Step5['Visualize my results' tab appears, user redirected to footprint view] -->Step6['Calculate my impact' button diappears]-->|Update digital service|Step2

{{< /mermaid >}}

![uc4_launch_estimation](../images/uc4_launch_estimation.png)

| Management rules | Title                | Rule description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| ---------------- |----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1                | Calculate my impact  | <li><u>_initialization rules_</u>: The button is displayed only for users with “write” role access and if the digital service has terminal, network or server equipments (cloud or non cloud).The button _disappears_ once the calculation is completed and reappears only when any equipment is added/modified.<br> <li><u>_action rules_</u>: Launch/ Update digital service footprint estimate. [Environmental Footprint Assessment methodology](../../global_concepts/environmental_footprint_assessment_methodology/_index.md). |
| 2                | Visualize my results | <li><u>_initialization rules_</u>: The tab is displayed only of the calculation have been performed. On click of this tab 'Footprint' view is displayed.                                                                                                                                                                                                                                                                                                                                                                             |


## Launch Estimation via 'Edit criteria' button
In the digital service 'Footprint' view calculation can be relaunched via 'Edit Criteria' button. Details
[2.5. Visualize digital service's footprint](uc5_visualize_footprint/_index.md).

### State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[Digital Service view] --> Decision1{First Calculation is done?}
Decision1-->|Yes|Step2[Tab'Visualize My Results' is enabled]
Decision1-->|No|Step3[Tab'Visualize My Results' is not enabled]
Step2-->|Click on 'Visualize My Results' tab|Step4[Multi criteria footprint view is displayed]-->|Click on 'Edit Criteria button' select criteria and click 'Save' button|Step5[Calculations relaunched and updated footprints are displayed]
{{< /mermaid >}}

![uc4_launch_criteria_estimation](../images/uc4_launch_criteria_estimation.png)
| Management rules | Title                | Rule description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| ---------------- |----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1                | Edit Criteria  | <li><u>_initialization rules_</u>: On button click, a pop-up window appears, select the criteria to make recalculation’ |
| 2                | Save | <li><u>_initialization rules_</u>: Click 'Save' button.<br> <li><u>_action rules_</u>:  Criteria updated for digital service, footprints re-estimated.                                                                                                                                                                                                                                                                                                                                                                          |

## Sequence Diagram

{{< mermaid >}}

sequenceDiagram
actor RND as Project team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant NumEcoEval
participant BOAVIZTAPI
participant DataBase
participant Azure storage

RND ->> front: Click on "Calculate My impact" button
front ->> back: POST /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/evaluating
back -> NumEcoEval: Estimate the impact of the terminals, networks, and non-cloud servers via POST /api/entrees/calculs/soumission
NumEcoEval -->> DataBase: Send indicators data in out_physical_equipment and out_virtual_equipment table
back ->> BOAVIZTAPI: Estimate the impact of the cloud services via POST /api/v1/cloud/instance
BOAVIZTAPI -->> DataBase: Send indicators data in out_virtual_equipment table
back -->> Azure storage: Create a zipped file of the terminals, networks, cloud services,<br> non-cloud servers and their indicators in csv file <br> format and store in the Azure storage
back -->> front: Estimation is finished and "Visualize My Results" button is enabled
front-->> RND: "Calculate My impact" button is disabled until any update is made in the DS
front-->> RND: "Export" button is enabled
front->> back: GET /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}
DataBase-->> back: Get the Digital Service
front-->> RND: Redirection to the footprint visualization view.

RND ->> front: Footprint view:click on "Edit Criteria" then "Save" button
front ->> back: PUT /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/evaluating
back ->> database: Criteria updated for Digital service
back ->> front: GET /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}
front ->> back: POST /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/evaluating
back ->> front: Same processing as for "Calculate My impact" button

{{< /mermaid >}}
