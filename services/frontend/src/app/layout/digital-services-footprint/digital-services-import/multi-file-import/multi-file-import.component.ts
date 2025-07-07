import { Component, EventEmitter, inject, Input, Output } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";

@Component({
    selector: "app-multi-file-import",
    templateUrl: "./multi-file-import.component.html",
    styleUrl: "./multi-file-import.component.scss",
})
export class MultiFileImportComponent {
    private readonly loadingService = inject(LoadingDataService);
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
        this.fileTypes =
            this.selectedMenuIndex === 0
                ? [
                      { key: "DATACENTER", label: "Datacenter" },
                      { key: "EQUIPEMENT_PHYSIQUE", label: "Physical Equipment" },
                      { key: "EQUIPEMENT_VIRTUEL_1", label: "Virtual Equipment" },
                  ]
                : [{ key: "EQUIPEMENT_VIRTUEL_2", label: "Virtual Equipment" }];
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
        Object.entries(this.selectedFiles)
            .filter(([key]) => this.fileTypes.some((type) => type.key === key))
            .forEach(([key, file]) => {
                const formKey = key.includes("EQUIPEMENT_VIRTUEL")
                    ? "EQUIPEMENT_VIRTUEL"
                    : key;
                formData.append(formKey, file, file.name);
            });
        return formData;
    }

    private handleUploadSuccess(): void {
        this.fileLoading = false;
        this.resetForm();
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
