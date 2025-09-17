import { Routes } from "@angular/router";
import { DigitalServicesFootprintDashboardComponent } from "../digital-services-footprint/digital-services-footprint-dashboard/digital-services-footprint-dashboard.component";
import { DigitalServicesResourcesComponent } from "../digital-services-footprint/digital-services-resources/digital-services-resources.component";
import { ShareDigitalServiceComponent } from "./share-digital-service.component";

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
                data: { isShared: true },
            },
            {
                path: "resources",
                component: DigitalServicesResourcesComponent,
                data: { isShared: true },
            },
        ],
    },
];
