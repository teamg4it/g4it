import { inject, Injectable } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import saveAs from "file-saver";
import { MessageService } from "primeng/api";
import { firstValueFrom } from "rxjs";
import { TaskRest } from "../../interfaces/inventory.interfaces";
import { FileSystemDataService } from "../data/file-system-data.service";
import { TaskDataService } from "../data/task-data.service";

@Injectable({
    providedIn: "root",
})
export class FileSystemBusinessService {
    private readonly fileSystemData = inject(FileSystemDataService);
    private readonly messageService = inject(MessageService);
    private readonly translate = inject(TranslateService);
    private readonly taskDataService = inject(TaskDataService);
    async downloadFile(
        taskId: string,
        selectedSubscriber: string,
        selectedOrganization: string,
        moduleId: number | string,
    ): Promise<void> {
        try {
            const blob: Blob = await firstValueFrom(
                this.fileSystemData.downloadResultsFile(taskId),
            );
            saveAs(
                blob,
                `g4it_${selectedSubscriber}_${selectedOrganization}_${moduleId}_rejected-files.zip`,
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
            this.taskDataService.getTask(+taskId),
        );
        this.messageService.add({
            severity: "error",
            summary: this.translate.instant("errors.error-occurred"),
            detail: taskRest.details.join("\n"),
        });
    }
}
