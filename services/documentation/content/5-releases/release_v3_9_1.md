---
title: 'Release v3.9.1'
description: 'Major performance optimizations for Inventory and Digital Services, memory leak fixes, faster re-evaluations, and multiple stability and UX improvements.'
weight: 110
---

## Overview

Release v3.9.1 focuses on performance, scalability, and stability improvements, particularly for large Inventories and Digital Services.
This release resolves a critical memory leak issue, significantly reduces evaluation and re-evaluation execution times, and introduces several backend optimizations to ensure smoother processing at scale.
Additional fixes address data consistency, error handling, and UI refinements.

### Inventory memory leak fix (Out Of Memory)

A major refactoring of the Inventory evaluation flow was performed to stabilize memory usage and improve processing time for large inventories.

Key improvements include:

- Batch persistence instead of single bulk save
- Reuse of external API data
- Reduced object creation inside loops
- New transaction per batch save
- Flush & clear ORM session after each batch
- Direct database updates for task progress
- Pre-calculation of shared metrics
- Optimized sorting and pagination logic
- Recreation of heavy maps instead of clear()
- These changes significantly improve stability and execution time when processing large inventories.



### Faster Digital Service re-evaluation on edit

Performance improvements were applied to the Digital Service re-evaluation flow when editing or adding extra resources.

Digital Service re-evaluation has been enhanced to deliver a faster, more responsive experience when editing or adding extra resources. The execution flow has been streamlined, enabling near-instant completion in most common scenarios and supported by improved logging for clearer processing insights. Making the process synchronous improves the Digital service more fluidity and user experience.


## Content

### V3.9.1

**Major Changes**

- 1838 | Code fix for Inventory memory leak (out of memory)
- 1845 | Code fix to reduce reevaluate time on digital service edit

**Minor Changes**

- 1892 | desynchronization of digital service criteria
- 1893 | greenItAnalysis style optimization
- 1890 | Sonar fixes
- 1832 | fix content spoofing to avoid any malicious text in request
- 1905 | fixImproper Error Management, avoid showing XML/technical error
- 1920 | Set Disk field default value to 1 in Shared Storage Server

## Installation Notes

