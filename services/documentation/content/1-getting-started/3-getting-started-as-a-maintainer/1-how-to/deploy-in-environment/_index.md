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

## Build and install

```shell
cd workspace/docker
podman-compose -f docker-compose-all.yml build
podman-compose -f docker-compose-all.yml up -d
```

This builds the frontend and backend using their Dockerfiles, then starts the stack.

Open [http://localhost:4200](http://localhost:4200) and sign in as `admin@g4it.com` with
the default password `password`.

If G4IT is deployed in a secured environment, don't forget to change passwords.

## Maven troubleshooting

For a manual backend build, run Maven from `services/backend`:

```shell
cd services/backend
mvn -P SKIP-ALL-TEST clean package
```

An HTTP `429 Too Many Requests` while downloading dependencies may be a temporary Maven
Central rate limit. Wait and retry, or try another network.
