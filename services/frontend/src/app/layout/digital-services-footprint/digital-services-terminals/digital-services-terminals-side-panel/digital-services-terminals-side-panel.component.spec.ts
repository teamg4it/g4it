/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA, forwardRef } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { NG_VALUE_ACCESSOR, ReactiveFormsModule } from "@angular/forms";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService, SharedModule } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { DigitalServiceTerminalConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServicesTerminalsSidePanelComponent } from "./digital-services-terminals-side-panel.component";

describe("DigitalServicesTerminalsSidePanelComponent", () => {
    let component: DigitalServicesTerminalsSidePanelComponent;
    let fixture: ComponentFixture<DigitalServicesTerminalsSidePanelComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [DigitalServicesTerminalsSidePanelComponent],
            providers: [
                {
                    provide: NG_VALUE_ACCESSOR,
                    useExisting: forwardRef(
                        () => DigitalServicesTerminalsSidePanelComponent,
                    ),
                    multi: true,
                },
                DigitalServicesDataService,
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
            ],
            imports: [
                SharedModule,
                ReactiveFormsModule,
                ButtonModule,
                DropdownModule,
                InputNumberModule,
                InputTextModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        });
        fixture = TestBed.createComponent(DigitalServicesTerminalsSidePanelComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("it should close", () => {
        spyOn(component.sidebarVisibleChange, "emit");
        component.close();
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalled();
    });

    it("should delete terminal", () => {
        spyOn(component.deleteTerminals, "emit");
        component.deleteTerminal();
        expect(component.deleteTerminals.emit).toHaveBeenCalled();
    });

    it("should submit data ", () => {
        spyOn(component.updateTerminals, "emit");
        component.terminalsForm.controls["country"].setValue("France");
        component.terminalsForm.controls["type"].setValue({
            code: "mobile",
            value: "mobile",
        });
        component.submitFormData();
        expect(component.terminal.type.value).toEqual("mobile");
        expect(component.terminal.country).toEqual("France");
        expect(component.updateTerminals.emit).toHaveBeenCalled();
        expect(component.close()).toHaveBeenCalled;
    });

    it("should reset terminal when resetTerminal() is call", () => {
        //mock data
        component.terminalDeviceTypes = [
            {
                code: "laptop-3",
                lifespan: 6,
                value: "LaptopTest",
            },
        ];
        component.countries = [
            {
                label: "France",
                value: "France",
            },
        ];
        component.terminal = {
            uid: "randomUID",
            name: "name",
            type: {
                code: "mobile-fix",
                value: "Mobile",
                lifespan: 5,
            },
            lifespan: 0,
            country: "France",
            numberOfUsers: 1,
            yearlyUsageTimePerUser: 17,
        };

        //expected terminal after call
        var expectedTerminal = {
            type: {
                code: "laptop-3",
                value: "LaptopTest",
                lifespan: 6,
            },
            name: "Terminal 1",
            lifespan: 6,
            country: "France",
            numberOfUsers: 0,
            yearlyUsageTimePerUser: 0,
        };

        //call function
        component.resetTerminal();
        delete component.terminal.idFront;
        expect(component.terminal).toEqual(expectedTerminal);
    });

    it("should initialize the form with default values and validators", () => {
        component.terminalData = [
            { name: "Terminal 1" } as DigitalServiceTerminalConfig,
            { name: "Terminal 2" } as DigitalServiceTerminalConfig,
        ];
        component.isNew = true;
        component.initForm();

        expect(component.existingNames).toEqual(["Terminal 1", "Terminal 2"]);
        expect(component.terminalsForm.controls["name"].value).toBe("");
        expect(component.terminalsForm.controls["name"].validator).toBeTruthy();
        expect(component.terminalsForm.controls["type"].value).toEqual({
            code: "",
            value: "",
            lifespan: null,
        });
        expect(component.terminalsForm.controls["type"].validator).toBeTruthy();
        expect(component.terminalsForm.controls["country"].value).toBe("");
        expect(component.terminalsForm.controls["country"].validator).toBeTruthy();
        expect(component.terminalsForm.controls["numberOfUsers"].value).toBe("0");
        expect(component.terminalsForm.controls["numberOfUsers"].validator).toBeTruthy();
        expect(component.terminalsForm.controls["lifespan"].value).toBeNull();
        expect(component.terminalsForm.controls["lifespan"].validator).toBeTruthy();
        expect(component.terminalsForm.controls["yearlyUsageTimePerUser"].value).toBe(
            "0",
        );
        expect(
            component.terminalsForm.controls["yearlyUsageTimePerUser"].validator,
        ).toBeTruthy();
        expect(component.terminalsForm.get("name")?.dirty).toBeTrue();
    });

    it("should exclude the current terminal name from existing names when not new", () => {
        component.terminalData = [
            { name: "Terminal 1" } as DigitalServiceTerminalConfig,
            { name: "Terminal 2" } as DigitalServiceTerminalConfig,
        ];
        component.isNew = false;
        component.terminal = { name: "Terminal 1" } as DigitalServiceTerminalConfig;
        component.initForm();

        expect(component.existingNames).toEqual(["Terminal 2"]);
    });
});
