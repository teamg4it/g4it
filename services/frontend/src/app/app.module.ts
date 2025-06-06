/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { DatePipe } from "@angular/common";
import { HTTP_INTERCEPTORS, HttpClient, HttpClientModule } from "@angular/common/http";
import { APP_INITIALIZER, NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { TranslateLoader, TranslateModule, TranslateService } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";
import { KeycloakAngularModule, KeycloakService } from "keycloak-angular";
import { MessageService } from "primeng/api";
import { ProgressBarModule } from "primeng/progressbar";
import { ToastModule } from "primeng/toast";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";
import { ApiInterceptor } from "./core/interceptors/api-request.interceptor";
import { HttpErrorInterceptor } from "./core/interceptors/http-error.interceptor";

// Function to load translation files using HttpClient
export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http, "assets/i18n/", ".json");
}

function initializeKeycloak(keycloak: KeycloakService) {
    return () =>
        keycloak.init({
            config: {
                url: environment.keycloak.issuer,
                realm: environment.keycloak.realm,
                clientId: environment.keycloak.clientId,
            },

            initOptions: {
                onLoad: "check-sso", // allowed values 'login-required', 'check-sso';
                flow: "standard", // allowed values 'standard', 'implicit', 'hybrid';
            },
        });
}

@NgModule({
    declarations: [AppComponent],
    imports: [
        BrowserModule,
        HttpClientModule,
        AppRoutingModule,
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
    ],
    providers: [
        environment.keycloak.enabled === "true"
            ? {
                  provide: APP_INITIALIZER,
                  useFactory: initializeKeycloak,
                  multi: true,
                  deps: [KeycloakService],
              }
            : [],
        MessageService,
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
        DatePipe,
    ],
    bootstrap: [AppComponent],
})
export class AppModule {
    constructor(private translate: TranslateService) {
        // Set the default language
        let lang = localStorage.getItem("lang") || translate.getBrowserLang() || "en";
        if (!Constants.LANGUAGES.includes(lang)) lang = "en";

        translate.setDefaultLang(lang);
        // Enable automatic language detection
        translate.addLangs(Constants.LANGUAGES);
        translate.use(lang);
        document.querySelector("html")!.setAttribute("lang", lang);
    }
}
