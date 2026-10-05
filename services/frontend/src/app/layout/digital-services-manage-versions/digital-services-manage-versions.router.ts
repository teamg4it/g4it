/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";

// Angular 21: Export routes array for lazy loading with standalone components
export const DIGITAL_SERVICES_MANAGE_VERSIONS_ROUTES: Routes = [
    {
        path: "",
        loadComponent: () =>
            import(
                "./digital-service-manage-version/digital-service-manage-version.component"
            ).then((m) => m.DigitalServiceManageVersionComponent),
        resolve: {
            title: TitleResolver,
        },
        data: {
            titleKey: "digital-services.version.versions-management",
        },
    },
];
