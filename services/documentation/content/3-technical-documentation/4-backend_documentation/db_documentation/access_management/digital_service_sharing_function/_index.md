---
title: 'Digital Service Sharing Function'
description: 'Digital Service Sharing Function'
weight: 2
---
## Entity relationship diagram 

```mermaid
erDiagram 

  g4it_user {
    int8 id PK
    varchar email UK
    timestamp creation_date
    timestamp last_update_date
    varchar first_name
    varchar last_name
    varchar sub UK
    varchar domain
  }
  g4it_organization {
    varchar name UK
    timestamp creation_date
    timestamp last_update_date
    int8 id PK
    int8 subscriber_id FK
    int4 storage_retention_day_export
    int4 storage_retention_day_output
    int4 data_retention_day
    text status
    timestamp deletion_date
    int8 last_updated_by FK
    int8 created_by FK
    bool is_migrated
    _varchar criteria_is
    _varchar criteria_ds
  }
  digital_service {
    varchar uid PK
    varchar name
    timestamp last_calculation_date
    timestamp creation_date
    timestamp last_update_date
    int8 note_id FK
    _varchar criteria
    bool is_new_arch
    int8 user_id FK
    int8 organization_id FK
  }
  digital_service_link {
    varchar uid PK
    varchar digital_service_uid FK
    timestamp expiration_date
  }
  digital_service_shared {
    int8 id PK
    int8 user_id FK
    int8 organization_id FK
    varchar digital_service_uid FK
  }
  g4it_user ||--o{ digital_service : "foreign key"
  g4it_user ||--o{ digital_service_shared : "foreign key"
  g4it_user ||--o{ g4it_organization : "foreign key"
  g4it_organization ||--o{ digital_service : "foreign key"
  g4it_organization ||--o{ digital_service_shared : "foreign key"
  digital_service ||--o{ digital_service_link : "foreign key"
  digital_service ||--o{ digital_service_shared : "foreign key"
``` 

## Tables 

### g4it_user 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table defines the list of the G4IT users and configuration associated. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|email|varchar||
|creation_date|timestamp||
|last_update_date|timestamp||
|first_name|varchar||
|last_name|varchar||
|sub|varchar||
|domain|varchar||

#### Primary Key 

 - id
{{% /expand %}}
### g4it_organization 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table defines the list of the existing organisations for a subscriber and configuration associated. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|name|varchar||
|creation_date|timestamp||
|last_update_date|timestamp||
|**id**|int8||
|*subscriber_id*|int8||
|storage_retention_day_export|int4||
|storage_retention_day_output|int4||
|data_retention_day|int4||
|status|text||
|deletion_date|timestamp||
|*last_updated_by*|int8||
|*created_by*|int8||
|is_migrated|bool||
|criteria_is|_varchar||
|criteria_ds|_varchar||

#### Primary Key 

 - id
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|created_by|g4it_user|id|
|last_updated_by|g4it_user|id|
|subscriber_id|g4it_subscriber|id|

{{% /expand %}}
### digital_service 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table defines the list of digital services and characteristics associated. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**uid**|varchar||
|name|varchar||
|last_calculation_date|timestamp||
|creation_date|timestamp||
|last_update_date|timestamp||
|*note_id*|int8||
|criteria|_varchar||
|is_new_arch|bool||
|*user_id*|int8||
|*organization_id*|int8||

#### Primary Key 

 - uid
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|note_id|note|id|
|organization_id|g4it_organization|id|
|user_id|g4it_user|id|

{{% /expand %}}
### digital_service_link 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table stores the link generated from a digital services and its characteristics like expiration date. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**uid**|varchar||
|*digital_service_uid*|varchar||
|expiration_date|timestamp||

#### Primary Key 

 - uid
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|digital_service_uid|digital_service|uid|

{{% /expand %}}
### digital_service_shared 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table stores the association between a user and a shared digital service. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|*user_id*|int8||
|*organization_id*|int8||
|*digital_service_uid*|varchar||

#### Primary Key 

 - id
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|digital_service_uid|digital_service|uid|
|organization_id|g4it_organization|id|
|user_id|g4it_user|id|

{{% /expand %}}

