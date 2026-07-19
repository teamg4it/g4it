/// <reference types="jasmine" />
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

    it("should update filter selection immediately on checkbox change", () => {
        spyOn(component, "onFilterSelected");
        const selectedValues = ["value1"];
        const tab = "tab1";
        const selection = "selection1";

        component.onCheckboxChange(selectedValues, tab, selection);

        expect(component.onFilterSelected).toHaveBeenCalledWith(
            selectedValues,
            tab,
            selection,
        );
    });

    it("should determine if a filter is active", () => {
        expect(component.filterActive([])).toBeTrue();
        expect(component.filterActive(["All"])).toBeFalse();
        expect(component.filterActive(["value1"])).toBeTrue();
    });
});
