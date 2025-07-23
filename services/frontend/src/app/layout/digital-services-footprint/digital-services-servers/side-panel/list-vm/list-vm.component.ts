/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, inject, ViewChild } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { UserService } from "src/app/core/service/business/user.service";

import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { PanelAddVmComponent } from "../add-vm/add-vm.component";

@Component({
    selector: "app-panel-list-vm",
    templateUrl: "./list-vm.component.html",
})
export class PanelListVmComponent {
    @ViewChild("vmSidePanel", { static: false })
    vmSidePanel!: PanelAddVmComponent;

    public digitalServiceStore = inject(DigitalServiceStoreService);

    addVMPanelVisible: boolean = false;
    index: number | undefined;
    headerFields = computed(() => {
        const { type } = this.digitalServiceStore.server();
        return type === "Compute"
            ? ["name", "quantity", "vCpu", "annualOperatingTime"]
            : ["name", "quantity", "disk", "annualOperatingTime"];
    });
    vmData = computed(() => {
        return [...this.digitalServiceStore.server().vm];
    });
    server = computed(() => {
        return this.digitalServiceStore.server();
    });

    constructor(
        private readonly digitalServiceBusiness: DigitalServiceBusinessService,
        private readonly router: Router,
        private readonly route: ActivatedRoute,
        public userService: UserService,
    ) {}

    resetIndex() {
        this.index = undefined;
        this.addVMPanelVisible = true;
    }

    setIndex(rowIndex: number) {
        this.index = rowIndex;
        this.addVMPanelVisible = true;
    }

    deleteVm(rowNumber: number) {
        const server = this.digitalServiceStore.server();

        if (rowNumber !== undefined && server.vm !== undefined) {
            server.vm.splice(rowNumber, 1);
            this.digitalServiceStore.setServer(server);
        }
    }

    previousStep() {
        this.router.navigate(["../panel-parameters"], { relativeTo: this.route });
    }

    async submitServer() {
        this.digitalServiceBusiness.submitServerForm(
            this.digitalServiceStore.server(),
            this.digitalServiceStore.digitalService(),
        );
        this.close();
    }

    close() {
        this.digitalServiceBusiness.closePanel();
    }

    openSidePanel() {
        this.digitalServiceBusiness.openPanel();
    }
}
