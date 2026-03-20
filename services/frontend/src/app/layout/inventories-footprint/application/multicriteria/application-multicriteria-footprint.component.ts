/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, inject, input, Input, signal, Signal } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import {
    GraphDescriptionContent,
    StatusCountMap,
} from "src/app/core/interfaces/digital-service.interfaces";
import {
    ConstantApplicationFilter,
    Filter,
    TransformedDomain,
} from "src/app/core/interfaces/filter.interface";
import {
    ApplicationFootprint,
    CriteriaCalculated,
    Criterias,
    FootprintCalculated,
    Impact,
    Stat,
} from "src/app/core/interfaces/footprint.interface";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import {
    getColorFormatter,
    getLabelFormatter,
} from "src/app/core/service/mapper/graphs-mapper";
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
    inventory = input<Inventory>();
    showInconsitencyGraph = false;
    criteriaMap: StatusCountMap = {};
    xAxisInput: string[] = [];
    currentLang: string = this.translate.currentLang;
    criteriakeys = Object.keys(this.translate.translations[this.currentLang]["criteria"]);
    protected footprintStore = inject(FootprintStoreService);
    private readonly filterService = inject(FilterService);
    private readonly footprintService = inject(FootprintService);

    selectedInventoryDate: string = "";
    domainFilter: string[] = [];
    appCount: number = 0;
    dimensions = Constants.APPLICATION_DIMENSIONS;
    selectedDimension = signal(this.dimensions[0]);
    textDescriptionImpacts: {
        text: string;
        impactName: string;
        impactNameVisible: string;
    }[] = [];

    applicationStats = computed<Stat[]>(() => {
        const localFootprint = this.appComponent.formatLifecycleImpact(this.footprint);
        return this.computeApplicationStats(
            localFootprint,
            this.footprintStore.applicationSelectedFilters(),
        );
    });

    criteriaCalculated: Signal<CriteriaCalculated> = computed(() => {
        const criteriaFootprint = this.footprint.reduce((acc, f) => {
            acc[f.criteria] = {
                impacts: f.impacts as any,
                label: f.criteria,
                unit: f.unit,
            };
            return acc;
        }, {} as Criterias);
        const filFields = this.filterFields
            .filter((fil) => !fil.children)
            .map((fil) => fil.field);
        filFields.push("subDomain");
        const filtersWithSubdomain: Filter<string | TransformedDomain> = {
            ...this.footprintStore.applicationSelectedFilters(),
            subDomain: this.extractCheckedLabels(
                this.footprintStore.applicationSelectedFilters()["domain"] as any,
            ),
        };
        delete filtersWithSubdomain["domain"];

        const { footprintCalculated, criteriaCountMap, impactsWithMaxDimensions } =
            this.footprintService.calculate(
                criteriaFootprint,
                filtersWithSubdomain,
                this.selectedDimension(),
                filFields,
            );

        // sort footprint by criteria
        for (const data of footprintCalculated) {
            data.impacts.sort(
                (a, b) =>
                    this.criteriakeys.indexOf(a.criteria) -
                    this.criteriakeys.indexOf(b.criteria),
            );
        }
        // sort statusIndicator key by criteria
        const sortedCriteriaCountMap: StatusCountMap = Object.keys(criteriaCountMap)
            .sort((a, b) => this.criteriakeys.indexOf(a) - this.criteriakeys.indexOf(b))
            .reduce((acc: StatusCountMap, key) => {
                acc[key] = criteriaCountMap[key];
                return acc;
            }, {});

        return {
            footprints: footprintCalculated,
            hasError: footprintCalculated.some((f) => f.status.error),
            total: {
                impact: footprintCalculated.reduce(
                    (sum, current) => sum + current.total.impact,
                    0,
                ),
                sip: footprintCalculated.reduce(
                    (sum, current) => sum + current.total.sip,
                    0,
                ),
            },
            criteriasCount: sortedCriteriaCountMap,
            impactsWithMaxDimensions,
        };
    });

    options: Signal<EChartsOption> = computed(() => {
        return this.renderChart(this.criteriaCalculated(), this.selectedDimension());
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

    renderChart(
        criteriaCalculated: CriteriaCalculated,
        selectedView: string,
    ): EChartsOption {
        const footprintCalculated = criteriaCalculated.footprints;

        if (footprintCalculated.length === 0) {
            this.xAxisInput = [];
            return {};
        }
        this.xAxisInput = this.footprint
            .flatMap((f) => f.criteria)
            .sort((a, b) => this.criteriakeys.indexOf(a) - this.criteriakeys.indexOf(b))
            .map((criteria) => {
                return this.translate.instant(`criteria.${criteria}`).title;
            });

        const criteriaCountMap = criteriaCalculated.criteriasCount || {};

        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const dataIndex = params.dataIndex;
                    const seriesIndex = params.seriesIndex;
                    const impact = footprintCalculated[seriesIndex].impacts[dataIndex];
                    const name = this.existingTranslation(
                        footprintCalculated[seriesIndex].data,
                        selectedView,
                    );
                    return `
                            <div style="display: flex; align-items: center; height: 30px;">
                                <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                                    params.color
                                }; border-radius: 50%; margin-right: 5px;"></span>
                                <span style="font-weight: bold; margin-right: 15px;">${name}</span>
                                <div>${this.translate.instant(`criteria.${impact.criteria}`).title} : ${this.integerPipe.transform(
                                    impact.sumSip,
                                )} ${this.translate.instant("common.peopleeq-min")} </div>
                            </div>
                        `;
                },
            },
            angleAxis: {
                type: "category",
                data: footprintCalculated[0].impacts.map((impact) => {
                    return {
                        value: impact.criteria,
                        textStyle: {
                            color: getColorFormatter(
                                !!criteriaCountMap[impact.criteria].status.error,
                                this.inventory()?.enableDataInconsistency!,
                            ),
                        },
                    };
                }),
                axisLabel: {
                    formatter: (value: any) => {
                        const title = this.translate.instant(`criteria.${value}`).title;
                        const hasError = !!criteriaCountMap[value].status.error;
                        return getLabelFormatter(
                            hasError,
                            this.inventory()?.enableDataInconsistency!,
                            title,
                        );
                    },
                    rich: Constants.CHART_RICH as any,
                    margin: 26,
                },
            },
            radiusAxis: {
                name: this.translate.instant("common.peopleeq"),
                nameLocation: "end",
                // THIS increases distance from chart
                nameGap: 21,
                nameTextStyle: {
                    fontStyle: "italic",
                },
            },
            polar: {
                radius: "62%",
                center: ["50%", "47%"],
            },
            series: footprintCalculated.map((item: FootprintCalculated) => ({
                name: item.data,
                type: "bar",
                coordinateSystem: "polar",
                data: item.impacts.map((impact: Impact) => ({
                    value: impact.sumSip,
                    label: {
                        formatter: () => {
                            return [
                                impact.sumImpact,
                                this.translate.instant(`criteria.${impact.criteria}`)
                                    .unit,
                            ].join(" ");
                        },
                    },
                })),
                stack: "a",
                emphasis: {
                    focus: "series",
                },
            })),
            avoidLabelOverlap: true,
            legend: {
                type: "scroll",
                bottom: 0,
                data: footprintCalculated.map((item: FootprintCalculated) => item.data),
                formatter: (param: any) => {
                    return this.existingTranslation(param, selectedView);
                },
            },
            color: Constants.COLOR,
        };
    }

    stackChartClick(event: string) {
        const key = Object.keys(this.translate.instant("criteria")).find(
            (key) => this.translate.instant("criteria")[key].title === event,
        );
        if (key) {
            this.onChartClick({ name: key });
        }
    }

    onChartClick(event: any) {
        if (event?.name) {
            this.router.navigate([`../${event.name}`], {
                relativeTo: this.route,
            });
        }
    }

    selectedStackBarClick(criteriaName: string): void {
        this.onChartClick({ name: criteriaName });
    }

    checkStatusIndicatorOk(statusIndicator: string): number {
        return statusIndicator === Constants.DATA_QUALITY_STATUS.ok ? 1 : 0;
    }

    checkStatusIndicatorError(statusIndicator: string): number {
        return statusIndicator === Constants.DATA_QUALITY_STATUS.error ? 1 : 0;
    }

    private computeApplicationStats(
        applications: ApplicationFootprint[],
        filters: Filter,
    ): Stat[] {
        applications = applications || [];
        let applicationCount = 0;
        let appNameList: string[] = [];
        for (const application of applications) {
            for (const impact of application.impacts) {
                let { applicationName } = impact;
                if (
                    this.filterService.getFilterincludes(filters, impact) &&
                    !appNameList.includes(applicationName)
                ) {
                    appNameList.push(applicationName);
                    applicationCount += 1;
                }
            }
        }

        this.appCount = applicationCount;
        return [
            {
                label: this.decimalsPipe.transform(this.appCount),
                value: Number.isNaN(this.appCount) ? undefined : this.appCount,
                description: this.translate.instant(
                    "inventories-footprint.application.tooltip.nb-app",
                ),
                title: this.translate.instant(
                    "inventories-footprint.application.applications",
                ),
            },
        ];
    }

    extractCheckedLabels(data: any[]): string[] {
        if (!data) return [];
        const result: string[] = [];

        for (const item of data) {
            // Include "All" if checked
            if (item?.label === "All" && item?.checked) {
                result.push(item?.label);
            }

            // Include all checked children labels (including 'unknown')
            if (Array.isArray(item?.children)) {
                for (const child of item.children) {
                    if (child?.checked && child?.label) {
                        result.push(child?.label);
                    }
                }
            }
        }

        return result;
    }

    getContentText = computed((): GraphDescriptionContent => {
        let translationKey: string;
        let textDescription: string = "";
        let textResourceDescription: string = "";

        translationKey = "ds-graph-description.global-vision.";

        textDescription = this.getTextDescription(
            translationKey,
            this.criteriaCalculated(),
        );
        return {
            description: this.translate.instant(`${translationKey}description`, {
                criteria: this.footprint
                    .map(
                        (impact) =>
                            this.translate.instant(`criteria.${impact.criteria}`).title,
                    )
                    .join(", "),
                module: this.translate.instant("ds-graph-module.inventory"),
            }),
            scale: this.translate.instant(`${translationKey}scale`),
            textDescription: textDescription,
            textResourceDescription: textResourceDescription,
            analysis: this.translate.instant(`${translationKey}analysis`, {
                module: this.translate.instant("ds-graph-module.inventory"),
            }),
            toGoFurther: this.translate.instant(
                `${translationKey}inventory-to-go-further`,
            ),
        };
    });

    getTextDescription(
        translationKey: string,
        criteriaCalculated: CriteriaCalculated,
    ): string {
        let textDescription = "";
        let textImpacts = [];
        const firstPrefix = this.translate.instant(
            `${translationKey}text-description-first-prefix`,
        );
        const iteratePrefix = this.translate.instant(
            `${translationKey}text-description-iterate-prefix`,
        );
        for (const [
            index,
            impact,
        ] of criteriaCalculated?.impactsWithMaxDimensions?.entries() ?? []) {
            const prefix = index === 0 ? firstPrefix : iteratePrefix;

            if (index === 0) {
                textDescription += this.translate.instant(
                    `${translationKey}text-description`,
                );
            }
            textImpacts.push({
                text:
                    prefix +
                    this.translate.instant(`${translationKey}text-description-iterate`, {
                        impactName: impact.title,
                        impactValue: this.integerPipe.transform(impact.peopleeq),
                        resource: impact.maxCriteria.name,
                        resourceValue: this.integerPipe.transform(
                            impact.maxCriteria.peopleeq,
                        ),
                        rawValue: this.decimalsPipe.transform(impact.raw),
                        unit: impact.unite,
                        resourceRawValue: this.decimalsPipe.transform(
                            impact.maxCriteria.raw,
                        ),
                        resourceUnit: impact.unite,
                    }),
                impactName: impact.name,
                impactNameVisible: impact.title,
            });
        }
        this.textDescriptionImpacts = textImpacts;
        return textDescription;
    }

    handleImpactClick(impactName: any) {
        this.onChartClick({ name: impactName });
    }
}
