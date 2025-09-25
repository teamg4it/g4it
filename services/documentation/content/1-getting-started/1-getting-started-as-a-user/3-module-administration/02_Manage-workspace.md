---
title: "How to Create, Edit, or Delete Workspaces"
description: "This guide explains how to create, edit, or delete a workspace in G4IT."
weight: 20
---

This guide explains how to create, edit, or delete a workspace (also referred to as an workspace) in G4IT.

<!-- TOC -->
  * [Concept Overview](#concept-overview)
  * [Prerequisites](#prerequisites)
  * [Step 1: Open the Administration Panel](#step-1-open-the-administration-panel)
  * [Step 2: Manage Workspaces](#step-2-manage-workspaces)
  * [Additional Resources](#additional-resources)
<!-- TOC -->

---

## Concept Overview

To manage data separation:

- Each license corresponds to an **organization**, managed by its own administrators.
- Within an organization, administrators can create and manage **workspaces**, which will soon be referred to as **workspaces** to improve clarity.

For more details on the difference between an **organization** and a **workspace**, refer to the [Glossary](../../../../4-help/glossary).

---

## Prerequisites

Before proceeding, ensure the following:

- The user has logged in to G4IT at least once.  
  See the [First Login Guide](./01_First-Login) for instructions.
- You are an administrator for the relevant **organization** or the **workspace**.

---

## Step 1: Open the Administration Panel

1. In G4IT, use the left-hand menu and click the **key icon** to open the Administration area.
2. Select the tab "Manage workspaces".
3. Select your **organization**.
4. When the organization view appears, click the **pencil icon** to enter edit mode.

![Access the administration area and select a organization](../images/03_Manage-organization-Step1.png)

---

## Step 2: Manage Workspaces

Once in edit mode, you can:
- **Create** a new workspace:  
  1. Enter a name in field. 
  2. Click **Add a workspace**.

**NOTE:** "New Workspace" button is displayed only if I am organization admin or if the organization domain equal to my domain.

- **Edit** an existing workspace:
    
    1. **Delete** a workspace by clicking the **delete icon**.
     
    2. **Rename** an existing workspace by changing the name. Remember to save by clicking on the "validation" button next to the organization selection.

> Note: Each workspace represents a separate workspace with isolated data.

> Note: After deletion, data will be stored in database for seven days, and it's possible to cancel the deletion.

![Create, rename, or delete workspaces](../images/03_Manage-organization-Step2.png)

---

## Additional Resources

For a full explanation of this feature, see the functional documentation:  
[3.1 Manage Workspaces](../../../../2-functional-documentation/use_cases/uc_administration/uc_administration_manage_workspaces/uc2_edit_workspaces/index.html)
