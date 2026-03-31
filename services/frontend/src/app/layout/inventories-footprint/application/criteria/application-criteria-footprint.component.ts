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
    OnChanges,
    signal,
    Signal,
    SimpleChanges,
} from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
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
import { AbstractDashboard } from "../../abstract-dashboard";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
@Component({
    selector: "app-application-criteria-footprint",
    templateUrl: "./application-criteria-footprint.component.html",
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
    selectedDimension = signal(this.dimensions[0]);
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
        let result: any = {};
        for (const data of barChartData) {
            if (data.criteria === selectedCriteria) {
                for (const impact of data.impacts) {
                    if (!impact.impact) {
                        impact.impact = 0;
                        impact.sip = 0;
                    }
                    if (this.filterService.getFilterincludes(selectedFilters, impact)) {
                        switch (this.footprintStore.appGraphType()) {
                            case "global":
                                this.computeImpactOrder(
                                    impact,
                                    impact.domain,
                                    selectedDimension,
                                );
                                break;
                            case "domain":
                                if (impact.domain === this.footprintStore.appDomain()) {
                                    this.computeImpactOrder(
                                        impact,
                                        impact.subDomain,
                                        selectedDimension,
                                    );
                                }
                                break;
                            case "subdomain":
                                if (
                                    impact.domain === this.footprintStore.appDomain() &&
                                    impact.subDomain ===
                                        this.footprintStore.appSubDomain()
                                ) {
                                    this.computeImpactOrder(
                                        impact,
                                        impact.applicationName,
                                        selectedDimension,
                                    );
                                }
                                break;
                            case "application":
                                if (
                                    impact.domain === this.footprintStore.appDomain() &&
                                    impact.subDomain ===
                                        this.footprintStore.appSubDomain() &&
                                    impact.applicationName ===
                                        this.footprintStore.appApplication()
                                ) {
                                    this.computeImpactOrder(
                                        impact,
                                        impact.virtualEquipmentName,
                                        selectedDimension,
                                    );
                                }
                                break;
                        }
                    }
                }
            }
        }
        result = this.initGraphData(this.impactOrder);
        return result;
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
        const xAxis: string[] = [];
        const yAxis: any[] = [];
        const unitImpact: number[] = [];
        const subdomainCount: number[] = [];
        const appCount: number[] = [];
        const equipmentList: string[] = [];
        const environmentList: string[] = [];
        const clusterList: string[] = [];
        const status: StatusCountMap = {};
        for (const impact of impactOrder) {
            let subdomainList: string[] = [];
            let appList: string[] = [];

            const repartitionEntries = Object.entries(impact.repartYaxis ?? {});
            switch (this.footprintStore.appGraphType()) {
                case "global": {
                    xAxis.push(impact.domain);
                    for (const [key, value] of repartitionEntries) {
                        const { raw, sip, apps, subDomain } =
                            value as RepartitionYAxisKeys;
                        const data = [impact.domain, raw, sip, apps, subDomain];

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
                    unitImpact.push(impact.unitImpact);
                    status[impact.domain] = { status: impact.status };
                    subdomainCount.push(new Set(impact.subdomains).size);
                    appCount.push(new Set(impact.apps).size);
                    break;
                }
                case "domain":
                    xAxis.push(impact.subdomain);
                    //////
                    for (const [key, value] of repartitionEntries) {
                        const { raw, sip, apps, subDomain } =
                            value as RepartitionYAxisKeys;
                        const data = [impact.subdomain, raw, sip, apps, subDomain];
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
                    //////
                    unitImpact.push(impact.unitImpact);
                    status[impact.subdomain] = { status: impact.status };
                    for (const app of impact.apps) {
                        if (!appList.includes(app)) {
                            appList.push(app);
                        }
                    }
                    appCount.push(appList.length);
                    break;
                case "subdomain":
                    xAxis.push(impact.app);
                    /////
                    for (const [key, value] of repartitionEntries) {
                        const { raw, sip, apps, subDomain } =
                            value as RepartitionYAxisKeys;
                        const data = [impact.app, raw, sip, apps, subDomain];

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
                    /////

                    unitImpact.push(impact.unitImpact);
                    status[impact.app] = { status: impact.status };
                    break;
                case "application":
                    xAxis.push(impact.virtualEquipmentName);
                    //////
                    for (const [key, value] of repartitionEntries) {
                        const {
                            raw,
                            sip,
                            apps,
                            subDomain,
                            cluster,
                            environment,
                            equipment,
                        } = value as RepartitionYAxisKeys;
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
                    ///////
                    unitImpact.push(impact.unitImpact);
                    equipmentList.push(impact.equipment);
                    environmentList.push(impact.environment);
                    clusterList.push(impact.cluster);
                    status[impact.virtualEquipmentName] = { status: impact.status };
                    break;
            }
        }
        return {
            xAxis,
            yAxis,
            unitImpact,
            subdomainCount,
            appCount,
            equipmentList,
            environmentList,
            clusterList,
            status,
        };
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
