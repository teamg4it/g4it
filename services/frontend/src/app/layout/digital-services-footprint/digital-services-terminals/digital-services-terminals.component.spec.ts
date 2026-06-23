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
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { SharedModule } from "./../../../core/shared/shared.module";
import { DigitalServicesTerminalsSidePanelComponent } from "./digital-services-terminals-side-panel/digital-services-terminals-side-panel.component";
import { DigitalServicesTerminalsComponent } from "./digital-services-terminals.component";

describe("DigitalServicesTerminalsComponent", () => {
    let component: DigitalServicesTerminalsComponent;
    let fixture: ComponentFixture<DigitalServicesTerminalsComponent>;
    let mockDigitalServiceDataService: any;
    let mockInPhysicalEquipmentsService: any;
    let mockDigitalServiceStore: any;
    let mockUserService: any;

    const mockDigitalService = {
        name: "Test Digital Service",
        uid: "test-uid",
        creationDate: Date.now(),
        lastUpdateDate: Date.now(),
        lastCalculationDate: null,
        networks: [],
        servers: [],
        terminals: [
            {
                uid: "terminal-1",
                name: "Test Terminal",
                type: { code: "mobile-fix", value: "Mobile", lifespan: 5 },
                country: "France",
                numberOfUsers: 100,
                yearlyUsageTimePerUser: 200,
                lifespan: 3,
            },
        ],
        enableDataInconsistency: false,
        activeDsvUid: "1",
    } as DigitalService;

    const mockInPhysicalEquipments = [
        {
            id: 1,
            name: "Terminal 1",
            type: "Terminal",
            model: "mobile-fix",
            location: "France",
            numberOfUsers: 100,
            durationHour: 200,
            datePurchase: new Date("2020-01-01"),
            dateWithdrawal: new Date("2023-01-01"),
            creationDate: Date.now(),
            digitalServiceUid: "test-uid",
        },
        {
            id: 2,
            name: "Server 1",
            type: "Server",
            model: "server-model",
            location: "Germany",
        },
    ];

    const mockDeviceTypes = [
        { code: "mobile-fix", value: "Mobile", lifespan: 5 },
        { code: "laptop", value: "Laptop", lifespan: 4 },
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
            terminalDeviceTypes: jasmine.createSpy().and.returnValue(mockDeviceTypes),
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

        await TestBed.configureTestingModule({
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
            ],
            imports: [
                SharedModule,
                TableModule,
                DrawerModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                DigitalServicesTerminalsComponent,
                DigitalServicesTerminalsSidePanelComponent,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).overrideComponent(DigitalServicesTerminalsComponent, {
            set: {
                providers: [
                    {
                        provide: DigitalServiceStoreService,
                        useValue: mockDigitalServiceStore,
                    },
                ],
            },
        });
        fixture = TestBed.createComponent(DigitalServicesTerminalsComponent);
        component = fixture.componentInstance;

        // Override injected services
        (component as any).inPhysicalEquipmentsService = mockInPhysicalEquipmentsService;
        (component as any).digitalServiceStore = mockDigitalServiceStore;

        fixture.detectChanges();
    });

    describe("Component Initialization", () => {
        it("should create", () => {
            expect(component).toBeTruthy();
        });

        it("should initialize with default values", () => {
            expect(component.sidebarVisible).toBeFalse();
            expect(component.sidebarPurpose).toBe("");
            expect(component.headerFields).toEqual([
                "name",
                "typeCode",
                "country",
                "numberOfUsers",
                "yearlyUsageTimePerUser",
                "lifespan",
            ]);
        });

        it("should subscribe to digitalService$ on init", () => {
            component.ngOnInit();
            expect(component.digitalService).toEqual(mockDigitalService);
        });
    });

    describe("Terminal Management", () => {
        it("should set the terminal when setTerminal is called", () => {
            const testTerminal = {
                uid: "randomUID",
                creationDate: 1700746167.59006,
                name: "name",
                type: {
                    code: "mobile-fix",
                    value: "Mobile",
                    lifespan: 5,
                },
                lifespan: 3,
                country: "France",
                numberOfUsers: 1,
                yearlyUsageTimePerUser: 17,
                idFront: 0,
            };

            component.setTerminal(testTerminal, 0);

            expect(component.terminal).toEqual(testTerminal);
            expect(component.terminal.idFront).toBe(0);
        });

        it("should reset terminal to empty object", () => {
            component.terminal = {
                name: "Test Terminal",
                type: { code: "mobile-fix", value: "Mobile", lifespan: 5 },
                country: "France",
                numberOfUsers: 100,
            } as any;

            component.resetTerminal();

            expect(component.terminal).toEqual({} as any);
        });

        it("should set item with event data", () => {
            const event = {
                index: 2,
                name: "Test Terminal",
                type: { code: "laptop", value: "Laptop", lifespan: 4 },
                country: "Germany",
                numberOfUsers: 50,
            };

            component.setItem(event);

            expect(component.terminal.name).toBe("Test Terminal");
            expect(component.terminal.idFront).toBe(2);
            expect(event.index).toBeUndefined();
        });
    });

    describe("Sidebar Management", () => {
        it("should toggle sidebar visibility", () => {
            component.sidebarVisible = false;
            component.changeSidebar(true);
            expect(component.sidebarVisible).toBeTrue();

            component.changeSidebar(false);
            expect(component.sidebarVisible).toBeFalse();
        });
    });

    describe("Terminal CRUD Operations", () => {
        it("should create a new terminal", async () => {
            const newTerminal = {
                name: "New Terminal",
                type: { code: "laptop", value: "Laptop", lifespan: 4 },
                country: "Germany",
                numberOfUsers: 50,
                yearlyUsageTimePerUser: 150,
                lifespan: 4,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.updateTerminals(newTerminal);

            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    name: "New Terminal",
                    type: "Terminal",
                    model: "laptop",
                    location: "Germany",
                    numberOfUsers: 50,
                    durationHour: 150,
                }),
            );
            expect(mockDigitalServiceStore.initInPhysicalEquipments).toHaveBeenCalledWith(
                "version-123",
            );
            expect(mockDigitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
        });

        it("should update an existing terminal", async () => {
            const existingTerminal = {
                id: 1,
                name: "Updated Terminal",
                type: { code: "mobile-fix", value: "Mobile", lifespan: 5 },
                country: "France",
                numberOfUsers: 200,
                yearlyUsageTimePerUser: 300,
                lifespan: 5,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.updateTerminals(existingTerminal);

            expect(mockInPhysicalEquipmentsService.update).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    id: 1,
                    name: "Updated Terminal",
                    type: "Terminal",
                    model: "mobile-fix",
                    location: "France",
                    numberOfUsers: 200,
                    durationHour: 300,
                }),
            );
            expect(mockDigitalServiceStore.initInPhysicalEquipments).toHaveBeenCalledWith(
                "version-123",
            );
            expect(mockDigitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
        });

        it("should calculate quantity correctly in updateTerminals", async () => {
            const terminal = {
                name: "Test Terminal",
                type: { code: "laptop", value: "Laptop", lifespan: 4 },
                country: "Germany",
                numberOfUsers: 100,
                yearlyUsageTimePerUser: 876, // 876 hours per year
                lifespan: 4,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.updateTerminals(terminal);

            const expectedQuantity = (100 * 876) / (365 * 24); // Should equal 10
            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    quantity: expectedQuantity,
                }),
            );
        });

        it("should delete item via inPhysicalEquipmentsService", async () => {
            const terminalToDelete = {
                id: 1,
                name: "Terminal to Delete",
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.deleteItem(terminalToDelete);

            expect(mockInPhysicalEquipmentsService.delete).toHaveBeenCalledWith({
                id: 1,
                digitalServiceUid: "test-uid",
                digitalServiceVersionUid: "version-123",
            });
            expect(mockDigitalServiceStore.initInPhysicalEquipments).toHaveBeenCalledWith(
                "version-123",
            );
            expect(mockDigitalServiceStore.setEnableCalcul).toHaveBeenCalledWith(true);
        });

        it("should delete terminal from digitalService.terminals array", async () => {
            component.digitalService = { ...mockDigitalService };
            fixture.componentRef.setInput("dsVersionUid", "version-123");

            const terminalToDelete = {
                uid: "terminal-1",
                name: "Test Terminal",
            } as any;

            await component.deleteTerminals(terminalToDelete);

            expect(mockDigitalServiceDataService.update).toHaveBeenCalled();
            expect(mockDigitalServiceDataService.get).toHaveBeenCalledWith("version-123");
        });

        it("should handle deleting non-existent terminal", async () => {
            component.digitalService = { ...mockDigitalService };
            fixture.componentRef.setInput("dsVersionUid", "version-123");

            const terminalToDelete = {
                uid: "non-existent-uid",
                name: "Non-existent Terminal",
            } as any;

            await component.deleteTerminals(terminalToDelete);

            expect(mockDigitalServiceDataService.update).toHaveBeenCalled();
        });

        it("should handle digitalService with no terminals", async () => {
            component.digitalService = {
                ...mockDigitalService,
                terminals: [] as any,
            } as any;
            fixture.componentRef.setInput("dsVersionUid", "version-123");

            const terminalToDelete = {
                uid: "terminal-1",
                name: "Test Terminal",
            } as any;

            await component.deleteTerminals(terminalToDelete);

            expect(mockDigitalServiceDataService.update).toHaveBeenCalled();
        });
    });

    describe("Terminal Data Computed Property", () => {
        it("should filter and transform terminal type equipment", () => {
            const result = component.terminalData();

            expect(result[0].name).toBe("Terminal 1");
            expect(result[0].typeCode).toBe("Mobile");
            expect(result[0].country).toBe("France");
            expect(result[0].numberOfUsers).toBe(100);
        });

        it("should calculate lifespan in years from date difference", () => {
            const result = component.terminalData();

            // Difference between 2023-01-01 and 2020-01-01 is 3 years
            expect(result[0].lifespan).toBeCloseTo(3, 0);
        });

        it("should map device type code to value", () => {
            const customEquipments = [
                {
                    id: 3,
                    name: "Laptop 1",
                    type: "Terminal",
                    model: "laptop",
                    location: "Spain",
                    numberOfUsers: 75,
                    durationHour: 180,
                    datePurchase: new Date("2021-01-01"),
                    dateWithdrawal: new Date("2025-01-01"),
                    creationDate: Date.now(),
                    digitalServiceUid: "test-uid",
                },
            ];
            mockDigitalServiceStore.inPhysicalEquipments.and.returnValue(
                customEquipments,
            );

            const result = component.terminalData();

            expect(result[0].typeCode).toBe("Mobile");
        });

        it("should handle equipment with unknown device type", () => {
            const equipmentsWithUnknownType = [
                {
                    id: 4,
                    name: "Unknown Terminal",
                    type: "Terminal",
                    model: "unknown-model",
                    location: "Italy",
                    numberOfUsers: 30,
                    durationHour: 100,
                    datePurchase: new Date("2022-01-01"),
                    dateWithdrawal: new Date("2024-01-01"),
                    creationDate: Date.now(),
                    digitalServiceUid: "test-uid",
                },
            ];
            mockDigitalServiceStore.inPhysicalEquipments.and.returnValue(
                equipmentsWithUnknownType,
            );

            const result = component.terminalData();

            expect(result[0].typeCode).toBe("Mobile");
        });

        it("should only include Terminal type equipment", () => {
            const result = component.terminalData();

            // Should only have 1 terminal (not the server)

            result.forEach((item) => {
                expect(item.name).not.toContain("Server");
            });
        });

        it("should preserve all relevant terminal properties", () => {
            const result = component.terminalData();

            expect(result[0]).toEqual(
                jasmine.objectContaining({
                    id: 1,
                    name: "Terminal 1",
                    typeCode: "Mobile",
                    country: "France",
                    numberOfUsers: 100,
                    yearlyUsageTimePerUser: 200,
                    digitalServiceUid: "test-uid",
                }),
            );
        });
    });

    describe("Date Calculations", () => {
        it("should calculate dateWithdrawal based on lifespan", async () => {
            const terminal = {
                name: "Test Terminal",
                type: { code: "laptop", value: "Laptop", lifespan: 4 },
                country: "Germany",
                numberOfUsers: 50,
                yearlyUsageTimePerUser: 150,
                lifespan: 5, // 5 years
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.updateTerminals(terminal);

            // Should add 5 years (5 * 365 days) to 2020-01-01
            const createdCall =
                mockInPhysicalEquipmentsService.create.calls.mostRecent().args[0];
            const datePurchase = new Date(createdCall.datePurchase);
            const dateWithdrawal = new Date(createdCall.dateWithdrawal);

            const daysDiff = Math.round(
                (dateWithdrawal.getTime() - datePurchase.getTime()) /
                    (1000 * 60 * 60 * 24),
            );
            expect(daysDiff).toBe(5 * 365);
        });
    });

    describe("Edge Cases", () => {
        it("should handle empty terminals array in digitalService", () => {
            component.digitalService = {
                ...mockDigitalService,
                terminals: [],
            };

            expect(component.digitalService.terminals).toEqual([]);
        });

        it("should handle terminal with zero users", async () => {
            const terminal = {
                name: "Zero Users Terminal",
                type: { code: "laptop", value: "Laptop", lifespan: 4 },
                country: "Germany",
                numberOfUsers: 0,
                yearlyUsageTimePerUser: 150,
                lifespan: 4,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.updateTerminals(terminal);

            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    numberOfUsers: 0,
                    quantity: 0,
                }),
            );
        });

        it("should handle terminal with zero usage time", async () => {
            const terminal = {
                name: "Zero Usage Terminal",
                type: { code: "laptop", value: "Laptop", lifespan: 4 },
                country: "Germany",
                numberOfUsers: 50,
                yearlyUsageTimePerUser: 0,
                lifespan: 4,
                digitalServiceUid: "test-uid",
            } as any;

            fixture.componentRef.setInput("dsVersionUid", "version-123");

            await component.updateTerminals(terminal);

            expect(mockInPhysicalEquipmentsService.create).toHaveBeenCalledWith(
                jasmine.objectContaining({
                    durationHour: 0,
                    quantity: 0,
                }),
            );
        });
    });
});
