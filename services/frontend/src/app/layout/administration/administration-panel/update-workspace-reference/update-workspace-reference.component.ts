import { CommonModule } from "@angular/common";
import { Component, inject, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { TranslateModule } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { FileUploadModule } from "primeng/fileupload";
import { ProgressBarModule } from "primeng/progressbar";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { WorkspaceWithOrganization } from "src/app/core/interfaces/administration.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
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
    workspace: WorkspaceWithOrganization = {} as WorkspaceWithOrganization;
    workspacelist: WorkspaceWithOrganization[] = [];
    private readonly administrationService = inject(AdministrationService);
    private readonly userService = inject(UserService);

    ngOnInit() {
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
}
