---
title: "stores"
description: "Explaination of the stores to stock informations"
date:  2023-12-28T08:20:38+01:00
weight: 10
---

The application G4IT uses four types of store, one for the graphs information, one for the filter, 
one for the inventory indicators and one for the inventory. In this project, we decided to use elf 
for the stores. 

All these files initialize the stores and provide methods to update them.

### the echarts (graph) store

You can find it in the `app/core/store/echarts.repository.ts` directory.

His role is to stock information for the equipment view. We can find four variables for each graph the unit and
to know if the data is initialized. It also calculates the sum for each impact on the graph.

In this case, we subscribe once to the stored information in the equipments view's components, and we just 
have to display the information via the Echarts options.

### The filter store

You can find it in the `app/core/store/filter.repository.ts` directory.

His role is to stock filters information for the equipment and application views. We can find stores  
for all the filters we receive from the backend, the selected filters (different for each view).

For the equipment view, we have :
- countries
- entities
- status 
- equipments

and for the application view, we have :
- environments
- types (of equipments)
- lifeCycles
- domains
- subdomains

In this case, we subscribe once to the stored information (received and selected filters) in the dataviz components
`app/layout/inventories-footprint/application/dataviz-filter-application` and `app/layout/inventories-footprint/dataviz-filter`
to display the filters.

### the indicator store

You can find it in the `app/core/store/footprint.repository.ts` directory.

His role is to stock indicators for the equipment and application view. In this store, we stock indicator according to 
its criteria, information we will need in the component (ex: on what level graph we are...), the impacts containing
information selected in filters (for the equipment view), ...

In this case, we subscribe once to the stored information in the components, and we just
have to display the information.

### the inventory store

You can find it in the `app/core/store/inventory.repository.ts` directory.

His role is to stock inventories information and which inventory is selected.

In this case, we subscribe once to the stored information in the components, so it helps us to display 
the correct information about the inventory.
