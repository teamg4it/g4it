/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    DestroyRef,
    EventEmitter,
    inject,
    Input,
    Output,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { take } from "rxjs";
import { Role, RoleValue } from "src/app/core/interfaces/roles.interfaces";
import { UserDetails } from "src/app/core/interfaces/user.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";
import { Constants } from "src/constants";

@Component({
    selector: "app-add-organization",
    templateUrl: "./add-organization.component.html",
    providers: [ConfirmationService, MessageService],
})
export class AddOrganizationComponent {
    @Input() userDetail!: UserDetails;
    @Input() organization: any;
    @Input() clearForm!: false;
    @Output() close: EventEmitter<any> = new EventEmitter();
    @Input() updateOrganizationEnable = false;

    dsRoles = [Role.DigitalServiceRead, Role.DigitalServiceWrite];
    isRoles = [Role.InventoryRead, Role.InventoryWrite];

    adminModule: any;
    dsModule: RoleValue = {} as RoleValue;
    isModule: RoleValue = {} as RoleValue;

    adminModuleValues: RoleValue[] = [] as RoleValue[];
    dsModuleValues: RoleValue[] = [] as RoleValue[];
    isModuleValues: RoleValue[] = [] as RoleValue[];

    isAdmin: boolean = false;
    isAdminRoleDisabled: boolean = false;
    private destroyRef = inject(DestroyRef);
    constructor(
        public administrationService: AdministrationService,
        private translate: TranslateService,
        private userDataService: UserDataService,
        private userService: UserService,
        private router: Router,
    ) {}
    ngOnInit() {
        this.isModuleValues = this.isRoles.map((role) => this.getRoleValue(role));

        this.dsModuleValues = this.dsRoles.map((role) => this.getRoleValue(role));

        this.adminModuleValues = [
            {
                code: "SimpleUser" as Role,
                value: this.translate.instant("administration.role.user"),
            },
            {
                code: Role.OrganizationAdmin,
                value: this.translate.instant("administration.role.admin"),
            },
        ];

        this.restrictAdminRoleByDomain();
    }

    ngOnChanges() {
        this.clearFormData();

        if (this.userDetail === undefined || this.userDetail.roles === undefined) return;

        const roles = this.userDetail.roles;

        if (roles.includes(Role.OrganizationAdmin)) {
            this.forceAdmin();
            return;
        }

        this.adminModule = {
            code: "SimpleUser" as Role,
            value: this.translate.instant(`administration.role.user`),
        };

        for (const role of [...this.isRoles].reverse()) {
            if (roles.includes(role)) {
                this.isModule = this.getRoleValue(role);
                break;
            }
        }

        for (const role of [...this.dsRoles].reverse()) {
            if (roles.includes(role)) {
                this.dsModule = this.getRoleValue(role);
                break;
            }
        }
    }

    getRoleValue(role: Role): RoleValue {
        return {
            code: role,
            value: this.translate.instant(
                `administration.role.${this.readOrWrite(role)}`,
            ),
        };
    }

    readOrWrite(role: Role): string | undefined {
        if (role.endsWith("READ")) return "read";
        if (role.endsWith("WRITE")) return "write";
        return undefined;
    }

    getOrganizationBody() {
        let roles: string[] = [];

        if (this.adminModule.code === Role.OrganizationAdmin) {
            roles.push(Role.OrganizationAdmin);
        } else {
            if (this.isModule) roles.push(this.isModule.code);
            if (this.dsModule) roles.push(this.dsModule.code);
        }
        return {
            organizationId: this.organization.organizationId,
            users: [
                {
                    userId: this.userDetail?.id,
                    roles,
                },
            ],
        };
    }

    addUpdateOrg() {
        this.administrationService
            .postUserToOrganizationAndAddRoles(this.getOrganizationBody())
            .subscribe(() => {
                this.userService.user$
                    .pipe(takeUntilDestroyed(this.destroyRef))
                    .subscribe((user) => {
                        this.userDataService
                            .fetchUserInfo()
                            .pipe(take(1))
                            .subscribe(() => {
                                const currentUserRoles =
                                    this.getOrganizationBody().users.find(
                                        (u) => u.userId === user.id,
                                    )?.roles;
                                const isAdmin =
                                    currentUserRoles?.includes(Role.SubscriberAdmin) ||
                                    currentUserRoles?.includes(Role.OrganizationAdmin);
                                if (!isAdmin && currentUserRoles) {
                                    this.router.navigateByUrl(Constants.WELCOME_PAGE);
                                    return;
                                }
                                this.close.emit(false);
                            });
                    });
            });
    }

    forceAdmin() {
        this.dsModule = this.getRoleValue(Role.DigitalServiceWrite);
        this.isModule = this.getRoleValue(Role.InventoryWrite);

        this.adminModule = {
            code: Role.OrganizationAdmin,
            value: this.translate.instant("administration.role.admin"),
        };
        this.isAdmin = true;
    }

    validateOnAdmin() {
        if (this.adminModule.code === Role.OrganizationAdmin) {
            this.forceAdmin();
        } else {
            this.isAdmin = false;
        }
    }

    cancel() {
        this.clearFormData();
        this.close.emit(false);
    }

    clearFormData() {
        this.isAdmin = false;
        this.adminModule = {} as RoleValue;
        this.dsModule = {} as RoleValue;
        this.isModule = {} as RoleValue;
        this.isAdminRoleDisabled = false;
    }

    restrictAdminRoleByDomain() {
        if (this.organization.authorizedDomains) {
            this.isAdminRoleDisabled = !this.organization.authorizedDomains.includes(
                this.userDetail.email.split("@")[1],
            );
            if (this.isAdminRoleDisabled) {
                this.adminModuleValues = this.adminModuleValues.filter(
                    (role) => role.code !== Role.OrganizationAdmin,
                );
            }
        }
    }
}
