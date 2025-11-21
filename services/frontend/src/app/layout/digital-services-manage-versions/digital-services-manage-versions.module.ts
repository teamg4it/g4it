/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from "@angular/core";
import { CommonDigitalServicesSharedModule } from "src/app/core/shared/common-digital-services-shared.module";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServiceManageVersionTableComponent } from "./digital-service-manage-version/digital-service-manage-version-table/digital-service-manage-version-table.component";
import { DigitalServiceManageVersionComponent } from "./digital-service-manage-version/digital-service-manage-version.component";
import { digitalServicesManageVersionsRouter } from "./digital-services-manage-versions.router";

@NgModule({
    declarations: [
        DigitalServiceManageVersionComponent,
        DigitalServiceManageVersionTableComponent,
    ],
    imports: [
        SharedModule,
        digitalServicesManageVersionsRouter,
        CommonDigitalServicesSharedModule,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
})
export class DigitalServicesManageVersionsModule {}
