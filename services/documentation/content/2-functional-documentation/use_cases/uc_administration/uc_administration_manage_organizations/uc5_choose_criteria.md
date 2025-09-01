---
title: '3.2.5 Choose criteria'
description: "This use case describes how to choose criteria for impact calculation and associate them to the subscriber, to the organization or to the Information System."
weight: 50
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

This use case allows to configure the impact criteria to perform an estimation.

**Navigation Path**  
- Administration panel / Manage organizations / Visualize workspace / configure criteria for one subscriber
- Administration panel / Manage users / Visualize role / configure criteria for one organization
- Digital Services panel / My DI inventory / My Digital Service footprint view / configure criteria for my digital service
- Information System panel / My IS inventory / My Information System / configure criteria for my information system

**Access Conditions**  
The connected user must have the subscriber administrator role for at least one subscriber
or organization administrator role for at least one organization.

## State Diagram
{{< mermaid >}}

flowchart TD;

    subgraph Subscriber#1[<i> Subcriber]
        Subscriber#1_Admin[fa:fa-cogs Criteria default for the Subscriber]
        subgraph Organization
            Organization#1_Admin[fa:fa-cogs  Criteria default for the Organization]
            subgraph IS1DS1[ User level]
                IS1("Override criteria for My Information Systems") 
                DS1("Override criteria for My Digital Services")
            end
        end
    end

Subscriber#1_Admin --> Organization#1_Admin
Organization#1_Admin --> IS1
Organization#1_Admin --> DS1

{{< /mermaid >}}

## Mockup
### Configure criteria for one subscriber ###

![uc5_choose_criteria.png](../images/uc5_choose_criteria.png)

## Behavior Rules

### Main page
{{% expand title="Show the detail" expanded="false" %}}

| Reference | Elements         | Type     | Description                                                                                                                                                                                                                                                                                                                                                      |
|-----------|------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1         | Select All       | Checkbox | <li><u>*action rules*</u>: Click the checkbox to get all criteria selected.                                                                                                                                                                                                                                                                                      |
| 2         |                  | group    | <li><u>*initialization rules*</u>:: List of the criteria.                                                                                                                                                                                                                                                                                                        |
| 3         | Reset to default | Button   | <li><u>*action rules*</u>: To configure criteria for a subscriber, it restores 5 default criteria (Climate change, Resource use, Ionising radiation, Particulate matter, Acidification); for an organization, it restores criteria set by the subscriber admin; for a digital service or information system, it restores criteria set by the organization admin. |
| 4         | Save             | group    | <li><u>*initialization rules*</u>: Button is enabled once the criteria are updated. <li><u>*action rules*</u>: Click to save the criteria.                                                                                                                                                                                                                       |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Administrator
participant front as G4IT Front-End
participant back as G4IT Back-End
participant db as G4IT Database

    RND ->> front: Configure criteria for one subscriber
    RND ->> front: Click settings button, select the criteria and click save button
    front ->> back: PUT /api/administrator/subscribers
    back ->> db: save the criteria
    front ->> back: GET /api/administrator/subscribers
    front ->> RND: Get updated criteria

    RND ->> front: Configure criteria for an organization
    RND ->> front: Click settings button, select criteria for Digital service and Information system module and click save button
    front ->> back: PUT /api/administrator/organizations
    back ->> db: save the criteria
    front ->> back: GET /api/administrator/organizations
    front ->> RND: Get updated criteria

    RND ->> front: Configure criteria for Digital service or Information system
    RND ->> front: Click settings button, select criteria and click save button
    front ->> back: PUT /api/subscribers/{subscriber}/organizations/{organization}/inventories
    front ->> back: PUT /api/subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}
    back ->> db: For an Information System, save the criteria
    back ->> db: For a Digital Service, save the criteria and re-launch the calculation

{{< /mermaid >}}
