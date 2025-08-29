/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { TitleResolver } from "./common/title-resolver.service";
import { LayoutComponent } from "./layout.component";

const routes: Routes = [
    {
        path: "",
        component: LayoutComponent,
        children: [
            {
                path: "digital-services",
                resolve: {
                    title: TitleResolver,
                },
                data: {
                    titleKey: "digital-services.page-title",
                },
                loadChildren: () =>
                    import("./digital-services/digital-services.module").then(
                        (modules) => modules.DigitalServicesModule,
                    ),
            },

            {
                path: "eco-mind-ai",
                data: { isIa: true, titleKey: "welcome-page.eco-mind-ai.title" },
                resolve: {
                    title: TitleResolver,
                },
                loadChildren: () =>
                    import("./digital-services/digital-services.module").then(
                        (modules) => modules.DigitalServicesModule,
                    ),
            },

            {
                path: "eco-mind-ai/:digitalServiceId/footprint",
                loadChildren: () =>
                    import(
                        "./digital-services-footprint/digital-services-footprint.module"
                    ).then((modules) => modules.DigitalServicesFootprintModule),
            },

            {
                path: "digital-services/:digitalServiceId/footprint",
                loadChildren: () =>
                    import(
                        "./digital-services-footprint/digital-services-footprint.module"
                    ).then((modules) => modules.DigitalServicesFootprintModule),
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
