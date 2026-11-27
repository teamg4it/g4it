---
title: "Start G4IT Locally"
description: "GreenIT"
weight: 6
---
<!-- AI_DISCLAIMER v1.0 -->
> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Overview

**G4IT** is the service developed by **Sopra Steria** based on the **NumEcoEval calculation engine**.

More information on this subject can be found in the documentation, but in this page, it will only be about its
deployment on a developer station.

## Retrieving source code

Proposed path : **C:\G4IT\\_repo**

```shell
# open git bash
mkdir -p /c/G4IT/_repo
cd /c/G4IT/_repo
# G4IT code
git clone https://github.com/G4ITTeam/g4it.git

cd g4it
git pull

# Add submodules: 
#  - documentation theme
git submodule update --init --remote --recursive
```

### IntelliJ Workspace

Launch IntelliJ Idea, and open the folder **C:\G4IT\\_repo\g4it\\workspace**

The workspace comes with :

- Launch Configurations:
  - G4IT Backend
  - Shell scripts for :
    - Frontend start with VS Code
    - Podman-compose command
  - OpenSource Documentation

- Services:
  - frontend
  - backend
  - keycloak
  - documentation

## Start everything

### Start Prerequisites :rocket:

**NumEcoEval** needs kafka and postgresql to work.  
**G4IT** needs postgresql, keycloak, and NumEcoEval to work.

Start prerequisites :

- Open IntelliJ workspace
- Select the configuration to run `podman-compose up -d` or `python -m podman_compose up -d` and run it
  - it will start all containers

Verifications :

- Open dbeaver
  - Connect to the local database G4IT : localhost:5432/postgres (postgres:postgres)
    - with tables en_*, ind_*, ref_*
  - Connect to the local database Keycloak : localhost:5432/keycloak (postgres:postgres)
    - with internal keycloak tables

Possible errors :

- toomanyrequests when we use `podman-compose up -d` :

In podman desktop, there is a Settings button with Proxy/Registries menu :

Proxy : if you have a company proxy, set it here

Registries -> Docker hub : Login with your user account if you reach toomanyrequests limits

- api containers crash : can be the consequence of postgres port already used (5432).

### Start G4IT :rocket:

In IntelliJ > Run Configurations, select `G4IT Backend` and run it.  
In IntelliJ > Run Configurations, select `VS Code Frontend` and run it.

- On VS Code, open a terminal and execute in services/frontend :
  - npm i
  - npm start

Verifications :

- Open a browser to the url: [http://localhost:4200](http://localhost:4200)
- Log in with :
  - user: `admin@g4it.com`
  - password: `password`
- You should be able to navigate in G4IT

#### Debug with keycloak in local (optional)

- Make sure the keycloak podman container is started in local
- In services/backend/src/main/resources/application.yml, remove **nosecurity** in spring.profiles.active (restart backend)
- In services/frontend/environments/environment.ts, update keycloak.enabled to **"true"**
- :warning: Do not commit the changes :warning:

### Test G4IT features

#### Digital service

We are going to create a simple example to visualize graphs for digital service use case

- On the left menu, click on the icon which looks like @
- Click on **Evaluate new service**
- In **Terminals** table, add a **Device** with type **Laptop**
- In **Networks** table, add a **Network** with type **Fixed FR**
- In **Private Infrastructures** table, add a **non-cloud server** with default settings, the Host available
- In **Public Clouds - IaaS** table, add a **cloud service** with instance type **a1.medium** for **aws** cloud provider
- Click on CALCULATE
  - It will route to the **Global Vision**

#### Inventory

We are going to create a simple example to visualize graphs for inventory use case

- On the left menu, click on the icon below the icon @
- Click on **NEW INVENTORY**
- Select a month / year
- Click on CHOOSE A .CSV FILE. For each type, there is a sample file located in services/backend/src/main/resources/samples/inputs/
- Once the four types are filled, click on CREATE
- then, click on LAUNCH ESTIMATE
- then, navigate to EQUIPMENT and APPLICATION to see graphs

### Troubleshooting

#### Missing dependencies issue

Sometimes, the backend may not start because of a missing dependency.

- In IntelliJ, open the backend project
- Open the Maven tool window (View > Tool Windows > Maven)
- Clean the project (Lifecycle > clean)
- Click on the refresh button/ Sync All Maven Projects to update dependencies
- Click on compile (Lifecycle > compile) to build the project
- Then, try to run the backend again

![missing_dependency.png](../../missing_dependency.png)

#### Keycloak container not starting

If the Keycloak container fails to start, it may be due to image pull issues. Check the following:

- Ensure you have a stable internet connection.
- If you are behind a proxy, configure your proxy settings in Podman Desktop.
- Go to C:\G4IT\_repo\g4it\workspace\docker path.
- Try pulling the images manually using the command:

```bash
python -m podman_compose up -d
```

- If the issue persists, check the logs for more details:

```bash
python -m podman_compose logs keycloak
```
