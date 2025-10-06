/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, DestroyRef, effect, inject, OnInit } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { firstValueFrom, take } from "rxjs";
import {
    WorkspaceCriteriaRest,
    WorkspaceWithOrganization,
} from "src/app/core/interfaces/administration.interfaces";
import { Role, RoleRightMap } from "src/app/core/interfaces/roles.interfaces";
import { Organization, UserDetails } from "src/app/core/interfaces/user.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";

@Component({
    selector: "app-users",
    templateUrl: "./users.component.html",
    providers: [ConfirmationService, MessageService],
})
export class UsersComponent implements OnInit {
    private readonly destroyRef = inject(DestroyRef);

    userDetails!: UserDetails[];
    userDetailEcoMind: boolean = false;
    workspace: WorkspaceWithOrganization = {} as WorkspaceWithOrganization;
    workspacelist: WorkspaceWithOrganization[] = [];
    enableList = false;
    clearForm: any;
    membersAndSearchVisible = false;
    organizationsDetails!: any;
    membersList: any;
    filteredMembers: any[] = [];
    openSearchResult: boolean = false;
    searchResult: any;
    addOrganizationEnable: boolean = false;
    userDetail!: UserDetails;
    membersListVisible: boolean = false;
    searchForm!: FormGroup;
    updateOrganizationEnable: boolean = false;

    sidebarCreateMode = false; // true for create mode, false for update mode
    sidebarVisible = false;
    errorMessageVisible = false;

    displayPopup = false;
    selectedCriteriaIS: string[] = [];
    selectedCriteriaDS: string[] = [];
    defaultCriteria: string[] = [];
    organization!: Organization;
    firstPage: number = 0;

    isEcoMindModuleEnabled: boolean = environment.isEcomindEnabled;
    isEcoMindEnabledForCurrentOrganizationSelected: boolean = false;

    constructor(
        private readonly administrationService: AdministrationService,
        private readonly formBuilder: FormBuilder,
        private readonly confirmationService: ConfirmationService,
        private readonly translate: TranslateService,
        private readonly userService: UserService,
        private readonly userDataService: UserDataService,
        private readonly globalStore: GlobalStoreService,
        private readonly router: Router,
    ) {
        effect(() => {
            if (this.administrationService.getUsersTriggered()) {
                this.getUsers();
            }
        });
    }

    ngOnInit() {
        this.getUsers();
        this.searchForm = this.formBuilder.group({
            searchName: [
                "",
                [Validators.pattern(/^[ A-Za-z0-9_@.-]*$/), Validators.minLength(3)],
            ],
        });
        this.userService.currentOrganization$.subscribe((res) => {
            this.organization = res;
        });
    }

    getUsers(updateOrganization: boolean = false) {
        this.administrationService.getUsers().subscribe((res) => {
            this.organizationsDetails = res;

            const list: WorkspaceWithOrganization[] = [];
            this.organizationsDetails.forEach((organization: Organization) => {
                organization.workspaces.forEach((workspace) => {
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
                });
            });

            this.workspacelist = list;
            if (updateOrganization && this.workspace.workspaceId) {
                const currentWorkspace = this.workspacelist.find(
                    (o) => o.workspaceId === this.workspace.workspaceId,
                );
                if (currentWorkspace) {
                    this.workspace = currentWorkspace;
                }
            }
        });
    }

    get searchFormControls() {
        return this.searchForm.controls;
    }

    clearSearchField() {
        this.searchForm.controls["searchName"].setValue("");
    }

    enrichAdmin(user: any) {
        if (!user.firstName) user.firstName = "";
        if (!user.lastName) user.lastName = "";

        user.isWorkspaceAdmin = user.roles.includes(Role.WorkspaceAdmin);
        user.isOrganizationAdmin = user.roles.includes(Role.OrganizationAdmin);
        user.isModule = this.getRole(user.roles, "INVENTORY_");
        user.dsModule = this.getRole(user.roles, "DIGITAL_SERVICE_");
        user.role = this.getRole(user.roles, "ADMINISTRATOR");
        user.ecomindModule = this.getRole(user.roles, "ECO_MIND_AI_");
        return user;
    }

    searchList() {
        this.errorMessageVisible = true;
        let searchData = this.searchForm.value.searchName.trim();
        if (searchData.length === 0) {
            this.getUsersDetails();
        } else {
            this.administrationService
                .getSearchDetails(
                    searchData,
                    this.workspace.organizationId,
                    this.workspace.workspaceId,
                )
                .subscribe((res: any) => {
                    this.filteredMembers = res.map((user: any) => this.enrichAdmin(user));
                });
        }
    }

    getUsersDetails() {
        this.administrationService
            .getUserDetails(this.workspace.workspaceId)
            .subscribe((res) => {
                this.firstPage = 0; // To reset the paginator to the first page
                this.membersList = res.map((user: any) => this.enrichAdmin(user));
                this.filteredMembers = [...this.membersList];
            });
    }

    isAdmin(roles: string[]): boolean {
        return (
            roles.includes(Role.WorkspaceAdmin) || roles.includes(Role.OrganizationAdmin)
        );
    }

    getRole(roles: string[], type: string) {
        if (!roles || roles.length === 0) return "";

        if (type === "ADMINISTRATOR") {
            return this.isAdmin(roles)
                ? "administration.role.admin"
                : "administration.role.user";
        }

        if (this.isAdmin(roles)) {
            return "administration.role.write";
        }

        const userRoles = roles
            .filter((role) => role.includes(type) && RoleRightMap[role])
            .map((role) => `administration.role.${RoleRightMap[role]}`);

        if (userRoles.length > 1) {
            return "administration.role.write";
        }

        return userRoles[0] || "";
    }

    async deleteUserDetails(event: Event, user: UserDetails) {
        const userId = (await firstValueFrom(this.userService.user$)).id;
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            message: this.translate.instant("administration.user.delete-message", {
                FirstName: user.firstName,
                LastName: user.lastName,
            }),
            header: this.translate.instant("administration.delete-confirmation"),
            icon: "pi pi-info-circle",
            acceptLabel: this.translate.instant("administration.delete"),
            acceptButtonStyleClass: "p-button-danger center",
            rejectButtonStyleClass: Constants.CONSTANT_VALUE.NONE,
            acceptIcon: Constants.CONSTANT_VALUE.NONE,
            rejectIcon: Constants.CONSTANT_VALUE.NONE,
            rejectVisible: false,

            accept: () => {
                let body = {
                    workspaceId: this.workspace.workspaceId,
                    users: [
                        {
                            userId: user.id,
                            roles: user?.roles,
                        },
                    ],
                };
                this.administrationService.deleteUserDetails(body).subscribe((res) => {
                    const currentUserRoles = body.users.find(
                        (u) => u.userId === userId,
                    )?.roles;
                    if (currentUserRoles?.includes(Role.WorkspaceAdmin)) {
                        this.userDataService
                            .fetchUserInfo()
                            .pipe(take(1))
                            .subscribe(() => {
                                this.router.navigateByUrl(Constants.WELCOME_PAGE);
                                return;
                            });
                    } else {
                        this.searchList();
                    }
                });
            },
        });
    }

    openSidepanelForAddORUpdateOrg(
        user: UserDetails,
        isEcoMindEnabledForCurrentOrganizationSelected: boolean,
    ) {
        this.sidebarVisible = true;
        this.sidebarCreateMode = user.roles.length === 0;
        this.userDetail = user;
        this.userDetailEcoMind = isEcoMindEnabledForCurrentOrganizationSelected;
    }

    displayPopupFct() {
        const slicedCriteria = Object.keys(this.globalStore.criteriaList()).slice(0, 5);
        this.selectedCriteriaDS =
            this.workspace.criteriaDs ?? this.organization?.criteria ?? slicedCriteria;
        this.selectedCriteriaIS =
            this.workspace.criteriaIs ?? this.organization?.criteria ?? slicedCriteria;
        this.displayPopup = true;
    }

    handleSaveWorkspace(organizationCriteria: WorkspaceCriteriaRest) {
        this.administrationService
            .updateWorkspaceCriteria(this.workspace.workspaceId, organizationCriteria)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res) => {
                this.selectedCriteriaDS = res.criteriaDs;
                this.selectedCriteriaIS = res.criteriaIs;
                this.displayPopup = false;
                this.getUsers(true);
                this.userDataService.fetchUserInfo().pipe(take(1)).subscribe();
            });
    }
    getSelectedOrganization() {
        this.administrationService
            .getOrganizationById(this.workspace.organizationId)
            .subscribe((res) => {
                this.isEcoMindEnabledForCurrentOrganizationSelected = res.ecomindai;
            });
    }
}
