---
title: "services"
description: "Explaination of the types of services"
date:  2023-12-28T08:20:38+01:00
weight: 30
---

The application G4IT uses two types of services, one for the management of the data (business services) and one for the 
API calls (data services).

### the business services

You can find it in the `app/core/service/business` directory.

The goal of this service is to transform the data we receive from the backend. It means, it's more of a functional 
service to adapt the data to what we need. It is also in this service, that we will call the initialization and update 
methods of the stores. 

### The data services

You can find it in the `app/core/service/data` directory.

The goal of this service is to retrieve the data from the backend via the API. It means, that its only purpose is to 
call the API. In order to do so, we use the HttpClient provided by Angular, and we just have to give the right 
parameters to make the call.
