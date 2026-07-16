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
            // In normal mode, criteria is in the impact data
            this.selectedCriteriaChange.emit(params.data.impact.criteria);
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

    selectedStackBarClick(event: string): void {
        const key = Object.keys(this.translate.instant("criteria")).find(
            (key) => this.translate.instant("criteria")[key].title === event,
        );
        if (key) {
            this.selectedCriteriaChange.emit(key);
        }
    }
}
