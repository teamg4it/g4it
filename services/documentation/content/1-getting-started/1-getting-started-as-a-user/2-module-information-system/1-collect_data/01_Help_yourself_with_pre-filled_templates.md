---
title: "Help with pre-filled templates"
description: "This section gives you pre-filled templates to create inventory in Information System module"
weight: 40
---
<!-- AI_DISCLAIMER v1.0 -->
> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Datacenter pre-filled template

| Templates                                                     | Input                                                  |
|---------------------------------------------------------------|--------------------------------------------------------|
| [Datacenter](../../documents/datacenter_sampleMandatory1.csv) | nomCourtDatacenter;nomLongDatacenter;pue;localisation  |

## Physical equipment pre-filled template

| Template                                                        | Input                                                         |
|-----------------------------------------------------------------|---------------------------------------------------------------|
| [Laptop](../../documents/physicalEquipment_example.csv)         | Serial number; Quantity; type; status; Laptop usage country   |
| [Laptop-1](../../documents/User_Devices_01_Laptop_Template.csv) | Serial number; Model; Purchase date; Laptop usage country     |

Both physical-equipment CSV samples include optional `contexteUsage` and
`fractionUsage` columns, initially empty. Leave them empty to retain full
allocation, or provide an exact `BYOD`/`COPE` context and/or a fraction between
0 and 1 for eligible inventory Devices. These values do not change the source
quantity. See [loading and validation rules]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc3_load_files.md" %}})
before filling them in. Existing spreadsheet downloads are not updated by this
CSV change.

## Virtual equipment pre-filled template

| Template                                                                      | Input                                                                                     |
|-------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| [Virtual Equipments](../../documents/virtualEquipment_Sample.csv)             | Instance name; Provider; Name of the size; Location Country; Annual usage time calculated |
| [Cloud Instances](../../documents/User_Server_01_InstancesCloud_Template.csv) | Instance name; Provider; Name of the size; Location Country; Annual usage time calculated |

## Application pre-filled template

| Templates                                                | Input                                                                                                         |
|----------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| [Application](../../documents/application_Sample.csv)    | nomApplication;typeEnvironnement;nomEquipementVirtuel;nomSourceDonneeEquipementVirtuel;domaine;sousDomaine;   |
