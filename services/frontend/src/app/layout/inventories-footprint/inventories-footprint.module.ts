/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from "@angular/core";
import { NgxEchartsModule } from "ngx-echarts";
import { BadgeModule } from "primeng/badge";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { OverlayModule } from "primeng/overlay";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { TabsModule } from "primeng/tabs";
import { ToastModule } from "primeng/toast";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { InventoryUtilService } from "src/app/core/service/business/inventory-util.service";
import { SharedChartsModule } from "src/app/core/shared/common-chart-module";
import { SharedModule } from "src/app/core/shared/shared.module";
import { inventoriesFootprintRouter } from "./inventories-footprint.router";

@NgModule({
    imports: [
        SharedModule,
        SharedChartsModule,
        ButtonModule,
        TabsModule,
        ToastModule,
        ScrollPanelModule,
        CardModule,
        OverlayModule,
        CheckboxModule,
        ConfirmPopupModule,
        BadgeModule,
        NgxEchartsModule.forRoot({
            echarts: () => import(
            /* webpackChunkName: "echarts" */
            /* webpackMode: "lazy" */
            "src/app/core/shared/echarts.module").then((m) => m.default),
        }),
        inventoriesFootprintRouter,
    ],
    providers: [InventoryUtilService, IntegerPipe, DecimalsPipe],
    schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
})
export class InventoriesFootprintModule {}
