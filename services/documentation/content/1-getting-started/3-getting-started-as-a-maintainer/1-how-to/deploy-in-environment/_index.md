---
title: "Deploy configuration"
description: "The tools and local configuration necessary to work on G4IT"
date: 2023-12-21T14:28:06+01:00
weight: 3
---

## Overview

G4IT can be deployed as containers, co-located with NumEcoEval containers.

An example of docker-compose file is located in workspace/docker:
- docker-compose-all.yml
- shared-docker-compose.yml

## Build

Currently, G4IT images are not available in a public docker registry, so it is needed to build frontend and backend images locally.

### Backend commands:

*can be executed from IntelliJ Maven view*
```
mvn -P SKIP-ALL-TEST clean package
```

```
cd services/backend
podman build . -t g4it-backend -f Dockerfile-opensource
```

### Frontend commands:

```
cd services/frontend
npm install
npm run build
podman build . -t g4it-frontend -f Dockerfile-opensource
```

## Install

```
# check g4it-backend and g4it-frontend images are created
podman images

# start all
cd workspace/docker
podman-compose -f docker-compose-all.yml up -d
```

Connect to the UI url : http://localhost:4200
- Login with `admin@g4it.com` user, default password is `password`

If G4IT is deployed in a secured environment, don't forget to change passwords.

## Configuration

### Auto configuration 

Once the application in started, automatic setups are executed :
- user admin@g4it.com is created in database, table g4it_user with super admin rights
- a subscriber/organization `SUBSCRIBER-DEMO/DEMO` is created
- open-source referential is uploaded and is located in ref_* tables

### Manual configuration

Specific referential data must be uploaded 

