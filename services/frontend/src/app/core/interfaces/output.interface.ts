export interface OutPhysicalEquipmentRest {
    name: string;
    criterion: string;
    lifecycleStep: string;
    statusIndicator: string;
    datacenterName: string;
    location: string;
    equipmentType: string;
    reference: string;
    hostingEfficiency: string;
    engineName: string;
    engineVersion: string;
    referentialVersion: string;
    unit: string;
    countValue: number;
    unitImpact: number;
    peopleEqImpact: number;
    electricityConsumption: number;
    quantity: number;
    lifespan: number;
    numberOfUsers: number;
    commonFilters?: string[];
    filters?: string[];
    errors?: string[];
    source?: string;
}

export interface OutVirtualEquipmentRest {
    name: string;
    criterion: string;
    lifecycleStep: string;
    datacenterName: string;
    physicalEquipmentName: string;
    infrastructureType: string;
    instanceType: string;
    type: string;
    provider: string;
    equipmentType: string;
    location: string;
    engineName: string;
    engineVersion: string;
    referentialVersion: string;
    statusIndicator: string;
    countValue: number;
    quantity: number;
    unitImpact: number;
    peopleEqImpact: number;
    electricityConsumption: number;
    unit: string;
    usageDuration: number;
    workload: number;
    commonFilters?: string[];
    filters?: string[];
    filtersPhysicalEquipment?: string[];
    errors?: string[];
    source?: string;
}

export interface OutApplicationsRest {
    name: string;
    virtualEquipmentName: string;
    environment: string;
    criterion: string;
    lifecycleStep: string;
    location: string;
    equipmentType: string;
    engineName: string;
    engineVersion: string;
    referentialVersion: string;
    statusIndicator: string;
    unit: string;
    countValue: number;
    unitImpact: number;
    peopleEqImpact: number;
    electricityConsumption: number;
    quantity: number;
    provider?: string;
    commonFilters?: string[];
    filters?: string[];
    filtersPhysicalEquipment?: string[];
    filtersVirtualEquipment?: string[];
    errors?: string[];
}
