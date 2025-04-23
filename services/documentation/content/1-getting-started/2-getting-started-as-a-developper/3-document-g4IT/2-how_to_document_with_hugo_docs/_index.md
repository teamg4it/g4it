---
title: 'Hugo syntax '
weight: 42
---

## Overview

Here you find exemple of how to use Hugo syntax and Mermaid to build documentation.
Bellow Examples of syntax to build documentation 💥

See also [Learn Theme for Hugo :: Documentation for Hugo Learn Theme](https://mcshelby.github.io/hugo-theme-relearn/)

## A picture

### External image (external)

![alt text for the logo](https://github.com/McShelby/hugo-theme-relearn/raw/main/images/hero.png)

### Image of *this* directory

This makes markdown preview works in vs code easily.

🔥 2 conditions for this to work fine:

- image in *this* directory (the page will be the name of the directory)
- this page is named ìndex.md`

![alt text for the logo](logo-overgreen.png)

## Table of contents


{{% children description="true" %}}

##  Sample mermaid

{{<mermaid align="left">}}
graph LR;
    A[Hard edge] -->|Link text| B(Round edge)
    B --> C{Decision}
    C -->|One| D[Result one]
    C -->|Two| E[Result two]
{{< /mermaid >}}

**[More About Mermaid For Hugo]({{% ref "./mermaid_sample.md" %}} "More About Mermaid for Hugo")**

## tabs shortcodes

This is a subpage with tabs

{{< tabs >}}
{{% tab title="python" %}}
```python
print("Hello World!")
```
{{% /tab %}}
{{% tab title="R" %}}
```R
> print("Hello World!")
```
{{% /tab %}}
{{% tab title="Bash" %}}
```Bash
echo "Hello World!"
```
{{% /tab %}}
{{< /tabs >}}

## Remove page from main menu

Add hidden:true in the metadata page.
