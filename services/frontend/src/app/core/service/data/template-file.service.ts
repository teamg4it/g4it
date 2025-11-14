import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
import { MessageService } from "primeng/api";
import { firstValueFrom, Observable } from "rxjs";
import { Constants } from "src/constants";
import {
    FileDescription,
    TemplateFileDescription,
} from "../../interfaces/file-system.interfaces";
import { extractFileName } from "../../utils/path";

const endpoint = Constants.ENDPOINTS.templateFiles;

@Injectable({
    providedIn: "root",
})
export class TemplateFileService {
    private readonly translate = inject(TranslateService);
    private readonly messageService = inject(MessageService);
    constructor(private readonly http: HttpClient) {}

    getTemplateFiles(): Observable<FileDescription[]> {
        return this.http.get<FileDescription[]>(endpoint);
    }

    downloadTemplateFile(fileName: string): Observable<any> {
        if (fileName.includes(".xlsx")) {
            return this.http.get(`${endpoint}/${fileName}`, {
                responseType: "blob",
                headers: { Accept: "application/vnd.ms-excel" },
            });
        }
        if (fileName.includes(".zip")) {
            return this.http.get(`${endpoint}/${fileName}`, {
                responseType: "blob",
                headers: { Accept: "application/zip" },
            });
        }
        return this.http.get(`${endpoint}/${fileName}`, {
            responseType: "blob",
            headers: { Accept: "text/csv" },
        });
    }

    transformTemplateFiles(
        templateFiles: FileDescription[],
        isDs: boolean,
    ): TemplateFileDescription[] {
        let zipFile: TemplateFileDescription = {} as TemplateFileDescription;
        let xlsxFile: TemplateFileDescription = {} as TemplateFileDescription;
        const csvFiles: TemplateFileDescription[] = [];

        for (const res of templateFiles) {
            let templateFileDescription = { ...res } as TemplateFileDescription;
            templateFileDescription.name = extractFileName(templateFileDescription.name);

            if (res.name.includes("zip") && !isDs) {
                templateFileDescription.type = "zip";
                templateFileDescription.displayFileName = this.translate.instant(
                    "inventories.templates.all-template-files",
                    {
                        type: templateFileDescription.type,
                        size: this.toKB(res.metadata.size),
                    },
                );
                zipFile = templateFileDescription;
            }
            if (res.name.includes("xlsx")) {
                templateFileDescription.type = "xlsx";
                templateFileDescription.displayFileName = this.translate.instant(
                    "inventories.templates.data-model",
                    {
                        type: templateFileDescription.type,
                        size: this.toKB(res.metadata.size),
                    },
                );
                xlsxFile = templateFileDescription;
            }
            if (res.name.includes("csv")) {
                templateFileDescription.type = "csv";
                for (const csvFileType of Constants.FILE_TYPES) {
                    if (res.name.includes(csvFileType)) {
                        templateFileDescription.displayFileName = this.translate.instant(
                            `inventories.templates.${csvFileType}-template-file`,
                            {
                                type: templateFileDescription.type,
                                size: this.toKB(res.metadata.size),
                            },
                        );
                        templateFileDescription.csvFileType = csvFileType;
                    }
                }

                csvFiles.push(templateFileDescription);
            }
        }

        csvFiles.sort(
            (a, b) =>
                Constants.FILE_TYPES.indexOf(a.csvFileType ?? "") -
                Constants.FILE_TYPES.indexOf(b.csvFileType ?? ""),
        );
        return isDs ? [...csvFiles, xlsxFile] : [zipFile, ...csvFiles, xlsxFile];
    }

    toKB(bytes: string | undefined) {
        if (bytes === undefined) return 0;
        return (Number.parseInt(bytes) / 1024).toFixed(2);
    }

    async getdownloadTemplateFile(selectedFileName: string) {
        try {
            const blob: Blob = await firstValueFrom(
                this.downloadTemplateFile(selectedFileName),
            );
            saveAs(blob, selectedFileName);
        } catch (err) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.fileNoLongerAvailable"),
            });
        }
    }
}
