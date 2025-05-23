/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { DropdownValue } from "./generic.interfaces";
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
}

export interface DigitalServiceUserInfo {
    id: number;
    firstName: string;
    lastName: string;
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

export interface DigitalServiceNetworkConfig {
    id?: number;
    uid?: string;
    creationDate?: number;
    type: NetworkType;
    typeCode?: string;
    yearlyQuantityOfGbExchanged: number;
    idFront?: number;
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

export interface NetworkType {
    code: string;
    value: string;
    type: string;
    annualQuantityOfGo: number;
    country: string;
}

export interface DigitalServiceTerminalsImpact {
    criteria: string;
    impactCountry: TerminalsImpact[];
    impactType: TerminalsImpact[];
}

export interface DigitalServiceCloudImpact {
    criteria: string;
    impactLocation: CloudsImpact[];
    impactInstance: CloudsImpact[];
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

export interface DigitalServiceNetworksImpact {
    criteria: string;
    impacts: ImpactNetworkSipValue[];
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
