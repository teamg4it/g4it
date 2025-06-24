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
            const physicalEquipments: OutPhysicalEquipmentRest[] = [];
            const deviceTypes: TerminalsType[] = [];
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
