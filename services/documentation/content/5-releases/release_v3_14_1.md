---
title: 'Release v3.14.1'
description: 'Improved scalability and concurrency for large datasets, a clearer Digital Service input journey, and enhanced accessibility and usability.'
weight: 145
---

## Overview

Release v3.14.1 focuses on **performance, scalability, concurrency, accessibility, and user experience**.

This version introduces significant improvements to the way the platform handles **large inventories and concurrent background processes**, strengthening the robustness of data retrieval, imports, calculations, scheduling, and database operations. These changes help ensure that long-running operations can execute concurrently without unnecessarily blocking other platform activities.

The release also improves the **Digital Service creation experience** with clearer guidance and a more structured input journey. In addition, several accessibility improvements ensure that the application remains usable at **200% browser zoom and focus to be retained on button while closing sidebar** and that key interface elements provide better support for assistive technologies.

Overall, this release improves platform scalability, operational reliability, accessibility compliance, and ease of use.

---

### Performance & Scalability for Large Datasets

The Inventory module has been optimized to better support inventories containing large amounts of data and growing database volumes.

#### Improvements

- Optimized inventory data retrieval and API processing
- Reduced unnecessary retrieval of large datasets
- Improved loading of inventory overview and table views
- Optimized loading of filter values
- Improved loading of environmental results and impact charts
- Reduced excessive in-memory processing and large response payloads
- Improved handling of large database volumes

#### Benefits

- More reliable opening of large inventories
- Reduced risk of timeouts and browser freezing
- Improved response times when navigating large datasets
- Better scalability as inventory and database volumes grow
- Improved support for concurrent users

---

### Scheduler & Concurrent Processing Improvements

The execution model for background processes has been reviewed and improved following the introduction of automatic detection of stalled imports.

The improvements focus on reducing contention between:

- Inventory imports
- Inventory calculations
- Digital Service calculations
- Scheduler activities
- Other background processes

#### Improvements

- Reduced database locking and transaction contention
- Improved scheduler execution to minimize its impact on business processes
- Improved concurrent processing of long-running tasks
- Reduced unnecessary serialization of workloads
- Improved transaction and locking management
- Enhanced monitoring and diagnostic capabilities

These changes ensure that monitoring and background activities do not unnecessarily delay imports, calculations, or other user operations.

---

### Digital Service Input Journey Improvements

The Digital Service creation journey has been redesigned to make the input process clearer and easier to understand.

#### Improvements

- Clearer structure and logical grouping of information
- Informational guidance displayed above input sections
- Improved explanations for fields and their impact
- Revised user device input journey with dedicated sections
- Additional explanatory content for devices, network, private infrastructure and cloud configuration
- Improved guidance when adding virtual machines to shared servers
- Support for progressive completion when precise information is not immediately available

These improvements reduce cognitive overload and help users better understand the information required during Digital Service creation.

---

### Accessibility Improvements

Several accessibility issues identified through accessibility testing and audits have been resolved.

#### Improvements

- Improved ARIA landmark roles across application pages
- Improved keyboard navigation and focus management
- Improved identification of the application's logo for assistive technologies
- Improved accessibility of workspace selection controls
- Improved accessibility of Digital Service tooltips
- Improved table caption association
- Improved accessibility of Inventory search and filter controls
- Improved focus management when opening filter panels
- Improved contextual labels for filter options

These changes provide a more consistent and accessible experience for keyboard users and users of assistive technologies.

---

### Accessibility at 200% Zoom

The application has also been improved to ensure key features remain usable when the browser is configured at **200% zoom**.

#### Improvements

- Digital Service Context and Assumptions editor remains fully usable
- Inventory note editor remains accessible
- Workspace selection remains available in responsive navigation mode
- Digital service side panels remain readable and functional

These improvements help ensure that users can continue to access and operate the application effectively at higher zoom levels.

---

## 3.14.1

### Major Changes

- 2384 | Improve performance and scalability for large datasets
- 2386 | Improve scheduler and concurrent processing management

### Minor Changes

- 2232 | Redesign Digital Service input journey to improve clarity, guidance, and usability
- 2376 | Resolve accessibility issues at 200% zoom
- 2371 | Resolve accessibility issues for drawers and other interface elements

---

## Installation Notes

This release contains important improvements to platform scalability and concurrent processing. It is recommended for environments handling large inventories or multiple long-running operations, as well as users who require improved accessibility and usability.
