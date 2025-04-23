---
title: 'Digital Service definition'
description: 'These tables are the datas available in the Digital Service module and their correspondance with the item for which impact datas are configured in G4IT'
weight: 6
---
## Entity relationship diagram 

```mermaid
erDiagram 

  ref_device_type {
    int8 id PK
    varchar description
    varchar reference
    text external_referential_description
    numeric lifespan
    text source
  }
  ref_item_type {
    int8 id PK
    varchar type UK
    varchar category
    varchar comment
    float8 default_lifespan
    bool is_server
    varchar source
    varchar ref_default_item
    varchar subscriber UK
    varchar version
  }
  ref_network_type {
    int8 id PK
    varchar description
    varchar reference
    text external_referential_description
    varchar type
    int4 annual_quantity_of_go
    varchar country
    text source
  }
  ref_server_host {
    int8 id PK
    text description
    varchar reference
    text external_referential_description
    varchar type
    int4 nb_of_vcpu
    int4 total_disk
    float8 lifespan
  }
``` 

## Tables 

### ref_device_type 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table lists the device type which can be selected for the Digital Service definition 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|description|varchar||
|reference|varchar||
|external_referential_description|text||
|lifespan|numeric||
|source|text||

#### Primary Key 

 - id
{{% /expand %}}
### ref_item_type 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table defines the correspondence between an equipment type in the inventory and an equipment in the reference inserted in G4IT. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|type|varchar||
|category|varchar||
|comment|varchar||
|default_lifespan|float8||
|is_server|bool||
|source|varchar||
|ref_default_item|varchar||
|subscriber|varchar||
|version|varchar||

#### Primary Key 

 - id
{{% /expand %}}
### ref_network_type 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table lists the network type which can be selected for the Digital Service definition 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|description|varchar||
|reference|varchar||
|external_referential_description|text||
|type|varchar||
|annual_quantity_of_go|int4||
|country|varchar||
|source|text||

#### Primary Key 

 - id
{{% /expand %}}
### ref_server_host 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table lists the server type which can be selected for the Digital Service definition 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|description|text||
|reference|varchar||
|external_referential_description|text||
|type|varchar||
|nb_of_vcpu|int4||
|total_disk|int4||
|lifespan|float8||

#### Primary Key 

 - id
{{% /expand %}}

