---
title: 'Ecomind architecture'
weight: 10
---
### Overview
This section illustrates the communication architecture between the frontend and backend components with the use of Ecomind.

### Display

```mermaid
flowchart LR
    subgraph Front
        A["Create AI service"]
        B["Ai parameters"]
        C["Ai infrastructure"]
    end

    subgraph Back
        A1["{...}/digital-services"]
        B1["/ecomindai/aiModelConfig/type/{type}"]
        B2["{...}/digital-services/{digitalServiceUid}/ai-parameter-input"]
        C1["{...}/digital-services/{digitalServiceUid}/ai-infra-input"]
    end

    subgraph Extern
        B11["EcomindAi"]
    end

    A --> A1
    B --> B1
    B --> B2
    C --> C1
    B1 --> B11
```
{...} : Another part of the url used (/organizations/{organization}/workspaces/{workspace}).

Ecomind is an externally deployed service that is invoked through a client [Ai Modelapi Client](https://github.com/teamg4it/g4it/blob/develop_ecomind/services/backend/src/main/java/com/soprasteria/g4it/backend/external/ecomindai/client/AiModelapiClient.java).

### Calculation

```mermaid
flowchart LR
    subgraph Front
        A["Calculate"]
    end

    subgraph Back
        A1["digital-services/{digitalServiceUid}/evaluating"]
        A2["Async: EvaluateAiService"]
        A3["Ecomind Client"]
    end

    subgraph Externe
        B11["EcomindAi"]
    end

    A --> A1
    A1 --> A2
    A2 --> A3
    A3 --> B11
```

The calculation involves multiple steps, which you can find on [Evaluating digital Service](../backend/api/evaluating/_index.md)

### Result
```mermaid
flowchart LR
    subgraph Front
        A["Display result"]
    end

    subgraph Back
        A1["{...}/digital-services/{digitalServiceUid}/outputs/physical-equipments"]
        A2["{...}/digital-services/{digitalServiceUid}/outputs/virtual-equipments"]
        A3["{...}/digital-services/{digitalServiceUid}/outputs/ai-recomandation"]
    end

    A --> A1
    A --> A2
    A --> A3
```
{...} : Another part of the url used (/organizations/{organization}/workspaces/{workspace}).

This change updates the "Visualize" tab in the digital service to present the recommendations provided by Ecomind.
