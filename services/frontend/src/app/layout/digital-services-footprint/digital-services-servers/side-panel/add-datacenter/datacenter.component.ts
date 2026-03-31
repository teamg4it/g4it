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
import { of } from "rxjs";
import { xssFormGroupValidator } from "src/app/core/custom-validators/xss-validator";
import { ServerDC } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-panel-datacenter",
    templateUrl: "./datacenter.component.html",
    providers: [MessageService],
})
export default class PanelDatacenterComponent {
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() addSidebarVisible: boolean = false;
    @Output() addSidebarVisibleChange: EventEmitter<boolean> = new EventEmitter();
    @Output() serverChange: EventEmitter<ServerDC> = new EventEmitter();
@Output() cancel = new EventEmitter<void>();

    datacenterForm = this.initForm();

    isToLow: boolean = false;
    disableButton: boolean = false;
    @Input() forceWriteAccess: boolean | null = null;
    get canWrite$() {
  if (this.forceWriteAccess !== null) {
    return of(this.forceWriteAccess);
  }
  return this.userService.isAllowedDigitalServiceWrite$;
}

@Input() showInternalButtons = true;
get hideInternalButtons() {
  return !this.showInternalButtons;
}

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
                // updateOn: "blur",
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
    const datacenter: ServerDC = {
      name: this.datacenterForm.value.name?.trim() || "",
      location: this.datacenterForm.value.country || "",
      pue: this.datacenterForm.value.pue || 1,
      displayLabel: `${this.datacenterForm.value.name} (${this.datacenterForm.value.country} - PUE = ${this.datacenterForm.value.pue ?? 1})`,
      uid: "" as any,
    };
    this.serverChange.emit(datacenter);
    this.close();
  }
}

    close() {
        this.datacenterForm = this.initForm();
        this.addSidebarVisibleChange.emit(false);
    }
    submit() {
    this.submitFormData();
    }
}
