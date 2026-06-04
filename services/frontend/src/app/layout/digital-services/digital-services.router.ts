/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Routes } from "@angular/router";

// Angular 21: Export routes array for lazy loading with standalone components
export const DIGITAL_SERVICES_ROUTES: Routes = [
    {
        path: "",
        loadComponent: () =>
            import("./digital-services.component").then(
                (m) => m.DigitalServicesComponent,
            ),
    },
];
