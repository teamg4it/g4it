import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
} from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import {
    OrganizationCriteriaRest,
    WorkspaceCriteriaRest,
    WorkspaceWithOrganization,
} from "src/app/core/interfaces/administration.interfaces";
import { DSCriteriaRest } from "src/app/core/interfaces/digital-service.interfaces";
import { InventoryCriteriaRest } from "src/app/core/interfaces/inventory.interfaces";
import { Organization } from "src/app/core/interfaces/user.interfaces";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-criteria-popup",
    templateUrl: "./criteria-popup.component.html",
    providers: [MonthYearPipe],
})
export class CriteriaPopupComponent implements OnChanges {
    @Input() displayPopup: boolean = false;
    @Input() type: "organization" | "workspace" | "inventory" | "ds" | undefined;
    @Input() selectedCriteriaIS: string[] = [];
    @Input() selectedCriteriaDS: string[] = [];
    //this organization contains the organization list
    @Input() organization!: OrganizationCriteriaRest;
    @Input() workspace!: WorkspaceCriteriaRest;
    //this workspace contains all the workspace details
    @Input() organizationDetails!: Organization;
    @Input() workspaceDetails!: WorkspaceWithOrganization;
    @Input() inventory: any;
    @Input() ds: any;

    @Output() outSaveWorkspace = new EventEmitter<WorkspaceCriteriaRest>();
    @Output() outSaveOrganization = new EventEmitter<OrganizationCriteriaRest>();
    @Output() outSaveInventory = new EventEmitter<InventoryCriteriaRest>();
    @Output() outSaveDS = new EventEmitter<DSCriteriaRest>();
    @Output() outClose = new EventEmitter<void>();

    constructor(
        private readonly globalStore: GlobalStoreService,
        private readonly translate: TranslateService,
        private readonly monthYear: MonthYearPipe,
    ) {}

    criteriaList: string[] = Object.keys(this.globalStore.criteriaList());
    tempSelectedCriteriaIS: string[] = [];
    tempSelectedCriteriaDS: string[] = [];
    defaultCriteria = Object.keys(this.globalStore.criteriaList()).slice(0, 5);
    hasChanged: boolean = false;
    allIs = ["All"];
    allDs = ["All"];

    ngOnChanges(changes: SimpleChanges) {
        if (changes["displayPopup"] && this.displayPopup) {
            this.criteriaList = Object.keys(this.globalStore.criteriaList());
            this.hasChanged = false;
        }
        if (changes["selectedCriteriaIS"] && this.type === "workspace") {
            this.tempSelectedCriteriaIS = [...this.selectedCriteriaIS];
        }
        if (changes["selectedCriteriaDS"] && this.type === "workspace") {
            this.tempSelectedCriteriaDS = [...this.selectedCriteriaDS];
        }
        if (changes["selectedCriteriaIS"] && this.type !== "workspace") {
            this.tempSelectedCriteriaIS = [...this.selectedCriteriaIS];
        }
        if (changes["workspaceDetails"]) {
            this.setCriteriaList();
        }
        if (changes["organization"]) {
            this.setCriteriaList();
        }
    }

    onCriteriaChange() {
        this.hasChanged = true;
    }

    closePopup() {
        this.selectedCriteriaIS = [...this.tempSelectedCriteriaIS];
        this.selectedCriteriaDS = [...this.tempSelectedCriteriaDS];
        this.outClose.emit();
    }

    setCriteriaList() {
        if (this.type === "workspace") {
            this.selectedCriteriaIS =
                this.workspaceDetails.criteriaIs ??
                this.organizationDetails?.criteria ??
                this.defaultCriteria;
            this.selectedCriteriaDS =
                this.workspaceDetails.criteriaDs ??
                this.organizationDetails?.criteria ??
                this.defaultCriteria;
        }
        if (this.type === "organization") {
            this.selectedCriteriaIS =
                this.organizationDetails?.criteria ?? this.defaultCriteria;
            this.selectedCriteriaDS =
                this.organizationDetails?.criteria ?? this.defaultCriteria;
        }
    }

    resetToDefault() {
        let initialSelectedCriteriaIS = [...this.selectedCriteriaIS];
        let initialSelectedCriteriaDS = [...this.selectedCriteriaDS];

        if (this.type === "organization") {
            this.selectedCriteriaIS = this.defaultCriteria;
        } else if (this.type === "workspace") {
            this.selectedCriteriaIS =
                this.organizationDetails?.criteria ?? this.defaultCriteria;
            this.selectedCriteriaDS =
                this.organizationDetails?.criteria ?? this.defaultCriteria;
        } else if (this.type === "inventory") {
            this.selectedCriteriaIS =
                this.workspace?.criteriaIs ??
                this.organization?.criteria ??
                this.defaultCriteria;
        } else if (this.type === "ds") {
            this.selectedCriteriaIS =
                this.workspaceDetails?.criteriaDs ??
                this.organizationDetails?.criteria ??
                this.defaultCriteria;
        }

        if (
            JSON.stringify(initialSelectedCriteriaIS) !==
                JSON.stringify(this.selectedCriteriaIS) ||
            JSON.stringify(initialSelectedCriteriaDS) !==
                JSON.stringify(this.selectedCriteriaDS)
        ) {
            this.hasChanged = true;
        }
    }

    saveChanges() {
        this.hasChanged = false;
        switch (this.type) {
            case "organization": {
                const organizationCriteria = { criteria: this.selectedCriteriaIS };
                this.outSaveOrganization.emit(organizationCriteria);
                break;
            }
            case "workspace": {
                const workspaceCriteria = {
                    organizationId: this.workspaceDetails?.organizationId,
                    name: this.workspaceDetails.workspaceName,
                    status: this.workspaceDetails.status,
                    dataRetentionDays: this.workspaceDetails?.dataRetentionDays,
                    criteriaIs: this.selectedCriteriaIS,
                    criteriaDs: this.selectedCriteriaDS,
                };
                this.outSaveWorkspace.emit(workspaceCriteria);
                break;
            }
            case "inventory": {
                const inventoryCriteria = {
                    id: this.inventory.id,
                    name: this.inventory.name,
                    criteria: this.selectedCriteriaIS,
                    note: this.inventory.note,
                    enableDataInconsistency: this.inventory.enableDataInconsistency,
                };
                this.outSaveInventory.emit(inventoryCriteria);
                break;
            }
            case "ds": {
                const dsCriteria = {
                    uid: this.ds.uid,
                    name: this.ds.name,
                    creator: this.ds.creator,
                    members: this.ds.members,
                    creationDate: this.ds.creationDate,
                    lastUpdateDate: this.ds.lastUpdateDate,
                    lastCalculationDate: this.ds.lastCalculationDate,
                    criteria: this.selectedCriteriaIS,
                    terminals: this.ds.terminals,
                    servers: this.ds.servers,
                    networks: this.ds.networks,
                    note: this.ds.note,
                    enableDataInconsistency: this.ds.enableDataInconsistency,
                    description: this.ds.description,
                    versionType: this.ds.versionType,
                };
                this.outSaveDS.emit(dsCriteria);
                break;
            }
            default:
                break;
        }
    }

    getHeader(): string {
        switch (this.type) {
            case "organization":
                return this.translate.instant("administration.user.choose-criteria", {
                    organization: this.organizationDetails.name,
                });

            case "workspace":
                return this.translate.instant(
                    "administration.organization.choose-criteria",
                    { workspaceName: this.workspaceDetails.workspaceName },
                );

            case "inventory":
                return this.translate.instant("inventories.choose-criteria", {
                    inventoryName: this.monthYear.transform(this.inventory.name),
                });

            default: // digital-services
                return this.translate.instant("digital-services.choose-criteria", {
                    digitalServiceName: this.ds.name,
                });
        }
    }

    onAllSelectedChange(selectedValue: string[], isIs: boolean): void {
        const allSelected = selectedValue.includes("All");
        if (isIs) {
            this.selectedCriteriaIS = allSelected ? this.criteriaList : [];
        } else {
            this.selectedCriteriaDS = allSelected ? this.criteriaList : [];
        }
        this.hasChanged = true;
    }
}
