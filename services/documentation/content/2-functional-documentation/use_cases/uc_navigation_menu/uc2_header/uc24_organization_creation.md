---
title: '5.2.4 Header-Create a Workspace'
description: 'Create a workspace description'
weight: 5024
---

## Table of Contents

<!-- TOC -->
  * [Table of Contents](#table-of-contents)
  * [Description](#description)
  * [State Diagram](#state-diagram)
  * [Create New Organization Button](#create-new-organization-button)
  * [Create a New Organization (Right Panel)](#create-a-new-organization-right-panel)
  * [Mockup](#mockup)
    * [Organization Name Duplicate Error](#organization-name-duplicate-error)
    * [Organization Name Space Error](#organization-name-space-error)
  * [Accessibility in Create New Organization (Right Panel)](#accessibility-in-create-new-organization-right-panel)
<!-- TOC -->

## Description

As a G4IT user (ROLE_DIGITAL_SERVICE_READ, ROLE_INVENTORY_READ), when accessing the list of organizations, users will
see a new button to create their own workspace (new organization).

## State Diagram

![State diagram illustrating the process of creating a new organization](../images/organization_creation_state_diagram.png)

## Create New Organization Button

The "Create New Organization" button is located in the organization list menu at the top header.

![Create new organization button located in the organization list menu at the top header](../images/create_organization_button.png)

## Create a New Organization (Right Panel)

Upon clicking the 'Create' button, a right panel will appear with a form to be filled out. The form contains two
fields: "Choose an Organization" and "Set a Workspace Name."  
The right panel displays the "Choose an Organization" menu only when more than one organization is available to select.
The "Next" button will remain disabled until the organization field is selected, and the "Create New Workspace" button
will stay disabled until all mandatory fields are filled.

If only one organization is available, the left panel will not be displayed, and that organization will be
auto-selected. Only the "Set a Workspace Name" field will appear.

## Mockup

![The form for creating a new organization, with options for selecting an organization and setting a workspace name](../images/create_organization_form_id.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements  | Sub-Elements | Type   | Description                                                                                                                                                                                                                                                            |
|-----------|-------------|-----------|--------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Left Panel  |           |              | Group  |                                                                                                                                                                                                                                                                        |
| 1         |             | Menu Item |              | Label  | The menu contains two options: "Choose an Organization" and "Set a Workspace Name." The left panel is only displayed when multiple organizations are available. The "Set a Workspace Name" option will be disabled until the "Choose an Organization" field is filled. |
|           | Right Panel |           |              | Group  |                                                                                                                                                                                                                                                                        |
| 2         |             | Form      |              |        | If only one organization is available, the form will only display the "Set a Workspace Name" field.                                                                                                                                                                    |
| 3         |             |           |              | Button | Clicking this button clears the form data and closes the right panel.                                                                                                                                                                                                  |
| 4         |             |           |              | Button | The "Create Workspace" button triggers the API call to create a new workspace. Once successful, the page automatically redirects to the new workspace. This button will be enabled only if the form is valid.                                                          |
| 5         |             |           |              | Button | The "Next" or "Previous" buttons are displayed based on the form's requirements. These buttons are enabled only when the respective field is valid.                                                                                                                    |
| 6         |             |           |              | Button | The "Close" button closes the right panel.                                                                                                                                                                                                                             |

{{% /expand %}}

### Organization Name Duplicate Error

![Error message indicating that the organization name is duplicated](../images/create_organization_name_duplicate.png)

### Organization Name Space Error

![Error message indicating that the organization name has invalid spaces](../images/create_organization_name_space.png)

## Accessibility in Create New Organization (Right Panel)

Upon initialization, focus will be placed automatically on the first form field.  
On the left menu panel, Arrow Up/Left and Arrow Down/Right can be used to navigate between the menu items.
