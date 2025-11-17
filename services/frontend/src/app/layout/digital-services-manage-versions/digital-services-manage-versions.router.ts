/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { DigitalServiceManageVersionComponent } from "./digital-service-manage-version/digital-service-manage-version.component";
const routes: Routes = [
    {
        path: "",
        component: DigitalServiceManageVersionComponent,
    },
];

export const digitalServicesManageVersionsRouter = RouterModule.forChild(routes);
