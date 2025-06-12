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
            import("../administration/administration.module").then(
                (modules) => modules.AdministrationModule,
            ),
        canActivate,
    },
    {
        path: "subscribers/:subscriber/organizations/:organization",
        loadChildren: () =>
            import("../layout.module").then((modules) => modules.LayoutModule),
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
