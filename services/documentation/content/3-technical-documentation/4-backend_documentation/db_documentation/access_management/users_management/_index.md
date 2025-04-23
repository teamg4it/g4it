---
title: 'Users Management'
description: 'These tables present the structure of Subscriber (customer of the plateform), Organization (Subscriber-defined structuring element) and User (Person authorized who accesses G4IT’s modules). Role are put in place : Administrator (at Subcriber level and at Organization level), Read/Write on G4IT’s modules (information system and/or digital services).'
weight: 1
---
## Entity relationship diagram 

```mermaid
erDiagram 

  g4it_role {
    int8 id PK
    varchar name UK
  }
  g4it_subscriber {
    int8 id PK
    varchar name UK
    timestamp creation_date
    timestamp last_update_date
    int4 storage_retention_day_export
    int4 storage_retention_day_output
    int4 data_retention_day
    text authorized_domains
    _varchar criteria
  }
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
  g4it_user_subscriber {
    int8 id PK
    int8 user_id FK
    int8 subscriber_id FK
    bool default_flag
  }
  g4it_user_organization {
    int8 id PK
    int8 user_id FK
    int8 organization_id FK
    bool default_flag
  }
  g4it_user_role_subscriber {
    int8 id PK
    int8 user_subscriber_id FK
    int8 role_id FK
  }
  g4it_user_role_organization {
    int8 id PK
    int8 user_organization_id FK
    int8 role_id FK
  }
  g4it_role ||--o{ g4it_user_role_organization : "foreign key"
  g4it_role ||--o{ g4it_user_role_subscriber : "foreign key"
  g4it_subscriber ||--o{ g4it_organization : "foreign key"
  g4it_subscriber ||--o{ g4it_user_subscriber : "foreign key"
  g4it_user ||--o{ g4it_organization : "foreign key"
  g4it_user ||--o{ g4it_user_organization : "foreign key"
  g4it_user ||--o{ g4it_user_subscriber : "foreign key"
  g4it_organization ||--o{ g4it_user_organization : "foreign key"
  g4it_user_subscriber ||--o{ g4it_user_role_subscriber : "foreign key"
  g4it_user_organization ||--o{ g4it_user_role_organization : "foreign key"
``` 

## Tables 

### g4it_role 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table defines the possible roles on G4IT and for each module. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|name|varchar||

#### Primary Key 

 - id
{{% /expand %}}
### g4it_subscriber 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table defines the list of the existing subscriber (customer of the platform) and configuration associated. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|name|varchar||
|creation_date|timestamp||
|last_update_date|timestamp||
|storage_retention_day_export|int4||
|storage_retention_day_output|int4||
|data_retention_day|int4||
|authorized_domains|text||
|criteria|_varchar||

#### Primary Key 

 - id
{{% /expand %}}
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
### g4it_user_subscriber 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table defines the association between user and subscriber. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|*user_id*|int8||
|*subscriber_id*|int8||
|default_flag|bool||

#### Primary Key 

 - id
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|subscriber_id|g4it_subscriber|id|
|user_id|g4it_user|id|

{{% /expand %}}
### g4it_user_organization 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table defines the association between user and organization. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|*user_id*|int8||
|*organization_id*|int8||
|default_flag|bool||

#### Primary Key 

 - id
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|organization_id|g4it_organization|id|
|user_id|g4it_user|id|

{{% /expand %}}
### g4it_user_role_subscriber 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table defines the correspondence between an equipment type in the inventory and an equipment in the reference inserted in G4IT. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|*user_subscriber_id*|int8||
|*role_id*|int8||

#### Primary Key 

 - id
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|role_id|g4it_role|id|
|user_subscriber_id|g4it_user_subscriber|id|

{{% /expand %}}
### g4it_user_role_organization 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - That table defines the role of the user for a organization. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|*user_organization_id*|int8||
|*role_id*|int8||

#### Primary Key 

 - id
#### Foreign keys
|Column name|Referenced table|Referenced primary key|
|---|---|---|
|role_id|g4it_role|id|
|user_organization_id|g4it_user_organization|id|

{{% /expand %}}

