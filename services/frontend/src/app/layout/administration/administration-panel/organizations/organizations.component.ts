/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, DestroyRef, inject, OnInit } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { firstValueFrom, take } from "rxjs";
import {
    DomainOrganizations,
    Organization,
    OrganizationCriteriaRest,
    Workspace,
    WorkspaceUpsertRest,
} from "src/app/core/interfaces/administration.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";

@Component({
    selector: "app-organizations",
    templateUrl: "./organizations.component.html",
    providers: [ConfirmationService, MessageService],
})
export class OrganizationsComponent implements OnInit {
    private readonly destroyRef = inject(DestroyRef);

    editable = false;
    organizationsDetails!: Organization[];
    unmodifiedOrganizationsDetails!: Organization[];
    organization!: Organization;
    newWorkspace: Workspace = {} as Workspace;

    status = Constants.WORKSPACE_STATUSES;

    displayPopup = false;
    selectedCriteria: string[] = [];
    Role = Role;
    myDomain!: string;
    notOrganizationAdminInSome = false;
    domainOrganizations: DomainOrganizations[] = [];
    constructor(
        private readonly confirmationService: ConfirmationService,
        public administrationService: AdministrationService,
        private readonly translate: TranslateService,
        private readonly userDataService: UserDataService,
        private readonly globalStore: GlobalStoreService,
        private readonly userService: UserService,
        private readonly workspaceService: WorkspaceService,
    ) {}

    ngOnInit() {
        this.init();
    }

    init(organization: string | undefined = undefined) {
        this.administrationService.getOrganizations().subscribe((res: Organization[]) => {
            this.organizationsDetails = res;
            this.unmodifiedOrganizationsDetails = structuredClone(res);
            if (organization) {
                this.organization = this.unmodifiedOrganizationsDetails.find(
                    (s) => s.name === organization,
                )!;
            }
            this.notOrganizationAdminInSome = this.organizationsDetails.some(
                (s) => !s.roles?.includes(Role.OrganizationAdmin),
            );

            if (this.notOrganizationAdminInSome) {
                this.getDomainOrganizationsList();
            }
        });

        this.userDataService.userSubject.subscribe((user) => {
            this.myDomain = user.email.split("@")[1];
        });
    }

    async getDomainOrganizationsList() {
        const userEmail = (await firstValueFrom(this.userService.user$)).email;

        if (userEmail) {
            const body = {
                email: userEmail,
            };
            this.workspaceService.getDomainOrganizations(body).subscribe((res) => {
                this.domainOrganizations = res;
            });
        }
    }

    checkOrganization(event: any, workspace: Workspace, organization: Organization) {
        const isOrganizationAdmin = organization.roles?.includes(Role.OrganizationAdmin);
        let workspaces: Workspace[] = [];
        if (isOrganizationAdmin) {
            workspaces =
                this.unmodifiedOrganizationsDetails.find(
                    (s) => s.name === organization.name,
                )?.workspaces || [];
            workspace.uiStatus = undefined;
        } else {
            workspaces = (this.domainOrganizations.find(
                (s) => s.name === organization.name,
            )?.workspaces || []) as Workspace[];
            workspace.uiStatus = undefined;
        }

        if (event.trim().includes(" ")) {
            workspace.uiStatus = "SPACE";
            return;
        }

        if (workspaces.some((org) => org.name === event && org.id !== workspace.id)) {
            workspace.uiStatus = "DUPLICATE";
            return;
        }

        if (event && !workspaces.some((org) => org.name === event)) {
            workspace.uiStatus = "OK";
        }
    }

    confirmDelete(event: Event, workspace: Workspace) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            message: this.translate.instant("administration.delete-message"),
            header: this.translate.instant("administration.delete-confirmation"),
            icon: "pi pi-info-circle",
            acceptLabel: this.translate.instant("administration.delete"),
            acceptButtonStyleClass: "p-button-danger center",
            rejectButtonStyleClass: Constants.CONSTANT_VALUE.NONE,
            acceptIcon: Constants.CONSTANT_VALUE.NONE,
            rejectIcon: Constants.CONSTANT_VALUE.NONE,
            rejectVisible: false,

            accept: () => {
                this.updateWorkspace(workspace.id, {
                    organizationId: this.organization.id,
                    name: workspace.name.trim(),
                    status: Constants.WORKSPACE_STATUSES.TO_BE_DELETED,
                });
            },
        });
    }

    confirmToActive(workspace: Workspace) {
        this.updateWorkspace(workspace.id, {
            organizationId: this.organization?.id,
            name: workspace.name.trim(),
            status: Constants.WORKSPACE_STATUSES.ACTIVE,
        });
    }

    saveWorkspaces(workspaces: Workspace[]) {
        for (const workspace of workspaces) {
            if (workspace.uiStatus === "OK") {
                this.updateWorkspace(workspace.id, {
                    organizationId: this.organization?.id,
                    name: workspace.name,
                    status: workspace.status,
                });
            }
        }
    }

    addWorkplace(workspace: Workspace) {
        if (workspace === undefined) return;
        let body = {
            organizationId: this.organization?.id,
            name: workspace.name.trim(),
            status: Constants.WORKSPACE_STATUSES.ACTIVE,
        };
        this.administrationService.postWorkspace(body).subscribe((_) => {
            this.init(this.organization?.name);
            this.newWorkspace = {} as Workspace;
            this.editable = false;
            this.userDataService.fetchUserInfo().pipe(take(1)).subscribe();
        });
    }

    updateWorkspace(workspaceId: number, body: WorkspaceUpsertRest) {
        this.administrationService.updateWorkspace(workspaceId, body).subscribe((_) => {
            this.init(this.organization?.name);
            this.userDataService.fetchUserInfo().pipe(take(1)).subscribe();
            if (this.notOrganizationAdminInSome) {
                this.getDomainOrganizationsList();
            }
        });
    }

    displayPopupFct() {
        const slicedCriteria = Object.keys(this.globalStore.criteriaList()).slice(0, 5);
        this.selectedCriteria = this.organization?.criteria ?? slicedCriteria;
        this.displayPopup = true;
    }

    handleSaveOrganization(organizationCriteria: OrganizationCriteriaRest) {
        this.administrationService
            .updateOrganizationCriteria(this.organization?.id, organizationCriteria)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((_) => {
                this.displayPopup = false;
                this.init(this.organization?.name);
                this.userDataService.fetchUserInfo().pipe(take(1)).subscribe();
            });
    }
}
