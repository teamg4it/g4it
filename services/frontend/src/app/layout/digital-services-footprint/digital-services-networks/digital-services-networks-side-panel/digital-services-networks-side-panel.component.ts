/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { AsyncPipe } from "@angular/common";
import { Component, EventEmitter, inject, Input, OnInit, Output } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { TranslatePipe } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { SelectModule } from "primeng/select";
import { noWhitespaceValidator } from "src/app/core/custom-validators/no-white-space.validator";
import { uniqueNameValidator } from "src/app/core/custom-validators/unique-name.validator";
import { xssFormGroupValidator } from "src/app/core/custom-validators/xss-validator";
import { DigitalServiceNetworkConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { AutofocusDirective } from "../../../../core/directives/auto-focus.directive";

@Component({
    selector: "app-digital-services-networks-side-panel",
    templateUrl: "./digital-services-networks-side-panel.component.html",
    providers: [MessageService],
    imports: [
        AutofocusDirective,
        ReactiveFormsModule,
        InputTextModule,
        SelectModule,
        InputNumberModule,
        Button,
        AsyncPipe,
        TranslatePipe,
    ],
})
export class DigitalServicesNetworksSidePanelComponent implements OnInit {
    protected digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() network: DigitalServiceNetworkConfig = {} as DigitalServiceNetworkConfig;
    @Input() networkData: DigitalServiceNetworkConfig[] = [];

    @Input() existingNames: string[] = [];

    @Output() update: EventEmitter<DigitalServiceNetworkConfig> = new EventEmitter();
    @Output() delete: EventEmitter<DigitalServiceNetworkConfig> = new EventEmitter();
    @Output() outCancel: EventEmitter<DigitalServiceNetworkConfig> = new EventEmitter();
    @Output() sidebarVisible: EventEmitter<boolean> = new EventEmitter();

    networksForm!: FormGroup;

    constructor(
        private readonly _formBuilder: FormBuilder,
        public userService: UserService,
    ) {}

    ngOnInit() {
        const isNew = this.network.idFront === undefined;
        this.existingNames = this.networkData
            .filter((c) => (isNew ? true : this.network.name !== c.name))
            .map((cloud) => cloud.name);

        const defaultType =
            this.network.type ?? this.digitalServiceStore.networkTypes()?.[0] ?? null;

        this.networksForm = this._formBuilder.group(
            {
                name: [
                    this.network.name ?? "",
                    [
                        Validators.required,
                        uniqueNameValidator(this.existingNames),
                        noWhitespaceValidator(),
                    ],
                ],
                type: [defaultType, Validators.required],
                yearlyQuantityOfGbExchanged: [
                    this.network.yearlyQuantityOfGbExchanged ?? 0,
                    [Validators.required],
                ],
            },
            {
                validators: [xssFormGroupValidator()],
                updateOn: "blur",
            },
        );
    }

    deleteNetwork() {
        this.delete.emit(this.network);
    }

    submitFormData() {
        const formValue = this.networksForm.getRawValue();
        this.update.emit({
            ...this.network,
            name: formValue.name,
            type: { ...formValue.type },
            yearlyQuantityOfGbExchanged: formValue.yearlyQuantityOfGbExchanged,
        });
    }

    cancelNetwork() {
        this.outCancel.emit(this.network);
    }
    close() {
        this.outCancel.emit(this.network);
        this.sidebarVisible.emit(false);
    }
}
