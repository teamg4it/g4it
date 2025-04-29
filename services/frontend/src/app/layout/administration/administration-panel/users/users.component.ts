/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, DestroyRef, inject } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { take } from "rxjs";
import {
    OrganizationCriteriaRest,
    OrganizationWithSubscriber,
} from "src/app/core/interfaces/administration.interfaces";
import { Role, RoleRightMap } from "src/app/core/interfaces/roles.interfaces";
import { Subscriber, UserDetails } from "src/app/core/interfaces/user.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";

@Component({
    selector: "app-users",
    templateUrl: "./users.component.html",
    providers: [ConfirmationService, MessageService],
})
export class UsersComponent {
    private destroyRef = inject(DestroyRef);

    userDetails!: UserDetails[];
    organization: OrganizationWithSubscriber = {} as OrganizationWithSubscriber;
    organizationlist: OrganizationWithSubscriber[] = [];
    enableList = false;
    clearForm: any;
    enableSearchButton: boolean = true;
    membersAndSearchVisible = false;
    subscribersDetails: any;
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
    subscriber!: Subscriber;
    firstPage: number = 0;

    constructor(
        private administrationService: AdministrationService,
        private formBuilder: FormBuilder,
        private confirmationService: ConfirmationService,
        private translate: TranslateService,
        private userService: UserService,
        private userDataService: UserDataService,
        private globalStore: GlobalStoreService,
    ) {}

    ngOnInit() {
        this.getUsers();
        this.searchForm = this.formBuilder.group({
            searchName: [
                "",
                [Validators.pattern(/^[ A-Za-z0-9_@.-]*$/), Validators.minLength(3)],
            ],
        });
        this.userService.currentSubscriber$.subscribe((res) => {
            this.subscriber = res;
        });
    }

    getUsers(updateOrganization: boolean = false) {
        this.administrationService.getUsers().subscribe((res) => {
            this.subscribersDetails = res;

            const list: OrganizationWithSubscriber[] = [];
            this.subscribersDetails.forEach((subscriber: Subscriber) => {
                subscriber.organizations.forEach((org: any) => {
                    const roles = this.userService.getRoles(subscriber, org);
                    if (
                        org.status === Constants.ORGANIZATION_STATUSES.ACTIVE &&
                        (roles.includes(Role.SubscriberAdmin) ||
                            roles.includes(Role.OrganizationAdmin))
                    ) {
                        list.push({
                            subscriberName: subscriber.name,
                            subscriberId: subscriber.id,
                            organizationName: org.name,
                            organizationId: org.id,
                            status: org.status,
                            dataRetentionDays: org.dataRetentionDays,
                            displayLabel: `${org.name} - (${subscriber.name})`,
                            criteriaDs: org.criteriaDs,
                            criteriaIs: org.criteriaIs,
                            authorizedDomains: subscriber.authorizedDomains,
                        });
                    }
                });
            });

            this.organizationlist = list;
            if (updateOrganization && this.organization.organizationId) {
                const currentOrganization = this.organizationlist.find(
                    (o) => o.organizationId === this.organization.organizationId,
                );
                if (currentOrganization) {
                    this.organization = currentOrganization;
                }
            }
        });
    }

    get searchFormControls() {
        return this.searchForm.controls;
    }

    checkForValidation() {
        const searchName = this.searchForm.value.searchName.trim();
        this.enableSearchButton =
            searchName.length >= 3 || searchName.length === 0 ? false : true;
    }

    clearSearchField() {
        this.searchForm.controls["searchName"].setValue("");
        this.enableSearchButton = true;
    }

    enrichAdmin(user: any) {
        if (!user.firstName) user.firstName = "";
        if (!user.lastName) user.lastName = "";

        user.isOrganizationAdmin = user.roles.includes(Role.OrganizationAdmin);
        user.isSubscriberAdmin = user.roles.includes(Role.SubscriberAdmin);
        user.isModule = this.getRole(user.roles, "INVENTORY_");
        user.dsModule = this.getRole(user.roles, "DIGITAL_SERVICE_");
        user.role = this.getRole(user.roles, "ADMINISTRATOR");
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
                    this.organization.subscriberId,
                    this.organization.organizationId,
                )
                .subscribe((res: any) => {
                    this.filteredMembers = res.map((user: any) => this.enrichAdmin(user));
                });
        }
    }

    getUsersDetails() {
        this.administrationService
            .getUserDetails(this.organization.organizationId)
            .subscribe((res) => {
                this.firstPage = 0; // To reset the paginator to the first page
                this.membersList = res.map((user: any) => this.enrichAdmin(user));
                this.filteredMembers = [...this.membersList];
            });
    }

    isAdmin(roles: string[]): boolean {
        return (
            roles.includes(Role.OrganizationAdmin) || roles.includes(Role.SubscriberAdmin)
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

    deleteUserDetails(event: Event, user: UserDetails) {
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
                    organizationId: this.organization.organizationId,
                    users: [
                        {
                            userId: user.id,
                            roles: user?.roles,
                        },
                    ],
                };
                this.administrationService.deleteUserDetails(body).subscribe((res) => {
                    this.searchList();
                });
            },
        });
    }

    openSidepanelForAddORUpdateOrg(user: UserDetails) {
        this.sidebarVisible = true;
        this.sidebarCreateMode = user.roles.length === 0;
        this.userDetail = user;
    }

    displayPopupFct() {
        const slicedCriteria = Object.keys(this.globalStore.criteriaList()).slice(0, 5);
        this.selectedCriteriaDS =
            this.organization.criteriaDs ?? this.subscriber?.criteria ?? slicedCriteria;
        this.selectedCriteriaIS =
            this.organization.criteriaIs ?? this.subscriber?.criteria ?? slicedCriteria;
        this.displayPopup = true;
    }

    handleSaveOrganization(organizationCriteria: OrganizationCriteriaRest) {
        this.administrationService
            .updateOrganizationCriteria(
                this.organization.organizationId,
                organizationCriteria,
            )
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res) => {
                this.selectedCriteriaDS = res.criteriaDs;
                this.selectedCriteriaIS = res.criteriaIs;
                this.displayPopup = false;
                this.getUsers(true);
                this.userDataService.fetchUserInfo().pipe(take(1)).subscribe();
            });
    }
}
