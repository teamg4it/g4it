---
title: 'Release v3.11'
description: 'Introduction of Application Table View, enhanced Application view experience, data deletion alert system, EcoMindAI Beta release, and multiple stability and configuration fixes.'
weight: 130
---

## Overview

Release v3.11 introduces major usability and accessibility improvements, especially in the Application view of the Inventory module, along with important data lifecycle management enhancements.

This version brings a new **Table View for Application impacts**, improves navigation and descriptions in Application and Equipment views, and introduces a **data deletion alert system** to better inform users about upcoming data removal.

Additionally, the **EcoMindAI module is now available in Beta**, allowing users to request access and explore early features.

Overall, this release focuses on improving user experience, accessibility, transparency of data lifecycle, and platform stability.

### Data Deletion Alert System

A new alert mechanism has been introduced to inform users about upcoming data deletion.

- Users are notified when their Inventory, Digital Service, and indicators data are scheduled for deletion
- Default retention period is **2 years** (configurable by administrators)
- Email notifications are sent:
    - 1 month before deletion
    - 2 days before deletion
- Ensures users can export or take action before data is permanently removed


### Application View – Table View (NEW)

A new **Table View** has been added to the Application view in Inventory.

- Toggle available between:
    - Graphical View
    - Table View
- Displays **Top 10 applications by impact (People Equivalent)** by default
- Features:
    - Sortable columns
    - Pagination support
    - Dynamic columns based on selected criteria
- Includes:
    - Application name
    - Criteria
    - Multi-criteria impact (People eq.)
    - Raw impact values per criterion

### Application View Improvements

The Application (and Equipment) view has been significantly improved for better usability and consistency.

#### Navigation & Layout

- Introduction of a **return button** aligned with Digital Service module
- Improved header structure:
    - Inventory name
    - View title (new positioning)
- Harmonized titles and labels across views

#### Graph & Description Enhancements

- Reuse of standardized descriptions from Digital Service module (adapted to Inventory)
- Improved explanation of:
    - Criteria
    - Scale
    - Impact analysis
- Enhanced navigation through graph interactions:
    - Drill-down from multi-criteria → domain → subdomain → application → equipment
- Added description with link to Table View for detailed analysis

#### Additional Improvements

- Fixed additional graphs display
- Improved breadcrumb behavior across navigation levels
- Consistent display of lifecycle and environment graphs
- Translation fixes (e.g., “électrique”)
- Improved accessibility support

These changes make the Application view more intuitive, consistent, and aligned with other modules

---

### EcoMindAI – Beta

The EcoMindAI module is now introduced as a **Beta feature**.

- Clearly identified as a **Beta module** on the homepage
- New **“Request access”** button:
    - Sends an email request to support
- Once access is granted:
    - Button changes to **“Go to Service”**
- Designed to:
    - Differentiate experimental features from stable modules
    - Collect user feedback and validate use cases

---

### Update Reference data

Introduced an enhanced platform-level feature that enables Super Admin users to efficiently manage G4IT reference data. This includes the ability to update key reference tables such as Item Impact, Criterion, Lifecycle Step, Hypothesis, Item Type, and Matching Item. This improvement provides greater flexibility, better control over configuration data, and ensures consistency across the platform.

---

## 3.11.0

### Major Changes

- 1912 | Data deletion alert system with email notifications
- 2000 | Inventory Application view – Table View added
- 1837 | Application View improvements (navigation, graph, descriptions, UX)
- 1991 | EcoMindAI – Beta module introduction

### Minor Changes

- 1989 | Update Reference data in Super Admin mode
- 2070 | Fix default host value configuration in server
- 2069 | Fix issue preventing datacenter creation
- 1940 | Improve trace logging by removing reference values

---

## Installation Notes
