import { Routes } from "@angular/router";
import { AuthGuard } from "src/app/guard/auth.gard";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";

const canActivate = [];
if (environment.keycloak.enabled === "true") canActivate.push(AuthGuard);

export const appRoutes: Routes = [
    {
        path: Constants.USEFUL_INFORMATION,
        loadComponent: () =>
            import("../about-us/useful-information/useful-information.component").then(
                (m) => m.UsefulInformationComponent,
            ),
        canActivate,
    },
    {
        path: Constants.DECLARATIONS,
        loadComponent: () =>
            import("../about-us/declarations/declarations.component").then(
                (m) => m.DeclarationsComponent,
            ),
        canActivate,
    },
    {
        path: "administration",
        loadChildren: () =>
            import("../administration/administration.router").then(
                (m) => m.ADMINISTRATION_ROUTES,
            ),
        canActivate,
    },
    {
        path: "organizations/:organization/workspaces/:workspace",
        loadChildren: () =>
            import("../layout.router").then(
                (m) => m.LAYOUT_ROUTES,
            ),
        canActivate,
    },
    {
        path: Constants.WELCOME_PAGE,
        loadComponent: () =>
            import("../welcome-page/welcome-page.component").then(
                (m) => m.WelcomePageComponent,
            ),
        pathMatch: "full",
        canActivate,
    },
    {
        path: "**",
        redirectTo: Constants.WELCOME_PAGE,
    },
];
