import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService } from "primeng/api";
import { of } from "rxjs";
import { InventoryItemComponent } from "./inventory-item.component";

import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { EvaluationDataService } from "src/app/core/service/data/evaluation-data.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";

describe("InventoryItemComponent", () => {
    let component: InventoryItemComponent;
    let fixture: ComponentFixture<InventoryItemComponent>;

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
            declarations: [InventoryItemComponent],
            providers: [
                { provide: ConfirmationService, useValue: confirmationService },
                { provide: InventoryService, useValue: inventoryService },
                { provide: EvaluationDataService, useValue: evaluationService },
                { provide: FootprintDataService, useValue: footprintService },
                { provide: Router, useValue: router },
                { provide: ActivatedRoute, useValue: {} },
                {
                    provide: TranslateService,
                    useValue: { instant: (k: string) => k },
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
                    },
                },
                { provide: GlobalStoreService, useValue: globalStore },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(InventoryItemComponent);
        component = fixture.componentInstance;

        component.inventory = {
            id: 1,
            name: "Inventory 1",
            physicalEquipmentCount: 1,
            virtualEquipmentCount: 0,
            applicationCount: 1,
            lastTaskEvaluating: { status: "RUNNING" },
            tasks: [{ type: "LOADING" }, { type: "EVALUATING" }],
        } as any;

        fixture.detectChanges();
    });
    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should split loading and evaluating tasks on init", () => {
        expect(component.taskLoading.length).toBe(1);
        expect(component.taskEvaluating.length).toBe(1);
    });

    it("should return true if evaluating task is running", () => {
        spyOn(Constants.EVALUATION_BATCH_RUNNING_STATUSES, "includes").and.returnValue(
            true,
        );
        expect(component.isTaskRunning()).toBeTrue();
    });

    it("should return false if no evaluating task", () => {
        component.inventory.lastTaskEvaluating = undefined;
        expect(component.isTaskRunning()).toBeFalse();
    });

    it("should show equipment when equipment exists", () => {
        expect(component.showEquipment()).toBeTrue();
    });

    it("should show application when application exists", () => {
        expect(component.showApplication()).toBeTrue();
    });

    it("should navigate to equipment footprint", () => {
        component.redirectFootprint("equipment");

        expect(router.navigate).toHaveBeenCalled();
    });

    it("should not navigate if no evaluating task", () => {
        component.inventory.lastTaskEvaluating = undefined;
        component.redirectFootprint("equipment");

        expect(router.navigate).not.toHaveBeenCalled();
    });

    it("should disable estimation if no equipment", () => {
        component.inventory.physicalEquipmentCount = 0;
        component.inventory.virtualEquipmentCount = 0;

        expect(component.isEstimationDisabled()).toBeTrue();
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
