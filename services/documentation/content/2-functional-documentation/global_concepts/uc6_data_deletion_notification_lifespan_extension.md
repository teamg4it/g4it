---
title: "GUI Data Deletion Notification & Lifespan Extension"
description: "Display data deletion warning in GUI and allow users to extend data lifespan"
weight: 535
---

## Overview

This feature aims to improve user awareness and control over data retention by displaying a clear notification in the GUI, in addition to the existing email reminder.

---

## Objective

- Inform users clearly that their data will be deleted.
- Improve visibility through a GUI message (in addition to email).
- Provide a direct way to extend data retention via an existing popup.

---

## Scope

This feature applies to:
- Digital Service
- EcoMindAI
- Inventory

---

## GUI Notification

### Display Condition

The notification message is displayed when:
- The data deletion date is within the **first reminder milestone**  
  (configured as **1 month before deletion**).

### Message Content

- **EN**:  
  "Data will be deleted on [date], click on this link to extend."

- **FR**:  
  "Les données vont être supprimées le [date], cliquez sur ce lien pour les conserver."

### Behavior

- The message is **always visible** during the reminder period.
- The **[link]** redirects to the popup already implemented in version **3.11.0**.
- The popup allows users to extend the data lifespan.

---

## Email Notification Requirement

In addition to the GUI notification:

- Email alerts must be sent to **all users with write permissions**.
- This ensures all relevant users are informed and can take action.

---

## Notes

- The GUI notification complements the existing email system.
- The UI/UX design of the message can be enhanced for better visibility and usability.
- The popup behavior remains unchanged (reuse of existing implementation).

---
