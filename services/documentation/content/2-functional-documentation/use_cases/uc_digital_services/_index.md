---
title: '2. Digital Service'
description: "All use cases related to assess an digital service footprint"
weight: 20
---

This module, designed for project teams, supports the eco-design of a digital service by measuring the impact of user
terminals, data circulating on the network, and hosting services associated with the service itself.

## Table of contents

{{% children description="true" %}}

## Global Concepts

{{< mermaid align="center">}}
flowchart TD;
G4IT --> 1(My Digital Services)
G4IT --> 2(My Information System)
G4IT --> 3(Administration panel)
1 --> A(My Digital Services <br><i> 2.1. Visualize My Digital Services)
1 --> B(Create new Digital Service <br><i> 2.2. Create a Digital Service)
A --> AC(Add Equipments <br><i> 2.3. Add Equipments)
A --> AD(Estimate the impact <br><i> 2.4. Launch an estimation)
A --> AE(Visualize footprint <br><i> 2.5. Visualize footprint)
A --> AF(Delete a digital service<br><i> 2.7. Delete a digital service)
A --> AG(Share a digital service<br><i> 2.8. Share a digital service)
A --> AH(Export a digital service<br><i> 2.6. Export a digital service)


classDef Type1 fill:gray, font-style: italic;
class 2,3 Type1;

{{< /mermaid >}}
