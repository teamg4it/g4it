import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
} from "@angular/core";
import {
    OrganizationCriteriaRest,
    OrganizationWithSubscriber,
    SubscriberCriteriaRest,
} from "src/app/core/interfaces/administration.interfaces";
import { DSCriteriaRest } from "src/app/core/interfaces/digital-service.interfaces";
import { InventoryCriteriaRest } from "src/app/core/interfaces/inventory.interfaces";
import { Subscriber } from "src/app/core/interfaces/user.interfaces";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-criteria-popup",
    templateUrl: "./criteria-popup.component.html",
})
export class CriteriaPopupComponent implements OnChanges {
    @Input() displayPopup: boolean = false;
    @Input() type: "subscriber" | "organization" | "inventory" | "ds" | undefined;
    @Input() selectedCriteriaIS: string[] = [];
    @Input() selectedCriteriaDS: string[] = [];
    //this subscriber contains the subscriber list
    @Input() subscriber!: SubscriberCriteriaRest;
    @Input() organization!: OrganizationCriteriaRest;
    //this subscriber contains all the subscriber details
    @Input() subscriberDetails!: Subscriber;
    @Input() organizationDetails!: OrganizationWithSubscriber;
    @Input() inventory: any;
    @Input() ds: any;

    @Output() onSaveOrganization = new EventEmitter<OrganizationCriteriaRest>();
    @Output() onSaveSubscriber = new EventEmitter<SubscriberCriteriaRest>();
    @Output() onSaveInventory = new EventEmitter<InventoryCriteriaRest>();
    @Output() onSaveDs = new EventEmitter<DSCriteriaRest>();
    @Output() onClose = new EventEmitter<void>();

    constructor(private readonly globalStore: GlobalStoreService) {}

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
        if (changes["selectedCriteriaIS"] && this.type === "organization") {
            this.tempSelectedCriteriaIS = [...this.selectedCriteriaIS];
        }
        if (changes["selectedCriteriaDS"] && this.type === "organization") {
            this.tempSelectedCriteriaDS = [...this.selectedCriteriaDS];
        }
        if (changes["selectedCriteriaIS"] && this.type !== "organization") {
            this.tempSelectedCriteriaIS = [...this.selectedCriteriaIS];
        }
        if (changes["organizationDetails"]) {
            this.setCriteriaList();
        }
        if (changes["subscriber"]) {
            this.setCriteriaList();
        }
    }

    onCriteriaChange() {
        this.hasChanged = true;
    }

    closePopup() {
        this.selectedCriteriaIS = [...this.tempSelectedCriteriaIS];
        this.selectedCriteriaDS = [...this.tempSelectedCriteriaDS];
        this.onClose.emit();
    }

    setCriteriaList() {
        if (this.type === "organization") {
            this.selectedCriteriaIS =
                this.organizationDetails.criteriaIs ??
                this.subscriberDetails?.criteria ??
                this.defaultCriteria;
            this.selectedCriteriaDS =
                this.organizationDetails.criteriaIs ??
                this.subscriberDetails?.criteria ??
                this.defaultCriteria;
        }
        if (this.type === "subscriber") {
            this.selectedCriteriaIS =
                this.subscriberDetails?.criteria ?? this.defaultCriteria;
            this.selectedCriteriaDS =
                this.subscriberDetails?.criteria ?? this.defaultCriteria;
        }
    }

    resetToDefault() {
        let initialSelectedCriteriaIS = [...this.selectedCriteriaIS];
        let initialSelectedCriteriaDS = [...this.selectedCriteriaDS];

        if (this.type === "subscriber") {
            this.selectedCriteriaIS = this.defaultCriteria;
        } else if (this.type === "organization") {
            this.selectedCriteriaIS =
                this.subscriberDetails?.criteria ?? this.defaultCriteria;
            this.selectedCriteriaDS =
                this.subscriberDetails?.criteria ?? this.defaultCriteria;
        } else if (this.type === "inventory") {
            this.selectedCriteriaIS =
                this.organization?.criteriaIs ??
                this.subscriber?.criteria ??
                this.defaultCriteria;
        } else if (this.type === "ds") {
            this.selectedCriteriaIS =
                this.organizationDetails?.criteriaDs ??
                this.subscriberDetails?.criteria ??
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
            case "subscriber":
                const subscriberCriteria = { criteria: this.selectedCriteriaIS };
                this.onSaveSubscriber.emit(subscriberCriteria);
                break;
            case "organization":
                const organizationCriteria = {
                    subscriberId: this.organizationDetails?.subscriberId,
                    name: this.organizationDetails.organizationName,
                    status: this.organizationDetails.status,
                    dataRetentionDays: this.organizationDetails?.dataRetentionDays,
                    criteriaIs: this.selectedCriteriaIS,
                    criteriaDs: this.selectedCriteriaDS,
                };
                this.onSaveOrganization.emit(organizationCriteria);
                break;
            case "inventory":
                const inventoryCriteria = {
                    id: this.inventory.id,
                    name: this.inventory.name,
                    criteria: this.selectedCriteriaIS,
                    note: this.inventory.note,
                };
                this.onSaveInventory.emit(inventoryCriteria);
                break;
            case "ds":
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
                };
                this.onSaveDs.emit(dsCriteria);
                break;
            default:
                break;
        }
    }

    onAllSelectedChange(selectedValue: string[], isIs: boolean): void {
        if (selectedValue.includes("All")) {
            if (isIs) {
                this.selectedCriteriaIS = this.criteriaList;
            } else {
                this.selectedCriteriaDS = this.criteriaList;
            }
        } else {
            if (isIs) {
                this.selectedCriteriaIS = [];
            } else {
                this.selectedCriteriaDS = [];
            }
        }
        this.hasChanged = true;
    }
}
