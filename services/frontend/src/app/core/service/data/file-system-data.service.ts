/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import saveAs from "file-saver";
import { MessageService } from "primeng/api";
import { firstValueFrom, Observable } from "rxjs";
import { Constants } from "src/constants";
import { TaskRest } from "../../interfaces/inventory.interfaces";
import { TaskDataService } from "./task-data.service";

const endpoint = Constants.ENDPOINTS.downloadReject;

@Injectable({
    providedIn: "root",
})
export class FileSystemDataService {
    private readonly messageService = inject(MessageService);
    private readonly translate = inject(TranslateService);
    private readonly taskDataService = inject(TaskDataService);
    private readonly http = inject(HttpClient);

    downloadResultsFile(taskId: string): Observable<any> {
        return this.http.get(`${endpoint}/${taskId}`, {
            responseType: "blob",
            headers: { Accept: "application/zip" },
        });
    }

    async downloadFile(
        taskId: string,
        selectedSubscriber: string,
        selectedOrganization: string,
        moduleId: number | string,
    ): Promise<void> {
        try {
            const blob: Blob = await firstValueFrom(this.downloadResultsFile(taskId));
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
