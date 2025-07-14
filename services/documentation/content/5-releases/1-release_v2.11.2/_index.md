---
title: 'Release v2.11.2'
weight: 10
---

## Overview

Version 2.11.2 of G4IT prepares the migration to a new architecture that simplifies the data model and improves performance.
In this version, we have introduced the migration process for digital services and inventories to the new architecture.

## Content

### V2.11.2
Major Changes

840 | Add a control on new architecture to verify if the physical or the virtual equipment is consistent
822 | Allow G4IT to accept different files type to load data
669 | New Arch - Migration code to copy inventory/ds data into the new mode and the rollback (Fix of 2.10)

Minor Changes

951 | Digital Service migration issue
948 | Error occured while migration
936 | Differences between visualization on application view in new architecture
944 | Control on the value of typeInfrastructure in virtualEquipment.csv
883 | Update the documentation based on the new Architecture
921 | Super admin functionality
882 | VMs are not deleted for the new architecture
900 | In digital services remove hover content visible
827 | In digital services Non cloud server values mismatching in old and new Arch
844 | Inventory file should not be part of export
820 | Allow to have decimal in annual electricity consumption
868 | In DS and IS set new Architecture as default
812 | Data model documentation improvement

## Installation Notes

Migrate data to new architecture: [here](1-migrate_new_arch)
