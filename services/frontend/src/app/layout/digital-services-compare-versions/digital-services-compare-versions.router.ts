/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";
import { DigitalServicesCompareVersionsComponent } from "./digital-services-compare-versions.component";

const routes: Routes = [
    {
        path: "",
        component: DigitalServicesCompareVersionsComponent,
        resolve: {
            title: TitleResolver,
        },
        data: {
            titleKey: "digital-services.version.compare-versions-title",
        },
    },
];

export const digitalServicesCompareVersionsRouter = RouterModule.forChild(routes);
