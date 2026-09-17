/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { AsyncPipe } from "@angular/common";
import {
    Component,
    computed,
    EventEmitter,
    inject,
    Input,
    OnInit,
    Output,
} from "@angular/core";
import {
    AbstractControl,
    FormBuilder,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { TranslatePipe } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { xssFormGroupValidator } from "src/app/core/custom-validators/xss-validator";
import { ServerVM } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { AutofocusDirective } from "../../../../../core/directives/auto-focus.directive";

@Component({
    selector: "app-panel-add-vm",
    templateUrl: "./add-vm.component.html",
    providers: [MessageService],
    standalone: true,
    imports: [
        AutofocusDirective,
        FormsModule,
        ReactiveFormsModule,
        InputTextModule,
        InputNumberModule,
        Button,
        AsyncPipe,
        TranslatePipe,
    ],
})
export class PanelAddVmComponent implements OnInit {
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() index: number | undefined;
    @Input() addVMPanelVisible: boolean = false;
    @Output() addVMPanelVisibleChange: EventEmitter<boolean> = new EventEmitter();

    server = computed(() => {
        return this.digitalServiceStore.server();
    });

    vm: ServerVM = {} as ServerVM;

    vcpuControl = this._formBuilder.control(1, [Validators.required]);
    diskControl = this._formBuilder.control(1, [Validators.required]);
    vramControl = this._formBuilder.control(1, [Validators.required]);
    quantityControl = this._formBuilder.control(0, [Validators.required]);
    electricityConsumptionControl = this._formBuilder.control<number | undefined>(
        undefined,
    );
    addVmForm = this._formBuilder.group(
        {
            name: ["", Validators.required],
            vcpu: this.vcpuControl,
            disk: this.diskControl,
            vram: this.vramControl,
            quantity: this.quantityControl,
            opratingTime: [0, [Validators.required]],
            electricityConsumption: this.electricityConsumptionControl,
        },
        {
            validators: [xssFormGroupValidator()],
            updateOn: "blur",
        },
    );

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
                vCpu: 1,
                disk: 1,
                vRam: 1,
                quantity: 1,
                annualOperatingTime: 8760,
                electricityConsumption: undefined as any,
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

        if (this.server().type === "Compute") {
            const isValueTooHigh =
                (value.vcpu || 0) * (value.quantity || 1) + sum > totalVCpu;
            this.setControlError(this.vcpuControl, "isValueTooHigh", isValueTooHigh);
        } else if (this.server().type === "Storage") {
            const isValueTooHigh =
                (value.disk || 0) * (value.quantity || 1) + sum > totalDisk;
            this.setControlError(this.diskControl, "isValueTooHigh", isValueTooHigh);
        } else {
            const isValueTooHigh =
                (value.vram || 0) * (value.quantity || 1) + sum >
                (this.server().totalVram || 0);
            this.setControlError(this.vramControl, "isValueTooHigh", isValueTooHigh);
        }

        this.setControlError(
            this.quantityControl,
            "isQuantityTooLow",
            value.quantity == 0,
        );
    }

    verifyElectricityValue() {
        const totalAnnualElecCons = this.server().annualElectricConsumption || 0;
        const value = this.addVmForm.value;
        const sum = this.server()
            .vm.filter(({ name }) => name !== this.vm.name)
            .reduce(
                (total, { electricityConsumption }) =>
                    total + (electricityConsumption || 0),
                0,
            );

        const isElecValueTooHigh =
            (value.electricityConsumption ?? 0) + sum > totalAnnualElecCons;

        this.setControlError(
            this.electricityConsumptionControl,
            "isElecValueTooHigh",
            isElecValueTooHigh,
        );
    }

    // Merges/clears a single custom error key without wiping errors set by the control's own validators.
    private setControlError(
        control: AbstractControl,
        errorKey: string,
        hasError: boolean,
    ) {
        const { [errorKey]: _removed, ...remainingErrors } = control.errors ?? {};
        control.setErrors(
            hasError
                ? { ...remainingErrors, [errorKey]: true }
                : Object.keys(remainingErrors).length
                  ? remainingErrors
                  : null,
        );
    }

    sum() {
        let sum: number = 0;
        const type = this.server().type;

        const field = type === "Compute" ? "vCpu" : type === "Storage" ? "disk" : "vRam";

        for (const vm of this.server().vm) {
            if (this.vm.name !== vm.name) {
                sum += vm[field]! * vm.quantity;
            }
        }
        return sum;
    }

    submitFormData() {
        // If the vm with the uid exists, update it; otherwise, add the new vm
        const server = this.server();
        if (this.index === undefined) {
            server.vm.push(this.vm);
        } else {
            server.vm[this.index] = this.vm;
        }
        this.digitalServiceStore.setServer(server);
        this.close();
    }

    close() {
        this.addVMPanelVisibleChange.emit(false);
    }
}
