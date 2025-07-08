import {
    DigitalServiceCloudImpact,
    DigitalServiceFootprint,
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalsImpact,
    Host,
    NetworkType,
    TerminalsType,
} from "../../interfaces/digital-service.interfaces";
import { MapString } from "../../interfaces/generic.interfaces";
import {
    OutPhysicalEquipmentRest,
    OutVirtualEquipmentRest,
} from "../../interfaces/output.interface";
import {
    convertToGlobalVision,
    transformOutPhysicalEquipmentsToNetworkData,
    transformOutPhysicalEquipmentstoServerData,
    transformOutPhysicalEquipmentsToTerminalData,
    transformOutVirtualEquipmentsToCloudData,
} from "./digital-service";

describe("Digital Service Mapper", () => {
    describe("convertToGlobalVision", () => {
        it("should convert physical and virtual equipment to global vision format", () => {
            const physicalEquipments: OutPhysicalEquipmentRest[] = [];
            const virtualEquipments: OutVirtualEquipmentRest[] = [];
            const result: DigitalServiceFootprint[] = convertToGlobalVision(
                physicalEquipments,
                virtualEquipments,
            );
            expect(result).toBeDefined();
            expect(Array.isArray(result)).toBe(true);
        });
    });

    describe("transformOutPhysicalEquipmentsToTerminalData", () => {
        it("should transform physical equipment to terminal data", () => {
            const physicalEquipments: any[] = [
                {
                    name: "Terminal 2",
                    criterion: "ACIDIFICATION",
                    lifecycleStep: "TRANSPORTATION",
                    statusIndicator: "OK",
                    location: "Egypt",
                    equipmentType: "Terminal",
                    unit: "mol H+ eq",
                    reference: "smartphone-2",
                    countValue: 1,
                    unitImpact: 0.000022824964931506846,
                    peopleEqImpact: 1.8259971945205475e-7,
                    electricityConsumption: 0,
                    quantity: 0.001141552511415525,
                    numberOfUsers: 2,
                    lifespan: 0.00684931506849315,
                    commonFilters: [""],
                    filters: [""],
                },
            ];
            const deviceTypes: TerminalsType[] = [
                {
                    code: "smartphone-2",
                    value: "Mobile Phone",
                    lifespan: 2.5,
                },
            ];
            const result: DigitalServiceTerminalsImpact[] =
                transformOutPhysicalEquipmentsToTerminalData(
                    physicalEquipments,
                    deviceTypes,
                );
            expect(result).toBeDefined();
            expect(Array.isArray(result)).toBe(true);
        });
    });

    describe("transformOutPhysicalEquipmentsToNetworkData", () => {
        it("should transform physical equipment to network data", () => {
            const physicalEquipments: OutPhysicalEquipmentRest[] = [];
            const networkTypes: NetworkType[] = [];
            const result: DigitalServiceNetworksImpact[] =
                transformOutPhysicalEquipmentsToNetworkData(
                    physicalEquipments,
                    networkTypes,
                );
            expect(result).toBeDefined();
            expect(Array.isArray(result)).toBe(true);
        });
    });

    describe("transformOutPhysicalEquipmentstoServerData", () => {
        it("should transform physical and virtual equipment to server data", () => {
            const physicalEquipments: OutPhysicalEquipmentRest[] = [];
            const virtualEquipments: OutVirtualEquipmentRest[] = [];
            const serverTypes: Host[] = [];
            const result: DigitalServiceServersImpact[] =
                transformOutPhysicalEquipmentstoServerData(
                    physicalEquipments,
                    virtualEquipments,
                    serverTypes,
                );
            expect(result).toBeDefined();
            expect(Array.isArray(result)).toBe(true);
        });
    });

    describe("transformOutVirtualEquipmentsToCloudData", () => {
        it("should transform virtual equipment to cloud data", () => {
            const virtualEquipments: OutVirtualEquipmentRest[] = [];
            const countryMap: MapString = {};
            const result: DigitalServiceCloudImpact[] =
                transformOutVirtualEquipmentsToCloudData(virtualEquipments, countryMap);
            expect(result).toBeDefined();
            expect(Array.isArray(result)).toBe(true);
        });
    });
    describe("transformOutPhysicalEquipmentsToNetworkData", () => {
        it("should return an empty array when no physical equipment is provided", () => {
            const physicalEquipments: OutPhysicalEquipmentRest[] = [];
            const networkTypes: NetworkType[] = [];
            const result = transformOutPhysicalEquipmentsToNetworkData(
                physicalEquipments,
                networkTypes,
            );
            expect(result).toEqual([]);
        });

        it("should group network data by criterion and map network types", () => {
            const physicalEquipments: OutPhysicalEquipmentRest[] = [
                {
                    name: "abe89b0f-4fa7-48f8-90ad-34b6d9306640",
                    criterion: "CLIMATE_CHANGE",
                    lifecycleStep: "END_OF_LIFE",
                    statusIndicator: "OK",
                    location: "France",
                    equipmentType: "Network",
                    unit: "kg CO2 eq",
                    reference: "fixed-line-network-1",
                    countValue: 1,
                    unitImpact: 0.0010218217006126842,
                    peopleEqImpact: 0.0000012003779155508772,
                    electricityConsumption: 0,
                    quantity: 0.001893939393939394,
                    numberOfUsers: 0,
                    lifespan: 0.001899128268991283,
                    commonFilters: [""],
                    filters: [""],
                    datacenterName: "Default Datacenter",
                    hostingEfficiency: "1.0",
                    engineName: "Default Engine",
                    engineVersion: "1.0",
                    referentialVersion: "1.0",
                },
            ];
            const networkTypes: NetworkType[] = [
                {
                    code: "fixed-line-network-1",
                    value: "Fiber Optic",
                    type: "Wired",
                    annualQuantityOfGo: 500,
                    country: "USA",
                },
                {
                    code: "fiber-2",
                    value: "DSL",
                    type: "Wired",
                    annualQuantityOfGo: 300,
                    country: "France",
                },
            ];
            const result = transformOutPhysicalEquipmentsToNetworkData(
                physicalEquipments,
                networkTypes,
            );
            expect(result).toBeDefined();
            expect(result.length).toBe(1);
            expect(result[0].criteria).toBe("climate-change");
            expect(result[0].impacts.length).toBe(1);
            expect(result[0].impacts[0].networkType).toBe("Fiber Optic");
        });
    });
});
