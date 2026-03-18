/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from "@angular/core";
import { DigitalServicesCompareVersionsComponent } from "./digital-services-compare-versions.component";
import { digitalServicesCompareVersionsRouter } from "./digital-services-compare-versions.router";

@NgModule({
    imports: [
        digitalServicesCompareVersionsRouter,
        DigitalServicesCompareVersionsComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
})
export class DigitalServicesCompareVersionsModule {}
