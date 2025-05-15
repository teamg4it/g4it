/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from "@angular/core";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SharedModule } from "src/app/core/shared/shared.module";
import { SidePanelDsSharedUsersComponent } from "../common/side-panel-ds-shared-users/side-panel-ds-shared-users.component";
import { DigitalServicesItemComponent } from "./eco-mind-ai-item/eco-mind-ai-item.component";
import { EcoMindAiComponent } from "./eco-mind-ai.component";
import { EcoMindAiRouter } from "./eco-mind-ai.router";

@NgModule({
    declarations: [EcoMindAiComponent, DigitalServicesItemComponent],
    imports: [
        ButtonModule,
        ScrollPanelModule,
        CardModule,
        SharedModule,
        ConfirmPopupModule,
        EcoMindAiRouter,
        SidePanelDsSharedUsersComponent,
    ],
    exports: [EcoMindAiComponent, DigitalServicesItemComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
})
export class EcoMindAiModule {}
