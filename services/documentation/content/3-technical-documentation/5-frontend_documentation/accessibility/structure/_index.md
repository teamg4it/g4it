---
title: "Structure"
description: "The structure guide"
date: 2023-12-28T08:20:38+01:00
weight: 90
---

For the accessibility, the structure of each page is important.

#### Structure

Some rules must be taken into account for the structure, such as :
- On each web page, information must be structured through the appropriate use of headings
- On each web page, the document structure must be consistent (except in special cases)
- On each web page, every list must be correctly structured
- On each web page, every quotation must be correctly indicated

#### Heading

The heading is used to understand the structure of the page for some tools that is used for accessibility like a reader screen.
To test the structure of the page you can use the [heading map](https://chromewebstore.google.com/detail/headingsmap/flbjommegcjonpdmenkdiocclhjacmbi) (extension Google Chrome). With this extension, you can view the order of each heading in the current page.
To change the heading, multiple options are possible : 
- if the text isn't considered as a heading, add `role="heading" aria-level="?"`, the number in aria-level is the order for the heading
- Otherwise, just add `aria-level` to specify the order

#### Global

Each page needs to have :
- `<header role="banner">`
- `<nav role="navigation">`
- `<main role="main>`
- `<footer role="contentinfo">`
