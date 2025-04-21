---
title: "2- Creating, Editing, or Deleting Workspaces (Organizations)"
description: "This guide explains how to create, edit, or delete a workspace (also referred to as an organization) in G4IT."
weight: 20
---

This guide explains how to create, edit, or delete a workspace (also referred to as an organization) in G4IT.

<!-- TOC -->
  * [Concept Overview](#concept-overview)
  * [Prerequisites](#prerequisites)
  * [Step 1: Open the Administration Panel](#step-1-open-the-administration-panel)
  * [Step 2: Manage Workspaces (Organizations)](#step-2-manage-workspaces-organizations)
  * [Additional Resources](#additional-resources)
<!-- TOC -->

---

## Concept Overview

G4IT is used by Sopra Steria collaborators and licensed external customers (e.g., ADEO, ArcelorMittal). Each customer holds a yearly license.

To manage licenses and data separation:

- Each license corresponds to a **subscriber**, managed by its own administrators.
- Within a subscriber, administrators can create and manage **organizations**, which will soon be referred to as **workspaces** to improve clarity.

For more details on the difference between a **subscriber** and an **organization**, refer to the [Glossary](../../../../4-help/glossary).

---

## Prerequisites

Before proceeding, ensure the following:

- The user has logged in to G4IT at least once.  
  See the [First Login Guide](../../01_First-Login) for instructions.
- You are an administrator for the relevant **subscriber**.

---

## Step 1: Open the Administration Panel

1. In G4IT, use the left-hand menu and click the **key icon** to open the Administration area.
2. Select the tab "Manage organizations".
3. Select your **subscriber**.
4. When the subscriber view appears, click the **pencil icon** to enter edit mode.

![Access the administration area and select a subscriber](../images/03_Manage-organization-Step1.png)

---

## Step 2: Manage Workspaces (Organizations)

Once in edit mode, you can:

- **Create** a new organization (workspace):  
  1. Enter a name in field. 
  2. Click **Add an organization**.

- **Edit** an existing organization (workspace):
    
    3. **Delete** an organization by clicking the **delete icon**.
     
    4. **Rename** an existing organization by changing the name. Remember to save by clicking on the "validation" button next to the subscriber selection.

> Note: Each organization represents a separate workspace with isolated data.

> Note: After deletion, data will be stored in database for seven days and it's possible to cancel the deletion.

![Create, rename, or delete organizations](../images/03_Manage-organization-Step2.png)

---

## Additional Resources

For a full explanation of this feature, see the functional documentation:  
[3.1 Manage Organizations](../../../../2-functional-documentation/use_cases/uc_administration/uc_administration_manage_organizations/uc2_edit_organizations/index.html)
