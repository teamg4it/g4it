/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { AsyncPipe, NgClass } from "@angular/common";
import { Component, computed, inject, ViewChild } from "@angular/core";
import {
    AbstractControl,
    FormBuilder,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { DrawerModule } from "primeng/drawer";
import { InputNumberModule } from "primeng/inputnumber";
import { ScrollPanel } from "primeng/scrollpanel";
import { SelectModule } from "primeng/select";
import { firstValueFrom } from "rxjs";
import { xssFormGroupValidator } from "src/app/core/custom-validators/xss-validator";
import {
    DigitalServiceServerConfig,
    Host,
    ServerDC,
    ServerVM,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { InDatacentersService } from "src/app/core/service/data/in-out/in-datacenters.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import * as uuid from "uuid";
import { AutofocusDirective } from "../../../../../core/directives/auto-focus.directive";
import { PanelDatacenterComponent } from "../add-datacenter/datacenter.component";

@Component({
    selector: "app-side-panel-server-parameters",
    templateUrl: "./server-parameters.component.html",
    providers: [MessageService],
    standalone: true,
    imports: [
        AutofocusDirective,
        FormsModule,
        ReactiveFormsModule,
        SelectModule,
        Button,
        DrawerModule,
        PanelDatacenterComponent,
        NgClass,
        InputNumberModule,
        AsyncPipe,
        TranslatePipe,
        ScrollPanel,
    ],
})
export class PanelServerParametersComponent {
    public translate = inject(TranslateService);
    public digitalServiceStore = inject(DigitalServiceStoreService);
    private readonly inDatacentersService = inject(InDatacentersService);

    @ViewChild("childSidePanel", { static: false })
    childSidePanel!: PanelDatacenterComponent;

    addSidebarVisible: boolean = false;

    totalVmvCpu = 0;
    serverForm = this._formBuilder.group(
        {
            host: ["", Validators.required],
            datacenter: [{ name: "", location: "", pue: 1 }, Validators.required],
            quantity: [0, [Validators.required]],
            vcpu: [0, [Validators.required]],
            disk: [0, [Validators.required]],
            vram: [0, [Validators.required]],
            lifespan: [0, [Validators.required]],
            electricityConsumption: [0, [Validators.required]],
            operatingTime: [8760, [Validators.required]],
        },
        {
            validators: [xssFormGroupValidator()],
            updateOn: "blur",
        },
    );

    datacenterOptions = computed<ServerDC[]>(() => {
        return this.digitalServiceStore.inDatacenters().map((datacenter) => {
            return {
                location: datacenter.location,
                name: datacenter.name,
                pue: datacenter.pue,
                displayLabel: datacenter.displayLabel,
                uid: "",
            };
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

        const hostfound = this.serverTypes().find((st) => st.value === srv.host?.value);
        if ((srv.id === undefined && srv.host === undefined) || !hostfound) {
            srv.host = this.defaultHostForType(srv.type, serverTypes);
        }
        this.current.host = srv.host!;
        this.current.datacenter = srv.datacenter!;

        this.applyDefaultCharacteristics(srv);

        const datacenter = this.resolveDatacenter(srv, datacenters);
        srv.datacenter = datacenter;
        this.current.datacenter = datacenter!;

        if (srv.quantity === -1) {
            srv.quantity = 1;
            srv.annualOperatingTime = 8760;
        }

        this.verifyValue(srv);
        return srv;
    });

    private defaultHostForType(type: string | undefined, serverTypes: Host[]): Host {
        let hostName: string;
        if (type === "Compute") {
            hostName = "Server Compute M";
        } else if (type === "Storage") {
            hostName = "Server Storage M";
        } else {
            hostName = "Medium AI Server";
        }
        return serverTypes[serverTypes.findIndex((x) => x.value === hostName)];
    }

    private applyDefaultCharacteristics(srv: DigitalServiceServerConfig) {
        if (!srv.totalVCpu && srv.type === "Compute") {
            srv.totalVCpu = srv.host?.characteristic.find(
                (c) => c.code === "vCPU",
            )?.value;
        }
        if (!srv.totalVram && srv.type === "AI") {
            srv.totalVram = srv.host?.characteristic.find(
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
    }

    private resolveDatacenter(
        srv: DigitalServiceServerConfig,
        datacenters: ServerDC[],
    ): ServerDC {
        let datacenterName: string | undefined;
        if (this.current.datacenter.name) {
            datacenterName = srv.datacenter?.name;
        } else {
            datacenterName = "Default DC";
        }
        return datacenters.find((x) => x.name === datacenterName)!;
    }

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
        if (!this.current?.host?.code) return;
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
        } else {
            this.serverForm.controls["vram"].setValue(
                this.current.host.characteristic.find((c) => c.code === "vCPU")?.value!,
            );
        }
    }

    changeServer(event: { value: Host }) {
        this.current.host = event.value;
        const server = this.server();
        this.setDefaultForm(server.type);

        server.host = this.current.host;
        this.digitalServiceStore.setServer(server);
    }

    verifyValue(server: DigitalServiceServerConfig) {
        this.totalVmvCpu = 0;
        let vcputControl: AbstractControl | null;
        if (server.type === "Compute") {
            vcputControl = this.serverForm.get("vcpu");
        } else if (server.type === "Storage") {
            vcputControl = this.serverForm.get("disk");
        } else {
            vcputControl = this.serverForm.get("vram");
        }
        if (!vcputControl) return;

        let isValueTooHigh = false;
        if (server.vm?.length) {
            this.totalVmvCpu = server.vm.reduce(
                (acc, vm) => acc + this.getVmValue(server, vm) * vm.quantity,
                0,
            );
            isValueTooHigh = (this.getServerTotal(server) ?? 0) < this.totalVmvCpu;
        }
        this.setControlError(vcputControl, "isValueTooHigh", isValueTooHigh);
        if (isValueTooHigh) {
            vcputControl.markAsDirty();
        }
    }

    private getServerTotal(server: DigitalServiceServerConfig): number | undefined {
        if (server.type === "Compute") {
            return server.totalVCpu;
        } else if (server.type === "Storage") {
            return server.totalDisk;
        } else {
            return server.totalVram;
        }
    }

    private getVmValue(server: DigitalServiceServerConfig, vm: ServerVM): number {
        if (server.type === "Compute") {
            return vm.vCpu;
        } else if (server.type === "Storage") {
            return vm.disk;
        } else {
            return vm.vRam!;
        }
    }

    // Merges/clears a single custom error key without wiping errors set by the control's own validators.
    private setControlError(
        control: AbstractControl,
        errorKey: string,
        hasError: boolean,
    ) {
        const { [errorKey]: _removed, ...remainingErrors } = control.errors ?? {};
        if (hasError) {
            control.setErrors({ ...remainingErrors, [errorKey]: true });
        } else if (Object.keys(remainingErrors).length) {
            control.setErrors(remainingErrors);
        } else {
            control.setErrors(null);
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
        this.digitalServiceStore.setServer({
            ...this.server(),
            annualElectricConsumption: undefined,
            lifespan: undefined,
            totalDisk: undefined,
            totalVCpu: undefined,
            totalVram: undefined,
        });

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
