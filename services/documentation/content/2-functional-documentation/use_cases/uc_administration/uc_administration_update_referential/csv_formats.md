---
title: "Referential Formats"
description: "Detailed description of CSV headers for each referential type."
weight: 10
---

When updating referential data, the CSV files must follow a specific structure with exact headers. The delimiter used is the **semicolon (`;`)**.

## Criterion

**Endpoint**: `criterion`

| Header        | Description                            |
| ------------- | -------------------------------------- |
| `code`        | Unique identifier for the criterion.   |
| `label`       | Human-readable name of the criterion.  |
| `description` | Detailed description of the criterion. |
| `unit`        | Unit of measure for the impact.        |

## Lifecycle Step

**Endpoint**: `lifecycleStep`

| Header  | Description                                |
| ------- | ------------------------------------------ |
| `code`  | Unique identifier for the lifecycle step.  |
| `label` | Human-readable name of the lifecycle step. |

## Hypothesis

**Endpoint**: `hypothesis`

| Header        | Description                                                   |
| ------------- | ------------------------------------------------------------- |
| `code`        | Unique identifier for the hypothesis.                         |
| `source`      | Source of the data/hypothesis.                                |
| `value`       | Numerical value of the hypothesis.                            |
| `description` | Detailed description.                                         |
| `subscriber`  | The organization name (must match your current organization). |
| `version`     | Versioning information.                                       |

## Item Type

**Endpoint**: `itemType`

| Header             | Description                                                   |
| ------------------ | ------------------------------------------------------------- |
| `type`             | Type of equipment (e.g., Server, Laptop).                     |
| `category`         | Higher-level category.                                        |
| `comment`          | Additional comments.                                          |
| `default_lifespan` | Default lifespan in years.                                    |
| `is_server`        | Boolean indicating if it's a server.                          |
| `source`           | Source of the data.                                           |
| `ref_default_item` | Default reference item.                                       |
| `subscriber`       | The organization name (must match your current organization). |
| `version`          | Versioning information.                                       |

## Item Impact

**Endpoint**: `itemImpact`

| Header                        | Description                                                   |
| ----------------------------- | ------------------------------------------------------------- |
| `criterion`                   | The code of the associated criterion.                         |
| `lifecycle_step`              | The code of the associated lifecycle step.                    |
| `name`                        | Name of the impact factor.                                    |
| `category`                    | Category of the impact.                                       |
| `avg_electricity_consumption` | Average electricity consumption.                              |
| `description`                 | Detailed description.                                         |
| `location`                    | Geographical location.                                        |
| `level`                       | Level of granularity.                                         |
| `source`                      | Source of the data.                                           |
| `tier`                        | Tier level.                                                   |
| `unit`                        | Unit of measure.                                              |
| `value`                       | Impact value.                                                 |
| `subscriber`                  | The organization name (must match your current organization). |
| `version`                     | Versioning information.                                       |

## Matching Item

**Endpoint**: `matchingItem`

| Header            | Description                                                   |
| ----------------- | ------------------------------------------------------------- |
| `item_source`     | The source item name or classification.                       |
| `ref_item_target` | The target referential item it maps to.                       |
| `subscriber`      | The organization name (must match your current organization). |
