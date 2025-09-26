/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { APP_BASE_HREF, DatePipe } from "@angular/common";
import { HTTP_INTERCEPTORS, HttpClient, HttpClientModule } from "@angular/common/http";
import { APP_INITIALIZER, NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { TranslateLoader, TranslateModule, TranslateService } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";
import { KeycloakAngularModule } from "keycloak-angular";
import { MessageService } from "primeng/api";
import { ProgressBarModule } from "primeng/progressbar";
import { TableModule } from "primeng/table";
import { ToastModule } from "primeng/toast";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";
import { ApiInterceptor } from "./core/interceptors/api-request.interceptor";
import { HttpErrorInterceptor } from "./core/interceptors/http-error.interceptor";
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

@NgModule({
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
})
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
