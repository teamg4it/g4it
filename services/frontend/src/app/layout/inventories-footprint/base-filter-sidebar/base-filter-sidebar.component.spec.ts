/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { signal } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { TranslateModule, TranslatePipe } from "@ngx-translate/core";
import { AccordionModule } from "primeng/accordion";
import { BadgeModule } from "primeng/badge";
import { ButtonModule } from "primeng/button";
import { CheckboxModule } from "primeng/checkbox";
import { DrawerModule } from "primeng/drawer";
import { FocusTrapModule } from "primeng/focustrap";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { Filter } from "src/app/core/interfaces/filter.interface";
import { Constants } from "src/constants";
import { BaseFilterSidebarComponent, FilterTab } from "./base-filter-sidebar.component";

describe("BaseFilterSidebarComponent", () => {
    let component: BaseFilterSidebarComponent;
    let fixture: ComponentFixture<BaseFilterSidebarComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                DrawerModule,
                FocusTrapModule,
                ButtonModule,
                ScrollPanelModule,
                BadgeModule,
                AccordionModule,
                CheckboxModule,
                FormsModule,
                TranslateModule.forRoot(),
                BaseFilterSidebarComponent,
            ],
            providers: [TranslatePipe],
        }).compileComponents();

        fixture = TestBed.createComponent(BaseFilterSidebarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("Input properties", () => {
        it("should initialize with default values", () => {
            expect(component.visible).toBeFalse();
            expect(component.tabs).toEqual([]);
            expect(component.allFilters).toEqual({});
            expect(component.selectedFilterCount).toBe(0);
            expect(component.translationKeyPrefix).toBe(
                "inventories-footprint.filter-tabs",
            );
            expect(component.hasTreeStructure).toBeFalse();
            expect(component.useCustomContent).toBeFalse();
        });

        it("should accept visible input", () => {
            component.visible = true;
            expect(component.visible).toBeTrue();
        });

        it("should accept tabs input", () => {
            const tabs: FilterTab[] = ["tab1", "tab2"];
            component.tabs = tabs;
            expect(component.tabs).toEqual(tabs);
        });

        it("should accept allFilters input", () => {
            const filters: Filter<any> = { field1: ["value1"] };
            component.allFilters = filters;
            expect(component.allFilters).toEqual(filters);
        });

        it("should accept localFilters signal input", () => {
            const filters = signal<Filter<any>>({ field1: ["value1"] });
            component.localFilters = filters;
            expect(component.localFilters()).toEqual({ field1: ["value1"] });
        });

        it("should accept selectedFilterCount input", () => {
            component.selectedFilterCount = 5;
            expect(component.selectedFilterCount).toBe(5);
        });

        it("should accept translationKeyPrefix input", () => {
            component.translationKeyPrefix = "custom.prefix";
            expect(component.translationKeyPrefix).toBe("custom.prefix");
        });

        it("should accept hasTreeStructure input", () => {
            component.hasTreeStructure = true;
            expect(component.hasTreeStructure).toBeTrue();
        });

        it("should accept useCustomContent input", () => {
            component.useCustomContent = true;
            expect(component.useCustomContent).toBeTrue();
        });
    });

    describe("Constants", () => {
        it("should have all constant defined", () => {
            expect(component.all).toBe(Constants.ALL);
        });

        it("should have empty constant defined", () => {
            expect(component.empty).toBe(Constants.EMPTY);
        });
    });

    describe("getTabField", () => {
        it("should return the string when tab is a string", () => {
            const tab: FilterTab = "equipment";
            expect(component.getTabField(tab)).toBe("equipment");
        });

        it("should return the field property when tab is an object", () => {
            const tab: FilterTab = { field: "application", children: [] };
            expect(component.getTabField(tab)).toBe("application");
        });
    });

    describe("hasChildren", () => {
        it("should return false when tab is a string", () => {
            const tab: FilterTab = "equipment";
            expect(component.hasChildren(tab)).toBeFalse();
        });

        it("should return false when tab object has no children", () => {
            const tab: FilterTab = { field: "application" };
            expect(component.hasChildren(tab)).toBeFalse();
        });

        it("should return false when tab object has empty children array", () => {
            const tab: FilterTab = { field: "application", children: [] };
            expect(component.hasChildren(tab)).toBeFalse();
        });

        it("should return true when tab object has children", () => {
            const tab: FilterTab = { field: "application", children: ["child1"] };
            expect(component.hasChildren(tab)).toBeTrue();
        });
    });

    describe("filterActive", () => {
        it("should return true for empty array", () => {
            expect(component.filterActive([])).toBeTrue();
        });

        it("should return false for array with 'All'", () => {
            expect(component.filterActive(["All"])).toBeFalse();
        });

        it("should return true for array with values", () => {
            expect(component.filterActive(["value1"])).toBeTrue();
            expect(component.filterActive(["value1", "value2"])).toBeTrue();
        });
    });

    describe("localSelectedFilterNames computed", () => {
        it("should return active filter names", () => {
            component.localFilters = signal<Filter<any>>({
                field1: ["value1", "value2"],
                field2: ["All"],
                field3: [],
            });

            const activeNames = component.localSelectedFilterNames();

            expect(activeNames).toContain("field1");
            expect(activeNames).not.toContain("field2");
            expect(activeNames).toContain("field3");
        });
    });

    describe("isFilterApplied computed", () => {
        it("should return filter active status map", () => {
            component.localFilters = signal<Filter<any>>({
                field1: ["value1"],
                field2: ["All"],
                field3: [],
            });

            const activeStatus = component.isFilterApplied();

            expect(activeStatus["field1"]).toBeTrue();
            expect(activeStatus["field2"]).toBeFalse();
            expect(activeStatus["field3"]).toBeTrue();
        });
    });

    describe("closeFilterSidebar", () => {
        it("should set visible to false", () => {
            component.visible = true;
            component.closeFilterSidebar();
            expect(component.visible).toBeFalse();
        });

        it("should emit visibleChange event with false", () => {
            spyOn(component.visibleChange, "emit");
            component.closeFilterSidebar();
            expect(component.visibleChange.emit).toHaveBeenCalledWith(false);
        });

        it("should emit filtersCancelled event", () => {
            spyOn(component.filtersCancelled, "emit");
            component.closeFilterSidebar();
            expect(component.filtersCancelled.emit).toHaveBeenCalled();
        });
    });

    describe("applyFilters", () => {
        it("should emit filtersApplied event with local filters", () => {
            const filters = signal<Filter<any>>({ field1: ["value1"] });
            component.localFilters = filters;
            spyOn(component.filtersApplied, "emit");

            component.applyFilters();

            expect(component.filtersApplied.emit).toHaveBeenCalledWith(filters());
        });

        it("should set visible to false", () => {
            component.visible = true;
            component.applyFilters();
            expect(component.visible).toBeFalse();
        });

        it("should emit visibleChange event with false", () => {
            spyOn(component.visibleChange, "emit");
            component.applyFilters();
            expect(component.visibleChange.emit).toHaveBeenCalledWith(false);
        });
    });

    describe("onCheckboxChange", () => {
        it("should emit checkboxChanged event with correct data", () => {
            const selectedValues = ["value1", "value2"];
            const tab = "equipment";
            const selection = "type";
            spyOn(component.checkboxChanged, "emit");

            component.onCheckboxChange(selectedValues, tab, selection);

            expect(component.checkboxChanged.emit).toHaveBeenCalledWith({
                selectedValues,
                tab,
                selection,
            });
        });

        it("should handle empty selected values", () => {
            const selectedValues: any[] = [];
            const tab = "equipment";
            const selection = "type";
            spyOn(component.checkboxChanged, "emit");

            component.onCheckboxChange(selectedValues, tab, selection);

            expect(component.checkboxChanged.emit).toHaveBeenCalledWith({
                selectedValues: [],
                tab,
                selection,
            });
        });
    });

    describe("onTreeChange", () => {
        it("should emit treeChanged event with event and item", () => {
            const event = { checked: true };
            const item = { name: "item1" };
            spyOn(component.treeChanged, "emit");

            component.onTreeChange(event, item);

            expect(component.treeChanged.emit).toHaveBeenCalledWith({ event, item });
        });
    });

    describe("onTreeChildChanged", () => {
        it("should emit treeChildChanged event with event and item", () => {
            const event = { checked: false };
            const item = { name: "child1" };
            spyOn(component.treeChildChanged, "emit");

            component.onTreeChildChanged(event, item);

            expect(component.treeChildChanged.emit).toHaveBeenCalledWith({ event, item });
        });
    });

    describe("Integration scenarios", () => {
        it("should handle complete filter workflow", () => {
            // Setup
            component.visible = true;
            component.localFilters = signal<Filter<any>>({
                equipment: ["laptop", "server"],
            });
            spyOn(component.filtersApplied, "emit");
            spyOn(component.visibleChange, "emit");

            // Apply filters
            component.applyFilters();

            // Verify
            expect(component.visible).toBeFalse();
            expect(component.filtersApplied.emit).toHaveBeenCalled();
            expect(component.visibleChange.emit).toHaveBeenCalledWith(false);
        });

        it("should handle cancel workflow", () => {
            // Setup
            component.visible = true;
            spyOn(component.filtersCancelled, "emit");
            spyOn(component.visibleChange, "emit");

            // Cancel
            component.closeFilterSidebar();

            // Verify
            expect(component.visible).toBeFalse();
            expect(component.filtersCancelled.emit).toHaveBeenCalled();
            expect(component.visibleChange.emit).toHaveBeenCalledWith(false);
        });

        it("should work with complex tab structures", () => {
            const tabs: FilterTab[] = [
                "simpleTab",
                { field: "complexTab", children: ["child1", "child2"] },
            ];
            component.tabs = tabs;

            expect(component.getTabField(tabs[0])).toBe("simpleTab");
            expect(component.hasChildren(tabs[0])).toBeFalse();

            expect(component.getTabField(tabs[1])).toBe("complexTab");
            expect(component.hasChildren(tabs[1])).toBeTrue();
        });

        it("should handle multiple filter selections", () => {
            component.localFilters = signal<Filter<any>>({
                equipment: ["laptop", "desktop"],
                status: ["active"],
                country: ["All"],
                type: [],
            });

            const activeNames = component.localSelectedFilterNames();
            const activeStatus = component.isFilterApplied();

            expect(activeNames).toContain("equipment");
            expect(activeNames).toContain("status");
            expect(activeNames).not.toContain("country");
            expect(activeNames).toContain("type");

            expect(activeStatus["equipment"]).toBeTrue();
            expect(activeStatus["status"]).toBeTrue();
            expect(activeStatus["country"]).toBeFalse();
            expect(activeStatus["type"]).toBeTrue();
        });
    });
});
