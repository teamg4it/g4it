---
title: '1.3 - Referential'
description: 'These tables are the referential with modification for Ecomind'
weight: 10
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
    bool compatible_ecomind
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
|compatible_ecomind|bool||

#### Primary Key

- id
  {{% /expand %}}

