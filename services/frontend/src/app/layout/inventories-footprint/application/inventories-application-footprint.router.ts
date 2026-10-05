/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Routes } from "@angular/router";
import { FootprintRedirectGuard } from "src/app/guard/footprint-redirect.guard";
import { TitleResolver } from "../../common/title-resolver.service";

// Angular 21: Export routes array for lazy loading with standalone components
export const INVENTORIES_APPLICATION_FOOTPRINT_ROUTES: Routes = [
    {
        path: ":criteria",
        loadComponent: () =>
            import("./inventories-application-footprint.component").then(
                (m) => m.InventoriesApplicationFootprintComponent,
            ),
        resolve: {
            title: TitleResolver,
        },
        data: {
            titleKey: "inventories.page-title",
        },
        canActivate: [FootprintRedirectGuard],
    },
];
