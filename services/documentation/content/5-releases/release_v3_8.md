---
title: 'Release v3.8'
description: 'Introducing Digital Service versioning, enabling users to create, manage, duplicate, and compare multiple versions of a Digital Service for enhanced design evaluation.'
weight: 90
---

## Overview

This release delivers one of the most impactful enhancements to G4IT: The introduction of Digital Service versions.
Users can now create, manage, duplicate, and compare multiple versions of a Digital Service to better evaluate the consequences of design decisions and improvement strategies.

### Introduction of version concept of Digital Services

A new versioning model is now implemented for Digital Services.
Users can create multiple versions of the same service to test different configurations, actions, and sustainability hypotheses — without altering the currently active state.

Key details:

A flexible and universal versioning model has been introduced to support Digital Services.
A dedicated version entity links resources and results to the corresponding Digital Service version, ensuring traceability and data separation.
No changes impact the existing Digital Service behavior; versioning is introduced on top of the current workflow.
![uc2_duplicate_digital_service_version.png](../images/uc2_duplicate_digital_service_version.png)

### Duplicating a version

To support experimentation, users can now duplicate an existing Digital Service version.

Behaviour:

Duplicate directly from the active version or via Manage Versions.
A new version is created with all resources copied from the original version.
The new version is automatically set to Draft, allowing safe modifications without affecting the source.


### Managing the versions

A dedicated Version Management interface is now available to navigate across versions of the same Digital Service.

Users can:

View all versions of a Digital Service, See version attributes (name, type, etc.), Open any version for consultation or further testing.
This makes it easier to iterate on design explorations while preserving the original or active configuration.



### Comparison of versions

Users can now compare the environmental footprint of two versions of a Digital Service to understand the impact of their changes.

Highlights:

Versions are selected via checkbox in the version list.
The Compare two versions button is enabled only when exactly two versions have been selected and both have completed impact calculations.
The comparison highlights differences across all available impact criteria.
This feature supports data-driven decision-making between design alternatives.
![uc11_compare_digital_service_versions.png](../images/uc11_compare_digital_service_versions.png)

### Update of datamodel to include import-specific rules

To help users better understand how to structure and validate input files for Digital Service import:

The Digital Service datamodel has been updated to include the specific requirements and rules for imports.
A direct link to the specification file is now available from the import side panel.
The specification describes all expected fields and formatting rules to support a smooth import experience.
![release_3_8UpdateModel.png](../images/release_3_8UpdateModel.png)


## Content

### V3.8.0

**Major Changes**

- 1604 | Introducing the version concept and DS creation
- 1605 | Duplicating a version
- 1632 | Managing the versions
- 1681 | Comparaison of versions

**Minor Changes**

- 1677 | Update the datamodel to include the specificities of Digital Service Import

## Installation Notes

