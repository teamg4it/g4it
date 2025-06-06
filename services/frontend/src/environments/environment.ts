/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
// This file can be replaced during build by using the `fileReplacements` array.
// If you modify values in this file, it will likely have no effect.
// The list of file replacements can be found in `angular.json`.

export const environment = {
    production: false,
    securedEndpoints: ["inventories", "digital-services", "template-files"],
    apiBaseUrl: "http://localhost:8080",
    keycloak: {
        issuer: "http://localhost:8180/auth",
        realm: "g4it",
        clientId: "g4it",
        enabled: "true",
    },
    frontEndUrl: "http://localhost:4200",
    showBetaFeatures: "true",
    matomo: {
        matomoTagManager: {
            containerUrl: "",
        },
    },
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 *
 * import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
 */
