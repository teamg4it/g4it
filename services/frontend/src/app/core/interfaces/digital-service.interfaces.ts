/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { DropdownValue } from "./generic.interfaces";
import { TaskRest } from "./inventory.interfaces";
import { Note } from "./note.interface";

export interface DigitalService {
    uid: string;
    name: string;
    creationDate: number;
    lastUpdateDate: number;
    lastCalculationDate: number | null;
    terminals: DigitalServiceTerminalConfig[];
    servers: DigitalServiceServerConfig[];
    networks: DigitalServiceNetworkConfig[];
    note?: Note;
    userId?: number;
    criteria?: string[];
    creator?: DigitalServiceUserInfo;
    isAi?: boolean;
    isShared?: boolean;
    tasks?: TaskRest[];
    enableDataInconsistency: boolean;
    activeDsvUid: string;
}

export interface DigitalServiceUserInfo {
    id: number;
    firstName: string;
    lastName: string;
}

export interface DigitalServiceParameterIa {
    id: number;
    modelName: string;
    nbParameters: string;
    framework: string;
    quantization: string;
    totalGeneratedTokens: number;
    numberUserYear: number;
    averageNumberRequest: number;
    averageNumberToken: number;
    isInference: boolean;
    isFinetuning: boolean;
    creationDate: string;
    lastUpdateDate: string;
}

export interface AiModelConfig {
    modelName: string;
    parameters: string;
    framework: string;
    quantization: string;
}

export interface DigitalServiceServerConfig {
    id?: number;
    sumOfVmQuantity?: number;
    uid?: string;
    creationDate?: number;
    name: string;
    mutualizationType: string;
    type: string;
    quantity: number;
    host?: Host;
    hostValue?: string;
    datacenter?: ServerDC;
    datacenterName?: string;
    totalVCpu?: number;
    totalDisk?: number;
    lifespan?: number;
    annualElectricConsumption?: number;
    annualOperatingTime?: number;
    vm: ServerVM[];
}

export interface DigitalServiceCloudServiceConfig {
    id: number;
    digitalServiceUid: string;
    creationDate?: number;
    name: string;
    cloudProvider: string;
    instanceType: string;
    quantity: number;
    location: DropdownValue;
    locationValue: string;
    annualUsage: number;
    averageWorkload: number;
    idFront?: number;
}

export interface DigitalServicesAiInfrastructure {
    infrastructureType: "SERVER_DC" | "LAPTOP" | "DESKTOP";
    nbCpuCores: number;
    nbGpu: number;
    gpuMemory: number;
    ramSize: number;
    location: string;
    pue: number;
}

export interface DigitalServiceNetworkConfig {
    id?: number;
    uid?: string;
    creationDate?: Date;
    type: NetworkType;
    typeCode?: string;
    yearlyQuantityOfGbExchanged: number;
    idFront?: number;
    name: string;
}

export interface DigitalServiceTerminalConfig {
    id?: number;
    uid?: string;
    creationDate?: number | string;
    typeCode?: string;
    type: TerminalsType;
    lifespan: number;
    country: string;
    numberOfUsers: number;
    yearlyUsageTimePerUser: number;
    idFront?: number;
    name: string;
}

export interface DigitalServiceFootprint {
    tier: string;
    impacts: DigitalServiceFootprintImpact[];
}

export interface DigitalServiceFootprintImpact {
    criteria: string;
    sipValue: number;
    unitValue: number;
    unit: string;
    status: string;
    countValue: number;
}

export interface TerminalsType {
    code: string;
    value: string;
    lifespan: number;
}

export interface EcomindType {
    code: string;
    value: string;
    lifespan: number;
    defaultCpuCores: number;
    defaultGpuCount: number;
    defaultGpuMemory: number;
    defaultRamSize: number;
    defaultDatacenterPue: number;
}

export interface NetworkType {
    code: string;
    value: string;
    type: string;
    annualQuantityOfGo: number;
    country: string;
}

export interface DigitalServiceTerminalsImpact {
    criteria: string;
    impactCountry: TerminalsImpactTypeLocation[];
    impactType: TerminalsImpactTypeLocation[];
}

export interface DigitalServiceCloudImpact {
    criteria: string;
    impactLocation: CloudImpactTypeLocation[];
    impactInstance: CloudImpactTypeLocation[];
}

export interface TerminalsImpact {
    rawValue?: any;
    unit?: any;
    name: string;
    totalSipValue: number;
    totalNbUsers: number;
    avgUsageTime: number;
    impact: ImpactTerminalsACVStep[];
}

export interface TerminalImpactGroup {
    [location: string]: {
        [terminalName: string]: ImpactTerminalsACVStep[];
    };
}

export interface TerminalsImpactTypeLocation {
    name: string;
    terminals: TerminalsImpact[];
    status: StatusCount;
}

export interface CloudsImpact {
    rawValue?: any;
    unit?: any;
    name: string;
    totalSipValue: number;
    totalQuantity: number;
    totalAvgUsage: number;
    totalAvgWorkLoad: number;
    impact: ImpactCloudsACVStep[];
}

export interface CloudImpactGroup {
    [location: string]: {
        [cloudName: string]: CloudNameImpact[];
    };
}

export interface CloudNameImpact {
    lifecycleStep: string;
    peopleEqImpact: number;
    unitImpact: number;
    quantity: number;
    usageDuration: number;
    workload: number;
    unit: string;
    statusIndicator: string;
    countValue: number;
    provider: string;
    statusCount: StatusCount;
}

export interface CloudImpactTypeLocation {
    name: string;
    clouds: CloudsImpact[];
    status: StatusCount;
}

export interface DigitalServiceNetworksImpact {
    criteria: string;
    impacts: ImpactNetworkNames[];
}

export interface ImpactNetworkNames {
    status: StatusCount;
    networkType: string;
    items: ImpactNetworkSipValue[];
}

export interface DigitalServiceServersImpact {
    criteria: string;
    impactsServer: ServersType[];
}

export interface ServersType {
    serverType: string;
    mutualizationType: string;
    servers: ServerImpact[];
}

export interface ServerImpact {
    name: string;
    totalSipValue: number;
    totalRawValue?: number;
    hostingEfficiency?: string;
    impactVmDisk: ImpactSipValue[];
    impactStep: ImpactACVStep[];
}

export interface ImpactACVStep {
    rawValue?: number;
    unit?: string;
    acvStep: string;
    sipValue: number;
    status?: string;
    countValue: number;
}

export interface ImpactTerminalsACVStep {
    acvStep: string;
    sipValue: number;
    rawValue: number;
    unit: string;
    status?: string;
    statusCount?: {
        ok: number;
        error: number;
        total: number;
    };
}

export interface ImpactCloudsACVStep {
    acvStep: string;
    sipValue: number;
    rawValue: number;
    unit: string;
    status?: string;
    statusCount?: {
        ok: number;
        error: number;
        total: number;
    };
}

export interface ImpactNetworkSipValue {
    unit?: string;
    networkType: string;
    sipValue: number;
    rawValue: number;
    status: string;
    countValue: number;
    name?: string;
}

export interface ImpactSipValue {
    rawValue?: number;
    unit?: string;
    name: string;
    sipValue: number;
    quantity: number;
    status?: string;
    countValue: number;
}
export interface ServerVM {
    uid: string;
    name: string;
    vCpu: number;
    disk: number;
    quantity: number;
    annualOperatingTime: number;
    electricityConsumption: number;
}

export interface ServerDC {
    uid?: string;
    name: string;
    location: string;
    pue: number;
    displayLabel?: string;
}

export interface Host {
    code: number;
    value: string;
    type?: string;
    reference?: string;
    characteristic: HostCharacteristics[];
}

export interface HostCharacteristics {
    code: string;
    value: number;
}

export interface DSCriteriaRest {
    uid: string;
    name: string;
    creator?: DigitalServiceUserInfo;
    members: DigitalServiceUserInfo[];
    creationDate: Date;
    lastUpdateDate: Date;
    lastCalculationDate: Date;
    criteria: string[];
    note?: Note;
}

export interface StatusCountMap {
    [key: string]: {
        status: StatusCount;
    };
}

export interface StatusCount {
    ok: number;
    error: number;
    total: number;
}

export interface AiRecommendation {
    id: number;
    taskId: number;
    electricityConsumption: number;
    runtime: number;
    recommendations: string; // JSON string
    digitalServiceUid: string;
    creationDate: string;
    lastUpdateDate: string;
}

export interface ShareLinkResp {
    url: string;
    expiryDate: Date;
}

export interface GraphDescriptionContent {
    description: string;
    scale: string;
    textDescription: string;
    analysis: string;
    toGoFurther: string;
}
