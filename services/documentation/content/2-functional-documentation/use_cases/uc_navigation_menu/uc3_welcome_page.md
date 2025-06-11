---
title: '5.3 Welcome page'
description: 'Welcome page'
weight: 503
---

## Table of Contents

<!-- TOC -->
  * [Table of Contents](#table-of-contents)
  * [Description](#description)
  * [Mockup](#mockup)
  * [Accessibility](#accessibility)
<!-- TOC -->

## Description

As a G4IT user, I want to access to a welcome page when I access to G4IT. In order to understand the different part of G4IT and create easily a new organization.

## Mockup

In Welcome page, it has "Create new Organization" button is available. And under Module access we have two cards such as "Inventories" and "Digital Services" both with description and navigation button.

A new button (Home button) is available in the left pannel when we access to the page.

![Welcome page visualization](../images/welcome_page.png)

As a projet team member:
When I don't have access to the inventories module associated to the selected organization
Then the icon of the module is in grey and the button is disabled.

![Welcome page visualization](../images/welcome_page_disabled_inventories.png)

As a sustainable IT leader:
When I don't have access to the digital service module associated to the selected organization
Then the icon of the module is in grey and the button is disabled.

![Welcome page visualization](../images/welcome_page_disabled_digital_services.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group       | Elements  | Sub-Elements | Type   | Description                                                                                                                                                                                                                                                            |
|-----------|-------------|-----------|--------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1         |             | G4IT logo |              | Link  | As a G4IT user, When i work in the different module and click on this G4IT logo, Then I'm redirected to the welcome page. |
| 2         |             | Menu Item |              | Link  | As a G4IT user, When i work in the different module and click on this "Home Page" button in the left pannel, Then I'm redirected to the welcome page. |
| 3         |             | Create New Workspace |              | Button | Clicking this button will open right sidebar with form to create new workspace. |
| 4         | Module Access | Inventories |              | Card | As a projet team member: When I don't have access to the inventories module associated to the selected organization, Then the icon of the module is in grey and the button is disabled. On clicking this button, it navigates to Inventories module. |
| 5         | Module Access | Digital Services |         | Card | As a sustainable IT leader: When I don't have access to the digital  service module associated to the selected organization, Then the icon of the module is in grey and the button is disabled. On clicking this button, it navigates to Digital Services module. |
| 6         | Footer | Useful Information |              | Link | On clicking this link, it navigates to "Useful information" page. |
| 7         | Footer | G4IT Declarations |              | Link | On clicking this link, it navigates to "G4IT Declarations" page.  |
| 7         | Footer | Help Center |              | Link | Here, we have two links like G4IT github and G4IT documentation, clicking on these link, it will navigate to respective pages. |

{{% /expand %}}

## Accessibility

On Tabulation, focusing into first focusable element i.e, "Create New Organization". Then "Inventories" and "Digital Services" if these buttons are enabled. And finally into footer parts like "Useful information", "G4IT Github" and "G4IT documentation".
