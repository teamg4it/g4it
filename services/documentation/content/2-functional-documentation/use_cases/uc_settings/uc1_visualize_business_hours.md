---
title: '5.1. Visualize business hours'
description: "This use case describes how to visualize the business hours of G4IT"
weight: 10
mermaid: true
---

## Table of contents
- [Table of contents](#table-of-contents)
- [Description](#description)
- [Sequence Diagram](#sequence-diagram)
- [Service Opening Hours section view](#service-opening-hours-section-view)


## Description 
For a seamless experience and to make the most of your time on the platform, here's how you can easily find out when it's open for use:
1. Click on the "Settings" icon located in the navigation bar.

2. Within the settings menu, locate the 'Service Opening Hours' section, Here, you'll find all the information you need regarding the platform's operating hours.

### Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Access the G4IT app and proceed to the 'Settings' icon on bottom left.
front ->> back: GET /api/business-hours
back -->> front: Fetch the business hours data from the G4IT business_hours table.

{{< /mermaid >}}


### Service Opening Hours section view
![business_hours.PNG](../images/business_hours.PNG)




