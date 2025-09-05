/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, inject, Input, OnInit } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Subject, takeUntil } from "rxjs";
import { Workspace } from "src/app/core/interfaces/user.interfaces";
import { FileSystemBusinessService } from "src/app/core/service/business/file-system.service";
import { UserService } from "src/app/core/service/business/user.service";
import { TaskDataService } from "src/app/core/service/data/task-data.service";
import { Constants } from "src/constants";

@Component({
    selector: "app-batch-status",
    templateUrl: "./batch-status.component.html",
})
export class BatchStatusComponent implements OnInit {
    private readonly fileSystemBusinessService = inject(FileSystemBusinessService);
    @Input() batchStatusCode: string = "";
    @Input() type: string = "loading";
    cssClass: string = "";
    toolTip: string = "";
    betweenDiv: string = "";
    localCreateTime: Date | undefined;
    @Input() createTime: Date | undefined;
    @Input() batchLoading = false;
    @Input() inventoryId: number = 0;
    @Input() inventoryName = "";
    @Input() taskId = "";
    @Input() fileUrl = "";

    selectedWorkspace!: string;
    selectedOrganization!: string;
    ngUnsubscribe = new Subject<void>();

    constructor(
        private readonly messageService: MessageService,
        private readonly translate: TranslateService,
        protected userService: UserService,
        private readonly taskDataService: TaskDataService,
    ) {}

    ngOnInit(): void {
        this.userService.currentOrganization$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((organization) => {
                this.selectedOrganization = organization.name;
            });
        this.userService.currentWorkspace$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((workspace: Workspace) => {
                this.selectedWorkspace = workspace.name;
            });

        const defaultClasses =
            "text-white text-lg border-circle p-1-5 w-2rem h-2rem text-center";

        if (Constants.EVALUATION_BATCH_RUNNING_STATUSES.includes(this.batchStatusCode)) {
            this.cssClass = "pi pi-spin pi-spinner icon-running";
            this.toolTip = this.translate.instant("common.running");
        } else if (this.batchStatusCode === "COMPLETED") {
            this.cssClass = `pi pi-check bg-tertiary ${defaultClasses}`;
            this.toolTip = this.translate.instant("common.completed");
        } else if (this.batchStatusCode === "FAILED") {
            this.cssClass = `pi pi-times bg-dark-red ${defaultClasses}`;
            this.toolTip = this.translate.instant("common.failed");
        } else if (this.batchStatusCode === "FAILED_HEADERS") {
            this.cssClass = `pi pi-times bg-dark-red ${defaultClasses}`;
            this.toolTip = this.translate.instant("common.failed-headers");
        } else if (
            this.batchStatusCode === "COMPLETED_WITH_ERRORS" ||
            this.batchStatusCode === "SKIPPED"
        ) {
            this.cssClass = `bg-warning ${defaultClasses}`;
            this.toolTip = this.translate.instant("common.completed-with-errors");
            this.betweenDiv = "!";
        } else {
            this.cssClass = `pi pi-hourglass bg-warning ${defaultClasses}`;
            this.toolTip = this.translate.instant("common.pending");
        }

        if (this.createTime) {
            this.localCreateTime = new Date(this.createTime.toString() + "Z");
        }
    }

    downloadFile() {
        this.fileSystemBusinessService.downloadFile(
            this.taskId,
            this.selectedOrganization,
            this.selectedWorkspace,
            this.inventoryId,
        );
    }

    getTaskDetail(taskId: string) {
        this.fileSystemBusinessService.getTaskDetail(taskId);
    }

    isNumeric(value: string) {
        return /^\d+$/.test(value);
    }
}
