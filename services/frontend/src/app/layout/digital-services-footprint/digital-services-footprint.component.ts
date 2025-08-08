/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, ElementRef, inject, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MenuItem } from "primeng/api";
import { firstValueFrom, lastValueFrom } from "rxjs";
import { sortByProperty } from "sort-by-property";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { InPhysicalEquipmentRest } from "src/app/core/interfaces/input.interface";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InDatacentersService } from "src/app/core/service/data/in-out/in-datacenters.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-digital-services-footprint",
    templateUrl: "./digital-services-footprint.component.html",
})
export class DigitalServicesFootprintComponent implements OnInit {
    private readonly global = inject(GlobalStoreService);
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);
    private readonly inDatacentersService = inject(InDatacentersService);

    digitalService: DigitalService = {} as DigitalService;
    inPhysicalEquipments: InPhysicalEquipmentRest[] = [];
    isEcoMindAi: boolean = false;
    tabItems: MenuItem[] | undefined;

    @ViewChild("footprintHeader", { read: ElementRef }) headerRef!: ElementRef;
    @ViewChild("footprintFooter", { read: ElementRef }) footerRef!: ElementRef;
    headerHeight = 0;
    footerHeight = 0;

    constructor(
        private readonly digitalServicesData: DigitalServicesDataService,
        private readonly digitalBusinessService: DigitalServiceBusinessService,
        private readonly route: ActivatedRoute,
        private readonly translate: TranslateService,
    ) {}

    async ngOnInit(): Promise<void> {
        this.global.setLoading(true);

        const uid = this.route.snapshot.paramMap.get("digitalServiceId") ?? "";
        const digitalService = await lastValueFrom(this.digitalServicesData.get(uid));
        // If the digital service is not found, 404 is catched by the interceptor.
        // Therefore we can continue without those verifications.
        this.digitalService = digitalService;

        // Retrieving the isAi parameter from the URL

        this.isEcoMindAi = this.digitalService.isAi ?? false;
        this.updateTabItems();

        this.digitalServiceStore.setDigitalService(this.digitalService);
        if (!this.isEcoMindAi) {
            await this.digitalServiceStore.initInPhysicalEquipments(uid);
            await this.digitalServiceStore.initInVirtualEquipments(uid);

            let inDatacenters = await firstValueFrom(this.inDatacentersService.get(uid));
            if (inDatacenters.length === 0) {
                await firstValueFrom(
                    this.inDatacentersService.create({
                        location: "France",
                        name: "Default DC",
                        pue: 1.5,
                        digitalServiceUid: uid,
                    }),
                );
                inDatacenters = await firstValueFrom(this.inDatacentersService.get(uid));
            }

            this.digitalServiceStore.setInDatacenters(inDatacenters);
            const referentials = await firstValueFrom(
                this.digitalServicesData.getNetworkReferential(),
            );
            this.digitalServiceStore.setNetworkTypes(referentials);

            const terminalReferentials = await firstValueFrom(
                this.digitalServicesData.getDeviceReferential(),
            );
            this.digitalServiceStore.setTerminalDeviceTypes(terminalReferentials);

            const serverHostRefCompute = await firstValueFrom(
                this.digitalServicesData.getHostServerReferential("Compute"),
            );
            const serverHostRefStorage = await firstValueFrom(
                this.digitalServicesData.getHostServerReferential("Storage"),
            );
            const shortCuts = [
                ...serverHostRefCompute.filter((item) =>
                    item.value.startsWith("Server "),
                ),
                ...serverHostRefStorage.filter((item) =>
                    item.value.startsWith("Server "),
                ),
            ].sort(sortByProperty("value", "desc"));

            this.digitalServiceStore.setServerTypes([
                ...shortCuts,
                ...serverHostRefCompute
                    .filter((item) => !item.value.startsWith("Server "))
                    .sort(sortByProperty("value", "asc")),
                ...serverHostRefStorage
                    .filter((item) => !item.value.startsWith("Server "))
                    .sort(sortByProperty("value", "asc")),
            ]);
        }
        this.global.setLoading(false);

        this.digitalBusinessService.initCountryMap();
    }

    ngAfterViewInit() {
        this.updateHeights();
        window.addEventListener("resize", () => this.updateHeights());
    }

    ngOnDestroy() {
        window.removeEventListener("resize", () => this.updateHeights());
    }

    updateHeights = () => {
        this.headerHeight = this.headerRef?.nativeElement.offsetHeight;
        this.footerHeight = this.footerRef?.nativeElement.offsetHeight;
    };

    updateTabItems() {
        if (this.isEcoMindAi) {
            this.tabItems = [
                {
                    label: this.translate.instant(
                        "digital-services.visualize-parameters",
                    ),
                    routerLink: "ecomind-parameters",
                    id: "ecomind-parameters",
                },
                {
                    label: this.translate.instant("digital-services.visualize-results"),
                    routerLink: "dashboard",
                    visible: this.digitalService.lastCalculationDate !== undefined,
                    id: "visualize",
                },
            ];
        } else {
            this.tabItems = [
                {
                    label: this.translate.instant("digital-services.view-resources"),
                    routerLink: "resources",
                    id: "resources",
                },
                {
                    label: this.translate.instant("digital-services.visualize-results"),
                    routerLink: "dashboard",
                    visible: this.digitalService.lastCalculationDate !== undefined,
                    id: "visualize",
                },
            ];
        }
    }

    updateEnableCalculation(event: boolean) {
        setTimeout(() => {
            this.updateHeights();
        }, 0);
    }

    async updateDigitalService() {
        // digital service is already updated thanks to data binding
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.update(this.digitalService),
        );

        this.digitalServiceStore.initInPhysicalEquipments(this.digitalService.uid);
        this.digitalServiceStore.initInVirtualEquipments(this.digitalService.uid);
        this.updateTabItems();
    }
}
