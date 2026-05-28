/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
// This file is required by karma.conf.js and loads recursively all the .spec and framework files

// Configure environment BEFORE any imports to prevent Keycloak initialization errors
// This must run before keycloak instance is created in custom-auth.service.ts
import { environment } from "./environments/environment";

if (!environment.keycloak) {
    (environment as any).keycloak = {
        enabled: "false",
        issuer: "http://test-issuer",
        realm: "test-realm",
        clientId: "test-client",
    };
}

import { getTestBed } from "@angular/core/testing";
import {
    BrowserDynamicTestingModule,
    platformBrowserDynamicTesting,
} from "@angular/platform-browser-dynamic/testing";
import "zone.js/testing";

// First, initialize the Angular testing environment.
getTestBed().initTestEnvironment(
    BrowserDynamicTestingModule,
    platformBrowserDynamicTesting(),
);
