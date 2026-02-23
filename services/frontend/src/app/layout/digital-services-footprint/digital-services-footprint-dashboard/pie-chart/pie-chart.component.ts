/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    EventEmitter,
    input,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
} from "@angular/core";
import { EChartsOption } from "echarts";
import {
    DigitalServiceFootprint,
    StatusCountMap,
} from "src/app/core/interfaces/digital-service.interfaces";
import {
    getColorFormatter,
    getLabelFormatter,
} from "src/app/core/service/mapper/graphs-mapper";
import { AbstractDashboard } from "src/app/layout/inventories-footprint/abstract-dashboard";
import { Constants } from "src/constants";
@Component({
    selector: "app-pie-chart",
    templateUrl: "./pie-chart.component.html",
})
export class PieChartComponent extends AbstractDashboard implements OnChanges {
    @Input() globalVisionChartData: DigitalServiceFootprint[] | undefined;
    @Input() selectedCriteria: string = "acidification";
    @Input() enableDataInconsistency: boolean = false;
    @Output() selectedParamChange: EventEmitter<any> = new EventEmitter();
    @Output() chartTypeChange: EventEmitter<any> = new EventEmitter();
    @Output() topPieThreeImpacts: EventEmitter<any[]> = new EventEmitter();

    criteriaMap: StatusCountMap = {};
    xAxisInput: string[] = [];

    showInconsitency = input<boolean>();

    options: EChartsOption = {};

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            this.options = this.loadPieChartOption(this.globalVisionChartData || []);
        }
    }

    onChartClick(params: any) {
        this.chartTypeChange.emit("bar");
        this.selectedParamChange.emit(params.data.tier);
    }

    loadPieChartOption(chartData: DigitalServiceFootprint[]): EChartsOption {
        const order = Constants.DIGITAL_SERVICES_CHART_ORDER;
        const dsTierOkmap: StatusCountMap = {};
        for (const chart of chartData) {
            dsTierOkmap[this.existingTranslation(chart.tier, "digital-services")] = {
                status: {
                    ok: chart.impacts
                        .filter(
                            (i) =>
                                i.status === Constants.DATA_QUALITY_STATUS.ok &&
                                i.criteria === this.selectedCriteria,
                        )
                        .reduce((sum, item) => sum + item.countValue, 0),
                    error: chart.impacts
                        .filter(
                            (i) =>
                                i.status !== Constants.DATA_QUALITY_STATUS.ok &&
                                i.criteria === this.selectedCriteria,
                        )
                        .reduce((sum, item) => sum + item.countValue, 0),
                    total: chart.impacts
                        .filter((i) => i.criteria === this.selectedCriteria)
                        .reduce((sum, item) => sum + item.countValue, 0),
                },
            };
            chart.impacts
                .filter((i) => i.criteria === this.selectedCriteria)
                .every((impact) => impact.status === Constants.DATA_QUALITY_STATUS.ok);
        }
        const seriesData = chartData.map((item) => {
            const selectedImpact = item.impacts.find(
                (impact: any) =>
                    impact.criteria.split(" ").slice(0, 2).join(" ") ===
                        this.selectedCriteria &&
                    impact.status === Constants.DATA_QUALITY_STATUS.ok,
            );
            const selectedImpactUnit = item.impacts.find(
                (impact: any) =>
                    impact.criteria.split(" ").slice(0, 2).join(" ") ===
                    this.selectedCriteria,
            );
            const value = selectedImpact ? selectedImpact.sipValue : 0;
            const nameValue = this.existingTranslation(item.tier, "digital-services");
            return {
                name: nameValue,
                value: value,
                tier: item.tier,
                unitValue: selectedImpactUnit?.unitValue,
                unit: selectedImpactUnit?.unit,
                label: {
                    color: getColorFormatter(
                        !!dsTierOkmap[nameValue]?.status.error,
                        this.enableDataInconsistency,
                    ),
                },
            };
        });
        seriesData.sort((a: any, b: any) => {
            return order.indexOf(a.tier) - order.indexOf(b.tier);
        });
        this.xAxisInput = seriesData.map((item) => item.name);
        this.criteriaMap = dsTierOkmap;
        const topThreeImpacts = [...seriesData]
            .sort((a, b) => b.value - a.value)
            .map((series) => ({
                ...series,
                percentage: (
                    (series.value /
                        seriesData.reduce((acc, curr) => acc + curr.value, 0)) *
                    100
                ).toFixed(0),
            }))
            .slice(0, 3);
        this.topPieThreeImpacts.emit(topThreeImpacts);
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const percentage = params.percent.toFixed(0);
                    const name = this.existingTranslation(
                        params.name,
                        "digital-services",
                    );
                    return `
                    <div style="display: flex; align-items: center; height: 30px;">
                        <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                            params.color
                        }; border-radius: 50%; margin-right: 5px;"></span>
                        <span style="font-weight: bold; margin-right: 15px;">${name}</span>
                    </div>
                    <div>${percentage} %</div>
                    <div>${this.integerPipe.transform(params.value)}
                    ${this.translate.instant("common.peopleeq-min")}<br>
                    ${this.decimalsPipe.transform(params.data?.unitValue)} ${params.data?.unit} </div>
                    `;
                },
            },
            legend: {
                orient: "horizontal",
                formatter: (param: any) => {
                    return this.existingTranslation(param, "digital-services");
                },
            },
            series: [
                {
                    name: "Access From",
                    type: "pie",
                    radius: "70%",
                    data: seriesData,

                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: "rgba(0, 0, 0, 0.5)",
                        },
                    },
                },
            ],
            label: {
                formatter: (value: any) => {
                    let nameWithUnit = value.name;
                    const hasError = !!dsTierOkmap[value.name]?.status?.error;
                    if (value.data?.unitValue && value.data?.unit) {
                        nameWithUnit = `${value.name}\n (${this.decimalsPipe.transform(value.data.unitValue)} ${value.data.unit})`;
                    }
                    const labelText = getLabelFormatter(
                        hasError,
                        this.enableDataInconsistency,
                        nameWithUnit,
                    );
                    // Add unit information to the label if available

                    return labelText;
                },
                rich: Constants.CHART_RICH as any,
            },

            color: Constants.COLOR,
        };
    }

    selectedStackBarClick(event: string): void {
        const keyTier = Object.keys(this.translate.instant("digital-services")).find(
            (key) => this.translate.instant("digital-services")[key] === event,
        );
        if (keyTier) {
            this.chartTypeChange.emit("bar");
            this.selectedParamChange.emit(keyTier);
        }
    }
}
