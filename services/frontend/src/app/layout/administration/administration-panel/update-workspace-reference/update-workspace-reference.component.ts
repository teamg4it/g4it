import { CommonModule } from "@angular/common";
import { Component, inject, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { FileUploadModule } from "primeng/fileupload";
import { ProgressBarModule } from "primeng/progressbar";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { WorkspaceWithOrganization } from "src/app/core/interfaces/administration.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { CsvImportEndpoint } from "src/app/core/service/data/api-route-referential.service";
import { WorkspaceReferenceDataService } from "src/app/core/service/data/workspace-reference-data.service";
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
    workspace: WorkspaceWithOrganization = {} as WorkspaceWithOrganization;
    workspacelist: WorkspaceWithOrganization[] = [];
    selectedEndpoint: CsvImportEndpoint | null = null;
    csvEndpoints: CsvImportEndpoint[] = [];
    fileUploadText: string = this.translate.instant("common.choose-file");
    maxFileSize = 100 * 1024 * 1024; // 100MB
    file: any = null;

    ngOnInit() {
        this.csvEndpoints = this.workspaceReferenceDataService.getWorkspaceCsvEndpoints();
        this.administrationService.getUsers().subscribe((res) => {
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
    }
}
