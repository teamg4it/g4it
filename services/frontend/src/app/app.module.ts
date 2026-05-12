/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient } from "@angular/common/http";
import { NgModule } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { CustomAuthService } from "./core/service/business/custom-auth.service";

// Function to load translation files using HttpClient
export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http, "assets/i18n/", ".json");
}

function baseHRefFactory() {
    // If the subpath is set in the environment, use it as the base href
    return environment.subpath ? "/" + environment.subpath : "/";
}

function initializeAuth(authService: CustomAuthService) {
    return () => authService.init();
}

@NgModule(/* TODO(standalone-migration): clean up removed NgModule class manually.
{
    declarations: [AppComponent],
    imports: [
        BrowserModule,
        HttpClientModule,
        BrowserAnimationsModule,
        AppRoutingModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient],
            },
        }),
        ToastModule,
        ProgressBarModule,
        KeycloakAngularModule,
        TableModule,
    ],
    providers: [
        {
            provide: APP_INITIALIZER,
            useFactory: initializeAuth,
            multi: true,
            deps: [CustomAuthService],
        },
        MessageService,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: ApiInterceptor,
            multi: true,
        },
        { provide: APP_BASE_HREF, useFactory: baseHRefFactory },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorInterceptor,
            multi: true,
        },
        DatePipe,
    ],
    bootstrap: [AppComponent],
} */)
export class AppModule {
    constructor(
        private readonly translate: TranslateService,
        private readonly authService: CustomAuthService,
    ) {
        // Set the default language
        let lang = localStorage.getItem("lang") || translate.getBrowserLang() || "en";
        if (!Constants.LANGUAGES.includes(lang)) lang = "en";

        translate.setDefaultLang(lang);
        // Enable automatic language detection
        translate.addLangs(Constants.LANGUAGES);
        translate.use(lang);
        document.querySelector("html")!.setAttribute("lang", lang);
        this.authService.setupRouteGuard();
    }
}
