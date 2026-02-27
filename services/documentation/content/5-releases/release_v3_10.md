---
title: 'Release v3.10'
description: 'New accessible and homogeneous Inventory results display, enhanced key indicators for Application view, improved data consistency configuration, and multiple usability and data quality fixes.'
weight: 120
---

## Overview

Release v3.10 introduces a major redesign of the Inventory results display, providing a more accessible, homogeneous, and user-friendly experience across modules (Digital Service, Inventory, EcoMind IA).

This version focuses on improving result readability, harmonizing the user interface, strengthening key indicators in the Application view, and resolving critical data and usability issues.

Users can now analyze Inventory impacts more intuitively, configure their view dynamically, and rely on improved country handling and corrected behaviors across the application.

### New display for Inventory results

The Inventory result view has been fully redesigned to ensure accessibility, clarity, and consistency across modules.

#### Criteria left-side panel
- Criteria are now displayed in a left-side panel, similar to the Digital Service module.
- Results can be viewed either in:
  - People equivalent, or
  - Criterion unit
- A button “Edit Criteria” allows users to easily modify selected criteria.
- The criteria section is now foldable/unfoldable, aligning the component with other modules.

#### Configure the view panel (NEW)

A new “Configure the view” button opens a left configuration panel including:

**Section 1 – Enable Data Consistency**

- Replaces the previous pop-up.
- Explains the meaning of data consistency.
- Checkbox: “I want to enable data consistency”.

**Section 2 – Units management**

- Users can select:
    - People equivalent 
    - Unit of the criterion

Buttons:

- Cancel / Annuler
- Save Settings / Enregistrer les paramètres

**Data Consistency button**

The “Data consistency” button is now:
- Displayed only if at least one indicator is in error
- Visible only when data consistency is enabled

#### Reorganized Result Layout

The Inventory result page has been restructured:
- Filters moved to the top
- New KEY INDICATORS section:
  - Infrastructure indicators
  - Energy indicators
- Main graph repositioned
- Additional graphs aligned for future improvements
- The explanation block has been temporarily removed (to be reintegrated in a future sprint)

#### Enhanced Key Indicators – Application View

Application view indicators have been reviewed and improved.

**Infrastructure indicators:**

- Quantity of Virtual Equipment (including cloud)
- Number of applications

**Energy indicators:**

- Total electricity consumption of all virtual equipment
    (new calculation: SUM(out_virtual_equipment.electricity_consumption × quantity))
- % of low impact (aligned with equipment view logic)


**Graph Improvements**

Graph navigation has been simplified and harmonized across views. In the Equipment view, the Lifecycle/Entity/Equipment/Status selector is now available via a dropdown at the top right. In the Application view, the multi-criteria visualization has been redesigned from a histogram to a radar chart. Application average impact has been removed from global and detailed graphs, while Lifecycle and Environment indicators remain unchanged


### Fix – Delete Network or Terminal

Resolved an issue where deleting Network or Terminal from the Digital Service detail page did not work.

### Country handling fix for Cloud Service and Data Centers

Improved support for countries containing special characters (, and ;).
All locations defined in the datamodel can now be successfully loaded for:


### Raw value display in camembert charts

Raw impact values are now displayed directly inside camembert charts in both the multicriteria and impact-by-tiers views. This improves version comparison, enhances screenshot readability, and makes it easier to quickly identify the highest-impact versions (not applied to the equipment semi-circle view).

### V3.10.0

**Major Changes**

- 1762 | New display for Inventory results

**Minor Changes**

- 1885 | DS : Fix for Delete Network or Terminal via the Delete button on the Edit detail page doesn't work
- 1833 | Country in error for Cloud Service and DC
- 1876 | fix the header for digital service detail page / for inventory - Equipment and Application view / for ecomindIA / for administration
- 1861 | Digital service : Add raw value into camembert


## Installation Notes

