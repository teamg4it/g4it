# G4IT INSTALLATION WITH DOCKER-COMPOSE

## Prerequisites

- We use Podman Desktop with podman-compose command to build and run components (in commands, podman <-> docker)
- Clone repository locally.
- Open a command prompt or bash inside the g4it folder (repository)

## Build docker images locally

Run each build section from the repository root. The commands below load
`G4IT_TAG` from `workspace/docker/.env` to match the image names used by
`docker-compose-all.yml`. This file must be trusted and use Bash-compatible
assignment syntax (`KEY=value`), as it is sourced in a subshell.

Both Dockerfiles compile the application inside their build stage, so no local
Maven or Node.js installation or separate compilation is required.

### BACKEND

```bash
(
    . ./workspace/docker/.env &&
    podman build --format docker -f services/backend/Dockerfile -t "g4it-backend:${G4IT_TAG:?G4IT_TAG must be set in workspace/docker/.env}" services/backend
)
```

It should build a docker image locally named `localhost/g4it-backend:<G4IT_TAG>`.

### FRONTEND

```bash
(
    . ./workspace/docker/.env &&
    podman build --format docker -f services/frontend/Dockerfile -t "g4it-frontend:${G4IT_TAG:?G4IT_TAG must be set in workspace/docker/.env}" services/frontend
)
```

It should build a docker image locally named `localhost/g4it-frontend:<G4IT_TAG>`.

### Hosts

In file `C:\Windows\System32\drivers\etc\hosts` or `/etc/hosts`, add line : 127.0.0.1 keycloak

### Run components

```bash
cd workspace/docker
podman-compose -f docker-compose-all.yml up -d
```

### Verify

Go to <http://localhost:4200>, it will redirect to keycloak login.
A built-in user already exists:

- username: <admin@g4it.com>
- password: password

### Grant Subscriber admin role

Connect on keycloak admin console, got to <http://keycloak:8180/auth/admin/>

- username: admin
- password: password

The <admin@g4it.com> user must automatically have subscriber admin role on SOPRA-STERIA-GROUP subscriber.
Then, refresh the page localhost:4200.

### Note

- I had an issue with Keycloak "iss" validation as it forces to have the same URL for issuer.
- As Keycloak is inside docker:
  - the user is calling from outside docker to get Keycloak connection
  - the backend is calling from inside docker to validate issuer
