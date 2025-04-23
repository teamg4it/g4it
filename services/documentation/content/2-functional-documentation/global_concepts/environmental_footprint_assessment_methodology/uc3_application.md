---
title: "Application equipment"
description: "This use case gives an overview how we assess footprint of an application"
weight: 30
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [How we assess the footprint of an application](#how-we-assess-the-footprint-of-an-application)

## How we assess the footprint of an application

The Environmental footprint of an application is directly related to the impact of the virtual machines
on which its is deployed.
```math
$$Impact_{step,criteria}=\sum_{i}^{n} Impact_{VMi,step,criteria}$$
```
*With :*
- *Impact: Impact of the VM*

NumEcoEval Environmental footprint assessment methodology is described in the
[NumEcoEval Documentation](https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/blob/develop/docs/MoteurDeCalculG4IT_V1.1.adoc "NumEcoEval Documentation")
