---
title: 'Common References Tables'
description: 'These tables are references tables for environmental impact assessment : list of the stages of the life cycle, list of possible criteria in G4IT and table to convert to the "people equivalent" unit.'
weight: 5
---
## Entity relationship diagram 

```mermaid
erDiagram 

  ref_criterion {
    varchar code PK
    varchar label
    varchar description
    varchar unit
  }
  ref_lifecycle_step {
    varchar code PK
    varchar label
  }
  ref_sustainable_individual_package {
    varchar criteria
    float8 planetary_boundary
    text source
    float8 individual_sustainable_package
  }
``` 

## Tables 

### ref_criterion 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table defines the list of possible criteria for environmental impact assessment 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**code**|varchar||
|label|varchar||
|description|varchar||
|unit|varchar||

#### Primary Key 

 - code
{{% /expand %}}
### ref_lifecycle_step 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table lists the stages of the life cycle of an equipment as defined in the life cycle analysis (LCA) methodology. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|**code**|varchar||
|label|varchar||

#### Primary Key 

 - code
{{% /expand %}}
### ref_sustainable_individual_package 

{{% expand title="Show details" expanded="false" center="true"%}} 

#### Comments 

 - This table defines for each criteria the conversion ratio to be taken into account to convert from the criterion unit to the "people equivalent" unit. 

#### Columns 

|Name|Data type|Comments|
|---|---|---|
|criteria|varchar||
|planetary_boundary|float8||
|source|text||
|individual_sustainable_package|float8||

{{% /expand %}}

