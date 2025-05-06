/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { RouterModule } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { firstValueFrom } from "rxjs";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";

@Component({
    selector: "app-welcome-page",
    templateUrl: "./welcome-page.component.html",
    styleUrls: ["./welcome-page.component.scss"],
    standalone: true,
    imports: [
        CommonModule,
        ButtonModule,
        TranslateModule,
        CardModule,
        RouterModule,
        ScrollPanelModule,
    ],
})
export class WelcomePageComponent {
    userName: string = "";

    constructor(
        private userService: UserService,
        private workspaceService: WorkspaceService,
    ) {}

    async ngOnInit() {
        const userDetails = await firstValueFrom(this.userService.user$);
        this.userName = userDetails?.firstName + " " + userDetails?.lastName;
    }

    openWorkspaceSidebar() {
        this.workspaceService.setOpen(true);
    }

    inventories() {
        console.log("Inventories");
    }

    digitalServices() {
        console.log("Digital Services");
    }
}
