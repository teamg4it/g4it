/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";
import { DigitalServiceManageVersionComponent } from "./digital-service-manage-version/digital-service-manage-version.component";
const routes: Routes = [
    {
        path: "",
        component: DigitalServiceManageVersionComponent,
        resolve: {
            title: TitleResolver,
        },
        data: {
            titleKey: "digital-services.version.versions-management",
        },
    },
];

export const digitalServicesManageVersionsRouter = RouterModule.forChild(routes);
