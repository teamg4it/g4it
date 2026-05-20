import { Routes } from "@angular/router";
import { SharedAccessGuard } from "src/app/guard/shared-ds.guard";
import { Constants } from "src/constants";

export const appRoutes: Routes = [
    {
        path: "",
        loadComponent: () =>
            import("./share-landing-page.component").then(
                (m) => m.ShareLandingPageComponent,
            ),
        children: [
            {
                path: `:share-token/dsv/:id/${Constants.USEFUL_INFORMATION}`,
                loadComponent: () =>
                    import(
                        "../../about-us/useful-information/useful-information.component"
                    ).then((m) => m.UsefulInformationComponent),
                canActivate: [SharedAccessGuard],
            },
            {
                path: `:share-token/dsv/:id/${Constants.DECLARATIONS}`,
                loadComponent: () =>
                    import("../../about-us/declarations/declarations.component").then(
                        (m) => m.DeclarationsComponent,
                    ),
                canActivate: [SharedAccessGuard],
            },
            {
                path: ":share-token/dsv/:id/footprint",
                loadChildren: () =>
                    import("../shared-digital-service.route").then(
                        (m) => m.shareDsRoutes,
                    ),
                canActivate: [SharedAccessGuard],
            },
            {
                path: "**",
                redirectTo: "",
            },
        ],
    },
    {
        path: "**",
        redirectTo: "",
    },
];
