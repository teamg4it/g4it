---
title: 'Release v3.15.0'
description: 'Introduces AI Services assessment with EcoLogits, new AI server models, an improved inventory creation journey, updated cloud support, and enhanced access management.'
weight: 150
---

## Overview

Release v3.15.0 introduces major capabilities for **AI Services assessment**, including the integration of the **EcoLogits calculation engine**, AI-specific server models, and a dedicated AI Services import file.

This version also improves the **inventory creation experience** with a guided tab-based journey, updates the **Data Model to version 3.2.0**, and upgrades the **Boavizta calculation engine** to provide updated cloud provider and instance support.

In addition, access management for newly created Sopra Steria users has been refined, and a navigation issue in the EcoMind AI version comparison flow has been resolved.

Overall, this release expands G4IT's capabilities for assessing AI-related environmental impacts while improving inventory management, cloud coverage, and user experience.

---

### AI Services & EcoLogits Integration

G4IT now supports the assessment of environmental impacts associated with AI Services through the integration of the **EcoLogits calculation engine**.

#### Improvements

- Integrated EcoLogits as a dedicated calculation engine within G4IT
- Added support for AI Service impact calculations using provider, model, output tokens, and location
- Integrated AI Service impacts into inventory calculations and contribution analyses
- Added AI Services as a dedicated category in inventory results
- Added handling for indicators not provided by EcoLogits as uncalculated impact share
- Ensured EcoLogits errors do not interrupt inventory calculations and are reported in exports
- Added EcoLogits to the calculation engine information displayed in G4IT

These changes allow AI usage to be included in environmental impact assessments while maintaining existing data quality and calculation workflows.

---

### AI Server Support in Digital Services

Digital Services can now represent AI-specific infrastructure through new **AI Server** types.

#### Improvements

- Added AI Server as a new Private Infrastructure type
- Added four predefined AI server models:
    - Small AI Server
    - Medium AI Server
    - Large Old AI Server
    - Large Recent AI Server
- Added support for dedicated and shared AI Servers
- Replaced vCPU terminology with vRAM for AI Server configuration
- Added predefined technical characteristics for each AI server model
- Included AI Server impacts in Digital Service results

This provides a more accurate way to model AI infrastructure and its environmental impacts.

---

### AI Services Import

A new optional **AI Services** import file is now available when creating an inventory.

#### Improvements

- Added an AI Services template for inventory preparation
- Added validation for required AI Service fields
- Added support for provider, model, output tokens, and location
- Stored imported AI Services in a dedicated inventory data structure
- AI Services import remains optional and does not prevent inventory creation when omitted

This enables AI usage information to be included directly in inventory assessments.

---

### Inventory Creation Journey

The inventory creation process has been redesigned as a **guided tab-based journey**.

#### Improvements

- Introduced dedicated tabs for Inventory Info, Infrastructure, End-user Devices, Application Services, and External Services
- Added Next and Previous navigation between steps
- Added contextual information explaining the purpose of each section
- Added direct access to relevant templates and Data Model files
- Allowed optional data files to be prepared progressively before inventory creation
- Preserved existing validation and import processing rules

The new journey provides a clearer and more structured way to prepare inventory information.

---

### Data Model Update

The G4IT Data Model has been updated to **version 3.2.0**.

#### Improvements

- Added documentation for the AI Services import file
- Added authorized AI providers and models
- Added new AI-specific physical equipment references
- Updated cloud provider and instance reference data

---

### Boavizta Calculation Engine Upgrade

The Boavizta calculation engine has been upgraded to a newer version, providing updated cloud support and calculation capabilities.

#### Improvements

- Upgraded BoaviztaAPI from version 1.3.10 to version 2.4.1
- Added OVHcloud as a supported cloud provider
- Updated available cloud instance references
- Improved support for water-use impact data
- Updated lifecycle impact handling to remain consistent with the calculation engine methodology

---

### User Access Management

Access to the Inventory module has been refined for newly created Sopra Steria users.

#### Improvements

- New Sopra Steria users receive Digital Services access by default
- Inventory remains visible but requires explicit access
- Added a Request Access action for users without Inventory permissions
- Added a cleanup mechanism for existing users without Inventory data in their workspace

This simplifies onboarding while preserving Inventory access for users who need it.

---

### EcoMind AI Navigation

Navigation from the EcoMind AI version comparison screen has been corrected.

#### Improvement

- Users now return to the appropriate EcoMind AI page instead of being redirected to the Digital Services screen.

---

## 3.15.0

### Major Changes

- 2329 | Add new AI server types to G4IT Digital Services
- 2338 | Integrate EcoLogits Calculation Engine into G4IT
- 2335 | Add a dedicated AI Services import file
- 2339 | Calculate environmental impacts of AI Services
- 2337 | Update Data Model documentation to version 3.2.0
- 2336 | Redesign the inventory creation journey
- 2219 | Upgrade BoaviztaAPI and add updated cloud provider and instance support

### Minor Changes

- 2433 | Restrict Inventory module access by default for new Sopra Steria users
- 2445 | Fix incorrect return navigation from EcoMind AI version comparison

---

## Installation Notes

This release introduces important new capabilities for **AI Services and AI infrastructure assessment**, including the EcoLogits calculation engine and new AI-specific inventory data.

It is recommended to update the **Data Model reference files** to version 3.2.0 when preparing inventory imports. Environments using cloud calculations should also take into account the BoaviztaAPI upgrade and updated cloud provider and instance references.
