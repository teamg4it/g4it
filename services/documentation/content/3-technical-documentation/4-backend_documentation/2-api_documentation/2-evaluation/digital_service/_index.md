---
title: "Evaluating digital service "
mermaid: true
---

## API PATH

| API                                                                                                         | Swagger                                                                                                              | Use Cases                                                                                                                      |
|:------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------|
| POST /organizations/{organization}/workspaces/{workspace}/digital-services/{digitalServiceUid}/evaluating | [Input/Output](https://saas-g4it.com/api/swagger-ui/index.html#/inventory-evaluating/launchEvaluatingDigitalService) | [Estimate a digital service]({{% ref "/2-functional-documentation/use_cases/uc_digital_services/uc4_launch_estimation.md" %}}) |

## Description

The use case allows a project team to launch the calculation for the estimation of impacts of the Digital Service whith the use of Ecomind. The
calculation is based on different indicators that contextualize the impacts observed. The user sends an
digitalServiceUid as pair as a workspace and organization.
The user will receive a response with a task id.

## API Call Processing

{{< mermaid align="center">}}

flowchart LR
A[API Call for Evaluation] --> B(Get the active criteria to evaluate impacts on)
B --> C(Create the evaluating task with status TO_START)
C --> D(Launch asynchronous evaluating process)
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

## possibilities for the next step

{{% children %}}
