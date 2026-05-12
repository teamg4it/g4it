import { Component, inject, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { Subscription } from "rxjs";
import { DigitalServicesTerminalsComponent } from "../digital-services-terminals/digital-services-terminals.component";
import { DigitalServicesNetworksComponent } from "../digital-services-networks/digital-services-networks.component";
import { DigitalServicesServersComponent } from "../digital-services-servers/digital-services-servers.component";
import { DigitalServicesCloudServicesComponent } from "../digital-services-cloud-services/digital-services-cloud-services.component";

@Component({
    selector: "app-digital-services-resources",
    templateUrl: "./digital-services-resources.component.html",
    standalone: true,
    imports: [
        DigitalServicesTerminalsComponent,
        DigitalServicesNetworksComponent,
        DigitalServicesServersComponent,
        DigitalServicesCloudServicesComponent,
    ],
})
export class DigitalServicesResourcesComponent implements OnInit, OnDestroy {
    private readonly route = inject(ActivatedRoute);
    dsVersionUid = "";
    sub!: Subscription;
    ngOnInit(): void {
        this.setDsVerId();
    }

    async setDsVerId(): Promise<void> {
        this.sub = this.route.parent!.paramMap.subscribe((params) => {
            this.dsVersionUid = params.get("digitalServiceVersionId") ?? "";
        });
    }

    ngOnDestroy(): void {
        this.sub?.unsubscribe();
    }
}
