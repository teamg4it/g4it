---
title: "Network"
description: "This use case gives an overview how we assess footprint of fixed line network (last kilometer) and mobile network"
weight: 40
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [How we assess the footprint of fixed line network](#how-we-assess-the-footprint-of-fixed-line-network)
-   [How we assess the footprint of mobile network](#how-we-assess-the-footprint-of-mobile-network)

## How we assess the footprint of fixed line network

**Impact on lifecycle steps other than use (manufacturing, distribution, end of life, etc...)**
```math
$$
Impact_{step,criteria}= {Data Quantity × Impact Factor_{step,criteria,country, Landline Network} \over Landline capacity_{geographical Area}}
$$
```
With:
- Data quantity: number of Go exchanged on network per year
- Impact Factor: Impact factor for fixed line for all 
- Fixed line capacity: see [Zoom on fixed line capacity](#zoom-on-fixed-line-capacity)

**Impact on use**
```math
$$
Impact_{use,criteria}= {Data Quantity × Fixed Line Electricity Consumption × Mix Elec Impact Factor_{criteria,country} \over Landline Capacity_{geographical Area}}
$$
```
*With :*
- *Data quantity: number of Go exchanged on network per year*
- *Fixed line electricity consumption: Average Annual electricity consumption (cooper and fiber line)*
- *Fixed line capacity: see [Zoom on fixed line capacity](#zoom-on-fixed-line-capacity)*
- *Mix electric Impact Factor: Impact factor per Kw for the criteria and country in which the equipment is used*

### Zoom on fixed line capacity

The capacity was estimated with :
1. For France: 220 Go/month/connection x 12 months =**2 640 Go/year/line**
*(source: Rapport ADEME ARCEP 2020, VOLET 2; p.70)*

2. For EU: 200 Go/month/connection x 12 months = **2 400 Go/year/line** 
*(source: The state of digital communications 2020; figure 2.7: Fixed and mobile data usage per connection europe, 2013–2019; p.37)*


## How we assess the footprint of mobile network

**Impact on lifecycle steps other than use (manufacturing, distribution, end of life, etc...)**
```math
$$
Impact_{step,criteria}= {Data Quantity × Impact Factor_{step,criteria,country, mobile Network}}
$$
```
*With :*
- *Data quantity: number of Go exchanged on network per year*
- *Impact Factor: Impact factor for mobile network for all*

**Impact on use**
```math
$$
Impact_{use,criteria}= {Data Quantity × Electricity Consumption × Mix Elec Impact Factor_{criteria,country}}
$$
```
*With :*
- *Data quantity: number of Go exchanged on network per year*
- *Electricity Consumption: Average consumption per Go for 2G, 3G, 4G lines*
- *Mix electric Impact Factor: Impact factor per Kw for the criteria and country in which the equipment is used*

NumEcoEval Environmental footprint assessment methodology is described in the
[NumEcoEval Documentation](https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/blob/develop/docs/MoteurDeCalculG4IT_V1.1.adoc "NumEcoEval Documentation")
