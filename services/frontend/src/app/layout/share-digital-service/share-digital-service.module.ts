import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { CommonDigitalServicesSharedModule } from "src/app/core/shared/common-digital-services-shared.module";
import { SharedModule } from "src/app/core/shared/shared.module";
import { ShareDigitalServiceComponent } from "./share-digital-service.component";
import { shareDsRoutes } from "./shared-digital-service.route";

@NgModule({
    declarations: [ShareDigitalServiceComponent],
    imports: [
        CommonModule,
        SharedModule,
        RouterModule.forChild(shareDsRoutes),
        CommonDigitalServicesSharedModule,
    ],
    exports: [ShareDigitalServiceComponent],
})
export class ShareDigitalServiceModule {}
