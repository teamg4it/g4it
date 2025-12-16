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
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { sortByProperty } from "sort-by-property";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import {
    ConstantApplicationFilter,
    Filter,
    TransformedDomain,
} from "src/app/core/interfaces/filter.interface";
import {
    ApplicationFootprint,
    ApplicationImpact,
    ImpactGraph,
} from "src/app/core/interfaces/footprint.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
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
    allUnmodifiedFilters = input<Filter<string | TransformedDomain>>({});
    showDataConsistencyBtn = false;
    showInconsitencyGraph = false;
    xAxisInput: string[] = [];
    criteriaMap: StatusCountMap = {};
    allCriteriaMap: StatusCountMap = {};
    protected footprintStore = inject(FootprintStoreService);
    private readonly filterService = inject(FilterService);

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

    showDomainByApplication = computed(() => {
        if (this.allUnmodifiedFilters()["domain"]?.length > 2) {
            const domainSelected: any = this.footprintStore
                .applicationSelectedFilters()
                [
                    "domain"
                ].find((d) => (d as TransformedDomain).label === this.footprintStore.appDomain());
            if (domainSelected?.children.length <= 1) {
                return true;
            }
        }
        return false;
    });

    showDomainLabel = computed(() => {
        if (this.allUnmodifiedFilters()["domain"]?.length <= 2) {
            return false;
        }
        return true;
    });

    showSubDomainLabel = computed(() => {
        if (this.allUnmodifiedFilters()["domain"]?.length <= 2) {
            if (
                (this.allUnmodifiedFilters()["domain"][1] as TransformedDomain)?.children
                    ?.length <= 1
            ) {
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
        );
    });

    maxNumberOfBarsToBeDisplayed: number = 10;

    constructor(
        private readonly appComponent: InventoriesApplicationFootprintComponent,
        override translate: TranslateService,
        override integerPipe: IntegerPipe,
        override decimalsPipe: DecimalsPipe,
        override globalStore: GlobalStoreService,
    ) {
        super(translate, integerPipe, decimalsPipe, globalStore);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            this.showInconsitencyGraph = false;
        }
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
                                this.computeImpactOrder(impact, impact.domain);
                                break;
                            case "domain":
                                if (impact.domain === this.footprintStore.appDomain()) {
                                    this.computeImpactOrder(impact, impact.subDomain);
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

    computeImpactOrder(impact: ApplicationImpact, yAxisValue: string) {
        if (this.yAxislist.includes(yAxisValue)) {
            const index = this.yAxislist.indexOf(yAxisValue);
            this.impactOrder[index] = {
                domain: impact.domain,
                sipImpact: this.impactOrder[index].sipImpact + impact.sip,
                unitImpact: this.impactOrder[index].unitImpact + impact.impact,
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

    initGraphData(impactOrder: any[]): any {
        impactOrder.sort(sortByProperty("sipImpact", "desc"));
        const xAxis: string[] = [];
        const yAxis: string[] = [];
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
            switch (this.footprintStore.appGraphType()) {
                case "global":
                    xAxis.push(impact.domain);
                    yAxis.push(impact.sipImpact);
                    unitImpact.push(impact.unitImpact);
                    status[impact.domain] = { status: impact.status };
                    for (const subdomain of impact.subdomains) {
                        if (!subdomainList.includes(subdomain)) {
                            subdomainList.push(subdomain);
                        }
                    }
                    for (const app of impact.apps) {
                        if (!appList.includes(app)) {
                            appList.push(app);
                        }
                    }
                    subdomainCount.push(subdomainList.length);
                    appCount.push(appList.length);
                    break;
                case "domain":
                    xAxis.push(impact.subdomain);
                    yAxis.push(impact.sipImpact);
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
                    yAxis.push(impact.sipImpact);
                    unitImpact.push(impact.unitImpact);
                    status[impact.app] = { status: impact.status };
                    break;
                case "application":
                    xAxis.push(impact.virtualEquipmentName);
                    yAxis.push(impact.sipImpact);
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
    ): EChartsOption {
        const unit = this.selectedCriteria().unite;
        let result: any = {};
        this.impactOrder = [];
        this.yAxislist = [];
        let showZoom: boolean = true;

        result = this.computeData(footprint, selectedFilters, selectedCriteria);

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

        if (result.yAxis.length < 10) {
            showZoom = false;
        }
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    let impact = "";
                    if (result?.unitImpact[params.dataIndex]) {
                        impact = `
                        <span>
                            Impact : ${this.integerPipe.transform(params.value)}
                                    ${this.translate.instant("common.peopleeq-min")}
                                <br>
                            Impact : ${
                                result?.unitImpact[params.dataIndex] < 1
                                    ? "< 1"
                                    : this.decimalsPipe.transform(
                                          result?.unitImpact[params.dataIndex],
                                      )
                            }
                                ${unit}
                                ${
                                    this.footprintStore.appGraphType() === "global"
                                        ? "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.nb-sd",
                                          ) +
                                          " : " +
                                          result.subdomainCount[params.dataIndex] +
                                          "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.nb-app",
                                          ) +
                                          " : " +
                                          result.appCount[params.dataIndex]
                                        : ""
                                }
                                ${
                                    this.footprintStore.appGraphType() === "domain"
                                        ? "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.nb-app",
                                          ) +
                                          " : " +
                                          result.appCount[params.dataIndex]
                                        : ""
                                }
                                ${
                                    this.footprintStore.appGraphType() === "application"
                                        ? "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.cluster",
                                          ) +
                                          " : " +
                                          result.clusterList[params.dataIndex] +
                                          "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.equipment",
                                          ) +
                                          " : " +
                                          result.equipmentList[params.dataIndex] +
                                          "<br>" +
                                          this.translate.instant(
                                              "inventories-footprint.application.tooltip.environnement",
                                          ) +
                                          " : " +
                                          result.environmentList[params.dataIndex]
                                        : ""
                                }
                                <span>
                        `;
                    }

                    return `
                        <div>
                            <span style="font-weight: bold; margin-right: 15px;">${
                                params.name
                            } : </span>
                            ${impact}
                        </div>
                    `;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
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
                        formatter: (value: any) => this.checkImpacts(value),
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
                        "inventories-footprint.application.graph-cv",
                    )}`,
                    type: "bar",
                    data: result.yAxis,
                },
            ],
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
}
