import { TestBed } from "@angular/core/testing";
import { of } from "rxjs";
import { DigitalServiceStoreService } from "./digital-service.store";
import { InPhysicalEquipmentsService } from "../service/data/in-out/in-physical-equipments.service";
import { InVirtualEquipmentsService } from "../service/data/in-out/in-virtual-equipments.service";
import {
    DigitalService,
    DigitalServiceServerConfig,
    Host,
    NetworkType,
    TerminalsType,
} from "../interfaces/digital-service.interfaces";
import {
    InDatacenterRest,
    InPhysicalEquipmentRest,
    InVirtualEquipmentRest,
} from "../interfaces/input.interface";

describe("DigitalServiceStoreService", () => {
    let service: DigitalServiceStoreService;

    const mockInPhysicalEquipmentsService = {
        get: jasmine.createSpy("get").and.returnValue(
            of([{ id: 1, label: "PE1" }] as unknown as InPhysicalEquipmentRest[]),
        ),
    };

    const mockInVirtualEquipmentsService = {
        getByDigitalService: jasmine
            .createSpy("getByDigitalService")
            .and.returnValue(of([{ id: 10, label: "VE1" }] as unknown as InVirtualEquipmentRest[])),
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                DigitalServiceStoreService,
                { provide: InPhysicalEquipmentsService, useValue: mockInPhysicalEquipmentsService },
                { provide: InVirtualEquipmentsService, useValue: mockInVirtualEquipmentsService },
            ],
        });
        service = TestBed.inject(DigitalServiceStoreService);
    });

    it("should create", () => {
        expect(service).toBeTruthy();
    });

    it("should have initial default values", () => {
        expect(service.enableCalcul()).toBeFalse();
        expect(service.ecomindEnableCalcul()).toBeFalse();
        expect(service.inPhysicalEquipments().length).toBe(0);
        expect(service.inVirtualEquipments().length).toBe(0);
        expect(service.isSharedDS()).toBeFalse();
    });

    it("setEnableCalcul & setEcoMindEnableCalcul should update signals", () => {
        service.setEnableCalcul(true);
        service.setEcoMindEnableCalcul(true);
        expect(service.enableCalcul()).toBeTrue();
        expect(service.ecomindEnableCalcul()).toBeTrue();
    });

    it("setDigitalService should update digitalService signal", () => {
        const ds = { uid: "ds-1" } as DigitalService;
        service.setDigitalService(ds);
        expect(service.digitalService().uid).toBe("ds-1");
    });

    it("setCountryMap should update countryMap signal", () => {
        service.setCountryMap({ FR: "France" });
        expect(service.countryMap()['FR']).toBe("France");
    });

    it("setNetworkTypes / setTerminalDeviceTypes / setServerTypes should update lists", () => {
        service.setNetworkTypes([{ code: "NT1" } as NetworkType]);
        service.setTerminalDeviceTypes([{ code: "TT1" } as TerminalsType]);
        service.setServerTypes([{ value: "Server A" } as Host]);
        expect(service.networkTypes()[0].code).toBe("NT1");
        expect(service.terminalDeviceTypes()[0].code).toBe("TT1");
        expect(service.serverTypes()[0].value).toBe("Server A");
    });


    it("setInDatacenters should add displayLabel", () => {
        const dcs = [
            {
                name: "DC1|meta",
                location: "Paris",
                pue: 1.4,
            },
        ] as unknown as InDatacenterRest[];
        service.setInDatacenters(dcs);
        expect(service.inDatacenters()[0].displayLabel).toBe("DC1 (Paris - PUE = 1.4)");
    });

    it("setInPhysicalEquipments should update list", () => {
        const list = [{ id: 2 }] as unknown as InPhysicalEquipmentRest[];
        service.setInPhysicalEquipments(list);
        expect(service.inPhysicalEquipments().length).toBe(1);
        expect(service.inPhysicalEquipments()[0].id).toBe(2);
    });

    it("initInPhysicalEquipments should fetch and set data", async () => {
        await service.initInPhysicalEquipments("ds-1");
        expect(mockInPhysicalEquipmentsService.get).toHaveBeenCalledWith("ds-1");
        expect(service.inPhysicalEquipments().length).toBe(1);
    });

    it("setInVirtualEquipments should update list", () => {
        const list = [{ id: 9 }] as unknown as InVirtualEquipmentRest[];
        service.setInVirtualEquipments(list);
        expect(service.inVirtualEquipments()[0].id).toBe(9);
    });

    it("initInVirtualEquipments should fetch and set data", async () => {
        await service.initInVirtualEquipments("ds-2");
        expect(mockInVirtualEquipmentsService.getByDigitalService).toHaveBeenCalledWith("ds-2");
        expect(service.inVirtualEquipments().length).toBe(1);
    });

    it("setRefresh should update refresh counter", () => {
        service.setRefresh(5);
        expect(service.refresh()).toBe(5);
    });

    it("setIsSharedDS should update isSharedDS flag", () => {
        service.setIsSharedDS(true);
        expect(service.isSharedDS()).toBeTrue();
    });
});
