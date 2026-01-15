/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { TranslateModule } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { FileUpload, FileUploadModule } from "primeng/fileupload";
import { MessageService } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { ProgressBarModule } from "primeng/progressbar";
import { CardModule } from "primeng/card";
import { DropdownModule } from "primeng/dropdown";
import { FormsModule } from "@angular/forms";
import {
    CsvImportDataService,
    CsvImportEndpoint,
} from "src/app/core/service/data/api-route-referential.service";

@Component({
    selector: "app-update-reference",
    standalone: true,
    imports: [
        CommonModule,
        TranslateModule,
        ButtonModule,
        FileUploadModule,
        ToastModule,
        ProgressBarModule,
        CardModule,
        DropdownModule,
        FormsModule,
    ],
    providers: [MessageService],
    templateUrl: "./update-reference.component.html",
    styleUrls: ["./update-reference.component.scss"],
})
export class UpdateReferenceComponent implements OnInit {
    @ViewChild("fileUpload") fileUpload!: FileUpload;

    uploadedFile: File | null = null;
    isProcessing = false;
    maxFileSize = 100 * 1024 * 1024; // 100MB
    csvEndpoints: CsvImportEndpoint[] = [];
    selectedEndpoint: CsvImportEndpoint | null = null;

    // Propriétés pour les réponses de l'API
    lastUploadResponse: any = null;
    uploadErrors: string[] = [];
    importedLineNumber: number = 0;

    constructor(
        private readonly messageService: MessageService,
        private readonly csvImportService: CsvImportDataService,
    ) {}

    ngOnInit() {
        this.csvEndpoints = this.csvImportService.getCsvEndpoints();
    }

    clearFile() {
        this.uploadedFile = null;
        this.lastUploadResponse = null;
        this.uploadErrors = [];
        this.importedLineNumber = 0;

        // Réinitialiser le composant FileUpload
        if (this.fileUpload) {
            this.fileUpload.clear();
        }

        this.messageService.add({
            severity: "info",
            summary: "File Removed",
            detail: "File has been removed. You can upload a new file.",
        });
    }

    onUpload(event: any) {
        if (!this.selectedEndpoint) {
            this.messageService.add({
                severity: "warn",
                summary: "Warning",
                detail: "Please select an endpoint before uploading",
            });
            return;
        }

        const files = event.files;
        if (files.length === 0) {
            return;
        }

        // Prendre seulement le premier fichier
        const file = files[0];
        this.isProcessing = true;
        this.lastUploadResponse = null;
        this.uploadErrors = [];
        this.importedLineNumber = 0;

        this.csvImportService.uploadCsvFile(this.selectedEndpoint.name, file).subscribe({
            next: (response) => {
                this.uploadedFile = file;
                this.isProcessing = false;
                this.lastUploadResponse = response;

                // Traiter la réponse
                if (response?.errors?.length > 0) {
                    // Il y a des erreurs de validation
                    this.uploadErrors = response.errors;
                    this.importedLineNumber = response.importedLineNumber ?? 0;

                    this.messageService.add({
                        severity: "warn",
                        summary: "Validation Errors",
                        detail: `File uploaded but contains ${response.errors.length} validation error(s). ${response.importedLineNumber ?? 0} line(s) imported successfully.`,
                    });
                } else {
                    // Succès complet
                    this.importedLineNumber = response.importedLineNumber ?? 0;

                    this.messageService.add({
                        severity: "success",
                        summary: "Success",
                        detail: `File ${file.name} uploaded successfully. ${this.importedLineNumber} line(s) imported.`,
                    });
                }
            },
            error: (error) => {
                this.isProcessing = false;
                console.error("Upload error:", error);
                this.lastUploadResponse = null;
                this.uploadErrors = [];
                this.importedLineNumber = 0;

                // Extraire le message d'erreur de l'API
                let errorMessage = "Unknown error";
                if (error.error?.message) {
                    errorMessage = error.error.message;
                } else if (error.message) {
                    errorMessage = error.message;
                } else if (typeof error.error === "string") {
                    errorMessage = error.error;
                }

                this.messageService.add({
                    severity: "error",
                    summary: "Upload Failed",
                    detail: `Failed to upload file ${file.name}: ${errorMessage}`,
                });
            },
        });
    }

    onError(event: any) {
        this.messageService.add({
            severity: "error",
            summary: "Error",
            detail: "Error uploading files",
        });
    }

    onSelect(event: any) {
        // Vérifier la taille du fichier et le type
        const files = event.files;
        if (files.length === 0) {
            return;
        }

        // Prendre seulement le premier fichier
        const file = files[0];

        if (file.size > this.maxFileSize) {
            this.messageService.add({
                severity: "error",
                summary: "File too large",
                detail: `File ${file.name} exceeds maximum size of 100MB`,
            });
            return;
        }

        // Vérifier que c'est un fichier CSV
        if (!file.name.toLowerCase().endsWith(".csv") && file.type !== "text/csv") {
            this.messageService.add({
                severity: "error",
                summary: "Invalid file type",
                detail: `File ${file.name} is not a CSV file`,
            });
        }
    }

    onRemove(event: any) {
        // Supprimer le fichier uploadé
        if (this.uploadedFile && this.uploadedFile.name === event.file.name) {
            this.uploadedFile = null;
            // Réinitialiser l'upload
            this.lastUploadResponse = null;
            this.uploadErrors = [];
            this.importedLineNumber = 0;
            // Réinitialiser l'endpoint sélectionné
            this.csvEndpoints = [];
        }
    }
}
