/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
// This file replace the environment.ts file for tests.
// The list of file replacements can be found in `angular.json`.

export const environment = {
    production: false,
    securedEndpoints: ["inventories", "digital-services", "template-files"],
    apiBaseUrl: "",
    frontEndUrl: "http://localhost:4200",
    keycloak: {
        issuer: "",
        realm: "g4it",
        clientId: "g4it",
        enabled: "false",
    },
    showBetaFeatures: "true",
    matomo: {
        matomoTagManager: {
            containerUrl: "",
        },
    },
};
