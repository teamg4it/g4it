---
title: "Testing"
description: "how to test accessibility"
date: 2023-12-28T08:20:38+01:00
weight: 40
---

In this section, to test the accessibility of a website, several test steps must be carried out

#### Test steps

- Test the navigation with a keyboard

- Test with a screen reader like NVD Access

- Add “semi-automated” testing with tools like Wave

- Add fully automated testing with tools like AXE Core

- Test with real humans

#### Tools

- Application [NV Access](https://www.nvaccess.org/download/) (screen reader)

- [Axe-core](https://www.npmjs.com/package/axe-core) (automatic testing)

#### Chrome extension :

- [Web disability Simulator](https://chromewebstore.google.com/detail/web-disability-simulator/olioanlbgbpmdlgjnnampnnlohigkjla) (Chrome extension): Simulates different types of disability.

- [Wave](https://chromewebstore.google.com/detail/wave-evaluation-tool/jbbplnpkjmmeebjpijfedlgcdilocofh) (extension): Displays errors identified on a page (e.g.: an image has no description) and gives recommendations for modification.

- [Let's get color blind](https://chromewebstore.google.com/detail/lets-get-color-blind/bkdgdianpkfahpkmphgehigalpighjck) (extension): Simulates different color-related disabilities 

#### Keyboard accessibility testing

{{< mermaid >}}
graph LR;
Tab[Tab] --> forward[Move forward]
Shift[Shift + tab ]--> backward[Move backward]
Enter[Enter/Return] --> button[To press a button]
Spacebars[Spacebars] --> boxes[Checks or unchecks boxes]
Arrow[Arrow keys] --> navigate[navigate in a webpage]
{{< /mermaid >}}
