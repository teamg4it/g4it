/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, HostListener, inject, OnInit } from "@angular/core";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { delay, filter, firstValueFrom, map, of, Subject, switchMap } from "rxjs";
import { environment } from "src/environments/environment";
import { CustomAuthService } from "./core/service/business/custom-auth.service";
import { MatomoScriptService } from "./core/service/business/matomo-script.service";
import { UserDataService } from "./core/service/data/user-data.service";
import { GlobalStoreService } from "./core/store/global.store";

@Component({
    selector: "app-root",
    templateUrl: "./app.component.html",
})
export class AppComponent implements OnInit {
    ngUnsubscribe = new Subject<void>();
    selectedLang: string = this.translate.currentLang;
    isZoomedIn = false;
    private readonly matomoScriptService = inject(MatomoScriptService);
    constructor(
        private readonly userService: UserDataService,
        private readonly keycloak: KeycloakService,
        private readonly translate: TranslateService,
        private readonly globalStoreService: GlobalStoreService,
        public router: Router,
        private readonly activatedRoute: ActivatedRoute,
        private readonly titleService: Title,
        private readonly customAuthService: CustomAuthService,
    ) {}

    ngOnInit(): void {
        this.initializeAsync();
    }

    private async initializeAsync(): Promise<void> {
        const isPublicRoute = this.customAuthService.isPublicRoute(
            globalThis.location.pathname,
        );
        if (environment.keycloak.enabled === "true" && !isPublicRoute) {
            const token = await this.keycloak.getToken();
            if (!token) {
                const loginHint = localStorage.getItem("username") || "";
                await this.keycloak.login({
                    redirectUri: globalThis.location.href,
                    loginHint,
                });
            }
        }

        const user = await firstValueFrom(
            isPublicRoute
                ? of({
                      email: "test@test.com",
                      firstName: "test",
                      lastName: "test",
                      id: 1,
                      organizations: [],
                      isSuperAdmin: false,
                  }).pipe(delay(2000))
                : this.userService.fetchUserInfo(),
        );
        localStorage.setItem("username", user.email);

        this.globalStoreService.setcriteriaList(
            this.translate.translations[this.selectedLang]["criteria"],
        );
        this.checkZoom();
        this.router.events
            .pipe(
                filter((event) => event instanceof NavigationEnd),
                switchMap(() => {
                    let route = this.activatedRoute.firstChild;
                    while (route?.firstChild) {
                        route = route.firstChild;
                    }
                    return route?.data || of({});
                }),
                map((data: { [key: string]: string }) => data["title"] || "G4IT"),
            )
            .subscribe((title: string) => {
                const fullTitle = title === "G4IT" ? title : `${title} - G4IT`;
                this.titleService.setTitle(fullTitle);
            });

        // configure matamo tag manager
        const matomoTagManagerUrl = environment.matomo.matomoTagManager.containerUrl;
        if (matomoTagManagerUrl !== "") {
            this.matomoScriptService.appendScriptToHead(matomoTagManagerUrl);
        }
    }

    @HostListener("window:resize", [])
    onResize() {
        this.checkZoom();
    }

    checkZoom() {
        const zoomLevel = Math.round((window.devicePixelRatio * 100 * 4) / 5);
        this.isZoomedIn = zoomLevel > 150;
        this.globalStoreService.setZoomLevel(zoomLevel);
        this.globalStoreService.setIsMobile(window.innerWidth < 992);
    }
}
