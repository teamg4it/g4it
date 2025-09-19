import { Injectable } from "@angular/core";
import { NavigationEnd, Router } from "@angular/router";
import { KeycloakService } from "keycloak-angular";
import { filter } from "rxjs/operators";
import { environment } from "src/environments/environment";
import { DigitalServiceStoreService } from "../../store/digital-service.store";

@Injectable({
    providedIn: "root",
})
export class CustomAuthService {
    // Add all your public routes here
    private readonly publicPaths = ["share", "ds"];

    constructor(
        private readonly keycloak: KeycloakService,
        private readonly router: Router,
        private readonly digitalServiceStore: DigitalServiceStoreService,
    ) {}

    init(): Promise<boolean> {
        if (this.isPublicRoute(window.location.pathname)) {
            this.digitalServiceStore.setIsSharedDS(true);
        } else {
            this.digitalServiceStore.setIsSharedDS(false);
        }
        if (
            !this.isPublicRoute(window.location.pathname) &&
            environment.keycloak.enabled === "true"
        ) {
            return this.keycloak.init({
                config: {
                    url: environment.keycloak.issuer,
                    realm: environment.keycloak.realm,
                    clientId: environment.keycloak.clientId,
                },
                initOptions: {
                    onLoad: "check-sso",
                },
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
                        const isLoggedIn: boolean = this.keycloak.isLoggedIn();
                        if (!isLoggedIn) {
                            // Initialize on demand if needed
                            this.init();
                        }
                    }
                });
        }
    }
}
