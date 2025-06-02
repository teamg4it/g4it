---
title: '5.2.1 Header-About G4IT'
description: "About G4IT Description"
weight: 5021
---

## Table of Contents

<!-- TOC -->
  * [Table of Contents](#table-of-contents)
  * [Focus on the Main Content Button](#focus-on-the-main-content-button)
  * [About G4IT](#about-g4it)
    * [Description](#description)
    * [Behavior Rules](#behavior-rules)
    * [Accessibility](#accessibility)
  * [Useful Information](#useful-information)
    * [Description](#description-1)
      * [Sequence Diagram](#sequence-diagram)
<!-- TOC -->

---

## Focus on the Main Content Button

A hidden skip link button is located in the top header.  
It becomes visible when navigating via **keyboard (Tab key)**.  
When focused and activated, it redirects the focus to the main content area of the page — improving keyboard
accessibility for all users.

---

## About G4IT

### Description

The **About G4IT** menu provides access to helpful resources such as support, documentation, and business information.  
It helps users find key information about the platform.

![Screenshot of the About G4IT menu showing Useful Information and Help Center links](../images/about_g4it.png)

---

### Behavior Rules

{{% expand title="Show details" expanded="false" %}}

| Ref | Group      | Element            | Sub-Element | Type           | Description                                                                                                                |
|-----|------------|--------------------|-------------|----------------|----------------------------------------------------------------------------------------------------------------------------|
| 1   | About G4IT |                    |             | Menu           | The "About G4IT" menu groups links to documentation, support, and business-related details.                                |
| 2   |            | Useful Information |             | Link           | Links to a page containing platform versions, business hours, and contact details for support.                             |
| 3   |            | G4IT declarations  |             | Link           | Links to a page containing Eco-design Declaration and Accessibility Declaration |
| 4   |            | Help Center        |             | External Links | Opens two external resources in a new tab: the G4IT GitHub repository and official documentation site for additional help. |

{{% /expand %}}

---

### Accessibility

- The **"About G4IT"** menu is fully accessible via keyboard.
- Use **Tab** to focus the menu.
- Use **Arrow Up** and **Arrow Down** to navigate within the menu list.
- All interactive elements are reachable and operable without a mouse.

---

## Useful Information

### Description

To check the platform's availability and support information:

1. Open the **About G4IT** menu and click the **Useful Information** link.
2. The **Business Hours** section provides the platform's operating hours.
3. The **Support Contact** section allows you to easily contact G4IT support.

---

#### Sequence Diagram

Illustrates how G4IT retrieves business hours from the backend when accessing the "Useful Information" page.

```mermaid
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End

RND ->> front: Access G4IT > About Us > Useful Information
front ->> back: GET /api/business-hours
back -->> front: Return data from business_hours table
