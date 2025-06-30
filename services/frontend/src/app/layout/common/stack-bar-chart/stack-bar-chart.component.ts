import { Component, computed, EventEmitter, input, Output } from "@angular/core";
import { EChartsOption } from "echarts";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import { AbstractDashboard } from "src/app/layout/inventories-footprint/abstract-dashboard";
import { Constants } from "src/constants";
@Component({
    selector: "app-stack-bar-chart",
    templateUrl: "./stack-bar-chart.component.html",
})
export class StackBarChartComponent extends AbstractDashboard {
    @Output() selectedStackBarClick: EventEmitter<string> = new EventEmitter();
    xAxisInput = input<string[]>([]);
    statusCountMap = input<StatusCountMap>({});
    cursor = input<string>("pointer");
    optionStackBar = computed(() => {
        return this.renderStackChart();
    });

    renderStackChart(): EChartsOption {
        const totalItems = this.xAxisInput().length; // Total number of data points in your chart
        const visibleItems = Constants.TOTAL_VISIBLE_GRAPH_ITEMS; // Number of items you want to show at the initial zoom level
        const endPercentage = (visibleItems / totalItems) * 100; // Calculate the end percentage
        const shouldShowDataZoom = totalItems > visibleItems;
        return {
            legend: {
                selectedMode: false,
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            yAxis: {
                type: "value",
            },
            xAxis: {
                type: "category",
                data: this.xAxisInput() as any[],
                axisLabel: {
                    interval: 0, // Show all labels
                    rotate: 30, // Rotate labels if they overlap
                },
            },
            dataZoom: shouldShowDataZoom
                ? [
                      {
                          type: "slider",
                          show: true,
                          xAxisIndex: 0,
                          start: 0, // Start at 0%
                          end: endPercentage, // Show only limited items at the beginning
                      },
                      {
                          type: "inside",
                          xAxisIndex: 0,
                          start: 0,
                          end: endPercentage,
                      },
                  ]
                : [],
            cursor: this.cursor(),
            color: [Constants.GRAPH_BLUE, Constants.GRAPH_RED],
            series: [
                this.translate.instant("error-graph.impact-calculated"),
                this.translate.instant("error-graph.unable-calculate"),
            ].map((name, index) => {
                return {
                    name,
                    type: "bar",
                    stack: "total",
                    barWidth: "50%",
                    label: {
                        show: true,
                        formatter: (params: any) =>
                            Math.round(params.value * 1000) / 10 + "%",
                    },
                    data: Object.values(this.statusCountMap()).map(
                        (value) =>
                            (index > 0 ? value.status.error : value.status.ok) /
                            value.status.total,
                    ),
                };
            }),
        };
    }

    onChartClick(params: any) {
        this.selectedStackBarClick.emit(params.name);
    }
}
