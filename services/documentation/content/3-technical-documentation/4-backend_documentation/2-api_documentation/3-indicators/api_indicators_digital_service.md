---
title: "3.1- Digital Service indicators"
description: "This use case describes how to get digital service indicators"
weight: 50
mermaid: true
---

## API PATH

| API                                                                                                                         | Swagger                                                                                                                                 | Use Cases                                                                                                                       |
|:----------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------|
| GET /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/outputs/physical-equipments | [Input/Output](https://saas-g4it.com/api/swagger-ui/index.html#/digital-service-outputs/getDigitalServiceOutputsPhysicalEquipmentsRest) | [Visualise digital service]({{% ref "/2-functional-documentation/use_cases/uc_digital_services/uc5_visualize_footprint.md" %}}) |
| GET /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/outputs/virtual-equipments  | [Input/Output](https://saas-g4it.com/api/swagger-ui/index.html#/digital-service-outputs/getDigitalServiceOutputsVirtualEquipmentsRest)  | [Visualise digital service]({{% ref "/2-functional-documentation/use_cases/uc_digital_services/uc5_visualize_footprint.md" %}}) |

## Description

physical-equipments API allows the user to get physical equipment indicators of digital service. The API returns the
indicators of terminal,network and non-cloud server.

virtual-equipments API allows the user to get virtual equipment indicators of digital service. The API returns the
indicators of virtual and cloud server.

## API Call Processing

The API call is handled
by [OutDigitalServiceController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/controller/OutDigitalServiceController.java)
and the business logic is handled
by [OutPhysicalEquipmentService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/business/OutPhysicalEquipmentService.java)
and [OutVirtualEquipmentService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/business/OutVirtualEquipmentService.java).

## Data Model

Below you will find the entities used to save digital service indicators.

| Package                                       | Entity               | table                                                                                                                           |
|-----------------------------------------------|----------------------|---------------------------------------------------------------------------------------------------------------------------------|
| com.soprasteria.g4it.backend.apiinout.modeldb | OutPhysicalEquipment | [out_physical_equipment](../../db_documentation/information_system_and_digital_service_output_data/digital_service_output_data) |
| com.soprasteria.g4it.backend.apiinout.modeldb | OutVirtualEquipment  | [out_virtual_equipment](../../db_documentation/information_system_and_digital_service_output_data/digital_service_output_data)  |
