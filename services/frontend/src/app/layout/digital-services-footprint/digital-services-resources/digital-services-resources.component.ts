import { Component, inject, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { Subscription } from "rxjs";
import type { ResourceCategories } from "../digital-services-footprint-dashboard/resource-button/resource-button.component";
import { ResourceSidebarComponent } from "../../common/resource-sidebar/resource-sidebar.component";
import { DigitalServicesAIServicesComponent } from "../digital-services-ai-services/digital-services-ai-services.component";
import { DigitalServicesCloudServicesComponent } from "../digital-services-cloud-services/digital-services-cloud-services.component";
import { DigitalServicesNetworksComponent } from "../digital-services-networks/digital-services-networks.component";
import { DigitalServicesServersComponent } from "../digital-services-servers/digital-services-servers.component";
import { DigitalServicesTerminalsComponent } from "../digital-services-terminals/digital-services-terminals.component";

@Component({
    selector: "app-digital-services-resources",
    templateUrl: "./digital-services-resources.component.html",
    standalone: true,
    imports: [
        DigitalServicesTerminalsComponent,
        DigitalServicesNetworksComponent,
        DigitalServicesServersComponent,
        DigitalServicesCloudServicesComponent,
        DigitalServicesAIServicesComponent,
        ResourceSidebarComponent,
    ],
})
export class DigitalServicesResourcesComponent implements OnInit, OnDestroy {
    private readonly route = inject(ActivatedRoute);
    selectedCategory: ResourceCategories = "devices";
    isSidebarCollapsed = false;
    dsVersionUid = "";
    sub!: Subscription;

    ngOnInit(): void {
        this.setDsVerId();
    }

    selectCategory(category: ResourceCategories): void {
        this.selectedCategory = category;
    }

    toggleSidebar(): void {
        this.isSidebarCollapsed = !this.isSidebarCollapsed;
    }

    setDsVerId(): void {
        this.sub = this.route.parent!.paramMap.subscribe((params) => {
            this.dsVersionUid = params.get("digitalServiceVersionId") ?? "";
        });
    }

    ngOnDestroy(): void {
        this.sub?.unsubscribe();
    }
}
