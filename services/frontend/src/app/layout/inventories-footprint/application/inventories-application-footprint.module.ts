/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { NgModule } from "@angular/core";
import { NgxEchartsModule } from "ngx-echarts";
import { ImageModule } from "primeng/image";
import { OverlayModule } from "primeng/overlay";
import { TabMenuModule } from "primeng/tabmenu";
import { TreeSelectModule } from "primeng/treeselect";
import { SharedModule } from "src/app/core/shared/shared.module";
import { ConfigureViewFiltersComponent } from "../../common/configure-view-filters/configure-view-filters.component";
import { ImpactButtonComponent } from "../../digital-services-footprint/digital-services-footprint-dashboard/impact-button/impact-button.component";
import { ApplicationCriteriaPieChartComponent } from "./application-criteria-pie-chart/application-criteria-pie-chart.component";
import { CriteriaStatsComponent } from "./criteria-stats/criteria-stats.component";
import { ApplicationCriteriaFootprintComponent } from "./criteria/application-criteria-footprint.component";
import { DatavizFilterApplicationComponent } from "./dataviz-filter-application/dataviz-filter-application.component";
import { InventoriesApplicationFootprintComponent } from "./inventories-application-footprint.component";
import { inventoriesApplicationRouteur } from "./inventories-application-footprint.router";
import { ApplicationMulticriteriaFootprintComponent } from "./multicriteria/application-multicriteria-footprint.component";

@NgModule({
    declarations: [
        InventoriesApplicationFootprintComponent,
        ApplicationMulticriteriaFootprintComponent,
        ApplicationCriteriaFootprintComponent,
        DatavizFilterApplicationComponent,
        CriteriaStatsComponent,
        ApplicationCriteriaPieChartComponent,
    ],
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
        ImpactButtonComponent,
        ConfigureViewFiltersComponent,
    ],
    exports: [InventoriesApplicationFootprintComponent],
})
export class InventoriesApplicationFootprintModule {}
