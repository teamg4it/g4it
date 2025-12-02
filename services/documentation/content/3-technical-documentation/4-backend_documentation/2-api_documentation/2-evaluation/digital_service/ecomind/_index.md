---
title: "Evaluating digital service version with Ecomind"
mermaid: true
---

## Evaluation Process

The digital service data is evaluated using active criteria in
the [EvaluateAiService class](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/EvaluateAiService.java).
Note: active criteria here refers to the criteria set for the digital service to calculate the impacts for.

### Impact Calculation

The EvaluateAiService call Ecomind to get the electricity consumption and evaluates the physical equipment and virtual equipment associated with the digital service.
Following this evaluation, it aggregates the results using active criteria and lifecycle steps.

#### Ecomind:
The external
service [AiService class](https://github.com/teamg4it/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiaiservice/business/AiService.java)
is used to call Ecomind and run the calculation.
The results of ecomind return the recommendation and the electricity consumption.

#### Physical Equipment:

Physical equipment entities are retrieved from the database in batches via
the [InPhysicalEquipmentRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/InPhysicalEquipmentRepository.java).
For the ia part there is only one physical equipment per digital service version.
It's build and updated with the information get in the forms of ecomind and the call of ecomind.

The
external [EvaluateNumEcoEvalService class](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java)
is used to calculate impacts for each piece of equipment based on criteria, lifecycle steps, and hypotheses.
The results for physical equipment indicators are aggregated in memory, and both the input data and generated indicators
are written to CSV files.

#### Virtual Equipment:

Virtual equipment entity corresponding to the physicalEquipment is retrieved from the database via
the [InVirtualEquipmentRepository](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository/InVirtualEquipmentRepository.java).
Processes virtual equipment associated with physical equipment.

The [EvaluateNumEcoEvalService class](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/engine/numecoeval/EvaluateNumEcoEvalService.java)
from numEcoEval library is used for the traditional virtual equipment.
The results for virtual equipment indicators are aggregated in memory, and both the input data and generated indicators
are written to CSV files.

### Models to save indicators

Below you will find the entities used to save the generated indicators in the database.

| Package                                       | Entity               | table                                                                                                                              |
|-----------------------------------------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------|
| com/soprasteria/g4it/backend/apiinout/modeldb | OutPhysicalEquipment | [out_physical_equipment](../../db/digital_service_output) |
| com/soprasteria/g4it/backend/apiinout/modeldb | OutVirtualEquipment  | [out_virtual_equipment](../../db/digital_service_output)  |
| com/soprasteria/g4it/backend/apiinout/modeldb | OutAiReco        | [out_ai_reco](../../db/digital_service_output)               |

These entities are saved by
the [SaveService class](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/SaveService.java)
using the 'out' repositories in
the [package](https://github.com/G4ITTeam/g4it/tree/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apiinout/repository).

### Export Process

The csv files stored in the local directory are compressed into a ZIP file and uploads it to file storage.
Local directory is cleaned up after successful uploads.

### Task life cycle:

The task progress percentage is updated dynamically during processing and sets to COMPLETED upon successful execution
with progress set to 100%.
Any errors encountered during execution are logged, and the task status is marked as FAILED.

Here is the status of the task:

This task has the type EVALUATING_DIGITAL_SERVICE.

{{< mermaid align="center">}}

stateDiagram-v2
[] --> TO_START: creation of the evaluation task
TO_START --> IN_PROGRESS: Launching of the asynchronous evaluation process
IN_PROGRESS --> COMPLETED: Evaluation process is completed
IN_PROGRESS --> FAILED : Blocking error during the evaluation process (details of the error persisted in the task)
IN_PROGRESS --> TO_START : Retry of the stuck evaluation process
{{</ mermaid >}}
