---
title: "2. Digital Service"
description: "All use cases related to assess an digital service footprint"
weight: 20
---

This module, designed for project teams, supports the eco-design of each digital service version. A single digital service can have multiple versions, and the module measures the environmental impact of every version individually — including the impact of user terminals, data circulating across the network, and hosting services associated with that version of the service.

## Table of contents

{{% children description="true" %}}

## Global Concepts

{{< mermaid align="center">}}
flowchart TD;
G4IT --> 1A(My Digital Services)
G4IT --> 2(My Information System)
G4IT --> 3(Administration panel)
1A --> 1B(List of Digital Services)
1B --> A(For a Digital Service, list of multiple versions)
A --> B(Digital Service version <br><i> 2.1. Visualize My Digital Service version)
A --> AB(Create new Digital Service Version <br><i> 2.2. Create a Digital Service Version)
A --> AC(Add Equipments <br><i> 2.3. Add Equipments)
A --> AD(Estimate the impact <br><i> 2.4. Launch an estimation)
A --> AE(Visualize footprint <br><i> 2.5. Visualize footprint)
A --> AF(Export a digital service version<br><i> 2.6. Export a digital service version)
A --> AG(Delete a digital service version<br><i> 2.7. Delete a digital service version)

classDef Type1 fill:gray, font-style: italic;
class 2,3 Type1;

{{< /mermaid >}}
