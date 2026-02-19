/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from "@angular/core";
import { NgxEchartsModule } from "ngx-echarts";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { OverlayModule } from "primeng/overlay";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { TabMenuModule } from "primeng/tabmenu";
import { TabViewModule } from "primeng/tabview";
import { ToastModule } from "primeng/toast";
import { InventoryUtilService } from "src/app/core/service/business/inventory-util.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { ImpactAggregateInfosComponent } from "src/app/layout/common/impact-aggregate-infos/impact-aggregate-infos.component";
import { ConfigureViewFiltersComponent } from "../common/configure-view-filters/configure-view-filters.component";
import { ImpactButtonComponent } from "../digital-services-footprint/digital-services-footprint-dashboard/impact-button/impact-button.component";
import { InventoriesCritereFootprintComponent } from "./critere/inventories-critere-footprint.component";
import { DataCenterEquipmentStatsComponent } from "./datacenter-equipment-stats/datacenter-equipment-stats.component";
import { InventoriesFootprintComponent } from "./inventories-footprint.component";
import { inventoriesFootprintRouter } from "./inventories-footprint.router";
import { InventoriesMultiCriteriaFootprintComponent } from "./multicriteria/inventories-multicriteria-footprint.component";

@NgModule({
    declarations: [
        InventoriesFootprintComponent,
        InventoriesMultiCriteriaFootprintComponent,
        InventoriesCritereFootprintComponent,
        DataCenterEquipmentStatsComponent,
        ImpactAggregateInfosComponent,
    ],
    imports: [
        SharedModule,
        ButtonModule,
        TabMenuModule,
        TabViewModule,
        ToastModule,
        ScrollPanelModule,
        CardModule,
        OverlayModule,
        CheckboxModule,
        ConfirmPopupModule,
        NgxEchartsModule.forRoot({
            echarts: () =>
                import(
                    /* webpackChunkName: "echarts" */
                    /* webpackMode: "lazy" */
                    "src/app/core/shared/echarts.module"
                ).then((m) => m.default),
        }),
        inventoriesFootprintRouter,
        ConfigureViewFiltersComponent,
        ImpactButtonComponent,
    ],
    providers: [InventoryUtilService],
    exports: [InventoriesFootprintComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
})
export class InventoriesFootprintModule {}
