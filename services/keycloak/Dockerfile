# A example build step that downloads a JAR file from a URL and adds it to the providers directory
FROM quay.io/keycloak/keycloak:26.0.7

# Add the provider JAR file to the providers directory
COPY --chown=keycloak:keycloak extensions/ /opt/keycloak/providers/
COPY --chown=keycloak:keycloak themes/ /opt/keycloak/themes/
COPY --chown=keycloak:keycloak imports/ /opt/keycloak/data/import/

USER keycloak
