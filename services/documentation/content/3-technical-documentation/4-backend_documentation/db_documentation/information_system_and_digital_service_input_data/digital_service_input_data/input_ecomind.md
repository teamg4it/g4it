---
title: 'Digital Service input data with Ecomind'
description: 'These tables present the datas provide by a user for each Digital Service with ecomind (terminals, networks and servers) in order to evaluate its environmental footprint.'
weight: 4
---
## Entity relationship diagram 

```mermaid
erDiagram 

  note {
  }
  digital_service {
    varchar uid PK
    varchar name
    timestamp last_calculation_date
    timestamp creation_date
    timestamp last_update_date
    int8 note_id FK
    _varchar criteria
    int8 user_id FK
    int8 organization_id FK
    bool is_new_arch
    bool is_migrated
    bool is_ai
  }
  in_datacenter {
  }
  in_physical_equipment {
  }
  in_virtual_equipment {
  }
    in_ai_parameter {
        int8 id PK
        varchar nb_parameters
        varchar framework
        varchar quantization
        int8 total_generated_tokens
        int8 number_user_year
        int8 average_number_request
        int8 average_number_token
        bool is_inference
        bool is_finetuning
        timestamp creation_date
        timestamp last_update_date
        varchar digital_service_uid FK
        varchar model_name
        varchar type
    }
    in_ai_infrastructure {
        int8 id PK
        float8 complementary_pue
        varchar infrastructure_type
        int8 nb_gpu
        int8 gpu_memory
        varchar digital_service_uid FK
    }
  note ||--o{ digital_service : "foreign key"
  digital_service ||--o{ in_datacenter : "foreign key"
  digital_service ||--o{ in_physical_equipment : "foreign key"
  digital_service ||--o{ in_virtual_equipment : "foreign key"
  digital_service ||--o{ in_ai_parameter : "foreign key"
  digital_service ||--o{ in_ai_infrastructure : "foreign key"
``` 

## Tables

### in_ai_parameter

{{% expand title="Show details" expanded="false" center="true"%}}

#### Comments

- That table present the virtual equipment data provided by a user for each  Information System or Digital Service. in order to evaluate its environmental footprint.

#### Columns

|Name|Data type| Comments                                                           |
|---|---|--------------------------------------------------------------------|
|**id**|int8| <ul><li>Auto incremented unique ai parameters identifier</li></ul> |
|nb_parameters|varchar| <ul><li>Nb_parameters of ai parameters</li></ul>                   |
|framework|varchar| <ul><li>Framework of ai parameters</li></ul>                       |
|quantization|varchar| <ul><li>Quantization of ai parameters</li></ul>                    |
|total_generated_tokens|int8| <ul><li>The total generated tokens</li></ul>                       |
|number_user_year|int8| <ul><li>Number of user per year</li></ul>                          |
|average_number_request|int8| <ul><li>Average number of request</li></ul>                        |
|average_number_token|int8| <ul><li>Average number of token</li></ul>                          |
|is_inference|bool| <ul><li>The inference type</li></ul>                               |
|is_finetuning|bool| <ul><li>The finetuning type</li></ul>                              |
|creation_date|timestamp| <ul><li>Ai parameters Creation Date</li></ul>                      |
|last_update_date|timestamp| <ul><li>Ai parameters Last update date</li></ul>                   |
|*digital_service_uid*|varchar| <ul><li>Foreign key to the Digital service</li></ul>               |
|model_name|varchar| <ul><li>The model name</li></ul>                                   |
|type|varchar| <ul><li>the type</li></ul>                                         |

#### Primary Key

- id
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|digital_service_uid|digital_service|uid|

{{% /expand %}}
### in_ai_infrastructure

{{% expand title="Show details" expanded="false" center="true"%}}

#### Comments

- That table present the virtual equipment data provided by a user for each  Information System or Digital Service. in order to evaluate its environmental footprint.

#### Columns

|Name|Data type| Comments                                                               |
|---|---|------------------------------------------------------------------------|
|**id**|int8| <ul><li>Auto incremented unique ai infrastructure identifier</li></ul> |
|complementary_pue|float8| <ul><li>Complementary pue</li></ul>                                    |
|infrastructure_type|varchar| <ul><li>Infrastructure type</li></ul>                                  |
|nb_gpu|int8| <ul><li>Number of gpu</li></ul>                                        |
|gpu_memory|int8| <ul><li>Memory of the gpu</li></ul>                                    |
|*digital_service_uid*|varchar| <ul><li>Foreign key to the Digital service</li></ul>                   |

#### Primary Key

- id
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|digital_service_uid|digital_service|uid|

{{% /expand %}}

