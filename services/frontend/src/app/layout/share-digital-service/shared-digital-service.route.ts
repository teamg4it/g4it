import { Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";
import { DigitalServicesFootprintDashboardComponent } from "../digital-services-footprint/digital-services-footprint-dashboard/digital-services-footprint-dashboard.component";
import { DigitalServicesResourcesComponent } from "../digital-services-footprint/digital-services-resources/digital-services-resources.component";
import { ShareDigitalServiceComponent } from "./share-digital-service.component";
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
        component: ShareDigitalServiceComponent,
        children: [
            {
                path: "",
                redirectTo: "resources",
                pathMatch: "full",
            },
            {
                path: "dashboard",
                component: DigitalServicesFootprintDashboardComponent,
                ...titleResolveObject,
            },
            {
                path: "resources",
                component: DigitalServicesResourcesComponent,
                ...titleResolveObject,
            },
        ],
    },
];
