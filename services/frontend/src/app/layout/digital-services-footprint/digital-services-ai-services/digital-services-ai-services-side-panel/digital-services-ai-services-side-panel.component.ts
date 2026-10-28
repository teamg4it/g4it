/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { AsyncPipe } from "@angular/common";
import { Component, EventEmitter, inject, Input, OnInit, Output } from "@angular/core";
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { Router } from "@angular/router";
import { TranslatePipe } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { ScrollPanel } from "primeng/scrollpanel";
import { SelectModule } from "primeng/select";
import { lastValueFrom } from "rxjs";
import { noWhitespaceValidator } from "src/app/core/custom-validators/no-white-space.validator";
import { uniqueNameValidator } from "src/app/core/custom-validators/unique-name.validator";
import { xssFormGroupValidator } from "src/app/core/custom-validators/xss-validator";
import { DigitalServiceAIServiceConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { DropdownValue } from "src/app/core/interfaces/generic.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { AutofocusDirective } from "../../../../core/directives/auto-focus.directive";

@Component({
    selector: "app-digital-services-ai-services-side-panel",
    templateUrl: "./digital-services-ai-services-side-panel.component.html",
    providers: [MessageService],
    standalone: true,
    imports: [
        AutofocusDirective,
        FormsModule,
        ReactiveFormsModule,
        InputTextModule,
        SelectModule,
        InputNumberModule,
        Button,
        AsyncPipe,
        TranslatePipe,
        ScrollPanel,
    ],
})
export class DigitalServicesAIServicesSidePanelComponent implements OnInit {
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() sidebarVisible: boolean = true;
    @Input() ai: DigitalServiceAIServiceConfig = {} as DigitalServiceAIServiceConfig;
    @Input() aiServices: DigitalServiceAIServiceConfig[] = [];
    @Output() sidebarVisibleChange: EventEmitter<boolean> = new EventEmitter();
    @Output() updateAIServices: EventEmitter<DigitalServiceAIServiceConfig> =
        new EventEmitter();
    @Output() deleteAIServices: EventEmitter<DigitalServiceAIServiceConfig> =
        new EventEmitter();

    aiForm!: FormGroup;
    countries: DropdownValue[] = [];
    aiProviders: string[] = [];
    aiModelsByProvider: Map<string, string[]> = new Map();
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
        this.isNew = this.ai.idFront === undefined;
        this.initForm();
        void this.getEcologitsReferentials();
        if (!this.ai.idFront) {
            void this.resetAIServices();
        }
    }

    async getEcologitsReferentials() {
        this.countries = [];
        const countryMap = this.digitalServiceStore.countryMap();

        for (const key in countryMap) {
            this.countries.push({
                code: key,
                name: countryMap[key],
            });
        }

        this.countries.sort((a, b) => a.name.localeCompare(b.name));

        this.aiProviders = await lastValueFrom(
            this.digitalDataService.getEcologitsApiAIProviders(),
        );
        if (!this.ai.idFront) {
            this.ai.aiProvider = this.aiProviders[0];
        }

        for (const aiProvider of this.aiProviders) {
            const models = await lastValueFrom(
                this.digitalDataService.getEcologitsApiAIModel(aiProvider),
            );
            this.aiModelsByProvider.set(aiProvider, models);
        }
    }

    async resetAIServices() {
        this.ai = {
            id: 0,
            digitalServiceUid: this.router.url.split("/")[6],
            name: this.digitalServiceBusiness.getNextAvailableName(
                this.existingNames,
                "AI Service",
                false,
            ),
            tokenQuantity: 1,
            aiProvider: "",
            aiModel: "",
            location: {
                code: "EEE",
                name: "Europe",
            },
            locationValue: "Europe",
        };
    }

    initForm() {
        this.existingNames = this.aiServices
            .filter((c) => (this.isNew ? true : this.ai.name !== c.name))
            .map((ai) => ai.name);
        this.aiForm = this._formBuilder.group(
            {
                name: [
                    "",
                    [
                        Validators.required,
                        uniqueNameValidator(this.existingNames),
                        noWhitespaceValidator(),
                    ],
                ],
                aiProvider: ["", Validators.required],
                aiModel: ["", Validators.required],
                location: ["", Validators.required],
                tokenQuantity: ["0", Validators.required],
            },
            {
                validators: [xssFormGroupValidator()],
                updateOn: "blur",
            },
        );
        this.aiForm.get("name")?.markAsDirty();
    }

    close() {
        this.sidebarVisibleChange.emit(false);
    }

    async submitFormData() {
        this.updateAIServices.emit(this.ai);
        this.close();
    }

    async deleteServerAI() {
        this.deleteAIServices.emit(this.ai);
        this.close();
    }
}
