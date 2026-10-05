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
    InputSignal,
    OnChanges,
    Output,
    SimpleChanges,
} from "@angular/core";
import { EChartsOption } from "echarts";
import { NgxEchartsDirective } from "ngx-echarts";
import {
    DigitalServiceFootprint,
    StatusCountMap,
} from "src/app/core/interfaces/digital-service.interfaces";
import {
    getColorFormatter,
    getLabelFormatter,
} from "src/app/core/service/mapper/graphs-mapper";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { AbstractDashboard } from "src/app/layout/inventories-footprint/abstract-dashboard";
import { Constants } from "src/constants";
import { StackBarChartComponent } from "../../../common/stack-bar-chart/stack-bar-chart.component";

@Component({
    selector: "app-radial-chart",
    templateUrl: "./radial-chart.component.html",
    standalone: true,
    imports: [StackBarChartComponent, NgxEchartsDirective],
})
export class RadialChartComponent extends AbstractDashboard implements OnChanges {
    @Input() globalVisionChartData: DigitalServiceFootprint[] | undefined;
    @Output() selectedCriteriaChange: EventEmitter<string> = new EventEmitter();
    @Input() enableDataInconsistency: boolean = false;
    @Input() isCompareScreen = false;
    shouldShowStackBarChart: InputSignal<boolean> = input(false);
    isAxisInverted = input<boolean>(false);
    compareMax = input<number>(0);
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

    getCriteriaMap(
        radialChartData: DigitalServiceFootprint[],
        isInverted: boolean,
    ): void {
        this.criteriaMap = {};

        if (isInverted) {
            this.buildTierBasedMap(radialChartData);
        } else {
            this.buildCriteriaBasedMap(radialChartData);
        }
    }

    private buildTierBasedMap(radialChartData: DigitalServiceFootprint[]): void {
        for (const data of radialChartData) {
            const translatedTier = this.existingTranslation(
                data.tier,
                "digital-services",
            );
            for (const impact of data.impacts) {
                this.updateStatusMap(translatedTier, impact);
            }
        }
    }

    private buildCriteriaBasedMap(radialChartData: DigitalServiceFootprint[]): void {
        for (const data of radialChartData) {
            for (const impact of data.impacts) {
                const twoWordsCriteria = this.extractTwoWordsCriteria(impact.criteria);
                const translatedCriteria = this.getCriteriaTranslation(twoWordsCriteria);
                this.updateStatusMap(translatedCriteria, impact);
            }
        }
    }

    private updateStatusMap(key: string, impact: any): void {
        const currentStatus = this.criteriaMap[key]?.status;
        const isOk = impact.status === Constants.DATA_QUALITY_STATUS.ok;

        this.criteriaMap[key] = {
            status: {
                ok: (currentStatus?.ok ?? 0) + (isOk ? impact.countValue : 0),
                error: (currentStatus?.error ?? 0) + (isOk ? 0 : impact.countValue),
                total: (currentStatus?.total ?? 0) + impact.countValue,
            },
        };
    }

    private extractTwoWordsCriteria(criteria: string): string {
        return criteria.split(" ").slice(0, 2).join(" ");
    }

    private truncateText(text: string, maxLength: number): string {
        return text.length > maxLength ? text.substring(0, maxLength) + "…" : text;
    }

    private formatAxisLabel(
        value: string,
        criteriaMap: StatusCountMap,
        criteriaUnitValues?: { [key: string]: { total: number; unit: string } },
    ): string {
        const hasError = !!criteriaMap[value]?.status?.error;

        if (!criteriaUnitValues) {
            return getLabelFormatter(hasError, this.enableDataInconsistency, value);
        }

        const unitData = criteriaUnitValues[value];
        const unitValueText = unitData
            ? `\n (${this.decimalsPipe.transform(unitData.total)} ${unitData.unit})`
            : "";

        const maxChars = 20;
        const truncatedValue = this.truncateText(value, maxChars);
        const shortUnitValue = this.truncateText(unitValueText, maxChars - 2);

        return getLabelFormatter(
            hasError,
            this.enableDataInconsistency,
            truncatedValue + shortUnitValue,
        );
    }

    private formatTooltip(
        color: string,
        label: string,
        dimensionLabel: string,
        sipValue: number,
        unitValue: number,
        unit: string,
    ): string {
        return `<div style="display: flex; align-items: center; height: 30px;">
                <span style="display: inline-block; width: 10px; height: 10px; background-color: ${color}; border-radius: 50%; margin-right: 5px;"></span>
                <span style="font-weight: bold; margin-right: 15px;">${label}</span>
            </div>
            <div>${dimensionLabel} : ${this.integerPipe.transform(sipValue)}
            ${this.translate.instant("common.peopleeq-min")}<br>
            ${this.decimalsPipe.transform(unitValue)} ${unit}
            </div>
        </div>`;
    }

    private findCriteriaKey(title: string): string | undefined {
        const criteriaTranslations = this.translate.instant("criteria");
        return Object.keys(criteriaTranslations).find(
            (key) => criteriaTranslations[key].title === title,
        );
    }

    onChartClick(params: any) {
        const isInverted = this.isAxisInverted();
        const isStackBar = this.shouldShowStackBarChart();

        let key: string | undefined;

        if (isInverted || (isStackBar && params.componentType === "series")) {
            const title = isInverted ? params.seriesName : params.name;
            key = this.findCriteriaKey(title);
        } else if (params.data?.impact) {
            key = params.data.impact.criteria;
        }

        if (key) {
            this.selectedCriteriaChange.emit(key);
        }
    }

    loadRadialChartOption(radialChartData: DigitalServiceFootprint[]): EChartsOption {
        const order = Constants.DIGITAL_SERVICES_CHART_ORDER;
        this.selectedLang = this.translate.currentLang;
        const criteriaOrder = Object.keys(this.global.criteriaList());
        const isInverted = this.isAxisInverted();

        radialChartData.sort((a: any, b: any) => {
            return order.indexOf(a.tier) - order.indexOf(b.tier);
        });
        for (const data of radialChartData) {
            data.impacts.sort((a, b) =>
                isInverted
                    ? order.indexOf(a.criteria) - order.indexOf(b.criteria)
                    : criteriaOrder.indexOf(a.criteria) -
                      criteriaOrder.indexOf(b.criteria),
            );
        }
        this.getCriteriaMap(radialChartData, isInverted);
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

        // Calculate total unit values per criterion
        const criteriaUnitValues: { [key: string]: { total: number; unit: string } } = {};
        noErrorRadialChartData.forEach((tierData) => {
            tierData.impacts.forEach((impact) => {
                const twoWordsImpact = this.extractTwoWordsCriteria(impact.criteria);
                const criteriaName = this.getCriteriaTranslation(twoWordsImpact);
                if (!criteriaUnitValues[criteriaName]) {
                    criteriaUnitValues[criteriaName] = { total: 0, unit: impact.unit };
                }
                criteriaUnitValues[criteriaName].total += impact.unitValue;
            });
        });

        // If more than 5 criteria and NOT inverted, return stack bar chart configuration
        if (this.shouldShowStackBarChart() && !isInverted) {
            return this.createStackBarChartConfig(noErrorRadialChartData, isInverted);
        }

        // Prepare data based on inversion state
        if (isInverted) {
            // When inverted: angle axis shows tiers, series are criteria
            this.xAxisInput = noErrorRadialChartData.map((tier) =>
                this.existingTranslation(tier.tier, "digital-services"),
            );

            // Group data by criteria
            const criteriaSeries: { [key: string]: any[] } = {};
            noErrorRadialChartData.forEach((tierData) => {
                tierData.impacts.forEach((impact) => {
                    const twoWordsImpact = this.extractTwoWordsCriteria(impact.criteria);
                    const criteriaName = this.getCriteriaTranslation(twoWordsImpact);
                    if (!criteriaSeries[criteriaName]) {
                        criteriaSeries[criteriaName] = [];
                    }
                    criteriaSeries[criteriaName].push({
                        value: impact.sipValue,
                        impact: impact,
                        tier: tierData.tier,
                    });
                });
            });

            const criteriaSetArray = Object.keys(criteriaSeries);
            return this.createRadialChartConfig(
                noErrorRadialChartData,
                criteriaSetArray,
                this.criteriaMap,
                criteriaUnitValues,
                true,
                criteriaSeries,
            );
        } else {
            // Normal mode: angle axis shows criteria, series are tiers
            const criteriaSetArray: string[] = Object.keys(this.criteriaMap);
            this.xAxisInput = criteriaSetArray;
            return this.createRadialChartConfig(
                noErrorRadialChartData,
                criteriaSetArray,
                this.criteriaMap,
                criteriaUnitValues,
                false,
            );
        }
    }

    createRadialChartConfig(
        noErrorRadialChartData: DigitalServiceFootprint[],
        criteriaSetArray: string[],
        criteriaMap: StatusCountMap,
        criteriaUnitValues: { [key: string]: { total: number; unit: string } },
        isInverted: boolean,
        criteriaSeries?: { [key: string]: any[] },
    ): EChartsOption {
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const dataIndex = params.dataIndex;
                    let impact: any;
                    let label: string;
                    let dimensionLabel: string;

                    if (isInverted) {
                        const criteriaName = params.seriesName;
                        const tierData = noErrorRadialChartData[dataIndex];
                        impact = criteriaSeries?.[criteriaName]?.[dataIndex]?.impact;
                        if (!impact) return "";
                        label = criteriaName;
                        dimensionLabel = this.existingTranslation(
                            tierData.tier,
                            "digital-services",
                        );
                    } else {
                        const seriesIndex = params.seriesIndex;
                        impact = noErrorRadialChartData[seriesIndex].impacts[dataIndex];
                        label = this.existingTranslation(
                            noErrorRadialChartData[seriesIndex].tier,
                            "digital-services",
                        );
                        const twoWordsCriteria = this.extractTwoWordsCriteria(
                            impact.criteria,
                        );
                        dimensionLabel = this.getCriteriaTranslation(twoWordsCriteria);
                    }

                    return this.formatTooltip(
                        params.color,
                        label,
                        dimensionLabel,
                        impact.sipValue,
                        impact.unitValue,
                        impact.unit,
                    );
                },
            },
            angleAxis: {
                type: "category",
                data: isInverted
                    ? noErrorRadialChartData.map((tierData) => {
                          const tierName = this.existingTranslation(
                              tierData.tier,
                              "digital-services",
                          );
                          return {
                              value: tierName,
                              textStyle: {
                                  color: getColorFormatter(
                                      !!criteriaMap[tierName]?.status?.error,
                                      this.enableDataInconsistency,
                                  ),
                              },
                          };
                      })
                    : noErrorRadialChartData[0].impacts.map((impact) => {
                          const twoWordsImpact = this.extractTwoWordsCriteria(
                              impact.criteria,
                          );
                          const wordsValue = this.getCriteriaTranslation(twoWordsImpact);
                          return {
                              value: wordsValue,
                              textStyle: {
                                  color: getColorFormatter(
                                      !!criteriaMap[wordsValue].status.error,
                                      this.enableDataInconsistency,
                                  ),
                              },
                          };
                      }),
                axisLabel: {
                    formatter: (value: string) => {
                        if (isInverted) {
                            return this.formatAxisLabel(value, criteriaMap);
                        }
                        return this.formatAxisLabel(
                            value,
                            criteriaMap,
                            criteriaUnitValues,
                        );
                    },
                    margin: this.isCompareScreen ? 20 : 26,
                    rich: this.isCompareScreen
                        ? (Constants.CHART_RICH_SMALL as any)
                        : (Constants.CHART_RICH as any),
                },
            },
            radiusAxis: {
                ...(this.compareMax() > 0 ? { max: this.compareMax() } : {}),
            },
            polar: {
                radius: this.isCompareScreen ? "48%" : "62%",
                center: this.isCompareScreen ? ["50%", "50%"] : ["50%", "47%"],
            },
            series: isInverted
                ? criteriaSetArray.map((criteriaName) => ({
                      name: criteriaName,
                      type: "bar",
                      coordinateSystem: "polar",
                      avoidLabelOverlap: true,
                      data: (criteriaSeries?.[criteriaName] || []).map((item: any) => ({
                          value: item.value,
                          label: {
                              formatter: () => {
                                  return `${item.impact.unitValue} ${item.impact.unit}`;
                              },
                          },
                          impact: item.impact,
                      })),
                      stack: "a",
                      emphasis: {
                          focus: "series",
                      },
                  }))
                : noErrorRadialChartData.map((item: any) => ({
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
                type: "scroll",
                data: isInverted
                    ? criteriaSetArray
                    : noErrorRadialChartData.map((item: any) => item.tier),
                formatter: (param: any) => {
                    if (isInverted) {
                        return param;
                    }
                    return this.existingTranslation(param, "digital-services");
                },
            },
            color: Constants.COLOR,
        };
    }

    createStackBarChartConfig(
        chartData: DigitalServiceFootprint[],
        isInverted: boolean,
    ): EChartsOption {
        // Calculate total values for each criteria and sort in decreasing order
        const criteriaTotals = new Map<string, number>();
        const criteriaUnitValues: { [key: string]: { total: number; unit: string } } = {};

        chartData.forEach((tierData) => {
            tierData.impacts.forEach((impact) => {
                const twoWordsImpact = this.extractTwoWordsCriteria(impact.criteria);
                const criteriaName = this.getCriteriaTranslation(twoWordsImpact);
                criteriaTotals.set(
                    criteriaName,
                    (criteriaTotals.get(criteriaName) || 0) + impact.sipValue,
                );

                if (!criteriaUnitValues[criteriaName]) {
                    criteriaUnitValues[criteriaName] = { total: 0, unit: impact.unit };
                }
                criteriaUnitValues[criteriaName].total += impact.unitValue;
            });
        });

        this.xAxisInput = Array.from(criteriaTotals.keys()).sort(
            (a, b) => (criteriaTotals.get(b) || 0) - (criteriaTotals.get(a) || 0),
        );

        // Build series data with optimized impact lookup
        const seriesData: any[] = [];

        chartData.forEach((tierData) => {
            const tierName = this.existingTranslation(tierData.tier, "digital-services");

            // Build impact map for O(1) lookup
            const impactMap = new Map<string, any>();
            tierData.impacts.forEach((impact) => {
                const twoWordsImpact = this.extractTwoWordsCriteria(impact.criteria);
                const criteriaName = this.getCriteriaTranslation(twoWordsImpact);
                impactMap.set(criteriaName, impact);
            });

            const data = this.xAxisInput.map((criteriaName) => {
                const impact = impactMap.get(criteriaName);
                return impact
                    ? {
                          value: impact.sipValue,
                          unitValue: impact.unitValue,
                          unit: impact.unit,
                      }
                    : {
                          value: 0,
                          unitValue: 0,
                          unit: "",
                      };
            });

            seriesData.push({
                name: tierName,
                type: "bar",
                stack: "total",
                emphasis: {
                    focus: "series",
                },
                data: data,
            });
        });
        const showZoom = this.xAxisInput.length >= Constants.TOTAL_VISIBLE_GRAPH_ITEMS;

        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const dataObj = params.data || { value: 0, unitValue: 0, unit: "" };
                    const sipValue = dataObj.value || 0;
                    const dimensionLabel = params.name; // x-axis label (criteria name)
                    const seriesName = params.seriesName; // tier name

                    return `
                        <div style="display: flex; align-items: center; height: 30px;">
                            <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                                params.color
                            }; border-radius: 50%; margin-right: 5px;"></span>
                            <span style="font-weight: bold; margin-right: 15px;">${seriesName}</span>
                            <div>${dimensionLabel} : ${this.integerPipe.transform(sipValue)} ${this.translate.instant("common.peopleeq-min")} </div>
                        </div>
                    `;
                },
            },
            legend: {
                type: "scroll",
                data: seriesData.map((s) => s.name),
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "10%",
                containLabel: true,
            },
            dataZoom: [
                {
                    show: showZoom,
                    startValue: this.xAxisInput[0],
                    endValue: this.xAxisInput[Constants.TOTAL_VISIBLE_GRAPH_ITEMS - 1],
                },
            ],
            xAxis: {
                type: "category",
                data: this.xAxisInput.map((criteriaName) => {
                    return {
                        value: criteriaName,
                        textStyle: {
                            color: getColorFormatter(
                                !!this.criteriaMap[criteriaName]?.status?.error,
                                this.enableDataInconsistency,
                            ),
                        },
                    };
                }),
                axisLabel: {
                    interval: 0,
                    rotate: this.xAxisInput.length > 5 ? 45 : 0,
                    formatter: (value: string) => {
                        return this.formatAxisLabel(
                            value,
                            this.criteriaMap,
                            criteriaUnitValues,
                        );
                    },
                    rich: this.isCompareScreen
                        ? (Constants.CHART_RICH_SMALL as any)
                        : (Constants.CHART_RICH as any),
                },
            },
            yAxis: {
                type: "value",
            },
            series: seriesData,
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
