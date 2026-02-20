/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, Signal, computed, inject, input, signal } from "@angular/core";
import { EChartsOption } from "echarts";
import { Constants } from "src/constants";

import { ActivatedRoute, Router } from "@angular/router";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import {
    CriteriaCalculated,
    Criterias,
    Datacenter,
    FootprintCalculated,
    Impact,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
} from "src/app/core/interfaces/footprint.interface";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import {
    getColorFormatter,
    getLabelFormatter,
} from "src/app/core/service/mapper/graphs-mapper";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { AbstractDashboard } from "../abstract-dashboard";

@Component({
    selector: "app-inventories-multicriteria-footprint",
    templateUrl: "./inventories-multicriteria-footprint.component.html",
})
export class InventoriesMultiCriteriaFootprintComponent extends AbstractDashboard {
    private readonly store = inject(FootprintStoreService);
    private readonly footprintService = inject(FootprintService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    currentLang: string = this.translate.currentLang;
    criteriakeys = Object.keys(this.translate.translations[this.currentLang]["criteria"]);
    @Input() footprint: Criterias = {} as Criterias;
    @Input() filterFields: string[] = [];
    @Input() datacenters: Datacenter[] = [];
    @Input() inVirtualEquipments: InVirtualEquipmentRest[] = [];
    @Input() equipments: [
        PhysicalEquipmentAvgAge[],
        PhysicalEquipmentLowImpact[],
        PhysicalEquipmentsElecConsumption[],
    ] = [[], [], []];
    inventory = input<Inventory>();
    showInconsitencyGraph = false;
    dimensions = Constants.EQUIPMENT_DIMENSIONS;
    selectedDimension = signal(this.dimensions[0]);
    criteriaMap: StatusCountMap = {};
    xAxisInput: string[] = [];

    criteriaCalculated: Signal<CriteriaCalculated> = computed(() => {
        const { footprintCalculated, criteriaCountMap } = this.footprintService.calculate(
            this.footprint,
            this.store.filters(),
            this.selectedDimension(),
            this.filterFields,
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
        };
    });

    options: Signal<EChartsOption> = computed(() => {
        return this.renderChart(this.criteriaCalculated(), this.selectedDimension());
    });

    renderChart(
        criteriaCalculated: CriteriaCalculated,
        selectedView: string,
    ): EChartsOption {
        const footprintCalculated = criteriaCalculated.footprints;

        if (footprintCalculated.length === 0) {
            this.xAxisInput = [];
            return {};
        }
        this.xAxisInput = Object.keys(this.footprint)
            .sort((a, b) => this.criteriakeys.indexOf(a) - this.criteriakeys.indexOf(b))
            .map((criteria) => this.translate.instant(`criteria.${criteria}`).title);

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
                    margin: 15,
                },
            },
            radiusAxis: {
                name: this.translate.instant("common.peopleeq"),
                nameLocation: "end",
                nameTextStyle: {
                    fontStyle: "italic",
                },
            },
            polar: {
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

    onChartClick(event: any) {
        if (event?.name) {
            this.router.navigate([`../${event.name}`], {
                relativeTo: this.route,
            });
        }
    }
}
