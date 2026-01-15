import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { SharedModule } from "src/app/core/shared/shared.module";
import { AdministrationPanelComponent } from "./administration-panel/administration-panel.component";
import { OrganizationsComponent } from "./administration-panel/organizations/organizations.component";
import { SuperAdminComponent } from "./administration-panel/super-admin/super-admin.component";
import { AddWorkspaceComponent } from "./administration-panel/users/add-workspace/add-workspace.component";
import { UsersComponent } from "./administration-panel/users/users.component";
import { administrationRouter } from "./administration.router";
import { UpdateReferenceComponent } from "./administration-panel/update-reference/update-reference.component";

@NgModule({
    declarations: [
        AdministrationPanelComponent,
        OrganizationsComponent,
        UsersComponent,
        SuperAdminComponent,
        AddWorkspaceComponent,
        UpdateReferenceComponent,
    ],
    imports: [CommonModule, SharedModule, administrationRouter],
})
export class AdministrationModule {}
