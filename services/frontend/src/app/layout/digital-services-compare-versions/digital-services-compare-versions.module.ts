/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from "@angular/core";
import { SharedChartsModule } from "src/app/core/shared/common-chart-module";
import { CommonDigitalServicesSharedModule } from "src/app/core/shared/common-digital-services-shared.module";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServicesCompareVersionsComponent } from "./digital-services-compare-versions.component";
import { digitalServicesCompareVersionsRouter } from "./digital-services-compare-versions.router";

@NgModule({
    declarations: [DigitalServicesCompareVersionsComponent],
    imports: [
        SharedModule,
        digitalServicesCompareVersionsRouter,
        CommonDigitalServicesSharedModule,
        SharedChartsModule,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
})
export class DigitalServicesCompareVersionsModule {}
