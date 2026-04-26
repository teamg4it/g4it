/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA, signal } from "@angular/core";
import { ComponentFixture, TestBed, fakeAsync, tick } from "@angular/core/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { SidebarModule } from "primeng/sidebar";
import { TableModule } from "primeng/table";
import { of } from "rxjs";

import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { InPhysicalEquipmentsService } from "src/app/core/service/data/in-out/in-physical-equipments.service";

import { SharedModule } from "./../../../core/shared/shared.module";
import { DigitalServicesNetworksSidePanelComponent } from "./digital-services-networks-side-panel/digital-services-networks-side-panel.component";
import { DigitalServicesNetworksComponent } from "./digital-services-networks.component";

describe("DigitalServicesNetworksComponent", () => {
    let component: DigitalServicesNetworksComponent;
    let fixture: ComponentFixture<DigitalServicesNetworksComponent>;

    let mockStore: any;
    let mockService: any;

    const digitalServiceDataMock = {
        digitalService$: of({
            name: "Test DS",
            uid: "uid",
        } as DigitalService),
    };

    beforeEach(() => {
        mockStore = {
            networkTypes: jasmine.createSpy().and.returnValue([
                {
                    code: "FIXED",
                    value: "Fixed",
                    type: "Fixed",
                    annualQuantityOfGo: 100,
                    country: "FR",
                },
                {
                    code: "MOBILE",
                    value: "Mobile",
                    type: "Mobile",
                    annualQuantityOfGo: 0,
                    country: "FR",
                },
            ]),
            inPhysicalEquipments: jasmine.createSpy().and.returnValue([
                {
                    id: 1,
                    type: "Network",
                    model: "FIXED",
                    quantity: 2,
                    name: "net1",
                    digitalServiceUid: "ds",
                    creationDate: "date",
                },
            ]),
            initInPhysicalEquipments: jasmine.createSpy(),
            setEnableCalcul: jasmine.createSpy(),
        };

        mockService = {
            delete: jasmine.createSpy().and.returnValue(of({})),
            update: jasmine.createSpy().and.returnValue(of({})),
            create: jasmine.createSpy().and.returnValue(of({})),
        };

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
                { provide: DigitalServicesDataService, useValue: digitalServiceDataMock },
                { provide: DigitalServiceStoreService, useValue: mockStore },
                { provide: InPhysicalEquipmentsService, useValue: mockService },
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

        (component as any).embedded = () => false;
        (component as any).dsVersionUid = () => "v1";

        fixture.detectChanges();
    });

    // =============================
    // BASIC
    // =============================
    it("should create", () => {
        expect(component).toBeTruthy();
    });

    // =============================
    // COMPUTED networkData
    // =============================
    it("should use networkDataInput (array)", () => {
        (component as any).networkDataInput = () => [{ name: "A" } as any];
        const result = component.networkData();

        expect(result.length).toBe(1);
    });

  it("should use networkDataInput (signal)", () => {
  fixture = TestBed.createComponent(DigitalServicesNetworksComponent);
  component = fixture.componentInstance;

  (component as any).embedded = () => false;
  (component as any).dsVersionUid = () => "v1";

  (component as any).networkDataInput = () => signal([{ name: "B" } as any]);

  fixture.detectChanges();

  const result = component.networkData();

  expect(result[0].name).toBe("B");
});

    it("should map store data", () => {
    (component as any).networkDataInput = () => undefined;
        const result = component.networkData();

        expect(result.length).toBeGreaterThan(0);
        expect(result[0].yearlyQuantityOfGbExchanged).toBe(200); // 100 * 2
    });

    it("should expose debugNetworkData", () => {
        const result = component.debugNetworkData;
        expect(result).toBeDefined();
    });

    // =============================
    // UI
    // =============================
    it("should change sidebar", () => {
        component.changeSidebar(true);
        expect(component.sidebarVisible).toBeTrue();
    });

    it("should set item normally", () => {
        component.setItem({ index: 2, name: "net" });

        expect(component.network.idFront).toBe(2);
    });

    it("should emit when embedded", () => {
        (component as any).embedded = () => true;
        spyOn(component.editEmbedded, "emit");

        component.setItem({ index: 1, name: "net" });

        expect(component.editEmbedded.emit).toHaveBeenCalled();
    });

    // =============================
    // DELETE
    // =============================
    it("should delete in embedded mode", fakeAsync(() => {
        (component as any).embedded = () => true;
        spyOn(component.deleteEmbedded, "emit");

        component.deleteItem({ id: 1 } as any);
        tick();

        expect(component.deleteEmbedded.emit).toHaveBeenCalled();
    }));

    it("should delete via service", fakeAsync(() => {
        component.deleteItem({ id: 1 } as any);
        tick();

        expect(mockService.delete).toHaveBeenCalled();
        expect(mockStore.setEnableCalcul).toHaveBeenCalled();
    }));

    // =============================
    // RESET
    // =============================
    it("should reset network", () => {
        component.network = {} as any;

        component.resetNetwork();

        expect(component.network.name).toBeDefined();
    });

    // =============================
    // ACTION NETWORK
    // =============================
    it("should cancel action", fakeAsync(() => {
        component.actionNetwork("cancel", {} as any);
        tick();

        expect(mockService.create).not.toHaveBeenCalled();
    }));

    it("should emit update in embedded", fakeAsync(() => {
        (component as any).embedded = () => true;
        spyOn(component.updateEmbedded, "emit");

        component.actionNetwork("save", {} as any);
        tick();

        expect(component.updateEmbedded.emit).toHaveBeenCalled();
    }));

    it("should create network", fakeAsync(() => {
        component.actionNetwork("save", {
            name: "net",
            type: { code: "FIXED", country: "FR", annualQuantityOfGo: 100 },
            yearlyQuantityOfGbExchanged: 200,
        } as any);
        tick();

        expect(mockService.create).toHaveBeenCalled();
    }));

    it("should update network", fakeAsync(() => {
        component.actionNetwork("save", {
            id: 1,
            name: "net",
            type: { code: "FIXED", country: "FR", annualQuantityOfGo: 100 },
            yearlyQuantityOfGbExchanged: 200,
        } as any);
        tick();

        expect(mockService.update).toHaveBeenCalled();
    }));

    // =============================
    // CALCULATE QUANTITY
    // =============================
    it("should calculate quantity mobile", () => {
        const result = (component as any).calculateQuantity(100, {
            type: "Mobile",
        });

        expect(result).toBe(100);
    });

    it("should calculate quantity fixed", () => {
        const result = (component as any).calculateQuantity(200, {
            type: "Fixed",
            annualQuantityOfGo: 100,
        });

        expect(result).toBe(2);
    });

    it("should return 0 if no quantity", () => {
        const result = (component as any).calculateQuantity(0, {} as any);
        expect(result).toBe(0);
    });

    // =============================
    // EDITOR
    // =============================
    it("should open editor normally", () => {
        component.openNetworkEditor({ name: "net" } as any);

        expect(component.sidebarVisible).toBeTrue();
    });

    it("should emit editor when embedded", () => {
        (component as any).embedded = () => true;
        spyOn(component.editEmbedded, "emit");

        component.openNetworkEditor({ name: "net" } as any);

        expect(component.editEmbedded.emit).toHaveBeenCalled();
    });

    // =============================
    // SET NETWORK
    // =============================
    it("should set network", () => {
        component.setNetworks({ name: "A" } as any, 5);

        expect(component.network.idFront).toBe(5);
    });
});