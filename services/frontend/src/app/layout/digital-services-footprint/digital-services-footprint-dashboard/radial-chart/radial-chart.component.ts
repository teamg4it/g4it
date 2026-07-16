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
                const translatedCriteria = this.getCriteriaTranslation(
                    impact.criteria.split(" ").slice(0, 2).join(" "),
                );
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

    onChartClick(params: any) {
        const isInverted = this.isAxisInverted();
        const isStackBar = this.shouldShowStackBarChart();

        // When inverted, criteria are in series (seriesName), otherwise in data name
        if (isInverted) {
            // In inverted mode, criteria name is in seriesName
            const key = Object.keys(this.translate.instant("criteria")).find(
                (key) =>
                    this.translate.instant("criteria")[key].title === params.seriesName,
            );
            if (key) {
                this.selectedCriteriaChange.emit(key);
            }
        } else {
            // In normal mode
            if (isStackBar && params.componentType === "series") {
                // Stack bar chart: criteria is in params.name (xAxis label)
                const key = Object.keys(this.translate.instant("criteria")).find(
                    (key) =>
                        this.translate.instant("criteria")[key].title === params.name,
                );
                if (key) {
                    this.selectedCriteriaChange.emit(key);
                }
            } else if (params.data && params.data.impact) {
                // Radial chart: criteria is in the impact data
                this.selectedCriteriaChange.emit(params.data.impact.criteria);
            }
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
                const twoWordsImpact = impact.criteria.split(" ").slice(0, 2).join(" ");
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
                    const twoWordsImpact = impact.criteria
                        .split(" ")
                        .slice(0, 2)
                        .join(" ");
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

                    if (isInverted) {
                        const criteriaName = params.seriesName;
                        const tierData = noErrorRadialChartData[dataIndex];
                        impact = criteriaSeries?.[criteriaName]?.[dataIndex]?.impact;
                        if (!impact) return "";
                        label = criteriaName;
                        const tierName = this.existingTranslation(
                            tierData.tier,
                            "digital-services",
                        );
                        return `<div style="display: flex; align-items: center; height: 30px;">
                        <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                            params.color
                        }; border-radius: 50%; margin-right: 5px;"></span>
                        <span style="font-weight: bold; margin-right: 15px;">${label}</span>
                    </div>
                        <div>${tierName} : ${this.integerPipe.transform(impact.sipValue)}
                        ${this.translate.instant("common.peopleeq-min")}<br>
                        ${this.decimalsPipe.transform(impact.unitValue)} ${impact.unit}
                        </div>
                    </div>
                `;
                    } else {
                        const seriesIndex = params.seriesIndex;
                        impact = noErrorRadialChartData[seriesIndex].impacts[dataIndex];
                        label = this.existingTranslation(
                            noErrorRadialChartData[seriesIndex].tier,
                            "digital-services",
                        );
                        return `<div style="display: flex; align-items: center; height: 30px;">
                        <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                            params.color
                        }; border-radius: 50%; margin-right: 5px;"></span>
                        <span style="font-weight: bold; margin-right: 15px;">${label}</span>
                    </div>
                        <div>${this.getCriteriaTranslation(
                            impact.criteria.split(" ").slice(0, 2).join(" "),
                        )} : ${this.integerPipe.transform(impact.sipValue)}
                        ${this.translate.instant("common.peopleeq-min")}<br>
                        ${this.decimalsPipe.transform(impact.unitValue)} ${impact.unit}
                        </div>
                    </div>
                `;
                    }
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
                          const twoWordsImpact = impact.criteria
                              .split(" ")
                              .slice(0, 2)
                              .join(" ");
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
                            const hasError = !!criteriaMap[value]?.status?.error;
                            return getLabelFormatter(
                                hasError,
                                this.enableDataInconsistency,
                                value,
                            );
                        } else {
                            const hasError = !!criteriaMap[value].status.error;
                            const unitData = criteriaUnitValues[value];
                            const unitValueText = unitData
                                ? `\n (${this.decimalsPipe.transform(unitData.total)} ${unitData.unit})`
                                : "";

                            const maxCharacters = 20;
                            const truncatedValue =
                                value.length > maxCharacters
                                    ? value.substring(0, maxCharacters) + "…"
                                    : value;

                            const shortUnitValue =
                                unitValueText.length > maxCharacters
                                    ? unitValueText.substring(0, maxCharacters - 2) + "…"
                                    : unitValueText;

                            return getLabelFormatter(
                                hasError,
                                this.enableDataInconsistency,
                                truncatedValue + shortUnitValue,
                            );
                        }
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
                show: true,
                data: isInverted
                    ? criteriaSetArray
                    : noErrorRadialChartData.map((item: any) => item.tier),
                formatter: (param: any) => {
                    return isInverted
                        ? param
                        : this.existingTranslation(param, "digital-services");
                },
            },
            color: Constants.COLOR,
        };
    }

    createStackBarChartConfig(
        chartData: DigitalServiceFootprint[],
        isInverted: boolean,
    ): EChartsOption {
        let showZoom = true;
        // Get unique criteria
        const criteriaSet = new Set<string>();
        chartData.forEach((tierData) => {
            tierData.impacts.forEach((impact) => {
                const twoWordsImpact = impact.criteria.split(" ").slice(0, 2).join(" ");
                criteriaSet.add(this.getCriteriaTranslation(twoWordsImpact));
            });
        });
        this.xAxisInput = Array.from(criteriaSet);

        // Build series data
        const seriesData: any[] = [];

        // X-axis: criteria, Series: tiers (stacked)
        chartData.forEach((tierData) => {
            const tierName = this.existingTranslation(tierData.tier, "digital-services");
            const data = this.xAxisInput.map((criteriaName) => {
                const impact = tierData.impacts.find((imp) => {
                    const twoWordsImpact = imp.criteria.split(" ").slice(0, 2).join(" ");
                    return this.getCriteriaTranslation(twoWordsImpact) === criteriaName;
                });
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
        if (this.xAxisInput.length < Constants.MAX_NUMBER_OF_BARS_TO_BE_DISPLAYED) {
            showZoom = false;
        }
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
                            <div>${dimensionLabel} : ${this.integerPipe.transform(
                                sipValue,
                            )} ${this.translate.instant("common.peopleeq-min")} </div>
                        </div>
                    `;
                },
            },
            legend: {
                data: seriesData.map((s) => s.name),
                bottom: 0,
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
                    endValue:
                        this.xAxisInput[Constants.MAX_NUMBER_OF_BARS_TO_BE_DISPLAYED - 1],
                },
            ],
            xAxis: {
                type: "category",
                data: this.xAxisInput,
                axisLabel: {
                    interval: 0,
                    rotate: this.xAxisInput.length > 5 ? 45 : 0,
                },
            },
            yAxis: {
                type: "value",
                axisLabel: {
                    formatter: (value: number) => this.integerPipe.transform(value),
                },
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
