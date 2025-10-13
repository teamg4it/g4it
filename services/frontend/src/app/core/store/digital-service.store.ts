import { computed, inject, Injectable, signal } from "@angular/core";
import { firstValueFrom } from "rxjs";
import {
    DigitalService,
    DigitalServiceServerConfig,
    Host,
    NetworkType,
    TerminalsType,
} from "../interfaces/digital-service.interfaces";
import { MapString } from "../interfaces/generic.interfaces";
import {
    InDatacenterRest,
    InPhysicalEquipmentRest,
    InVirtualEquipmentRest,
} from "../interfaces/input.interface";
import { InPhysicalEquipmentsService } from "../service/data/in-out/in-physical-equipments.service";
import { InVirtualEquipmentsService } from "../service/data/in-out/in-virtual-equipments.service";

interface DigitalServiceState {
    enableCalcul: boolean;
    ecomindEnableCalcul: boolean;
    digitalService: DigitalService;
    countryMap: MapString;
    networkTypes: NetworkType[];
    serverTypes: Host[];
    terminalDeviceTypes: TerminalsType[];
    server: DigitalServiceServerConfig;
    inDatacenters: InDatacenterRest[];
    inPhysicalEquipments: InPhysicalEquipmentRest[];
    inVirtualEquipments: InVirtualEquipmentRest[];
    refresh: number;
    isSharedDs: boolean;
}

const initialState: DigitalServiceState = {
    enableCalcul: false,
    ecomindEnableCalcul: false,
    digitalService: {} as DigitalService,
    countryMap: {} as MapString,
    networkTypes: [] as NetworkType[],
    terminalDeviceTypes: [] as TerminalsType[],
    serverTypes: [] as Host[],
    server: {} as DigitalServiceServerConfig,
    inDatacenters: [] as InDatacenterRest[],
    inPhysicalEquipments: [] as InPhysicalEquipmentRest[],
    inVirtualEquipments: [] as InVirtualEquipmentRest[],
    refresh: 0,
    isSharedDs: false,
};

@Injectable({
    providedIn: "root",
})
export class DigitalServiceStoreService {
    private readonly inPhysicalEquipmentsService = inject(InPhysicalEquipmentsService);
    private readonly inVirtualEquipmentsService = inject(InVirtualEquipmentsService);
    private readonly _store = signal(initialState);

    readonly enableCalcul = computed(() => this._store().enableCalcul);
    setEnableCalcul(enableCalcul: boolean) {
        this._store.update((s) => ({ ...s, enableCalcul }));
    }

    readonly ecomindEnableCalcul = computed(() => this._store().ecomindEnableCalcul);
    setEcoMindEnableCalcul(ecomindEnableCalcul: boolean) {
        this._store.update((s) => ({ ...s, ecomindEnableCalcul }));
    }

    readonly digitalService = computed(() => this._store().digitalService);
    setDigitalService(digitalService: DigitalService) {
        this._store.update((s) => ({ ...s, digitalService }));
    }

    readonly countryMap = computed(() => this._store().countryMap);
    setCountryMap(countryMap: MapString) {
        this._store.update((s) => ({ ...s, countryMap }));
    }

    readonly networkTypes = computed(() => this._store().networkTypes);
    setNetworkTypes(networkTypes: NetworkType[]) {
        this._store.update((s) => ({ ...s, networkTypes }));
    }

    readonly serverTypes = computed(() => this._store().serverTypes);
    setServerTypes(serverTypes: Host[]) {
        this._store.update((s) => ({ ...s, serverTypes }));
    }

    readonly terminalDeviceTypes = computed(() => this._store().terminalDeviceTypes);
    setTerminalDeviceTypes(terminalDeviceTypes: TerminalsType[]) {
        this._store.update((s) => ({ ...s, terminalDeviceTypes }));
    }

    readonly server = computed(() => this._store().server);
    setServer(server: DigitalServiceServerConfig) {
        this._store.update((s) => ({ ...s, server: { ...server } }));
    }

    readonly inDatacenters = computed(() => this._store().inDatacenters);
    setInDatacenters(inDatacenters: InDatacenterRest[]) {
        for (const data of inDatacenters) {
            data.displayLabel = `${data.name.split("|")[0]} (${data.location} - PUE = ${data.pue})`;
        }
        this._store.update((s) => ({ ...s, inDatacenters }));
    }

    readonly inPhysicalEquipments = computed(() => this._store().inPhysicalEquipments);
    setInPhysicalEquipments(inPhysicalEquipments: InPhysicalEquipmentRest[]) {
        this._store.update((s) => ({ ...s, inPhysicalEquipments }));
    }
    async initInPhysicalEquipments(uid: string) {
        const inPhysicalEquipments = await firstValueFrom(
            this.inPhysicalEquipmentsService.get(uid),
        );
        this._store.update((s) => ({
            ...s,
            inPhysicalEquipments: [...inPhysicalEquipments],
        }));
    }

    readonly inVirtualEquipments = computed(() => this._store().inVirtualEquipments);
    setInVirtualEquipments(inVirtualEquipments: InVirtualEquipmentRest[]) {
        this._store.update((s) => ({ ...s, inVirtualEquipments }));
    }
    async initInVirtualEquipments(uid: string) {
        const inVirtualEquipments = await firstValueFrom(
            this.inVirtualEquipmentsService.getByDigitalService(uid),
        );
        this._store.update((s) => ({ ...s, inVirtualEquipments }));
    }

    readonly refresh = computed(() => this._store().refresh);
    setRefresh(refresh: number) {
        this._store.update((s) => ({ ...s, refresh }));
    }

    readonly isSharedDS = computed(() => this._store().isSharedDs);
    setIsSharedDS(isSharedDs: boolean) {
        this._store.update((s) => ({ ...s, isSharedDs }));
    }
}
