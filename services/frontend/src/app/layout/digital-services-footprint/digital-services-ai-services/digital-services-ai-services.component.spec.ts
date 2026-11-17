/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { signal } from "@angular/core";
import { Router } from "@angular/router";
import { of } from "rxjs";
import { DigitalServiceAIServiceConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { UserService } from "src/app/core/service/business/user.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServicesAIServicesComponent } from "./digital-services-ai-services.component";

const createVirtualEquipment = (
    overrides: Partial<InVirtualEquipmentRest> = {},
): InVirtualEquipmentRest => ({
    id: 1,
    name: "AI service",
    quantity: 1000,
    infrastructureType: "AI_SERVICES",
    location: "FR",
    ...overrides,
});

describe("DigitalServicesAIServicesComponent", () => {
    let component: DigitalServicesAIServicesComponent;
    let fixture: ComponentFixture<DigitalServicesAIServicesComponent>;
    let inVirtualEquipmentsService: jasmine.SpyObj<InVirtualEquipmentsService>;
    let digitalServiceStore: {
        inVirtualEquipments: () => InVirtualEquipmentRest[];
        countryMap: () => Record<string, string>;
        initInVirtualEquipments: jasmine.Spy;
        setEnableCalcul: jasmine.Spy;
    };
    let virtualEquipments: ReturnType<typeof signal<InVirtualEquipmentRest[]>>;
    let countries: ReturnType<typeof signal<Record<string, string>>>;

    beforeEach(async () => {
        virtualEquipments = signal<InVirtualEquipmentRest[]>([]);
        countries = signal<Record<string, string>>({ FR: "France" });
        digitalServiceStore = {
            inVirtualEquipments: () => virtualEquipments(),
            countryMap: () => countries(),
            initInVirtualEquipments: jasmine
                .createSpy("initInVirtualEquipments")
                .and.resolveTo(undefined),
            setEnableCalcul: jasmine.createSpy("setEnableCalcul"),
        };
        inVirtualEquipmentsService = jasmine.createSpyObj("InVirtualEquipmentsService", [
            "create",
            "update",
            "delete",
        ]);
        inVirtualEquipmentsService.create.and.returnValue(
            of({} as InVirtualEquipmentRest),
        );
        inVirtualEquipmentsService.update.and.returnValue(
            of({} as InVirtualEquipmentRest),
        );
        inVirtualEquipmentsService.delete.and.returnValue(
            of({} as InVirtualEquipmentRest),
        );
        const router = jasmine.createSpyObj<Router>("Router", [], {
            url: "/organizations/org1/workspaces/ws1/digital-services/ds1/footprint/123",
        });

        await TestBed.configureTestingModule({
            imports: [DigitalServicesAIServicesComponent],
            providers: [
                {
                    provide: InVirtualEquipmentsService,
                    useValue: inVirtualEquipmentsService,
                },
                { provide: DigitalServiceStoreService, useValue: digitalServiceStore },
                { provide: UserService, useValue: {} },
                { provide: Router, useValue: router },
            ],
        })
            .overrideComponent(DigitalServicesAIServicesComponent, {
                set: { template: "", imports: [] },
            })
            .compileComponents();

        fixture = TestBed.createComponent(DigitalServicesAIServicesComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput("dsVersionUid", "version-1");
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should load virtual equipment for the digital service on init", () => {
        fixture.detectChanges();

        expect(component.digitalServiceUid).toBe("ds1");
        expect(digitalServiceStore.initInVirtualEquipments).toHaveBeenCalledWith("ds1");
    });

    it("should return only AI services, mapped with country names", () => {
        virtualEquipments.set([
            createVirtualEquipment({
                id: 10,
                name: "AI model",
                digitalServiceUid: "ds1",
                provider: "OpenAI",
                instanceType: "model-1",
            }),
            createVirtualEquipment({
                id: 11,
                name: "Cloud instance",
                infrastructureType: "CLOUD_SERVICES",
            }),
            createVirtualEquipment({
                id: 12,
                name: "AI model with unknown location",
                location: "ZZ",
                provider: "Provider",
                instanceType: "model-2",
            }),
        ]);

        expect(component.aiServices()).toEqual([
            jasmine.objectContaining({
                id: 10,
                digitalServiceUid: "ds1",
                name: "AI model",
                tokenQuantity: 1000,
                aiProvider: "OpenAI",
                aiModel: "model-1",
                location: { code: "FR", name: "France" },
                locationValue: "France",
            }),
            jasmine.objectContaining({
                id: 12,
                name: "AI model with unknown location",
                tokenQuantity: 1000,
                aiProvider: "Provider",
                aiModel: "model-2",
                location: { code: "ZZ", name: "ZZ" },
                locationValue: "ZZ",
            }),
        ]);
        expect(component.aiServices()[1].digitalServiceUid).toBeUndefined();
    });

    it("should refresh virtual equipments for the current digital service", async () => {
        component.digitalServiceUid = "ds1";

        await component.getAIServices();

        expect(digitalServiceStore.initInVirtualEquipments).toHaveBeenCalledWith("ds1");
    });

    it("should update sidebar visibility", () => {
        component.changeSidebar(true);

        expect(component.sidebarVisible).toBeTrue();
    });

    it("should set the selected item and retain its table index", () => {
        const event = { id: 4, name: "AI model", index: 2 };

        component.setItem(event);

        expect(component.ai).toEqual(
            jasmine.objectContaining({ id: 4, name: "AI model", idFront: 2 }),
        );
        expect("index" in event).toBeFalse();
    });

    it("should set a selected AI service with its index", () => {
        const ai = { id: 4, name: "AI model" } as DigitalServiceAIServiceConfig;

        component.setAIservices(ai, 3);

        expect(component.ai).toEqual({ ...ai, idFront: 3 });
        expect(component.ai).not.toBe(ai);
    });

    it("should reset the selected AI service", () => {
        component.ai = { id: 4 } as DigitalServiceAIServiceConfig;

        component.resetAIServices();

        expect(Object.keys(component.ai)).toHaveSize(0);
    });

    it("should map AI service configuration to virtual equipment input", () => {
        const ai = {
            id: 4,
            digitalServiceUid: "ds1",
            name: "AI model",
            aiProvider: "OpenAI",
            aiModel: "model-1",
            tokenQuantity: 500,
            location: { code: "FR", name: "France" },
        } as DigitalServiceAIServiceConfig;

        expect(component.toInVirtualEquipmentRest(ai)).toEqual({
            id: 4,
            digitalServiceUid: "ds1",
            digitalServiceVersionUid: "version-1",
            name: "AI model",
            infrastructureType: "AI_SERVICES",
            quantity: 500,
            provider: "OpenAI",
            instanceType: "model-1",
            location: "FR",
        });
    });

    it("should create a new AI service, refresh data and enable calculation", async () => {
        component.digitalServiceUid = "ds1";
        const ai = {
            id: 0,
            digitalServiceUid: "ds1",
            name: "AI model",
            aiProvider: "OpenAI",
            aiModel: "model-1",
            tokenQuantity: 500,
            location: { code: "FR", name: "France" },
        } as DigitalServiceAIServiceConfig;

        await component.updateAIServices(ai);

        expect(inVirtualEquipmentsService.create).toHaveBeenCalledWith(
            component.toInVirtualEquipmentRest(ai),
        );
        expect(inVirtualEquipmentsService.update).not.toHaveBeenCalled();
        expect(digitalServiceStore.initInVirtualEquipments).toHaveBeenCalledWith("ds1");
        expect(digitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
    });

    it("should update an existing AI service, refresh data and enable calculation", async () => {
        component.digitalServiceUid = "ds1";
        const ai = {
            id: 4,
            digitalServiceUid: "ds1",
            name: "AI model",
            aiProvider: "OpenAI",
            aiModel: "model-1",
            tokenQuantity: 500,
            location: { code: "FR", name: "France" },
        } as DigitalServiceAIServiceConfig;

        await component.updateAIServices(ai);

        expect(inVirtualEquipmentsService.update).toHaveBeenCalledWith(
            component.toInVirtualEquipmentRest(ai),
        );
        expect(inVirtualEquipmentsService.create).not.toHaveBeenCalled();
        expect(digitalServiceStore.initInVirtualEquipments).toHaveBeenCalledWith("ds1");
        expect(digitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
    });

    it("should delete a table item using the version uid, refresh data and enable calculation", async () => {
        const ai = { id: 4 } as DigitalServiceAIServiceConfig;

        await component.deleteItem(ai);

        expect(inVirtualEquipmentsService.delete).toHaveBeenCalledWith(4, "version-1");
        expect(digitalServiceStore.initInVirtualEquipments).toHaveBeenCalledWith("");
        expect(digitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
    });

    it("should delete an AI service from its side panel and refresh data", async () => {
        component.digitalServiceUid = "ds1";
        const ai = {
            id: 4,
            digitalServiceUid: "ds1",
        } as DigitalServiceAIServiceConfig;
        component.ai = ai;

        await component.deleteAIServices(ai);

        expect(inVirtualEquipmentsService.delete).toHaveBeenCalledWith(4, "ds1");
        expect(digitalServiceStore.initInVirtualEquipments).toHaveBeenCalledWith("ds1");
        expect(digitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
    });
});
