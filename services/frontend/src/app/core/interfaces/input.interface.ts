export interface InDatacenterRest {
    id?: number;
    inventoryId?: number;
    digitalServiceUid?: string;
    name: string;
    pue: number;
    location: string;
    displayLabel?: string;
}

export interface InPhysicalEquipmentRest {
    id?: number;
    name: string;
    inventoryId?: number;
    digitalServiceUid: string;
    datacenterName?: string;
    quantity: number;
    location: string;
    type: string;
    model?: string;
    manufacturer?: string;
    datePurchase?: string;
    dateWithdrawal?: string;
    cpuType?: string;
    cpuCoreNumber?: number;
    sizeMemoryGb?: number;
    sizeDiskGb?: number;
    source?: string;
    description?: string;
    electricityConsumption?: number;
    durationHour?: number;
    commonFilters?: string[];
    filters?: string[];
    creationDate?: string;
    lastUpdatedDate?: string;
    numberOfUsers?: number;
}

export interface InVirtualEquipmentRest {
    id: number;
    name: string;
    inventoryId?: number;
    digitalServiceUid: string;
    datacenterName?: string;
    physicalEquipmentName?: string;
    quantity: number;
    infrastructureType: string;
    instanceType?: string;
    type?: string;
    provider?: string;
    location: string;
    durationHour?: number;
    workload?: number;
    electricityConsumption?: number;
    vcpuCoreNumber?: number;
    sizeMemoryMb?: number;
    sizeDiskGb?: number;
    commonFilters?: string[];
    filters?: string[];
    creationDate?: string;
    lastUpdatedDate?: string;
    allocationFactor?: number;
}
