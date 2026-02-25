---
title: "2.11. Compare two digital service versions"
description: "This use case describes how to compare the impact of two digital service versions"
weight: 110
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

This use case allows a user to compare the environmental impact of two versions of a digital service in order to understand which impact criteria differ between them. When a user selects two versions, the system highlights the differences across impact criteria that are active and have calculated results for both versions.

Users can select versions using a checkbox column. The "Compare two versions" button is enabled only when exactly two versions are selected and both have calculated impact results. Select two versions to compare their results. The versions must have calculated impact criteria.

**Navigation Path**

-   My Digital Services / Digital Service Version view / Manage versions / Compare two versions

**Access Conditions**
The connected user must have access to view impact results for the selected versions.


## State Diagram


{{< mermaid align="center" >}}
graph TD

    Step1[List of digital services view] --> Step2[Click on a digital service version]

    Step2 --> Step3[Click on a Manage Versions button]
    Step3 --> Step4[Select two versions with calculated impact results]
    Step4 --> Step5[Click on Compare two versions button]
    Step5 --> Step6[Display comparison of impact results highlighting differences]
{{< /mermaid >}}


## Mockup

-   **Version Selection and Compare Button**
    ![uc11_compare_digital_service_versions.png](../images/uc11_compare_digital_service_versions.png)

-   **Comparison of evaluated Digital Service Versions**
    ![uc11_compare_dsv.png](../images/uc11_compare_dsv_new.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements                 | Type   | Description                                                                                  |
|-----------|-------------|--------------------------|--------|----------------------------------------------------------------------------------------------|
|           | Page Header |                          | group  |                                                                                              |
| 1         |             | Digital service versions | label  | Selected two versions to compare                                                             |
| 2         |             | Compare two versions     | button | Click on the Compare two versions button to get the evaluated data of two selected versions. |

{{% /expand %}}
## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND -->> front: Click on Compare Two Versions
front -->> back: POST /organizations/{organization}/workspaces/{workspace}/digital-service-version/compare-versions
DataBase -->> back: Get digital service versions data from out_physical_equipment, out_virtual_equipment
back -->> front: Display the results of comparison
{{< /mermaid >}}
