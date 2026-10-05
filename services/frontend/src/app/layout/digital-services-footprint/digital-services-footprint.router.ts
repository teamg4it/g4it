/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";

const titleResolveObject = {
    resolve: {
        title: TitleResolver,
    },
    data: {
        titleKey: "digital-services.page-title",
    },
};

const ecomindTitleResolveObject = {
    resolve: {
        title: TitleResolver,
    },
    data: {
        titleKey: "welcome-page.eco-mind-ai.title",
    },
};

// Angular 21: Export routes array for lazy loading with standalone components
export const DIGITAL_SERVICES_FOOTPRINT_ROUTES: Routes = [
    {
        path: "",
        loadComponent: () =>
            import("./digital-services-footprint.component").then(
                (m) => m.DigitalServicesFootprintComponent,
            ),
        children: [
            {
                path: "dashboard",
                loadComponent: () =>
                    import(
                        "./digital-services-footprint-dashboard/digital-services-footprint-dashboard.component"
                    ).then((m) => m.DigitalServicesFootprintDashboardComponent),
            },
            {
                path: "resources",
                loadComponent: () =>
                    import(
                        "./digital-services-resources/digital-services-resources.component"
                    ).then((m) => m.DigitalServicesResourcesComponent),
                ...titleResolveObject,
                children: [
                    {
                        path: "panel-create",
                        loadComponent: () =>
                            import(
                                "./digital-services-servers/side-panel/create-server/create-server.component"
                            ).then((m) => m.PanelCreateServerComponent),
                        ...titleResolveObject,
                    },
                    {
                        path: "panel-parameters",
                        loadComponent: () =>
                            import(
                                "./digital-services-servers/side-panel/server-parameters/server-parameters.component"
                            ).then((m) => m.PanelServerParametersComponent),
                        ...titleResolveObject,
                    },
                    {
                        path: "panel-vm",
                        loadComponent: () =>
                            import(
                                "./digital-services-servers/side-panel/list-vm/list-vm.component"
                            ).then((m) => m.PanelListVmComponent),
                        ...titleResolveObject,
                    },
                ],
            },
            {
                path: "ecomind-parameters",
                loadComponent: () =>
                    import(
                        "./digital-services-ecomind-parameters/digital-services-ecomind-parameters.component"
                    ).then((m) => m.DigitalServicesEcomindParametersComponent),
                ...ecomindTitleResolveObject,
            },

            {
                path: "",
                redirectTo: "dashboard",
                pathMatch: "full",
            },
        ],
    },
];
