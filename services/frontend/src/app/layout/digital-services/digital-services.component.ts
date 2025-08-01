/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, DestroyRef, inject } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ActivatedRoute, NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { PaginatorState } from "primeng/paginator";
import { finalize, lastValueFrom } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { Organization } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { environment } from "src/environments/environment";

@Component({
    selector: "app-digital-services",
    templateUrl: "./digital-services.component.html",
    providers: [MessageService, ConfirmationService],
})
export class DigitalServicesComponent {
    private readonly global = inject(GlobalStoreService);

    digitalServices: DigitalService[] = [];
    selectedDigitalService: DigitalService = {} as DigitalService;
    sidebarVisible = false;

    allDigitalServices: DigitalService[] = [];
    paginatedDigitalServices: DigitalService[] = [];
    selectedOrganization!: string;
    isAllowedDigitalService: boolean = false;
    isAllowedEcoMindAiService: boolean = false;
    isEcoMindEnabledForCurrentSubscriber: boolean = false;
    isEcoMindModuleEnabled: boolean = environment.isEcomindEnabled;

    first: number = 0;

    rowsPerPage: number = 10;
    currentPage = 0;
    isEcoMindAi = false;
    private readonly destroyRef = inject(DestroyRef);

    constructor(
        private readonly digitalServicesData: DigitalServicesDataService,
        private readonly route: ActivatedRoute,
        private readonly router: Router,
        private readonly translate: TranslateService,
        private readonly messageService: MessageService,
        public userService: UserService,
    ) {}

    async ngOnInit(): Promise<void> {
        this.route.parent?.data.subscribe((data) => {
            this.isEcoMindAi = data["isIa"] === true;
        });
        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization: Organization) => {
                this.selectedOrganization = organization.name;
            });
        this.userService.currentSubscriber$.subscribe((subscriber: any) => {
            this.isEcoMindEnabledForCurrentSubscriber = subscriber.ecomindai;
        });
        this.userService.roles$.subscribe((roles: Role[]) => {
            this.isAllowedDigitalService =
                roles.includes(Role.DigitalServiceRead) ||
                roles.includes(Role.DigitalServiceWrite);
            this.isAllowedEcoMindAiService =
                roles.includes(Role.EcoMindAiRead) || roles.includes(Role.EcoMindAiWrite);
        });
        this.global.setLoading(true);
        await this.retrieveDigitalServices();
        this.global.setLoading(false);
        this.router.events
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((event) => {
                if (event instanceof NavigationEnd) {
                    if (this.isAllowedDigitalService) {
                        this.retrieveDigitalServices();
                    }
                }
            });
    }

    async retrieveDigitalServices() {
        this.allDigitalServices = [];
        if (
            this.isEcoMindAi &&
            this.isAllowedEcoMindAiService &&
            this.isEcoMindEnabledForCurrentSubscriber &&
            this.isEcoMindModuleEnabled
        ) {
            const apiResult = await lastValueFrom(this.digitalServicesData.list(true));
            apiResult.sort((x, y) => x.name.localeCompare(y.name));
            this.allDigitalServices.push(...apiResult);
        } else if (!this.isEcoMindAi && this.isAllowedDigitalService) {
            const apiResult = await lastValueFrom(this.digitalServicesData.list(false));
            apiResult.sort((x, y) => x.name.localeCompare(y.name));
            this.allDigitalServices.push(...apiResult);
        }
        this.updatePaginatedItems();
    }

    async createNewDigitalService() {
        if (
            this.isEcoMindAi &&
            this.isAllowedEcoMindAiService &&
            this.isEcoMindEnabledForCurrentSubscriber &&
            this.isEcoMindModuleEnabled
        ) {
            const { uid } = await lastValueFrom(this.digitalServicesData.create(true));
            this.goToDigitalServiceFootprint(uid);
        } else if (!this.isEcoMindAi && this.isAllowedDigitalService) {
            const { uid } = await lastValueFrom(this.digitalServicesData.create(false));
            this.goToDigitalServiceFootprint(uid);
        }
    }

    onPageChange(event: PaginatorState) {
        this.first = event.first!;
        this.rowsPerPage = event.rows!;
        this.currentPage = event.page!;
        this.updatePaginatedItems();
    }

    updatePaginatedItems() {
        const start = this.currentPage * this.rowsPerPage;
        const end = start + this.rowsPerPage;
        this.paginatedDigitalServices = this.allDigitalServices.slice(start, end);
    }

    goToDigitalServiceFootprint(uid: string) {
        if (this.isEcoMindAi) {
            this.router.navigate([`${uid}/footprint/infrastructure`], {
                relativeTo: this.route,
            });
        } else {
            this.router.navigate([`${uid}/footprint/terminals`], {
                relativeTo: this.route,
            });
        }
    }

    itemNoteOpened(digitalService: DigitalService) {
        this.sidebarVisible = true;
        this.selectedDigitalService = digitalService;
    }

    itemDelete(uid: string) {
        this.global.setLoading(true);
        this.digitalServicesData
            .delete(uid)
            .pipe(
                takeUntilDestroyed(this.destroyRef),
                finalize(() => {
                    this.global.setLoading(false);
                }),
            )
            .subscribe(() => this.retrieveDigitalServices());
    }

    noteSaveValue(event: any) {
        // Get digital services data.
        this.digitalServicesData.get(this.selectedDigitalService.uid).subscribe((res) => {
            // update note
            res.note = {
                content: event,
            };
            this.digitalServicesData.update(res).subscribe(() => {
                this.sidebarVisible = false;
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.save"),
                    sticky: false,
                });
                this.selectedDigitalService.note = {
                    content: event,
                };
            });
        });
    }

    noteDelete() {
        // Get digital services data.
        this.digitalServicesData.get(this.selectedDigitalService.uid).subscribe((res) => {
            // update note
            res.note = undefined;
            this.digitalServicesData.update(res).subscribe(() => {
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.delete"),
                    sticky: false,
                });
            });
        });
        this.selectedDigitalService.note = undefined;
    }
}
