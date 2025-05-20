---
title: 'Release v3.0'
weight: 10
---

## Overview

A new major version of G4IT has been released in production.
To simplify the architecture and be more eco-designed, we made some majors change to improve how we use NumEcoEval, the calculation engine.
It should not have any impact on the results and the design but performance has been greatly improved for files loading, evaluation and export.

## Content

### V3.0.0
**Major Changes**
- 958 | New Arch - Clean module information system and API in the back-end of G4IT
- 745 | New Arch - Clean module digital service and API in the back-end of G4IT
- 746 | New Arch - Clean database

**Minor Changes**
- 1043 | Deletion of digital services is not possible and set default criteria if no active criteria
- 1040 | Default domain and subdomain for applications
- 803 | Pen test : Content spoofing
- 1042 | versions not displayed in ui
- 1044 | Details is not visible for network in bar graph in case of data consistency error in digital services
- 971 | Get country and electricity mix impact based on the new architecture

### V3.0.1
**Minor Changes**
- 1071 | Location has not been set during data migration for network
- 1051 | Organization name is displayed in equipment Type on the UI

## Installation Notes

