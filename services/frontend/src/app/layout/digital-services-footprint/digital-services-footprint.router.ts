/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";
import { DigitalServicesEcomindParametersComponent } from "./digital-services-ecomind-parameters/digital-services-ecomind-parameters.component";
import { DigitalServicesFootprintDashboardComponent } from "./digital-services-footprint-dashboard/digital-services-footprint-dashboard.component";
import { DigitalServicesFootprintComponent } from "./digital-services-footprint.component";
import { DigitalServicesResourcesComponent } from "./digital-services-resources/digital-services-resources.component";
import { PanelCreateServerComponent } from "./digital-services-servers/side-panel/create-server/create-server.component";
import { PanelListVmComponent } from "./digital-services-servers/side-panel/list-vm/list-vm.component";
import { PanelServerParametersComponent } from "./digital-services-servers/side-panel/server-parameters/server-parameters.component";
import { DigitalServicesRecommendationsComponent } from "./digital-services-recommendations/digital-services-recommendations.component";

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

const routes: Routes = [
    {
        path: "",
        component: DigitalServicesFootprintComponent,
        children: [
            {
                path: "dashboard",
                component: DigitalServicesFootprintDashboardComponent,
            },
            {
                path: "resources",
                component: DigitalServicesResourcesComponent,
                ...titleResolveObject,
                children: [
                    {
                        path: "panel-create",
                        component: PanelCreateServerComponent,
                        ...titleResolveObject,
                    },
                    {
                        path: "panel-parameters",
                        component: PanelServerParametersComponent,
                        ...titleResolveObject,
                    },
                    {
                        path: "panel-vm",
                        component: PanelListVmComponent,
                        ...titleResolveObject,
                    },
                ],
            },
            {
                path: "ecomind-parameters",
                component: DigitalServicesEcomindParametersComponent,
                ...ecomindTitleResolveObject,
            },

            {
                path: "",
                redirectTo: "dashboard",
                pathMatch: "full",
            },
             {
                path: "recommendations",
                component: DigitalServicesRecommendationsComponent,
                ...titleResolveObject,
            },
        ],
    },
];

export const digitalServicesFootprintRouter = RouterModule.forChild(routes);
