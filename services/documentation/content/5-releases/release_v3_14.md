---
title: 'Release v3.14.0'
description: 'Enhanced visualization, Digital Service context management, improved import reliability, smarter workspace administration, and multiple calculation and usability improvements.'
weight: 142
---

## Overview

Release v3.14.0 introduces significant enhancements focused on **analysis capabilities, data quality, import reliability, and user experience**.

This version enriches the **Digital Service module** by allowing users to document the context and assumptions behind each impact assessment, improving traceability and collaboration over time. The visualization experience has also been redesigned with new histogram-based charts and axis inversion capabilities, making impact comparisons clearer for complex datasets.

Additionally, several improvements strengthen the reliability of inventory imports, workspace reference data management, and impact calculations. Together with numerous usability enhancements and calculation fixes, this release delivers a more robust, transparent, and user-friendly platform.

---

### Digital Service Context & Calculation Assumptions

- Dedicated **Context and assumptions** tab during Digital Service creation
- Capture assessment context, assumptions, sources, methodology and limitations
- Context is versioned and remains editable
- Improves traceability and collaboration

---

### Enhanced Graph Visualization

- Automatic histogram view when more than five comparison items are displayed
- Axis inversion for easier comparison of lifecycle stages, entities, countries, environments and equipment types
- Histogram sorting, zoom controls and preserved axis selection across views

---

### EcoMindIA Access Request Workflow

- EcoMindIA access is no longer granted automatically for new workspaces
- Users must explicitly request access
- New **Request access** button available when access has not yet been granted

---

### Smarter Inventory Import Management

- Automatic detection and timeout of stalled imports
- Loading indicator during inventory creation
- Improved validation messages for inventory imports
- Better validation during workspace reference data imports

---

### Workspace Reference Data Improvements

- New workspace reference data model available for download

---

### Calculation Accuracy Improvements

- Correct equipment count and average lifespan calculations
- Improved electricity mix resolution priority
- Average PUE display fixes
- Correct handling of configured retention periods

---

### Additional Improvements

- Automatic redirection to login after session expiration
- General stability and usability improvements

---

## 3.14.0

### Major Changes

- 2141 | Explicit access request workflow for the EcoMindIA module
- 2196 | Context and calculation assumptions for Digital Services
- 2158 | Axis inversion for main impact graphs
- 2150 | Automatic histogram view for graphs displaying more than five items
- 2229 | Automatic detection and timeout of stalled file imports

### Minor Changes

- 2295 | Correct equipment count and average lifespan calculations
- 2183 | New workspace reference data model and updated platform data model
- 2193 | Improved validation during workspace reference data imports
- 2304 | Redirect users to the login page when sessions expire
- 2217 | Loading indicator during inventory creation
- 2288 | Improved electricity mix resolution during equipment impact calculation
- 2190 | Fixed character encoding when exporting platform and workspace reference data
- 2364 | Correct Average PUE display when filtering equipment types
- 2157 | Correct handling of configured data retention periods
- 2195 | Improved validation and actionable feedback during inventory imports

---

## Installation Notes


