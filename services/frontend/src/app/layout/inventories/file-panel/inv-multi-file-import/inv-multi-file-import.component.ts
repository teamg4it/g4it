/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, inject, Input, OnChanges, SimpleChanges } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import { Button } from "primeng/button";
import { FileUploadModule } from "primeng/fileupload";
import { SelectModule } from "primeng/select";

export interface FileTypeOption {
    key: string;
    label: string;
}

export interface FileRow {
    id: number;
    type: FileTypeOption;
    file?: File;
}

@Component({
    selector: "app-inv-multi-file-import",
    templateUrl: "./inv-multi-file-import.component.html",
    standalone: true,
    imports: [SelectModule, FormsModule, FileUploadModule, Button, TranslatePipe],
})
export class InvMultiFileImportComponent implements OnChanges {
    private readonly translate = inject(TranslateService);

    @Input() selectedMenuIndex: number | null = 0;
    @Input() form: any;
    @Input() importForm: any;
    @Input() inventoryId?: number;

    allowedFileExtensions = [".csv", ".xlsx", ".ods"];
    fileTypes: FileTypeOption[] = [];
    rows: FileRow[] = [];
    private nextRowId = 0;
    // preserves selected rows/files per tab when switching between menu tabs
    private readonly rowsByMenuIndex = new Map<number, FileRow[]>();

    ngOnChanges(changes?: SimpleChanges): void {
        if (changes && !changes["selectedMenuIndex"] && this.fileTypes.length) return;

        this.fileTypes = this.getFileTypes();
        const index = this.selectedMenuIndex ?? -1;
        let rows = this.rowsByMenuIndex.get(index);
        if (!rows) {
            rows = this.fileTypes.map((type) => this.createRow(type));
        }
        this.setRows(rows);
    }

    private setRows(rows: FileRow[]): void {
        this.rows = rows;
        this.rowsByMenuIndex.set(this.selectedMenuIndex ?? -1, rows);
    }

    private createRow(type: FileTypeOption): FileRow {
        return { id: this.nextRowId++, type };
    }

    private getFileTypes(): FileTypeOption[] {
        switch (this.selectedMenuIndex) {
            case 1: // Infrastructure
                return [
                    {
                        key: "DATACENTER",
                        label: this.translate.instant(
                            "digital-services-import.datacenter",
                        ),
                    },
                    {
                        key: "EQUIPEMENT_PHYSIQUE",
                        label: this.translate.instant(
                            "digital-services-import.physical-equipment",
                        ),
                    },
                ];
            case 2: // End-user devices
                return [
                    {
                        key: "EQUIPEMENT_PHYSIQUE",
                        label: this.translate.instant(
                            "digital-services-import.physical-equipment",
                        ),
                    },
                ];
            case 3: // Digital services
                return [
                    {
                        key: "EQUIPEMENT_VIRTUEL",
                        label: this.translate.instant(
                            "digital-services-import.virtual-equipment",
                        ),
                    },
                    {
                        key: "APPLICATION",
                        label: this.translate.instant(
                            "digital-services-import.application",
                        ),
                    },
                ];
            case 4: // External services
                return [
                    {
                        key: "EQUIPEMENT_VIRTUEL",
                        label: this.translate.instant(
                            "digital-services-import.virtual-equipment",
                        ),
                    },
                    {
                        key: "AI_SERVICES",
                        label: this.translate.instant(
                            "digital-services-import.ai-services",
                        ),
                    },
                ];
            default:
                return [];
        }
    }

    addRow(): void {
        if (this.fileTypes.length === 0) return;
        this.rows.push(this.createRow(this.fileTypes[0]));
        this.setRows(this.rows);
    }

    onSelectFile(event: any, row: FileRow): void {
        const selectedFile = event?.files?.[0];
        if (selectedFile) {
            row.file = selectedFile;
            this.updateFormValidity();
        }
    }

    onDeleteRow(row: FileRow): void {
        this.setRows(this.rows.filter((r) => r.id !== row.id));
        this.updateFormValidity();
    }

    updateFormValidity(): void {
        const isUploadEnabled = this.isUploadEnabled();
        const formField = this.importForm.get(this.form?.[this.selectedMenuIndex!]?.name);
        formField?.setValue(isUploadEnabled ? "enabled" : "");
    }

    isUploadEnabled(): boolean {
        return this.rows.some((row) => !!row.file);
    }

    // aggregates every file selected across all visited tabs, keyed by file type
    getFormData(): FormData {
        const formData = new FormData();
        for (const rows of this.rowsByMenuIndex.values()) {
            for (const row of rows) {
                if (row.file) {
                    formData.append(row.type.key, row.file, row.file.name);
                }
            }
        }

        return formData;
    }

    resetForm(): void {
        this.rowsByMenuIndex.clear();
        this.setRows(this.fileTypes.map((type) => this.createRow(type)));
    }
}
