---
title: 'Digital Service output data with Ecomind'
description: 'Theses tables present the datamodel to store the environmental footprint calculated indicators for Digital Service with ecomind.'
weight: 10
---
## Entity relationship diagram

```mermaid
erDiagram 

  digital_service {
  }
  task {
    int8 id PK
    int8 inventory_id FK
    varchar digital_service_uid FK
    varchar type
    varchar status
    varchar progress_percentage
    _varchar criteria
    text result_file_url
    int4 result_file_size
    timestamp creation_date
    timestamp last_update_date
    timestamp end_time
    _varchar details
    _varchar filenames
    _varchar errors
    int8 created_by FK
  }
  out_physical_equipment {
  }
  out_virtual_equipment {
  }
  out_ai_reco {
        int8 id PK
        int8 task_id FK
        float8 electricity_consumption
        jsonb recommendation
        timestamp creation_date
        timestamp last_update_date 
  }
  digital_service ||--o{ task : "foreign key"
  task ||--o{ out_physical_equipment : "foreign key"
  task ||--o{ out_virtual_equipment : "foreign key"
  task ||--o{ out_ai_reco : "foreign key"
``` 

## Tables

### out_ai_reco

{{% expand title="Show details" expanded="false" center="true"%}}

#### Comments

- This table contains the results of the Ecomind indicator call for the digital service.

#### Columns

| Name                      |Data type| Comments                                                               |
|---------------------------|---|------------------------------------------------------------------------|
| **id**                    |int8| <ul><li>Auto incremented unique virtual equipment identifier</li></ul> |
| *task_id*                 |int8| <ul><li>Foreign key to the task</li></ul>                              |
| *electricity_consumption* |float8| <ul><li>The electricity consumption return by ecomind</li></ul>        |
| *recommendation*          |jsonb| <ul><li>The recommendations return by ecomind</li></ul>                |
| *creation_date*           |timestamp| <ul><li>The creation date</li></ul>                                    |
| *last_update_date*        |timestamp| <ul><li>The last update date</li></ul>                                 |

#### Primary Key

- id
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|task_id|task|id|

{{% /expand %}}

