---
title: "Enable Ecomind module on G4IT"
description: "How to enable Ecomind module on G4IT"
weight: 3
---

### Locally

- In frontend : 
In the environment.ts file, go to the isEcomindEnabled variable :
if the value is at true then Ecomind module is enabled for the frontend, if the value is at false then Ecomind module is disabled for the frontend


- In backend : 
In the application.yml file, go to the ecomindaimodule.enabled variable : 
if the value is at true then Ecomind module is enabled for the backend, if the value is at false then Ecomind module is disabled for the backend

Make sure that both variables are set to the same value !  

### Deployment

- In the values.yaml file go to the <ENV_VAR> variable :
if the value is at true then Ecomind module is enabled, if the value is at false then Ecomind module is disabled
