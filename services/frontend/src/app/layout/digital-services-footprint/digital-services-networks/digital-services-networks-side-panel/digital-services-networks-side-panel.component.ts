/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, inject, Input, OnInit, Output } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "primeng/api";
import { noWhitespaceValidator } from "src/app/core/custom-validators/no-white-space.validator";
import { uniqueNameValidator } from "src/app/core/custom-validators/unique-name.validator";
import { xssFormGroupValidator } from "src/app/core/custom-validators/xss-validator";
import { DigitalServiceNetworkConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-digital-services-networks-side-panel",
    templateUrl: "./digital-services-networks-side-panel.component.html",
    providers: [MessageService],
})
export class DigitalServicesNetworksSidePanelComponent implements OnInit {
    protected digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() network: DigitalServiceNetworkConfig = {} as DigitalServiceNetworkConfig;
    @Input() networkData: DigitalServiceNetworkConfig[] = [];
    existingNames: string[] = [];

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
        this.networksForm = this._formBuilder.group(
            {
                name: [
                    "",
                    [
                        Validators.required,
                        uniqueNameValidator(this.existingNames),
                        noWhitespaceValidator(),
                    ],
                ],
                type: [
                    { code: "", value: "", country: "", type: "", annualQuantityOfGo: 0 },
                    Validators.required,
                ],
                yearlyQuantityOfGbExchanged: [0, [Validators.required]],
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
        this.network.type = { ...this.networksForm.get("type")!.value! };
        this.update.emit(this.network);
    }

    cancelNetwork() {
        this.outCancel.emit(this.network);
    }
    close() {
        this.outCancel.emit(this.network);
        this.sidebarVisible.emit(false);
    }
}
