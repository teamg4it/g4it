---
title: 'Release v3.5'
weight: 60
---

## Overview

A new **Digital Service View** has been introduced, offering a centralized interface to configure all types of resources—networks, terminals, cloud services, and non-cloud services—in a single, streamlined tab.

> **Note**
> - *Non-Cloud Servers* are now referred to as **Private Infrastructures**.
> - *Cloud Services* are now referred to as **Public Clouds - IaaS**.

### Redesigned Digital Service View
![releaseV350_digital_service_view.png](../images/releaseV350_digital_service_view.png)

The Digital Service View has been redesigned to be more intuitive and user-friendly. All resource inputs are now grouped into one dedicated tab, while results are displayed in a separate tab.

**Key improvements include:**
- Updated button design
- Improved mobile support
- Enhanced accessibility, including support for 200% zoom

### Option to Disable Data Consistency
![releaseV350_configureView_EditCriteria.png](../images/releaseV350_configureView_EditCriteria.png)

By default, **data consistency is now disabled** in the Digital Service View. However, users can choose to re-enable it when needed.

This update helps avoid confusion in the result display—especially regarding cloud data—by making the data behavior more transparent.

### Expanded Criteria for Cloud Evaluation

With this new version, it is now possible to evaluate cloud infrastructures against **all G4IT criteria**, not just **Climate Change**. This enhancement provides a more comprehensive impact assessment of **Public Cloud - IaaS** services.

## Content

### V3.5.0
**Major Changes**

- 796 | Add new criteria based on the one available in BoaviztAPI
- 1103 | Give the possibility to disable the data consistency
- 585 | Display only users that can be managed in the administration panel
- 1349 | Design changes required for the Maturity panel
- 1344 | Direct vizualisation of app without domain
- 1482 | Rename Server Resource in Digital Service

**Minor Changes**

- 1446 | Fix medium priority checkmarks issues
- 1391 | Update spring boot verion
- 1346 | Rename / delete workspace for non subscriber admin
- 1245 | Facilitate the criteria selection
- 1345 | Improve tracability of the calculation
- 1430 | Consistency for the import file on the virtual equipment
- 1373 | BE Sonar fixe

## Installation Notes

