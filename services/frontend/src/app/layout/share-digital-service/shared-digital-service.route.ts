import { Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";
const titleResolveObject = {
    resolve: {
        title: TitleResolver,
    },
    data: {
        titleKey: "digital-services.page-title",
        isShared: true,
    },
};

export const shareDsRoutes: Routes = [
    {
        path: "",
        loadComponent: () =>
            import("./share-digital-service.component").then(
                (m) => m.ShareDigitalServiceComponent,
            ),
        children: [
            {
                path: "",
                redirectTo: "resources",
                pathMatch: "full",
            },
            {
                path: "dashboard",
                loadComponent: () =>
                    import(
                        "../digital-services-footprint/digital-services-footprint-dashboard/digital-services-footprint-dashboard.component"
                    ).then((m) => m.DigitalServicesFootprintDashboardComponent),
                ...titleResolveObject,
            },
            {
                path: "resources",
                loadComponent: () =>
                    import(
                        "../digital-services-footprint/digital-services-resources/digital-services-resources.component"
                    ).then((m) => m.DigitalServicesResourcesComponent),
                ...titleResolveObject,
            },
        ],
    },
];
