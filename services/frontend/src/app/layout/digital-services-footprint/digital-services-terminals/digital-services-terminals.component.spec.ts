/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { SidebarModule } from "primeng/sidebar";
import { TableModule } from "primeng/table";
import { of } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { SharedModule } from "./../../../core/shared/shared.module";
import { DigitalServicesTerminalsSidePanelComponent } from "./digital-services-terminals-side-panel/digital-services-terminals-side-panel.component";
import { DigitalServicesTerminalsComponent } from "./digital-services-terminals.component";

describe("DigitalServicesTerminalsComponent", () => {
    let component: DigitalServicesTerminalsComponent;
    let fixture: ComponentFixture<DigitalServicesTerminalsComponent>;

    // Mock DigitalServicesDataService
    const digitalServiceDataMock = {
        digitalService$: of({
            name: "Test Digital Service",
            uid: "test-uid",
            creationDate: Date.now(),
            lastUpdateDate: Date.now(),
            lastCalculationDate: null,
            networks: [],
            servers: [],
            terminals: [],
        } as DigitalService),
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [
                DigitalServicesTerminalsComponent,
                DigitalServicesTerminalsSidePanelComponent,
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
                {
                    provide: DigitalServicesDataService,
                    useValue: digitalServiceDataMock,
                },
            ],
            imports: [
                SharedModule,
                TableModule,
                SidebarModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        });
        fixture = TestBed.createComponent(DigitalServicesTerminalsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should set the terminal when setTerminal is called", () => {
        //Mock a terminal
        var testTerminal = {
            uid: "randomUID",
            creationDate: 1700746167.59006,
            type: {
                code: "mobile-fix",
                value: "Mobile",
                lifespan: 5,
            },
            lifespan: 0,
            country: "France",
            numberOfUsers: 1,
            yearlyUsageTimePerUser: 17,
            idFront: 0,
        };

        //function call
        component.setTerminal(testTerminal, 0);

        expect(component.terminal).toEqual(testTerminal);
    });
});
