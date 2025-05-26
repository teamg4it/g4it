---
title: "Open API"
description: "List of open api url"
date: 2023-12-21T14:28:38+01:00
weight: 40
---

## Open API

https://saas-g4it.com/api/swagger-ui/index.html

## Correspondence between APIs and use cases

### Loading

| API                                                                                                    | Use Cases                                                               | Technical documentation                                                                                                      |
|:-------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------|
| POST /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/load-input-files | [Load files]({{% ref "../use_cases/uc_inventory/uc3_load_files.md" %}}) | [API Loading]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/1-loading/api_loading.md" %}}) |

### Evaluation

| API                                                                                                         | Use Cases                                                                                                       | Technical documentation                                                                                                                                                |
|:------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| POST /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/evaluating | [Digital Services - Launch Estimation]({{% ref "../use_cases/uc_digital_services/uc4_launch_estimation.md" %}}) | [API Evaluating Digital Services]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/2-evaluation/api_evaluating_digital_service.md" %}}) |
| POST /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/evaluating            | [Inventory - Launch Estimation]({{% ref "../use_cases/uc_inventory/uc4_launch_estimation.md" %}})               | [API Evaluating Inventories]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/2-evaluation/api_evaluating_inventory.md" %}})            |

### Get indicators

| API                                                                                                                               | Use Cases                                                                                                                         | Technical documentation                                                                                                                                               |
|:----------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/outputs/physical-equipments       | [Visualize digital service]({{% ref "/2-functional-documentation/use_cases/uc_digital_services/uc5_visualize_footprint.md" %}})   | [API indicators Digital Service]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_digital_service.md" %}}) |
| GET /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/outputs/virtual-equipments        | [Visualize digital service]({{% ref "/2-functional-documentation/use_cases/uc_digital_services/uc5_visualize_footprint.md" %}})   | [API indicators Digital Service]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_digital_service.md" %}}) |
| GET /referential/boaviztapi/countries                                                                                             | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})     | [API indicators Inventory]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_inventory.md" %}})             |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/equipments                        | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})     | [API indicators Inventory]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_inventory.md" %}})             |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/datacenters                       | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})     | [API indicators Inventory]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_inventory.md" %}})             |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/physicalEquipmentsAvgAge          | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})     | [API indicators Inventory]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_inventory.md" %}})             |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/physicalEquipmentsElecConsumption | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})     | [API indicators Inventory]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_inventory.md" %}})             |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/outputs/virtual-equipments                   | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})     | [API indicators Inventory]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_inventory.md" %}})             |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/inputs/virtual-equipments                    | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})     | [API indicators Inventory]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_inventory.md" %}})             |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/applications                      | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) | [API indicators Inventory]({{% ref "/3-technical-documentation/4-backend_documentation/2-api_documentation/3-indicators/api_indicators_inventory.md" %}})             |
