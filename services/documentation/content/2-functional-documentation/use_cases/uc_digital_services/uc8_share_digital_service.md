---
title: '2.8. Share digital service'
description: "This use case describes how to share a digital service"
weight: 80
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [Mockup](#mockup)
-   [State Diagram](#state-diagram)
-   [Sequence Diagram](#sequence-diagram)

## Description

This use case enables project team members to share a digital service through a link.
The link generated can be accessed by other team members to visualize and/or make changes in the digital service.
Team members have the option to disconnect from the shared digital service from their access without deleting it, leaving the decision to the creator.
The project team can view the details of the creator and members who have access to a digital service.

**NOTE:**  
The link generated will expire after a day of creation.

**Navigation Path**  
- My Digital Services / "Created by me" section / Copy Link button
- My Digital Services / "Share with me" section / Copy Link button
- My Digital Services / Digital Service view / Copy Link button

**Access Conditions**
- To be able to share a DS, the connected member must have the 'write' role for the digital service module on the selected organization.
- To access a shared DS link, a member must have at least the 'read' role for the digital service module on the selected organization.

## State Diagram

{{< mermaid align="center">}}
graph TD;
User1 --> Step1[DS list view/ DS view] --> Decision1{User has DIGITAL_SERVICE_Write right}
Decision1 -->|Yes| Step2[Click on 'Copy Link' button]
Decision1 -->|No| Step3[No button visible]
Step2 --> Step4[DS link is copied to the clipboard]
Step4 --> Step5['Copy Link' button changes to 'Link Copied' and reverts after 10 seconds] -->|Share the DS link to another user| Step6
Step6[Link expires after 1 day]

User2 -->|Click the shared link| Decision2{User has the DIGITAL_SERVICE_READ right on the organization of shared DS?}
Decision2 -->|No| Step7[Error message and user is redirected to the default page]
Decision2 -->|Yes| Step8[DS is associated with the user] --> Step9
Step9[User is redirected to the DS footprint view] --> Step10
Step10[Linked DS is displayed under the 'Shared with me' section]

{{</ mermaid >}}

## Mockup

- My Digital Services
![uc8_share_digitalService_globalview.png](../images/uc8_share_digitalService_globalview.png)

- My Digital Services / Digital Service view
![uc8_share_digitalService_DSview.png](../images/uc8_share_digitalService_DSview.png)

- 'Eye Hide' button to unlink the digital service
![uc8_share_digitalService_unlink.png](../images/uc8_share_digitalService_unlink.png)

- 'User icon' button to view the creator and members' information
![uc8_share_digitalService_userIcon.png](../images/uc8_share_digitalService_userIcon.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

### Share a Digital Service accessible by the connected user

| Management rules | Title         | Rule description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1                | User icon     | <li><u>*initialization rules*</u>: The button is displayed only if the digital service is shared with one person.<br><li><u>*action rules*</u>: One click on the button open a window  and the user's information accessing the digital service is displayed : <br>- one section named "Creator" displayed the first name and last name of the creator;<br>- one section named "members" displayed the first name and last name of all the users which access the shared link.                                                                                 |
| 2                | Share number  | <li><u>*initialization rules*</u>: The button is concatenation with  "(" + "the number of users with whom the current user shared the Digital Service" +")"                                                                                                                                                                                                                                                                                                                                                                                                    |
| 3                | Copy Link     | <li><u>*action rules*</u>: On click of the button, a link is generated and is copied to the user's clipboard. Once the link is copied the 'Copy Link' button changes to 'Link Copied' and reverts back after 10 seconds.                                                                                                                                                                                                                                                                                                                                       |
| 4                | Eye hide icon | <li><u>*initialization rules*</u>: The button is displayed only if the digital service is on the section "Share with me".<br><li><u>*action rules*</u>: The confirmation message : "By removing the link too this digital service, you will no longer have access to it. Are you sure ?" is displayed.<br> If the user click on "no", the window is closed and no change.<br>If the user click on "Yes", disconnects the digital service from the user's access without deleting it. The Digital service is not more displayed in the section "Share with me". |

### Access to a Digital Service shared by one other user

| Management rules | Title            | Rule description                                                                                                                                                                                                                                                                                       |
|------------------|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1                | Copied link used | <li><u>*action rules*</u>: Digital Service is associated with the user : the digital service will be displayed in the "Share with me" section of the Digital Services page. <br> User is redirected to the DS footprint view [2.5. Visualize digital service's footprint](uc5_visualize_footprint.md)  |

{{% /expand %}}


## Sequence Diagram

### Share a Digital Service accessible by the connected user
{{< mermaid >}}
sequenceDiagram
actor RND as Project Team User 1
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "Copy Link" button
front ->> back: POST /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/share
back -> DataBase: Link generated for the digital service
back ->> front: The generated link
front->> RND: Link is copied in the clipboard
front-> RND: The button "Copy Link" changes to "Link Copied" button
back -> DataBase: The link expires after 1 day of creation.
{{</ mermaid >}}

### Access to a Digital Service shared by one other user
{{< mermaid >}}
sequenceDiagram
actor RND as Project Team User 2
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on the 'shared link' button
front ->> back: POST /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/shared/{sharedUid}
back -> DataBase: If user has read right on DS module on the selected organization.
back -> DataBase: User is associated to the shared DS
back ->> front: User is associated
front->> RND: Redirect to the visualization of this digital service
front-> RND: The DS can be visualized in the 'Shared with me' list view
RND ->> front: Click on the 'eye hide' button
front ->> back: DELETE /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/shared/{sharedUid}
back -> DataBase: Access to the shared DS is removed
front-> RND: DS is removed from the 'Shared with me' view.
{{</ mermaid >}}

