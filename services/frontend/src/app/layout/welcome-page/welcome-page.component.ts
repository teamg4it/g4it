/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { Component, DestroyRef, inject } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Router, RouterModule } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { firstValueFrom } from "rxjs";
import { Subscriber } from "src/app/core/interfaces/administration.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { environment } from "src/environments/environment";
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
        ScrollPanelModule,
        RouterModule,
    ],
})
export class WelcomePageComponent {
    userName: string = "";
    selectedPath: string = "";
    currentSubscriber: Subscriber = {} as Subscriber;
    isAllowedInventory: boolean = false;
    isAllowedDigitalService: boolean = false;
    isAllowedEcoMindAi: boolean = false;
    isEcoMindEnabledForCurrentSubscriber: boolean = false;
    isEcoMindModuleEnabled: boolean = environment.isEcomindEnabled;

    private readonly destroyRef = inject(DestroyRef);
    public userService = inject(UserService);
    ecoDesignPercent = this.userService.ecoDesignPercent;

    externalLinks = [
        {
            href: "https://github.com/G4ITTeam/g4it",
            label: "common.github-link",
            iconClass: "pi pi-sign-out ml-auto",
        },
        {
            href: "https://saas-g4it.com/documentation/",
            label: "common.doc-link",
            iconClass: "pi pi-sign-out ml-auto",
        },
        {
            href: "https://github.com/Boavizta/boaviztapi",
            label: "common.boaviztapi-github-link",
            iconClass: "pi pi-sign-out ml-3 lg:ml-4",
        },
    ];

    constructor(
        private workspaceService: WorkspaceService,
        public readonly router: Router,
    ) {}

    async ngOnInit() {
        const userDetails = await firstValueFrom(this.userService.user$);
        this.userService.isAllowedInventoryRead$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((isAllowed: boolean) => {
                this.isAllowedInventory = isAllowed;
            });
        this.userService.isAllowedDigitalServiceRead$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((isAllowed: boolean) => {
                this.isAllowedDigitalService = isAllowed;
            });
        this.userService.isAllowedEcoMindAiRead$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((isAllowed: boolean) => {
                this.isAllowedEcoMindAi = isAllowed;
            });
        this.userName = userDetails?.firstName + " " + userDetails?.lastName;

        this.userService.currentSubscriber$.subscribe((subscriber: any) => {
            this.currentSubscriber = subscriber;
            this.isEcoMindEnabledForCurrentSubscriber = this.currentSubscriber.ecomindai;
        });

        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization: any) => {
                this.selectedPath = `/subscribers/${this.currentSubscriber.name}/organizations/${organization?.id}`;
            });
    }

    openWorkspaceSidebar() {
        this.workspaceService.setOpen(true);
    }

    inventories() {
        if (this.isAllowedInventory) {
            this.router.navigateByUrl(`${this.selectedPath}/inventories`);
        } else {
            this.router.navigateByUrl("/useful-information");
        }
    }

    digitalServices() {
        if (this.isAllowedDigitalService) {
            this.router.navigateByUrl(`${this.selectedPath}/digital-services`, {
                state: { isIa: false },
            });
        } else {
            this.router.navigateByUrl("/useful-information");
        }
    }

    ecoMindAi() {
        if (this.isAllowedEcoMindAi) {
            this.router.navigateByUrl(`${this.selectedPath}/eco-mind-ai`, {
                state: { isIa: true },
            });
        } else {
            this.router.navigateByUrl("/useful-information");
        }
    }
}
