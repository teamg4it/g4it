---
title: '5.2.4 Header-Create or search a Workspace'
description: 'Create or search a workspace description'
weight: 5024
---

## Table of Contents

<!-- TOC -->
  * [Table of Contents](#table-of-contents)
  * [Description](#description)
  * [State Diagram](#state-diagram)
  * [Create New Workspace or search a workspace Button](#create-new-workspace-or-search-a-workspace-button)
  * [Create a New Workspace (Right Panel)](#create-a-new-workspace-right-panel)
  * [Mockup](#mockup)
    * [Workspace Name Duplicate Error](#workspace-name-duplicate-error)
    * [Workspace Name Space Error](#workspace-name-space-error)
  * [Accessibility in Create New Workspace (Right Panel)](#accessibility-in-create-new-workspace-right-panel)
<!-- TOC -->

## Description

As a G4IT user (ROLE_DIGITAL_SERVICE_READ, ROLE_INVENTORY_READ), when accessing the list of workspaces, users will
see a new button to create their own workspace.

To allow users to search for a workspace by name, a text input field is provided that supports case-insensitive searching.
## State Diagram

![State diagram illustrating the process of creating a new workspace](../images/organization_creation_state_diagram.png)

## Create New Workspace or Search a Workspace 

The "Create New Workspace" search a workspace" buttons are located in the workspace list menu at the top header.

![Create new workspace button located in the workspace list menu at the top header](../images/create_organization_button.png)

## Create a New Workspace (Right Panel)

Upon clicking the 'Create' button, a right panel will appear with a form to be filled out. The form contains two
fields: "Choose a Workspace" and "Set a Workspace Name."  
The right panel displays the "Choose a Workspace" menu only when more than one workspace is available to select.
The "Next" button will remain disabled until the workspace field is selected, and the "Create New Workspace" button
will stay disabled until all mandatory fields are filled.

If only one workspace is available, the left panel will not be displayed, and that workspace will be
auto-selected. Only the "Set a Workspace Name" field will appear.

## Mockup

![The form for creating a new workspace, with options for selecting a workspace and setting a workspace name](../images/create_organization_form_id.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements  | Sub-Elements | Type   | Description                                                                                                                                                                                                                                                 |
|-----------|-------------|-----------|--------------|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Left Panel  |           |              | Group  |                                                                                                                                                                                                                                                             |
| 1         |             | Menu Item |              | Label  | The menu contains two options: "Choose a Workspace" and "Set a Workspace Name." The left panel is only displayed when multiple workspaces are available. The "Set a Workspace Name" option will be disabled until the "Choose a Workspace" field is filled. |
|           | Right Panel |           |              | Group  |                                                                                                                                                                                                                                                             |
| 2         |             | Form      |              |        | If only one workspace is available, the form will only display the "Set a Workspace Name" field.                                                                                                                                                            |
| 3         |             |           |              | Button | Clicking this button clears the form data and closes the right panel.                                                                                                                                                                                       |
| 4         |             |           |              | Button | The "Create Workspace" button triggers the API call to create a new workspace. Once successful, the page automatically redirects to the new workspace. This button will be enabled only if the form is valid.                                               |
| 5         |             |           |              | Button | The "Next" or "Previous" buttons are displayed based on the form's requirements. These buttons are enabled only when the respective field is valid.                                                                                                         |
| 6         |             |           |              | Button | The "Close" button closes the right panel.                                                                                                                                                                                                                  |

{{% /expand %}}

### Workspace Name Duplicate Error

![Error message indicating that the workspace name is duplicated](../images/create_organization_name_duplicate.png)

### Workspace Name Space Error

![Error message indicating that the workspace name has invalid spaces](../images/create_organization_name_space.png)

## Accessibility in Create New Workspace (Right Panel)

Upon initialization, focus will be placed automatically on the first form field.  
On the left menu panel, Arrow Up/Left and Arrow Down/Right can be used to navigate between the menu items.
