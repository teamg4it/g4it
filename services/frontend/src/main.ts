/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { enableProdMode, importProvidersFrom, inject, provideAppInitializer } from "@angular/core";

import { APP_BASE_HREF, DatePipe } from "@angular/common";
import { HTTP_INTERCEPTORS, HttpClient, HttpClientModule } from "@angular/common/http";
import { BrowserModule, bootstrapApplication } from "@angular/platform-browser";
import { provideAnimations } from "@angular/platform-browser/animations";
import { TranslateLoader, TranslateModule } from "@ngx-translate/core";
import Aura from '@primeuix/themes/aura';
import { KeycloakAngularModule } from "keycloak-angular";
import { MessageService } from "primeng/api";
import { providePrimeNG } from "primeng/config";
import { ProgressBarModule } from "primeng/progressbar";
import { TableModule } from "primeng/table";
import { ToastModule } from "primeng/toast";
import { AppRoutingModule } from "./app/app-routing.module";
import { AppComponent } from "./app/app.component";
import { HttpLoaderFactory } from "./app/app.module";
import { ApiInterceptor } from "./app/core/interceptors/api-request.interceptor";
import { HttpErrorInterceptor } from "./app/core/interceptors/http-error.interceptor";
import { CustomAuthService } from "./app/core/service/business/custom-auth.service";
import { environment } from "./environments/environment";

function initializeAuth(authService: CustomAuthService) {
    return () => authService.init();
}
function baseHRefFactory() {
    // If the subpath is set in the environment, use it as the base href
    return environment.subpath ? "/" + environment.subpath : "/";
}



if (environment.production) {
    enableProdMode();
}

bootstrapApplication(AppComponent, {
    providers: [
        importProvidersFrom(BrowserModule, HttpClientModule, AppRoutingModule, TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient],
            },
        }), ToastModule, ProgressBarModule, KeycloakAngularModule, TableModule),
        provideAppInitializer(() => {
            const initializerFn = (initializeAuth)(inject(CustomAuthService));
            return initializerFn();
        }),
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
        providePrimeNG({
            theme: {
                preset: Aura,
                options: { darkModeSelector: false || 'none' }
            }
        }),
        provideAnimations(),
    ]
})
    .catch((err) => console.error(err));
