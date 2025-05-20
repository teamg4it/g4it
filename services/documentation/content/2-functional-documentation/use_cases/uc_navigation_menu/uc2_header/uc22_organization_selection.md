---
title: "5.2.2 Header-Select a workspace"
description: "Select a workspace description"
weight: 5022
---

## Table of Contents

<!-- TOC -->
  * [Table of Contents](#table-of-contents)
  * [Workspace Selection](#workspace-selection)
    * [Description](#description)
    * [Behavior Rules](#behavior-rules)
    * [Accessibility in Workspace Selection](#accessibility-in-workspace-selection)
<!-- TOC -->

## Workspace Selection

### Description

The **"Workspace Selection"** menu bar allows users to select a workspace from a list of available workspaces.

![Workspace selection menu showing available workspaces with radio buttons to select from](../images/workspace_selection.png)

### Behavior Rules

{{% expand title="Show the details" expanded="false" %}}

| Reference | Group               | Elements        | Sub-Elements | Type                  | Description                                                                                                                                |
|-----------|---------------------|-----------------|--------------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| 1         | Workspace Selection |                 |              | Menu button           | The menu button displays the text "{selected workspace} ({organization label})". Clicking this button opens the workspace selection popup. |
| 2         |                     | Your Workspaces |              | List of Radio Buttons | A list of workspaces is displayed with the text "Workspace {workspace label} ({organization label})" for each workspace.                   |

{{% /expand %}}

##  Move to another workspace from admin module

{{< mermaid >}}
graph TD;
Step1[View of the users list] --> Step2[Change Workspace from<br> Top banner]
Step2 --> Decision1{Have Admin access for<br> selected workspace?}
Decision1 -->|Yes|Step3[Stay on the<br> manage user page]
Decision1 -->|No|Step4[Navigate to Home page]
{{< /mermaid >}}


## Move to another workspace from Inventories or Digital Services Module 

{{< mermaid >}}
graph TD;
Step1[On Inventories or<br> Digital Services Module] --> Step2[Change Workspace<br> on Top banner]
Step2 --> Decision1{Have respective module<br> access for selected<br> workspace?}
Decision1 -->|Yes|Step3[Stay on the same module]
Decision1 -->|No|Step4[Navigate to Home page]
{{< /mermaid >}}

### Accessibility in Workspace Selection

The **Workspace Selection** menu is accessible via the **Tab** key. Upon clicking the menu button, a list of available
workspaces will appear. Users can navigate through the list using the **Up Arrow**/ **Left Arrow** and **Down Arrow**/ *
*Right Arrow** keys.
