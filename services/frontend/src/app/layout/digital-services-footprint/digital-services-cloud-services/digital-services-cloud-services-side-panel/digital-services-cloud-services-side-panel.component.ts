/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, inject, Input, OnInit, Output } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import { noWhitespaceValidator } from "src/app/core/custom-validators/no-white-space.validator";
import { uniqueNameValidator } from "src/app/core/custom-validators/unique-name.validator";
import { DigitalServiceCloudServiceConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { DropdownValue } from "src/app/core/interfaces/generic.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-digital-services-cloud-services-side-panel",
    templateUrl: "./digital-services-cloud-services-side-panel.component.html",
    providers: [MessageService],
})
export class DigitalServicesCloudServicesSidePanelComponent implements OnInit {
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() sidebarVisible: boolean = true;
    @Input() cloud: DigitalServiceCloudServiceConfig =
        {} as DigitalServiceCloudServiceConfig;
    @Input() cloudServices: DigitalServiceCloudServiceConfig[] = [];
    @Output() sidebarVisibleChange: EventEmitter<boolean> = new EventEmitter();
    @Output() updateCloudServices: EventEmitter<DigitalServiceCloudServiceConfig> =
        new EventEmitter();
    @Output() deleteCloudServices: EventEmitter<DigitalServiceCloudServiceConfig> =
        new EventEmitter();

    cloudForm!: FormGroup;
    countries: DropdownValue[] = [];
    cloudProviders: string[] = [];
    instanceTypesByProvider: Map<string, string[]> = new Map();
    isNew = false;
    existingNames: string[] = [];
    selectedLocation: DropdownValue = {} as DropdownValue;

    constructor(
        private readonly digitalDataService: DigitalServicesDataService,
        private readonly digitalServiceBusiness: DigitalServiceBusinessService,
        private readonly _formBuilder: FormBuilder,
        public userService: UserService,
        private readonly router: Router,
    ) {}

    ngOnInit(): void {
        this.isNew = this.cloud.idFront === undefined;
        this.initForm();
        this.getBoaviztaReferentials();
        if (!this.cloud.idFront) {
            this.resetCloudServices();
        }
    }

    async getBoaviztaReferentials() {
        this.countries = [];
        const countryMap = this.digitalServiceStore.countryMap();

        for (const key in countryMap) {
            this.countries.push({
                code: key,
                name: countryMap[key],
            });
        }

        this.countries.sort((a, b) => a.name.localeCompare(b.name));

        this.cloudProviders = await lastValueFrom(
            this.digitalDataService.getBoaviztapiCloudProviders(),
        );
        if (!this.cloud.idFront) {
            this.cloud.cloudProvider = this.cloudProviders[0];
        }

        for (const cloudProvider of this.cloudProviders) {
            const instances = await lastValueFrom(
                this.digitalDataService.getBoaviztapiInstanceTypes(cloudProvider),
            );
            this.instanceTypesByProvider.set(cloudProvider, instances);
        }
    }

    async resetCloudServices() {
        this.cloud = {
            id: 0,
            digitalServiceUid: this.router.url.split("/")[6],
            name: this.digitalServiceBusiness.getNextAvailableName(
                this.existingNames,
                "Cloud Service",
                false,
            ),
            quantity: 1,
            cloudProvider: "",
            instanceType: "",
            location: {
                code: "EEE",
                name: "Europe",
            },
            locationValue: "Europe",
            annualUsage: 8760,
            averageWorkload: 50,
        };
    }

    initForm() {
        this.existingNames = this.cloudServices
            .filter((c) => (!this.isNew ? this.cloud.name !== c.name : true))
            .map((cloud) => cloud.name);
        this.cloudForm = this._formBuilder.group({
            name: [
                "",
                [
                    Validators.required,
                    uniqueNameValidator(this.existingNames),
                    noWhitespaceValidator(),
                ],
            ],
            cloudProvider: ["", Validators.required],
            instanceType: ["", Validators.required],
            location: ["", Validators.required],
            quantity: ["0", Validators.required],
            averageWorkload: ["0", Validators.required],
            annualUsage: ["0", Validators.required],
        });
        this.cloudForm.get("name")?.markAsDirty();
    }

    close() {
        this.sidebarVisibleChange.emit(false);
    }

    async submitFormData() {
        this.updateCloudServices.emit(this.cloud);
        this.close();
    }

    async deleteServerCloud() {
        this.deleteCloudServices.emit(this.cloud);
        this.close();
    }
}
