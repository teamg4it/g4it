/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { DomainSubscribers } from "src/app/core/interfaces/administration.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";

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
    organizationlist: DomainSubscribers[] = [];

    constructor(
        private administrationService: AdministrationService,
        private userDataService: UserDataService,
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
        this.userDataService.fetchUserInfo().subscribe((userInfo) => {
            if (userInfo?.email) {
                const body = {
                    email: userInfo?.email,
                };
                this.administrationService.getDomainSubscribers(body).subscribe((res) => {
                    this.organizationlist = res;

                    if (
                        Array.isArray(this.organizationlist) &&
                        this.organizationlist.length === 1 &&
                        this.organizationlist[0]["id"]
                    ) {
                        this.spaceForm.controls["organization"].setValue(
                            this.organizationlist[0]["id"],
                        );
                        this.spaceDetails["menu"][0]["hidden"] = true;
                        this.selectTab(1);
                    }
                });
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
