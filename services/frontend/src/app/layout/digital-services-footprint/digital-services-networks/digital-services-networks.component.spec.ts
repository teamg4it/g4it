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
import { DrawerModule } from "primeng/drawer";
import { TableModule } from "primeng/table";
import { of } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { SharedModule } from "./../../../core/shared/shared.module";
import { DigitalServicesNetworksSidePanelComponent } from "./digital-services-networks-side-panel/digital-services-networks-side-panel.component";
import { DigitalServicesNetworksComponent } from "./digital-services-networks.component";

describe("DigitalServicesNetworksComponent", () => {
    let component: DigitalServicesNetworksComponent;
    let fixture: ComponentFixture<DigitalServicesNetworksComponent>;
    let mockDigitalServiceDataService: any;
    let mockInPhysicalEquipmentsService: any;
    let mockDigitalServiceStore: any;
    let mockUserService: any;
    let mockDigitalServicesBusiness: any;

    const mockDigitalService = {
        name: "Test Digital Service",
        uid: "test-uid",
        creationDate: Date.now(),
        lastUpdateDate: Date.now(),
        lastCalculationDate: null,
        networks: [
            {
                uid: "network-1",
                name: "Network 1",
                type: {
                    code: "fixed-network",
                    value: "Fixed Network",
                    type: "Fixed",
                    annualQuantityOfGo: 1000,
                    country: "France",
                },
                yearlyQuantityOfGbExchanged: 500,
            },
        ],
        servers: [],
        terminals: [],
        enableDataInconsistency: false,
        activeDsvUid: "1",
    } as DigitalService;

    const mockInPhysicalEquipments = [
        {
            id: 1,
            name: "Network 1",
            type: "Network",
            model: "fixed-network",
            quantity: 0.5,
            location: "France",
            creationDate: Date.now(),
            digitalServiceUid: "test-uid",
        },
        {
            id: 2,
            name: "Network 2",
            type: "Network",
            model: "mobile-network",
            quantity: 2000,
            location: "Germany",
            creationDate: Date.now(),
            digitalServiceUid: "test-uid",
        },
        {
            id: 3,
            name: "Server 1",
            type: "Server",
            model: "server-model",
            location: "Spain",
        },
    ];

    const mockNetworkTypes = [
        {
            code: "fixed-network",
            value: "Fixed Network",
            type: "Fixed",
            annualQuantityOfGo: 1000,
            country: "France",
        },
        {
            code: "mobile-network",
            value: "Mobile Network",
            type: "Mobile",
            annualQuantityOfGo: 0,
            country: "Germany",
        },
    ];

    beforeEach(async () => {
        mockDigitalServiceDataService = {
            digitalService$: of(mockDigitalService),
            get: jasmine.createSpy().and.returnValue(of(mockDigitalService)),
            update: jasmine.createSpy().and.returnValue(of(mockDigitalService)),
        };

        mockInPhysicalEquipmentsService = {
            create: jasmine.createSpy().and.returnValue(of({})),
            update: jasmine.createSpy().and.returnValue(of({})),
            delete: jasmine.createSpy().and.returnValue(of({})),
        };

        mockDigitalServiceStore = {
            networkTypes: jasmine.createSpy().and.returnValue(mockNetworkTypes),
            inPhysicalEquipments: jasmine
                .createSpy()
                .and.returnValue(mockInPhysicalEquipments),
            initInPhysicalEquipments: jasmine
                .createSpy()
                .and.returnValue(Promise.resolve()),
            setEnableCalcul: jasmine.createSpy(),
        };

        mockUserService = {
            isAllowedDigitalServiceWrite$: of(true),
        };

        mockDigitalServicesBusiness = {
            getNextAvailableName: jasmine.createSpy().and.returnValue("Network 3"),
        };

        TestBed.configureTestingModule({
            providers: [
                TranslatePipe,
                TranslateService,
                MessageService,
                {
                    provide: DigitalServicesDataService,
                    useValue: mockDigitalServiceDataService,
                },
                {
                    provide: UserService,
                    useValue: mockUserService,
                },
                {
                    provide: DigitalServiceBusinessService,
                    useValue: mockDigitalServicesBusiness,
                },
            ],
            imports: [
                SharedModule,
                TableModule,
                DrawerModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                DigitalServicesNetworksComponent,
                DigitalServicesNetworksSidePanelComponent,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).overrideComponent(DigitalServicesNetworksComponent, {
            set: {
                providers: [
                    {
                        provide: DigitalServiceStoreService,
                        useValue: mockDigitalServiceStore,
                    },
                ],
            },
        });

        fixture = TestBed.createComponent(DigitalServicesNetworksComponent);
        component = fixture.componentInstance;

        // Override injected services
        (component as any).inPhysicalEquipmentsService = mockInPhysicalEquipmentsService;
        (component as any).digitalServiceStore = mockDigitalServiceStore;
        (component as any).digitalServicesBusiness = mockDigitalServicesBusiness;

        fixture.detectChanges();
    });

    describe("Component Initialization", () => {
        it("should create", () => {
            expect(component).toBeTruthy();
        });

        it("should initialize with default values", () => {
            expect(component.sidebarVisible).toBe(false);
            expect(component.headerFields).toEqual([
                "name",
                "typeCode",
                "yearlyQuantityOfGbExchanged",
            ]);
        });

        it("should initialize digitalService on init", async () => {
            component.ngOnInit();
            await fixture.whenStable();

            expect(component.digitalService).toEqual(mockDigitalService);
        });

        it("should call resetNetwork on init", async () => {
            spyOn(component, "resetNetwork");

            component.ngOnInit();
            await fixture.whenStable();

            expect(component.resetNetwork).toHaveBeenCalled();
        });
    });

    describe("Network Management", () => {
        it("should set network with index", () => {
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

            component.setNetworks(testNetwork, 0);

            expect(component.network.name).toEqual(testNetwork.name);
            expect(component.network.idFront).toBe(0);
        });

        it("should set item with event data", () => {
            const event = {
                index: 2,
                name: "Network B",
                type: {
                    code: "mobile-network",
                    value: "Mobile Network",
                    type: "Mobile",
                },
                yearlyQuantityOfGbExchanged: 1000,
            };

            component.setItem(event);

            expect(component.network.name).toBe("Network B");
            expect(component.network.idFront).toBe(2);
            expect(event.index).toBeUndefined();
        });

        it("should reset network with next available name", async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.resetNetwork();

            expect(mockDigitalServicesBusiness.getNextAvailableName).toHaveBeenCalledWith(
                jasmine.any(Array),
                "Network",
                true,
            );
            expect(component.network.name).toBe("Network 3");
            expect(component.network.uid).toBeUndefined();
            expect(component.network.yearlyQuantityOfGbExchanged).toBe(0);
        });

        it("should set existing names when resetting network", async () => {
            component.ngOnInit();
            await fixture.whenStable();
            component.network = { name: "Network 1" } as any;

            component.resetNetwork();

            const existingNames = component.existingNames();
            expect(existingNames.length).toBeGreaterThan(0);
        });

        it("should filter out current network name from existing names", async () => {
            component.ngOnInit();
            await fixture.whenStable();
            component.network = {
                name: "Network 1",
                idFront: 0,
            } as any;

            component.resetNetwork();

            const existingNames = component.existingNames();
            expect(existingNames).not.toContain("Network 1");
        });
    });

    describe("Sidebar Management", () => {
        it("should toggle sidebar visibility", () => {
            component.sidebarVisible = false;
            component.changeSidebar(true);
            expect(component.sidebarVisible).toBe(true);

            component.changeSidebar(false);
            expect(component.sidebarVisible).toBe(false);
        });
    });

    describe("Network CRUD Operations", () => {
        it("should create a new fixed network", async () => {
            const newNetwork = {
                name: "New Fixed Network",
                type: {
                    code: "fixed-network",
                    value: "Fixed Network",
                    type: "Fixed",
                    annualQuantityOfGo: 1000,
                    country: "France",
                },
                yearlyQuantityOfGbExchanged: 2000,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", newNetwork);

            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    name: "New Fixed Network",
                    type: "Network",
                    model: "fixed-network",
                    quantity: 2, // 2000 / 1000
                    location: "France",
                }),
            );
            expect(mockDigitalServiceStore.initInPhysicalEquipments).toHaveBeenCalledWith(
                "version-123",
            );
            expect(mockDigitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
            expect(component.sidebarVisible).toBe(false);
        });

        it("should create a new mobile network", async () => {
            const newNetwork = {
                name: "New Mobile Network",
                type: {
                    code: "mobile-network",
                    value: "Mobile Network",
                    type: "Mobile",
                    annualQuantityOfGo: 0,
                    country: "Germany",
                },
                yearlyQuantityOfGbExchanged: 3000,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", newNetwork);

            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    name: "New Mobile Network",
                    type: "Network",
                    model: "mobile-network",
                    quantity: 3000, // Mobile type uses yearlyQuantityOfGbExchanged directly
                    location: "Germany",
                }),
            );
        });

        it("should update an existing network", async () => {
            const existingNetwork = {
                id: 1,
                name: "Updated Network",
                type: {
                    code: "fixed-network",
                    value: "Fixed Network",
                    type: "Fixed",
                    annualQuantityOfGo: 1000,
                    country: "France",
                },
                yearlyQuantityOfGbExchanged: 1500,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", existingNetwork);

            expect(mockInPhysicalEquipmentsService.update).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    id: 1,
                    name: "Updated Network",
                    type: "Network",
                    model: "fixed-network",
                    quantity: 1.5, // 1500 / 1000
                }),
            );
            expect(mockDigitalServiceStore.initInPhysicalEquipments).toHaveBeenCalledWith(
                "version-123",
            );
            expect(mockDigitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
        });

        it("should cancel action without saving", async () => {
            const network = {
                name: "Test Network",
                type: mockNetworkTypes[0],
            } as any;

            await component.actionNetwork("cancel", network);

            expect(mockInPhysicalEquipmentsService.create).not.toHaveBeenCalled();
            expect(mockInPhysicalEquipmentsService.update).not.toHaveBeenCalled();
            expect(component.sidebarVisible).toBe(false);
        });

        it("should delete network via inPhysicalEquipmentsService", async () => {
            const networkToDelete = {
                id: 1,
                name: "Network to Delete",
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.deleteItem(networkToDelete);

            expect(mockInPhysicalEquipmentsService.delete).toHaveBeenCalledWith({
                id: 1,
                digitalServiceVersionUid: "version-123",
            });
            expect(mockDigitalServiceStore.initInPhysicalEquipments).toHaveBeenCalledWith(
                "version-123",
            );
            expect(mockDigitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
        });
    });

    describe("Quantity Calculation", () => {
        it("should calculate quantity for Fixed network type", async () => {
            const network = {
                name: "Fixed Network",
                type: {
                    code: "fixed-network",
                    value: "Fixed Network",
                    type: "Fixed",
                    annualQuantityOfGo: 1000,
                    country: "France",
                },
                yearlyQuantityOfGbExchanged: 5000,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", network);

            // For Fixed type: quantity = yearlyQuantityOfGbExchanged / annualQuantityOfGo
            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    quantity: 5, // 5000 / 1000
                }),
            );
        });

        it("should calculate quantity for Mobile network type", async () => {
            const network = {
                name: "Mobile Network",
                type: {
                    code: "mobile-network",
                    value: "Mobile Network",
                    type: "Mobile",
                    annualQuantityOfGo: 0,
                    country: "Germany",
                },
                yearlyQuantityOfGbExchanged: 7500,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", network);

            // For Mobile type: quantity = yearlyQuantityOfGbExchanged
            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    quantity: 7500,
                }),
            );
        });

        it("should return 0 quantity when yearlyQuantityOfGbExchanged is 0", async () => {
            const network = {
                name: "Zero Network",
                type: mockNetworkTypes[0],
                yearlyQuantityOfGbExchanged: 0,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", network);

            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    quantity: 0,
                }),
            );
        });

        it("should return 0 quantity when Fixed type has zero annualQuantityOfGo", async () => {
            const network = {
                name: "Zero Annual Network",
                type: {
                    code: "fixed-network",
                    value: "Fixed Network",
                    type: "Fixed",
                    annualQuantityOfGo: 0,
                    country: "France",
                },
                yearlyQuantityOfGbExchanged: 1000,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", network);

            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    quantity: 0,
                }),
            );
        });
    });

    describe("Network Data Computed Property", () => {
        it("should filter and transform network type equipment", () => {
            const result = component.networkData();
            // Only networks, not servers
            expect(result[0].name).toBe("Network 1");
            expect(result[0].typeCode).toBe("Fixed Network");
        });

        it("should calculate yearlyQuantityOfGbExchanged for Fixed network", () => {
            const result = component.networkData();

            const fixedNetwork = result.find((n) => n.name === "Network 1");
            // Fixed type: yearlyQuantityOfGbExchanged = annualQuantityOfGo * quantity
            // 1000 * 0.5 = 500
            expect(fixedNetwork?.yearlyQuantityOfGbExchanged).toBe(500);
        });

        it("should use quantity as yearlyQuantityOfGbExchanged for Mobile network", () => {
            const result = component.networkData();

            const mobileNetwork = result.find((n) => n.name === "Network 2");
            // Mobile type: yearlyQuantityOfGbExchanged = quantity
            expect(mobileNetwork?.yearlyQuantityOfGbExchanged).toBe(2000);
        });

        it("should map network type code to value", () => {
            const result = component.networkData();

            expect(result[0].typeCode).toBe("Fixed Network");
            expect(result[1].typeCode).toBe("Mobile Network");
        });

        it("should handle equipment with unknown network type", () => {
            const equipmentsWithUnknownType = [
                {
                    id: 4,
                    name: "Unknown Network",
                    type: "Network",
                    model: "unknown-model",
                    quantity: 100,
                    location: "Italy",
                    creationDate: Date.now(),
                    digitalServiceUid: "test-uid",
                },
            ];
            mockDigitalServiceStore.inPhysicalEquipments.and.returnValue(
                equipmentsWithUnknownType,
            );

            const result = component.networkData();

            expect(result[0].typeCode).toBe("Fixed Network");
        });

        it("should only include Network type equipment", () => {
            const result = component.networkData();

            result.forEach((item) => {
                expect(item.name).not.toContain("Server");
            });
        });

        it("should preserve all relevant network properties", () => {
            const result = component.networkData();

            expect(result[0]).toEqual(
                jasmine.objectContaining({
                    id: 1,
                    name: "Network 1",
                    typeCode: "Fixed Network",
                    yearlyQuantityOfGbExchanged: 500,
                    digitalServiceUid: "test-uid",
                }),
            );
        });
    });

    describe("Date Calculations", () => {
        it("should set datePurchase to 2020-01-01", async () => {
            const network = {
                name: "Test Network",
                type: mockNetworkTypes[0],
                yearlyQuantityOfGbExchanged: 1000,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", network);

            const createdCall =
                mockInPhysicalEquipmentsService.create.calls.mostRecent().args[0];
            const datePurchase = new Date(createdCall.datePurchase);

            expect(datePurchase.getFullYear()).toBe(2020);
            expect(datePurchase.getMonth()).toBe(0); // January
            expect(datePurchase.getDate()).toBe(1);
        });

        it("should set dateWithdrawal to 1 year after purchase", async () => {
            const network = {
                name: "Test Network",
                type: mockNetworkTypes[0],
                yearlyQuantityOfGbExchanged: 1000,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", network);

            const createdCall =
                mockInPhysicalEquipmentsService.create.calls.mostRecent().args[0];
            const datePurchase = new Date(createdCall.datePurchase);
            const dateWithdrawal = new Date(createdCall.dateWithdrawal);

            const yearsDiff = dateWithdrawal.getFullYear() - datePurchase.getFullYear();
            expect(yearsDiff).toBe(1);
        });
    });

    describe("Edge Cases", () => {
        it("should handle network with null yearlyQuantityOfGbExchanged", async () => {
            const network = {
                name: "Null Network",
                type: mockNetworkTypes[0],
                yearlyQuantityOfGbExchanged: null as any,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.actionNetwork("save", network);

            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    quantity: 0,
                }),
            );
        });

        it("should handle network with undefined type", async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.network = {
                name: "No Type Network",
                type: undefined as any,
            } as any;

            component.resetNetwork();

            expect(component.network.type).toBeDefined();
        });

        it("should handle multiple networks with same name pattern", async () => {
            mockDigitalServicesBusiness.getNextAvailableName.and.returnValue(
                "Network 10",
            );
            component.ngOnInit();
            await fixture.whenStable();

            component.resetNetwork();

            expect(component.network.name).toBe("Network 10");
        });
    });
});
