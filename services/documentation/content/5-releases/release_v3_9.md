---
title: 'Release v3.9'
description: 'Strengthening Digital Service version lifecycle management, improving accessibility and external user experience, and introducing the new Water use environmental impact criterion.'
weight: 100
---

## Overview

Release v3.9 builds on the Digital Service versioning foundation introduced in v3.8.
This version focuses on version lifecycle control, broader data accessibility, improved usability for external users, and the introduction of a new environmental impact criterion: Water use.
Users can now better manage the evolution of their Digital Services, compare and promote versions with confidence, and access richer sustainability insights across modules.

### Promoting a version as the new current

Users can now promote a Draft version to become the new current version of a Digital Service.

Key details:

A new “Promote” action is available in the Version Management screen.
Only versions in Draft status can be promoted.
Promotion allows users to track progress and define a new reference snapshot without recreating the service.

![release_v3_9_promoteVersion.png](../images/release_v3_9_promoteVersion.png)

### Deleting a Digital Service version

To keep version lists clean and relevant, users can now delete versions they no longer need.

Behaviour:

The current (active) version cannot be deleted.
Only non-current versions can be removed using the Delete action.
A confirmation popup is displayed before deletion.
An additional warning is shown when deleting a Past version.

### Unified banner for non-authenticated users

External users accessing a shared Digital Service now experience a consistent application banner aligned with the connected-user experience. The left sidebar displays only the Sopra Steria logo, while the top banner includes only the About G4IT menu. Informational links (Useful Information, G4IT Declaration) open in a new tab for non-authenticated mode. In addition, the Useful Information page has been simplified with a new introduction and a list of modules available in G4IT.

![release_v3_9_non-authroize.png](../images/release_v3_9_non-authroize.png)

### Introduction of the Water use impact criterion

A new Water use environmental indicator is now available to assess the footprint of Digital Services, Information Systems, and EcoMind IA services. It is fully integrated into criteria selection, impact graphs, and descriptions, alongside existing water-related criteria. Water use can be configured at organization, workspace, and inventory levels, and is included in data exports. The indicator is also visible in non-authenticated (shared) mode.

### Export access for all connected users

Users having READ Role can export the data

![release_v3_9_export.png](../images/release_v3_9_export.png)

## Content

### V3.9.0

**Major Changes**

- 1606 | Making a version the new current
- 1607 | Deleting a Digital Service Version
- 1757 | Unified banner for non-authenticated users
- 1769 | Add Water use criteria

**Minor Changes**

- 1768 | Duplicate checks on Digital Service and Version
- 1777 | Access to Manage Versions for read role
- 1778 | Direct link to open version from comparison page
- 1756 | Export enabled for all connected users
- 1715 | Accessible text description with links to impact details

## Installation Notes

