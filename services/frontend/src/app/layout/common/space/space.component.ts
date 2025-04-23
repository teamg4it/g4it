/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { OrganizationWithSubscriber } from "src/app/core/interfaces/administration.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { Subscriber } from "src/app/core/interfaces/user.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { Constants } from "src/constants";

interface SpaceDetails {
    menu: {
        title?: string;
        subTitle?: string;
        description?: string;
        iconClass?: string;
        active?: boolean;
        hidden?: boolean;
        optional?: boolean;
    }[];
    form: {
        name: string;
        label?: string;
        hintText?: string;
        type?: string;
        placeholder?: string;
        options?: {
            label?: string;
            value?: string;
        };
    }[];
}

@Component({
    selector: "app-space",
    templateUrl: "./space.component.html",
    styleUrls: ["./space.component.scss"],
})
export class SpaceComponent implements OnInit {
    @Input() spaceDetails: SpaceDetails = {
        menu: [
            {
                subTitle: "Mandatory",
                title: "Choose an organization",
                description: "No organization chosen",
                iconClass: "pi pi-exclamation-circle",
                active: true,
            },
            {
                subTitle: "Mandatory",
                title: "Set a workspace name",
                description: "No space name indicated",
                iconClass: "pi pi-exclamation-circle",
            },
            {
                subTitle: "Optional",
                title: "Invite contributors",
                description: "No contributor indicated",
                iconClass: "pi pi-arrow-right-arrow-left",
                optional: true,
            },
        ],
        form: [
            {
                name: "organization",
                label: "Choose an organization",
                hintText:
                    "Before creating your space, please check in which organization it will be located.",
                type: "select",
                placeholder: "Select an organization",
            },
            {
                name: "spaceName",
                label: "Set a workspace name",
                hintText:
                    "Before creating your space, please check in which organization it will be located.",
                type: "text",
                placeholder: "Type the space name",
            },
            {
                name: "contributor",
                label: "Select contributors",
                hintText:
                    "Please enter the email addresses of your contributors, separated by comma. This step is optional",
                type: "text",
                placeholder: "Type the contributor address",
            },
        ],
    };

    @Output() sidebarVisibleChange: EventEmitter<any> = new EventEmitter();

    selectedMenuIndex: number | null = null;
    subscribersDetails: any;
    organization: OrganizationWithSubscriber = {} as OrganizationWithSubscriber;
    organizationlist: OrganizationWithSubscriber[] = [];

    constructor(
        private administrationService: AdministrationService,
        private userService: UserService,
    ) {}

    spaceForm = new FormGroup({
        organization: new FormControl<number | null>(null, Validators.required),
        spaceName: new FormControl("", Validators.required),
        contributor: new FormControl(""),
    });

    ngOnInit() {
        this.getUsers();
        this.selectTab(0);
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
        this.selectedMenuIndex = index;
        this.spaceDetails["menu"].forEach((detail, i) => {
            detail.active = i === index;
        });
    }

    closeSidebar() {
        this.selectTab(0);
        this.spaceForm.reset();
        this.sidebarVisibleChange.emit(false);
    }

    getUsers() {
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
            if (this.organizationlist.length === 1) {
                this.spaceForm.controls["organization"].setValue(
                    this.organizationlist[0]["organizationId"],
                );
                this.spaceDetails["menu"][0]["hidden"] = true;
                this.selectTab(1);
            }
        });
    }

    createSpace() {
        console.log(this.spaceForm);
    }

    createInventory() {
        console.log(this.spaceForm);
    }
}
