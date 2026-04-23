import { CommonModule } from "@angular/common";
import { Component, DestroyRef, inject, OnInit, ViewChild } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { FormsModule } from "@angular/forms";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
import { MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { FileUpload, FileUploadModule } from "primeng/fileupload";
import { ProgressBarModule } from "primeng/progressbar";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { finalize } from "rxjs";
import { WorkspaceWithOrganization } from "src/app/core/interfaces/administration.interfaces";
import {
    FileDescription,
    TemplateFileDescription,
} from "src/app/core/interfaces/file-system.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { CsvImportEndpoint } from "src/app/core/service/data/api-route-referential.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { WorkspaceReferenceDataService } from "src/app/core/service/data/workspace-reference-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { Constants } from "src/constants";

@Component({
    selector: "app-update-workspace-reference",
    standalone: true,
    imports: [
        CommonModule,
        ButtonModule,
        FileUploadModule,
        ProgressBarModule,
        TranslateModule,
        DropdownModule,
        FormsModule,
        ScrollPanelModule,
        SharedModule,
    ],
    templateUrl: "./update-workspace-reference.component.html",
})
export class UpdateWorkspaceReferenceComponent implements OnInit {
    private readonly administrationService = inject(AdministrationService);
    private readonly userService = inject(UserService);
    private readonly workspaceReferenceDataService = inject(
        WorkspaceReferenceDataService,
    );
    private readonly translate = inject(TranslateService);
    private readonly messageService = inject(MessageService);
    protected readonly templateFileService = inject(TemplateFileService);
    private readonly destroyRef = inject(DestroyRef);
    @ViewChild("fileUpload") fileUpload!: FileUpload;
    workspace: WorkspaceWithOrganization = {} as WorkspaceWithOrganization;
    workspacelist: WorkspaceWithOrganization[] = [];
    selectedEndpoint: CsvImportEndpoint | null = null;
    csvEndpoints: CsvImportEndpoint[] = [];
    fileUploadText: string = this.translate.instant("common.choose-file");
    maxFileSize = 100 * 1024 * 1024; // 100MB
    file: any = null;
    dataModel: TemplateFileDescription | undefined;
    loadingResults: any[] = [];
    downloadInProgress = false;
    uploadInProgress = false;

    ngOnInit() {
        this.csvEndpoints = this.workspaceReferenceDataService.getWorkspaceCsvEndpoints();
        this.getTemplates();
        this.administrationService
            .getUsers()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res) => {
                const organizationsDetails: any = res;

                const list: WorkspaceWithOrganization[] = [];
                for (const organization of organizationsDetails) {
                    for (const workspace of organization.workspaces) {
                        const roles = this.userService.getRoles(organization, workspace);
                        if (
                            workspace.status === Constants.WORKSPACE_STATUSES.ACTIVE &&
                            (roles.includes(Role.OrganizationAdmin) ||
                                roles.includes(Role.WorkspaceAdmin))
                        ) {
                            list.push({
                                organizationName: organization.name,
                                organizationId: organization.id,
                                workspaceName: workspace.name,
                                workspaceId: workspace.id,
                                status: workspace.status,
                                dataRetentionDays: workspace.dataRetentionDays!,
                                displayLabel: `${workspace.name} - (${organization.name})`,
                                criteriaDs: workspace.criteriaDs!,
                                criteriaIs: workspace.criteriaIs!,
                                authorizedDomains: organization.authorizedDomains,
                            });
                        }
                    }
                }

                this.workspacelist = list;
            });
    }

    onSelect(event: any) {
        // Check file size and type
        const files = event.files;
        if (files.length === 0) {
            return;
        }

        // Take only the first file
        const file = files[0];
        this.file = file;

        if (this.file.size > this.maxFileSize) {
            this.messageService.add({
                severity: "error",
                summary: "File too large",
                detail: `File ${this.file?.name} exceeds maximum size of 100MB`,
            });
            return;
        }

        if (
            !this.file?.name.toLowerCase().endsWith(".csv") &&
            this.file.type !== "text/csv"
        ) {
            this.messageService.add({
                severity: "error",
                summary: "Invalid file type",
                detail: `File ${this.file?.name} is not a CSV file`,
            });
        }
    }

    onDeleteButton() {
        this.file = null;
        this.fileUpload.clear();
    }

    getTemplates() {
        this.templateFileService
            .getTemplateFiles()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((templateFiles: FileDescription[]) => {
                const transformedTemplateFiles =
                    this.templateFileService.transformTemplateFiles(templateFiles, true);
                transformedTemplateFiles[0].displayFileName = this.translate.instant(
                    "digital-services-import.templates.data-model",
                );
                this.dataModel = transformedTemplateFiles.find((file) =>
                    file.name?.toLowerCase()?.includes("datamodel"),
                );
                if (this.dataModel?.displayFileName) {
                    this.dataModel.displayFileName = this.translate.instant(
                        "digital-services-import.templates.data-model",
                    );
                }
            });
    }

    downloadTemplateFile(selectedFileName: string) {
        this.templateFileService.getdownloadTemplateFile(selectedFileName);
    }

    startUpload() {
        this.uploadInProgress = true;
        this.loadingResults.push({
            status: "IN_PROGRESS",
            creationDate: new Date(),
        });
        this.workspaceReferenceDataService
            .workspaceUploadCsvFile(
                this.selectedEndpoint?.name ?? "",
                this.file,
                this.workspace.workspaceId,
                this.workspace.organizationName,
            )
            .pipe(
                takeUntilDestroyed(this.destroyRef),
                finalize(() => (this.uploadInProgress = false)),
            )
            .subscribe({
                next: (response) => {
                    this.loadingResults = [];
                    if (response?.errors?.length > 0) {
                        this.loadingResults.push({
                            status: "FAILED",
                            creationDate: new Date(),
                            cssClass: " red-tag status-tag",
                            tooltip: this.translate.instant("common.failed"),
                        });
                    } else {
                        this.loadingResults.push({
                            status: "COMPLETED",
                            creationDate: new Date(),
                            cssClass: "green-tag status-tag",
                            tooltip: this.translate.instant("common.completed"),
                        });
                    }
                },
                error: (error) => {
                    this.loadingResults = [];
                    this.loadingResults.push({
                        status: "FAILED",
                        creationDate: new Date(),
                        cssClass: "red-tag status-tag",
                        tooltip: this.translate.instant("common.failed"),
                    });
                },
            });
    }

    downloadWorkspaceReferential() {
        this.downloadInProgress = true;
        this.workspaceReferenceDataService
            .workspaceDownloadZipFile(
                this.workspace.workspaceId,
                this.workspace.organizationName,
            )
            .pipe(
                takeUntilDestroyed(this.destroyRef),
                finalize(() => (this.downloadInProgress = false)),
            )
            .subscribe((blob) =>
                saveAs(
                    blob,
                    this.workspaceReferenceDataService.getZipFileName(
                        this.workspace.workspaceName,
                    ),
                ),
            );
    }
}
