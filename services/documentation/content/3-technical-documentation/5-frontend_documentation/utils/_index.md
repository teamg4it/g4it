---
title: "utils"
description: "utils methods to all the views"
date:  2023-12-28T08:20:38+01:00
weight: 40
---

The application G4IT uses `criteria.ts` file to convert the criteria name to its long, short or normal version.
But also to provide the unit.

### criteria.ts

You can find it in the `app/core/utils` directory.

The goal of this file is to transform the criteria name to the version we need and depending on what the backend
sends. These conversion are really important to make sure that the selected criteria (tab selected) and the 
criteria in the indicators match. This allows the application to display the indicators.
