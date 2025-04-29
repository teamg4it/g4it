/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, inject, OnInit } from "@angular/core";
import {
    DigitalService,
    DigitalServiceTerminalConfig,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

import { addDays, differenceInDays } from "date-fns";
import { MessageService } from "primeng/api";
import { firstValueFrom, lastValueFrom } from "rxjs";
import { InPhysicalEquipmentRest } from "src/app/core/interfaces/input.interface";
import { UserService } from "src/app/core/service/business/user.service";
import { InPhysicalEquipmentsService } from "src/app/core/service/data/in-out/in-physical-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import * as uuid from "uuid";
@Component({
    selector: "app-digital-services-terminals",
    templateUrl: "./digital-services-terminals.component.html",
    providers: [MessageService],
})
export class DigitalServicesTerminalsComponent implements OnInit {
    digitalServiceStore = inject(DigitalServiceStoreService);
    inPhysicalEquipmentsService = inject(InPhysicalEquipmentsService);

    sidebarVisible: boolean = false;
    sidebarPurpose: string = "";
    terminal: DigitalServiceTerminalConfig = {} as DigitalServiceTerminalConfig;
    digitalService: DigitalService = {} as DigitalService;

    headerFields = [
        "typeCode",
        "country",
        "numberOfUsers",
        "yearlyUsageTimePerUser",
        "lifespan",
    ];

    terminalData = computed(() => {
        const deviceTypes = this.digitalServiceStore.terminalDeviceTypes();
        if (deviceTypes.length === 0) return [];
        return this.digitalServiceStore
            .inPhysicalEquipments()
            .filter((item) => item.type === "Terminal")
            .map((item) => {
                const deviceType = deviceTypes.find((type) => type.code === item.model);

                return {
                    id: item.id,
                    creationDate: item.creationDate,
                    typeCode: deviceType?.value,
                    lifespan:
                        differenceInDays(item.dateWithdrawal!, item.datePurchase!) / 365,
                    country: item.location,
                    numberOfUsers: item.numberOfUsers,
                    yearlyUsageTimePerUser: item.durationHour,
                } as DigitalServiceTerminalConfig;
            });
    });

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        public userService: UserService,
    ) {}

    ngOnInit() {
        this.digitalServicesData.digitalService$.subscribe((res) => {
            this.digitalService = res;
        });
    }

    changeSidebar(event: boolean) {
        this.sidebarVisible = event;
    }

    setItem(event: any) {
        const index = event.index;
        delete event.index;

        this.terminal = { ...event };
        this.terminal.idFront = index;
    }

    async deleteItem(event: DigitalServiceTerminalConfig) {
        await firstValueFrom(
            this.inPhysicalEquipmentsService.delete({
                id: event.id,
                digitalServiceUid: this.digitalService.uid,
            } as InPhysicalEquipmentRest),
        );
        await this.digitalServiceStore.initInPhysicalEquipments(this.digitalService.uid);
        this.digitalServiceStore.setEnableCalcul(true);
    }

    setTerminal(terminal: DigitalServiceTerminalConfig, index: number) {
        this.terminal = { ...terminal, idFront: index };
    }

    resetTerminal() {
        this.terminal = {} as DigitalServiceTerminalConfig;
    }

    async updateTerminals(terminal: DigitalServiceTerminalConfig) {
        const datePurchase = new Date("2020-01-01");
        const dateWithdrawal = addDays(datePurchase, terminal.lifespan * 365);

        const elementToSave = {
            digitalServiceUid: this.digitalService.uid,
            name: terminal.uid || uuid.v4(),
            type: "Terminal",
            model: terminal.type.code,
            location: terminal.country,
            numberOfUsers: terminal.numberOfUsers,
            quantity:
                (terminal.numberOfUsers * terminal.yearlyUsageTimePerUser) / (365 * 24),
            durationHour: terminal.yearlyUsageTimePerUser,
            datePurchase: datePurchase.toISOString(),
            dateWithdrawal: dateWithdrawal.toISOString(),
        } as InPhysicalEquipmentRest;

        if (terminal.id) {
            elementToSave.id = terminal.id;
            await firstValueFrom(this.inPhysicalEquipmentsService.update(elementToSave));
        } else {
            await firstValueFrom(this.inPhysicalEquipmentsService.create(elementToSave));
        }
        await this.digitalServiceStore.initInPhysicalEquipments(this.digitalService.uid);
        this.digitalServiceStore.setEnableCalcul(true);
    }

    async deleteTerminals(terminal: DigitalServiceTerminalConfig) {
        let existingTerminalIndex = this.digitalService.terminals?.findIndex(
            (t) => t.uid === terminal.uid,
        );

        if (
            existingTerminalIndex !== -1 &&
            existingTerminalIndex !== undefined &&
            this.digitalService.terminals !== undefined
        ) {
            this.digitalService.terminals.splice(existingTerminalIndex, 1);
        }
        await lastValueFrom(this.digitalServicesData.update(this.digitalService));
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.get(this.digitalService.uid),
        );
    }
}
