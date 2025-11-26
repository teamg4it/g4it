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
}

export enum DigitalServiceVersionType {
    ACTIVE = "active",
    DRAFT = "draft",
    ARCHIVED = "archived",
}
