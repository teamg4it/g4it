import { NgModule } from "@angular/core";
import { NgxEchartsModule } from "ngx-echarts";
import { PieChartComponent } from "src/app/layout/digital-services-footprint/digital-services-footprint-dashboard/pie-chart/pie-chart.component";
import { RadialChartComponent } from "src/app/layout/digital-services-footprint/digital-services-footprint-dashboard/radial-chart/radial-chart.component";
import { SharedModule } from "./shared.module";

@NgModule({
    declarations: [RadialChartComponent, PieChartComponent],
    exports: [RadialChartComponent, PieChartComponent],
    imports: [
        SharedModule,
        NgxEchartsModule.forRoot({
            echarts: () => import("echarts"),
        }),
    ],
})
export class SharedChartsModule {}
