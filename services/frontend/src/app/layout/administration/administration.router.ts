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
        titleKey: "administration.title",
    },
};

// Angular 21: Export routes array for lazy loading with standalone components
export const ADMINISTRATION_ROUTES: Routes = [
    {
        path: "",
        loadComponent: () =>
            import("./administration-panel/administration-panel.component").then(
                (m) => m.AdministrationPanelComponent,
            ),
        children: [
            {
                path: "organizations",
                loadComponent: () =>
                    import(
                        "./administration-panel/organizations/organizations.component"
                    ).then((m) => m.OrganizationsComponent),
                ...titleResolveObject,
            },
            {
                path: "users",
                loadComponent: () =>
                    import("./administration-panel/users/users.component").then(
                        (m) => m.UsersComponent,
                    ),
                ...titleResolveObject,
            },
            {
                path: "actions",
                loadComponent: () =>
                    import(
                        "./administration-panel/super-admin/super-admin.component"
                    ).then((m) => m.SuperAdminComponent),
                ...titleResolveObject,
            },
            {
                path: "update-referential",
                loadComponent: () =>
                    import(
                        "./administration-panel/update-reference/update-reference.component"
                    ).then((m) => m.UpdateReferenceComponent),
                ...titleResolveObject,
            },
            {
                path: "update-workspace-referential",
                loadComponent: () =>
                    import(
                        "./administration-panel/update-workspace-reference/update-workspace-reference.component"
                    ).then((m) => m.UpdateWorkspaceReferenceComponent),
                ...titleResolveObject,
            },
        ],
    },
];
