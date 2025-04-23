---
title: "Table"
description: "The table guide"
date: 2023-12-28T08:20:38+01:00
weight: 100
---

In this section, you will learn what are the rule for table elements on a Web page to respect accessibility

#### Rules

- Each table needs a `<caption>` to define a title for this table and for it to be linked. This title can be hidden.
  For that, you can hide the `<caption>` in the visual page, but it can be readable by a screen reader with :

```
.visually-hidden {
    position: absolute !important;
    width: 1px !important;
    height: 1px !important;
    padding: 0 !important;
    margin: -1px !important;
    overflow: hidden !important;
    clip: rect(0, 0, 0, 0) !important;
    white-space: nowrap !important;
    border: 0 !important;
}
```

- Each title cell in a table must have a scope attribute to define whether it's a row or a column.
