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
- Administration panel / Manage organizations / Visualize organization / configure criteria for one subscriber
- Administration panel / Manage users / Visualize role / configure criteria for one organization
- Digital Services panel / My DI inventory / My Digital Service / configure criteria for my digital service
- Information System panel / My IS inventory / My Information System / configure criteria for my information system

**Access Conditions**  


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

![uc5_choose_criteria.png](../images/uc5_choose_criteria.png)

## Behavior Rules

### Main page
{{% expand title="Show the detail" expanded="false" %}}

| Reference | Elements                    | Type   | Description                                                                                                                                                                                                                                                                                                                                                                                      |
|-----------|-----------------------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 4         | Delete confirmation         | Label  |                                                                                                                                                                                                                                                                                                                                                                                                  |
| 5         | Delete confirmation message | Label  | <li><u>*initialization rules*</u>:: The message is displayed "After 7 days, all your organization's data will be deleted. are you sure you want to delete this organization ?"                                                                                                                                                                                                                   |
| 6         | Close the window            | Button | <li><u>*action rules*</u>: Close the window. No deletion is performed.                                                                                                                                                                                                                                                                                                                           |
| 6         | Delete                      | Button | <li><u>*action rules*</u>: After confirmation of deletion, the window is closed and datas are retentionned 7 days, and during this period, administrator can cancel the deletion at any time (on the organisations list, near the organization for which the deletion have been requested, Cancel deletion button and information message with the date of the effective deletion is displayed). |

{{% /expand %}}

## Sequence Diagram

