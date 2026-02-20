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
    signal,
    SimpleChanges,
} from "@angular/core";

import { EChartsOption } from "echarts";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import {
    Criteria,
    CriteriaCalculated,
    Criterias,
    Datacenter,
    EchartPieDataItem,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
} from "src/app/core/interfaces/footprint.interface";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { getLifeCycleList, getLifeCycleMap } from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../abstract-dashboard";

@Component({
    selector: "app-inventories-critere-footprint",
    templateUrl: "./inventories-critere-footprint.component.html",
})
export class InventoriesCritereFootprintComponent
    extends AbstractDashboard
    implements OnChanges
{
    protected footprintStore = inject(FootprintStoreService);
    private readonly footprintService = inject(FootprintService);
    @Input() footprint: Criterias = {} as Criterias;
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
    selectedDimension = signal(this.dimensions[0]);
    totalValue = 0;
    criteriaMap: StatusCountMap = {};
    xAxisInput: string[] = [];

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
            this.selectedDimension(),
            this.filterFields,
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

    options: Signal<EChartsOption> = computed(() => {
        return this.renderChart(
            this.criteriaCalculated(),
            this.selectedDimension(),
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
                <div class="font-bold">${name}:</div>
                <div class="ml-2">${value}</div>
            </div>
         `;
    }
}
