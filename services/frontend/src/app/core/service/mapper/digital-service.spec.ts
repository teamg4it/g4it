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
});
