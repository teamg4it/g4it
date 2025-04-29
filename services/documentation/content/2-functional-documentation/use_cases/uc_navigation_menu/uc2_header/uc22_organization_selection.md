---
title: "5.2.2 Header - Select and create an workspace"
description: ""
weight: 5022
---

## Table of contents

-   [Workspace selection](#workspace-selection)
-   [Workspace creation](#workspace-creation)

## Workspace selection

### Description

The "Workspace Selection" menu bar provides feature for users to select the Workspace. There is list of workspace from which user can select any workspace.

![workspace_selection.png](../images/workspace_selection.png)

## Behavior Rules

{{% expand title="Show the detail" expanded="false" %}}

| Reference | Group               | Elements        | Sub-Elements | Type                  | Description                                                                                                                                                  |
| --------- | ------------------- | --------------- | ------------ | --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1         | Workspace Selection |                 |              | Menu button           | It is menu button of header which have "{selected workspace} ({organization label})" text on button. On click of button workspace selection popup will open. |
| 2         |                     | Your Workspaces |              | List of Radio Buttons | This section displays the workspace list displayed with text - "Workspace {workspace label} ({organization label})"                                          |

{{% /expand %}}


## Accessibility in Workspace selection

Workspace selection menu can be accessed using tab button. On click of menu button a menu list will open 
To navigating between menu list Arrow up/Arrow left and Arrow down/Arrow right can be used.

## Workspace creation

## Table of contents

-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Create new Organization button](#create-new-organization-button)
-   [Mockup](#mockup)

## Description

As a G4IT user (ROLE_DIGITAL_SERVICE_READ, ROLE_INVENTORY_READ), When I access to the list of organizations. A new button is available to create its own space (create a new organization)

## State Diagram

![organization_creation_state_diagram.PNG](../images/organization_creation_state_diagram.PNG)

## Create new Organization button

Create new organization button located in organization list menu from the top header.

![create_organization_button.PNG](../images/create_organization_button.PNG)

## Create new Organization (Right panel)

When triggering Create button, it will display right panel with form to fill. The form has two menu like "Choose an Organization" and "Set a workspace name". It has divided into left and right panel. Left panel will displayed only when more than one Organization available to choose (as shown below screenshot).

Note that "Next" button will remains diabled until Organization selected (mandatory field), also unable to navigate via left panel menu. "Create new workspace" button will remains disabled until all mandatory fields filed in the form.

If there is only one Organization, then left panel will not displayed and auto selected with that available Organization. And displayed only one menu "Set a workspace name" form (as shown below screenshot).

## Mockup

![create_organization_form_id.PNG](../images/create_organization_form_id.PNG)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements  | Sub-Elements | Type   | Description                                                                                                                                                                                                                                                                                                                                   |
| --------- | ----------- | --------- | ------------ | ------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Left panel  |           |              | Group  |                                                                                                                                                                                                                                                                                                                                               |
| 1         |             | Menu item |              | Label  | It has two menu like "Choose an Organization" and "Set a workspace name". Left panel will displayed only when more than one Organization available to choose. 2nd menu "Set a workspace name" will be disabled until "Choose an organization" option filled. Once respective field filled and valid, then "Completed" label will be appended. |
|           | Right panel |           |              | Group  |                                                                                                                                                                                                                                                                                                                                               |
| 2         |             | Form      |              |        | When only one organization available, then directly "Set a workspace name" field only displayed.                                                                                                                                                                                                                                              |
| 3         |             |           |              | Button | Click on this button, it will clear the form data and close the Right panel.                                                                                                                                                                                                                                                                  |
| 4         |             |           |              | Button | Create Workspace will trigger the create api call. Once successfully created workspace, then page automatically redirected to new workspace. This button will be enabled only form is valid.                                                                                                                                                  |
| 5         |             |           |              | Button | Next or Previous button will be displayed based on the form availability. It will be enabled based on respective field is valid.                                                                                                                                                                                                              |
| 6         |             |           |              | Button | Close button will be close the Right panel.                                                                                                                                                                                                                                                                                                   |

{{% /expand %}}

![create_organization_menu1_selected.PNG](../images/create_organization_menu1_selected.PNG)

![create_organization_menu2_selected.PNG](../images/create_organization_menu2_selected.PNG)

### Organization name duplicate error

![create_organization_name_duplicate.PNG](../images/create_organization_name_duplicate.PNG)

### Organization name space error

![create_organization_name_space.PNG](../images/create_organization_name_space.PNG)

## Accessibility in Create new Organization (Right panel)

On initialize, first focus will be on form field which is displayed.
On left menu panel, Arrow up/Arrow left and Arrow down/Arrow right can be used for navigating between menu list.
