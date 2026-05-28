import { inject } from "@angular/core";
import {
    ActivatedRouteSnapshot,
    CanActivateFn,
    RouterStateSnapshot,
    UrlTree,
} from "@angular/router";
import { environment } from "src/environments/environment";
import {
    CustomAuthService,
    keycloak,
} from "../core/service/business/custom-auth.service";

/**
 * Angular 21 functional guard for Keycloak authentication
 */
export const authGuard: CanActivateFn = async (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
): Promise<boolean | UrlTree> => {
    const customAuthService = inject(CustomAuthService);

    // Check if the route is public
    const isPublicRoute = customAuthService.isPublicRoute(state.url);

    // For public routes, allow access
    if (isPublicRoute) {
        return true;
    }

    // For protected routes, check Keycloak authentication
    if (environment.keycloak.enabled === "true") {
        // Check if user is authenticated
        if (keycloak.authenticated) {
            return true;
        }

        // Not authenticated - redirect to login
        const loginHint = localStorage.getItem("username") || "";
        await keycloak.login({
            redirectUri: globalThis.location.origin + state.url,
            loginHint,
        });

        // This line won't be reached due to redirect, but TypeScript needs it
        return false;
    }

    // If Keycloak is disabled, allow access
    return true;
};
