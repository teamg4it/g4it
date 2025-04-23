---
title: "Virtual equipment"
description: "This use case gives an overview how we assess footprint of a virtual equipment"
weight: 20
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [How we assess the footprint of_virtual equipment](#how-we-assess-the-footprint-of-virtual-equipment)

## How we assess the footprint of virtual equipment

The Environmental footprint of a virtual machine is directly linked to the environmental footprint of the underlying physical equipment 
on which the virtual machine is hosted. Therefore, we allocate a portion of the impact assessed for the underlying physical equipment to determine the impact of virtual equipment.  
```math
$$
Impact_{step,criteria}= {Impact_{physical equipment,step,criteria} × AllocationFactor}
$$
```
*With :*
- *Impact: The impact of the underlying physical equipment*
- *Allocation Factor: Allocation factor determined according to the type of the virtual equipment*

**Specificity for digital service module :**

Projects are often unaware of the underlying host of the virtual machine and the number of other VMs used in the same host. 
In consequence, the host could be approximated by the proposed server size S, M, L in the list box and the formula is adapted : 
- for a compute VM, the denominator 
```math
$$
{\sum_{VM \in host}{_{v}CPU}_{VM}}
$$
```
is replaced by
```math
$$
{_{v}CPU_{VM}}
$$
```
- and, for a storage VM,
```math
$$
{\sum_{VM \in host}storageCap_{VM}}
$$
```
 is replaced by 
 ```math
$$
{storageCap_{VM}}
$$
```
- and a news ratio is integrated 
```math
$$
{ × Annual Fixed Time}\over{NumberOfAnnualHours}
$$
```

### Zoom on an allocation factor

The allocation factor to apply is determined depending on three different cases :
1. Allocation factor to apply is an attribute of the VM
2. VM is of type “compute”

```math
$$
Allocation Factor={{_{v}CPU}\over{\sum_{VM \in host}{_{v}CPU}_{VM}}}
$$
```
*With*
- *vCPU: Number of vCPU of the VM*
- *Host: The underlying physical equipment*

3. VM is of type “storage”
```math
$$
Allocation Factor={{storageCap}\over{\sum_{VM \in host}storageCap_{VM}}}
$$
```
*With*
- *vCPU: Number of vCPU of the VM*
- *Host: The underlying physical equipment*

NumEcoEval Environmental footprint assessment methodology is described in the
[NumEcoEval Documentation](https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/blob/develop/docs/MoteurDeCalculG4IT_V1.1.adoc "NumEcoEval Documentation")
