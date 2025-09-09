/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, DestroyRef, inject, OnInit } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ActivatedRoute, Router } from "@angular/router";
import { differenceInDays } from "date-fns";
import { MessageService } from "primeng/api";
import { firstValueFrom, lastValueFrom } from "rxjs";
import {
    DigitalService,
    DigitalServiceServerConfig,
    ServerVM,
} from "src/app/core/interfaces/digital-service.interfaces";
import {
    InPhysicalEquipmentRest,
    InVirtualEquipmentRest,
} from "src/app/core/interfaces/input.interface";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InPhysicalEquipmentsService } from "src/app/core/service/data/in-out/in-physical-equipments.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
@Component({
    selector: "app-digital-services-servers",
    templateUrl: "./digital-services-servers.component.html",
    providers: [MessageService],
})
export class DigitalServicesServersComponent implements OnInit {
    protected digitalServiceStore = inject(DigitalServiceStoreService);
    private readonly inPhysicalEquipmentsService = inject(InPhysicalEquipmentsService);
    private readonly inVirtualEquipmentsService = inject(InVirtualEquipmentsService);
    private readonly destroyRef = inject(DestroyRef);

    digitalService: DigitalService = {} as DigitalService;
    sidebarVisible: boolean = false;
    existingNames: string[] = [];

    headerFields = [
        "name",
        "mutualizationType",
        "type",
        "quantityVms",
        "hostValue",
        "datacenterName",
    ];

    serverData = computed(() => {
        const serverTypes = this.digitalServiceStore.serverTypes();
        const datacenters = this.digitalServiceStore.inDatacenters();

        if (datacenters.length === 0 || serverTypes.length === 0) return [];

        const inVirtualEquipments = this.digitalServiceStore
            .inVirtualEquipments()
            .filter((ve) => ve.infrastructureType !== "CLOUD_SERVICES")
            .reduce((acc: any, obj: any) => {
                const key = obj.physicalEquipmentName;
                if (!acc[key]) acc[key] = [];
                acc[key].push(obj);
                return acc;
            }, {});

        const inPhysicalEquipments = this.digitalServiceStore
            .inPhysicalEquipments()
            .filter((item) => item.type.endsWith(" Server"));

        this.existingNames = inPhysicalEquipments.map((pe) => pe.name);

        return inPhysicalEquipments.map((item) => {
            let serverType = serverTypes.find(
                (server) => server.value === item.description,
            );

            if (serverType === undefined) {
                serverType = serverTypes.find(
                    (server) => server.reference === item.model,
                );
            }

            const quantity =
                item.type === "Dedicated Server"
                    ? item.quantity / (item.durationHour! / 8760)
                    : 1;

            const datacenter = datacenters.find((dc) => dc.name === item.datacenterName);
            const vms = inVirtualEquipments[item.name] || [];
            const totalQuantityVms = vms.reduce(
                (acc: number, vm: InVirtualEquipmentRest) => acc + vm.quantity,
                0,
            );

            return {
                id: item.id,
                name: item.name,
                mutualizationType: item.type.replace(" Server", ""),
                quantity,
                quantityVms: `${quantity} (${totalQuantityVms})`,
                type: serverType?.type,
                host: serverType,
                hostValue: serverType?.value,
                datacenter,
                datacenterName: item.datacenterName?.split("|")[0],
                annualElectricConsumption: item.electricityConsumption,
                annualOperatingTime: item.durationHour,
                lifespan:
                    differenceInDays(item.dateWithdrawal!, item.datePurchase!) / 365,
                totalVCpu: item.cpuCoreNumber,
                totalDisk: item.sizeDiskGb,
                vm: vms.map((vm: InVirtualEquipmentRest) => {
                    return {
                        name: vm.name,
                        annualOperatingTime: vm.durationHour,
                        disk: vm.sizeDiskGb,
                        quantity: vm.quantity,
                        uid: vm.id.toString(),
                        vCpu: vm.vcpuCoreNumber,
                    } as ServerVM;
                }),
            } as DigitalServiceServerConfig;
        });
    });

    constructor(
        private readonly digitalServicesData: DigitalServicesDataService,
        private readonly digitalServicesBusiness: DigitalServiceBusinessService,
        private readonly router: Router,
        private readonly route: ActivatedRoute,
        public userService: UserService,
    ) {}

    ngOnInit(): void {
        this.digitalServicesData.digitalService$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res) => {
                this.digitalService = res;
            });
        this.digitalServicesBusiness.panelSubject$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res) => {
                this.sidebarVisible = res;
                if (res === false && !this.router.url.endsWith("/resources")) {
                    this.router.navigate(["../resources"], { relativeTo: this.route });
                }
            });
    }

    changeSidebar(event: boolean) {
        this.sidebarVisible = event;
    }

    setItem(event: any) {
        delete event.index;
        event.uid = event.id.toString();
        this.updateServer(event);
    }

    async deleteItem(event: DigitalServiceServerConfig) {
        const digitalServiceUid = this.digitalServiceStore.digitalService().uid;
        if (event.vm.length > 0) {
            for (const vm of event.vm) {
                await firstValueFrom(
                    this.inVirtualEquipmentsService.delete(
                        Number(vm.uid),
                        digitalServiceUid,
                    ),
                );
            }
        }

        await firstValueFrom(
            this.inPhysicalEquipmentsService.delete({
                digitalServiceUid,
                id: event.id,
            } as InPhysicalEquipmentRest),
        );
        await this.digitalServiceStore.initInPhysicalEquipments(digitalServiceUid);
        this.digitalServiceStore.setEnableCalcul(true);
    }

    addNewServer() {
        let newServer: DigitalServiceServerConfig = {
            uid: "",
            name: this.digitalServicesBusiness.getNextAvailableName(
                this.existingNames,
                "Server",
                false,
            ),
            mutualizationType: "Dedicated",
            type: "Compute",
            quantity: -1,
            datacenter: {
                uid: "",
                name: "",
                location: "",
                pue: 0,
            },
            vm: [],
        };

        this.digitalServiceStore.setServer(newServer);
        this.router.navigate(["panel-create"], { relativeTo: this.route });

        this.digitalServicesBusiness.openPanel();
    }

    updateServer(server: DigitalServiceServerConfig) {
        this.digitalServiceStore.setServer(server);
        this.router.navigate(["panel-parameters"], { relativeTo: this.route });

        this.digitalServicesBusiness.openPanel();
    }

    async deleteServers(server: DigitalServiceServerConfig) {
        let existingServerIndex = this.digitalService.servers?.findIndex(
            (t) => t.uid === server.uid,
        );
        if (
            existingServerIndex !== -1 &&
            existingServerIndex !== undefined &&
            this.digitalService.servers
        ) {
            this.digitalService.servers.splice(existingServerIndex, 1);
        }
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.update(this.digitalService),
        );
    }
    closeSidebar() {
        this.digitalServicesBusiness.closePanel();
    }
    ngOnDestroy() {
        if (!this.router.url.includes("resources") && this.sidebarVisible) {
            this.digitalServicesBusiness.closePanel();
        }
    }
}
