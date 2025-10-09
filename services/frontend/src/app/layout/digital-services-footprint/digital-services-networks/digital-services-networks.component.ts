/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, inject, OnInit, signal, ViewChild } from "@angular/core";
import { addYears } from "date-fns";
import { MessageService } from "primeng/api";
import { firstValueFrom } from "rxjs";
import {
    DigitalService,
    DigitalServiceNetworkConfig,
    NetworkType,
} from "src/app/core/interfaces/digital-service.interfaces";
import { InPhysicalEquipmentRest } from "src/app/core/interfaces/input.interface";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InPhysicalEquipmentsService } from "src/app/core/service/data/in-out/in-physical-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServicesNetworksSidePanelComponent } from "./digital-services-networks-side-panel/digital-services-networks-side-panel.component";
@Component({
    selector: "app-digital-services-networks",
    templateUrl: "./digital-services-networks.component.html",
    providers: [MessageService],
})
export class DigitalServicesNetworksComponent implements OnInit {
    digitalServiceStore = inject(DigitalServiceStoreService);
    inPhysicalEquipmentsService = inject(InPhysicalEquipmentsService);
    private readonly digitalServicesBusiness = inject(DigitalServiceBusinessService);

    @ViewChild("networkSidePanel", { static: false })
    networkSidePanel!: DigitalServicesNetworksSidePanelComponent;
    digitalService: DigitalService = {} as DigitalService;
    network: DigitalServiceNetworkConfig = {} as DigitalServiceNetworkConfig;

    sidebarVisible = false;
    existingNames = signal<string[]>([]);

    headerFields = ["name", "typeCode", "yearlyQuantityOfGbExchanged"];

    networkData = computed(() => {
        const networkTypes = this.digitalServiceStore.networkTypes();
        return this.digitalServiceStore
            .inPhysicalEquipments()
            .filter((item) => item.type === "Network")
            .map((item) => {
                const type = networkTypes.find(
                    (networkType) => networkType.code === item.model,
                );

                let yearlyQuantityOfGbExchanged = item.quantity;
                if (type?.type === "Fixed") {
                    yearlyQuantityOfGbExchanged = type.annualQuantityOfGo * item.quantity;
                }
                return {
                    creationDate: item.creationDate,
                    id: item.id,
                    typeCode: type?.value,
                    type,
                    yearlyQuantityOfGbExchanged,
                    name: item.name,
                } as DigitalServiceNetworkConfig;
            });
    });

    constructor(
        private readonly digitalServicesData: DigitalServicesDataService,
        public userService: UserService,
    ) {}

    ngOnInit() {
        this.asyncInit();
    }
    private async asyncInit() {
        this.digitalService = await firstValueFrom(
            this.digitalServicesData.digitalService$,
        );

        this.resetNetwork();
    }

    changeSidebar(event: boolean) {
        this.sidebarVisible = event;
    }

    setItem(event: any) {
        const index = event.index;
        delete event.index;

        this.network = { ...event, idFront: index };
    }

    async deleteItem(event: DigitalServiceNetworkConfig) {
        await firstValueFrom(
            this.inPhysicalEquipmentsService.delete({
                id: event.id,
                digitalServiceUid: this.digitalService.uid,
            } as InPhysicalEquipmentRest),
        );
        await this.digitalServiceStore.initInPhysicalEquipments(this.digitalService.uid);
        this.digitalServiceStore.setEnableCalcul(true);
    }

    resetNetwork() {
        this.existingNames.set(
            this.networkData()
                .filter((c) =>
                    this.network.idFront !== undefined
                        ? this.network.name !== c.name
                        : true,
                )
                .map((network) => network.name),
        );
        this.network = {
            name: this.digitalServicesBusiness.getNextAvailableName(
                this.existingNames(),
                "Network",
                true,
            ),
            uid: undefined,
            type: this.digitalServiceStore.networkTypes()[0],
            yearlyQuantityOfGbExchanged: 0,
        };
    }

    setNetworks(network: DigitalServiceNetworkConfig, index: number) {
        this.network = { ...network, idFront: index };
    }

    async actionNetwork(action: string, network: DigitalServiceNetworkConfig) {
        this.sidebarVisible = false;
        if ("cancel" === action) return;

        const datePurchase = new Date("2020-01-01");
        const dateWithdrawal = addYears(datePurchase, 1);

        const elementToSave = {
            digitalServiceUid: this.digitalService.uid,
            name: network.name,
            type: "Network",
            model: network.type.code,
            quantity: this.calculateQuantity(
                network.yearlyQuantityOfGbExchanged,
                network.type,
            ),
            location: network.type.country,
            datePurchase: datePurchase.toISOString(),
            dateWithdrawal: dateWithdrawal.toISOString(),
        } as InPhysicalEquipmentRest;

        if (network.id) {
            elementToSave.id = network.id;
            await firstValueFrom(this.inPhysicalEquipmentsService.update(elementToSave));
        } else {
            await firstValueFrom(this.inPhysicalEquipmentsService.create(elementToSave));
        }
        await this.digitalServiceStore.initInPhysicalEquipments(this.digitalService.uid);
        this.digitalServiceStore.setEnableCalcul(true);
    }

    private calculateQuantity(
        yearlyQuantityOfGbExchanged: number,
        type: NetworkType,
    ): number {
        if (!yearlyQuantityOfGbExchanged) return 0;
        if (type.type === "Mobile") return yearlyQuantityOfGbExchanged;

        if (type.annualQuantityOfGo && type.annualQuantityOfGo > 0) {
            return yearlyQuantityOfGbExchanged / type.annualQuantityOfGo;
        }
        return 0;
    }
}
