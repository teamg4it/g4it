/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { NgModule } from "@angular/core";
import { NgxEchartsModule } from "ngx-echarts";
import { BadgeModule } from "primeng/badge";
import { ImageModule } from "primeng/image";
import { OverlayModule } from "primeng/overlay";
import { TabMenuModule } from "primeng/tabmenu";
import { TreeSelectModule } from "primeng/treeselect";
import { SharedChartsModule } from "src/app/core/shared/common-chart-module";
import { SharedModule } from "src/app/core/shared/shared.module";
import { inventoriesApplicationRouteur } from "./inventories-application-footprint.router";

@NgModule({
    imports: [
        SharedModule,
        TabMenuModule,
        OverlayModule,
        TreeSelectModule,
        ImageModule,
        NgxEchartsModule.forRoot({
            echarts: () => import("echarts"),
        }),
        inventoriesApplicationRouteur,
        TabMenuModule,
        BadgeModule,
        SharedChartsModule,
    ],
})
export class InventoriesApplicationFootprintModule {}
