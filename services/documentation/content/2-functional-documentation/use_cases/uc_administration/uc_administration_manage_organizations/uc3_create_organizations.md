---
title: '3.2.3 Create organizations'
description: "This use case describes how to create an organization in the administration module"
weight: 30
mermaid: true
---


## Table of contents
- [Table of contents](#table-of-contents)
- [Description](#description)
- [State Diagram](#state-diagram)
- [Mockup](#mockup)
- [Behavior Rules](#behavior-rules)
- [Sequence Diagram](#sequence-diagram)


## Description

This use case allows an administrator to create organizations in an administration.
In admin panel, when a subscriber is chosen, administrator can edit the list with pencil button at the top right.
At the top of the list, administrator can name a new organization then add it to subscriber.
If name already exists, a warning appears while typing.

**Navigation Path**  
Administration panel / Manage organizations / Edit mode 

**Access Conditions**  
The connected user must have the subscriber administrator role for at least one subscriber.

## State Diagram

{{< mermaid >}}

graph TD;
Step1[Organization Panel]-->|Click on edit button|Step2[Edition mode]
Step2 --> |Name a new organization|Decision1{Is name already exists?}
Decision1 -->|Yes|Step2
Decision1 -->|click on add button|Step3[Organization added]-->Step1

{{< /mermaid >}}

## Mockup
![uc3_create_orga.png](../images/uc3_create_orga.png)

## Behavior Rules
{{% expand title="Show the detail" expanded="false" %}}

| Reference | Elements     | Sub-Elements        | Type        | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|-----------|--------------|---------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Organization |                     | Input field | <li><u>*initialization rules*</u>:: Display the label of the organization<br><li><u>*action rules*</u>: The name can be filled in. If the name already exists, the error message "Organization already exist" is displayed. If the modification is done to add New Organization(first line of the list), the creation of the organization will be triggered on the "Add an organization" button. If the modification is done for an existing organization, the modified label will be saved when the connected user clicks on the validate button. |
| 6         |              | Add an organization | Button      | <li><u>*initialization rules*</u>:: activate only when the "New organization name" is filled in with at least one caracter.<br><li><u>*action rules*</u>: If the name filled in already exists, the error message "Organization already exist" is displayed else the organization is created.                                                                                                                                                                                                                                                      |
{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Open Edition mode
RND ->> front: Name a new organization
front ->> RND : Alert if name already exists
RND ->> front : Click on Add button
front ->> back : POST /api/administrator/organizations
front ->> RND: Display the list of organizations

{{< /mermaid >}}

