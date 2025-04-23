---
title: 'Correspondences for Impact Calculation'
description: 'These tables are the tables taken into account to make the correspondance between the entry datas in the inventory and the items for which impact datas are configured in G4IT. Before to launch the calculation, some hypothesis are applied like PUE, device lifetime, BYOD usage rate and also COPE usage rate.'
weight: 7
---
## Entity relationship diagram 

```mermaid
erDiagram 

  ref_hypothesis {
    int8 id PK
    varchar code UK
    varchar source
    float8 value
    varchar description
    varchar subscriber UK
    varchar version
  }
  ref_item_impact {
    int8 id PK
    varchar criterion UK
    varchar lifecycle_step UK
    varchar name UK
    varchar category
    float8 avg_electricity_consumption
    varchar description
    varchar location
    varchar level
    varchar source
    varchar tier
    varchar unit
    float8 value
    varchar subscriber UK
    varchar version
  }
  ref_matching_item {
    int8 id PK
    varchar item_source UK
    varchar ref_item_target
    varchar subscriber UK
  }
``` 

## Tables 

### ref_hypothesis 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table defines default values such as PUE, device lifetime, BYOD usage rate (rate of employees using their own work devices to access company resources) and also COPE usage rate (rate of employees using company-provided mobile devices for personal use) 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|code|varchar||
|source|varchar||
|value|float8||
|description|varchar||
|subscriber|varchar||
|version|varchar||

#### Primary Key 

 - id
{{% /expand %}}
### ref_item_impact 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table defines the impact factor as well as the average electrical consumption to be taken into account for each piece of equipment depending on the desired criterion and the stage of the life cycle. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|criterion|varchar||
|lifecycle_step|varchar||
|name|varchar||
|category|varchar||
|avg_electricity_consumption|float8||
|description|varchar||
|location|varchar||
|level|varchar||
|source|varchar||
|tier|varchar||
|unit|varchar||
|value|float8||
|subscriber|varchar||
|version|varchar||

#### Primary Key 

 - id
{{% /expand %}}
### ref_matching_item 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table defines the correspondence between an equipment model in the inventory and an equipment in the reference inserted in G4IT. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**id**|int8||
|item_source|varchar||
|ref_item_target|varchar||
|subscriber|varchar||

#### Primary Key 

 - id
{{% /expand %}}

