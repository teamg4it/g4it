---
title: 'Data consistency'
description: "Explanations and generalities about data consistency"
weight: 300
---

## Table of contents

<!-- TOC -->
  * [Table of contents](#table-of-contents)
  * [Description](#description)
    * [Formula to explain the calculation](#formula-to-explain-the-calculation)
  * [Global view](#global-view)
<!-- TOC -->

## Description

As we have different calculation engine used in G4IT (NumEcoEval and BoaviztAPI), it's important to know what we measure to compare things that can be compared.

G4IT needs to be clear about the perimeter evaluated and display the limit of each engine. For different reasons, it may happen that we are unable to produce indicators for one specific criterion or lifecycle step and that impacts the result. G4IT will give a solid evaluation but needs to be clear about the evaluation to compare effectively different items.

G4IT needs to display that it wasn't able to produce indicators, but that doesn't mean that there is no impact, only that there is a part of the impact that couldn't be evaluated (mostly for impact data reason).

Type of error that can happen:
* We did an evaluation regarding the criterion "Acidification" but BoaviztAPI doesn't have data about this criterion.
* Lifespan data wasn't provided
* Data is missing in the referential
* Other technical reasons

As described in the following diagram, data consistency is available in different use cases. See these pages to have more information :
* [1. My IS inventory/1.5 Visualize equipment footprint](../use_cases/uc_inventory/uc5_visualize_equipment_footprint.md)
* [1. My IS inventory/1.6 Visualize application footprint](../use_cases/uc_inventory/uc6_visualize_application_footprint.md)
* [2. Digital Service/2.5 Visualize digital service's footprint](../use_cases/uc_digital_services/uc5_visualize_footprint.md)

![uc1_data_consistency_use_case](../images/Dataconsistency_use_cases.png)

List of errors can be found in the export associated with a digital service or an inventory. To know how to export data from G4IT, please see the following pages : 
* [1. My IS inventory/1.7 Export files](../use_cases/uc_inventory/uc7_export_files.md)
* [2. Digital Service/2.6 Export a digital service](../use_cases/uc_digital_services/uc6_export_digital_service.md)

### Formula to explain the calculation
```math
$$
UnableToCalculatePercentage_{step,criteria} = {AggIndicatorsInError_{step, criteria}\over TotalAggIndicators_{step, cirteria}}
$$
```
*With :*
- ***AggIndicatorsInError**: number of indicators coming from an out_..._equipment table in database with status in error for any specific reason*
- ***TotalAggIndicators**: total number of indicators coming from an out_..._equipment table in database*

Warning: some differences can happen about the percentage between raw indicators and aggregated indicators


## Global view

For every graph view in G4IT, a button is displayed when G4IT wasn't able to produce a part of the indicators associated with the perimeter the user analyzes.

![uc1_data_consistency_off.png](../images/uc1_data_consistency_off.png)
A click on the button will change the visualization to display a graph representing for the different items the percentage of the perimeter that wasn't possible to evaluate for different reasons.

![uc1_data_consistency_on.png](../images/uc1_data_consistency_on.png)

{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Reference | Section                | Elements                         | Type          | Description                                                                                                                                                                                                                                                                                                                                                                |
|-----------|------------------------|----------------------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1         | Impact graph           | Data consistency                 | Switch button | <li><u>*initialization rules*</u>: By default, the button is not visible if all the data is consistent. The button is visible only when a inconsistency exists for one of the element on which the impact has been evaluated.<br><li><u>*action rules*</u>: A click on the button switches the view to visualize the percentage of inconsistency associated to the impact. |
| 2         | Impact graph           | Indicators on a graph element    | Icon + color  | <li><u>*initialization rules*</u>: By default, the button is not visible if all the data is consistent. The icon and label is visible only when a inconsistency exists for this specific element.<br>                                                                                                                                                                      |
| 3         | Data consistency graph | Data consistency for one element | Bar graph     | <li><u>*initialization rules*</u>: The bar represents a percentage between 0 and 1 representing the potential impact that should have been calculated. In the example we couldn't evaluate 50% of the impact associated to the cloud services.                                                                                                                             |

{{% /expand %}}
