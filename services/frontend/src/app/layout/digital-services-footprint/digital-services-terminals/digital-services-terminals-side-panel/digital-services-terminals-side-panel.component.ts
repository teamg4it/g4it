/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import {
    DigitalServiceTerminalConfig,
    TerminalsType,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-digital-services-terminals-side-panel",
    templateUrl: "./digital-services-terminals-side-panel.component.html",
    providers: [MessageService],
})
export class DigitalServicesTerminalsSidePanelComponent {
    @Input() sidebarVisible: boolean = true;
    @Input() terminal: DigitalServiceTerminalConfig = {} as DigitalServiceTerminalConfig;

    @Output() sidebarVisibleChange: EventEmitter<boolean> = new EventEmitter();
    @Output() updateTerminals: EventEmitter<DigitalServiceTerminalConfig> =
        new EventEmitter();
    @Output() deleteTerminals: EventEmitter<DigitalServiceTerminalConfig> =
        new EventEmitter();

    terminalDeviceTypes: TerminalsType[] = [];
    countries: { label: string; value: string }[] = [];

    terminalsForm!: FormGroup;
    isNew = false;

    constructor(
        private digitalDataService: DigitalServicesDataService,
        private _formBuilder: FormBuilder,
        public userService: UserService,
    ) {}

    async ngOnInit() {
        this.isNew = this.terminal.idFront === undefined;
        this.initForm();
        await this.getTerminalsReferentials();
        if (!this.terminal.idFront) {
            this.resetTerminal();
        } else {
            if (this.terminal.typeCode) {
                this.terminal.type = this.terminalDeviceTypes.find(
                    (item) => item.value === this.terminal.typeCode,
                )!;
            }
        }
    }

    initForm() {
        this.terminalsForm = this._formBuilder.group({
            type: [{ code: "", value: "", lifespan: null }, Validators.required],
            country: ["", Validators.required],
            numberOfUsers: ["0", Validators.required],
            lifespan: [null, Validators.required],
            yearlyUsageTimePerUser: ["0", Validators.required],
        });
    }

    async getTerminalsReferentials() {
        const referentials = await lastValueFrom(
            this.digitalDataService.getDeviceReferential(),
        );

        this.terminalDeviceTypes = referentials.sort((a, b) =>
            a.value.localeCompare(b.value),
        );

        const countryList = await lastValueFrom(
            this.digitalDataService.getCountryReferential(),
        );
        const exclusions = ["Non spécifié", "France-CNR"];
        this.countries = countryList
            .sort()
            .filter((item) => !exclusions.includes(item))
            .map((item) => ({ value: item, label: item }));
    }

    resetTerminal() {
        const defaultType = this.terminalDeviceTypes.filter(
            (item) => item.value === "Laptop",
        );
        const type =
            defaultType.length > 0 ? defaultType[0] : this.terminalDeviceTypes[0];

        const defaultCountry = this.countries.filter((item) => item.label === "France");

        const country =
            defaultCountry.length > 0 ? defaultCountry[0].value : this.countries[0].value;

        this.terminal = {
            type,
            country,
            numberOfUsers: 0,
            yearlyUsageTimePerUser: 0,
            lifespan: type.lifespan,
        };
    }

    close() {
        this.sidebarVisibleChange.emit(false);
    }

    async submitFormData() {
        this.updateTerminals.emit(this.terminal);
        this.close();
    }

    async deleteTerminal() {
        this.deleteTerminals.emit(this.terminal);
        this.close();
    }
}
