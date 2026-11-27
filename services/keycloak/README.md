# Keycloak

A keycloak instance to manage user authentications for G4IT

## Export realm config

Open git bash in the `g4it` repository:

```shell
podman exec -it keycloak bash
/opt/bitnami/keycloak/bin/kc.sh export --file /tmp/dev-realm-export.json --realm g4it
exit
podman cp keycloak:/tmp/dev-realm-export.json services/keycloak/imports/dev-realm-export.json
```

## Upgrade Keycloak version

For upgrading the Keycloak version:

- Update the image tag in :
  - services/keycloak/Dockerfile
  - services/keycloak/.gitlab-ci.yml
  - workspace/numecoeval/docker-compose.yml
- Commit and push -> it will generate the new docker image

The new docker image can then be used in the deployment phase.
