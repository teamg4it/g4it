---
title: '1. My IS inventory'
description: "All use cases related to assess an IS footprint"
weight: 10
---

This module, aimed at the information systems department, measures the impact of an information system, which includes
all employees' digital equipment (their screens, PCs, phones, etc.), shared equipment (televisions in meeting rooms,
surveillance cameras, etc.), network equipment, and servers.
The persona associated with this usecase is called the sustainable IT leader, who is in charge of managing an organization's environmental footprint.

## Table of contents

{{% children description="true" %}}

## Global Concepts

{{< mermaid align="center">}}
flowchart TD;
G4IT --> 1(My Digital Services)
G4IT --> 2(My Information System)
G4IT --> 3(Administration panel)
2 --> A(Information System overview <br><i> 1.1. Visualize IS inventory)
2 --> B(Create an Inventory - with or without files <br><i> 1.2. Create an inventory)
A --> AA(Load files <br><i> 1.3. Load files)
A --> AB(Launch/Update an estimate <br><i> 1.4. Launch an estimate)
A --> AC(Visualize equipment footprint <br><i> 1.5. Visualize equipment footprint)
A --> AD(Visualize application footprint <br><i> 1.6. Visualize application footprint)
A --> AE(Delete an inventory <br><i> 1.8. Delete inventory)
AC --> ACA(Export Equipment Indicators files <br><i> 1.7. Export files)
AD --> ADA(Export Application Indicators files <br><i> 1.7. Export files)

classDef Type1 fill:gray, font-style: italic;
class 1,3 Type1;

{{< /mermaid >}}
