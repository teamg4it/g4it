/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { Note } from "./note.interface";

export interface Inventory {
    id: number;
    type?: string;
    name: string;
    creationDate: Date;
    lastUpdateDate: Date;
    workspace: string;
    dataCenterCount: number;
    physicalEquipmentCount: number;
    virtualEquipmentCount: number;
    applicationCount: number;
    enableDataInconsistency: boolean;
    date?: Date;
    note?: Note;
    criteria?: string[];
    tasks: TaskRest[];
    lastTaskLoading?: TaskRest;
    lastTaskEvaluating?: TaskRest;
    workspaceId?: number;
}

export interface InventoryUpdateRest {
    id: number;
    name: string;
    note?: Note;
}

export interface CreateInventory {
    type: string;
    name: string;
}

export interface TaskRest {
    id: number;
    status: string;
    creationDate: Date;
    progressPercentage: string;
    progress?: number;
    type: string;
    resultFileUrl: string;
    details: string[];
    criteria?: string[];
}

export interface IntegrationBatchLaunchDetail {
    inventoryId: number;
    time: Date;
}

export interface ExportReport {
    batchStatusCode: string;
    createTime: Date;
    endTime: Date;
    batchName: string;
    resultFileUrl: string;
    resultFileSize: number;
}

export interface InventoryFilterSet {
    [key: string]: Set<string>;
}

export interface InventoryCriteriaRest {
    id: number;
    name: string;
    criteria: string[];
    note: Note;
    enableDataInconsistency: boolean;
}
