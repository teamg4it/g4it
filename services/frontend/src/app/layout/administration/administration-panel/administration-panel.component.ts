/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MenuItem } from "primeng/api";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-administration-panel",
    templateUrl: "./administration-panel.component.html",
})
export class AdministrationPanelComponent {
    tabMenuList!: MenuItem[];

    constructor(
        public userService: UserService,
        public router: Router,
        private translate: TranslateService,
    ) {}

    ngOnInit() {
        this.userService.user$.subscribe((user) => {
            this.tabMenuList = [];

            if (this.userService.hasAnyAdminRole(user)) {
                this.tabMenuList.push({
                    label: this.translate.instant("administration.manage-users"),
                    routerLink: "users",
                    id: "users-tab",
                });
            }

            if (this.userService.hasAnySubscriberAdminRole(user)) {
                this.tabMenuList.push({
                    label: this.translate.instant("administration.manage-organizations"),
                    routerLink: "organizations",
                    id: "organizations-tab",
                });
            }

            if (user.isSuperAdmin) {
                this.tabMenuList.push({
                    label: this.translate.instant("administration.super-admin-title"),
                    routerLink: "actions",
                    id: "super-admin-tab",
                });
            }
        });
    }
}
