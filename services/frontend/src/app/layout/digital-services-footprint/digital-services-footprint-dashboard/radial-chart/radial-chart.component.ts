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
    inject,
    input,
    Input,
    Output,
    SimpleChanges,
} from "@angular/core";
import { EChartsOption } from "echarts";
import {
    DigitalServiceFootprint,
    StatusCountMap,
} from "src/app/core/interfaces/digital-service.interfaces";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { AbstractDashboard } from "src/app/layout/inventories-footprint/abstract-dashboard";
import { Constants } from "src/constants";

@Component({
    selector: "app-radial-chart",
    templateUrl: "./radial-chart.component.html",
})
export class RadialChartComponent extends AbstractDashboard {
    @Input() globalVisionChartData: DigitalServiceFootprint[] | undefined;
    @Output() selectedCriteriaChange: EventEmitter<string> = new EventEmitter();
    @Input() showDataButton: boolean = false;
    showInconsitency = input<boolean>();
    selectedCriteria = input<string>();
    options: EChartsOption = {};
    criteriaMap: StatusCountMap = {};
    xAxisInput: string[] = [];
    private readonly global = inject(GlobalStoreService);

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            this.options = this.loadRadialChartOption(this.globalVisionChartData || []);
        }
    }

    getCriteriaMap(radialChartData: DigitalServiceFootprint[]): void {
        radialChartData?.forEach((data) => {
            data.impacts.forEach((impact) => {
                const translatedCriteria = this.getCriteriaTranslation(
                    impact.criteria.split(" ").slice(0, 2).join(" "),
                );
                this.criteriaMap[translatedCriteria] = {
                    status: {
                        ok:
                            (this.criteriaMap[translatedCriteria]?.status?.ok ?? 0) +
                            (impact.status === Constants.DATA_QUALITY_STATUS.ok
                                ? impact.countValue
                                : 0),
                        error:
                            (this.criteriaMap[translatedCriteria]?.status?.error ?? 0) +
                            (impact.status !== Constants.DATA_QUALITY_STATUS.ok
                                ? impact.countValue
                                : 0),
                        total:
                            (this.criteriaMap[translatedCriteria]?.status?.total ?? 0) +
                            impact.countValue,
                    },
                };
            });
        });
    }

    onChartClick(params: any) {
        this.selectedCriteriaChange.emit(params.data.impact.criteria);
    }

    loadRadialChartOption(radialChartData: DigitalServiceFootprint[]): EChartsOption {
        const order = Constants.DIGITAL_SERVICES_CHART_ORDER;
        this.selectedLang = this.translate.currentLang;
        const criteriaOrder = Object.keys(this.global.criteriaList());
        radialChartData.sort((a: any, b: any) => {
            return order.indexOf(a.tier) - order.indexOf(b.tier);
        });
        radialChartData.forEach((data) => {
            data.impacts.sort(
                (a, b) =>
                    criteriaOrder.indexOf(a.criteria) - criteriaOrder.indexOf(b.criteria),
            );
        });
        this.getCriteriaMap(radialChartData);
        const noErrorRadialChartData: DigitalServiceFootprint[] = radialChartData.map(
            (item) => {
                return {
                    ...item,
                    impacts: item.impacts.filter(
                        (i) =>
                            i.status === Constants.DATA_QUALITY_STATUS.ok ||
                            (i.status === Constants.DATA_QUALITY_STATUS.error &&
                                !item.impacts.some(
                                    (impact) =>
                                        impact.criteria === i.criteria &&
                                        impact.status ===
                                            Constants.DATA_QUALITY_STATUS.ok,
                                )),
                    ),
                };
            },
        );
        const criteriaSetArray: string[] = Object.keys(this.criteriaMap);
        this.xAxisInput = criteriaSetArray;
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const dataIndex = params.dataIndex;
                    const seriesIndex = params.seriesIndex;
                    const impact = noErrorRadialChartData[seriesIndex].impacts[dataIndex];
                    const name = this.existingTranslation(
                        noErrorRadialChartData[seriesIndex].tier,
                        "digital-services",
                    );
                    return `<div style="display: flex; align-items: center; height: 30px;">
                    <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                        params.color
                    }; border-radius: 50%; margin-right: 5px;"></span>
                    <span style="font-weight: bold; margin-right: 15px;">${name}</span>
                </div>
                    <div>${this.getCriteriaTranslation(
                        impact.criteria.split(" ").slice(0, 2).join(" "),
                    )} : ${this.integerPipe.transform(impact.sipValue)}
                    ${this.translate.instant("common.peopleeq-min")}<br>
                    ${this.decimalsPipe.transform(impact.unitValue)} ${impact.unit}
                    </div>
                </div>
            `;
                },
            },
            angleAxis: {
                type: "category",
                data: noErrorRadialChartData[0].impacts.map((impact) => {
                    const twoWordsImpact = impact.criteria
                        .split(" ")
                        .slice(0, 2)
                        .join(" ");
                    const wordsValue = this.getCriteriaTranslation(twoWordsImpact);
                    return {
                        value: wordsValue,
                        textStyle: {
                            color:
                                this.criteriaMap[wordsValue].status.error &&
                                this.showDataButton
                                    ? Constants.GRAPH_RED
                                    : Constants.GRAPH_GREY,
                        },
                    };
                }),
                axisLabel: {
                    formatter: (value: string) => {
                        return this.criteriaMap[value].status.error
                            ? `{redBold| \u24d8} {red| ${value}}`
                            : `{grey| ${value}}`;
                    },
                    rich: Constants.CHART_RICH as any,
                    margin: 15,
                },
            },
            radiusAxis: {
                name: this.translate.instant("common.peopleeq"),
                nameLocation: "end",
                nameTextStyle: {
                    fontStyle: "italic",
                },
            },
            polar: {
                radius: "76%",
                center: ["50%", "55%"],
            },
            series: noErrorRadialChartData.map((item: any) => ({
                name: item.tier,
                type: "bar",
                coordinateSystem: "polar",
                avoidLabelOverlap: true,
                data: item.impacts.map((impact: any) => ({
                    value: impact.sipValue,
                    label: {
                        formatter: (params: any) => {
                            return `${impact.unitValue} ${impact.unit}`;
                        },
                    },
                    impact: impact,
                })),
                stack: "a",
                emphasis: {
                    focus: "series",
                },
            })),
            legend: {
                show: true,
                data: noErrorRadialChartData.map((item: any) => item.tier),
                formatter: (param: any) => {
                    return this.existingTranslation(param, "digital-services");
                },
            },
            color: Constants.COLOR,
        };
    }

    selectedStackBarClick(event: string): void {
        const key = Object.keys(this.translate.instant("criteria")).find(
            (key) => this.translate.instant("criteria")[key].title === event,
        );
        if (key) {
            this.selectedCriteriaChange.emit(key);
        }
    }
}
