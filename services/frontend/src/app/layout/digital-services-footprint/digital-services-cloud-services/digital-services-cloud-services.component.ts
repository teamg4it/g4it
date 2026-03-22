/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, EventEmitter, inject, input, OnInit, Output, Signal, signal } from "@angular/core";
import { Router } from "@angular/router";
import { MessageService } from "primeng/api";
import { firstValueFrom, lastValueFrom } from "rxjs";
import { DigitalServiceCloudServiceConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { MapString } from "src/app/core/interfaces/generic.interfaces";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { UserService } from "src/app/core/service/business/user.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-digital-services-cloud-services",
    templateUrl: "./digital-services-cloud-services.component.html",
    providers: [MessageService],
})
export class DigitalServicesCloudServicesComponent implements OnInit {
    private readonly inVirtualEquipmentsService = inject(InVirtualEquipmentsService);
    protected digitalServiceStore = inject(DigitalServiceStoreService);

    dsVersionUid = input("");
    embedded = input(false);
    @Output() editEmbedded = new EventEmitter<DigitalServiceCloudServiceConfig>();
    @Output() updateEmbedded = new EventEmitter<DigitalServiceCloudServiceConfig>();
    @Output() deleteEmbedded = new EventEmitter<DigitalServiceCloudServiceConfig>();

    sidebarVisible: boolean = false;
    sidebarPurpose: string = "";
    cloud: DigitalServiceCloudServiceConfig = {} as DigitalServiceCloudServiceConfig;
    digitalServiceUid = "";

    virtualEquipments = signal<InVirtualEquipmentRest[]>([]);
    cloudDataInput = input<
        Signal<DigitalServiceCloudServiceConfig[]> | DigitalServiceCloudServiceConfig[] | undefined
        >(undefined);

cloudServices = computed(() => {
    const input = this.cloudDataInput();

    if (input) {
        return typeof input === "function" ? input() : input;
    }
    return this.digitalServiceStore
        .inVirtualEquipments()
        .filter((server) => server.infrastructureType === "CLOUD_SERVICES")
        .map((server: InVirtualEquipmentRest) =>
            this.toDigitalServiceCloudServiceConfig(
                server,
                this.digitalServiceStore.countryMap(),
            ),
        );
});

    headerFields = [
        "name",
        "cloudProvider",
        "instanceType",
        "quantity",
        "locationValue",
        "annualUsage",
        "averageWorkload",
    ];

    constructor(
        public userService: UserService,
        private readonly router: Router,
    ) {}

    ngOnInit(): void {
        this.digitalServiceUid = this.router.url.split("/")[6];
        this.getCloudServices();
    }

    async getCloudServices() {
        this.digitalServiceStore.initInVirtualEquipments(this.digitalServiceUid);
    }

    changeSidebar(event: boolean) {
        this.sidebarVisible = event;
    }

    setItem(event: any) {
        const index = event.index;
        delete event.index;

        this.cloud = { ...event };
        this.cloud.idFront = index;
        this.editEmbedded.emit(this.cloud);
        if(this.embedded()){
            this.editEmbedded.emit(this.cloud);
            return ; 
        }
    }

    async deleteItem(event: DigitalServiceCloudServiceConfig) {
            if (this.embedded()) {
        this.deleteEmbedded.emit(event);
        return;
    }
        await firstValueFrom(
            this.inVirtualEquipmentsService.delete(event.id, this.dsVersionUid()),
        );
        await this.getCloudServices();
        this.digitalServiceStore.setEnableCalcul(true);
    }

    setServerCloud(cloud: DigitalServiceCloudServiceConfig, index: number) {
        this.cloud = { ...cloud };
        this.cloud.idFront = index;
    }

    resetCloudServices() {
        this.cloud = {} as DigitalServiceCloudServiceConfig;
    }

    async updateCloudServices(cloud: DigitalServiceCloudServiceConfig) {
    if (this.embedded()) {
        this.updateEmbedded.emit(cloud);
        return;
    }
        if (cloud.id) {
            await firstValueFrom(
                this.inVirtualEquipmentsService.update(
                    this.toInVirtualEquipmentRest(cloud),
                ),
            );
        } else {
            await firstValueFrom(
                this.inVirtualEquipmentsService.create(
                    this.toInVirtualEquipmentRest(cloud),
                ),
            );
        }
        await this.getCloudServices();
        this.digitalServiceStore.setEnableCalcul(true);
    }

    async deleteCloudServices(cloud: DigitalServiceCloudServiceConfig) {
        this.cloud.digitalServiceUid = this.digitalServiceUid;
        await lastValueFrom(
            this.inVirtualEquipmentsService.delete(cloud.id, cloud.digitalServiceUid),
        );
        await this.getCloudServices();
        this.digitalServiceStore.setEnableCalcul(true);
    }

    //mapper
    toInVirtualEquipmentRest(
        cloud: DigitalServiceCloudServiceConfig,
    ): InVirtualEquipmentRest {
        return {
            id: cloud.id,
            digitalServiceUid: cloud.digitalServiceUid,
            digitalServiceVersionUid: this.dsVersionUid(),
            name: cloud.name,
            infrastructureType: "CLOUD_SERVICES",
            quantity: cloud.quantity,
            provider: cloud.cloudProvider,
            instanceType: cloud.instanceType,
            location: cloud.location.code,
            durationHour: cloud.annualUsage,
            workload: cloud.averageWorkload / 100,
        };
    }

    toDigitalServiceCloudServiceConfig(
        virtualEq: InVirtualEquipmentRest,
        countryMap: MapString,
    ): DigitalServiceCloudServiceConfig {
        return {
            id: virtualEq.id,
            digitalServiceUid: virtualEq.digitalServiceUid!,
            name: virtualEq.name,
            quantity: virtualEq.quantity,
            cloudProvider: virtualEq.provider!,
            instanceType: virtualEq.instanceType!,
            location: {
                code: virtualEq.location,
                name: countryMap[virtualEq.location] || virtualEq.location,
            },
            locationValue: countryMap[virtualEq.location] || virtualEq.location,
            annualUsage: virtualEq.durationHour!,
            averageWorkload: virtualEq.workload! * 100,
        };
    }
}
