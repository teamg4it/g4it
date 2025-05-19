/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, effect, inject, signal } from "@angular/core";

import { TranslateService } from "@ngx-translate/core";
import {
    Criterias,
    Datacenter,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
    Stat,
} from "src/app/core/interfaces/footprint.interface";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { InventoryUtilService } from "src/app/core/service/business/inventory-util.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { AbstractDashboard } from "../abstract-dashboard";

@Component({
    selector: "app-datacenter-equipment-stats",
    templateUrl: "./datacenter-equipment-stats.component.html",
})
export class DataCenterEquipmentStatsComponent extends AbstractDashboard {
    private store = inject(FootprintStoreService);
    private inventoryUtilService = inject(InventoryUtilService);
    @Input() footprint: Criterias = {} as Criterias;
    @Input() filterFields: string[] = [];
    @Input() datacenters: Datacenter[] = [];
    @Input() equipments: [
        PhysicalEquipmentAvgAge[],
        PhysicalEquipmentLowImpact[],
        PhysicalEquipmentsElecConsumption[],
    ] = [[], [], []];
    @Input() inVirtualEquipments: InVirtualEquipmentRest[] = [];

    datacenterStats = signal<Stat[]>([]);
    cloudStats = signal<Stat[]>([]);
    equipmentStats = signal<Stat[]>([]);

    constructor() {
        super(
            inject(TranslateService),
            inject(IntegerPipe),
            inject(DecimalsPipe),
            inject(GlobalStoreService),
        );
        effect(() => {
            (async () => {
                const res = await this.inventoryUtilService.computeEquipmentStats(
                    this.equipments,
                    this.store.filters(),
                    this.filterFields,
                    this.footprint,
                );
                this.equipmentStats.set(res);
            })();

            (async () => {
                const res = await this.inventoryUtilService.computeCloudStats(
                    this.inVirtualEquipments,
                    this.store.filters(),
                    this.filterFields,
                );
                this.cloudStats.set(res);
            })();

            (async () => {
                const res = await this.inventoryUtilService.computeDataCenterStats(
                    this.store.filters(),
                    this.filterFields,
                    this.datacenters,
                );
                this.datacenterStats.set(res);
            })();
        });
    }
}
