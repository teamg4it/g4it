/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    Input,
    InputSignal,
    Signal,
    computed,
    inject,
    input,
    signal,
} from "@angular/core";
import { EChartsOption } from "echarts";
import { Constants } from "src/constants";

import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { PrimeTemplate } from "primeng/api";
import { Button } from "primeng/button";
import { SelectModule } from "primeng/select";
import {
    GraphDescriptionContent,
    StatusCountMap,
} from "src/app/core/interfaces/digital-service.interfaces";
import {
    CriteriaCalculated,
    Criterias,
    Datacenter,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
} from "src/app/core/interfaces/footprint.interface";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import * as InventoryMultiCriteriaViewMapper from "src/app/core/service/mapper/inventory-multicriteria-graph-mapper";
import { createStackBarChartConfig } from "src/app/core/service/mapper/multicriteria-stack-bar-chart.mapper";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { AbstractDashboard } from "../abstract-dashboard";

import { TranslatePipe } from "@ngx-translate/core";
import { NgxEchartsDirective } from "ngx-echarts";
import { InverseAxisButtonComponent } from "../../common/inverse-axis-button/inverse-axis-button.component";
import { StackBarChartComponent } from "../../common/stack-bar-chart/stack-bar-chart.component";
import { GraphDescriptionComponent } from "../../digital-services-footprint/digital-services-footprint-dashboard/graph-description/graph-description.component";

@Component({
    selector: "app-inventories-multicriteria-footprint",
    templateUrl: "./inventories-multicriteria-footprint.component.html",
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
export class InventoriesMultiCriteriaFootprintComponent extends AbstractDashboard {
    protected readonly store = inject(FootprintStoreService);
    private readonly footprintService = inject(FootprintService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    currentLang: string = this.translate.currentLang;
    criteriakeys = Object.keys(this.translate.translations[this.currentLang]["criteria"]);
    footprint: InputSignal<Criterias> = input({});
    @Input() filterFields: string[] = [];
    @Input() datacenters: Datacenter[] = [];
    @Input() inVirtualEquipments: InVirtualEquipmentRest[] = [];
    @Input() equipments: [
        PhysicalEquipmentAvgAge[],
        PhysicalEquipmentLowImpact[],
        PhysicalEquipmentsElecConsumption[],
    ] = [[], [], []];
    inventory = input<Inventory>();
    sourceList = input<string[]>([]);
    showInconsitencyGraph = false;
    isAxisInverted = signal<boolean>(false);
    dimensions = Constants.EQUIPMENT_DIMENSIONS;
    criteriaMap: StatusCountMap = {};
    xAxisInput: string[] = [];
    textDescriptionImpacts: {
        text: string;
        impactName: string;
        impactNameVisible: string;
    }[] = [];

    shouldShowStackBarChart = computed(() => {
        return (
            Object.keys(this.footprint()).length > Constants.MAX_NUMBER_OF_CRITERIA_RADAR
        );
    });

    criteriaCalculated: Signal<CriteriaCalculated> = computed(() => {
        const isInverted = this.isAxisInverted();
        const dimension = this.store.dimension();
        const lifeCycleOrder = LifeCycleUtils.getLifeCycleList();
        const isAlphabetical = isInverted && dimension !== Constants.ACV_STEP;
        const { footprintCalculated, criteriaCountMap, impactsWithMaxDimensions } =
            this.footprintService.calculate(
                this.footprint(),
                this.store.filters(),
                dimension,
                this.filterFields,
                isInverted,
                this.shouldShowStackBarChart(),
            );
        if (isInverted) {
            footprintCalculated.sort(
                (a, b) =>
                    this.criteriakeys.indexOf(a.data) - this.criteriakeys.indexOf(b.data),
            );
        }

        // Only sort impacts by criteria if NOT inverted

        const impactOrder = isInverted ? lifeCycleOrder : this.criteriakeys;

        for (const data of footprintCalculated) {
            data.impacts.sort((a, b) =>
                isAlphabetical
                    ? a.criteria.localeCompare(b.criteria)
                    : impactOrder.indexOf(a.criteria) - impactOrder.indexOf(b.criteria),
            );
        }
        const countOrder = isInverted ? lifeCycleOrder : this.criteriakeys;
        // sort statusIndicator key by criteria
        const sortedCriteriaCountMap: StatusCountMap = Object.keys(criteriaCountMap)
            .sort((a, b) =>
                isAlphabetical
                    ? a.localeCompare(b)
                    : countOrder.indexOf(a) - countOrder.indexOf(b),
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
        return this.renderChart(this.criteriaCalculated(), this.store.dimension());
    });

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

        const isAcvStep = this.store.dimension() === Constants.ACV_STEP;

        // Set xAxisInput based on chart type
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
            this.xAxisInput = Object.keys(this.footprint())
                .sort(
                    (a, b) => this.criteriakeys.indexOf(a) - this.criteriakeys.indexOf(b),
                )
                .map((criteria) => this.translate.instant(`criteria.${criteria}`).title);
        }

        const criteriaCountMap = criteriaCalculated.criteriasCount || {};

        // Return stack bar chart if criteria count > 5
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

        // Return radial chart for 4 or fewer criteria
        return InventoryMultiCriteriaViewMapper.createRadialChartConfig(
            footprintCalculated,
            criteriaCountMap,
            isInverted,
            selectedView,
            this.inventory()?.enableDataInconsistency ?? false,
            this.translate,
            this.integerPipe,
            false,
            undefined,
            Constants.COLOR,
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

        if (isAxisInverted) {
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
                criteria: Object.keys(this.footprint())
                    .map((impact) => this.translate.instant(`criteria.${impact}`).title)
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
