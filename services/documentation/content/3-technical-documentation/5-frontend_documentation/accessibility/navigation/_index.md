---
title: "Navigation"
description: "The navigation guide"
date: 2023-12-28T08:20:38+01:00
weight: 80
---

In this section, you'll learn about the importance of navigation from an accessibility point of view.

#### Navigation

The navigation of a site is an important part to consider. For this, we need to consider the different devices that will
be used and the disabilities that users may have.

The order of headings must be planned according to the target audience (right to left and/or top to bottom).

To do this, you need to expect that some users will use the site using only their keyboard.

#### Keyboard

To navigate to the website with a keyboard, we need to view the current focus item, and we can navigate to the website
without blocking point. We need to interact and have the same functionality as the mouse with a keyboard.

##### Keyboard navigation

{{< mermaid >}}
graph LR;
Tab[Tab] --> forward[Move forward]
Shift[Shift + tab ]--> backward[Move backward]
Enter[Enter/Return] --> button[To press a button]
Spacebars[Spacebars] --> boxes[Checks or unchecks boxes]
Arrow[Arrow keys] --> navigate[navigate in a webpage]
{{< /mermaid >}}

##### Rule

- Every element that can be interacted with the mouse must be usable with both keyboard and mouse. This element must
  also be visible with focus (avoid outline:none to avoid focus, prefer tabindex=“-1”).
- It must be possible for the user to know exactly where he is on the page when navigating with the keyboard, thanks to
  the focus, and each element must be reachable.
- Avoid elements that block the user's keyboard focus on a part of the page
- Avoid html elements containing links that reload continuously, as this can cause focus traps when using the keyboard.
