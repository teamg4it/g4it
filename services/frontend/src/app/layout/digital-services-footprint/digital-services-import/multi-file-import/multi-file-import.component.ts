import { Component, EventEmitter, inject, Input, OnChanges, Output } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";

@Component({
    selector: "app-multi-file-import",
    templateUrl: "./multi-file-import.component.html",
})
export class MultiFileImportComponent implements OnChanges {
    private readonly loadingService = inject(LoadingDataService);
    private readonly translate = inject(TranslateService);
    private readonly route = inject(ActivatedRoute);
    @Output() formSubmit = new EventEmitter<string>();
    @Input() selectedMenuIndex: number | null = 0;
    @Input() form: any;
    @Input() importForm: any;

    allowedFileExtensions = [".csv", ".xlsx", ".ods"];
    selectedFiles: Record<string, File> = {};
    fileTypes: Array<{ key: string; label: string }> = [];
    fileLoading = false;

    ngOnChanges(): void {
        this.fileTypes = this.getFileTypes();
    }

    private getFileTypes(): Array<{ key: string; label: string }> {
        if (this.selectedMenuIndex === 0 || this.selectedMenuIndex === 1) {
            return [
                {
                    key: `EQUIPEMENT_PHYSIQUE_${this.selectedMenuIndex}`,
                    label: this.translate.instant(
                        "digital-services-import.physical-equipment",
                    ),
                },
            ];
        } else if (this.selectedMenuIndex === 2) {
            return [
                {
                    key: "DATACENTER",
                    label: this.translate.instant("digital-services-import.datacenter"),
                },
                {
                    key: `EQUIPEMENT_PHYSIQUE_${this.selectedMenuIndex}`,
                    label: this.translate.instant(
                        "digital-services-import.physical-equipment",
                    ),
                },
                {
                    key: "EQUIPEMENT_VIRTUEL_1",
                    label: this.translate.instant(
                        "digital-services-import.virtual-equipment",
                    ),
                },
            ];
        } else {
            return [
                {
                    key: "EQUIPEMENT_VIRTUEL_2",
                    label: this.translate.instant(
                        "digital-services-import.virtual-equipment",
                    ),
                },
            ];
        }
    }

    onSelectFile(event: any, key: string): void {
        const selectedFile = event?.files?.[0];
        if (selectedFile) {
            this.selectedFiles[key] = selectedFile;
            this.updateFormValidity();
        }
    }

    updateFormValidity(): void {
        const isUploadEnabled = this.isUploadEnabled();
        const formField = this.importForm.get(this.form?.[this.selectedMenuIndex!]?.name);
        formField?.setValue(isUploadEnabled ? "enabled" : "");
    }

    isUploadEnabled(): boolean {
        return this.fileTypes.some((type) => !!this.selectedFiles[type.key]);
    }

    uploadAllFiles(): void {
        if (this.fileLoading) return;

        this.fileLoading = true;
        const formData = this.createFormData();
        const dsId = this.route.snapshot.paramMap.get("digitalServiceId") ?? "";

        this.loadingService.launchLoadInputFiles(dsId, formData, true).subscribe({
            next: () => this.handleUploadSuccess(),
            error: () => (this.fileLoading = false),
        });
    }

    private createFormData(): FormData {
        const formData = new FormData();
        for (const [key, file] of Object.entries(this.selectedFiles).filter(([key]) =>
            this.fileTypes.some((type) => type.key === key),
        )) {
            let formKey = key;

            if (key.startsWith("EQUIPEMENT_VIRTUEL")) {
                formKey = "EQUIPEMENT_VIRTUEL";
            } else if (key.startsWith("EQUIPEMENT_PHYSIQUE")) {
                formKey = "EQUIPEMENT_PHYSIQUE";
            }

            formData.append(formKey, file, file.name);
        }

        return formData;
    }

    private handleUploadSuccess(): void {
        this.fileLoading = false;
        // delete selected files after successful upload
        for (const type of this.fileTypes) {
            delete this.selectedFiles[type.key];
        }
        this.updateFormValidity();
        this.formSubmit.emit("submit");
    }

    resetForm(): void {
        this.selectedFiles = {};
    }

    onDeleteButton(key: string): void {
        delete this.selectedFiles[key];
        this.updateFormValidity();
    }
}
