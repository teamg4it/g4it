---
title: "Evaluating digital service with Ecomind"
mermaid: true
---

## API PATH

| API                                                                                                         | Swagger                                                                                                              | Use Cases                                                                                                                      |
|:------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------|
| POST /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/evaluating | [Input/Output](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-evaluating/launchEvaluatingDigitalService) | [Estimate a digital service]({{% ref "/2-functional-documentation/use_cases/uc_digital_services/uc4_launch_estimation.md" %}}) |

## Description

The use case allows a project team to launch the calculation for the estimation of impacts of the Digital Service whith the use of Ecomind. The
calculation is based on different indicators that contextualize the impacts observed. The user sends an
digitalServiceUid as pair as an organisation and subscriber.
The user will receive a response with a task id.

## API Call Processing

{{< mermaid align="center">}}

flowchart LR
A[API Call for Evaluation] --> B(Get the active criteria to evaluate impacts on)
B --> C(Create the evaluating task with status TO_START)
C --> D(Launch asynchroneous evaluating process)
D --> E(Return the task id)
{{</ mermaid >}}

Note that, the loading process is done asynchronous.
Attention, to consume small resource the loading process is done by one thread. So if there are two evaluate
in the instance, one will wait for the other to finish.

The API call is handled
by [EvaluatingController](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/controller/EvaluatingController.java)
and the business logic is handled
by [EvaluatingService](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/EvaluatingService.java).
The EvaluatingService retrieves active criteria for evaluation or defaults to predefined criteria if none are active and
then handles the logic for initiating and managing evaluation tasks.

## Asynchronous Execution

The asynchronous evaluation process follows these steps:

{{< mermaid align="center">}}

flowchart LR
A[Set the Task in IN_PROGRESS] -->B[Create a local export directory specific to each task for storing csv files]
B --> C[Choose the Evaluate Service to call with the attribute is_ai of digital_Service]
C --> D(Invoke the doEvaluate method of EvaluateService to perform evaluations)
D --> E[Set Task as COMPLETED upon successful execution with progress set to 100%]
E --> F[Save the indicators in the database tables]
F --> G[Compress results into a ZIP file and uploads it to file storage]
G --> H[Clean up local directory after successful execution.]

{{</ mermaid >}}

This process is done in
the [AsyncEvaluatingService class](https://github.com/G4ITTeam/g4it/blob/main/services/backend/src/main/java/com/soprasteria/g4it/backend/apievaluating/business/asyncevaluatingservice/AsyncEvaluatingService.java).

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
For the ia part there is only one physical equipment per digital service.
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
