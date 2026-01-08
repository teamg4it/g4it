---
title: "2.12. Promote a digital service version"
description: "This use case describes how to promote a digital service version"
weight: 120
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

This use case allows a user to promote a digital service version. User click on Promote button to make a version my new current version in order to track my progress. .

The "Promote version" button is enabled only when exactly a version is in draft status. After promoting the draft version will become active and previous active version will become archived.

**Navigation Path**

-   My Digital Services / Digital Service Version view / Manage versions / Promote version
-   My Digital Services / Digital Service Version view / Promote version



## State Diagram


{{< mermaid align="center" >}}
graph TD

    Step1[List of digital services view] --> Step2[Click on a digital service version]
	
	Step2[Click on a digital service version] --> Decision1{Do you want to promote current version?}
	
    Decision1 -->|Yes| Step3[Click on Promote version button]
    Decision1 -->|No| Step4[Click on manage versions icon]
	
	
    Step4 --> Step3
	Step3 --> Step6[Popup will appear to confirm. Click on Promote button]
    Step6 --> Step7[Manage Version List Updated Current Version will become active version]
	
{{< /mermaid >}}


## Mockup

-   **Promote a Digital Service Version**
    ![uc12_promote.png](../images/uc12_promote.png)
	![uc12_promote_popup.png](../images/uc12_promote_popup.png)
	

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements                      | Type   | Description                                                                                  |
|-----------|-------------|-------------------------------|--------|----------------------------------------------------------------------------------------------|
|           | Page Header |                               | group  |                                                                                              |
| 1         |             | Promote Digital Service button| button | Selected a version to promote and click on promote button in row of version which we want to promote|
| 2         |             | Current status of a version   | label  | It show the current status (active, draft, archive) of a digital service version             |
| 2         |             | Promote Confirmation Popup    | Button | Click on the Promote button to promote a version                                             |

{{% /expand %}}
## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND -->> front: Click on Promote for Version
front -->> back: POST /organizations/{organization}/workspaces/{workspace}/digital-service-version/digitalServiceVersion/promote-version
DataBase -->> back: Promote current version to make active version.
front -->> back: GET /organizations/{organization}/workspaces/{workspace}/digital-service-version/{digitalServiceVersionUid}/manage-versions
back -->> DataBase: fetch all digital service versions related to digital service id of digital service version
back -->> front: return all versions related to digital service id of digital service version
back -->> front: Display the results of version with current version as active version and previous active as archived version.
{{< /mermaid >}}
