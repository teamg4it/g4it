/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, HostListener, inject } from "@angular/core";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { Subject, filter, firstValueFrom, map, of, switchMap } from "rxjs";
import { environment } from "src/environments/environment";
import { MatomoScriptService } from "./core/service/business/matomo-script.service";
import { UserDataService } from "./core/service/data/user-data.service";
import { GlobalStoreService } from "./core/store/global.store";

@Component({
    selector: "app-root",
    templateUrl: "./app.component.html",
})
export class AppComponent {
    ngUnsubscribe = new Subject<void>();
    selectedLang: string = this.translate.currentLang;
    isZoomedIn = false;
    private readonly matomoScriptService = inject(MatomoScriptService);
    constructor(
        private userService: UserDataService,
        private keycloak: KeycloakService,
        private translate: TranslateService,
        private globalStoreService: GlobalStoreService,
        public router: Router,
        private activatedRoute: ActivatedRoute,
        private titleService: Title,
    ) {}

    async ngOnInit() {
        if (environment.keycloak.enabled === "true") {
            const token = await this.keycloak.getToken();
            if (!token) {
                const loginHint = localStorage.getItem("username") || "";
                await this.keycloak.login({
                    redirectUri: window.location.href,
                    loginHint,
                });
            }
        }

        const user = await firstValueFrom(this.userService.fetchUserInfo());
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
    }
}
