---
title: "Physical equipment"
description: "This use case gives an overview how we assess footprint of an equipment depending on lifecycle steps and how we assess footprint of a Terminal (for Digital Service)"
weight: 10
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [How we assess the footprint of equipment depending on lifecycle steps](#how-we-assess-the-footprint-of-equipment-depending-on-lifecycle-steps)
-   [How we assess the footprint of a Terminal (for Digital Service)](#how-we-assess-the-footprint-of-a-terminal-for-digital-service)

## How we assess the footprint of equipment depending on lifecycle steps

**Impact on lifecycle steps other than use (manufacturing, distribution, end of life, etc...)**
```math
$$
Impact_{step,criteria} = {Quantity × Impact Factor_{criteria,step,equipment}\over lifespan_{equipment}}
$$
```
*With :*
- *Quantity: quantity of equipment*
- *Impact Factor: Impact factor per Kw for the criteria and country in which the equipment is used*
- *Lifespan: Lifespan of the equipment*

**Impact on use**
```math
$$
Impact_{use,criteria} = {Quantity × Electricity Consumption × PUE × Impact Factor_{criteria,country} }
$$
```
*With :*
- *Quantity: quantity of equipment*
- *Electricity consumption: annual electricity consumption of the equipment*
- *PUE: Power usage effectiveness of the datacenter in which the equipment is hosted or used. When the PUE of a datacenter is unknown a default setting define on the platform is used (Default PUE = 1.75). NB: Not relevant for equipment used outside a datacenter.*
- *Impact Factor: Impact factor per Kw for the criteria and country in which the equipment is used*

### Zoom on lifespan
Several cases to determine the lifespan of equipment.
1. Equipment is retired, and its purchase date and retirement date are known
```math
$$
lifespan={Retirement Date - Purchase Date}
$$
```
2. Equipment is still under the responsibility of the organization and its purchase date is known
```math
$$
lifespan={Current Date - Purchase Date}
$$
```
3. Alternatively, lifespan is determined from hypothesis set at equipment type level by the organization 
4. As a last resort, lifespan is determined based on a default setting defined on the platform: 2 years.
N.B: As footprint is assessed yearly, when lifespan is less than 1 year, the system brings it to 1.

## How we assess the footprint of a Terminal (for Digital Service)

**Impact on lifecycle steps other than use (manufacturing, distribution, end of life, etc...)**
```math
$$
Impact_{step,criteria} = {Number Of Users × Yearly Usage Time Per User× Impact Factor_{criteria,step,equipment} \over lifespan_{equipment} × NumberOfAnnualHours} 
$$
```
*With :*
- *Number of users: the number of users of the digital service*
- *Yearly usage time per user: time spent by each user on the service (in hours)*
- *Impact Factor: Impact factor per Kw for the criteria and country in which the equipment is used*
- *Lifespan: Lifespan of the equipment*
- *Number of annual hours: 365 x 24*

**Impact on use**
```math
$$
Impact_{use,criteria} = {{Number Of Users × Yearly Usage Time Per User × Electricity Consumption × Impact Factor_{criteria,country}} \over NumberOfAnnualHours} 
$$
```
*With :*
- *Number of users: the number of users of the digital service*
- *Yearly usage time per user: time spent by each user on the service (in hours)*
- *Electricity consumption: annual electricity consumption of the equipment*
- *Impact Factor: Impact factor per Kw for the criteria and country in which the equipment is used*
- *Number of annual hours: 365 x 24*

### Zoom on lifespan

Several cases to determine the lifespan of equipment.
1. Lifespan set in the application
2. Alternatively, lifespan is determined from hypothesis set at equipment type level by the organization 
N.B: As footprint is assessed yearly, when lifespan is less than 1 year, the system brings it to 1.

NumEcoEval Environmental footprint assessment methodology is described in the
[NumEcoEval Documentation](https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/blob/develop/docs/MoteurDeCalculG4IT_V1.1.adoc "NumEcoEval Documentation")

