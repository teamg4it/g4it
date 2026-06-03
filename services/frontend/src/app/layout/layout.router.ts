/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Routes } from "@angular/router";

// Angular 21: Export routes array for use with provideRouter() or loadChildren
export const LAYOUT_ROUTES: Routes = [
    {
        path: "",
        loadComponent: () => import("./layout.component").then((m) => m.LayoutComponent),
        children: [
            {
                path: "digital-services",
                loadChildren: () =>
                    import("./digital-services/digital-services.router").then(
                        (m) => m.DIGITAL_SERVICES_ROUTES,
                    ),
            },

            {
                path: "eco-mind-ai",
                data: { isIa: true },
                loadChildren: () =>
                    import("./digital-services/digital-services.router").then(
                        (m) => m.DIGITAL_SERVICES_ROUTES,
                    ),
            },

            {
                path: "eco-mind-ai/:digitalServiceVersionId/footprint",
                loadChildren: () =>
                    import(
                        "./digital-services-footprint/digital-services-footprint.router"
                    ).then((m) => m.DIGITAL_SERVICES_FOOTPRINT_ROUTES),
            },
            {
                path: "digital-service-version/:digitalServiceVersionId/footprint",
                loadChildren: () =>
                    import(
                        "./digital-services-footprint/digital-services-footprint.router"
                    ).then((m) => m.DIGITAL_SERVICES_FOOTPRINT_ROUTES),
            },
            {
                path: "eco-mind-ai/:digitalServiceVersionId/manage-versions",
                loadChildren: () =>
                    import(
                        "./digital-services-manage-versions/digital-services-manage-versions.router"
                    ).then((m) => m.DIGITAL_SERVICES_MANAGE_VERSIONS_ROUTES),
            },
            {
                path: "digital-service-version/:digitalServiceVersionId/manage-versions",
                loadChildren: () =>
                    import(
                        "./digital-services-manage-versions/digital-services-manage-versions.router"
                    ).then((m) => m.DIGITAL_SERVICES_MANAGE_VERSIONS_ROUTES),
            },
            {
                path: "eco-mind-ai/:digitalServiceVersionId/compare-versions",
                loadChildren: () =>
                    import(
                        "./digital-services-compare-versions/digital-services-compare-versions.router"
                    ).then((m) => m.DIGITAL_SERVICES_COMPARE_VERSIONS_ROUTES),
            },
            {
                path: "digital-service-version/:digitalServiceVersionId/compare-versions",
                loadChildren: () =>
                    import(
                        "./digital-services-compare-versions/digital-services-compare-versions.router"
                    ).then((m) => m.DIGITAL_SERVICES_COMPARE_VERSIONS_ROUTES),
            },
            {
                path: "inventories",
                loadChildren: () =>
                    import("./inventories/inventories.router").then(
                        (m) => m.INVENTORIES_ROUTES,
                    ),
            },
            {
                path: "inventories/:inventoryId/footprint/application",
                loadChildren: () =>
                    import(
                        "./inventories-footprint/application/inventories-application-footprint.router"
                    ).then((m) => m.INVENTORIES_APPLICATION_FOOTPRINT_ROUTES),
            },
            {
                path: "inventories/:inventoryId/footprint",
                loadChildren: () =>
                    import("./inventories-footprint/inventories-footprint.router").then(
                        (m) => m.INVENTORIES_FOOTPRINT_ROUTES,
                    ),
            },
            {
                path: "**",
                redirectTo: "/",
            },
        ],
    },
    {
        path: "**",
        redirectTo: "",
    },
];
