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
import { DigitalServicesNetworksSidePanelComponent } from "./digital-services-networks-side-panel/digital-services-networks-side-panel.component";
import { DigitalServicesNetworksComponent } from "./digital-services-networks.component";

describe("DigitalServicesNetworksComponent", () => {
    let component: DigitalServicesNetworksComponent;
    let fixture: ComponentFixture<DigitalServicesNetworksComponent>;

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
            enableDataInconsistency: false,
        } as DigitalService),
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [
                DigitalServicesNetworksComponent,
                DigitalServicesNetworksSidePanelComponent,
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
        fixture = TestBed.createComponent(DigitalServicesNetworksComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should set the terminal when setTerminal is called", () => {
        //Mock a terminal
        const testNetwork: any = {
            name: "Network A",
            uid: "uid-001",
            type: {
                code: "type1",
                value: "Fixed Network",
                type: "Fixed",
                annualQuantityOfGo: 1000,
                country: "France",
            },
            yearlyQuantityOfGbExchanged: 500,
            id: 1,
            creationDate: new Date("2023-01-01"),
        };

        //function call
        component.setNetworks(testNetwork, 0);

        expect(component.network.name).toEqual(testNetwork.name);
    });
});
