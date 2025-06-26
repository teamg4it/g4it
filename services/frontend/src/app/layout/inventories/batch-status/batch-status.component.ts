/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, OnInit } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
import { MessageService } from "primeng/api";
import { Subject, firstValueFrom, takeUntil } from "rxjs";
import { TaskRest } from "src/app/core/interfaces/inventory.interfaces";
import { Organization, Subscriber } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { FileSystemDataService } from "src/app/core/service/data/file-system-data.service";
import { TaskDataService } from "src/app/core/service/data/task-data.service";
import { Constants } from "src/constants";

@Component({
    selector: "app-batch-status",
    templateUrl: "./batch-status.component.html",
})
export class BatchStatusComponent implements OnInit {
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

    selectedOrganization!: string;
    selectedSubscriber!: string;
    ngUnsubscribe = new Subject<void>();

    constructor(
        private fileSystemDataService: FileSystemDataService,
        private messageService: MessageService,
        private translate: TranslateService,
        protected userService: UserService,
        private taskDataService: TaskDataService,
    ) {}

    ngOnInit(): void {
        this.userService.currentSubscriber$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((subscriber: Subscriber) => {
                this.selectedSubscriber = subscriber.name;
            });
        this.userService.currentOrganization$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((organization: Organization) => {
                this.selectedOrganization = organization.name;
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

    async downloadFile() {
        try {
            const blob: Blob = await firstValueFrom(
                this.fileSystemDataService.downloadResultsFile(
                    this.inventoryId,
                    this.taskId,
                ),
            );
            saveAs(
                blob,
                `g4it_${this.selectedSubscriber}_${this.selectedOrganization}_${this.inventoryId}_rejected-files.zip`,
            );
        } catch (err) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.fileNoLongerAvailable"),
            });
        }
    }

    async getTaskDetail(taskId: string) {
        const taskRest: TaskRest = await firstValueFrom(
            this.taskDataService.getTask(this.inventoryId, +taskId),
        );
        this.messageService.add({
            severity: "error",
            summary: this.translate.instant("errors.error-occurred"),
            detail: taskRest.details.join("\n"),
        });
    }

    isNumeric(value: string) {
        return /^\d+$/.test(value);
    }
}
