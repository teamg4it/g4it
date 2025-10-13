/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, inject, Input, OnInit, Output } from "@angular/core";
import { AbstractControl, FormControl, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { firstValueFrom, take } from "rxjs";
import { DomainOrganizations } from "src/app/core/interfaces/administration.interfaces";
import { CustomSidebarMenuForm } from "src/app/core/interfaces/sidebar-menu-form.interface";
import { User } from "src/app/core/interfaces/user.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";
import { Constants } from "src/constants";

@Component({
    selector: "app-workspace",
    templateUrl: "./workspace.component.html",
    styleUrls: ["./workspace.component.scss"],
})
export class WorkspaceComponent implements OnInit {
    private readonly userDataService = inject(UserDataService);
    @Input() spaceDetails: CustomSidebarMenuForm = {
        menu: [
            {
                subTitle: this.translate.instant("common.workspace.mandatory"),
                title: this.translate.instant("common.workspace.choose-organisation"),
                description: this.translate.instant(
                    "common.workspace.no-organization-chosen",
                ),
                iconClass: "pi pi-exclamation-circle",
                hidden: true,
            },
            {
                subTitle: this.translate.instant("common.workspace.mandatory"),
                title: this.translate.instant("common.workspace.set-workspace-name"),
                description: this.translate.instant("common.workspace.no-space-name"),
                iconClass: "pi pi-exclamation-circle",
            },
        ],
        form: [
            {
                name: "organization",
                label: this.translate.instant("common.workspace.choose-organisation"),
                hintText: this.translate.instant("common.workspace.hint-text"),
                type: "select",
                placeholder: this.translate.instant(
                    "common.workspace.select-organisation",
                ),
            },
            {
                name: "spaceName",
                label: this.translate.instant("common.workspace.set-workspace-name"),
                hintText: this.translate.instant(
                    "common.workspace.hint-text-workspace-name",
                ),
                type: "text",
                placeholder: this.translate.instant("common.workspace.type-space-name"),
            },
        ],
    };

    @Output() sidebarVisibleChange: EventEmitter<any> = new EventEmitter();

    selectedMenuIndex: number | null = null;
    organizationlist: DomainOrganizations[] = [];
    existingWorkspace: any = [];

    constructor(
        private readonly workspaceService: WorkspaceService,
        private readonly userService: UserService,
        private readonly messageService: MessageService,
        private readonly translate: TranslateService,
        private readonly router: Router,
        private readonly administrationService: AdministrationService,
    ) {}

    spaceForm = new FormGroup({
        organization: new FormControl<number | undefined>(undefined, Validators.required),
        spaceName: new FormControl<string | undefined>(undefined, [
            Validators.required,
            this.spaceDuplicateValidator.bind(this),
            Validators.maxLength(20),
            Validators.pattern(/^[^@\/;?]*$/),
        ]),
    });

    ngOnInit() {
        this.getDomainOrganizationsList();
        this.selectTab(0);

        this.spaceForm.get("organization")?.valueChanges.subscribe((value) => {
            if (value) {
                this.existingWorkspace =
                    this.organizationlist?.find((org) => org.id === value)?.workspaces ??
                    [];
                this.spaceForm.get("spaceName")?.updateValueAndValidity();
            }
        });
    }

    organizationValidator() {
        return this.organizationlist.length === 0 ? { noOrganization: true } : null;
    }

    spaceDuplicateValidator(control: AbstractControl) {
        if (control?.value?.includes(" ")) {
            return { spaceNotAllowed: true };
        } else if (control?.value) {
            const getSpaceName =
                this.existingWorkspace?.find(
                    (data: any) => data.name.toLowerCase() == control.value.toLowerCase(),
                ) ?? undefined;
            if (getSpaceName) {
                return { duplicate: true };
            }
        }
        return null;
    }

    previousTab(index: number) {
        if (index > 0) {
            this.selectTab(--index);
        }
    }

    nextTab(index: number) {
        if (index < this.spaceDetails["menu"].length - 1) {
            this.selectTab(++index);
        }
    }

    selectTab(index: number) {
        if (
            index > 0 &&
            this.spaceDetails.form[index - 1] &&
            this.spaceForm.get(this.spaceDetails.form[index - 1].name)?.invalid
        ) {
            return;
        }
        this.selectedMenuIndex = index;
        for (const [i, detail] of this.spaceDetails.menu.entries()) {
            detail.active = i === index;
        }
    }

    async getDomainOrganizationsList() {
        const userEmail = (await firstValueFrom(this.userService.user$)).email;

        if (userEmail) {
            const body = {
                email: userEmail,
            };
            this.workspaceService.getDomainOrganizations(body).subscribe((res) => {
                this.organizationlist = res;
                this.spaceForm
                    .get("organization")
                    ?.addValidators([this.organizationValidator.bind(this)]);
                this.spaceForm.get("organization")?.updateValueAndValidity();

                if (
                    Array.isArray(this.organizationlist) &&
                    this.organizationlist.length === 1 &&
                    this.organizationlist[0]["id"]
                ) {
                    this.spaceForm.controls["organization"].setValue(
                        this.organizationlist[0]["id"],
                    );
                    this.selectTab(1);
                } else {
                    this.spaceDetails["menu"][0]["hidden"] = false;
                }
            });
        }
    }

    getValidMenu() {
        return this.spaceDetails["menu"].filter((menu) => {
            return menu.hidden !== true;
        });
    }

    createSpace() {
        if (this.spaceForm.valid) {
            const body = {
                organizationId: this.spaceForm.value["organization"] ?? undefined,
                name: this.spaceForm.value["spaceName"]?.trim() ?? undefined,
                status: "ACTIVE",
            };
            this.workspaceService.postUserWorkspace(body).subscribe((res) => {
                const organization =
                    this.organizationlist?.find(
                        (org) => org.id === this.spaceForm.value["organization"],
                    ) ?? undefined;
                this.closeSidebar();
                if (organization) {
                    this.userDataService
                        .fetchUserInfo()
                        .pipe(take(1))
                        .subscribe((user: User) => {
                            const routeSplit = this.router.url.split("/");
                            const page = routeSplit.pop();
                            if (
                                page === Constants.ENDPOINTS.digitalServices ||
                                page === Constants.ENDPOINTS.inventories
                            ) {
                                this.router.navigateByUrl(
                                    `organizations/${organization.name}/workspaces/${res.id}/${page}`,
                                );
                            } else {
                                const newOrganization = user.organizations.find(
                                    (sub) => sub.name === organization.name,
                                );
                                let newWorkspace;
                                if (newOrganization?.workspaces) {
                                    newWorkspace = newOrganization.workspaces.find(
                                        (org) => org.id === res.id,
                                    );
                                }
                                if (newOrganization && newWorkspace) {
                                    // To set the new organization and workspace in the user service. So that Top component is updated with new workspace which created
                                    this.userService.setOrganizationAndWorkspace(
                                        newOrganization,
                                        newWorkspace,
                                    );
                                }
                                if (routeSplit.includes("administration")) {
                                    // To refresh workspace list in Admin page.
                                    this.administrationService.refreshGetUsers();
                                }
                                this.router.navigate([this.router.url]);
                            }
                        });
                }
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.workspace.workspace-created"),
                });
            });
        }
    }

    closeSidebar() {
        this.selectTab(0);
        this.spaceForm.reset();
        this.workspaceService.setOpen(false);
        this.sidebarVisibleChange.emit(false);
    }
}
