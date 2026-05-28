import { Injectable } from "@angular/core";
import { NavigationEnd, Router } from "@angular/router";
import Keycloak from "keycloak-js";
import { filter } from "rxjs/operators";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { DigitalServiceStoreService } from "../../store/digital-service.store";

export const keycloak = new Keycloak({
    url: environment.keycloak.issuer,
    realm: environment.keycloak.realm,
    clientId: environment.keycloak.clientId,
});

@Injectable({
    providedIn: "root",
})
export class CustomAuthService {
    private readonly publicPaths = [
        Constants.ENDPOINTS.sharedDs,
        Constants.ENDPOINTS.dsv,
    ];

    constructor(
        private readonly router: Router,
        private readonly digitalServiceStore: DigitalServiceStoreService,
    ) {}

    init(): Promise<boolean> {
        const isPublic = this.isPublicRoute(globalThis.location.pathname);
        this.digitalServiceStore.setIsSharedDS(isPublic);
        if (!isPublic && environment.keycloak.enabled === "true") {
            return keycloak.init({
                onLoad: "check-sso",
                flow: "standard",
                checkLoginIframe: false,
            });
        }

        // For public routes, don't initialize Keycloak
        return Promise.resolve(true);
    }

    isPublicRoute(url: string): boolean {
        return this.publicPaths.some((path) => url.includes(path));
    }

    setupRouteGuard(): void {
        if (environment.keycloak.enabled === "true") {
            this.router.events
                .pipe(filter((event) => event instanceof NavigationEnd))
                .subscribe((event: NavigationEnd) => {
                    // If navigating to a protected route but Keycloak isn't initialized
                    if (!this.isPublicRoute(event.url)) {
                        const isLoggedIn: boolean = !!keycloak.authenticated;
                        if (!isLoggedIn) {
                            // Initialize on demand if needed
                            this.init();
                        }
                    }
                });
        }
    }
}
