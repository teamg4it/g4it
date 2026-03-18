/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { AsyncPipe, NgIf } from "@angular/common";
import { Component, computed, EventEmitter, inject, Input, Output } from "@angular/core";
import {
    FormBuilder,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { TranslatePipe } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { xssFormGroupValidator } from "src/app/core/custom-validators/xss-validator";
import { ServerDC } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { AutofocusDirective } from "../../../../../core/directives/auto-focus.directive";

@Component({
    selector: "app-panel-datacenter",
    templateUrl: "./datacenter.component.html",
    providers: [MessageService],
    imports: [
        AutofocusDirective,
        FormsModule,
        ReactiveFormsModule,
        InputTextModule,
        NgIf,
        InputNumberModule,
        DropdownModule,
        Button,
        AsyncPipe,
        TranslatePipe,
    ]
})
export class PanelDatacenterComponent {
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() addSidebarVisible: boolean = false;
    @Input() server: any;
    @Output() addSidebarVisibleChange: EventEmitter<boolean> = new EventEmitter();
    @Output() serverChange: EventEmitter<ServerDC> = new EventEmitter();

    datacenterForm = this.initForm();

    isToLow: boolean = false;
    disableButton: boolean = false;

    countries = computed(() => {
        const countryList = [];
        const countryMap = this.digitalServiceStore.countryMap();

        for (const key in countryMap) {
            countryList.push({
                value: countryMap[key],
                label: countryMap[key],
            });
        }

        return countryList.sort((a, b) => a.value.localeCompare(b.value));
    });

    initForm() {
        return this._formBuilder.group(
            {
                name: ["", [Validators.required, Validators.pattern("[^|]*")]],
                pue: [2, [Validators.required]],
                country: ["France", Validators.required],
            },
            {
                validators: [xssFormGroupValidator()],
                updateOn: "blur",
            },
        );
    }

    constructor(
        private readonly _formBuilder: FormBuilder,
        public userService: UserService,
    ) {}

    verifyPue() {
        this.isToLow = this.datacenterForm.value.pue! < 1;
    }
    onInputCheck() {
        this.disableButton = this.datacenterForm.value.name?.trim() === "";
    }

    submitFormData() {
        if (!this.isToLow) {
            let datacenter: ServerDC = {
                name: this.datacenterForm.value.name?.trim() || "",
                location: this.datacenterForm.value.country || "",
                pue: this.datacenterForm.value.pue || 1,
            };
            this.serverChange.emit(datacenter);
            this.close();
        }
    }

    close() {
        this.datacenterForm = this.initForm();
        this.addSidebarVisibleChange.emit(false);
    }
}
