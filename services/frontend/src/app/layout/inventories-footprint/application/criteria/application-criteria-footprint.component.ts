/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { NgClass, NgTemplateOutlet } from "@angular/common";
import {
    Component,
    computed,
    inject,
    input,
    Input,
    OnChanges,
    signal,
    Signal,
    SimpleChanges,
} from "@angular/core";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { NgxEchartsDirective } from "ngx-echarts";
import { PrimeTemplate } from "primeng/api";
import { Button } from "primeng/button";
import { SelectModule } from "primeng/select";
import { sortByProperty } from "sort-by-property";
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
    ApplicationImpact,
    ImpactGraph,
    RepartitionYAxisKeys,
} from "src/app/core/interfaces/footprint.interface";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import {
    getColorFormatter,
    getLabelFormatter,
    getUniqueColorFromText,
} from "src/app/core/service/mapper/graphs-mapper";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { StackBarChartComponent } from "../../../common/stack-bar-chart/stack-bar-chart.component";
import { GraphDescriptionComponent } from "../../../digital-services-footprint/digital-services-footprint-dashboard/graph-description/graph-description.component";
import { AbstractDashboard } from "../../abstract-dashboard";
import { ApplicationCriteriaPieChartComponent } from "../application-criteria-pie-chart/application-criteria-pie-chart.component";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
@Component({
    selector: "app-application-criteria-footprint",
    templateUrl: "./application-criteria-footprint.component.html",
    standalone: true,
    imports: [
        NgClass,
        NgTemplateOutlet,
        SelectModule,
        FormsModule,
        PrimeTemplate,
        StackBarChartComponent,
        NgxEchartsDirective,
        ApplicationCriteriaPieChartComponent,
        GraphDescriptionComponent,
        Button,
        TranslatePipe,
    ],
})
export class ApplicationCriteriaFootprintComponent
    extends AbstractDashboard
    implements OnChanges
{
    @Input() footprint: ApplicationFootprint = {} as ApplicationFootprint;
    @Input() filterFields: ConstantApplicationFilter[] = [];
    @Input() selectedInventoryId!: number;
    inventory = input<Inventory>();
    allUnmodifiedFilters = input<Filter<string | TransformedDomain>>({});
    showDataConsistencyBtn = false;
    showInconsitencyGraph = false;
    xAxisInput: string[] = [];
    criteriaMap: StatusCountMap = {};
    allCriteriaMap: StatusCountMap = {};
    protected footprintStore = inject(FootprintStoreService);
    private readonly filterService = inject(FilterService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    textDescriptionImpacts: {
        text: string;
        impactName: string;
        impactNameVisible: string;
    }[] = [];

    selectedCriteria = computed(() => {
        return this.translate.instant(
            `criteria.${this.footprintStore.applicationCriteria()}`,
        );
    });

    selectedUnit: string = "peopleeq";
    noData: Signal<boolean> = computed(() => {
        return this.checkIfNoData(this.footprintStore.applicationSelectedFilters());
    });
    domainFilter: string[] = [];

    criteriaFootprintSignal = signal([]);

    impactOrder: ImpactGraph[] = [];
    yAxislist: string[] = [];
    dimensions = Constants.APPLICATION_DIMENSIONS;
    selectedDimension = computed(() => this.footprintStore.appDimension());
    showBackButton = computed(() => {
        if (this.allUnmodifiedFilters()["domain"]?.length <= 2) {
            if (
                (this.allUnmodifiedFilters()["domain"][1] as TransformedDomain)?.children
                    ?.length <= 1 &&
                this.footprintStore.appGraphType() === "subdomain"
            ) {
                return false;
            }

            if (this.footprintStore.appGraphType() === "domain") {
                return false;
            }
        }
        return true;
    });

    showDomainByApplication = input<boolean>(false);

    showDomainLabel = computed(() => {
        if (this.allUnmodifiedFilters()["domain"]?.length <= 2) {
            return false;
        }
        return true;
    });

    showSubDomainLabel = computed(() => {
        if (this.allUnmodifiedFilters()["domain"]?.length <= 2) {
            if (
                (this.allUnmodifiedFilters()?.["domain"]?.[1] as TransformedDomain)
                    ?.children?.length <= 1
            ) {
                return false;
            }
        } else {
            const domainSelected: any = this.allUnmodifiedFilters()?.["domain"]?.find(
                (d) =>
                    (d as TransformedDomain)?.label === this.footprintStore?.appDomain(),
            );
            if (domainSelected?.children?.length <= 1) {
                return false;
            }
        }
        return true;
    });

    options: Signal<EChartsOption> = computed(() => {
        const localFootprint = this.appComponent.formatLifecycleImpact([this.footprint]);
        return this.loadBarChartOption(
            this.footprintStore.applicationSelectedFilters(),
            localFootprint,
            this.footprintStore.applicationCriteria(),
            this.inventory()!,
            this.selectedDimension(),
        );
    });

    maxNumberOfBarsToBeDisplayed: number = 10;

    constructor(
        protected readonly appComponent: InventoriesApplicationFootprintComponent,
        override translate: TranslateService,
        override integerPipe: IntegerPipe,
        override decimalsPipe: DecimalsPipe,
        override globalStore: GlobalStoreService,
    ) {
        super(translate, integerPipe, decimalsPipe, globalStore);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes && !changes["showDomainByApplication"]) {
            this.showInconsitencyGraph = false;
        }
    }

    handleImpactClick(impactName: string) {
        this.onChartClick({ name: impactName });
    }

    onChartClick(event: any) {
        if (this.footprintStore.appGraphType() === "global") {
            const domainSelected: any = this.footprintStore
                .applicationSelectedFilters()
                ["domain"].find((d) => (d as TransformedDomain).label === event.name);
            if (domainSelected?.children.length <= 1) {
                this.footprintStore.setDomain(event.name);
                this.footprintStore.setSubDomain(domainSelected?.children[0].label);
                this.footprintStore.setGraphType("subdomain");
            } else {
                this.footprintStore.setGraphType("domain");
                this.footprintStore.setDomain(event.name);
                this.footprintStore.setSubDomain("");
            }
        } else if (this.footprintStore.appGraphType() === "domain") {
            this.footprintStore.setGraphType("subdomain");
            this.footprintStore.setSubDomain(event.name);
        } else if (this.footprintStore.appGraphType() === "subdomain") {
            this.footprintStore.setGraphType("application");
            this.footprintStore.setApplication(event.name);
        }
    }

    onArrowClick() {
        if (this.footprintStore.appGraphType() === "application") {
            this.footprintStore.setGraphType("subdomain");
            this.footprintStore.setApplication("");
        } else if (this.footprintStore.appGraphType() === "subdomain") {
            const domainSelected: any = this.allUnmodifiedFilters()["domain"].find(
                (d) => (d as TransformedDomain).label === this.footprintStore.appDomain(),
            );
            if (domainSelected?.children.length <= 1) {
                this.footprintStore.setGraphType("global");
                this.footprintStore.setDomain("");
                this.footprintStore.setSubDomain("");
            } else {
                this.footprintStore.setGraphType("domain");
                this.footprintStore.setSubDomain("");
            }
        } else if (this.footprintStore.appGraphType() === "domain") {
            this.footprintStore.setGraphType("global");
            this.footprintStore.setDomain("");
            this.footprintStore.setSubDomain("");
        }
    }

    checkIfNoData(selectedFilters: Filter) {
        this.appComponent.formatLifecycleImpact([this.footprint]);
        let hasNoData = true;
        for (const impact of this.footprint?.impacts || []) {
            if (this.filterService.getFilterincludes(selectedFilters, impact)) {
                hasNoData = false;
            }
        }

        return hasNoData;
    }

    computeData(
        barChartData: ApplicationFootprint[],
        selectedFilters: Filter,
        selectedCriteria: string,
        selectedDimension: string,
    ) {
        this.processBarChartData(
            barChartData,
            selectedFilters,
            selectedCriteria,
            selectedDimension,
        );
        return this.initGraphData(this.impactOrder);
    }

    private processBarChartData(
        barChartData: ApplicationFootprint[],
        selectedFilters: Filter,
        selectedCriteria: string,
        selectedDimension: string,
    ): void {
        for (const data of barChartData) {
            if (data.criteria === selectedCriteria) {
                this.processImpacts(data.impacts, selectedFilters, selectedDimension);
            }
        }
    }

    private processImpacts(
        impacts: ApplicationImpact[],
        selectedFilters: Filter,
        selectedDimension: string,
    ): void {
        for (const impact of impacts) {
            this.normalizeImpactValues(impact);
            if (this.filterService.getFilterincludes(selectedFilters, impact)) {
                this.handleImpactByGraphType(impact, selectedDimension);
            }
        }
    }

    private normalizeImpactValues(impact: ApplicationImpact): void {
        if (!impact.impact) {
            impact.impact = 0;
            impact.sip = 0;
        }
    }

    private handleImpactByGraphType(
        impact: ApplicationImpact,
        selectedDimension: string,
    ): void {
        const graphType = this.footprintStore.appGraphType();

        if (graphType === "global") {
            this.computeImpactOrder(impact, impact.domain, selectedDimension);
        } else if (graphType === "domain") {
            this.processDomainImpact(impact, selectedDimension);
        } else if (graphType === "subdomain") {
            this.processSubdomainImpact(impact, selectedDimension);
        } else if (graphType === "application") {
            this.processApplicationImpact(impact, selectedDimension);
        }
    }

    private processDomainImpact(
        impact: ApplicationImpact,
        selectedDimension: string,
    ): void {
        if (impact.domain === this.footprintStore.appDomain()) {
            this.computeImpactOrder(impact, impact.subDomain, selectedDimension);
        }
    }

    private processSubdomainImpact(
        impact: ApplicationImpact,
        selectedDimension: string,
    ): void {
        if (
            impact.domain === this.footprintStore.appDomain() &&
            impact.subDomain === this.footprintStore.appSubDomain()
        ) {
            this.computeImpactOrder(impact, impact.applicationName, selectedDimension);
        }
    }

    private processApplicationImpact(
        impact: ApplicationImpact,
        selectedDimension: string,
    ): void {
        if (
            impact.domain === this.footprintStore.appDomain() &&
            impact.subDomain === this.footprintStore.appSubDomain() &&
            impact.applicationName === this.footprintStore.appApplication()
        ) {
            this.computeImpactOrder(
                impact,
                impact.virtualEquipmentName,
                selectedDimension,
            );
        }
    }

    computeImpactOrder(
        impact: ApplicationImpact,
        yAxisValue: string,
        selectedDimension: string,
    ) {
        const dimensionValue = this.getImpactDimensionValue(impact, selectedDimension);
        if (this.yAxislist.includes(yAxisValue)) {
            const index = this.yAxislist.indexOf(yAxisValue);
            this.impactOrder[index] = {
                domain: impact.domain,
                sipImpact: this.impactOrder[index].sipImpact + impact.sip,
                unitImpact: this.impactOrder[index].unitImpact + impact.impact,
                repartYaxis: {
                    ...this.impactOrder[index]?.repartYaxis,
                    [dimensionValue]: {
                        sip:
                            (this.impactOrder[index].repartYaxis?.[dimensionValue]?.sip ??
                                0) + impact.sip,
                        raw:
                            (this.impactOrder[index].repartYaxis?.[dimensionValue]?.raw ??
                                0) + impact.impact,
                        apps: Array.from(
                            new Set([
                                ...(this.impactOrder[index].repartYaxis?.[dimensionValue]
                                    ?.apps ?? []),
                                impact.applicationName,
                            ]),
                        ),
                        subDomain: Array.from(
                            new Set([
                                ...(this.impactOrder[index].repartYaxis?.[dimensionValue]
                                    ?.subDomain ?? []),
                                impact.subDomain,
                            ]),
                        ),
                        cluster: impact.cluster,
                        environment: impact.environment,
                        equipment: impact.equipmentType,
                    },
                },
                subdomain: impact.subDomain,
                app: impact.applicationName,
                equipment: impact.equipmentType,
                environment: impact.environment,
                virtualEquipmentName: impact.virtualEquipmentName,
                cluster: impact.cluster,
                subdomains: this.impactOrder[index].subdomains.concat(impact.subDomain),
                apps: this.impactOrder[index].apps.concat(impact.applicationName),
                lifecycle: impact.lifeCycle,
                status: {
                    ok:
                        this.impactOrder[index]?.status?.ok +
                        (impact.statusIndicator === Constants.DATA_QUALITY_STATUS.ok
                            ? 1
                            : 0),
                    error:
                        this.impactOrder[index]?.status?.error +
                        (impact.statusIndicator === Constants.DATA_QUALITY_STATUS.error
                            ? 1
                            : 0),
                    total: this.impactOrder[index]?.status?.total + 1,
                },
            };
        } else {
            this.yAxislist.push(yAxisValue);
            this.impactOrder.push({
                domain: impact.domain,
                sipImpact: impact.sip,
                unitImpact: impact.impact,
                subdomain: impact.subDomain,
                repartYaxis: {
                    [dimensionValue]: {
                        sip: impact.sip,
                        raw: impact.impact,
                        apps: [impact.applicationName],
                        subDomain: [impact.subDomain],
                        cluster: impact.cluster,
                        environment: impact.environment,
                        equipment: impact.equipmentType,
                    },
                },
                app: impact.applicationName,
                equipment: impact.equipmentType,
                environment: impact.environment,
                virtualEquipmentName: impact.virtualEquipmentName,
                cluster: impact.cluster,
                subdomains: [impact.subDomain],
                apps: [impact.applicationName],
                lifecycle: impact.lifeCycle,
                status: {
                    ok:
                        impact.statusIndicator === Constants.DATA_QUALITY_STATUS.ok
                            ? 1
                            : 0,
                    error:
                        impact.statusIndicator === Constants.DATA_QUALITY_STATUS.error
                            ? 1
                            : 0,
                    total: 1,
                },
            });
        }
    }

    private getImpactDimensionValue(
        impact: ApplicationImpact,
        selectedDimension: string,
    ): string {
        const value = (impact as unknown as Record<string, unknown>)[selectedDimension];
        return value === undefined || value === null || value === ""
            ? Constants.EMPTY
            : String(value);
    }

    initGraphData(impactOrder: any[]): any {
        impactOrder.sort(sortByProperty("sipImpact", "desc"));
        const result = {
            xAxis: [] as string[],
            yAxis: [] as any[],
            unitImpact: [] as number[],
            subdomainCount: [] as number[],
            appCount: [] as number[],
            equipmentList: [] as string[],
            environmentList: [] as string[],
            clusterList: [] as string[],
            status: {} as StatusCountMap,
        };

        for (const impact of impactOrder) {
            const repartitionEntries = Object.entries(impact.repartYaxis ?? {});
            this.processImpactByGraphType(impact, repartitionEntries, result);
        }

        result.yAxis.sort((a, b) => b.name.localeCompare(a.name));
        return result;
    }

    private processImpactByGraphType(
        impact: any,
        repartitionEntries: [string, any][],
        result: any,
    ): void {
        const graphType = this.footprintStore.appGraphType();

        if (graphType === "global") {
            this.processGlobalGraph(impact, repartitionEntries, result);
        } else if (graphType === "domain") {
            this.processDomainGraph(impact, repartitionEntries, result);
        } else if (graphType === "subdomain") {
            this.processSubdomainGraph(impact, repartitionEntries, result);
        } else if (graphType === "application") {
            this.processApplicationGraph(impact, repartitionEntries, result);
        }
    }

    private addYAxisEntry(yAxis: any[], key: string, data: any[]): void {
        yAxis.push({
            name: key,
            data: [data],
            type: "bar",
            stack: "Ad",
            emphasis: {
                focus: "series",
            },
            itemStyle: {
                color: getUniqueColorFromText(key),
            },
        });
    }

    private processGlobalGraph(
        impact: any,
        repartitionEntries: [string, any][],
        result: any,
    ): void {
        result.xAxis.push(impact.domain);
        for (const [key, value] of repartitionEntries) {
            const { raw, sip, apps, subDomain } = value as RepartitionYAxisKeys;
            const data = [impact.domain, raw, sip, apps, subDomain];
            this.addYAxisEntry(result.yAxis, key, data);
        }
        result.unitImpact.push(impact.unitImpact);
        result.status[impact.domain] = { status: impact.status };
        result.subdomainCount.push(new Set(impact.subdomains).size);
        result.appCount.push(new Set(impact.apps).size);
    }

    private processDomainGraph(
        impact: any,
        repartitionEntries: [string, any][],
        result: any,
    ): void {
        result.xAxis.push(impact.subdomain);
        for (const [key, value] of repartitionEntries) {
            const { raw, sip, apps, subDomain } = value as RepartitionYAxisKeys;
            const data = [impact.subdomain, raw, sip, apps, subDomain];
            this.addYAxisEntry(result.yAxis, key, data);
        }
        result.unitImpact.push(impact.unitImpact);
        result.status[impact.subdomain] = { status: impact.status };
        const appList: string[] = [];
        for (const app of impact.apps) {
            if (!appList.includes(app)) {
                appList.push(app);
            }
        }
        result.appCount.push(appList.length);
    }

    private processSubdomainGraph(
        impact: any,
        repartitionEntries: [string, any][],
        result: any,
    ): void {
        result.xAxis.push(impact.app);
        for (const [key, value] of repartitionEntries) {
            const { raw, sip, apps, subDomain } = value as RepartitionYAxisKeys;
            const data = [impact.app, raw, sip, apps, subDomain];
            this.addYAxisEntry(result.yAxis, key, data);
        }
        result.unitImpact.push(impact.unitImpact);
        result.status[impact.app] = { status: impact.status };
    }

    private processApplicationGraph(
        impact: any,
        repartitionEntries: [string, any][],
        result: any,
    ): void {
        result.xAxis.push(impact.virtualEquipmentName);
        for (const [key, value] of repartitionEntries) {
            const { raw, sip, apps, subDomain, cluster, environment, equipment } =
                value as RepartitionYAxisKeys;
            const data = [
                impact.virtualEquipmentName,
                raw,
                sip,
                apps,
                subDomain,
                cluster,
                environment,
                equipment,
            ];
            this.addYAxisEntry(result.yAxis, key, data);
        }
        result.unitImpact.push(impact.unitImpact);
        result.equipmentList.push(impact.equipment);
        result.environmentList.push(impact.environment);
        result.clusterList.push(impact.cluster);
        result.status[impact.virtualEquipmentName] = { status: impact.status };
    }

    loadBarChartOption(
        selectedFilters: Filter,
        footprint: ApplicationFootprint[],
        selectedCriteria: string,
        inventory: Inventory,
        selectedDimension: string,
    ): EChartsOption {
        const unit = this.selectedCriteria().unite;
        let result: any = {};
        this.impactOrder = [];
        this.yAxislist = [];
        let showZoom: boolean = true;

        result = this.computeData(
            footprint,
            selectedFilters,
            selectedCriteria,
            selectedDimension,
        );
        this.allCriteriaMap = result.status;
        // sort descending of status error
        const sortedCriteriaMap = Object.fromEntries(
            Object.entries(this.allCriteriaMap)
                .filter(([, value]) => value.status.error > 0)
                .sort(([, a], [, b]) => b.status.error - a.status.error),
        );
        this.criteriaMap = sortedCriteriaMap;
        this.xAxisInput = Object.keys(this.criteriaMap);

        setTimeout(() => {
            this.showDataConsistencyBtn = Object.values(this.allCriteriaMap).some(
                (f) => f.status?.error,
            );
        });
        if (result.xAxis.length < 10) {
            showZoom = false;
        }
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    let impact = "";
                    if (params.data[1]) {
                        impact = `
                        <span>
                            Impact : ${this.integerPipe.transform(params.data[2])}
                                    ${this.translate.instant("common.peopleeq-min")}
                                <br>
                            Impact : ${
                                params.data[1] < 1
                                    ? "< 1"
                                    : this.decimalsPipe.transform(params.data[1])
                            }
                                ${unit}
                                ${
                                    this.footprintStore.appGraphType() === "global"
                                        ? "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.nb-sd",
                                          ) +
                                          " : " +
                                          params.data[4]?.length +
                                          "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.nb-app",
                                          ) +
                                          " : " +
                                          params.data[3]?.length
                                        : ""
                                }
                                ${
                                    this.footprintStore.appGraphType() === "domain"
                                        ? "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.nb-app",
                                          ) +
                                          " : " +
                                          params.data[3]?.length
                                        : ""
                                }
                                ${
                                    this.footprintStore.appGraphType() === "application"
                                        ? "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.cluster",
                                          ) +
                                          " : " +
                                          params.data[5] +
                                          "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.equipment",
                                          ) +
                                          " : " +
                                          params.data[7] +
                                          "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.environnement",
                                          ) +
                                          " : " +
                                          params.data[6]
                                        : ""
                                }
                                <span>
                        `;
                    }

                    return `
                        <div>
                            <span style="font-weight: bold; margin-right: 15px;">${
                                params.seriesName
                            } : </span>
                            ${impact}
                        </div>
                    `;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: 25,
                containLabel: true,
            },
            dataZoom: [
                {
                    show: showZoom,
                    startValue: result.xAxis[0],
                    endValue: result.xAxis[this.maxNumberOfBarsToBeDisplayed - 1],
                },
            ],
            xAxis: [
                {
                    type: "category",
                    data: result.xAxis,
                    axisLabel: {
                        color: (value: any) => {
                            const hasError = !!this.allCriteriaMap[value].status.error;
                            return getColorFormatter(
                                hasError,
                                inventory.enableDataInconsistency,
                            );
                        },
                        formatter: (value: any) => {
                            const hasError = !!this.allCriteriaMap[value].status.error;
                            return getLabelFormatter(
                                hasError,
                                inventory.enableDataInconsistency,
                                value,
                            );
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
            series: result.yAxis,
            color: Constants.BLUE_COLOR,
        };
    }

    checkImpacts(value: any) {
        const isAllImpactsOK = this.allCriteriaMap[value].status.error <= 0;
        return isAllImpactsOK ? `{grey|${value}}` : `{redBold| \u24d8} {red|${value}}`;
    }

    selectedStackBarClick(criteriaName: string): void {
        this.onChartClick({ name: criteriaName });
    }

    moveToMultiCriteria() {
        this.router.navigate(["../", "multi-criteria"], {
            relativeTo: this.route,
        });
    }

    getContentText = computed((): GraphDescriptionContent => {
        let translationKey: string;
        let textResourceDescription: string = "";

        translationKey = `ds-graph-description.criteria.`;

        const key =
            "criteria." +
            this.footprintStore.applicationCriteria().toLowerCase().replaceAll(" ", "-") +
            ".";

        return {
            description: this.translate.instant(`${translationKey}description`, {
                criteria: Object.keys(this.footprint)
                    .map((impact) => this.translate.instant(`criteria.${impact}`).title)
                    .join(", "),
                module: this.translate.instant("ds-graph-module.inventory"),
            }),
            scale: this.translate.instant(`${key}scale`),
            textDescription: this.getTextDescription(translationKey, this.options()),
            textResourceDescription: textResourceDescription,
            analysis: this.translate.instant(
                `ds-graph-description.global-vision.analysis`,
                {
                    module: this.translate.instant("ds-graph-module.inventory"),
                },
            ),
            toGoFurther: this.translate.instant(
                `ds-graph-description.global-vision.inventory-to-go-further`,
            ),
        };
    });

    getTextDescription(translationKey: string, options: EChartsOption): string {
        let textDescription = "";
        let textImpacts = [];
        const criteriaKey = this.footprintStore.applicationCriteria();
        const filterImpacts = [...this.impactOrder]
            .sort((a, b) => b.sipImpact - a.sipImpact)
            .slice(0, 3);
        for (const [index, impact] of filterImpacts.entries()) {
            if (index === 0) {
                textDescription += this.translate.instant(
                    `${translationKey}text-description`,
                    {
                        resource: this.translate.instant(`criteria.${criteriaKey}.title`),
                    },
                );
            }
            const repartitionEntries = Object.entries(impact.repartYaxis ?? {}) as [
                string,
                any,
            ][];

            if (repartitionEntries.length === 0) continue;

            const top = repartitionEntries.reduce((max, current) => {
                return (current[1]?.raw ?? 0) > (max[1]?.raw ?? 0) ? current : max;
            }, repartitionEntries[0]);

            const maxCriteria = {
                name: top[0],
                peopleeq: top[1]?.sip ?? 0,
                raw: top[1]?.raw ?? 0,
            };

            textImpacts.push({
                text: this.translate.instant(
                    `${translationKey}inventory-application-text-description-iterate`,
                    {
                        impactName: (options.xAxis as any)?.[0]?.data[index],
                        impactValue: this.integerPipe.transform(impact.sipImpact),
                        resource: maxCriteria.name,
                        resourceValue: this.integerPipe.transform(maxCriteria.peopleeq),
                        rawValue: this.decimalsPipe.transform(impact.unitImpact),
                        unit: this.translate.instant(
                            `criteria.${this.footprintStore.applicationCriteria()}.unite`,
                        ),
                        resourceRawValue: this.decimalsPipe.transform(maxCriteria.raw),
                        resourceUnit: this.translate.instant(
                            `criteria.${this.footprintStore.applicationCriteria()}.unite`,
                        ),
                    },
                ),
                impactName:
                    this.footprintStore.appGraphType() === "application"
                        ? undefined
                        : (options.xAxis as any)?.[0]?.data[index],
                impactNameVisible:
                    this.footprintStore.appGraphType() === "application"
                        ? undefined
                        : (options.xAxis as any)?.[0]?.data[index],
            });
        }
        this.textDescriptionImpacts = textImpacts;
        return textDescription;
    }
}
