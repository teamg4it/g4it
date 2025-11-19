import { NgModule } from "@angular/core";
import { NgxEchartsModule } from "ngx-echarts";
import { GraphDescriptionComponent } from "src/app/layout/digital-services-footprint/digital-services-footprint-dashboard/graph-description/graph-description.component";
import { PieChartComponent } from "src/app/layout/digital-services-footprint/digital-services-footprint-dashboard/pie-chart/pie-chart.component";
import { RadialChartComponent } from "src/app/layout/digital-services-footprint/digital-services-footprint-dashboard/radial-chart/radial-chart.component";
import { SharedModule } from "./shared.module";

@NgModule({
    declarations: [RadialChartComponent, PieChartComponent, GraphDescriptionComponent],
    exports: [RadialChartComponent, PieChartComponent, GraphDescriptionComponent],
    imports: [
        SharedModule,
        NgxEchartsModule.forRoot({
            echarts: () => import("echarts"),
        }),
    ],
})
export class SharedChartsModule {}
