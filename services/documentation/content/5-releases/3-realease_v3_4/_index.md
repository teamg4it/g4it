---
title: "Release v3.4"
weight: 50
---

## Content

### V3.4.0

### Major Changes

- 994  | Update changelog file
- 1357 | Integrate EcoMindAI in G4IT
- 1204 | Import csv files for digital services
- 1263 | Naming network entries in the Digital Service module – Network input
- 1261 | Naming user groups in the Digital Service module – Terminales input
- 740  | Visualize different cloud instances for the same type

### Minor Changes

- 1373 | Sonar fixes
- 950  | Accessibility Corrections
- 1320 | Mobile Screen Responsiveness for Top Header, Left side nav and Welcome page
- 1255 | Add BoaviztAPI link to github
- 1271 | Open Source - Be able to launch a calculation just after first installation
- 1200 | New Bug on electricity consumption : total display different than sum compute manually with export file
- 1193 | DB cleanup:Remove the link between a user and a digital service
- 1257 | Very High level of Digital Service calculated - Click in May
- 1200 | New Bug on electricity consumption : total display different than sum compute manually with export file
- 1174 | When there is one criteria evaluated, multicriteria view should be disabled in digital service module and criteria should be selected in inventory module
- 1251 | Character "%" breaks the note feature
- 1258 | Table "task", the column "created_by" is not alway valuated

## Installation Notes

### Prerequisites
1. [ ] Migration must be performed starting from version **3.2.0**.
2. [ ] You must have **super admin credentials**.
### Rename randomly generated terminal and network names

**Automatic launch**

To automatically launch the migration script, here is the step-by-step procedure to follow :

-   Login as super admin
-   Go to Administration page
-   Click on Super Admin tab
-   Click on the 'Start the release's script' button

**Manual launch**

In case you need to manually launch the scripts check this
{{% children depth="3" %}}


