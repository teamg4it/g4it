/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";
import { DigitalServicesAiInfrastructureComponent } from "./digital-services-ai-infrastructure/digital-services-ai-infrastructure.component";
import { DigitalServicesAiParametersComponent } from "./digital-services-ai-parameters/digital-services-ai-parameters.component";
import { DigitalServicesFootprintDashboardComponent } from "./digital-services-footprint-dashboard/digital-services-footprint-dashboard.component";
import { DigitalServicesFootprintComponent } from "./digital-services-footprint.component";
import { DigitalServicesResourcesComponent } from "./digital-services-resources/digital-services-resources.component";
import { PanelCreateServerComponent } from "./digital-services-servers/side-panel/create-server/create-server.component";
import { PanelListVmComponent } from "./digital-services-servers/side-panel/list-vm/list-vm.component";
import { PanelServerParametersComponent } from "./digital-services-servers/side-panel/server-parameters/server-parameters.component";

const titleResolveObject = {
    resolve: {
        title: TitleResolver,
    },
    data: {
        titleKey: "digital-services.page-title",
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
                ...titleResolveObject,
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
                path: "infrastructure",
                component: DigitalServicesAiInfrastructureComponent,
                ...titleResolveObject,
            },

            {
                path: "AiParameters",
                component: DigitalServicesAiParametersComponent,
                ...titleResolveObject,
            },

            {
                path: "",
                redirectTo: "dashboard",
                pathMatch: "full",
            },
        ],
    },
];

export const digitalServicesFootprintRouter = RouterModule.forChild(routes);
