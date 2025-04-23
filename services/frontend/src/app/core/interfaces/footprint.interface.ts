export interface Stat {
    label: string;
    value?: number;
    unit?: string;
    title: string;
    description: string;
    lang?: string;
}

export interface CriteriaCalculated {
    footprints: FootprintCalculated[];
    hasError: boolean;
    total: SumImpact;
    criteriasCount?: StatusCountMap;
}

export interface FootprintCalculated {
    data: string;
    impacts: Impact[];
    total: SumImpact;
    status: OkErrorStatus;
}

interface StatusCountMap {
    [key: string]: {
        status: StatusCount;
    };
}

interface StatusCount {
    ok: number;
    error: number;
    total: number;
}

export interface Impact {
    criteria: string;
    sumSip: number;
    sumImpact: number;
}

export interface SumImpact {
    impact: number;
    sip: number;
}

export interface EchartPieDataItem {
    name: string;
    value: number;
    otherData: OtherDataValue;
    status: OkErrorStatus;
}

interface OkErrorStatus {
    ok: number;
    error: number;
    total: number;
}

export interface OtherDataValue {
    name: string;
    value: number;
    percent: number;
}

export interface Criterias {
    [key: string]: Criteria;
}

export interface Criteria {
    label: string;
    unit: string;
    impacts: Impact[];
}

export interface ChartData<ComputedSelection> {
    [key: string]: ComputedSelection;
}

export interface ComputedSelection {
    acvStep: DataComputed[];
    country: DataComputed[];
    entity: DataComputed[];
    equipment: DataComputed[];
    status: DataComputed[];
}

export interface DataComputed {
    name: string;
    impact: number;
    sip: number;
}

export interface Impact {
    acvStep: string;
    country: string;
    entity: string | null;
    equipment: string | null;
    status: string | null;
    impact: number;
    sip: number;
    statusIndicator: string;
    countValue: number;
    quantity?: number;
    unit?: string;
}

export interface ImpactEntity extends Impact {
    id: number;
}

export interface Datacenter {
    dataCenterName: string;
    physicalEquipmentCount: number;
    country: string | null;
    entity: string | null;
    equipment: string;
    status: string;
    pue: number;
    count?: number;
    avgWeightedPue?: number;
}

export interface PhysicalEquipment {
    organisation: string;
    inventoryDate: string;
    country: string | null;
    type: string;
    nomEntite: string | null;
    statut: string;
    poids: number;
    ageMoyen: number;
    avgWeightedAge?: number;
}

export interface PhysicalEquipmentAvgAge {
    organisation: string;
    inventoryDate: string;
    country: string | null;
    type: string;
    nomEntite: string | null;
    statut: string;
    poids: number;
    ageMoyen: number;
    avgWeightedAge?: number;
}

export interface PhysicalEquipmentLowImpact {
    inventoryDate: string;
    country: string | null;
    type: string;
    nomEntite: string | null;
    statut: string;
    quantite: number;
    lowImpact: boolean;
    pourcentageLowImpact?: number;
    count?: number;
}

export interface PhysicalEquipmentsElecConsumption {
    country: string;
    type: string;
    nomEntite: string;
    statut: string;
    elecConsumption: number;
}

export interface PhysicalEquipmentStats {
    averageAge: PhysicalEquipmentAvgAge[];
    lowImpact: PhysicalEquipmentLowImpact[];
    elecConsumption: PhysicalEquipmentsElecConsumption[];
}

export interface ApplicationFootprint {
    id?: number;
    criteria: string;
    unit: string;
    criteriaTitle: string;
    impacts: ApplicationImpact[];
}

export interface ApplicationImpact {
    applicationName: string;
    domain: string;
    subDomain: string;
    environment: string;
    equipmentType: string;
    lifeCycle: string;
    virtualEquipmentName: string;
    cluster: string;
    impact: number;
    sip: number;
    statusIndicator: string;
    criteria?: string;
}

export interface ImpactGraph {
    domain: string;
    sipImpact: number;
    unitImpact: number;
    subdomain: string;
    app: string;
    equipment: string;
    environment: string;
    virtualEquipmentName: string;
    cluster: string;
    subdomains: string[];
    apps: string[];
    lifecycle: string;
    status: StatusCount;
}

export interface ApplicationCriteriaFootprint {
    criteria: string;
    criteriaTitle: string;
    unit: string;
    impacts: ApplicationCriteriaImpact[];
}

export interface ApplicationCriteriaImpact {
    environment: string;
    equipmentType: string;
    lifeCycle: string;
    impact: number;
    sip: number;
    virtualEquipmentName: string;
    cluster: string;
}

export interface ApplicationGraphPosition {
    domain: string;
    subdomain: string;
    app: string;
    graph: string;
}
