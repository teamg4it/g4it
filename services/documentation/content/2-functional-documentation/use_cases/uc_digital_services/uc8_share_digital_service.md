---
title: "2.8. Share a digital service"
description: "This use case describes how to share a digital service"
weight: 80
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Global view](#global-view)
-   [Sequence Diagram](#sequence-diagram)

## Description

This use case enables a project team to share a digital service externally from G4IT, allowing access without requiring a user account.
The shared digital service will be accessible to the user in read-only view.

**Navigation Path**

-   My Digital Services / Digital Service view / Share button

**Access Conditions**

-   The connected member must have the 'write' role for the digital service module one the selected organization.

## State Diagram

{{< mermaid align="center">}}
graph TD
Step1[Digital Service View] --> Decision1{Already Shared?}
Decision1 -- No --> Step2['Share' button displayed]
Step2 --> Step3[Click 'Share']
Step3 --> Step4[Popup opens to copy link]
Step4 --> Step5[Link copied]
Step5 --> Step6[Button changes to 'Shared']
Step6 --> Step7[Share with another user]

    Decision1 -- Yes --> Step8['Shared' button displayed]
    Step8 --> Step9[Click 'Shared']
    Step9 --> Step10[Popup opens to copy link]
    Step10 --> Step11[Link copied]
    Step11 --> Step12[Share with another user]


{{</ mermaid >}}

## Global View

-   'Share' button in the Digital service view :
    ![uc8_shareDs.png](../images/uc8_shareDs.png)

-   Popup to copy the link:
    ![uc8_sharePopUp.png](../images/uc8_sharePopUp.png)

-   'Share' button changes to 'Shared' button in the Digital service view :
    ![uc8_sharedDs.png](../images/uc8_sharedDs.png)



| Management rules | Title        | Rule description                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| ---------------- |--------------| ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1                | Share/Shared | <li><u>_initialization rules_</u>: The button is displayed only if user has the write access. <br><li> |

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Share/Shared" button
front ->> back: Share /api/organizations/{organization}/workspaces/{workspace}/digital-services/{digitalServiceUid}/share:
back -> DataBase: Generate a record in digital_service_shared_link table, with a validation token with expiry date after 60 days
back ->> front: Return the url
front ->> RND: PopUp opens with the link
{{</ mermaid >}}
