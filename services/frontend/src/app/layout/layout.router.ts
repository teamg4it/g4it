/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { LayoutComponent } from "./layout.component";

const routes: Routes = [
    {
        path: "",
        component: LayoutComponent,
        children: [
            {
                path: "digital-services",
                loadChildren: () =>
                    import("./digital-services/digital-services.module").then(
                        (modules) => modules.DigitalServicesModule,
                    ),
            },

            {
                path: "eco-mind-ai",
                data: { isIa: true },
                loadChildren: () =>
                    import("./digital-services/digital-services.module").then(
                        (modules) => modules.DigitalServicesModule,
                    ),
            },

            {
                path: "eco-mind-ai/:digitalServiceVersionId/footprint",
                loadChildren: () =>
                    import(
                        "./digital-services-footprint/digital-services-footprint.module"
                    ).then((modules) => modules.DigitalServicesFootprintModule),
            },
            {
                path: "digital-service-version/:digitalServiceVersionId/footprint",
                loadChildren: () =>
                    import(
                        "./digital-services-footprint/digital-services-footprint.module"
                    ).then((modules) => modules.DigitalServicesFootprintModule),
            },
            {
                path: "digital-service-version/:digitalServiceVersionId/manage-versions",
                loadChildren: () =>
                    import(
                        "./digital-services-manage-versions/digital-services-manage-versions.module"
                    ).then((modules) => modules.DigitalServicesManageVersionsModule),
            },
            {
                path: "digital-service-version/:digitalServiceVersionId/compare-versions",
                loadChildren: () =>
                    import(
                        "./digital-services-compare-versions/digital-services-compare-versions.module"
                    ).then((modules) => modules.DigitalServicesCompareVersionsModule),
            },
            {
                path: "inventories",
                loadChildren: () =>
                    import("./inventories/inventories.module").then(
                        (modules) => modules.InventoriesModule,
                    ),
            },
            {
                path: "inventories/:inventoryId/footprint/application",
                loadChildren: () =>
                    import(
                        "./inventories-footprint/application/inventories-application-footprint.module"
                    ).then((modules) => modules.InventoriesApplicationFootprintModule),
            },
            {
                path: "inventories/:inventoryId/footprint",
                loadChildren: () =>
                    import("./inventories-footprint/inventories-footprint.module").then(
                        (modules) => modules.InventoriesFootprintModule,
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

export const layoutRouter = RouterModule.forChild(routes);
