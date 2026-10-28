import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService } from "primeng/api";
import { of, Subject } from "rxjs";
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
    let inventory: any;

    let inventoryService: jasmine.SpyObj<InventoryService>;
    let evaluationService: jasmine.SpyObj<EvaluationDataService>;
    let footprintService: jasmine.SpyObj<FootprintDataService>;
    let router: jasmine.SpyObj<Router>;
    let route: ActivatedRoute;
    let globalStore: any;

    beforeEach(async () => {
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
            imports: [InventoryItemComponent],
            providers: [
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
        })
            .overrideComponent(InventoryItemComponent, { set: { template: "" } })
            .compileComponents();

        fixture = TestBed.createComponent(InventoryItemComponent);
        component = fixture.componentInstance;
        route = TestBed.inject(ActivatedRoute);
        inventory = {
            id: 1,
            name: "Inventory 1",
            creationDate: new Date("2024-01-01T12:00:00"),
            lastUpdateDate: new Date("2024-01-01T12:00:00"),
            physicalEquipmentCount: 1,
            virtualEquipmentCount: 0,
            applicationCount: 1,
            lastTaskEvaluating: {
                status: "IN_PROGRESS",
                creationDate: new Date("2024-01-01T12:00:00"),
            },
            tasks: [
                {
                    id: 1,
                    type: "LOADING",
                    status: "IN_PROGRESS",
                    creationDate: new Date("2024-01-01T12:00:00"),
                },
                {
                    id: 2,
                    type: "EVALUATING",
                    status: "IN_PROGRESS",
                    creationDate: new Date("2024-01-01T12:00:00"),
                },
            ],
        } as any;

        fixture.componentRef.setInput("inventory", inventory);
        fixture.detectChanges();
    });

    function setInventory(updates: Record<string, unknown>) {
        inventory = {
            ...inventory,
            ...updates,
        };
        fixture.componentRef.setInput("inventory", inventory);
        fixture.detectChanges();
    }

    it("should disable estimation if no equipment", async () => {
        setInventory({
            physicalEquipmentCount: 0,
            virtualEquipmentCount: 0,
            cloudServiceCount: 0,
            aiServiceCount: 0,
            lastTaskEvaluating: undefined,
            tasks: [],
        });
        await fixture.whenStable();

        expect(component.isEstimationDisabled()).toBeTrue();
    });

    it("should enable estimation otherwise", () => {
        setInventory({
            lastTaskLoading: undefined,
            lastTaskEvaluating: { status: "COMPLETED" },
        });
        expect(component.isEstimationDisabled()).toBeFalse();
    });

    it("should enable estimation if only AI services are present", async () => {
        setInventory({
            physicalEquipmentCount: 0,
            virtualEquipmentCount: 0,
            cloudServiceCount: 0,
            applicationCount: 0,
            aiServiceCount: 2,
            lastTaskEvaluating: undefined,
            tasks: [],
        });
        await fixture.whenStable();

        expect(component.isEstimationDisabled()).toBeFalse();
    });

    it("should split tasks, show available inventory sections, and report running status", () => {
        setInventory({
            lastTaskEvaluating: { status: "IN_PROGRESS" },
            physicalEquipmentCount: 1,
            applicationCount: 1,
            tasks: [{ type: "LOADING" }, { type: "EVALUATING" }, { type: "OTHER" }],
        });

        expect(component.taskLoading()).toHaveSize(1);
        expect(component.taskEvaluating()).toHaveSize(1);
        expect(component.isTaskRunning()).toBeTrue();
        expect(component.showEquipment()).toBeTrue();
        expect(component.showApplication()).toBeTrue();

        setInventory({ lastTaskEvaluating: undefined });
        expect(component.isTaskRunning()).toBeFalse();
        expect(component.showEquipment()).toBeFalsy();
        expect(component.showApplication()).toBeFalsy();

        for (const count of [
            { virtualEquipmentCount: 1 },
            { cloudServiceCount: 1 },
            { aiServiceCount: 1 },
        ]) {
            setInventory({
                physicalEquipmentCount: 0,
                virtualEquipmentCount: 0,
                cloudServiceCount: 0,
                aiServiceCount: 0,
                ...count,
                lastTaskEvaluating: { status: "IN_PROGRESS" },
            });
            expect(component.showEquipment()).toBeTrue();
        }
    });

    it("should apply configured limits and expiry reminder", () => {
        expect(component.equipmentLimitExceed()).toBeFalse();
        expect(component.applicationLimitExceed()).toBeFalse();
        expect(component.showExpiryMessage()).toBeFalse();

        setInventory({
            outVirtualCount: 300001,
            outApplicationCount: 300001,
            expiryDate: "01/01/2000",
        });

        expect(component.equipmentLimitExceed()).toBeTrue();
        expect(component.applicationLimitExceed()).toBeTrue();
        expect(component.showExpiryMessage()).toBeTrue();

        setInventory({ outVirtualCount: 0, outAiCount: 300001 });
        expect(component.equipmentLimitExceed()).toBeTrue();
    });

    it("should disable estimation while either task is running", () => {
        setInventory({
            physicalEquipmentCount: 1,
            lastTaskLoading: { status: "IN_PROGRESS" },
            lastTaskEvaluating: undefined,
        });
        expect(component.isEstimationDisabled()).toBeTrue();

        setInventory({
            lastTaskLoading: undefined,
            lastTaskEvaluating: { status: "IN_PROGRESS" },
        });
        expect(component.isEstimationDisabled()).toBeTrue();
    });

    it("should emit the selected tab state and inventory actions", () => {
        spyOn(component.openTab, "emit");
        spyOn(component.closeTab, "emit");
        spyOn(component.openSidebarForUploadInventory, "emit");
        spyOn(component.openSidebarForNote, "emit");
        spyOn(component.renewInventoryId, "emit");

        component.onSelectedChange(1, true);
        component.onSelectedChange(1, false);
        component.onSelectedChange(1, undefined);
        component.openSidebarUploadFile();
        component.openSidebarNote();
        component.renewService({ id: 2 });

        expect(component.openTab.emit).toHaveBeenCalledOnceWith(1);
        expect(component.closeTab.emit).toHaveBeenCalledOnceWith(1);
        expect(component.openSidebarForUploadInventory.emit).toHaveBeenCalledOnceWith(1);
        expect(component.openSidebarForNote.emit).toHaveBeenCalledOnceWith(1);
        expect(component.renewInventoryId.emit).toHaveBeenCalledOnceWith(2);
        expect(component.trackByFn(3)).toBe(3);
    });

    it("should select inventory criteria before workspace criteria for the popup", () => {
        setInventory({ criteria: ["inventory-criteria"] });
        component.displayPopupFct();
        expect(component.selectedCriteria).toEqual(["inventory-criteria"]);
        expect(component.displayPopup).toBeTrue();

        setInventory({ criteria: [] });
        component.displayPopupFct();
        expect(component.selectedCriteria).toEqual(["B"]);

        component.workspace.criteriaIs = undefined as any;
        component.displayPopupFct();
        expect(component.selectedCriteria).toEqual(["A"]);

        component.organization.criteria = undefined as any;
        component.displayPopupFct();
        expect(component.selectedCriteria).toEqual(["A", "B", "C", "D", "E"]);
    });

    it("should navigate to footprints only when the corresponding data exists", () => {
        setInventory({ lastTaskEvaluating: undefined });
        component.redirectFootprint("equipment");
        expect(router.navigate).not.toHaveBeenCalled();

        setInventory({
            id: 7,
            physicalEquipmentCount: 1,
            applicationCount: 1,
            lastTaskEvaluating: { criteria: ["criterion"] },
        });

        component.redirectFootprint("equipment");
        expect(router.navigate).toHaveBeenCalledWith(["7/footprint/criterion"], {
            relativeTo: route,
        });

        router.navigate.calls.reset();
        component.redirectFootprint("application");
        expect(router.navigate).toHaveBeenCalledWith(
            ["7/footprint/application/criterion"],
            {
                relativeTo: route,
            },
        );

        router.navigate.calls.reset();
        setInventory({
            criteria: ["first", "second"],
            lastTaskEvaluating: { criteria: ["first", "second"] },
        });
        component.redirectFootprint("equipment");
        expect(router.navigate).toHaveBeenCalledWith(
            [`7/footprint/${Constants.MUTLI_CRITERIA}`],
            { relativeTo: route },
        );

        router.navigate.calls.reset();
        setInventory({ physicalEquipmentCount: 0, applicationCount: 0 });
        component.redirectFootprint("equipment");
        component.redirectFootprint("application");
        component.redirectFootprint("unknown");
        expect(router.navigate).not.toHaveBeenCalled();
    });

    it("should use default criteria when an evaluation has no selected criteria", () => {
        setInventory({
            physicalEquipmentCount: 1,
            lastTaskEvaluating: {},
        });
        component.redirectFootprint("equipment");

        expect(router.navigate).toHaveBeenCalledWith(
            [`1/footprint/${Constants.MUTLI_CRITERIA}`],
            { relativeTo: route },
        );
    });

    it("should delete the inventory after confirmation", async () => {
        const confirm = fixture.debugElement.injector.get(ConfirmationService);
        const confirmSpy = spyOn(confirm, "confirm");
        footprintService.deleteIndicators.and.returnValue(of([]));
        inventoryService.deleteInventory.and.returnValue(of([]));
        spyOn(component.reloadInventoriesAndLoop, "emit");

        component.confirmDelete(new Event("click"));
        await confirmSpy.calls.mostRecent().args[0].accept?.();

        expect(footprintService.deleteIndicators).toHaveBeenCalledWith(1);
        expect(inventoryService.deleteInventory).toHaveBeenCalledWith(1);
        expect(component.reloadInventoriesAndLoop.emit).toHaveBeenCalled();
        expect(globalStore.setLoading).toHaveBeenCalledWith(true);
        expect(globalStore.setLoading).toHaveBeenCalledWith(false);
    });

    it("should launch an estimate and reload the inventory after confirmation", async () => {
        const confirm = fixture.debugElement.injector.get(ConfirmationService);
        const confirmSpy = spyOn(confirm, "confirm");
        evaluationService.launchEvaluating.and.returnValue(of(1));
        spyOn(component.reloadInventoryAndLoop, "emit");

        component.launchEstimate(new Event("click"));
        await confirmSpy.calls.mostRecent().args[0].accept?.();

        expect(evaluationService.launchEvaluating).toHaveBeenCalledWith(1);
        expect(component.reloadInventoryAndLoop.emit).toHaveBeenCalledWith(1);
    });
});
