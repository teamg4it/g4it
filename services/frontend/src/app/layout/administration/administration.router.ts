/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";
import { AdministrationPanelComponent } from "./administration-panel/administration-panel.component";
import { OrganizationsComponent } from "./administration-panel/organizations/organizations.component";
import { SuperAdminComponent } from "./administration-panel/super-admin/super-admin.component";
import { UsersComponent } from "./administration-panel/users/users.component";

const titleResolveObject = {
    resolve: {
        title: TitleResolver,
    },
    data: {
        titleKey: "administration.title",
    },
};

const routes: Routes = [
    {
        path: "",
        component: AdministrationPanelComponent,
        children: [
            {
                path: "organizations",
                component: OrganizationsComponent,
                ...titleResolveObject,
            },
            {
                path: "users",
                component: UsersComponent,
                ...titleResolveObject,
            },
            {
                path: "actions",
                component: SuperAdminComponent,
                ...titleResolveObject,
            },
        ],
    },
];

export const administrationRouter = RouterModule.forChild(routes);
