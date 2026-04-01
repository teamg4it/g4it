import { provideZoneChangeDetection } from "@angular/core";
/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { bootstrapApplication } from "@angular/platform-browser";
import { AppComponent } from "./app/app.component";
import { appConfig } from "./app/app.config";

bootstrapApplication(AppComponent, {
    ...appConfig,
    providers: [provideZoneChangeDetection(), ...appConfig.providers],
}).catch((err) => console.error(err));
