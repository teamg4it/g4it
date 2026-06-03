/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Routes } from "@angular/router";
import { environment } from "src/environments/environment";
import { authGuard } from "./guard/auth.gard";
import { ErrorComponent } from "./layout/common/error/error.component";
import { LandingPageComponent } from "./layout/landing-page/landing-page.component";

const canActivate = [];
if (environment.keycloak?.enabled === "true") canActivate.push(authGuard);

export const appRoutes: Routes = [
    {
        path: "something-went-wrong/:err",
        component: ErrorComponent,
    },
    {
        path: "shared",
        loadChildren: () =>
            import(
                "./layout/share-digital-service/share-landing-page/share-landing-page.route"
            ).then((t) => {
                return t.appRoutes;
            }),
    },
    {
        path: "",
        component: LandingPageComponent,
        loadChildren: () =>
            import("./layout/landing-page/landing-page.router").then((t) => {
                return t.appRoutes;
            }),
        canActivate,
    },
    {
        path: "**",
        redirectTo: "",
    },
];
