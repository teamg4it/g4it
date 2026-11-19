---
title: 'Release v3.15.1'
description: 'Improves Application View performance and scalability through API migration, introduces new AI server references and release information at login, and delivers usability improvements and bug fixes across G4IT.'
weight: 155
---

## Overview

Release v3.15.1 focuses on improving the **performance and scalability of the Application View** through an API migration designed to handle large inventories more efficiently.

The release also introduces additional **AI server references** and a new way to keep users informed about product updates through release information displayed at login.

Several corrections and usability improvements are also included across G4IT, covering workspace management, Digital Service deletion, virtual equipment indicators and power consumption calculations. The default retention period for generated export files is extended to **180 days**.

---

### Application View API Migration & Performance Optimization

The Application View APIs have been redesigned to improve performance and scalability when handling large inventories.

#### Improvements

- Introduced dedicated APIs for multi-criteria impact retrieval and aggregation.
- Optimized data retrieval for application charts and indicators.
- Added drill-down support across domains, subdomains, applications, and virtual equipment.
- Introduced pagination for application indicator tables.
- Improved API response efficiency for large datasets.

These changes reduce unnecessary data transfers and improve the responsiveness of the Application View.

---

### New AI Server References

G4IT now includes two additional AI server models, making them available wherever the AI server catalog is used.

#### Improvements

- Added **Server-ai-4-L40S** and **Server-ai-8-B300** to the platform equipment references.
- Added the associated environmental impact factors.
- Made the new models available in Inventory equipment selection and Digital Services private infrastructure.
- Added predefined technical characteristics for each model.
- Updated the Data Model documentation to version **3.2.1**.

These additions allow users to model and assess a wider range of AI server configurations.

---

### Release Information at First Login

A new release information modal helps users discover the latest G4IT updates when they log in.

#### Improvements

- Added an administration section for environment super administrators to edit and publish release information.
- Displayed the published release information to users at login.
- Allowed users to dismiss the information for the current release.
- Redisplayed the modal when the release content is updated.
- Added a feedback action so users can share comments and suggestions about the release.

This provides a direct way to communicate new features, improvements, and fixes to users.

---

### Workspace Management

- Fixed a recursion issue that could freeze the interface when loading workspaces for organisations containing large amounts of data.

---

### Digital Service Deletion

- Fixed an issue that could leave Digital Service deletion indefinitely loading without completing or displaying a result.

---

### Virtual Equipment Indicators and Power Consumption

- Corrected the **Quantity of Virtual Equipment** indicator to use the sum of declared virtual equipment quantities.
- Updated the **Total Power Consumption** calculation to take virtual equipment quantity into account when allocating host electricity consumption.

---

### Export File Retention

The default retention period for generated Inventory and Digital Services export files has been extended to **180 days (6 months)**.

#### Improvements

- Set the default retention period to 180 days for new workspaces and subscribers.
- Updated existing retention settings below 180 days to the new default.
- Kept existing settings unchanged when they are already 180 days or longer.

This gives users more time to retrieve previously generated exports and reduces the need to relaunch exports.

---

This release updates the AI server reference data and the Data Model documentation to version **3.2.1**. Ensure that the updated reference data and Data Model are deployed together so that the new AI server models are available for equipment selection and environmental impact calculations.

The default export retention period is now 180 days. Existing workspace and subscriber settings below this threshold are updated as part of the deployment.

---

## 3.15.1

### Major Changes

- 04 | API Migration – Application View
- 56 | Add two new AI server references to G4IT
- 44 | Display release information at first login after a new release

### Minor Changes

- 128 | Fix recursion error in workspace management
- 123 | Fix Digital Service deletion that could remain stuck
- 20 | Correct the Quantity of Virtual Equipment key indicator
- 19 | Include virtual equipment quantity in total power consumption
- 22 | Extend export file retention to 180 days

---

## Installation Notes
