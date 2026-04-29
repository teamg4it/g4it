import { TestBed } from "@angular/core/testing";
import { of } from "rxjs";
import { DigitalServiceStoreService } from "./digital-service.store";
import { InPhysicalEquipmentsService } from "../service/data/in-out/in-physical-equipments.service";
import { InVirtualEquipmentsService } from "../service/data/in-out/in-virtual-equipments.service";

describe("DigitalServiceStoreService", () => {
    let service: DigitalServiceStoreService;

    const physicalMock = {
        get: jasmine.createSpy().and.returnValue(of([{ id: 1 }])),
    };

    const virtualMock = {
        getByDigitalService: jasmine.createSpy().and.returnValue(of([{ id: 2 }])),
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                DigitalServiceStoreService,
                { provide: InPhysicalEquipmentsService, useValue: physicalMock },
                { provide: InVirtualEquipmentsService, useValue: virtualMock },
            ],
        });

        service = TestBed.inject(DigitalServiceStoreService);
    });

    // -------------------------
    // BASIC
    // -------------------------
    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    // -------------------------
    // SIMPLE SETTERS
    // -------------------------
    it("should set enableCalcul", () => {
        service.setEnableCalcul(true);
        expect(service.enableCalcul()).toBeTrue();
    });

    it("should set ecomindEnableCalcul", () => {
        service.setEcoMindEnableCalcul(true);
        expect(service.ecomindEnableCalcul()).toBeTrue();
    });

    it("should set digitalService", () => {
        const ds: any = { name: "test" };
        service.setDigitalService(ds);
        expect(service.digitalService()).toEqual(ds);
    });

    it("should set countryMap", () => {
        const map = { FR: "France" };
        service.setCountryMap(map);
        expect(service.countryMap()).toEqual(map);
    });

    it("should set networkTypes", () => {
        const types: any = [{ name: "4G" }];
        service.setNetworkTypes(types);
        expect(service.networkTypes()).toEqual(types);
    });

    it("should set serverTypes", () => {
        const types: any = [{ name: "server" }];
        service.setServerTypes(types);
        expect(service.serverTypes()).toEqual(types);
    });

    it("should set terminalDeviceTypes", () => {
        const types: any = [{ name: "mobile" }];
        service.setTerminalDeviceTypes(types);
        expect(service.terminalDeviceTypes()).toEqual(types);
    });

    it("should set server", () => {
        const server: any = { cpu: 4 };
        service.setServer(server);
        expect(service.server()).toEqual(server);
    });

    it("should set refresh", () => {
        service.setRefresh(5);
        expect(service.refresh()).toBe(5);
    });

    it("should set isSharedDS", () => {
        service.setIsSharedDS(true);
        expect(service.isSharedDS()).toBeTrue();
    });

    // -------------------------
    // DATACENTERS
    // -------------------------
    it("should set inDatacenters and build displayLabel", () => {
        const data: any = [
            {
                name: "DC1|extra",
                location: "FR",
                pue: 1.2,
            },
        ];

        service.setInDatacenters(data);

        expect(service.inDatacenters().length).toBe(1);
        expect(service.inDatacenters()[0].displayLabel).toContain("DC1");
        expect(service.inDatacenters()[0].displayLabel).toContain("PUE");
    });

    it("should add datacenter", () => {
        const dc: any = {
            name: "DC2|extra",
            location: "DE",
            pue: 1.5,
        };

        service.addDatacenter(dc);

        expect(service.inDatacenters().length).toBe(1);
        expect(service.inDatacenters()[0].displayLabel).toContain("DC2");
    });

    // -------------------------
    // PHYSICAL EQUIPMENTS
    // -------------------------
    it("should set inPhysicalEquipments", () => {
        const data: any = [{ id: 1 }];
        service.setInPhysicalEquipments(data);

        expect(service.inPhysicalEquipments()).toEqual(data);
    });

    it("should initInPhysicalEquipments", async () => {
        await service.initInPhysicalEquipments("uid");

        expect(physicalMock.get).toHaveBeenCalledWith("uid");
        expect(service.inPhysicalEquipments().length).toBe(1);
    });

    // -------------------------
    // VIRTUAL EQUIPMENTS
    // -------------------------
    it("should set inVirtualEquipments", () => {
        const data: any = [{ id: 2 }];
        service.setInVirtualEquipments(data);

        expect(service.inVirtualEquipments()).toEqual(data);
    });

    it("should initInVirtualEquipments", async () => {
        await service.initInVirtualEquipments("uid");

        expect(virtualMock.getByDigitalService).toHaveBeenCalledWith("uid");
        expect(service.inVirtualEquipments().length).toBe(1);
    });
});