/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, inject, Input, OnInit, Output } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import {
    OrganizationCriteriaRest,
    WorkspaceCriteriaRest,
} from "src/app/core/interfaces/administration.interfaces";
import {
    Inventory,
    InventoryCriteriaRest,
    TaskRest,
} from "src/app/core/interfaces/inventory.interfaces";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { EvaluationDataService } from "src/app/core/service/data/evaluation-data.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import * as TimeUtils from "src/app/core/utils/time";
import { Constants } from "src/constants";

@Component({
    selector: "app-inventory-item",
    templateUrl: "./inventory-item.component.html",
    providers: [ConfirmationService, MessageService],
})
export class InventoryItemComponent implements OnInit {
    private readonly global = inject(GlobalStoreService);

    @Input() inventory: Inventory = {} as Inventory;
    @Input() open: boolean = false;
    @Output() reloadInventoriesAndLoop: EventEmitter<number> = new EventEmitter();
    @Output() reloadInventoryAndLoop: EventEmitter<number> = new EventEmitter();
    @Output() openSidebarForUploadInventory: EventEmitter<number> = new EventEmitter();
    @Output() openSidebarForNote: EventEmitter<number> = new EventEmitter();
    @Output() openTab: EventEmitter<number> = new EventEmitter();
    @Output() closeTab: EventEmitter<number> = new EventEmitter();
    @Output() saveInventory = new EventEmitter<InventoryCriteriaRest>();

    batchStatusMapping: any = Constants.EVALUATION_BATCH_STATUS_MAPPING;
    displayPopup = false;
    selectedCriteria: string[] = [];
    organization: OrganizationCriteriaRest = { criteria: [] };
    workspace: WorkspaceCriteriaRest = {
        organizationId: 0,
        name: "",
        status: "",
        dataRetentionDays: 0,
        criteriaIs: [],
        criteriaDs: [],
    };

    taskLoading: TaskRest[] = [];
    taskEvaluating: TaskRest[] = [];

    constructor(
        private readonly inventoryService: InventoryService,
        private readonly evaluationService: EvaluationDataService,
        private readonly footprintService: FootprintDataService,
        public router: Router,
        private readonly confirmationService: ConfirmationService,
        private readonly translate: TranslateService,
        private readonly route: ActivatedRoute,
        public userService: UserService,
    ) {}

    ngOnInit() {
        this.userService.currentOrganization$.subscribe((organization) => {
            this.organization.criteria = organization.criteria!;
        });
        this.userService.currentWorkspace$.subscribe((workspace) => {
            this.workspace.organizationId = workspace.organizationId!;
            this.workspace.name = workspace.name;
            this.workspace.status = workspace.status;
            this.workspace.dataRetentionDays = workspace.dataRetentionDays!;
            this.workspace.criteriaIs = workspace.criteriaIs!;
            this.workspace.criteriaDs = workspace.criteriaDs!;
        });

        if (this.inventory.tasks) {
            this.taskLoading = this.inventory.tasks.filter((t) => t.type === "LOADING");
            this.taskEvaluating = this.inventory.tasks.filter(
                (t) => t.type === "EVALUATING",
            );
        }
    }

    isTaskRunning() {
        if (!this.inventory.lastTaskEvaluating) return false;
        return Constants.EVALUATION_BATCH_RUNNING_STATUSES.includes(
            this.inventory.lastTaskEvaluating.status,
        );
    }

    showEquipment = () => {
        return (
            this.inventory.lastTaskEvaluating &&
            (this.inventory.physicalEquipmentCount > 0 ||
                this.inventory.virtualEquipmentCount > 0)
        );
    };

    showApplication = () => {
        return this.inventory.lastTaskEvaluating && this.inventory.applicationCount > 0;
    };

    confirmDelete(event: Event) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: `${this.translate.instant("inventories.popup.delete-question")} ${
                this.inventory.name
            } ?
            ${this.translate.instant("inventories.popup.delete-text")}`,
            icon: "pi pi-exclamation-triangle",
            accept: async () => {
                this.global.setLoading(true);
                await lastValueFrom(
                    this.footprintService.deleteIndicators(this.inventory.id),
                );
                await lastValueFrom(
                    this.inventoryService.deleteInventory(this.inventory.id),
                );
                this.reloadInventoriesAndLoop.emit();
                this.global.setLoading(false);
            },
        });
    }

    redirectFootprint(redirectTo: string): void {
        if (!this.inventory.lastTaskEvaluating) return;

        const defaultCriteria = Object.keys(this.global?.criteriaList())?.slice(0, 5);
        const criteria = this.inventory.lastTaskEvaluating?.criteria ?? defaultCriteria;
        const isSingleCriteria = criteria.length === 1;
        const criteriaUri = isSingleCriteria ? criteria[0] : Constants.MUTLI_CRITERIA;

        let uri: string | undefined;

        switch (redirectTo) {
            case "equipment":
                if (
                    this.inventory.physicalEquipmentCount > 0 ||
                    this.inventory.virtualEquipmentCount > 0
                ) {
                    uri = criteriaUri;
                }
                break;
            case "application":
                if (this.inventory.applicationCount > 0) {
                    uri = `application/${criteriaUri}`;
                }
                break;
        }

        if (uri === undefined) return;

        this.router.navigate([`${this.inventory.id}/footprint/${uri}`], {
            relativeTo: this.route,
        });
    }

    launchEstimate(event: Event) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: this.translate.instant("inventories.popup.estimate"),
            icon: "pi pi-exclamation-triangle",
            accept: async () => {
                await lastValueFrom(
                    this.evaluationService.launchEvaluating(this.inventory.id),
                );

                await TimeUtils.delay(500);
                this.reloadInventoryAndLoop.emit(this.inventory.id);
            },
        });
    }

    isEstimationDisabled() {
        // If there is no physical equipement and no virtual equipment, disable button
        if (
            this.inventory.physicalEquipmentCount <= 0 &&
            this.inventory.virtualEquipmentCount <= 0
        )
            return true;

        // If there is already an loading running
        if (this.inventory.lastTaskLoading) {
            if (
                Constants.EVALUATION_BATCH_RUNNING_STATUSES.includes(
                    this.inventory.lastTaskLoading?.status,
                )
            )
                return true;
        }

        // If there is already an evaluation running
        if (this.inventory.lastTaskEvaluating) {
            if (
                Constants.EVALUATION_BATCH_RUNNING_STATUSES.includes(
                    this.inventory.lastTaskEvaluating?.status,
                )
            )
                return true;
        }

        // Else enable button
        return false;
    }

    openSidebarUploadFile() {
        this.openSidebarForUploadInventory.emit(this.inventory.id);
    }

    openSidebarNote() {
        this.openSidebarForNote.emit(this.inventory.id);
    }

    async onSelectedChange(id: number, event: any) {
        if (event === undefined) return;
        if (event === true) {
            this.openTab.emit(id);
        } else {
            this.closeTab.emit(id);
        }
    }

    trackByFn(index: any) {
        return index;
    }

    displayPopupFct() {
        const defaultCriteria = Object.keys(this.global.criteriaList()).slice(0, 5);
        this.selectedCriteria =
            this.inventory.criteria ??
            this.workspace?.criteriaIs ??
            this.organization?.criteria ??
            defaultCriteria;
        this.displayPopup = true;
    }
}
