/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { NgModule } from "@angular/core";
import { FileUploadModule } from "primeng/fileupload";
import { InplaceModule } from "primeng/inplace";
import { ProgressSpinnerModule } from "primeng/progressspinner";
import { DigitalServicesFootprintHeaderComponent } from "src/app/layout/digital-services-footprint/digital-services-footprint-header/digital-services-footprint-header.component";
import { LinkCreatePopupComponent } from "src/app/layout/digital-services-footprint/digital-services-footprint-header/link-create-popup/link-create-popup.component";
import { DigitalServicesImportComponent } from "src/app/layout/digital-services-footprint/digital-services-import/digital-services-import.component";
import { MultiFileImportComponent } from "src/app/layout/digital-services-footprint/digital-services-import/multi-file-import/multi-file-import.component";
import { SharedModule } from "./shared.module";

@NgModule({
    declarations: [
        DigitalServicesFootprintHeaderComponent,
        LinkCreatePopupComponent,
        DigitalServicesImportComponent,
        MultiFileImportComponent,
    ],
    imports: [SharedModule, FileUploadModule, ProgressSpinnerModule, InplaceModule],
    exports: [
        DigitalServicesFootprintHeaderComponent,
        LinkCreatePopupComponent,
        DigitalServicesImportComponent,
        MultiFileImportComponent,
    ],
})
export class CommonDigitalServicesSharedModule {}
