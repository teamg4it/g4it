---
title: "How to configure my local IDentity Provider Keycloak"
description: "The tools and local configuration necessary to work on G4IT"
weight: 7
---

## Overview

Keycloak is used in G4IT architecture as an authentication manager.

It manages in the G4IT context:
- Basic authentication 
- Identity provider proxy authentication
- Backend API authentication

## Configuration files

Keycloak configuration files are located in the G4IT repository, in services/keycloak

__Tree folder:__
- __extensions__ : home-idp-discovery extension
- __imports__ : auto-import the g4it realm with default configuration
- __themes__: custom theme for login
- __Dockerfile__: container build, it uses official image bitnami/keycloak

## Install the docker image

The __local dev environment__ uses podman-compose to start the keycloak instance (in `workspace/numecoeval/docker-compose.yml`)
- Docker image used: bitnami/keycloak
- URL : http://localhost:8180/auth
- It uses this [extension](https://github.com/sventorben/keycloak-home-idp-discovery) located in `extensions` folder

## Keycloak admin users

*Note: applicative roles are not configured inside Keycloak, but in the G4IT backend.*

On the first setup, all users have `password` as password

There are 2 admins users:
- `admin` : the global keycloak admin user
- `admin@g4it.com` : the application super admin, has the role ROLE_SUPER_ADMINISTRATOR

On startup, keycloak automatically imports the `g4it` realm config name __g4it-realm-export.json__. This realm has :
- 1 user: admin@g4it.com (Super Admin).
 
:warning: __SECURITY WARNING__ :warning:

In __deployed environment__, passwords must be changed after the first setup !

Connect to keycloak UI in admin realm : [http://localhost:8180/auth/admin/master/console](http://localhost:8180/auth/admin/master/console) (or deployed url)
- Go to Users > admin > Credentials (tab) > Reset password

Switch to __g4it realm__
- Go to Users > admin@g4it.com > Credentials (tab) > Reset password

:lock: To improve security, it is recommended to __deactivate the super admin user__ and activate it only when doing modifications:
- Connect to keycloak url, g4it realm > Users > admin@g4it.com > Click on Enabled toggle button


## Keycloak configurations

G4IT uses few keycloak configurations, most configurations are by default.

Here are the configurations used in __keycloak__ realm :
- Users : admin users

Here are the configurations used in __g4it__ realm :
- Clients: __g4it__ client with custom theme and redirect URL
- Users : g4it applicative users
- Realm settings : General, Login, Email, Localization
- Authentication : home-idp-discovery-flow
- Identity providers : none in local mode, add providers if needed


## Extension configuration

### home.idp.discovery.domains

On the extension used, it is possible to add a custom configuration for domains authorized on an identity provider.

```shell
# Go into the keycloak container

# Example for an identity provider named 'microsoft'

# Get the config
/opt/bitnami/keycloak/bin/kcadm.sh get identity-provider/instances/microsoft --server http://localhost:8080/auth -r g4it --user admin --realm master
# > Enter admin password

# Set the config for domains : example.com, test.com
/opt/bitnami/keycloak/bin/kcadm.sh update identity-provider/instances/microsoft --server http://localhost:8080/auth -r g4it --user admin --realm master -s 'config."home.idp.discovery.domains"="example.com##test.com"'
# > Enter admin password
```
