/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, EventEmitter, inject, Input, Output } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { MessageService } from "primeng/api";
import { ServerVM } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-panel-add-vm",
    templateUrl: "./add-vm.component.html",
    providers: [MessageService],
})
export class PanelAddVmComponent {
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() index: number | undefined;
    @Input() addVMPanelVisible: boolean = false;
    @Output() addVMPanelVisibleChange: EventEmitter<boolean> = new EventEmitter();

    server = computed(() => {
        return this.digitalServiceStore.server();
    });

    vm: ServerVM = {} as ServerVM;

    vcpuControl = this._formBuilder.control(0, [Validators.required]);
    diskControl = this._formBuilder.control(0, [Validators.required]);
    quantityControl = this._formBuilder.control(0, [Validators.required]);
    addVmForm = this._formBuilder.group({
        name: ["", Validators.required],
        vcpu: this.vcpuControl,
        disk: this.diskControl,
        quantity: this.quantityControl,
        opratingTime: [0, [Validators.required]],
    });

    isValueTooHigh: boolean = false;

    constructor(
        private readonly _formBuilder: FormBuilder,
        public userService: UserService,
    ) {}

    ngOnInit() {
        if (this.index === undefined) {
            const num = (this.server().vm?.length || 0) + 1;
            this.vm = {
                uid: "",
                name: "VM " + num,
                vCpu: 0,
                disk: 0,
                quantity: 1,
                annualOperatingTime: 8760,
            };
        } else {
            this.vm = { ...this.server().vm[this.index] };
        }
    }

    verifyValue() {
        const sum = this.sum();

        const value = this.addVmForm.value;
        const totalVCpu = this.server().totalVCpu || 0;
        const totalDisk = this.server().totalDisk || 0;

        let isValueTooHigh = false;
        if (this.server().type === "Compute") {
            isValueTooHigh = (value.vcpu || 0) * (value.quantity || 1) + sum > totalVCpu;

            if (isValueTooHigh) {
                this.vcpuControl.setErrors({
                    ...this.vcpuControl?.errors,
                    isValueTooHigh,
                });
            } else {
                delete this.vcpuControl.errors?.["isValueTooHigh"];
                this.vcpuControl.updateValueAndValidity();
            }
        } else if (this.server().type === "Storage") {
            isValueTooHigh = (value.disk || 0) * (value.quantity || 1) + sum > totalDisk;

            if (isValueTooHigh) {
                this.diskControl.setErrors({
                    ...this.diskControl?.errors,
                    isValueTooHigh,
                });
            } else {
                delete this.diskControl.errors?.["isValueTooHigh"];
                this.diskControl.updateValueAndValidity();
            }
        }

        if (value.quantity == 0) {
            this.quantityControl.setErrors({
                ...this.quantityControl?.errors,
                isQuantityTooLow: true,
            });
        } else {
            delete this.quantityControl.errors?.["isQuantityTooLow"];
            this.quantityControl.updateValueAndValidity();
        }
    }

    sum() {
        let sum: number = 0;
        const type = this.server().type;

        const field = type === "Compute" ? "vCpu" : "disk";

        this.server().vm.forEach((vm) => {
            if (this.vm.name !== vm.name) {
                sum += vm[field] * vm.quantity;
            }
        });
        return sum;
    }

    submitFormData() {
        // If the vm with the uid exists, update it; otherwise, add the new vm
        const server = this.server();
        if (this.index !== undefined) {
            server.vm[this.index] = this.vm;
        } else {
            server.vm.push(this.vm);
        }
        this.digitalServiceStore.setServer(server);
        this.close();
    }

    close() {
        this.addVMPanelVisibleChange.emit(false);
    }
}
