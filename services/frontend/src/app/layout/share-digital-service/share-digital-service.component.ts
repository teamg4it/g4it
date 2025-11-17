import { Component, inject, OnInit, signal } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MenuItem } from "primeng/api";
import { firstValueFrom, lastValueFrom } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { MapString } from "src/app/core/interfaces/generic.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { ShareDigitalServiceDataService } from "src/app/core/service/data/share-digital-service-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
@Component({
    selector: "app-share-digital-service",
    templateUrl: "./share-digital-service.component.html",
})
export class ShareDigitalServiceComponent implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly shareDigitalServiceDataService = inject(
        ShareDigitalServiceDataService,
    );
    private readonly digitalServicesDataService = inject(DigitalServicesDataService);
    private readonly digitalServiceStoreService = inject(DigitalServiceStoreService);
    private readonly translate = inject(TranslateService);
    private readonly router = inject(Router);
    digitalService: DigitalService = {} as DigitalService;
    tabItems: MenuItem[] | undefined;
    selectedTab: any;
    isMobile = signal(false);

    ngOnInit(): void {
        this.initComponent();
    }

    async initComponent(): Promise<void> {
        this.setMobileView();
        const uid = this.route.snapshot.paramMap.get("id");
        const shareToken = this.route.snapshot.paramMap.get("share-token")!;
        const digitalService = await lastValueFrom(
            this.digitalServicesDataService.getDs(uid!, shareToken),
        );
        this.digitalService = digitalService;

        this.updateTabItems();
        if (this.isMobile()) {
            this.setSelectedTabFromRoute();
        }

        const inPhysicalEquipments = await firstValueFrom(
            this.shareDigitalServiceDataService.getSharedPhysicalEquipments(
                uid,
                shareToken,
            ),
        );
        this.digitalServiceStoreService.setInPhysicalEquipments(inPhysicalEquipments);
        const inVirtualEquipments = await firstValueFrom(
            this.shareDigitalServiceDataService.getSharedVirtualEquipments(
                uid,
                shareToken,
            ),
        );
        this.digitalServiceStoreService.setInVirtualEquipments(inVirtualEquipments);

        const dataCenters = await firstValueFrom(
            this.shareDigitalServiceDataService.getInSharedDataCenters(uid!, shareToken),
        );
        this.digitalServiceStoreService.setInDatacenters(dataCenters);

        const referentialData: any = await firstValueFrom(
            this.shareDigitalServiceDataService.getReferentialData(uid!, shareToken),
        );
        this.digitalServiceStoreService.setNetworkTypes(referentialData.networkTypes);
        this.digitalServiceStoreService.setTerminalDeviceTypes(
            referentialData.terminalTypes,
        );

        this.digitalServiceStoreService.setServerTypes([
            ...referentialData.computeServerTypes,
            ...referentialData.storageServerTypes,
        ]);

        const countryMap: MapString = {};
        for (const key in referentialData.countries) {
            countryMap[referentialData.countries[key]] = key;
        }
        this.digitalServiceStoreService.setCountryMap(countryMap);
    }

    updateTabItems() {
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

    onMenuTabChange(event: any) {
        this.selectedTab = event;
    }

    private setMobileView(): void {
        this.isMobile.set(window.innerWidth < 560);
        window.addEventListener("resize", () => {
            this.isMobile.set(window.innerWidth < 560);
        });
    }

    onTabChange(tab: any): void {
        if (tab?.routerLink) {
            this.router.navigate([tab.routerLink], { relativeTo: this.route });
        }
    }

    private setSelectedTabFromRoute(): void {
        console.log(this.tabItems);
        if (!this.tabItems) return;
        // Find the tab whose routerLink matches the last segment of the current route
        const currentSegment = this.route.snapshot.firstChild?.routeConfig?.path;
        console.log(this.tabItems.find((tab) => tab.routerLink === currentSegment));
        console.log(this.tabItems);
        this.selectedTab =
            this.tabItems.find((tab) => tab.routerLink === currentSegment) ||
            this.tabItems[0];
    }
}
