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
import { signal } from "@angular/core";

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
            enableDataInconsistency: false,
            activeDsvUid: "1",
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
        const testTerminal = {
            uid: "randomUID",
            creationDate: 1700746167.59006,
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
            idFront: 0,
        };

        //function call
        component.setTerminal(testTerminal, 0);

        expect(component.terminal).toEqual(testTerminal);
    });
//   it("should return terminals from signal input (computed branch)", () => {
//     const inputSignal = signal([
//         {
//             id: 1,
//             name: "T1",
//             typeCode: "mobile",
//             country: "FR",
//             numberOfUsers: 1,
//             yearlyUsageTimePerUser: 10,
//             lifespan: 2,
//         },
//     ]);

//     component.terminalData = inputSignal as any;

//     const result = component.terminals();

//     expect(result.length).toBe(1);
//     expect(result[0].name).toBe("T1");
// });
// it("should return terminals from direct array input", () => {
//     component.terminalData = signal([
//         {
//             id: 2,
//             name: "T2",
//             typeCode: "mobile",
//             country: "FR",
//             numberOfUsers: 1,
//             yearlyUsageTimePerUser: 10,
//             lifespan: 2,
//         },
//     ]) as any;

//     const result = component.terminals();

//     expect(result.length).toBe(1);
//     expect(result[0].name).toBe("T2");
// });

it("should update sidebarVisible when changeSidebar is called", () => {
    component.changeSidebar(true);
    expect(component.sidebarVisible).toBeTrue();

    component.changeSidebar(false);
    expect(component.sidebarVisible).toBeFalse();
});
it("should emit editEmbedded when embedded mode is true", () => {
    component.embedded = signal(true) as any;

    spyOn(component.editEmbedded, "emit");

    component.setItem({ index: 0, name: "X" });

    expect(component.editEmbedded.emit).toHaveBeenCalled();
});
it("should set terminal when embedded mode is false", () => {
    component.embedded = signal(false) as any;

    component.setItem({ index: 2, name: "T" });

    expect(component.terminal.name).toBe("T");
    expect(component.terminal.idFront).toBe(2);
});
it("should emit deleteEmbedded when embedded", () => {
    component.embedded = signal(true) as any;

    spyOn(component.deleteEmbedded, "emit");

    component.deleteItem({ name: "T" } as any);

    expect(component.deleteEmbedded.emit).toHaveBeenCalled();
});
it("should call API delete when not embedded", async () => {
    component.embedded = signal(false) as any;

    component.digitalService = { uid: "123" } as any;

    spyOn(component.inPhysicalEquipmentsService, "delete").and.returnValue(of({}) as any);
    spyOn(component.digitalServiceStore, "initInPhysicalEquipments").and.resolveTo();
    spyOn(component.digitalServiceStore, "setEnableCalcul");

    await component.deleteItem({
        id: 1,
    } as any);

    expect(component.inPhysicalEquipmentsService.delete).toHaveBeenCalled();
});
it("should create terminal when no id exists", async () => {
    component.embedded = signal(false) as any;
    component.dsVersionUid = signal("v1") as any;

    spyOn(component.inPhysicalEquipmentsService, "create").and.returnValue(of({}) as any);
    spyOn(component.digitalServiceStore, "initInPhysicalEquipments").and.resolveTo();
    spyOn(component.digitalServiceStore, "setEnableCalcul");

    await component.updateTerminals({
        name: "T",
        type: { code: "A" },
        country: "FR",
        numberOfUsers: 1,
        yearlyUsageTimePerUser: 10,
        lifespan: 1,
        digitalServiceUid: "x",
    } as any);

    expect(component.inPhysicalEquipmentsService.create).toHaveBeenCalled();
});
it("should update terminal when id exists", async () => {
    component.embedded = signal(false) as any;
    component.dsVersionUid = signal("v1") as any;

    spyOn(component.inPhysicalEquipmentsService, "update").and.returnValue(of({}) as any);
    spyOn(component.digitalServiceStore, "initInPhysicalEquipments").and.resolveTo();
    spyOn(component.digitalServiceStore, "setEnableCalcul");

    await component.updateTerminals({
        id: 1,
        name: "T",
        type: { code: "A" },
        country: "FR",
        numberOfUsers: 1,
        yearlyUsageTimePerUser: 10,
        lifespan: 1,
        digitalServiceUid: "x",
    } as any);

    expect(component.inPhysicalEquipmentsService.update).toHaveBeenCalled();
});

});
