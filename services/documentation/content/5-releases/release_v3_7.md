---
title: 'Release v3.7'
weight: 80
---

## Overview

This release introduces major usability, data, and sustainability improvements to G4IT.

### Segmented Graph Descriptions

To enhance the clarity and accessibility of graph descriptions in Digital Services, the description format is updated.
The graph description are segmented by items to facilitate better comprehension of the displayed information.
![Picture showing the detailed digital service graph descriptions](../images/releaseV370_description_digital_service.png)


### Decimal Input for VM/Pod Size

Fields "Total vCPU" and "Total Disk (GB)" while creating a VM within a Digital Service, now accept decimal numbers to better support containerized workloads.

### Direct VM Electricity Consumption

Users can now specify the actual electricity consumption of a VM within Digital Service(e.g., via Scaphandre, Kepler, Power API). If provided, this value is used for the "usage" phase footprint instead of host-based allocation.

## Content

### V3.7.0

**Major Changes**

- 1461 | Digital Service Modification of graph Description

**Minor Changes**

- 1511 | Remove Complementary PUE field from Ecomind
- 1548 | New design for Visualize my results
- 1549 | [Digital service] Same design for the pop-up in the visualization
- 1597 | add comma for VM
- 1598 | [AFNIC] NumEcoEval - Electricity consumption of a Virtual Machine

## Installation Notes

