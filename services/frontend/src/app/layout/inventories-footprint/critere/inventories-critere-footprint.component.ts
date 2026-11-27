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
    Signal,
    SimpleChanges,
} from "@angular/core";

import { NgClass } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslatePipe } from "@ngx-translate/core";
import { EChartsOption, SunburstSeriesOption } from "echarts";
import { NgxEchartsDirective } from "ngx-echarts";
import { PrimeTemplate } from "primeng/api";
import { Button } from "primeng/button";
import { SelectModule } from "primeng/select";
import {
    GraphDescriptionContent,
    StatusCount,
    StatusCountMap,
} from "src/app/core/interfaces/digital-service.interfaces";
import {
    Criteria,
    CriteriaCalculated,
    Criterias,
    Datacenter,
    EchartPieDataItem,
    Impact,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
} from "src/app/core/interfaces/footprint.interface";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { createSunburstChartConfig } from "src/app/core/service/mapper/sunburst-chart.mapper";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { getLifeCycleList, getLifeCycleMap } from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";
import { StackBarChartComponent } from "../../common/stack-bar-chart/stack-bar-chart.component";
import { GraphDescriptionComponent } from "../../digital-services-footprint/digital-services-footprint-dashboard/graph-description/graph-description.component";
import { AbstractDashboard } from "../abstract-dashboard";
import { InventoriesFootprintComponent } from "../inventories-footprint.component";

type SunburstNode = { name: string; value?: number; children?: SunburstNode[] };

// Item categories come from the reference data ("Devices", "External Services", "Non_IT"...)
// or from the backend fallback ("INFRASTRUCTURE"), so they are matched ignoring case and separators.
const SUNBURST_CATEGORY_BY_NORMALIZED_NAME: Record<string, string> = {
    devices: "devices",
    infrastructure: "infrastructure",
    externalservices: "externalServices",
    nonit: "nonIT",
};
const SUNBURST_CATEGORY_KEYS = [
    ...new Set(Object.values(SUNBURST_CATEGORY_BY_NORMALIZED_NAME)),
    "unclassified",
];

@Component({
    selector: "app-inventories-critere-footprint",
    templateUrl: "./inventories-critere-footprint.component.html",
    standalone: true,
    imports: [
        NgClass,
        Button,
        SelectModule,
        FormsModule,
        PrimeTemplate,
        StackBarChartComponent,
        NgxEchartsDirective,
        GraphDescriptionComponent,
        TranslatePipe,
    ],
})
export class InventoriesCritereFootprintComponent
    extends AbstractDashboard
    implements OnChanges
{
    protected footprintStore = inject(FootprintStoreService);
    private readonly footprintService = inject(FootprintService);
    protected readonly inventoryComponent = inject(InventoriesFootprintComponent);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    sourceList = input<string[]>([]);
    @Input() footprint: Criterias = {};
    @Input() criteriaFootprint: Criteria = {} as Criteria;
    @Input() filterFields: string[] = [];
    @Input() datacenters: Datacenter[] = [];
    @Input() equipments: [
        PhysicalEquipmentAvgAge[],
        PhysicalEquipmentLowImpact[],
        PhysicalEquipmentsElecConsumption[],
    ] = [[], [], []];
    @Input() inVirtualEquipments: InVirtualEquipmentRest[] = [];
    inventory = input<Inventory>();
    showInconsitencyGraph = false;
    allErrorData = false;

    dimensions = Constants.EQUIPMENT_DIMENSIONS;
    peopleeq = Constants.PEOPLEEQ;
    totalValue = 0;
    criteriaMap: StatusCountMap = {};
    xAxisInput: string[] = [];
    textDescriptionImpacts: {
        text: string;
        impactName: string;
        impactNameVisible: string;
    }[] = [];

    allFootprintsCriteriaKeys = computed(() => {
        return Object.keys(this.inventoryComponent.allUnmodifiedFootprint());
    });

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            this.showInconsitencyGraph = false;
        }
    }

    criteriaCalculated: Signal<CriteriaCalculated> = computed(() => {
        const criteria = this.footprintStore.criteria();

        const footprint: Criterias = {};
        footprint[criteria] = this.criteriaFootprint;

        const { footprintCalculated } = this.footprintService.calculate(
            footprint,
            this.footprintStore.filters(),
            this.footprintStore.dimension(),
            this.filterFields,
            false,
            false,
        );

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
        };
    });

    showDataConsistency = computed(
        () =>
            !!this.criteriaCalculated().hasError &&
            !!this.inventory()?.enableDataInconsistency,
    );

    options: Signal<EChartsOption> = computed(() => {
        return this.renderChart(
            this.criteriaCalculated(),
            this.footprintStore.dimension(),
            this.footprintStore.unit(),
            this.inventory()!,
        );
    });

    unitOfCriteria = computed(() =>
        this.translate.instant(`criteria.${this.footprintStore.criteria()}.unite`),
    );

    changeUnitInStore(unit: string) {
        this.footprintStore.setUnit(unit);
    }

    infocard = computed(() => {
        return {
            title: this.translate.instant(
                `criteria.${this.footprintStore.criteria()}.inventory-title`,
            ),
            text: this.translate.instant(
                `criteria.${this.footprintStore.criteria()}.inventory-text`,
            ),
        };
    });

    getTotal(unit: string, criteriaCalculated: CriteriaCalculated): number {
        return unit === Constants.PEOPLEEQ
            ? criteriaCalculated.total.sip
            : criteriaCalculated.total.impact;
    }

    checkDataStatus(data: EchartPieDataItem) {
        if (data.status) {
            this.criteriaMap[data.name] = { status: data.status };
        }
    }

    computeToolTipHtml(
        echartsData: EchartPieDataItem[],
        params: any,
        unit: string,
        selectedView: string,
    ) {
        return this.getToolTipHtml(
            echartsData[params.dataIndex],
            unit === Constants.PEOPLEEQ ? Constants.PEOPLEEQ : this.unitOfCriteria(),
            params.color,
            selectedView,
        );
    }

    renderChart(
        criteriaCalculated: CriteriaCalculated,
        selectedView: string,
        unit: string,
        inventory: Inventory,
    ): EChartsOption {
        this.xAxisInput = [];
        this.criteriaMap = {};

        if (criteriaCalculated.footprints.length === 0) {
            return {};
        }

        if (selectedView === "equipment") {
            const filters = this.footprintStore.filters();
            const hasAllFilters = this.filterFields.every((field) =>
                filters[field]?.includes(Constants.ALL),
            );
            const filteredImpacts = hasAllFilters
                ? this.criteriaFootprint.impacts
                : this.criteriaFootprint.impacts.filter((impact) =>
                      this.filterFields.every((field) => {
                          const value =
                              this.footprintService.valueImpact(impact, field) ||
                              Constants.EMPTY;
                          return filters[field]?.includes(value) ?? false;
                      }),
                  );
            const allImpactsGroupedByCategory = Object.groupBy(
                filteredImpacts,
                (i: Impact) => this.getSunburstCategoryKey(i),
            );

            // Convert the grouped impacts into a tree structure suitable for the sunburst chart
            const data: NonNullable<SunburstSeriesOption["data"]> = Object.keys(
                allImpactsGroupedByCategory,
            ).map((category) => {
                const items = Object.groupBy(
                    allImpactsGroupedByCategory[category]!,
                    (i: Impact) => i.sunburstServiceType || i.itemType || i.equipment!,
                );

                return {
                    // Use the translated label if the category is known, otherwise use the raw category name
                    name: SUNBURST_CATEGORY_KEYS.includes(category)
                        ? this.translate.instant(
                              `inventories-footprint.sunburst-category.${category}`,
                          )
                        : category,

                    // Calculate the total value for the category by summing the values of all items in that category
                    children: Object.keys(items).map((item) => {
                        // If the item has a sunburstServiceType, group by sunburstProvider and create children for each provider
                        if (items[item]![0].sunburstServiceType) {
                            const g = Object.groupBy(items[item]!, (i: Impact) =>
                                (i.sunburstProvider ?? "").trim().toUpperCase(),
                            );
                            return {
                                name: this.translate.instant(
                                    `inventories-footprint.sunburst-category.${items[item]![0].sunburstServiceType}`,
                                ),
                                children: Object.entries(g).map(
                                    ([provider, impact]) =>
                                        ({
                                            name: provider || impact![0].equipment!,
                                            value: impact!.reduce(
                                                (sum, current) =>
                                                    sum +
                                                    (unit === Constants.PEOPLEEQ
                                                        ? current.sip
                                                        : current.impact),
                                                0,
                                            ),
                                        }) satisfies SunburstNode,
                                ),
                            } satisfies SunburstNode;
                        }

                        // If the item does not have a sunburstServiceType, return a single node with the total value for that item
                        const itemImpacts = items[item]!;
                        if (itemImpacts.some((impact) => impact.itemModel?.trim())) {
                            const models = Object.groupBy(
                                itemImpacts,
                                (impact: Impact) =>
                                    impact.itemModel?.trim() ||
                                    this.translate.instant(
                                        "inventories-footprint.sunburst-category.unclassified",
                                    ),
                            );
                            return {
                                name: item,
                                children: Object.entries(models).map(
                                    ([model, impacts]) =>
                                        ({
                                            name: model,
                                            value: impacts!.reduce(
                                                (sum, current) =>
                                                    sum +
                                                    (unit === Constants.PEOPLEEQ
                                                        ? current.sip
                                                        : current.impact),
                                                0,
                                            ),
                                        }) satisfies SunburstNode,
                                ),
                            } satisfies SunburstNode;
                        }

                        return {
                            name: item,
                            value: itemImpacts.reduce(
                                (sum, current) =>
                                    sum +
                                    (unit === Constants.PEOPLEEQ
                                        ? current.sip
                                        : current.impact),
                                0,
                            ),
                        } satisfies SunburstNode;
                    }),
                } satisfies SunburstNode;
            });

            this.setSunburstItemStatus(filteredImpacts);

            return createSunburstChartConfig({
                data,
                chartOptions: {
                    tooltip: {
                        trigger: "item",
                        formatter: (params: any) =>
                            this.getSunburstTooltipHtml(params, unit),
                    },
                },
            });
        }

        const lifecycleMap = getLifeCycleMap();
        const echartsData: EchartPieDataItem[] = [];
        const otherData: any = [];

        const total = this.getTotal(unit, criteriaCalculated);
        for (const item of criteriaCalculated.footprints) {
            const sumValue =
                unit === Constants.PEOPLEEQ ? item.total.sip : item.total.impact;
            const translated = lifecycleMap.get(item.data);
            const v: any = {
                value: sumValue,
                name: translated ?? item.data,
                status: item.status,
            };
            let percent = (sumValue / total) * 100;
            if (percent < 1) v.percent = percent;
            percent < 1 ? otherData.push(v) : echartsData.push(v);
        }

        // Push the single data entry for multiple entities with impact less than 1%.
        if (otherData.length > 0) {
            const otherValue = otherData
                .map((data: any) => data.value)
                .reduce((sum: number, current: number) => sum + current, 0);
            const otherOk = otherData
                .map((data: any) => data.status.ok)
                .reduce((sum: number, current: number) => sum + current, 0);
            const otherError = otherData
                .map((data: any) => data.status.error)
                .reduce((sum: number, current: number) => sum + current, 0);
            const otherTotal = otherData
                .map((data: any) => data.status.total)
                .reduce((sum: number, current: number) => sum + current, 0);

            echartsData.push({
                name: this.translate.instant("common.other"),
                value: otherValue,
                status: {
                    ok: otherOk,
                    error: otherError,
                    total: otherTotal,
                },
                otherData: otherData.sort((a: any, b: any) => a.value < b.value),
            });
        }
        if (selectedView == Constants.ACV_STEP) {
            echartsData.sort((a: any, b: any) => {
                return (
                    getLifeCycleList().indexOf(a.name) -
                    getLifeCycleList().indexOf(b.name)
                );
            });
        } else {
            // Sort by alphabetical order
            echartsData.sort((a: any, b: any) => a.name.localeCompare(b.name));
        }

        const errorEchartsData = echartsData.filter((e) => e.status?.error > 0);
        // sort descending of error percentage
        errorEchartsData.sort(
            (a: any, b: any) =>
                b.status?.error! / b.status.total - a.status?.error! / a.status.total,
        );
        this.xAxisInput = errorEchartsData.map((data: any) =>
            data.name === Constants.EMPTY
                ? this.translate.instant("common.empty")
                : (this.translate.instant("acvStep")[data.name] ?? data.name),
        );
        for (const data of errorEchartsData) {
            this.checkDataStatus(data);
        }
        this.allErrorData = echartsData?.every((f) => !f.status.ok);

        return {
            height: 700,
            tooltip: {
                enterable: true,
                hideDelay: 200,
                trigger: "item",
                formatter: (params: any) => {
                    return this.computeToolTipHtml(
                        echartsData,
                        params,
                        unit,
                        selectedView,
                    );
                },
            },
            legend: {
                show: true,
                type: "scroll",
                bottom: -5,
                selectedMode: false,
                formatter: (param: any) => {
                    return this.existingTranslation(param, selectedView, "legend");
                },
            },
            series: [
                {
                    avoidLabelOverlap: true,
                    type: "pie",
                    radius: ["50%", "75%"],
                    center: ["50%", "45%"],
                    // adjust the start angle
                    startAngle: 180,
                    endAngle: 360,
                    label: {
                        show: true,
                        formatter: (param: any) => {
                            // correct the percentage
                            const translatedLabel = this.existingTranslation(
                                param.name,
                                selectedView,
                            );
                            if (
                                inventory.enableDataInconsistency &&
                                param.data.status.error
                            ) {
                                return `{redBold|\u24d8} {red|${translatedLabel}} {grey|${param.percent.toFixed(1)}%}`;
                            } else {
                                return `{grey|${translatedLabel} ${param.percent.toFixed(1)}%}`;
                            }
                        },
                        rich: Constants.CHART_RICH as any,
                    },
                    data: echartsData,
                },
            ],
            color: Constants.COLOR,
        };
    }

    getToolTipHtml(chartValue: any, unit: string, color: any, selectedView: string) {
        let height = 22;
        let maxHeight = 250;
        let tooltipLines = [];

        const translatedUnit =
            unit === Constants.PEOPLEEQ
                ? this.translate.instant(
                      `common.${Constants.PEOPLEEQ.toLocaleLowerCase()}`,
                  )
                : unit;

        const pipe = unit === Constants.PEOPLEEQ ? this.integerPipe : this.decimalsPipe;

        if (["other", "autres"].includes(chartValue.name.toLowerCase())) {
            tooltipLines = chartValue.otherData.map((data: any) =>
                this.getTooltipItemHtml(
                    data.name,
                    `${data.percent.toFixed(3)}% - ${pipe.transform(
                        data.value,
                    )} ${translatedUnit}`,
                ),
            );
        } else {
            tooltipLines = [
                this.getTooltipItemHtml(
                    this.existingTranslation(chartValue.name, selectedView),
                    `${pipe.transform(chartValue.value)} ${translatedUnit}`,
                    color,
                ),
            ];
        }

        height = tooltipLines.length <= 10 ? height * tooltipLines.length : maxHeight;
        const titleDiv =
            tooltipLines.length > 1
                ? `${this.getCircleColorHtml(color)} ${this.translate.instant(
                      "common.otherLegend",
                  )}`
                : "";

        return `
            ${titleDiv}
            <div class="mt-1" style="display: flex; flex-direction: column; align-items: baseline; height: ${height}px; overflow-y:auto">
                ${tooltipLines.join("")}
            </div>
        `;
    }

    /**
     * Key of the first sunburst ring for an impact. Cloud and AI services always
     * belong to external services; known categories are normalized to a translation key,
     * unknown custom categories keep their raw name.
     */
    getSunburstCategoryKey(impact: Impact): string {
        if (impact.sunburstServiceType) return "externalServices";
        const raw = impact.itemCategory?.trim();
        if (!raw) return "unclassified";
        return (
            SUNBURST_CATEGORY_BY_NORMALIZED_NAME[
                raw.toLowerCase().replaceAll(/[^a-z]/g, "")
            ] ?? raw
        );
    }

    /**
     * Feeds the data consistency stack-bar chart in the Item view: one entry per
     * sunburst item (second ring), keeping only items with errors, worst first.
     */
    setSunburstItemStatus(impacts: Impact[]) {
        const statusByItem = new Map<string, StatusCount>();
        for (const impact of impacts) {
            const name = impact.sunburstServiceType
                ? this.translate.instant(
                      `inventories-footprint.sunburst-category.${impact.sunburstServiceType}`,
                  )
                : impact.itemType || impact.equipment || Constants.EMPTY;
            const status = statusByItem.get(name) ?? { ok: 0, error: 0, total: 0 };
            if (impact.statusIndicator === Constants.DATA_QUALITY_STATUS.ok) {
                status.ok++;
            } else {
                status.error++;
            }
            status.total++;
            statusByItem.set(name, status);
        }

        this.allErrorData = [...statusByItem.values()].every((s) => !s.ok);

        const itemsWithError = [...statusByItem.entries()]
            .filter(([, status]) => status.error > 0)
            .sort(([, a], [, b]) => b.error / b.total - a.error / a.total);
        this.xAxisInput = itemsWithError.map(([name]) => name);
        for (const [name, status] of itemsWithError) {
            this.criteriaMap[name] = { status };
        }
    }

    getSunburstTooltipHtml(params: any, unit: string) {
        const isPeopleEq = unit === Constants.PEOPLEEQ;
        const translatedUnit = isPeopleEq
            ? this.translate.instant(`common.${Constants.PEOPLEEQ.toLocaleLowerCase()}`)
            : this.unitOfCriteria();
        const pipe = isPeopleEq ? this.integerPipe : this.decimalsPipe;

        return this.getTooltipItemHtml(
            params.name,
            `${pipe.transform(params.value)} ${translatedUnit}`,
            params.color,
        );
    }

    getCircleColorHtml(color: string | undefined = undefined) {
        return color
            ? `<div class="mr-1 pb-1 inline-block" style="width: 9px; height: 9px; background-color: ${color}; border-radius: 50%;"></div>`
            : "";
    }

    getTooltipItemHtml(
        name: string,
        value: string,
        color: string | undefined = undefined,
    ) {
        return `
            <div class="ml-1 mr-2">
                ${this.getCircleColorHtml(color)}
                <span class="font-bold">${name}:</span>
                <span class="ml-2">${value}</span>
            </div>
         `;
    }

    moveToMultiCriteria() {
        void this.router.navigate(["../", "multi-criteria"], {
            relativeTo: this.route,
        });
    }

    getContentText = computed((): GraphDescriptionContent => {
        let translationKey: string;
        let textResourceDescription: string = "";

        translationKey = `ds-graph-description.criteria.`;

        const key =
            "criteria." +
            this.footprintStore.criteria().toLowerCase().replaceAll(" ", "-") +
            ".";

        return {
            description: this.translate.instant(`${translationKey}description`, {
                criteria: Object.keys(this.footprint)
                    .map((impact) => this.translate.instant(`criteria.${impact}`).title)
                    .join(", "),
                module: this.translate.instant("ds-graph-module.inventory"),
            }),
            scale: this.translate.instant(`${key}scale`),
            textDescription: this.getTextDescription(
                translationKey,
                this.criteriaCalculated(),
                this.footprintStore.unit(),
            ),
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

    getTextDescription(
        translationKey: string,
        criteriaCalculated: CriteriaCalculated,
        unit: string,
    ): string {
        let allImpacts: any[] = [];
        const total = this.getTotal(unit, criteriaCalculated);
        for (const item of criteriaCalculated.footprints) {
            const sumValue =
                unit === Constants.PEOPLEEQ ? item.total.sip : item.total.impact;
            const translatedName = getLifeCycleList().includes(item.data)
                ? this.translate.instant(`acvStep.${item.data}`)
                : item.data;
            const v: any = {
                name: translatedName === Constants.EMPTY ? "Empty" : translatedName,
                status: item.status,
                percentage: (sumValue / total) * 100,
                unit: this.criteriaFootprint.unit,
                unitValue: item.total.impact,
                value: item.total.sip,
            };

            allImpacts.push(v);
        }
        const filterImpacts = [...allImpacts]
            .sort((a, b) => b.percentage - a.percentage)
            .slice(0, 3);
        let textDescription = "";
        let textImpacts = [];
        const criteriaKey = this.footprintStore.criteria();
        for (const [index, impact] of filterImpacts.entries()) {
            if (index === 0) {
                textDescription += this.translate.instant(
                    `${translationKey}text-description`,
                    {
                        resource: this.translate.instant(`criteria.${criteriaKey}.title`),
                    },
                );
            }
            textImpacts.push({
                text: this.translate.instant(
                    `${translationKey}text-description-iterate`,
                    {
                        impactName: impact.name,
                        impactValue: this.integerPipe.transform(impact.value),
                        resource: this.translate.instant(`criteria.${criteriaKey}.title`),
                        resourceValue: this.integerPipe.transform(impact.percentage),
                        rawValue: this.decimalsPipe.transform(impact.unitValue),
                        unit: impact.unit,
                    },
                ),
                impactName: impact.tier,
                impactNameVisible: impact.name,
            });
        }
        this.textDescriptionImpacts = textImpacts;
        return textDescription;
    }
}
