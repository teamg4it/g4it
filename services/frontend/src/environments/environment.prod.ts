/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
// Production env variable are replaced before nginx start
export const environment = {
    production: true,
    apiBaseUrl: "$URL_INVENTORY",
    securedEndpoints: ["inventories", "digital-services", "template-files"],
    keycloak: {
        issuer: "${KEYCLOAK_URL}",
        realm: "g4it",
        clientId: "g4it",
        enabled: "${KEYCLOAK_ENABLED}",
    },
    frontEndUrl: "${FRONTEND_URL}",
    showBetaFeatures: "${SHOW_BETA_FEATURES}",
    matomo: {
        matomoTagManager: {
            containerUrl: "${MATOMO_TAG_MANAGER_URL}",
        },
    },
};
