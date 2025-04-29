---
title: "1- Load inventory files"
description: "This use case describes how to load files"
weight: 30
mermaid: true
---

## API PATH

| API                                                                                                    | Swagger                                                                                                       | Use Cases                                                                                                  |
| :----------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------------------------------------------------------- |
| POST /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/load-input-files | [Input/Output](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-loading-files/launchloadInputFiles) | [Load inventory files]({{% ref "/2-functional-documentation/use_cases/uc_inventory/uc3_load_files.md" %}}) |

## Description

This API allows the user to upload files to the inventory. The user can upload files containing datacenters, physical
equipments, virtual equipments, and applications.
The files are uploaded to the fileStorage and then the loading is done asynchronously.

The API returns the async task id that can be used to track the loading process.

## API Call Processing

{{< mermaid align="center">}}

flowchart LR
A[API Call for Loading] --> B(Upload file into the fileStorage)
B --> C(Create the loading task)
C --> D(Launch asynchroneous loading process)
D --> E(Return the task id)
{{</ mermaid >}}

Note that, the loading process is done asynchronous. The user can track the loading process by calling the
API [`GET /subscribers/{subscriber}/organizations/{organization}/inventories/{inventoryId}/task/{taskId}`](https://saas-g4it.com/api/swagger-ui/index.html#/task/getTask).

Attention, to consume small resource the loading process is done by one thread. So if there are two loads
in the instance, one will wait for the other to finish.

The API call is handled
by [LoadInputFilesController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/controller/LoadInputFilesController.java)
and the business logic is handled
by [LoadInputFilesService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/business/LoadInputFilesService.java).

## Asynchronous Loading Process

The asynchronous loading process follows these steps:

{{< mermaid align="center">}}

flowchart LR
A[Download files] --> B(Transform files into CSV)
B--> C[Check for missing mandatory headers]
C --> D[Load files in metadata checking tables, then wait for complete loading]
D --> E[Check coherence errors]
E --> F[Bulk parse files]
F --> G[Handle rejected files]
G --> H[Update task status]

{{</ mermaid >}}

This process is done in
the [AsyncLoadFilesService class](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiloadinputfiles/business/asyncloadservice/AsyncLoadFilesService.java).

### Checking process

Before any checking process starts, a check for missing mandatory headers is done. In case there is any mandatory field mising for any of the uploaded files, the task fails with status 'FAILED' and no further processing is done.

The main purpose of the checking process is to check the global coherence of the files to be loaded between each other
and
between already loaded files of the same inventory.

To do this efficiently, we load each file in parallel using a newly created thread pool. Each file is loaded into the
appropriate check metadata table depending on its type.

Loading of metadata is a dump load with no business logic. We just bulk read each file depending
on its type, and we insert the bulk of data into the corresponding check table.

The process waits until all files are loaded continuing.

This is done in the [AsyncLoadMetadataService class](). The process of loading the metadata is done in
the [LoadMetadataService class]() using loaders depending on the file type.

##### File to load Metadata

Bellow you will find the entities used to load the metadata of the files to load.

| Package                                                | Entity                 | table                                                                                                                                    |
| ------------------------------------------------------ | ---------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| com.soprasteria.g4it.backend.apiloadinputfiles.modeldb | CheckApplication       | [check_inv_load_application](../../db_documentation/information_system_and_digital_service_input_data/digital_service_input_data)        |
| com.soprasteria.g4it.backend.apiloadinputfiles.modeldb | CheckDatacenter        | [check_inv_load_datacenter](../../db_documentation/information_system_and_digital_service_input_data/digital_service_input_data)         |
| com.soprasteria.g4it.backend.apiloadinputfiles.modeldb | CheckPhysicalEquipment | [check_inv_load_physical_equipment](../../db_documentation/information_system_and_digital_service_input_data/digital_service_input_data) |
| com.soprasteria.g4it.backend.apiloadinputfiles.modeldb | CheckVirtualEquipment  | [check_inv_load_virtual_equipment](../../db_documentation/information_system_and_digital_service_input_data/digital_service_input_data)  |

These entities are loaded by the loader in the package apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders
using repository in the package apiloadinputfiles.repository

To efficiently load the data, the entity must have a generated id using sequence incrementing in batch (such as 100)

During the insertion into the loader, data is inserted in bulk, and at each iteration of batch data, the entity manager
is flushed and cleared to avoid memory overload.
See the class LoadApplicationMetadataService for an example of the application loading process.

##### Coherency check

The coherency check is performed by service in the package apiloadinputfiles.business.asyncloadservice.checkmetadata

This consists of requesting a native query to proceed in the cross-check for the loaded task id between data loaded in
the check tables and inventory data.

The coherency check returns a Map of filename, Map of line number, List of LineError :
filename -> [ line number -> LineErrors ].
It returns all the coherence errors depending on each file by line number.

This map is integrated in the process of writing rejected files.

See the CheckMetadataInventoryFileService class for an example of the application coherency checking process.

### Managing file processing

After checking for coherency errors and verifying that the error size is less than a threshold (e.g.: 50000 lines), we
proceed to load the file.

This is done by the LoadFileService class by parsing the file using the appropriate parser and then inserting the data
in bulk using the appropriate repository.

The batch of records is inserted by the loader of the apiloadinputfiles.business.asyncloadservice.loadobject package as
a LoadApplicationService.

The loader service uses a repository of the package apiinout.resitory as the used tables are the responsibility of inout
api used for digital service inventory input.

Similar to efficiently loading the data, the entity must have a generated id using sequence incrementing in batch (such
as 100). This is present in the package apiinout.modeldb as a class public class InApplication.

##### Model of files to load

Bellow you will find the entities used to load the metadata of the files to load.

| Package                                       | Entity              | table                                                                                                                        |
| --------------------------------------------- | ------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| com.soprasteria.g4it.backend.apiinout.modeldb | InApplication       | [in_application](../../db_documentation/information_system_and_digital_service_input_data/digital_service_input_data)        |
| com.soprasteria.g4it.backend.apiinout.modeldb | InDatacenter        | [in_datacenter](../../db_documentation/information_system_and_digital_service_input_data/digital_service_input_data)         |
| com.soprasteria.g4it.backend.apiinout.modeldb | InPhysicalEquipment | [in_physical_equipment](../../db_documentation/information_system_and_digital_service_input_data/digital_service_input_data) |
| com.soprasteria.g4it.backend.apiinout.modeldb | InVirtualEquipment  | [in_virtual_equipment](../../db_documentation/information_system_and_digital_service_input_data/digital_service_input_data)  |

### Handling rejected files

During the consistency check and the file loading process, some records of the original file may be rejected.

They are placed in a list of LineError corresponding to the fileToLoad.

So each fileToLoad has a file of rejected lines with the same filename but with the prefix rejected.

At the end of processing, these files are uploaded to the file store and the task status is updated.

To know all the rejection cases related to the performed checks, please refer to the
[uc_inventory uc3_load_files behaviour detail](../../../../2-functional-documentation/use_cases/uc_inventory/uc3_load_files)
documentation.

## Retry stuck loading task

If the loading task is stuck in the IN_PROGRESS state, a distributed scheduler in the class InventoryLoadingScheduler is
responsible for reprocessing tasks that have not been updated for more than 15 minutes (configurable value).

The task is restarted and the async loading is triggered again.

## Task life cycle

Here is the status of the task:

This task has the type LOADING.

{{< mermaid align="center">}}

stateDiagram-v2
[] --> TO_START: creation of the loading task
TO_START --> IN_PROGRESS: Launching of the asynchronous loading process
IN_PROGRESS --> COMPLETED: Loading process is completed without error
IN_PROGRESS --> COMPLETED_WITH_ERRORS : Loading process is completed with some loading errors (there is rejected lines in the rejected files)
IN_PROGRESS --> FAILED : Blocking error during the loading process (details of the error persisted in the task)
IN_PROGRESS --> TO_START : Retry of the stuck loading process
{{</ mermaid >}}

Update date is persisted in the task to know the last update date of the task.
