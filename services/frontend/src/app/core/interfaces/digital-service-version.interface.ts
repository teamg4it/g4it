import { DigitalServiceFootprint } from "./digital-service.interfaces";
import { OutPhysicalEquipmentRest, OutVirtualEquipmentRest } from "./output.interface";

export interface DigitalServiceVersionRequestBody {
    dsName: string;
    versionName: string;
    isAi?: boolean;
}

export interface DigitalServiceVersionResponse {
    digitalServiceUid: string;
    digitalServiceVersionUid: string;
    versionName: string;
    versionType: string;
    selected?: boolean;
}

export interface DigitalServicePromoteResponse {
    digitalServiceUid: string;
    digitalServiceVersionUid: string;
    isPromoted: boolean;
}

export const DigitalServiceVersionType = {
    ACTIVE: "active",
    DRAFT: "draft",
    ARCHIVED: "archived",
} as const

export interface CompareVersion {
    versionId: string;
    versionName: string;
    physicalEquipment: OutPhysicalEquipmentRest[];
    virtualEquipment: OutVirtualEquipmentRest[];
    convertToChartData?: DigitalServiceFootprint[];
}
