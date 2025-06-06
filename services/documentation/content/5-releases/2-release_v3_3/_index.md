---
title: "Release v3.3"
weight: 40
---

## Prerequisites

1. [ ] Migration must be performed starting from version **3.2.0**.
2. [ ] You must have **super admin credentials**.

## Overview

G4IT v3.3.0 prepares for two major changes:

1. The **_migration_** of digital services from the "DEMO" workspace to a **_new workspace_** named "DEMO - {Creator LastName} - #{Number}" (e.g., DEMO-Laigneau-#1). The number increases to prevent duplicates. The digital service creator creator receives "ROLE_ADMIN_ORGANIZATION" right on the new workspace, while shared users get "ROLE_DIGITAL_SERVICES_WRITE" access only. \
   <br> The goal is to have a clean "DEMO" workspace.

2. On a user's **_first connection_** to G4IT, grant default access to the "DEMO" organization with "ROLE_INVENTORY_READ" and "ROLE_DIGITAL_SERVICE_READ" permissions (previously included "ROLE_DIGITAL_SERVICE_WRITE") when the organization is known.
   "ROLE_DIGITAL_SERVICE_WRITE" from all existing non-admin users in the DEMO organization for each subscriber to be removed. \

## Content

### V3.3.0

**Major Changes**

-   1079 | [BE]: Migrate digital services from "Demo" workspace to new workspace
-   1001 | Modify the first connection workflow
-   1149 | Visualize ecodesign & accessibility declaration
-   1003 | Remove the link between a user and a digital service

**Minor Changes**

-   1166 | [BE]: Allow G4IT to accept a copy/paste of model with special character from DataModel
-   1189 | CalculImpactDSI Inventories have been deleted in Prod
-   1205 | [BE] Error in administation page when changing criteria
-   797 | Lag on the filter when there is a lot of data
-   1218 | Issue on the DS export feature
-   1246 | Inventory : Export available from Demo Workspace

## Installation Notes

### Digital services migration from DEMO workspace to new workspaces

**Automatic launch**

To automatically launch the migration script, here is the step-by-step procedure to follow :

-   Login as super admin
-   Go to Administration page
-   Click on Super Admin tab
-   Click on the 'Start the release's script' button

**Manual launch**

In case you need to manually launch the scripts check this : [digital service migration](1-ds_migration) and then [remove digital service write access](2-remove-write-access-demo)
