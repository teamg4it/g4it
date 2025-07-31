/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, inject, ViewChild } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { firstValueFrom } from "rxjs";
import {
    DigitalServiceServerConfig,
    Host,
    ServerDC,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { InDatacentersService } from "src/app/core/service/data/in-out/in-datacenters.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import * as uuid from "uuid";
import PanelDatacenterComponent from "../add-datacenter/datacenter.component";

@Component({
    selector: "app-side-panel-server-parameters",
    templateUrl: "./server-parameters.component.html",
    providers: [MessageService],
})
export class PanelServerParametersComponent {
    public translate = inject(TranslateService);
    public digitalServiceStore = inject(DigitalServiceStoreService);
    private readonly inDatacentersService = inject(InDatacentersService);

    @ViewChild("childSidePanel", { static: false })
    childSidePanel!: PanelDatacenterComponent;

    addSidebarVisible: boolean = false;

    totalVmvCpu = 0;
    serverForm = this._formBuilder.group({
        host: ["", Validators.required],
        datacenter: [{ name: "", location: "", pue: 1 }, Validators.required],
        quantity: [0, [Validators.required]],
        vcpu: [0, [Validators.required]],
        disk: [0, [Validators.required]],
        lifespan: [0, [Validators.required]],
        electricityConsumption: [0, [Validators.required]],
        operatingTime: [8760, [Validators.required]],
    });

    datacenterOptions = computed(() => {
        return this.digitalServiceStore.inDatacenters().map((datacenter) => {
            return {
                location: datacenter.location,
                name: datacenter.name,
                pue: datacenter.pue,
                displayLabel: datacenter.displayLabel,
                uid: "",
            } as ServerDC;
        });
    });
    indexDatacenter: number = 0;
    dataInitialized: boolean = false;
    current = {
        host: {} as Host,
        datacenter: {} as ServerDC,
    };

    serverTypes = computed(() => {
        return this.digitalServiceStore
            .serverTypes()
            .filter((st) => st.type === this.digitalServiceStore.server().type);
    });

    server = computed(() => {
        const srv = this.digitalServiceStore.server();
        const datacenters = this.datacenterOptions();
        const serverTypes = this.digitalServiceStore.serverTypes();

        if (srv.id === undefined && srv.host === undefined) {
            if (srv.type === "Compute") {
                srv.host =
                    serverTypes[
                        serverTypes.findIndex((x) => x.value === "Server Compute M")
                    ];
            } else if (srv.type === "Storage") {
                srv.host =
                    serverTypes[
                        serverTypes.findIndex((x) => x.value === "Server Storage M")
                    ];
            }
        }
        this.current.host = srv.host!;
        this.current.datacenter = srv.datacenter!;

        if (!srv.totalVCpu && srv.type === "Compute") {
            srv.totalVCpu = srv.host?.characteristic.find(
                (c) => c.code === "vCPU",
            )?.value;
        }
        if (!srv.annualElectricConsumption) {
            srv.annualElectricConsumption = srv.host?.characteristic.find(
                (c) => c.code === "annualElectricityConsumption",
            )?.value;
        }
        if (!srv.lifespan) {
            srv.lifespan = srv.host?.characteristic.find(
                (c) => c.code === "lifespan",
            )?.value;
        }
        if (!srv.totalDisk && srv.type === "Storage") {
            srv.totalDisk = srv.host?.characteristic.find(
                (c) => c.code === "disk",
            )?.value;
        }

        let datacenterName = this.current.datacenter.name
            ? srv.datacenter?.name
            : "Default DC";

        const datacenter = datacenters.find((x) => x.name === datacenterName);
        srv.datacenter = datacenter;

        this.current.datacenter = datacenter!;

        if (srv.quantity === -1) {
            srv.quantity = 1;
            srv.annualOperatingTime = 8760;
        }

        this.verifyValue(srv);
        return srv;
    });

    createLabelKey = computed(() => {
        if (this.server().mutualizationType === "Dedicated" && !this.server().id) {
            return "common.add";
        }
        if (this.server().mutualizationType === "Dedicated" && this.server().id) {
            return "common.save";
        }
        if (this.server().mutualizationType === "Shared") {
            return "common.next";
        }
        return "common.add";
    });

    constructor(
        private readonly digitalServiceBusiness: DigitalServiceBusinessService,
        private readonly _formBuilder: FormBuilder,
        private readonly router: Router,
        private readonly route: ActivatedRoute,
        public userService: UserService,
    ) {}

    setDefaultForm(type: string) {
        if (!this.current.host || !this.current.host.code) return;
        this.serverForm.controls["electricityConsumption"].setValue(
            this.current.host.characteristic.find(
                (c) => c.code === "annualElectricityConsumption",
            )?.value!,
        );

        this.serverForm.controls["lifespan"].setValue(
            this.current.host.characteristic.find((c) => c.code === "lifespan")?.value!,
        );

        if (type === "Compute") {
            this.serverForm.controls["vcpu"].setValue(
                this.current.host.characteristic.find((c) => c.code === "vCPU")?.value!,
            );
        } else if (type === "Storage") {
            this.serverForm.controls["disk"].setValue(
                this.current.host.characteristic.find((c) => c.code === "disk")?.value!,
            );
        }
    }

    changeServer() {
        const server = this.server();
        this.setDefaultForm(server.type);

        server.host = this.current.host;

        this.digitalServiceStore.setServer(server);
    }

    verifyValue(server: DigitalServiceServerConfig) {
        this.totalVmvCpu = 0;
        const vcputControl = this.serverForm.get("vcpu");
        if (server.vm?.length) {
            this.totalVmvCpu = server.vm.reduce(
                (acc, vm) => acc + vm.vCpu * vm.quantity,
                0,
            );
            if (
                server?.totalVCpu !== null &&
                (server.totalVCpu ?? 0) < this.totalVmvCpu
            ) {
                vcputControl?.setErrors({
                    ...vcputControl?.errors,
                    isValueTooHigh: true,
                });
                vcputControl?.markAsDirty();
            } else {
                delete vcputControl?.errors?.["isValueTooHigh"];
                vcputControl?.updateValueAndValidity();
            }
        } else {
            delete vcputControl?.errors?.["isValueTooHigh"];
            vcputControl?.updateValueAndValidity();
        }
    }

    async addDatacenter(event: ServerDC) {
        const digitalServiceUid = this.digitalServiceStore.digitalService().uid;
        const datacenterName = `${event.name}|${uuid.v4()}`;
        await firstValueFrom(
            this.inDatacentersService.create({
                location: event.location,
                name: datacenterName,
                pue: event.pue,
                digitalServiceUid,
            }),
        );
        const inDatacenters = await firstValueFrom(
            this.inDatacentersService.get(digitalServiceUid),
        );
        this.digitalServiceStore.setInDatacenters(inDatacenters);
        const server = this.digitalServiceStore.server();
        server.datacenter = inDatacenters.find(
            (datacenter) => datacenter.name == datacenterName,
        );
        this.digitalServiceStore.setServer(server);
    }

    previousStep() {
        this.digitalServiceStore.setServer(this.server());
        this.router.navigate(["../panel-create"], { relativeTo: this.route });
    }

    async nextStep() {
        const server = this.server();

        server.host = this.current.host;
        server.datacenter = this.current.datacenter;

        this.digitalServiceStore.setServer(server);
        if (this.server().mutualizationType === "Dedicated") {
            this.digitalServiceBusiness.submitServerForm(
                this.server(),
                this.digitalServiceStore.digitalService(),
            );
            this.close();
        } else if (this.server().mutualizationType === "Shared") {
            this.router.navigate(["../panel-vm"], { relativeTo: this.route });
        }
    }

    close() {
        this.digitalServiceStore.setServer({} as DigitalServiceServerConfig);
        this.digitalServiceBusiness.closePanel();
    }
}
