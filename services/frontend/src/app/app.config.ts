/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    ApplicationConfig,
    importProvidersFrom,
    inject,
    provideAppInitializer,
} from "@angular/core";

import {
    HTTP_INTERCEPTORS,
    HttpClient,
    provideHttpClient,
    withInterceptorsFromDi,
} from "@angular/common/http";

import { APP_BASE_HREF, DatePipe } from "@angular/common";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { provideRouter, withRouterConfig } from "@angular/router";

import { TranslateLoader, TranslateModule, TranslateService } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";
import { KeycloakAngularModule } from "keycloak-angular";

import { MessageService } from "primeng/api";
import { ProgressBarModule } from "primeng/progressbar";
import { TableModule } from "primeng/table";
import { ToastModule } from "primeng/toast";

import { environment } from "src/environments/environment";

import { ApiInterceptor } from "./core/interceptors/api-request.interceptor";
import { HttpErrorInterceptor } from "./core/interceptors/http-error.interceptor";
import { CustomAuthService } from "./core/service/business/custom-auth.service";

import Aura from "@primeuix/themes/aura";
import { providePrimeNG } from "primeng/config";
import { Constants } from "src/constants";
import { appRoutes } from "./app-routing.module";
import { DecimalsPipe } from "./core/pipes/decimal.pipe";
import { IntegerPipe } from "./core/pipes/integer.pipe";

/* ---------------- TRANSLATION LOADER ---------------- */

export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http, "assets/i18n/", ".json");
}

/* ---------------- BASE HREF ---------------- */

function baseHRefFactory() {
    return environment.subpath ? "/" + environment.subpath : "/";
}

/* ---------------- AUTH INITIALIZER ---------------- */

function initializeAuth(authService: CustomAuthService) {
    return () => authService.init();
}

export function initializeLanguage(translate: TranslateService) {
    return () => {
        let lang = localStorage.getItem("lang") || translate.getBrowserLang() || "en";

        if (!Constants.LANGUAGES.includes(lang)) {
            lang = "en";
        }

        translate.setDefaultLang(lang);
        translate.addLangs(Constants.LANGUAGES);
        translate.use(lang);

        document.querySelector("html")?.setAttribute("lang", lang);
    };
}

export const appConfig: ApplicationConfig = {
    providers: [
        providePrimeNG({
            theme: {
                preset: Aura,
                options: {
                    darkModeSelector: "none",
                    cssLayer: {
                        name: "primeng",
                        order: "tailwind-base, primeng, tailwind-utilities",
                    },
                },
            },
            csp: {
                nonce: undefined,
            },
        }),
        provideRouter(appRoutes, withRouterConfig({ onSameUrlNavigation: "reload" })),
        provideHttpClient(withInterceptorsFromDi()),
        importProvidersFrom(
            BrowserAnimationsModule,
            KeycloakAngularModule,
            ToastModule,
            ProgressBarModule,
            TableModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useFactory: HttpLoaderFactory,
                    deps: [HttpClient],
                },
            }),
        ),
        MessageService,
        DatePipe,
        IntegerPipe,
        DecimalsPipe,
        provideAppInitializer(() => {
            const initializerFn = initializeAuth(inject(CustomAuthService));
            return initializerFn();
        }),
        provideAppInitializer(() => {
            const initializerFn = initializeLanguage(inject(TranslateService));
            return initializerFn();
        }),
        {
            provide: APP_BASE_HREF,
            useFactory: baseHRefFactory,
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: ApiInterceptor,
            multi: true,
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorInterceptor,
            multi: true,
        },
    ],
};
