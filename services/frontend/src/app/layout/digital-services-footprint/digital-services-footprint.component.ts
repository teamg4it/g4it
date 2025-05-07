/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, inject, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MenuItem } from "primeng/api";
import { firstValueFrom, lastValueFrom } from "rxjs";
import { sortByProperty } from "sort-by-property";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { InPhysicalEquipmentRest } from "src/app/core/interfaces/input.interface";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InDatacentersService } from "src/app/core/service/data/in-out/in-datacenters.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-digital-services-footprint",
    templateUrl: "./digital-services-footprint.component.html",
})
export class DigitalServicesFootprintComponent implements OnInit {
    private global = inject(GlobalStoreService);
    private digitalServiceStore = inject(DigitalServiceStoreService);
    private inDatacentersService = inject(InDatacentersService);

    digitalService: DigitalService = {} as DigitalService;
    inPhysicalEquipments: InPhysicalEquipmentRest[] = [];
    tabItems: MenuItem[] | undefined;

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        private digitalBusinessService: DigitalServiceBusinessService,
        private route: ActivatedRoute,
        private translate: TranslateService,
    ) {}

    async ngOnInit(): Promise<void> {
        this.global.setLoading(true);
        const uid = this.route.snapshot.paramMap.get("digitalServiceId") ?? "";
        const digitalService = await lastValueFrom(this.digitalServicesData.get(uid));
        // If the digital service is not found, 404 is catched by the interceptor.
        // Therefore we can continue without those verifications.
        this.digitalService = digitalService;

        this.digitalServiceStore.setDigitalService(this.digitalService);
        await this.digitalServiceStore.initInPhysicalEquipments(uid);
        await this.digitalServiceStore.initInVirtualEquipments(uid);

        let inDatacenters = await firstValueFrom(this.inDatacentersService.get(uid));
        if (inDatacenters.length === 0) {
            await firstValueFrom(
                this.inDatacentersService.create({
                    location: "France",
                    name: "Default DC",
                    pue: 1.5,
                    digitalServiceUid: uid,
                }),
            );
            inDatacenters = await firstValueFrom(this.inDatacentersService.get(uid));
        }

        this.digitalServiceStore.setInDatacenters(inDatacenters);
        const referentials = await firstValueFrom(
            this.digitalServicesData.getNetworkReferential(),
        );
        this.digitalServiceStore.setNetworkTypes(referentials);

        const terminalReferentials = await firstValueFrom(
            this.digitalServicesData.getDeviceReferential(),
        );
        this.digitalServiceStore.setTerminalDeviceTypes(terminalReferentials);

        const serverHostRefCompute = await firstValueFrom(
            this.digitalServicesData.getHostServerReferential("Compute"),
        );
        const serverHostRefStorage = await firstValueFrom(
            this.digitalServicesData.getHostServerReferential("Storage"),
        );
        const shortCuts = [
            ...serverHostRefCompute.filter((item) => item.value.startsWith("Server ")),
            ...serverHostRefStorage.filter((item) => item.value.startsWith("Server ")),
        ].sort(sortByProperty("value", "desc"));

        this.digitalServiceStore.setServerTypes([
            ...shortCuts,
            ...serverHostRefCompute
                .filter((item) => !item.value.startsWith("Server "))
                .sort(sortByProperty("value", "asc")),
            ...serverHostRefStorage
                .filter((item) => !item.value.startsWith("Server "))
                .sort(sortByProperty("value", "asc")),
        ]);

        this.global.setLoading(false);
        this.updateTabItems();
        this.digitalBusinessService.initCountryMap();
    }

    updateTabItems() {
        this.tabItems = [
            {
                label: this.translate.instant("digital-services.Terminal"),
                routerLink: "terminals",
                id: "terminals",
            },
            {
                label: this.translate.instant("digital-services.Network"),
                routerLink: "networks",
                id: "networks",
            },
            {
                label: this.translate.instant("digital-services.Server"),
                routerLink: "servers",
                id: "servers",
            },
            {
                label: this.translate.instant("digital-services.CloudService"),
                routerLink: "cloudServices",
                id: "cloudServices",
            },
            {
                label: "Filler",
                separator: true,
                style: { flex: 1 },
                id: "separator",
            },
            {
                label: this.translate.instant("digital-services.visualize"),
                routerLink: "dashboard",
                visible: this.digitalService.lastCalculationDate !== undefined,
                id: "visualize",
            },
        ];
    }

    async updateDigitalService() {
        // digital service is already updated thanks to data binding
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.update(this.digitalService),
        );

        this.digitalServiceStore.initInPhysicalEquipments(this.digitalService.uid);
        this.digitalServiceStore.initInVirtualEquipments(this.digitalService.uid);
        this.updateTabItems();
    }
}
