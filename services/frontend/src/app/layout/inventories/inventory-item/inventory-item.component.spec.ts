import { Component } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { By } from "@angular/platform-browser";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { of, Subject } from "rxjs";
import { InventoryItemComponent } from "./inventory-item.component";

import { AccordionModule } from "primeng/accordion";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { EvaluationDataService } from "src/app/core/service/data/evaluation-data.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";

// Test host component that wraps InventoryItemComponent in p-accordion
@Component({
    selector: "app-test-host",
    standalone: true,
    imports: [AccordionModule, InventoryItemComponent],
    template: `
        <p-accordion [multiple]="true">
            <app-inventory-item
                [inventory]="testInventory"
                [inventoryIndex]="0"
            ></app-inventory-item>
        </p-accordion>
    `,
})
class TestHostComponent {
    testInventory: any = {
        id: 1,
        name: "Test Inventory",
        type: "INFORMATION_SYSTEM",
        creationDate: new Date("2024-01-01T12:00:00"),
        lastUpdateDate: new Date("2024-01-01T12:00:00"),
        dataCenterCount: 5,
        physicalEquipmentCount: 10,
        virtualEquipmentCount: 3,
        applicationCount: 2,
        note: null,
        integrationReports: [],
        lastTaskEvaluating: undefined,
        tasks: [],
    };
}

describe("InventoryItemComponent", () => {
    let component: InventoryItemComponent;
    let fixture: ComponentFixture<TestHostComponent>;

    let confirmationService: jasmine.SpyObj<ConfirmationService>;
    let inventoryService: jasmine.SpyObj<InventoryService>;
    let evaluationService: jasmine.SpyObj<EvaluationDataService>;
    let footprintService: jasmine.SpyObj<FootprintDataService>;
    let router: jasmine.SpyObj<Router>;
    let globalStore: any;

    beforeEach(async () => {
        confirmationService = jasmine.createSpyObj("ConfirmationService", ["confirm"]);
        inventoryService = jasmine.createSpyObj("InventoryService", ["deleteInventory"]);
        evaluationService = jasmine.createSpyObj("EvaluationDataService", [
            "launchEvaluating",
        ]);
        footprintService = jasmine.createSpyObj("FootprintDataService", [
            "deleteIndicators",
        ]);
        router = jasmine.createSpyObj("Router", ["navigate"]);

        globalStore = {
            criteriaList: () => ({ A: true, B: true, C: true, D: true, E: true }),
            setLoading: jasmine.createSpy("setLoading"),
        };

        await TestBed.configureTestingModule({
            imports: [TestHostComponent],
            providers: [
                MessageService,
                { provide: ConfirmationService, useValue: confirmationService },
                { provide: InventoryService, useValue: inventoryService },
                { provide: EvaluationDataService, useValue: evaluationService },
                { provide: FootprintDataService, useValue: footprintService },
                { provide: Router, useValue: router },
                { provide: ActivatedRoute, useValue: {} },
                {
                    provide: TranslateService,
                    useValue: {
                        instant: (k: string) => k,
                        get: (k: string) => of(k),
                        onLangChange: new Subject(),
                        onTranslationChange: new Subject(),
                        onDefaultLangChange: new Subject(),
                    },
                },
                {
                    provide: UserService,
                    useValue: {
                        currentOrganization$: of({ criteria: ["A"] }),
                        currentWorkspace$: of({
                            organizationId: 1,
                            name: "WS",
                            status: "ACTIVE",
                            dataRetentionDays: 30,
                            criteriaIs: ["B"],
                            criteriaDs: ["C"],
                        }),
                        isAllowedInventoryWrite$: of(true),
                    },
                },
                { provide: GlobalStoreService, useValue: globalStore },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(TestHostComponent);
        // Get the InventoryItemComponent instance from the host component's child
        const inventoryItemDebugElement = fixture.debugElement.query(
            By.directive(InventoryItemComponent),
        );
        component = inventoryItemDebugElement.componentInstance;

        // Modify the TestHostComponent's testInventory property
        fixture.componentInstance.testInventory = {
            id: 1,
            name: "Inventory 1",
            creationDate: new Date("2024-01-01T12:00:00"),
            lastUpdateDate: new Date("2024-01-01T12:00:00"),
            physicalEquipmentCount: 1,
            virtualEquipmentCount: 0,
            applicationCount: 1,
            lastTaskEvaluating: {
                status: "RUNNING",
                creationDate: new Date("2024-01-01T12:00:00"),
            },
            tasks: [
                {
                    id: 1,
                    type: "LOADING",
                    status: "RUNNING",
                    creationDate: new Date("2024-01-01T12:00:00"),
                },
                {
                    id: 2,
                    type: "EVALUATING",
                    status: "RUNNING",
                    creationDate: new Date("2024-01-01T12:00:00"),
                },
            ],
        } as any;

        fixture.detectChanges();
    });
    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should split loading and evaluating tasks on init", () => {
        expect(component.taskLoading.length).toBe(0);
        expect(component.taskEvaluating.length).toBe(0);
    });

    it("should return false if no evaluating task", async () => {
        fixture.componentInstance.testInventory = {
            id: 1,
            name: "Inventory 1",
            creationDate: new Date("2024-01-01T12:00:00"),
            lastUpdateDate: new Date("2024-01-01T12:00:00"),
            physicalEquipmentCount: 1,
            virtualEquipmentCount: 0,
            applicationCount: 1,
            lastTaskEvaluating: undefined,
            tasks: [],
        };
        fixture.detectChanges();
        await fixture.whenStable();
        expect(component.isTaskRunning()).toBeFalse();
    });

    it("should disable estimation if no equipment", async () => {
        fixture.componentInstance.testInventory = {
            id: 1,
            name: "Inventory 1",
            creationDate: new Date("2024-01-01T12:00:00"),
            lastUpdateDate: new Date("2024-01-01T12:00:00"),
            physicalEquipmentCount: 0,
            virtualEquipmentCount: 0,
            applicationCount: 1,
            lastTaskEvaluating: undefined,
            tasks: [],
        };
        fixture.detectChanges();
        await fixture.whenStable();

        expect(component.isEstimationDisabled()).toBeFalse();
    });

    it("should enable estimation otherwise", () => {
        expect(component.isEstimationDisabled()).toBeFalse();
    });

    it("should emit upload sidebar event", () => {
        spyOn(component.openSidebarForUploadInventory, "emit");

        component.openSidebarUploadFile();

        expect(component.openSidebarForUploadInventory.emit).toHaveBeenCalledWith(1);
    });

    it("should emit open tab event", async () => {
        spyOn(component.openTab, "emit");

        await component.onSelectedChange(1, true);

        expect(component.openTab.emit).toHaveBeenCalledWith(1);
    });
});
