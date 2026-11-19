---
title: "3.2- Inventory indicators"
description: "Visualize indicators for digital service or information system"
weight: 60
mermaid: true
---

## API PATH

#### API PATH FOR EQUIPMENTS

| API                                                                                                                             | Swagger                                                                                                                                  | Use Cases                                                                                                                       |
|:--------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------|
| GET /referential/boaviztapi/countries                                                                                           | [Boavizta countries](https://saas-g4it.com/api/swagger-ui/index.html#/digital-service%20referential/getBoaviztaCountries)                | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/equipments                        | [Equipment indicators](hhttps://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getEquipmentIndicators)                     | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/datacenters                       | [Datacenters](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getDataCenterIndicators)                              | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/physicalEquipmentsAvgAge          | [Average age](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getPhysicalEquipmentAvgAge)                           | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/physicalEquipmentsElecConsumption | [Electricity consumption](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getPhysicalEquipmentElecConsumption)      | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/outputs/virtual-equipments                   | [Output virtual equipments](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-outputs/getInventoryOutputsVirtualEquipmentsRest) | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/physicalEquipmentsLowEquipment    | [Low Equipment](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getPhysicalEquipmentsLowImpact)                         | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/outputs/ai-services                          | [Ai service](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getPhysicalEquipmentsLowImpact)                         | [Visualize equipments]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc5_visualize_equipment_footprint.md" %}})   |

#### API PATH FOR APPLICATIONS

| API                                                                                                                            | Swagger                                                                                                                 | Use Cases                                                                                                                         |
|:-------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/applications                     | [Application indicators](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getApplicationIndicators) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |
| POST /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/applications | [Application indicators (paginated)](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getApplicationIndicators) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |
| POST /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/applications/multi-criteria-impacts | [Application multi-criteria impacts](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getApplicationMultiCriteriaImpacts) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |
| POST /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/applications/multi-criteria | [Application multi-criteria (graph drill-down)](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getApplicationMultiCriteriaIndicators) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/applications/filters | [Application distinct filter values](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getApplicationFilters) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/virtualEquipmentsElecConsumption | [Low Equipment](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getVirtualEquipmentElecConsumption) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |
| GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/virtualEquipmentsLowEquipment                     | [Electricity consumption](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getVirtualEquipmentsLowImpact) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |
| POST /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/virtualEquipmentsLowImpact/count | [Virtual equipment low impact count](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getVirtualEquipmentsLowImpactCount) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |
| POST /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/virtualEquipmentsElecConsumption/sum | [Virtual equipment electricity consumption sum](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-indicator/getVirtualEquipmentElecConsumptionSum) | [Visualize applications]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc6_visualize_application_footprint.md" %}}) |

## Description

These APIs enable users to visualize the impacts of equipment and applications within an inventory.
The user sends an inventoryId as pair as a workspace and organization.

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
| com/soprasteria/g4it/backend/apiinout/modeldb | OutAIService         | [out_aiservice](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data/)        |

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

#### Physical equipment low impact
The API call is handled by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java), 
and the data retrieval is performed through the native query defined in [InPhysicalEquipmentLowImpactView](https://github.com/teamg4it/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/modeldb/InPhysicalEquipmentLowImpactView.java), executed via its repository. The query aggregates physical equipment quantities per inventory, country, type, and organizational filters.

The low-impact determination is handled by [LowImpactService](https://github.com/teamg4it/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/LowImpactService.java), which evaluates each country’s electricity mix impact by summing quartile positions across configured environmental criteria. A country is considered low impact if its total electricity mix score falls within the first quartile compared to all countries.

#### AI services

The AI Services output API retrieves the environmental footprint indicators calculated for the AI services
of an inventory.

The API uses the `out_aiservice` table, which stores the calculated AI Service indicators generated using
the EcoLogits calculation engine.

The returned AI Service data includes the AI service name, criterion, lifecycle step, provider, model,
location, calculation engine information, status indicator, quantity, unit impact, people equivalent impact,
and calculation errors when applicable.

The API is exposed through:

GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/outputs/ai-services

The task_id field associates each AI Service result with the evaluation task that generated the indicators.

## API Call Processing for application view

#### Application indicators

The API call is handled
by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and the business logic is handled
by [InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java)
which returns the application indicators saved in the
table [out_application](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data)
via [OutApplicationRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/business/OutApplicationRepository.java).

#### Application Indicators — Paginated table view

To avoid loading every impact row of a large inventory in a single response, the
`applications` endpoint accepts a JSON filter body (`environment`, `equipmentType`,
`lifeCycle`, `domain`, `subDomain`, `applicationName`, `criterion`) plus `page`/`size`
query parameters. The call is handled by
[InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and delegated to
[InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java)
and [IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java).

The data is retrieved by
[OutApplicationCustomRepositoryImpl](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/OutApplicationCustomRepositoryImpl.java)
(`findByTaskId(taskId, filters, pageable)`), which builds a single native SQL statement
doing `WHERE` filtering, then `GROUP BY (application_name, criterion, unit)` with
`SUM(unit_impact)`/`SUM(people_eq_impact)` **before** applying `LIMIT`/`OFFSET` — this
grouping is required because `out_application` stores one row per
application × criterion × lifecycle-step (and further dimensions), so an un-grouped
page could split or duplicate a single application/criterion's data across pages. A
paired `COUNT(*)` query (same predicates, no `LIMIT`) supplies the page's
`totalElements`/`totalPages`.

The result is returned as a `Page<ApplicationIndicatorRowProjection>` (`applicationName`,
`criterion`, `unit`, `impact`, `sip`), mapped 1:1 to
`ApplicationIndicatorRowBO`/`ApplicationIndicatorRowRest` by
[ApplicationIndicatorMapper](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/mapper/ApplicationIndicatorMapper.java) — no entity loading, no in-memory grouping.

#### Application Multi-Criteria Impacts (aggregated, filtered)

Rather than returning the full impact row set per criterion, this endpoint returns, for
each criterion, the **aggregated impact totals** (`impact`, `sip`, `countValue`)
computed only on the rows matching the supplied filters (`environment`, `equipmentType`,
`lifeCycle`, `domain`, `subDomain`). This feeds the criteria selector/summary panel with
live, filter-aware totals without downloading any underlying row.

The call is handled by
[InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and the business logic by
[IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java)
(`getApplicationMultiCriteriaImpacts`), which performs a pure 1:1 mapping of the
projection rows returned by
[OutApplicationRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/OutApplicationRepository.java)
(`aggregateMultiCriteriaImpactsByTaskIdAndFilters`) — a single native SQL query doing
`WHERE` + `GROUP BY (criterion, unit)` + `SUM`/`COUNT` entirely in the database
(`domain`/`subDomain` are read via 1-based indexing of the native Postgres `filters`
array column: `filters[1]`, `filters[2]`).

Cached via `@Cacheable("applicationMultiCriteriaImpacts")` keyed on `(taskId, filters)`.

#### Application Multi-Criteria (graph drill-down, dual-axis)

This endpoint powers the graph view's drill-down navigation
(Global → Domain → SubDomain → Application → VM, `graphLevel`), each node further broken
down by a fixed secondary axis (`repartition`: `lifeCycle` / `environment` /
`equipmentType`). Both `graphLevel` and `repartition` are required on every call and are
combined in a single 2-column `GROUP BY` executed by the database in one round trip.

- At `graphLevel=global` (initial page load, before the first click), no node/hierarchy
  grouping is performed — the response contains a single node broken down only by
  `repartition`.
- Clicking through the hierarchy advances `graphLevel` to `domain`, `subDomain`,
  `application`, then `virtualEquipment`, each time adding the clicked value
  (`domain`, `subDomain`, `applicationName`) to the request's `filters`. `repartition`
  stays independent of the drill depth and can be changed by the user at any point.
- Each node carries `consistentCount`/`inconsistentCount` (derived from
  `OutApplication.statusIndicator`) at every level, plus level-specific fields:
  `subDomainCount` + `applicationCount` at `domain`, `applicationCount` at `subDomain`,
  and `cluster`/`equipmentType`/`environment` at `virtualEquipment`.

The call is handled by
[InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and the business logic by
[IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java)
(`getApplicationMultiCriteriaIndicators`), which dispatches to the matching native query
on [OutApplicationRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/OutApplicationRepository.java)
based on the `(graphLevel, repartition)` pair (one native query per valid combination —
5 graph levels × 3 repartitions), each returning a
`MultiCriteriaAggregateProjection` that the service re-nests into
`repartitions[]` per node — a cheap re-shaping of already DB-aggregated numbers, never a
re-computation of sums.

Cached per `(taskId, criteria, graphLevel, repartition, filters)` key.

#### Application Distinct Filter Values

Populates the application view's filter selectors (Environment, Equipment type,
Lifecycle, Domain, SubDomain) with the distinct values actually present in the
inventory's latest task, instead of a hardcoded list — so the UI never offers a filter
value that would return zero rows. Domain/subDomain are returned as a linked tree
(`{ domain, subDomains[] }`), computed via `GROUP BY filters[1]` +
`array_agg(DISTINCT filters[2])` so each subDomain stays linked to its parent domain.

The call is handled by
[InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and the business logic by
[IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java)
(`getApplicationFilters`), backed by two native SQL queries on
[OutApplicationRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/OutApplicationRepository.java) —
one computing the scalar distinct lists (`array_agg(DISTINCT ...) FILTER (WHERE ... IS NOT NULL)`),
one building the domain→subDomains tree — both fully DB-side, no in-memory grouping.

#### Virtual Equipment Electricity Consumption

The API call is handled by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java)
and the business logic is implemented in [InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java).
The service retrieves aggregated electricity consumption of virtual equipment
from the [out_application](../../db_documentation/information_system_and_digital_service_output_data/information_sytem_output_data) table, using the native query defined in
[InVirtualEquipmentElecConsumptionView](https://github.com/teamg4it/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/modeldb/InVirtualEquipmentElecConsumptionView.java) and executed via 
[InVirtualEquipmentElecConsumptionViewRepository](services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/repository/InVirtualEquipmentElecConsumptionViewRepository.java). Electricity is aggregated per virtual equipment by location, domain, sub-domain, environment, and equipment type, with specific handling to correctly sum non-cloud services and preserve cloud service values.

#### Virtual Equipment Low Impact

The API call is handled by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java),
and the data is retrieved using the native query defined in [InVirtualEquipmentLowImpactView](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/modeldb/InVirtualEquipmentLowImpactView.java), executed via its repository.
The lowImpact field is not calculated in the SQL query.
It is determined separately by the [LowImpactService](https://github.com/teamg4it/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/LowImpactService.java).
The service:
Evaluates the environmental performance of each country based on its electricity mix.
Sums quartile rankings across configured environmental criteria.
Classifies a country as Low Impact if it belongs to the best 25% of countries (first quartile).
If the country hosting the virtual equipment is considered environmentally favorable, the lowImpact flag is set to true.

#### Virtual Equipment Low Impact Count (aggregated)

The API call is handled by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java),
and the business logic is implemented in [InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java)
and [IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java).

Unlike the **Virtual Equipment Low Impact** endpoint above, which returns the full per-row
list and lets the frontend reduce it into a total/low-impact count client-side, this
endpoint returns only the two aggregated counts (`virtualEquipmentTotalCount`,
`lowImpactVirtualEquipmentCount`) needed to compute the low-impact percentage, computed
**entirely in the database** via a single native SQL query executed by
[OutApplicationCustomRepositoryImpl](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/OutApplicationCustomRepositoryImpl.java)
(`countVirtualEquipmentLowImpact`), avoiding the need to download and reduce every row.

The query:
- replicates the same `distinct_applications` deduplication `GROUP BY`
  (`virtual_equipment_name`, `name`, `location`, `lifecycle_step`, `domain`, `subDomain`,
  `environment`, `equipment_type`) used by the legacy
  [InVirtualEquipmentLowImpactView](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/modeldb/InVirtualEquipmentLowImpactView.java)
  native query, to stay consistent with it;
- replicates the low-impact determination performed by
  [LowImpactService](https://github.com/teamg4it/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/LowImpactService.java)
  (per-criterion electricity-mix quartile score, summed per country, compared to the
  first quartile of all countries) directly as SQL, via dedicated `criterion_percentiles`,
  `criterion_quartile`, `country_score`, `threshold` and `country_low_impact` CTEs;
- binds the external Boavizta ISO → country-name mapping (resolved via
  [BoaviztapiService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/external/boavizta/business/BoaviztapiService.java))
  as plain array parameters into an `iso_map` CTE, since this mapping is not itself
  stored in the database.

The result is returned as a `VirtualEquipmentLowImpactCountProjection` (`totalCount`,
`lowImpactCount`), mapped 1:1 to `VirtualEquipmentLowImpactCountBO`/`VirtualEquipmentLowImpactCountRest`,
and cached (`@Cacheable("virtualEquipmentLowImpactCount")`).

#### Virtual Equipment Electricity Consumption Sum (aggregated)

The API call is handled by [InventoryIndicatorController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/controller/InventoryIndicatorController.java),
and the business logic is implemented in [InventoryIndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/InventoryIndicatorService.java)
and [IndicatorService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/business/IndicatorService.java).

Unlike the **Virtual Equipment Electricity Consumption** endpoint above, which returns
the full per-row list and lets the frontend sum `elecConsumption`/`quantity` client-side,
this endpoint returns only the aggregated `elecConsumptionSum`, `noOfVirtualEquipmentsSum`
and `applicationCount`, computed **entirely in the database** via a single native SQL
query executed by
[OutApplicationCustomRepositoryImpl](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/OutApplicationCustomRepositoryImpl.java)
(`sumVirtualEquipmentElecConsumption`).

The query replicates the exact two-level `GROUP BY` performed by the legacy
[InVirtualEquipmentElecConsumptionView](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiindicator/modeldb/InVirtualEquipmentElecConsumptionView.java)
native query — first deduplicating by virtual equipment/location/domain/subDomain/
environment/equipment type/application/infrastructure type/quantity, then
re-aggregating per application (summing non-cloud-service consumption and adding, at
most once, any cloud-service consumption) — before summing over the deduplicated,
filtered set. All shared filter predicates (environment, equipment type, lifecycle,
domain, subDomain, application name) are applied inside a `MATERIALIZED` CTE, before any
aggregation work, which reduced the measured execution time on production-sized data
from ~24s to ~3s.

The result is returned as a `VirtualEquipmentElecConsumptionSumProjection`
(`elecConsumptionSum`, `quantitySum`, `applicationCount`), mapped 1:1 to
`VirtualEquipmentElecConsumptionSumBO`/`VirtualEquipmentElecConsumptionSumRest`, and
cached (`@Cacheable("virtualEquipmentElecConsumptionSum")`).

