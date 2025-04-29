/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { inject, Injectable } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Constants } from "src/constants";
import { Filter } from "../../interfaces/filter.interface";
import {
    Criterias,
    Datacenter,
    Impact,
    PhysicalEquipment,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
    Stat,
} from "../../interfaces/footprint.interface";
import { InVirtualEquipmentRest } from "../../interfaces/input.interface";
import { InventoryFilterSet } from "../../interfaces/inventory.interfaces";
import { DecimalsPipe } from "../../pipes/decimal.pipe";
import { IntegerPipe } from "../../pipes/integer.pipe";
import { transformEquipmentType } from "../mapper/array";

@Injectable({
    providedIn: "root",
})
export class InventoryUtilService {
    private translate = inject(TranslateService);
    private integerPipe = inject(IntegerPipe);
    private decimalsPipe = inject(DecimalsPipe);

    maxCriteriaAndStep(footprint: Criterias): string[] {
        let maxCriteriaLength = -1;
        let maxCriteria = "climate-change";

        for (let criteria in footprint) {
            if (!footprint[criteria] || !footprint[criteria].impacts) continue;
            const criteriaLength = footprint[criteria].impacts.length;
            if (criteriaLength > maxCriteriaLength) {
                maxCriteriaLength = criteriaLength;
                maxCriteria = criteria;
            }
        }

        let maxStepLength = -1;
        let maxStep = "UTILISATION";

        const sizeBySteps = footprint[maxCriteria]?.impacts.reduce((p: any, c) => {
            const name = c.acvStep;
            if (!p.hasOwnProperty(name)) {
                p[name] = 0;
            }
            p[name]++;
            return p;
        }, {});

        for (const step in sizeBySteps) {
            if (sizeBySteps[step] > maxStepLength) {
                maxStepLength = sizeBySteps[step];
                maxStep = step;
            }
        }

        return [maxCriteria, maxStep];
    }

    computeEquipmentStats(
        equipments: [
            PhysicalEquipmentAvgAge[],
            PhysicalEquipmentLowImpact[],
            PhysicalEquipmentsElecConsumption[],
        ],
        filters: Filter<string>,
        filterFields: string[],
        footprint: Criterias,
    ): Stat[] {
        const equipmentsAvgAge = equipments[0];
        const equipmentsLowImpact = equipments[1];
        const equipmentsElecConsumption = equipments[2];

        const filtersSet: InventoryFilterSet = {};
        filterFields.forEach((field) => (filtersSet[field] = new Set(filters[field])));

        const hasAllFilters = Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );

        const impacts = footprint[this.maxCriteriaAndStep(footprint)[0]]?.impacts
            .filter((impact) => impact.acvStep === this.maxCriteriaAndStep(footprint)[1])
            .filter((i) => i.status !== Constants.CLOUD_SERVICES);

        const physicalEquipmentCount = hasAllFilters
            ? impacts?.reduce((n, impact) => n + impact.countValue, 0)
            : impacts
                  ?.filter((impact) => this.isItemPresent(impact, filtersSet))
                  ?.reduce((n, impact) => n + impact.countValue, 0);

        const filteredEquipmentsAvgAge = hasAllFilters
            ? equipmentsAvgAge
            : equipmentsAvgAge.filter((equipment) => {
                  return this.isEquipmentPresent(equipment, filtersSet, false);
              });

        let physicalEquipmentSum = 0;
        let poidsSum = 0;

        filteredEquipmentsAvgAge.forEach((physicalEquipment) => {
            let { poids, ageMoyen } = physicalEquipment;
            poids = poids || 0;
            ageMoyen = ageMoyen || 0;

            physicalEquipmentSum += poids * ageMoyen;
            poidsSum += poids;
        });

        const filteredEquipmentsLowImpact = hasAllFilters
            ? equipmentsLowImpact
            : equipmentsLowImpact.filter((equipment) => {
                  return this.isEquipmentPresent(equipment, filtersSet, true);
              });

        let physicalEquipmentTotalCount = 0;
        let lowImpactPhysicalEquipmentCount = 0;

        filteredEquipmentsLowImpact.forEach((physicalEquipment) => {
            let { quantite, lowImpact: isLowImpact } = physicalEquipment;

            quantite = quantite || 0;

            physicalEquipmentTotalCount += quantite;
            if (isLowImpact) {
                lowImpactPhysicalEquipmentCount += quantite;
            }
        });

        const filteredEquipmentsElecConsumption = hasAllFilters
            ? equipmentsElecConsumption
            : equipmentsElecConsumption.filter((equipment) => {
                  return this.isEquipmentPresent(equipment, filtersSet, false);
              });

        let elecConsumptionSum = 0;

        filteredEquipmentsElecConsumption.forEach((physicalEquipment) => {
            let { elecConsumption } = physicalEquipment;

            elecConsumptionSum += elecConsumption;
        });

        return this.getEquipmentStats(
            physicalEquipmentCount,
            physicalEquipmentCount == 0 ? undefined : physicalEquipmentSum / poidsSum,
            (lowImpactPhysicalEquipmentCount / physicalEquipmentTotalCount) * 100,
            elecConsumptionSum,
        );
    }

    computeCloudStats(
        inVirtualEquipments: InVirtualEquipmentRest[],
        filters: Filter<string>,
        filterFields: string[],
    ): Stat[] {
        const filtersSet: InventoryFilterSet = this.createFiltersSet(
            filters,
            filterFields,
        );
        const hasAllFilters = this.checkAllFilters(filtersSet);

        const cloudLength = hasAllFilters
            ? inVirtualEquipments.reduce((n, impact) => n + (impact?.quantity ?? 0), 0)
            : inVirtualEquipments
                  .filter((impact) =>
                      this.isItemPresent(impact as any as Impact, filtersSet),
                  )
                  .reduce((n, impact) => n + (impact?.quantity ?? 0), 0);

        return this.getCloudStats(cloudLength);
    }

    private createFiltersSet(
        filters: Filter<string>,
        filterFields: string[],
    ): InventoryFilterSet {
        const filtersSet: InventoryFilterSet = {};
        filterFields.forEach((field) => (filtersSet[field] = new Set(filters[field])));
        return filtersSet;
    }

    private checkAllFilters(filtersSet: InventoryFilterSet): boolean {
        return Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );
    }

    getCloudStats(count: number = NaN) {
        return [
            {
                label: this.decimalsPipe.transform(count),
                value: isNaN(count) ? undefined : count,
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.nb-cloud",
                ),
                title: this.translate.instant(
                    "inventories-footprint.global.cloud-instances",
                ),
                lang: "en",
            },
        ];
    }

    getEquipmentStats(
        count: number = NaN,
        avgAge: number = NaN,
        lowImpact: number = NaN,
        elecConsumption: number = NaN,
    ) {
        return [
            {
                label: this.decimalsPipe.transform(count),
                value: isNaN(count) ? undefined : count,
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.nb-eq",
                ),
                title: this.translate.instant("inventories-footprint.global.equipments"),
            },
            {
                label: this.decimalsPipe.transform(avgAge),
                value: isNaN(avgAge) ? undefined : avgAge,
                unit: this.translate.instant("inventories-footprint.global.years"),
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.ave-age",
                ),
                title: this.translate.instant("inventories-footprint.global.ave-age"),
            },
            {
                label: `${this.integerPipe.transform(lowImpact)}`,
                value: isNaN(lowImpact) ? undefined : lowImpact,
                unit: "%",
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.low-impact",
                ),
                title: this.translate.instant("inventories-footprint.global.low-impact"),
            },
            {
                label: `${this.decimalsPipe.transform(elecConsumption)}`,
                unit: this.translate.instant("inventories-footprint.global.kwh"),
                value: isNaN(elecConsumption) ? undefined : Math.round(elecConsumption),
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.elec-consumption",
                ),
                title: this.translate.instant(
                    "inventories-footprint.global.elec-consumption",
                ),
            },
        ];
    }

    computeDataCenterStats(
        filters: Filter<string>,
        filterFields: string[],
        datacenters: Datacenter[] = [],
    ): Stat[] {
        const filtersSet: InventoryFilterSet = {};
        filterFields.forEach((field) => (filtersSet[field] = new Set(filters[field])));

        const hasAllFilters = Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );

        const filteredDatacenters = hasAllFilters
            ? datacenters
            : datacenters.filter((datacenter) => {
                  return this.isItemPresent(datacenter, filtersSet);
              });

        let datacenterPhysicalEquipmentCount = 0;
        let datacenterSum = 0;
        const datacenterNames = new Set<string>();

        filteredDatacenters.forEach((datacenter) => {
            let { dataCenterName, physicalEquipmentCount, pue } = datacenter;

            pue = pue || 0;
            dataCenterName = dataCenterName || "";
            physicalEquipmentCount = physicalEquipmentCount || 0;

            datacenterPhysicalEquipmentCount += physicalEquipmentCount;
            datacenterSum += physicalEquipmentCount * pue;
            datacenterNames.add(dataCenterName);
        });

        return this.getDatacenterStats(
            datacenterNames.size,
            datacenterSum / datacenterPhysicalEquipmentCount,
        );
    }

    isItemPresent(item: Impact | Datacenter, filtersSet: InventoryFilterSet): boolean {
        return this.isPresent(item, filtersSet, false, this.valueImpact);
    }

    isEquipmentPresent(
        equipment:
            | PhysicalEquipment
            | PhysicalEquipmentLowImpact
            | PhysicalEquipmentsElecConsumption,
        filtersSet: InventoryFilterSet,
        islowImpact: boolean,
    ): boolean {
        return this.isPresent(equipment, filtersSet, islowImpact, this.valueEquipment);
    }

    isPresent<
        T extends
            | Impact
            | Datacenter
            | PhysicalEquipment
            | PhysicalEquipmentLowImpact
            | PhysicalEquipmentsElecConsumption,
    >(
        item: T,
        filtersSet: InventoryFilterSet,
        islowImpact: boolean,
        valueFn: (v: T, field: string, islowImpact: boolean) => string | null,
    ): boolean {
        let isPresent = true;
        for (const field in filtersSet) {
            let value = valueFn(item, field, islowImpact)!;
            if (!value) value = Constants.EMPTY;
            if (!filtersSet[field].has(value)) {
                isPresent = false;
                break;
            }
        }
        return isPresent;
    }

    valueEquipment(
        v:
            | PhysicalEquipment
            | PhysicalEquipmentLowImpact
            | PhysicalEquipmentsElecConsumption,
        field: string,
    ) {
        switch (field) {
            case "country":
                return v.country;
            case "entity":
                return v.nomEntite;
            case "equipment":
                return v.type;
            case "status":
                return v.statut;
            default:
                return null;
        }
    }

    valueImpact(v: Impact | Datacenter, field: string) {
        switch (field) {
            case "country":
                return v.country;
            case "entity":
                return v.entity;
            case "equipment":
                return v.equipment;
            case "status":
                return v.status;
            default:
                return null;
        }
    }

    getDatacenterStats(count: number = NaN, avgPue: number = NaN) {
        return [
            {
                label: this.decimalsPipe.transform(count),
                value: isNaN(count) ? undefined : count,
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.nb-dc",
                ),
                title: this.translate.instant("inventories-footprint.global.datacenters"),
            },
            {
                label: this.decimalsPipe.transform(avgPue),
                value: isNaN(avgPue) ? undefined : avgPue,
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.ave-pue",
                ),
                title: this.translate.instant("inventories-footprint.global.ave-pue"),
            },
        ];
    }

    removeOrganizationNameFromType(
        equipment: [
            PhysicalEquipmentAvgAge[],
            PhysicalEquipmentLowImpact[],
            PhysicalEquipmentsElecConsumption[],
        ],
        currentOrganization: string,
    ): [
        PhysicalEquipmentAvgAge[],
        PhysicalEquipmentLowImpact[],
        PhysicalEquipmentsElecConsumption[],
    ] {
        return equipment.map((eq) =>
            eq.map((i) => ({
                ...i,
                type: transformEquipmentType(i.type, currentOrganization),
            })),
        ) as [
            PhysicalEquipmentAvgAge[],
            PhysicalEquipmentLowImpact[],
            PhysicalEquipmentsElecConsumption[],
        ];
    }

    removeOrganizationNameFromCriteriaType(
        footprint: Criterias,
        currentOrganization: string,
    ): Criterias {
        return Object.fromEntries(
            Object.entries(footprint).map(([key, value]) => [
                key,
                {
                    ...value,
                    impacts: value.impacts?.map((i) => ({
                        ...i,
                        equipment: transformEquipmentType(
                            i?.equipment!,
                            currentOrganization,
                        ),
                    })),
                },
            ]),
        );
    }
}
