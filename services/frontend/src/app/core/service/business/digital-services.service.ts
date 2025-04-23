/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable, inject } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { addDays, formatDate } from "date-fns";
import { Observable, ReplaySubject, firstValueFrom, lastValueFrom, map } from "rxjs";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";
import { removeBlankSpaces } from "../../custom-validators/unique-name.validator";
import {
    CloudImpact,
    CloudsImpact,
    DSCriteriaRest,
    DigitalService,
    DigitalServiceCloudImpact,
    DigitalServiceCloudResponse,
    DigitalServiceFootprint,
    DigitalServiceNetworksImpact,
    DigitalServiceServerConfig,
    DigitalServiceServersImpact,
    DigitalServiceTerminalResponse,
    DigitalServiceTerminalsImpact,
    ServerImpact,
    ServerVM,
    ServersType,
    TerminalImpact,
    TerminalsImpact,
} from "../../interfaces/digital-service.interfaces";
import { MapString } from "../../interfaces/generic.interfaces";
import {
    InPhysicalEquipmentRest,
    InVirtualEquipmentRest,
} from "../../interfaces/input.interface";
import { DigitalServiceStoreService } from "../../store/digital-service.store";
import { InPhysicalEquipmentsService } from "../data/in-out/in-physical-equipments.service";
import { InVirtualEquipmentsService } from "../data/in-out/in-virtual-equipments.service";
import { DigitalServicesDataService } from "./../data/digital-services-data.service";

@Injectable({
    providedIn: "root",
})
export class DigitalServiceBusinessService {
    private translate = inject(TranslateService);
    private digitalServiceStore = inject(DigitalServiceStoreService);
    private inPhysicalEquipmentsService = inject(InPhysicalEquipmentsService);
    private inVirtualEquipmentsService = inject(InVirtualEquipmentsService);
    private serverSubject = new ReplaySubject<DigitalServiceServerConfig>(1);

    serverFormSubject$ = this.serverSubject.asObservable();

    private panelSubject = new ReplaySubject<boolean>(1);
    panelSubject$ = this.panelSubject.asObservable();

    private dataInitializedSubject = new ReplaySubject<boolean>(1);
    dataInitializedSubject$ = this.dataInitializedSubject.asObservable();

    constructor(private digitalServiceData: DigitalServicesDataService) {}

    setDataInitialized(param: boolean) {
        this.dataInitializedSubject.next(param);
    }

    closePanel() {
        this.panelSubject.next(false);
    }

    openPanel() {
        this.panelSubject.next(true);
    }

    setServerForm(server: DigitalServiceServerConfig) {
        this.serverSubject.next(server);
    }

    async submitServerForm(
        server: DigitalServiceServerConfig,
        digitalService: DigitalService,
    ) {
        if (this.digitalServiceStore.isNewArch()) {
            const physicalEquipment = this.toInPhysicalEquipment(
                server,
                digitalService.uid,
            );

            if (server.id) {
                physicalEquipment.id = server.id;
                const isSharedServer = server.mutualizationType === "Shared";
                await firstValueFrom(
                    this.inPhysicalEquipmentsService.update(physicalEquipment),
                );
                const existingVms = server.vm.filter((vm) => vm.uid !== "");
                const newVms = server.vm.filter((vm) => vm.uid === "");
                let allVms: InVirtualEquipmentRest[] = [];
                if (existingVms.length && isSharedServer) {
                    allVms = existingVms.map((vm) =>
                        this.toInVirtualEquipment(vm, server, digitalService.uid),
                    );
                }
                await firstValueFrom(
                    this.inVirtualEquipmentsService.updateAllVms(
                        allVms,
                        digitalService.uid,
                        physicalEquipment.id,
                    ),
                );
                if (isSharedServer) {
                    for (const vm of newVms) {
                        await firstValueFrom(
                            this.inVirtualEquipmentsService.create(
                                this.toInVirtualEquipment(vm, server, digitalService.uid),
                            ),
                        );
                    }
                }
            } else {
                await firstValueFrom(
                    this.inPhysicalEquipmentsService.create(physicalEquipment),
                );
                for (const vm of server.vm) {
                    await firstValueFrom(
                        this.inVirtualEquipmentsService.create(
                            this.toInVirtualEquipment(vm, server, digitalService.uid),
                        ),
                    );
                }
            }
            await this.digitalServiceStore.initInPhysicalEquipments(digitalService.uid);
            await this.digitalServiceStore.initInVirtualEquipments(digitalService.uid);
            this.digitalServiceStore.setEnableCalcul(true);
        } else {
            this.submitServerFormOld(server, digitalService);
        }
    }

    async submitServerFormOld(
        server: DigitalServiceServerConfig,
        digitalService: DigitalService,
    ) {
        this.serverSubject.next(server);

        // Find the index of the server with the matching uid
        let existingServerIndex = digitalService.servers?.findIndex(
            (t) => t.uid === server.uid,
        );
        // If the server with the uid exists, update it; otherwise, add the new server
        if (
            existingServerIndex !== -1 &&
            existingServerIndex !== undefined &&
            digitalService.servers &&
            server.uid !== undefined
        ) {
            digitalService.servers[existingServerIndex] = server;
        } else {
            digitalService.servers.push(server);
        }

        await lastValueFrom(this.digitalServiceData.update(digitalService));
    }

    toInPhysicalEquipment(
        server: DigitalServiceServerConfig,
        digitalServiceUid: string,
    ): InPhysicalEquipmentRest {
        const quantity =
            server.mutualizationType === "Dedicated"
                ? server.quantity * (server.annualOperatingTime! / 8760)
                : 1;

        return {
            name: server.name,
            digitalServiceUid,
            quantity,
            type:
                server.mutualizationType === "Dedicated"
                    ? "Dedicated Server"
                    : "Shared Server",
            model: server.host?.reference,
            datePurchase: "2020-01-01",
            dateWithdrawal: formatDate(
                addDays(new Date("2020-01-01"), 365 * server.lifespan!),
                "yyyy-MM-dd",
            ),
            location: server.datacenter?.location,
            datacenterName: server.datacenter?.name,
            electricityConsumption: server.annualElectricConsumption,
            durationHour: server.annualOperatingTime,
            cpuCoreNumber: server.totalVCpu,
            sizeDiskGb: server.totalDisk,
            description: server.host?.value,
        } as InPhysicalEquipmentRest;
    }

    toInVirtualEquipment(
        vm: ServerVM,
        server: DigitalServiceServerConfig,
        digitalServiceUid: string,
    ) {
        return {
            id: vm.uid ? Number(vm.uid) : undefined,
            digitalServiceUid: digitalServiceUid,
            durationHour: vm.annualOperatingTime,
            infrastructureType: "NON_CLOUD_SERVERS",
            name: vm.name,
            quantity: vm.quantity,
            vcpuCoreNumber: vm.vCpu,
            sizeDiskGb: vm.disk,
            physicalEquipmentName: server.name,
            allocationFactor:
                server.type === "Compute" && server.mutualizationType === "Shared"
                    ? (vm.vCpu / server.totalVCpu!) *
                      (vm.annualOperatingTime / 8760) *
                      vm.quantity
                    : (vm.disk / server.totalDisk!) *
                      (vm.annualOperatingTime / 8760) *
                      vm.quantity,
        } as InVirtualEquipmentRest;
    }

    transformTerminalData(
        terminalFootprint: DigitalServiceTerminalResponse[],
    ): DigitalServiceTerminalsImpact[] {
        const transformedData: DigitalServiceTerminalsImpact[] = [];
        const order = LifeCycleUtils.getLifeCycleList();

        terminalFootprint.forEach((item) => {
            const criteria = item.criteria.split(" ").slice(0, 2).join(" ");
            const impactCountry: TerminalsImpact[] = [];
            const impactType: TerminalsImpact[] = [];

            item.impacts.forEach((impact) => {
                this.processImpactTerminal(impact, impactCountry, order, "country");
                this.processImpactTerminal(impact, impactType, order, "type");
            });

            this.finalizeImpactsCommon(impactCountry, "terminal");
            this.finalizeImpactsCommon(impactType, "terminal");

            transformedData.push({
                criteria,
                impactCountry,
                impactType,
            });
        });

        return transformedData;
    }

    private processImpactTerminal(
        impact: TerminalImpact,
        impactArray: TerminalsImpact[],
        order: string[],
        type: "country" | "type",
    ): void {
        const name = type === "country" ? impact.country : impact.description;
        const existingImpact = impactArray.find((item) => item.name === name);

        if (existingImpact) {
            this.updateExistingImpactCommon(existingImpact, impact, order, "terminal");
        } else {
            impactArray.push(
                this.createNewImpactCommon(name, impact, "terminal") as TerminalsImpact,
            );
        }
    }

    private updateExistingImpactCommon(
        existingImpact: TerminalsImpact | CloudsImpact,
        impact: TerminalImpact | CloudImpact,
        order: string[],
        deviceType: string,
    ): void {
        existingImpact.totalSipValue += impact.sipValue;
        existingImpact.rawValue += impact.rawValue;
        existingImpact.unit = impact.unit;

        if (deviceType === "terminal") {
            const terminalImpact = existingImpact as TerminalsImpact;
            const terminalImpactData = impact as TerminalImpact;
            terminalImpact.totalNbUsers += terminalImpactData.numberUsers;
            terminalImpact.avgUsageTime +=
                terminalImpactData.numberUsers *
                terminalImpactData.yearlyUsageTimePerUser;
        } else {
            const cloudImpact = existingImpact as CloudsImpact;
            const cloudImpactData = impact as CloudImpact;
            cloudImpact.totalQuantity += cloudImpactData.quantity;
            cloudImpact.totalAvgUsage += cloudImpactData.averageUsage;
            cloudImpact.totalAvgWorkLoad +=
                cloudImpactData.averageWorkLoad * cloudImpactData.quantity;
        }

        const existingACVStep = existingImpact.impact.find(
            (step) => step.acvStep === impact.acvStep,
        );

        if (existingACVStep) {
            existingACVStep.sipValue += impact.sipValue;
            existingACVStep.rawValue += impact.rawValue;
            existingACVStep.unit = impact.unit;
            existingACVStep.statusCount = existingACVStep.statusCount || {
                ok: 0,
                error: 0,
                total: 0,
            };
            existingACVStep.statusCount.ok +=
                impact.status === Constants.DATA_QUALITY_STATUS.ok
                    ? impact.countValue
                    : 0;
            existingACVStep.statusCount.error +=
                impact.status !== Constants.DATA_QUALITY_STATUS.ok
                    ? impact.countValue
                    : 0;
            existingACVStep.statusCount.total += impact.countValue;
        } else {
            existingImpact.impact.push(this.createACVStepCommon(impact));
        }

        existingImpact.impact.sort(
            (a, b) => order.indexOf(a.acvStep) - order.indexOf(b.acvStep),
        );
    }

    private createNewImpactCommon(
        name: string,
        impact: TerminalImpact | CloudImpact,
        deviceType: string,
    ): TerminalsImpact | CloudsImpact {
        const commonImpact = {
            name,
            totalSipValue: impact.sipValue,
            rawValue: impact.rawValue,
            unit: impact.unit,
            impact: [this.createACVStepCommon(impact)],
        };

        if (deviceType === "terminal") {
            const terminalImpact = impact as TerminalImpact;
            return {
                ...commonImpact,
                totalNbUsers: terminalImpact.numberUsers,
                avgUsageTime:
                    terminalImpact.numberUsers * terminalImpact.yearlyUsageTimePerUser,
            } as TerminalsImpact;
        } else {
            const cloudImpact = impact as CloudImpact;
            return {
                ...commonImpact,
                totalQuantity: cloudImpact.quantity,
                totalAvgUsage: cloudImpact.averageUsage,
                totalAvgWorkLoad: cloudImpact.averageWorkLoad * cloudImpact.quantity,
            } as CloudsImpact;
        }
    }

    private createACVStepCommon(impact: CloudImpact | TerminalImpact) {
        return {
            acvStep: impact.acvStep,
            sipValue: impact.sipValue,
            rawValue: impact.rawValue,
            unit: impact.unit,
            status: impact.status,
            statusCount: {
                ok:
                    impact.status === Constants.DATA_QUALITY_STATUS.ok
                        ? impact.countValue
                        : 0,
                error:
                    impact.status !== Constants.DATA_QUALITY_STATUS.ok
                        ? impact.countValue
                        : 0,
                total: impact.countValue,
            },
        };
    }

    private finalizeImpactsCommon(
        impacts: TerminalsImpact[] | CloudsImpact[],
        type: "terminal" | "cloud",
    ) {
        if (type === "terminal") {
            (impacts as TerminalsImpact[]).forEach((impact) => {
                impact.avgUsageTime = impact.avgUsageTime / impact.totalNbUsers;
                impact.totalNbUsers = impact.totalNbUsers / 4;
            });
        } else {
            (impacts as CloudsImpact[]).forEach((impact) => {
                impact.totalAvgWorkLoad = impact.totalAvgWorkLoad / impact.totalQuantity;
                impact.totalAvgUsage = impact.totalAvgUsage / impact.totalQuantity;
            });
        }

        impacts.sort((a, b) => a.name.localeCompare(b.name));
    }

    transformCloudData(
        cloudFootprint: DigitalServiceCloudResponse[],
        countryMap: MapString,
    ): DigitalServiceCloudImpact[] {
        const transformedData: DigitalServiceCloudImpact[] = [];
        const order = LifeCycleUtils.getLifeCycleList();

        cloudFootprint.forEach((item) => {
            const criteria = item.criteria.split(" ").slice(0, 2).join(" ");
            const impactLocation: CloudsImpact[] = [];
            const impactInstance: CloudsImpact[] = [];

            item.impacts
                .map((i) => ({ ...i, averageWorkLoad: i.averageWorkLoad * 100 }))
                .forEach((impact: CloudImpact) => {
                    this.processImpact(
                        impact,
                        impactLocation,
                        order,
                        "country",
                        countryMap,
                    );
                    this.processImpact(
                        impact,
                        impactInstance,
                        order,
                        "instance",
                        countryMap,
                    );
                });

            this.finalizeImpactsCommon(impactLocation, "cloud");
            this.finalizeImpactsCommon(impactInstance, "cloud");

            transformedData.push({
                criteria,
                impactLocation,
                impactInstance,
            });
        });

        return transformedData;
    }

    private processImpact(
        impact: CloudImpact,
        impactArray: CloudsImpact[],
        order: string[],
        type: "country" | "instance",
        countryMap: MapString,
    ): void {
        const name =
            type === "country"
                ? countryMap[impact.country]
                : `${impact.cloudProvider?.toUpperCase()}-${impact.instanceType}`;
        const existingImpact = impactArray.find((item) => item.name === name);

        if (existingImpact) {
            this.updateExistingImpactCommon(existingImpact, impact, order, "cloud");
        } else {
            impactArray.push(
                this.createNewImpactCommon(name, impact, "cloud") as CloudsImpact,
            );
        }
    }

    getFootprint(uid: DigitalService["uid"]): Observable<DigitalServiceFootprint[]> {
        return this.digitalServiceData
            .getFootprint(uid)
            .pipe(map((footprint) => this.transformFootprintCriteriaUnit(footprint)));
    }

    getTerminalsIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceTerminalResponse[]> {
        return this.digitalServiceData
            .getTerminalsIndicators(uid)
            .pipe(
                map((networkFootprint) =>
                    this.transformTerminalNetworkCriteriaUnit(networkFootprint),
                ),
            );
    }

    getNetworksIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceNetworksImpact[]> {
        return this.digitalServiceData
            .getNetworksIndicators(uid)
            .pipe(
                map((terminalFootprint) =>
                    this.transformTerminalNetworkCriteriaUnit(terminalFootprint),
                ),
            );
    }

    getServersIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceServersImpact[]> {
        return this.digitalServiceData
            .getServersIndicators(uid)
            .pipe(
                map((serverFootprint) =>
                    this.transformServerCriteriaUnit(serverFootprint),
                ),
            );
    }

    getCloudsIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceCloudResponse[]> {
        return this.digitalServiceData
            .getCloudsIndicators(uid)
            .pipe(
                map((cloudFootprint) =>
                    this.transformTerminalNetworkCriteriaUnit(cloudFootprint),
                ),
            );
    }

    transformFootprintCriteriaUnit(
        footprint: DigitalServiceFootprint[],
    ): DigitalServiceFootprint[] {
        return footprint.map((f) => ({
            ...f,
            impacts: f.impacts.map((fi) => ({
                ...fi,
                unit: this.translate.instant(`criteria.${fi.criteria}.unite`),
                unitValue: fi.unitValue ?? 0,
                sipValue: fi.sipValue ?? 0,
            })),
        }));
    }

    transformTerminalNetworkCriteriaUnit<
        T extends
            | DigitalServiceTerminalResponse
            | DigitalServiceNetworksImpact
            | DigitalServiceCloudResponse,
    >(footprint: T[]): T[] {
        return footprint.map((f) => ({
            ...f,
            impacts: f.impacts.map((fi) => ({
                ...fi,
                unit: this.translate.instant(`criteria.${f.criteria}.unite`),
                rawValue: fi.rawValue ?? 0,
                sipValue: fi.sipValue ?? 0,
            })),
        }));
    }

    transformServerCriteriaUnit(
        serverFootprint: DigitalServiceServersImpact[],
    ): DigitalServiceServersImpact[] {
        return serverFootprint.map((footprint) => ({
            ...footprint,
            impactsServer: footprint.impactsServer.map((impacts) => ({
                ...impacts,
                servers: this.transformServers(impacts, footprint),
            })),
        }));
    }

    transformServers(
        impacts: ServersType,
        footprint: DigitalServiceServersImpact,
    ): ServerImpact[] {
        return impacts.servers.map((server) => ({
            ...server,
            impactVmDisk: server.impactVmDisk.map((impact) => ({
                ...impact,
                unit: this.translate.instant(`criteria.${footprint.criteria}.unite`),
                rawValue: impact.rawValue ?? 0,
                sipValue: impact.sipValue ?? 0,
            })),
            impactStep: server.impactStep.map((impact) => ({
                ...impact,
                unit: this.translate.instant(`criteria.${footprint.criteria}.unite`),
                rawValue: impact.rawValue ?? 0,
                sipValue: impact.sipValue ?? 0,
            })),
        }));
    }

    updateDsCriteria(
        digitalServiceUid: string,
        DSCriteria: DSCriteriaRest,
    ): Observable<DSCriteriaRest> {
        return this.digitalServiceData.updateDsCriteria(digitalServiceUid, DSCriteria);
    }

    getNextAvailableName(existingNames: string[], baseName: string): string {
        const nameSet = new Set(existingNames?.map((n) => removeBlankSpaces(n)));
        let index = 1;
        let newName = `${removeBlankSpaces(baseName)}${String.fromCharCode(64 + index)}`; // Start with "ServerA"

        while (nameSet.has(newName)) {
            index++;
            newName = `${removeBlankSpaces(baseName)}${String.fromCharCode(64 + index)}`; // Increment to "ServerB", "ServerC", etc.
        }
        return `${baseName} ${String.fromCharCode(64 + index)}`;
    }

    async initCountryMap() {
        if (this.digitalServiceStore.countryMap.length > 0) return;

        const boaviztaCountryMap = await lastValueFrom(
            this.digitalServiceData.getBoaviztapiCountryMap(),
        );
        const countryMap: MapString = {};
        for (const key in boaviztaCountryMap) {
            countryMap[boaviztaCountryMap[key]] = key;
        }
        this.digitalServiceStore.setCountryMap(countryMap);
    }
}
