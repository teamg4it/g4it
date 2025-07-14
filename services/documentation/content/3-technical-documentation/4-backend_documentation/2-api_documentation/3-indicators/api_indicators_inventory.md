---
title: "3.2- Inventory indicators"
description: "Visualize indicators for digital service or information system"
weight: 60
mermaid: true
---

## API PATH

#### API PATH FOR EQUIPMENTS

| API                                                                                                                               | Swagger                                                                                                                                                 | Use Cases                                                                                                                       |
|:----------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------|
| GET /referential/boaviztapi/countries                                                                                             | [Boavizta countries](https://saas-g4it.com/api/swagger-ui/index.html#/digital-service%20referential/getBoaviztaCountries)                               | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/equipments                        | [Equipment indicators](hhttps://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getEquipmentIndicators)                                    | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/datacenters                       | [Datacenters](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getDataCenterIndicators)                                             | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/physicalEquipmentsAvgAge          | [Average age](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getPhysicalEquipmentAvgAge)                                          | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/physicalEquipmentsElecConsumption | [Electricity consumption](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getPhysicalEquipmentElecConsumption)                     | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/outputs/virtual-equipments                   | [Output virtual equipments](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-outputs/getInventoryOutputsVirtualEquipmentsRest)                | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/inputs/virtual-equipments                    | [Input virtual equipments](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-inputs-virtual-equipment/getInventoryInputsVirtualEquipmentsRest) | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |

#### API PATH FOR APPLICATIONS

| API                                                                                                          | Swagger                                                                                                                 | Use Cases                                                                                                                         |
|:-------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/indicators/applications | [Application indicators](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getApplicationIndicators) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |

## Description

These APIs enable users to visualize the impacts of equipment and applications within an inventory.
The user sends an inventoryId as pair as an organisation and subscriber.

### Models to retrieve indicators

Below you will find the entities used to retrieve the saved indicators in the database.

| Package                                       | Entity               | table                                                                                                                              |
|-----------------------------------------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------|
| com/soprasteria/g4it/backend/apiinout/modeldb | InDatacenter         | [in_datacenter](../../db_documentation/information_system_and_digital_service_input_data/information_sytem_input_data/)            |
| com/soprasteria/g4it/backend/apiinout/modeldb | InPhysicalEquipment  | [ in_physical_equipment ](../../db_documentation/information_system_and_digital_service_input_data/information_sytem_input_data/)  |
| com/soprasteria/g4it/backend/apiinout/modeldb | OutPhysicalEquipment | [out_physical_equipment](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data/) |
| com/soprasteria/g4it/backend/apiinout/modeldb | InVirtualEquipment   | [ in_virtual_equipment ](../../db_documentation/information_system_and_digital_service_input_data/information_sytem_input_data/)   |
| com/soprasteria/g4it/backend/apiinout/modeldb | OutVirtualEquipment  | [out_virtual_equipment](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data/)  |
| com/soprasteria/g4it/backend/apiinout/modeldb | OutApplication       | [out_application](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data/)        |

## API Call Processing for equipment view

#### Boavizta countries API

The API call is handled
by [DigitalServiceReferentialController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apidigitalservice/controller/DigitalServiceReferentialController.java)
and the business logic is handled
by [DigitalServiceReferentialService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apidigitalservice/business/DigitalServiceReferentialService.java)
which returns the map of countries available in boavizta by
calling [Boavizta API](https://api.boavizta.org//docs#/utils/utils_get_all_countries_v1_utils_country_code_get)
via [BoaviztapiService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/external/boavizta/business/BoaviztapiService.java).

#### Equipment indicators

The API call is handled
by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and the business logic is handled
by [InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java)
which returns the physical equipments indicators saved in the
table [out_physical_equipment](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data)
via [OutPhysicalEquipmentRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/OutPhysicalEquipmentRepository.java)
after grouping them by criteria in
the [IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java).

#### Datacenters

The API call is handled
by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java),
while the business logic is executed by
the [InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java).
This service retrieves aggregated data about datacenters and their associated physical equipment by querying
the [in_datacenter](information_system_and_digital_service_input_data/information_sytem_input_data/)
and [in_physical_equipment](../../db_documentation/information_system_and_digital_service_input_data/information_sytem_input_data/)
tables within
the [InDatacenterIndicatorView](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/modeldb/InDatacenterIndicatorView.java),
utilizing
the [InDatacenterViewRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/InDatacenterViewRepository.java)
invoked from
the [IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java).

#### Average age

The API call is handled
by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and the business logic is handled
by [InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java).
This service retrieves the average age of the equipment by aggregating data of
the [out_physical_equipment](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data/)
table within
the [InPhysicalEquipmentAvgAgeView](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/modeldb/InPhysicalEquipmentAvgAgeView.java),
utilizing
the [InPhysicalEquipmentAvgAgeViewRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/InPhysicalEquipmentAvgAgeViewRepository.java)
invoked from
the [IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java).

#### Electricity consumption

The API call is handled
by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and the business logic is handled
by [InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java).
This service retrieves the avg electric consumption of the equipment by aggregating electricity consumption per criteria
by location, equipment type, data of
the [out_physical_equipment](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data/)
table within
the [InPhysicalEquipmentElecConsumptionView](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/modeldb/InPhysicalEquipmentElecConsumptionView.java),
utilizing
the [InPhysicalEquipmentElecConsumptionViewRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/InPhysicalEquipmentElecConsumptionViewRepository.java)
invoked from
the [IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java).

#### Input virtual equipments

The API call is handled
by [InVirtualEquipmentInventoryController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/controller/InVirtualEquipmentInventoryController.java)
and the business logic is handled
by [InVirtualEquipmentService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/business/InVirtualEquipmentService.java)
which returns input data of virtual equipments stored in the
table [in_virtual_equipment](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_input_data)
via [InVirtualEquipmentRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/InVirtualEquipmentRepository.java).

#### Output virtual equipments

The API call is handled
by [OutInventoryController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/controller/OutInventoryController.java)
and the business logic is handled
by [OutVirtualEquipmentService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/business/OutVirtualEquipmentService.java)
which returns virtual equipments indicators saved in the
table [out_virtual_equipment](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data)
via [OutVirtualEquipmentRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/business/OutVirtualEquipmentService.java).

## API Call Processing for application view

#### Application indicators

The API call is handled
by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and the business logic is handled
by [InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java)
which returns the application indicators saved in the
table [out_application](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data)
via [OutApplicationRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/business/OutApplicationRepository.java).
