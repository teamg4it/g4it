export interface DigitalServiceVersionRequestBody {
    dsName: string;
    versionName: string;
    isAi?: boolean;
}

export enum DigitalServiceVersionType {
    ACTIVE = "active",
    DRAFT = "draft",
    ARCHIVED = "archived",
}
