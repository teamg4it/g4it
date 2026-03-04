import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateService } from "@ngx-translate/core";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { CriteriaPopupComponent } from "./criteria-popup.component";

describe("CriteriaPopupComponent", () => {
    let component: CriteriaPopupComponent;
    let fixture: ComponentFixture<CriteriaPopupComponent>;
    let translateMock: any;
    let globalStoreMock: any;

    beforeEach(async () => {
        translateMock = {
            instant: jasmine.createSpy("instant").and.callFake((key, params) => {
                return `${key}-${JSON.stringify(params)}`;
            }),
            currentLang: "en",
        };

        globalStoreMock = {
            criteriaList: () => ({
                A: true,
                B: true,
                C: true,
                D: true,
                E: true,
                F: true,
            }),
        };

        await TestBed.configureTestingModule({
            declarations: [CriteriaPopupComponent],
            providers: [
                MonthYearPipe,
                { provide: TranslateService, useValue: translateMock },
                { provide: GlobalStoreService, useValue: globalStoreMock },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(CriteriaPopupComponent);
        component = fixture.componentInstance;

        component.organizationDetails = { name: "Org", criteria: ["A", "B"] } as any;
        component.workspaceDetails = {
            workspaceName: "WS",
            organizationId: "1",
            status: "ACTIVE",
            criteriaIs: ["C"],
            criteriaDs: ["D"],
        } as any;

        component.inventory = { id: 1, name: "01-2024", note: "" };
        component.ds = { uid: "1", name: "DS" };
    });
    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should reset criteria list when popup opens", () => {
        component.displayPopup = true;

        component.ngOnChanges({
            displayPopup: {
                currentValue: true,
                previousValue: false,
                firstChange: false,
                isFirstChange: () => false,
            },
        });

        expect(component.criteriaList.length).toBe(6);
        expect(component.hasChanged).toBeFalse();
    });

    it("should copy selected criteria when workspace type", () => {
        component.type = "workspace";
        component.selectedCriteriaIS = ["A"];
        component.selectedCriteriaDS = ["B"];

        component.ngOnChanges({
            selectedCriteriaIS: {} as any,
            selectedCriteriaDS: {} as any,
        });

        expect(component.tempSelectedCriteriaIS).toEqual(["A"]);
        expect(component.tempSelectedCriteriaDS).toEqual(["B"]);
    });

    it("should reset criteria to organization defaults", () => {
        component.type = "organization";
        component.selectedCriteriaIS = ["X"];

        component.resetToDefault();

        expect(component.selectedCriteriaIS).toEqual(component.defaultCriteria);
        expect(component.hasChanged).toBeTrue();
    });

    it("should reset workspace criteria from organization", () => {
        component.type = "workspace";

        component.resetToDefault();

        expect(component.selectedCriteriaIS).toEqual(["A", "B"]);
        expect(component.selectedCriteriaDS).toEqual(["A", "B"]);
    });

    it("should emit organization criteria on save", () => {
        component.type = "organization";
        component.selectedCriteriaIS = ["A"];

        spyOn(component.outSaveOrganization, "emit");

        component.saveChanges();

        expect(component.outSaveOrganization.emit).toHaveBeenCalledWith({
            criteria: ["A"],
        });
    });

    it("should emit workspace criteria on save", () => {
        component.type = "workspace";
        component.selectedCriteriaIS = ["A"];
        component.selectedCriteriaDS = ["B"];

        spyOn(component.outSaveWorkspace, "emit");

        component.saveChanges();

        expect(component.outSaveWorkspace.emit).toHaveBeenCalled();
    });

    it("should emit inventory criteria on save", () => {
        component.type = "inventory";
        component.selectedCriteriaIS = ["A"];

        spyOn(component.outSaveInventory, "emit");

        component.saveChanges();

        expect(component.outSaveInventory.emit).toHaveBeenCalled();
    });

    it("should emit ds criteria on save", () => {
        component.type = "ds";
        component.selectedCriteriaIS = ["A"];

        spyOn(component.outSaveDS, "emit");

        component.saveChanges();

        expect(component.outSaveDS.emit).toHaveBeenCalled();
    });

    it("should return organization header", () => {
        component.type = "organization";

        const header = component.getHeader();

        expect(translateMock.instant).toHaveBeenCalled();
        expect(header).toContain("choose-criteria");
    });

    it("should return inventory header with transformed date", () => {
        component.type = "inventory";

        const header = component.getHeader();

        expect(header).toContain("inventories.choose-criteria");
    });

    it("should select all IS criteria when All is selected", () => {
        component.onAllSelectedChange(["All"], true);

        expect(component.selectedCriteriaIS.length).toBe(6);
        expect(component.hasChanged).toBeTrue();
    });

    it("should clear DS criteria when All is not selected", () => {
        component.onAllSelectedChange([], false);

        expect(component.selectedCriteriaDS).toEqual([]);
    });

    it("should restore temp selections and emit close", () => {
        component.tempSelectedCriteriaIS = ["A"];
        component.tempSelectedCriteriaDS = ["B"];

        spyOn(component.outClose, "emit");

        component.closePopup();

        expect(component.selectedCriteriaIS).toEqual(["A"]);
        expect(component.selectedCriteriaDS).toEqual(["B"]);
        expect(component.outClose.emit).toHaveBeenCalled();
    });
});
