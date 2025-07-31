/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, inject, Input, Signal } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { sortByProperty } from "sort-by-property";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import {
    ConstantApplicationFilter,
    Filter,
} from "src/app/core/interfaces/filter.interface";
import { ApplicationFootprint, Stat } from "src/app/core/interfaces/footprint.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../../abstract-dashboard";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";

@Component({
    selector: "app-application-multicriteria-footprint",
    templateUrl: "./application-multicriteria-footprint.component.html",
})
export class ApplicationMulticriteriaFootprintComponent extends AbstractDashboard {
    @Input() footprint: ApplicationFootprint[] = [];
    @Input() filterFields: ConstantApplicationFilter[] = [];
    showDataConsistencyBtn = false;
    showInconsitencyGraph = false;
    criteriaMap: StatusCountMap = {};
    xAxisInput: string[] = [];
    protected footprintStore = inject(FootprintStoreService);
    private readonly filterService = inject(FilterService);

    selectedInventoryDate: string = "";
    domainFilter: string[] = [];
    appCount: number = 0;

    applicationStats = computed<Stat[]>(() => {
        const localFootprint = this.appComponent.formatLifecycleImpact(this.footprint);
        return this.computeApplicationStats(
            localFootprint,
            this.footprintStore.applicationSelectedFilters(),
        );
    });

    options: Signal<EChartsOption> = computed(() => {
        return this.loadBarChartOption(
            this.footprint,
            this.footprintStore.applicationSelectedFilters(),
        );
    });

    constructor(
        private readonly router: Router,
        private readonly route: ActivatedRoute,
        private readonly appComponent: InventoriesApplicationFootprintComponent,
        override translate: TranslateService,
        override globalStore: GlobalStoreService,
        override integerPipe: IntegerPipe,
        override decimalsPipe: DecimalsPipe,
    ) {
        super(translate, integerPipe, decimalsPipe, globalStore);
    }

    onChartClick(event: any) {
        const translatedCriteria = this.translate.instant("criteria");

        const uri = this.getUriFromCriterias(translatedCriteria, event.name);

        if (uri) {
            this.router.navigate([`../${uri}`], {
                relativeTo: this.route,
            });
        }
    }

    selectedStackBarClick(criteriaName: string): void {
        this.onChartClick({ name: criteriaName });
    }

    getUriFromCriterias(translatedCriteria: any, criteria: string): string | undefined {
        let uri = undefined;
        for (const key in translatedCriteria) {
            if (translatedCriteria[key].title === criteria) {
                uri = key;
                break;
            }
        }
        return uri;
    }

    checkStatusIndicatorOk(statusIndicator: string): number {
        return statusIndicator === Constants.DATA_QUALITY_STATUS.ok ? 1 : 0;
    }

    checkStatusIndicatorError(statusIndicator: string): number {
        return statusIndicator === Constants.DATA_QUALITY_STATUS.error ? 1 : 0;
    }

    loadBarChartOption(
        barChartData: ApplicationFootprint[],
        selectedFilters: Filter,
    ): EChartsOption {
        const xAxis: any[] = [];
        const yAxis: any[] = [];
        const unitImpact: any[] = [];
        const unit: string[] = [];
        const impactOrder: any[] = [];
        this.xAxisInput = [];
        this.criteriaMap = {};

        barChartData.forEach((data) => {
            const translatedTitle = this.translate.instant(
                `criteria.${data.criteria}`,
            ).title;
            let sumSip = 0;
            let sumUnit = 0;
            data.impacts.forEach((impact) => {
                if (!impact.impact) {
                    impact.impact = 0;
                    impact.sip = 0;
                }
                if (this.filterService.getFilterincludes(selectedFilters, impact)) {
                    sumSip += impact.sip;
                    sumUnit += impact.impact;

                    const criteriaStatus = this.criteriaMap[translatedTitle]?.status;
                    const statusIndicator = impact.statusIndicator;

                    if (criteriaStatus) {
                        criteriaStatus.ok += this.checkStatusIndicatorOk(statusIndicator);
                        criteriaStatus.error +=
                            this.checkStatusIndicatorError(statusIndicator);
                        criteriaStatus.total += 1;
                    } else {
                        this.criteriaMap[translatedTitle] = {
                            status: {
                                ok: this.checkStatusIndicatorOk(statusIndicator),
                                error: this.checkStatusIndicatorError(statusIndicator),
                                total: 1,
                            },
                        };
                    }
                }
            });
            impactOrder.push({
                criteria: data.criteriaTitle,
                unit: data.unit,
                sipImpact: sumSip,
                unitImpact: sumUnit,
            });
        });
        impactOrder.sort(sortByProperty("sipImpact", "desc"));
        impactOrder.forEach((impact) => {
            xAxis.push(impact.criteria);
            yAxis.push(impact.sipImpact);
            unitImpact.push(impact.unitImpact);
            unit.push(impact.unit);
        });
        this.xAxisInput = xAxis;
        // sort statusIndicator key by criteria
        this.criteriaMap = Object.keys(this.criteriaMap)
            .sort((a, b) => this.xAxisInput.indexOf(a) - this.xAxisInput.indexOf(b))
            .reduce((acc: StatusCountMap, key) => {
                acc[key] = this.criteriaMap[key];
                return acc;
            }, {});

        setTimeout(() => {
            this.showDataConsistencyBtn = Object.values(this.criteriaMap).some(
                (f) => f.status?.error,
            );
        });

        return {
            tooltip: {
                show: true,
                renderMode: "html",
                formatter: (params: any) => {
                    return `
                        <div style="display: flex; align-items: center; height: 30px;">
                            <span style="font-weight: bold; margin-right: 15px;">${
                                xAxis[params.dataIndex]
                            } : </span>
                            <div>
                            ${
                                unitImpact[params.dataIndex] < 1
                                    ? "< 1"
                                    : unitImpact[params.dataIndex].toFixed(0)
                            }
                            ${unit[params.dataIndex]} </div>
                        </div>
                    `;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            xAxis: [
                {
                    type: "category",
                    data: xAxis,
                    axisLabel: {
                        formatter: (value: any) => {
                            const isAllImpactsOK =
                                this.criteriaMap[value].status.error <= 0;
                            return isAllImpactsOK
                                ? `{grey|${value}}`
                                : `{redBold| \u24d8} {red|${value}}`;
                        },
                        rich: Constants.CHART_RICH as any,
                        margin: 15,
                        rotate: 30,
                    },
                },
            ],
            yAxis: [
                {
                    type: "value",
                },
            ],
            series: [
                {
                    name: `${this.translate.instant(
                        "inventories-footprint.application.graph-mc",
                    )}`,
                    type: "bar",
                    data: yAxis,
                },
            ],
            color: Constants.BLUE_COLOR,
        };
    }

    private computeApplicationStats(
        applications: ApplicationFootprint[],
        filters: Filter,
    ): Stat[] {
        applications = applications || [];
        let applicationCount = 0;
        let appNameList: string[] = [];
        applications.forEach((application) => {
            application.impacts.forEach((impact) => {
                let { applicationName } = impact;
                if (
                    this.filterService.getFilterincludes(filters, impact) &&
                    !appNameList.includes(applicationName)
                ) {
                    appNameList.push(applicationName);
                    applicationCount += 1;
                }
            });
        });

        this.appCount = applicationCount;
        return [
            {
                label: this.decimalsPipe.transform(this.appCount),
                value: isNaN(this.appCount) ? undefined : this.appCount,
                description: this.translate.instant(
                    "inventories-footprint.application.tooltip.nb-app",
                ),
                title: this.translate.instant(
                    "inventories-footprint.application.applications",
                ),
            },
        ];
    }
}
