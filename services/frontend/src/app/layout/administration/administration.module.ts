import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { SharedModule } from "src/app/core/shared/shared.module";
import { AdministrationPanelComponent } from "./administration-panel/administration-panel.component";
import { OrganizationsComponent } from "./administration-panel/organizations/organizations.component";
import { SuperAdminComponent } from "./administration-panel/super-admin/super-admin.component";
import { AddOrganizationComponent } from "./administration-panel/users/add-organization/add-organization.component";
import { UsersComponent } from "./administration-panel/users/users.component";
import { administrationRouter } from "./administration.router";

@NgModule({
    declarations: [
        AdministrationPanelComponent,
        OrganizationsComponent,
        AddOrganizationComponent,
        UsersComponent,
        SuperAdminComponent,
    ],
    imports: [CommonModule, SharedModule, administrationRouter],
})
export class AdministrationModule {}
