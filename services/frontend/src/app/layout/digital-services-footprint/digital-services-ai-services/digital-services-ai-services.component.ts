/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, inject, input, OnInit, signal } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { DrawerModule } from "primeng/drawer";
import { firstValueFrom, lastValueFrom } from "rxjs";
import { DigitalServiceAIServiceConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { MapString } from "src/app/core/interfaces/generic.interfaces";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { UserService } from "src/app/core/service/business/user.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServiceTableComponent } from "../../common/digital-service-table/digital-service-table.component";
import { DigitalServicesAIServicesSidePanelComponent } from "./digital-services-ai-services-side-panel/digital-services-ai-services-side-panel.component";

@Component({
    selector: "app-digital-services-ai-services",
    templateUrl: "./digital-services-ai-services.component.html",
    providers: [MessageService],
    standalone: true,
    imports: [
        DigitalServiceTableComponent,
        DrawerModule,
        DigitalServicesAIServicesSidePanelComponent,
        TranslateModule,
    ],
})
export class DigitalServicesAIServicesComponent implements OnInit {
    private readonly inVirtualEquipmentsService = inject(InVirtualEquipmentsService);
    protected digitalServiceStore = inject(DigitalServiceStoreService);

    dsVersionUid = input("");

    sidebarVisible: boolean = false;
    sidebarPurpose: string = "";
    ai: DigitalServiceAIServiceConfig = {} as DigitalServiceAIServiceConfig;
    digitalServiceUid = "";

    virtualEquipments = signal<InVirtualEquipmentRest[]>([]);

    aiServices = computed(() => {
        return this.digitalServiceStore
            .inVirtualEquipments()
            .filter((server) => server.infrastructureType === "AI_SERVICES")
            .map((server: InVirtualEquipmentRest) =>
                this.toDigitalServiceAIServiceConfig(
                    server,
                    this.digitalServiceStore.countryMap(),
                ),
            );
    });

    headerFields = ["name", "aiProvider", "aiModel", "tokenQuantity", "locationValue"];

    constructor(
        public userService: UserService,
        private readonly router: Router,
    ) {}

    ngOnInit(): void {
        this.digitalServiceUid = this.router.url.split("/")[6];
        void this.getAIServices();
    }

    async getAIServices() {
        await this.digitalServiceStore.initInVirtualEquipments(this.digitalServiceUid);
    }

    changeSidebar(event: boolean) {
        this.sidebarVisible = event;
    }

    setItem(event: any) {
        const index = event.index;
        delete event.index;

        this.ai = { ...event };
        this.ai.idFront = index;
    }

    async deleteItem(event: DigitalServiceAIServiceConfig) {
        await firstValueFrom(
            this.inVirtualEquipmentsService.delete(event.id, this.dsVersionUid()),
        );
        await this.getAIServices();
        this.digitalServiceStore.setEnableCalcul(true);
    }

    setAIservices(ai: DigitalServiceAIServiceConfig, index: number) {
        this.ai = { ...ai };
        this.ai.idFront = index;
    }

    resetAIServices() {
        this.ai = {} as DigitalServiceAIServiceConfig;
    }

    async updateAIServices(ai: DigitalServiceAIServiceConfig) {
        if (ai.id) {
            await firstValueFrom(
                this.inVirtualEquipmentsService.update(this.toInVirtualEquipmentRest(ai)),
            );
        } else {
            await firstValueFrom(
                this.inVirtualEquipmentsService.create(this.toInVirtualEquipmentRest(ai)),
            );
        }
        await this.getAIServices();
        this.digitalServiceStore.setEnableCalcul(true);
    }

    async deleteAIServices(ai: DigitalServiceAIServiceConfig) {
        this.ai.digitalServiceUid = this.digitalServiceUid;
        await lastValueFrom(
            this.inVirtualEquipmentsService.delete(ai.id, ai.digitalServiceUid),
        );
        await this.getAIServices();
        this.digitalServiceStore.setEnableCalcul(true);
    }

    //mapper
    toInVirtualEquipmentRest(ai: DigitalServiceAIServiceConfig): InVirtualEquipmentRest {
        return {
            id: ai.id,
            digitalServiceUid: ai.digitalServiceUid,
            digitalServiceVersionUid: this.dsVersionUid(),
            name: ai.name,
            infrastructureType: "AI_SERVICES",
            quantity: ai.tokenQuantity,
            provider: ai.aiProvider,
            instanceType: ai.aiModel,
            location: ai.locationValue,
        };
    }

    toDigitalServiceAIServiceConfig(
        virtualEq: InVirtualEquipmentRest,
        countryMap: MapString,
    ): DigitalServiceAIServiceConfig {
        return {
            id: virtualEq.id,
            digitalServiceUid: virtualEq.digitalServiceUid!,
            name: virtualEq.name,
            tokenQuantity: virtualEq.quantity,
            aiProvider: virtualEq.provider!,
            aiModel: virtualEq.instanceType!,
            location: {
                code: virtualEq.location,
                name: countryMap[virtualEq.location] || virtualEq.location,
            },
            locationValue: countryMap[virtualEq.location] || virtualEq.location,
        };
    }

    focusAIButton() {
        setTimeout(() => {
            const id =
                this.ai.idFront !== undefined ? `add-ai${this.ai.idFront}` : "add-ai";

            document.getElementById(id)?.querySelector("button")?.focus();
        }, 400);
    }
}
