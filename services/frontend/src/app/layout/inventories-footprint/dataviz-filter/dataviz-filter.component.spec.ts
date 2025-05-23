import { TestBed } from "@angular/core/testing";
import { TranslateService } from "@ngx-translate/core";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { DatavizFilterComponent } from "./dataviz-filter.component";

describe("DatavizFilterComponent", () => {
    let component: DatavizFilterComponent;
    let footprintStoreMock: jasmine.SpyObj<FootprintStoreService>;
    let filterServiceMock: jasmine.SpyObj<FilterService>;
    let translateServiceMock: jasmine.SpyObj<TranslateService>;

    beforeEach(() => {
        footprintStoreMock = jasmine.createSpyObj("FootprintStoreService", [
            "filters",
            "setFilters",
            "setCustomFilters",
        ]);
        filterServiceMock = jasmine.createSpyObj("FilterService", [
            "getUpdateSelectedValues",
        ]);
        translateServiceMock = jasmine.createSpyObj("TranslateService", ["instant"]);

        TestBed.configureTestingModule({
            providers: [
                DatavizFilterComponent,
                { provide: FootprintStoreService, useValue: footprintStoreMock },
                { provide: FilterService, useValue: filterServiceMock },
                { provide: TranslateService, useValue: translateServiceMock },
            ],
        });

        component = TestBed.inject(DatavizFilterComponent);
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should compute selectedFilterNames correctly", () => {
        footprintStoreMock.filters.and.returnValue({
            tab1: ["value1"],
            tab2: ["test"],
            tab3: ["All"],
        });
        translateServiceMock.instant.and.callFake((key: string) => key);

        const result = component.selectedFilterNames();

        expect(result).toBe(
            "inventories-footprint.filter-tabs.tab1, inventories-footprint.filter-tabs.tab2",
        );
    });

    it("should debounce onCheckboxChange calls", (done) => {
        spyOn(component, "onFilterSelected");
        const selectedValues = ["value1"];
        const tab = "tab1";
        const selection = "selection1";

        component.onCheckboxChange(selectedValues, tab, selection);

        setTimeout(() => {
            expect(component.onFilterSelected).toHaveBeenCalledWith(
                selectedValues,
                tab,
                selection,
            );
            done();
        }, 300); // Wait for debounceTime (200ms) + buffer
    });

    it("should determine if a filter is active", () => {
        expect(component.filterActive([])).toBeTrue();
        expect(component.filterActive(["All"])).toBeFalse();
        expect(component.filterActive(["value1"])).toBeTrue();
    });
});
