/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    computed,
    inject,
    input,
    Input,
    InputSignal,
    signal,
    Signal,
} from "@angular/core";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { PrimeTemplate } from "primeng/api";
import { Button } from "primeng/button";
import { SelectModule } from "primeng/select";
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
    Stat,
} from "src/app/core/interfaces/footprint.interface";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { getUniqueColorFromText } from "src/app/core/service/mapper/graphs-mapper";
import * as InventoryMultiCriteriaViewMapper from "src/app/core/service/mapper/inventory-multicriteria-graph-mapper";
import { createStackBarChartConfig } from "src/app/core/service/mapper/multicriteria-stack-bar-chart.mapper";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../../abstract-dashboard";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";

import { NgxEchartsDirective } from "ngx-echarts";
import { InverseAxisButtonComponent } from "src/app/layout/common/inverse-axis-button/inverse-axis-button.component";
import { StackBarChartComponent } from "../../../common/stack-bar-chart/stack-bar-chart.component";
import { GraphDescriptionComponent } from "../../../digital-services-footprint/digital-services-footprint-dashboard/graph-description/graph-description.component";

@Component({
    selector: "app-application-multicriteria-footprint",
    templateUrl: "./application-multicriteria-footprint.component.html",
    standalone: true,
    imports: [
        Button,
        SelectModule,
        FormsModule,
        PrimeTemplate,
        StackBarChartComponent,
        NgxEchartsDirective,
        GraphDescriptionComponent,
        TranslatePipe,
        InverseAxisButtonComponent,
    ],
})
export class ApplicationMulticriteriaFootprintComponent extends AbstractDashboard {
    footprint: InputSignal<ApplicationFootprint[]> = input([] as any);
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
    selectedDimension = computed(() => this.footprintStore.appDimension());
    textDescriptionImpacts: {
        text: string;
        impactName: string;
        impactNameVisible: string;
    }[] = [];
    isAxisInverted = signal<boolean>(false);

    shouldShowStackBarChart = computed(() => {
        const isInverted = this.isAxisInverted();

        if (isInverted) {
            // When inverted, count the number of data points (axes) from footprint
            const footprintData = this.footprint();
            const dimension = this.selectedDimension();

            // Count unique dimension values across all criteria
            const uniqueDimensions = new Set<string>();
            footprintData.forEach((criteriaData) => {
                if (criteriaData?.impacts) {
                    criteriaData.impacts.forEach((impact: any) => {
                        if (impact[dimension]) {
                            uniqueDimensions.add(impact[dimension]);
                        }
                    });
                }
            });
            return uniqueDimensions.size > Constants.MAX_NUMBER_OF_CRITERIA_RADAR;
        } else {
            // When not inverted, check number of criteria
            return this.footprint().length > Constants.MAX_NUMBER_OF_CRITERIA_RADAR;
        }
    });

    applicationStats = computed<Stat[]>(() => {
        const localFootprint = this.appComponent.formatLifecycleImpact(this.footprint());
        return this.computeApplicationStats(
            localFootprint,
            this.footprintStore.applicationSelectedFilters(),
        );
    });

    criteriaCalculated: Signal<CriteriaCalculated> = computed(() => {
        const isInverted = this.isAxisInverted();
        const criteriaFootprint = this.footprint().reduce((acc, f) => {
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
                isInverted,
                this.shouldShowStackBarChart(),
            );
        if (isInverted) {
            footprintCalculated.sort(
                (a, b) =>
                    this.criteriakeys.indexOf(a.data) - this.criteriakeys.indexOf(b.data),
            );
        }
        // sort footprint by criteria
        for (const data of footprintCalculated) {
            // sort impacts by criteria/dimension
            data.impacts.sort((a, b) =>
                isInverted
                    ? a.criteria.localeCompare(b.criteria)
                    : this.criteriakeys.indexOf(a.criteria) -
                      this.criteriakeys.indexOf(b.criteria),
            );
        }
        // sort statusIndicator key by criteria

        const sortedCriteriaCountMap: StatusCountMap = Object.keys(criteriaCountMap)
            .sort((a, b) =>
                isInverted
                    ? a.localeCompare(b)
                    : this.criteriakeys.indexOf(a) - this.criteriakeys.indexOf(b),
            )
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
        const isInverted = this.isAxisInverted();
        const showStackBar = this.shouldShowStackBarChart();

        if (footprintCalculated.length === 0) {
            this.xAxisInput = [];
            return {};
        }

        const isAcvStep = this.selectedDimension() === Constants.ACV_STEP;

        if (isInverted) {
            // Determine data source: footprint data for stack bar, criteria for radial
            const dataSource = showStackBar
                ? footprintCalculated?.map((fp) => fp.data) || []
                : footprintCalculated[0]?.impacts.map((impact) => impact.criteria) || [];

            // Apply sorting and transformation
            this.xAxisInput = InventoryMultiCriteriaViewMapper.sortAndTransformAxis(
                dataSource,
                isAcvStep,
                this.translate,
            );
        } else {
            this.xAxisInput = this.footprint()
                .flatMap((f) => f.criteria)
                .sort(
                    (a, b) => this.criteriakeys.indexOf(a) - this.criteriakeys.indexOf(b),
                )
                .map((criteria) => {
                    return this.translate.instant(`criteria.${criteria}`).title;
                });
        }

        const criteriaCountMap = criteriaCalculated.criteriasCount || {};

        if (showStackBar) {
            return createStackBarChartConfig({
                footprints: footprintCalculated,
                criteriaCountMap,
                selectedView,
                enableDataInconsistency:
                    this.inventory()?.enableDataInconsistency ?? false,
                isAxisInverted: isInverted,
                translate: this.translate,
                integerPipe: this.integerPipe,
            });
        }
        return InventoryMultiCriteriaViewMapper.createRadialChartConfig(
            footprintCalculated,
            criteriaCountMap,
            isInverted,
            selectedView,
            this.inventory()?.enableDataInconsistency ?? false,
            this.translate,
            this.integerPipe,
            true,
            getUniqueColorFromText,
        );
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
        const isAxisInverted = this.isAxisInverted();
        let criteriaName: string | undefined;
        // condition for graph, only not description click
        if (isAxisInverted && event?.seriesType) {
            criteriaName = event?.seriesName;

            if (this.shouldShowStackBarChart()) {
                const criteria = this.translate.instant("criteria");

                criteriaName = Object.keys(criteria).find(
                    (key) => criteria[key].title === event?.seriesName,
                );
            }
        } else {
            criteriaName = event?.name;
        }

        if (!criteriaName) {
            return;
        }

        this.router.navigate([`../${criteriaName}`], {
            relativeTo: this.route,
        });
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
                criteria: this.footprint()
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
        const result = InventoryMultiCriteriaViewMapper.getTextDescription(
            translationKey,
            criteriaCalculated,
            this.translate,
            this.integerPipe,
            this.decimalsPipe,
        );
        this.textDescriptionImpacts = result.textImpacts;
        return result.textDescription;
    }

    handleImpactClick(impactName: any) {
        this.onChartClick({ name: impactName });
    }

    onAxisInversionChange(isInverted: boolean): void {
        this.isAxisInverted.set(isInverted);
    }
}
