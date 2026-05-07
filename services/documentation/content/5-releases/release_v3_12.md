---
title: 'Release v3.12'
description: 'Redesigned Inventory filters, workspace reference data management, enhanced data retention visibility, source transparency in graphs, and multiple usability and stability improvements.'
weight: 135
---

## Overview

Release v3.12 introduces major improvements focused on **customization and usability** across the platform.

This version brings a redesigned **filter experience in the Inventory module**, enhances **data retention visibility and extension capabilities**, and introduces **workspace-level reference data management** for greater flexibility in impact calculations.

Additionally, a new **Source section below graphs** improves transparency on calculation data, while several refinements enhance consistency and usability across Inventory and Digital Service modules.

Overall, this release strengthens user autonomy, transparency, and analytical capabilities while improving overall platform experience.

### Inventory Module – Filter Experience Redesign

The filtering experience in the Inventory module has been redesigned for better usability and clarity.

- Filters moved from **top overlay bar to a right-side panel**
- Aligned with the **“Configure the view”** button
- Features:
    - Foldable/unfoldable filter sections (collapsed by default)
    - Indicator showing the **number of active filters**
    - Visual icon displayed when filters are applied
- Filters now:
    - Apply to the **entire page**
    - Dynamically update key indicators and impacts

These filters allow users to view only a portion of the calculated data while ensuring consistency across the page.

---

### Data Retention Visibility & Extension

Enhancements to the data lifecycle management system introduced in v3.11 improve transparency and user control.

- Persistent message displayed when data is approaching deletion:
    - **EN:** “Data will be deleted on [date], click here to extend”
    - **FR:** “Les données vont être supprimées le [date], cliquez sur ce lien pour les conserver”
- Visible within the **pre-deletion notification window (e.g., 1 month before deletion)**
- Direct link to the existing extension popup
- Applies to:
    - Inventory
    - Digital Services
    - EcoMindAI
- Email notifications are now sent to **all users with write access**

This ensures users can anticipate, manage, and extend their data lifecycle effectively.

---

### Workspace Reference Data Management 

A new feature allows **workspace-level customization of reference data**, giving users full control over their impact models.

- Workspace admin users can:
    - View, download, edit, and upload reference data at workspace level
- Editable tables:
    - `ref_item_type`
    - `ref_item_impact`
    - `ref_matching_item`

#### Behavior

- Workspace data **overrides platform reference data**
- Upload rules:
    - Existing data → updated
    - New data → created
    - Missing data → deleted
    - Empty file → deletes all workspace data
- Full validation before update (no partial updates)

#### Additional Improvements

- New tab: **“Update workspace reference data”**
- Existing tab renamed: **“Update platform reference data”**
- Downloadable ZIP:
    - `workspace_reference_data_<workspaceName>.zip`

This enables organizations to tailor environmental impact calculations to their specific context.

---

### Workspace Reference Data in Calculations

Workspace-level reference data is now fully integrated into calculation logic.

- Priority order:
    1. Workspace reference data
    2. Platform reference data (fallback)

#### Calculation Logic

- If model exists in `ref_matching_item`:
    - Use associated impact reference (workspace first)
- If no model match:
    - Use equipment type reference (workspace first)
- If no model provided:
    - Use equipment type reference

#### Additional Feature

- New export link in Inventory:
    - **EN:** Workspace specific settings (.zip)
    - **FR:** Paramètres spécifiques à l’espace de travail (.zip)

This ensures more accurate and customizable impact calculations.

---

### Sources Visibility in Graphs 

A new **“Source” section** has been added below each graph to improve transparency.

- Located between:
    - Analysis
    - To go further
- Displays:
    - Reference datasets used
    - Calculation engines used

This helps users better understand the origin and reliability of calculated results.

---

### Application View Improvements

Further refinements have been made to the Application view in the Inventory module:

- **Graph view is now the default view**
- Introduction of a new section:
    - **“Additional elements of analysis”**
- Improvements include:
    - Clear separation of lifecycle and environment graphs
    - Cleaner layout with centered titles and removed icons

These updates improve readability and analytical clarity.

---

## 3.12.0

### Major Changes

- 2001 | Inventory module filter redesign (side panel)
- 2017 | Data retention visibility and extension in GUI
- 1983 | Workspace reference data management
- 2080 | Workspace reference data used in calculations
- 2081 | Source visibility below graphs

### Minor Changes

- 2129 | Fix unauthorized error when changing unit in Digital Service
- 2140 | Application view improvements (default graph view, layout updates)
- 2142 | Retain dropdown selection in navigation on results pages

---

## Installation Notes
