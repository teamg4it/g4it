---
title: 'Release v3.12.1'
description: 'Performance optimizations for Inventory calculations, workspace reference data enhancements, average lifespan calculation fix, and Equipment view stability improvements.'
weight: 136
---

## Overview

Release v3.12.1 focuses on **performance, scalability, and calculation accuracy** following the introduction of workspace-specific reference data in v3.12.

This version significantly improves the performance of Inventory impact calculations, resolves issues affecting average lifespan computation, and fixes stability problems encountered in the Equipment view. Additional optimizations have been implemented to ensure workspace-level reference data management scales efficiently across large datasets while maintaining platform responsiveness.

Overall, this release improves reliability, calculation accuracy, and user experience when working with large inventories and simulations.

---

### Inventory Calculation Performance Improvements

Major optimizations have been implemented to improve the performance and scalability of impact calculations in the Inventory module.

#### Improvements

- Optimized calculation processing for large inventories
- Reduced execution times for impact calculations across all dataset sizes
- Improved handling of workspace-specific reference data during calculations
- Enhanced background processing to prevent resource contention
- Improved platform responsiveness during long-running operations

#### Benefits

- Faster impact calculation execution
- Better scalability for large inventories containing hundreds of thousands of equipment records
- Improved performance for medium and small datasets
- Reduced risk of operations remaining in a pending state
- Better support for concurrent platform usage

---

### Workspace Reference Data Performance Optimization

Following the introduction of workspace-level reference data management in v3.12, several optimizations have been implemented to improve performance when workspace-specific reference datasets are used.

#### Improvements

- Optimized lookup and matching logic for workspace reference data
- Reduced overhead during impact calculation processing
- Improved handling of large reference datasets
- Enhanced scalability of workspace-specific calculations

---

### Average Lifespan Calculation Fix

An issue affecting average lifespan calculations has been resolved.

#### Previous Behavior

In specific scenarios where equipment datasets contained both:

- Purchase date
- Retirement date

the calculated average lifespan could produce incorrect values despite valid input data and correct impact calculations.

#### Resolution

- Corrected the average lifespan computation logic
- Improved handling of equipment age calculations across datasets
- Ensured consistency between input data and reported average lifespan values

Users can now rely on accurate average lifespan indicators and reporting results.

---

### Equipment View Stability Improvements

A stability issue affecting the Equipment view page has been resolved.

#### Improvements

- Fixed breakdown and rendering issues encountered in Equipment view pages
- Improved page reliability when displaying large equipment datasets
- Enhanced overall user experience and navigation within equipment results

---

## 3.12.1

### Major Changes

- 2181 | Severe performance degradation and blocking during impact calculation after introduction of workspace-specific referential

### Minor Changes

- 2205 | Average lifespan calculation fix

---

## Installation Notes

