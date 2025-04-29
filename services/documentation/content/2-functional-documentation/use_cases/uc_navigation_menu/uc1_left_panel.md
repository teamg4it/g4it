---
title: '5.1. Left Panel'
description: "Description of the left panel"
weight: 501
---
## Overview

The **Left Panel** is a navigation menu that provides access to the main sections of G4IT:

- **Inventories**
- **Digital Services**
- **Administrator**

It is always visible on the left side of the screen to facilitate consistent navigation across the application.

![Screenshot of the left navigation panel showing links to Inventories, Digital Services, and Administrator modules](../images/left_panel.png)

## Behavior Rules

{{% expand title="Show details" expanded="false" %}}

| Ref | Group      | Element           | Sub-Element | Type  | Description                                                                                                                                                                           |
|-----|------------|-------------------|-------------|-------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1   | Left Panel |                   |             | Panel | The left panel is a permanent navigation component that helps users access different G4IT modules efficiently.                                                                        |
| 2   |            | Home Icon         |             | Link  | Clicking this icon takes the user to the **Inventories** page. It is accessible to all users and is placed at the top for quick access.                                               |
| 3   |            | Inventories       |             | Link  | Visible only if the user has the **read** permission for inventories. Clicking it takes the user to the **Inventories** page. The active page is highlighted with a green background. |
| 4   |            | Digital Services  |             | Link  | Visible only if the user has the **read** permission for digital services. Clicking it redirects to the **Digital Services** page.                                                    |
| 5   |            | Administrator     |             | Link  | Visible only to users with the **admin** role. If the user lacks this role, the link is hidden. Clicking it takes the user to the **Administrator** module.                           |
| 6   |            | Sopra Steria Icon |             | Icon  | This is a non-interactive image displaying the **Sopra Steria** logo. It is decorative only and should be marked as such for screen readers (`alt=""`).                               |

{{% /expand %}}
